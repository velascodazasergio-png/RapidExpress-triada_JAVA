package com.rapidexpress.service;

import com.rapidexpress.dao.ClienteDAO;
import com.rapidexpress.dao.PaqueteDAO;
import com.rapidexpress.dao.RutaDAO;
import com.rapidexpress.excepcion.NegocioException;
import com.rapidexpress.integracion.Auditoria;
import com.rapidexpress.integracion.AuditoriaGateway;
import com.rapidexpress.model.Cliente;
import com.rapidexpress.model.EstadoPaquete;
import com.rapidexpress.model.Paquete;
import com.rapidexpress.util.ConexionBD;
import com.rapidexpress.util.TransaccionUtil;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Reglas de negocio de paquetes y clientes. (Persona 2)
 *
 * Igual que en FlotaService: aqui vive TODA la validacion; los DAO no validan
 * y las vistas tampoco.
 */
public class PaqueteService {

    private static final DateTimeFormatter FORMATO_CODIGO = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final String ALFABETO = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    private final PaqueteDAO paqueteDAO = new PaqueteDAO();
    private final ClienteDAO clienteDAO = new ClienteDAO();
    private final RutaDAO rutaDAO = new RutaDAO();
    private final AuditoriaGateway auditoria;

    /** Usa el gateway de auditoria vigente (lo fija la Persona 3 con Auditoria.usar). */
    public PaqueteService() {
        this(null);
    }

    public PaqueteService(AuditoriaGateway auditoria) {
        this.auditoria = auditoria;
    }

    private AuditoriaGateway auditoria() {
        return auditoria != null ? auditoria : Auditoria.actual();
    }

    // ================================================================= ALTAS

    public Paquete registrarPaquete(Paquete paquete) throws NegocioException {
        validarDatos(paquete);

        Paquete guardado = TransaccionUtil.ejecutar("registrar el paquete", cn -> {
            if (clienteDAO.buscarPorId(cn, paquete.getIdRemitente()) == null) {
                throw new NegocioException("No existe un cliente con id " + paquete.getIdRemitente()
                        + " para usarlo como remitente.");
            }
            if (clienteDAO.buscarPorId(cn, paquete.getIdDestinatario()) == null) {
                throw new NegocioException("No existe un cliente con id " + paquete.getIdDestinatario()
                        + " para usarlo como destinatario.");
            }
            paquete.setCodigoSeguimiento(generarCodigoUnico(cn));
            paquete.setEstado(EstadoPaquete.EN_BODEGA);
            paqueteDAO.insertar(cn, paquete);
            return paquete;
        });

        auditoria().registrar("CREAR_PAQUETE", "paquetes", String.valueOf(guardado.getIdPaquete()),
                "codigo=" + guardado.getCodigoSeguimiento() + ", peso=" + guardado.getPesoKg() + " kg");
        return guardado;
    }

    public Cliente registrarCliente(Cliente cliente) throws NegocioException {
        if (cliente.getNombre() == null || cliente.getNombre().isBlank()) {
            throw new NegocioException("El nombre del cliente es obligatorio.");
        }
        if (cliente.getDocumento() == null || cliente.getDocumento().isBlank()) {
            throw new NegocioException("El documento del cliente es obligatorio.");
        }
        return TransaccionUtil.ejecutar("registrar el cliente", cn -> {
            if (clienteDAO.buscarPorDocumento(cn, cliente.getDocumento()) != null) {
                throw new NegocioException("Ya existe un cliente con el documento " + cliente.getDocumento() + ".");
            }
            clienteDAO.insertar(cn, cliente);
            return cliente;
        });
    }

    // ============================================================= CONSULTAS

    public Paquete rastrear(String codigoSeguimiento) throws NegocioException {
        try (Connection cn = ConexionBD.obtenerConexion()) {
            Paquete paquete = paqueteDAO.buscarPorCodigo(cn, codigoSeguimiento.trim().toUpperCase());
            if (paquete == null) {
                throw new NegocioException("No existe un paquete con el codigo " + codigoSeguimiento + ".");
            }
            return paquete;
        } catch (SQLException e) {
            throw new NegocioException("Error de base de datos al consultar el paquete: " + e.getMessage(), e);
        }
    }

    public List<Paquete> listarTodos() throws NegocioException {
        try (Connection cn = ConexionBD.obtenerConexion()) {
            return paqueteDAO.listar(cn);
        } catch (SQLException e) {
            throw new NegocioException("Error de base de datos al listar paquetes: " + e.getMessage(), e);
        }
    }

    public List<Paquete> listarPorEstado(EstadoPaquete estado) throws NegocioException {
        try (Connection cn = ConexionBD.obtenerConexion()) {
            return paqueteDAO.listarPorEstado(cn, estado);
        } catch (SQLException e) {
            throw new NegocioException("Error de base de datos al listar paquetes: " + e.getMessage(), e);
        }
    }

    public List<Cliente> listarClientes() throws NegocioException {
        try (Connection cn = ConexionBD.obtenerConexion()) {
            return clienteDAO.listar(cn);
        } catch (SQLException e) {
            throw new NegocioException("Error de base de datos al listar clientes: " + e.getMessage(), e);
        }
    }

    // ========================================================== MODIFICACION

    /** Edicion de datos: solo mientras el paquete siga En Bodega. */
    public void actualizarDatos(Paquete paquete) throws NegocioException {
        validarDatos(paquete);
        TransaccionUtil.ejecutar("actualizar el paquete", cn -> {
            Paquete actual = paqueteDAO.buscarPorIdBloqueando(cn, paquete.getIdPaquete());
            if (actual == null) {
                throw new NegocioException("No existe un paquete con id " + paquete.getIdPaquete() + ".");
            }
            if (actual.getEstado() != EstadoPaquete.EN_BODEGA) {
                throw new NegocioException("Solo se pueden editar paquetes En Bodega; este esta "
                        + actual.getEstado().getEtiqueta() + ".");
            }
            paqueteDAO.actualizarDatos(cn, paquete);
            return null;
        });
        auditoria().registrar("EDITAR_PAQUETE", "paquetes", String.valueOf(paquete.getIdPaquete()),
                "datos actualizados");
    }

    /**
     * Cambio de estado manual validando el ciclo de vida.
     * Los cambios masivos (iniciar/cerrar ruta) los hace RutaService.
     */
    public void cambiarEstado(int idPaquete, EstadoPaquete nuevoEstado) throws NegocioException {
        EstadoPaquete anterior = TransaccionUtil.ejecutar("cambiar el estado del paquete", cn -> {
            Paquete paquete = paqueteDAO.buscarPorIdBloqueando(cn, idPaquete);
            if (paquete == null) {
                throw new NegocioException("No existe un paquete con id " + idPaquete + ".");
            }
            EstadoPaquete estadoActual = paquete.getEstado();
            if (estadoActual == nuevoEstado) {
                throw new NegocioException("El paquete ya se encuentra en estado " + nuevoEstado.getEtiqueta() + ".");
            }
            // No se toca a mano un paquete que pertenece a una ruta activa: su
            // estado lo maneja RutaService (crear / iniciar / entregar / cerrar).
            if (rutaDAO.paqueteYaTieneRutaActiva(cn, idPaquete)) {
                throw new NegocioException("El paquete " + paquete.getCodigoSeguimiento()
                        + " pertenece a una ruta activa; su estado lo gestiona el modulo de rutas.");
            }
            if (!estadoActual.puedePasarA(nuevoEstado)) {
                throw new NegocioException("Transicion invalida: " + estadoActual.getEtiqueta()
                        + " -> " + nuevoEstado.getEtiqueta() + ".");
            }
            if (!paqueteDAO.actualizarEstadoSi(cn, idPaquete, estadoActual, nuevoEstado)) {
                throw new NegocioException("El paquete fue modificado por otra terminal. Intentelo de nuevo.");
            }
            return estadoActual;
        });

        auditoria().registrar("CAMBIO_ESTADO_PAQUETE", "paquetes", String.valueOf(idPaquete),
                anterior.getEtiqueta() + " -> " + nuevoEstado.getEtiqueta());
    }

    // ================================================================= APOYO

    private void validarDatos(Paquete p) throws NegocioException {
        if (p.getDescripcion() == null || p.getDescripcion().isBlank()) {
            throw new NegocioException("La descripcion del paquete es obligatoria.");
        }
        if (p.getPesoKg() == null || p.getPesoKg().compareTo(BigDecimal.ZERO) <= 0) {
            throw new NegocioException("El peso debe ser mayor que cero.");
        }
        if (menorQueCero(p.getLargoCm()) || menorQueCero(p.getAnchoCm()) || menorQueCero(p.getAltoCm())) {
            throw new NegocioException("Las dimensiones no pueden ser negativas.");
        }
        if (p.getCiudadOrigen() == null || p.getCiudadOrigen().isBlank()
                || p.getCiudadDestino() == null || p.getCiudadDestino().isBlank()) {
            throw new NegocioException("La ciudad de origen y la de destino son obligatorias.");
        }
        if (p.getIdRemitente() == p.getIdDestinatario()) {
            throw new NegocioException("El remitente y el destinatario no pueden ser el mismo cliente.");
        }
    }

    private boolean menorQueCero(BigDecimal valor) {
        return valor != null && valor.compareTo(BigDecimal.ZERO) < 0;
    }

    /**
     * Codigo tipo RE-20260902-K7X2Q. Se verifica contra la BD y ademas la
     * columna es UNIQUE, asi que dos terminales no pueden crear el mismo.
     */
    private String generarCodigoUnico(Connection cn) throws SQLException, NegocioException {
        for (int intento = 0; intento < 10; intento++) {
            StringBuilder sufijo = new StringBuilder();
            for (int i = 0; i < 5; i++) {
                sufijo.append(ALFABETO.charAt(ThreadLocalRandom.current().nextInt(ALFABETO.length())));
            }
            String codigo = "RE-" + LocalDate.now().format(FORMATO_CODIGO) + "-" + sufijo;
            if (!paqueteDAO.existeCodigo(cn, codigo)) {
                return codigo;
            }
        }
        throw new NegocioException("No se pudo generar un codigo de seguimiento unico. Intentelo de nuevo.");
    }
}
