package com.rapidexpress.service;

import com.rapidexpress.controller.ConductorController;
import com.rapidexpress.controller.FlotaController;
import com.rapidexpress.dao.PaqueteDAO;
import com.rapidexpress.dao.RutaDAO;
import com.rapidexpress.excepcion.NegocioException;
import com.rapidexpress.integracion.Auditoria;
import com.rapidexpress.integracion.AuditoriaGateway;
import com.rapidexpress.model.Conductor;
import com.rapidexpress.model.EstadoConductor;
import com.rapidexpress.model.EstadoPaquete;
import com.rapidexpress.model.EstadoRuta;
import com.rapidexpress.model.EstadoVehiculo;
import com.rapidexpress.model.Paquete;
import com.rapidexpress.model.Ruta;
import com.rapidexpress.model.Vehiculo;
import com.rapidexpress.util.ConexionBD;
import com.rapidexpress.util.TransaccionUtil;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Nucleo del modulo 2: planificacion, inicio, seguimiento y cierre de rutas.
 *
 * ---------------------------------------------------------------------------
 * FRONTERA CON EL MODULO DE FLOTA (Persona 1)
 * ---------------------------------------------------------------------------
 * Este servicio NUNCA hace UPDATE sobre vehiculos ni conductores. Para eso usa
 * la API que expone la Persona 1:
 *
 *   FlotaController.buscarVehiculoBloqueando(cn, id)      -> capacidad y estado (bloqueado)
 *   FlotaController.obtenerVehiculosDisponibles()         -> lista para el menu
 *   FlotaController.iniciarOperacion(cn, idVehiculo)      -> vehiculo y conductor a "En Ruta"
 *   FlotaController.finalizarOperacion(cn, idVehiculo)    -> vuelven a Disponible/Activo
 *   ConductorController.obtenerConductorAsignadoA(cn, id) -> conductor de la hoja de ruta
 *
 * El conductor NO se elige a mano: sale de la asignacion vigente. Por eso
 * crearHojaDeRuta pide solo el vehiculo.
 *
 * ---------------------------------------------------------------------------
 * ESTRATEGIA DE CONCURRENCIA (requisito del enunciado)
 * ---------------------------------------------------------------------------
 *  1. Transaccion unica por operacion, con rollback si algo falla
 *     (util/TransaccionUtil).
 *  2. Bloqueo pesimista con SELECT ... FOR UPDATE. Orden fijo de bloqueo:
 *     ruta -> vehiculo -> conductor -> paquetes por id ASCENDENTE. Bloquear
 *     siempre en el mismo orden es lo que evita los deadlocks entre terminales.
 *  3. Escritura condicional: los UPDATE llevan "AND estado = <esperado>" y se
 *     revisa cuantas filas cambiaron. Si es 0, otra terminal gano la carrera y
 *     la operacion se aborta completa.
 *  4. El cambio de estado de la flota (iniciar/cerrar ruta) entra en la MISMA
 *     transaccion, usando las variantes iniciarOperacion(cn, ...) /
 *     finalizarOperacion(cn, ...). Todo es una sola unidad ACID: ya no hace
 *     falta logica de compensacion.
 */
public class RutaService {

    private static final DateTimeFormatter FORMATO_CODIGO = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final RutaDAO rutaDAO = new RutaDAO();
    private final PaqueteDAO paqueteDAO = new PaqueteDAO();
    private final FlotaController flotaController = new FlotaController();
    private final ConductorController conductorController = new ConductorController();
    private final AuditoriaGateway auditoria;

    /** Usa el gateway de auditoria vigente (lo fija la Persona 3 con Auditoria.usar). */
    public RutaService() {
        this(null);
    }

    public RutaService(AuditoriaGateway auditoria) {
        this.auditoria = auditoria;
    }

    private AuditoriaGateway auditoria() {
        return auditoria != null ? auditoria : Auditoria.actual();
    }

    // ================================================= 1. CREAR HOJA DE RUTA

    /**
     * Crea la hoja de ruta diaria validando que la carga no exceda la capacidad
     * del vehiculo. El conductor se toma de la asignacion vigente del vehiculo.
     * Todo (lectura bloqueada de flota + insercion de la ruta) ocurre dentro de
     * una unica transaccion.
     */
    public Ruta crearHojaDeRuta(LocalDate fecha, int idVehiculo, List<Integer> idsPaquetes,
                                String observaciones) throws NegocioException {

        if (fecha == null) {
            throw new NegocioException("La fecha de la ruta es obligatoria.");
        }
        if (fecha.isBefore(LocalDate.now())) {
            throw new NegocioException("La fecha de la ruta no puede ser anterior a hoy.");
        }
        if (idsPaquetes == null || idsPaquetes.isEmpty()) {
            throw new NegocioException("Debe asignar al menos un paquete a la ruta.");
        }
        List<Integer> ids = new ArrayList<>(new LinkedHashSet<>(idsPaquetes));
        Collections.sort(ids); // orden fijo de bloqueo

        Ruta ruta = TransaccionUtil.ejecutar("crear la hoja de ruta", cn -> {

            // --- Flota (modulo de la Persona 1), dentro de ESTA transaccion ---
            Vehiculo vehiculo = flotaController.buscarVehiculoBloqueando(cn, idVehiculo);
            if (vehiculo == null) {
                throw new NegocioException("No existe un vehiculo con id " + idVehiculo + ".");
            }
            if (vehiculo.getEstado() != EstadoVehiculo.DISPONIBLE) {
                throw new NegocioException("El vehiculo " + vehiculo.getPlaca() + " no esta Disponible (esta "
                        + vehiculo.getEstado() + ").");
            }
            Conductor conductor = conductorController.obtenerConductorAsignadoA(cn, idVehiculo);
            if (conductor == null) {
                throw new NegocioException("El vehiculo " + vehiculo.getPlaca() + " no tiene conductor asignado. "
                        + "Asignele uno desde el menu de conductores antes de crear la ruta.");
            }
            if (conductor.getEstado() != EstadoConductor.ACTIVO) {
                throw new NegocioException("El conductor " + conductor.getNombreCompleto() + " no esta Activo (esta "
                        + conductor.getEstado() + ").");
            }
            if (rutaDAO.vehiculoTieneRutaActivaEnFecha(cn, idVehiculo, fecha)) {
                throw new NegocioException("El vehiculo " + vehiculo.getPlaca()
                        + " ya tiene una hoja de ruta activa para el " + fecha + ".");
            }
            if (rutaDAO.conductorTieneRutaActivaEnFecha(cn, conductor.getIdConductor(), fecha)) {
                throw new NegocioException("El conductor " + conductor.getNombreCompleto()
                        + " ya tiene una hoja de ruta activa para el " + fecha + ".");
            }

            BigDecimal pesoTotal = BigDecimal.ZERO;
            List<Paquete> seleccionados = new ArrayList<>();

            for (Integer idPaquete : ids) {
                Paquete paquete = paqueteDAO.buscarPorIdBloqueando(cn, idPaquete);
                if (paquete == null) {
                    throw new NegocioException("No existe un paquete con id " + idPaquete + ".");
                }
                if (paquete.getEstado() != EstadoPaquete.EN_BODEGA) {
                    throw new NegocioException("El paquete " + paquete.getCodigoSeguimiento()
                            + " no esta En Bodega (esta " + paquete.getEstado().getEtiqueta() + ").");
                }
                if (rutaDAO.paqueteYaTieneRutaActiva(cn, idPaquete)) {
                    throw new NegocioException("El paquete " + paquete.getCodigoSeguimiento()
                            + " ya pertenece a otra ruta activa.");
                }
                pesoTotal = pesoTotal.add(paquete.getPesoKg());
                seleccionados.add(paquete);
            }

            // ---- REGLA CENTRAL: capacidad de carga del vehiculo ----
            if (pesoTotal.compareTo(vehiculo.getCapacidadCargaKg()) > 0) {
                throw new NegocioException("Capacidad excedida: " + pesoTotal + " kg seleccionados y el vehiculo "
                        + vehiculo.getPlaca() + " soporta " + vehiculo.getCapacidadCargaKg() + " kg (exceso de "
                        + pesoTotal.subtract(vehiculo.getCapacidadCargaKg()) + " kg).");
            }

            Ruta nueva = new Ruta();
            nueva.setCodigo(generarCodigoRuta(cn, fecha));
            nueva.setFecha(fecha);
            nueva.setIdVehiculo(idVehiculo);
            nueva.setIdConductor(conductor.getIdConductor());
            nueva.setEstado(EstadoRuta.PLANIFICADA);
            nueva.setObservaciones(observaciones);
            rutaDAO.insertar(cn, nueva);

            int orden = 1;
            for (Paquete paquete : seleccionados) {
                rutaDAO.agregarPaquete(cn, nueva.getIdRuta(), paquete.getIdPaquete(), orden++);
                boolean cambiado = paqueteDAO.actualizarEstadoSi(cn, paquete.getIdPaquete(),
                        EstadoPaquete.EN_BODEGA, EstadoPaquete.ASIGNADO_A_RUTA);
                if (!cambiado) {
                    throw new NegocioException("El paquete " + paquete.getCodigoSeguimiento()
                            + " fue tomado por otra terminal. No se creo la ruta.");
                }
                paquete.setEstado(EstadoPaquete.ASIGNADO_A_RUTA);
            }

            nueva.setPaquetes(seleccionados);
            nueva.setPlacaVehiculo(vehiculo.getPlaca());
            nueva.setNombreConductor(conductor.getNombreCompleto());
            nueva.setCapacidadVehiculoKg(vehiculo.getCapacidadCargaKg());
            return nueva;
        });

        auditoria().registrar("CREAR_RUTA", "rutas", String.valueOf(ruta.getIdRuta()),
                "codigo=" + ruta.getCodigo() + ", vehiculo=" + idVehiculo
                        + ", conductor=" + ruta.getIdConductor()
                        + ", paquetes=" + ruta.getPaquetes().size() + ", peso=" + ruta.getPesoTotalKg() + " kg");
        return ruta;
    }

    // ======================================================= 2. INICIAR RUTA

    /**
     * Inicia la ruta en UNA sola transaccion: bloquea la ruta, pone vehiculo y
     * conductor En Ruta (via FlotaController sobre la misma conexion), pasa los
     * paquetes a En Transito y la ruta a En Curso. Si algo falla, rollback total.
     */
    public Ruta iniciarRuta(int idRuta) throws NegocioException {
        Ruta previa = consultarRutaConPaquetes(idRuta);
        if (previa.getEstado() != EstadoRuta.PLANIFICADA) {
            throw new NegocioException("Solo se pueden iniciar rutas Planificadas; esta esta "
                    + previa.getEstado().getEtiqueta() + ".");
        }
        if (previa.getPaquetes().isEmpty()) {
            throw new NegocioException("La ruta " + previa.getCodigo() + " no tiene paquetes asignados.");
        }

        TransaccionUtil.ejecutar("iniciar la ruta", cn -> {
            Ruta actual = rutaDAO.buscarPorIdBloqueando(cn, idRuta);
            if (actual == null) {
                throw new NegocioException("No existe una ruta con id " + idRuta + ".");
            }
            if (actual.getEstado() != EstadoRuta.PLANIFICADA) {
                throw new NegocioException("Otra terminal ya inicio o cancelo esta ruta.");
            }

            // Paso 1: flota, en LA MISMA transaccion.
            int idConductorReal = flotaController.iniciarOperacion(cn, actual.getIdVehiculo());

            // Paso 2: paquetes y ruta.
            for (Integer idPaquete : rutaDAO.idsPaquetesDeRuta(cn, idRuta)) {
                Paquete paquete = paqueteDAO.buscarPorIdBloqueando(cn, idPaquete);
                if (paquete.getEstado() != EstadoPaquete.ASIGNADO_A_RUTA) {
                    throw new NegocioException("El paquete " + paquete.getCodigoSeguimiento()
                            + " no esta Asignado a Ruta (esta " + paquete.getEstado().getEtiqueta() + ").");
                }
                if (!paqueteDAO.actualizarEstadoSi(cn, idPaquete,
                        EstadoPaquete.ASIGNADO_A_RUTA, EstadoPaquete.EN_TRANSITO)) {
                    throw new NegocioException("El paquete " + paquete.getCodigoSeguimiento()
                            + " fue modificado por otra terminal.");
                }
            }

            if (!rutaDAO.actualizarEstadoSi(cn, idRuta, EstadoRuta.PLANIFICADA, EstadoRuta.EN_CURSO)) {
                throw new NegocioException("Otra terminal ya inicio esta ruta.");
            }
            // Si la asignacion cambio desde que se planifico, se deja constancia
            // del conductor que realmente salio.
            if (actual.getIdConductor() != idConductorReal) {
                rutaDAO.actualizarConductor(cn, idRuta, idConductorReal);
            }
            return null;
        });

        auditoria().registrar("INICIAR_RUTA", "rutas", String.valueOf(idRuta),
                "codigo=" + previa.getCodigo() + " -> En Curso");
        return consultarRutaConPaquetes(idRuta);
    }

    // ============================================ 3. SEGUIMIENTO DE ENTREGAS

    /** Marca un paquete como Entregado. Debe venir de En Transito. */
    public void registrarEntrega(String codigoSeguimiento) throws NegocioException {
        moverPaqueteEnTransito(codigoSeguimiento, EstadoPaquete.ENTREGADO, "ENTREGAR_PAQUETE");
    }

    /** Marca un paquete como Devuelto (no se pudo entregar). */
    public void registrarDevolucion(String codigoSeguimiento) throws NegocioException {
        moverPaqueteEnTransito(codigoSeguimiento, EstadoPaquete.DEVUELTO, "DEVOLVER_PAQUETE");
    }

    private void moverPaqueteEnTransito(String codigo, EstadoPaquete destino, String accion)
            throws NegocioException {
        String codigoNormalizado = codigo.trim().toUpperCase();

        Integer idPaquete = TransaccionUtil.ejecutar("actualizar el paquete", cn -> {
            Paquete paquete = paqueteDAO.buscarPorCodigo(cn, codigoNormalizado);
            if (paquete == null) {
                throw new NegocioException("No existe un paquete con el codigo " + codigoNormalizado + ".");
            }
            // Se relee bloqueando: entre la busqueda y el update pudo cambiar.
            Paquete bloqueado = paqueteDAO.buscarPorIdBloqueando(cn, paquete.getIdPaquete());
            if (bloqueado.getEstado() != EstadoPaquete.EN_TRANSITO) {
                throw new NegocioException("El paquete " + codigoNormalizado + " no esta En Transito (esta "
                        + bloqueado.getEstado().getEtiqueta() + ").");
            }
            if (!paqueteDAO.actualizarEstadoSi(cn, bloqueado.getIdPaquete(), EstadoPaquete.EN_TRANSITO, destino)) {
                throw new NegocioException("Otra terminal ya actualizo este paquete.");
            }
            return bloqueado.getIdPaquete();
        });

        auditoria().registrar(accion, "paquetes", String.valueOf(idPaquete),
                "codigo=" + codigoNormalizado + " -> " + destino.getEtiqueta());
    }

    // =============================================== 4. CERRAR O CANCELAR

    /**
     * Finaliza la ruta: exige que ningun paquete siga pendiente y devuelve el
     * vehiculo a Disponible y el conductor a Activo, todo en la misma transaccion.
     */
    public void finalizarRuta(int idRuta) throws NegocioException {
        Ruta ruta = TransaccionUtil.ejecutar("finalizar la ruta", cn -> {
            Ruta actual = rutaDAO.buscarPorIdBloqueando(cn, idRuta);
            if (actual == null) {
                throw new NegocioException("No existe una ruta con id " + idRuta + ".");
            }
            if (actual.getEstado() != EstadoRuta.EN_CURSO) {
                throw new NegocioException("Solo se pueden finalizar rutas En Curso; esta esta "
                        + actual.getEstado().getEtiqueta() + ".");
            }
            int pendientes = rutaDAO.contarPaquetesPendientes(cn, idRuta);
            if (pendientes > 0) {
                throw new NegocioException("Quedan " + pendientes
                        + " paquete(s) sin marcar como Entregado o Devuelto.");
            }
            if (!rutaDAO.actualizarEstadoSi(cn, idRuta, EstadoRuta.EN_CURSO, EstadoRuta.FINALIZADA)) {
                throw new NegocioException("Otra terminal ya cerro esta ruta.");
            }
            // Liberar la flota dentro de la misma transaccion.
            flotaController.finalizarOperacion(cn, actual.getIdVehiculo());
            return actual;
        });

        auditoria().registrar("FINALIZAR_RUTA", "rutas", String.valueOf(idRuta),
                "codigo=" + ruta.getCodigo() + " -> Finalizada");
    }

    /** Cancela una ruta que aun no inicia y devuelve los paquetes a bodega. */
    public void cancelarRuta(int idRuta, String motivo) throws NegocioException {
        Ruta ruta = TransaccionUtil.ejecutar("cancelar la ruta", cn -> {
            Ruta actual = rutaDAO.buscarPorIdBloqueando(cn, idRuta);
            if (actual == null) {
                throw new NegocioException("No existe una ruta con id " + idRuta + ".");
            }
            if (actual.getEstado() != EstadoRuta.PLANIFICADA) {
                throw new NegocioException("Solo se pueden cancelar rutas Planificadas; esta esta "
                        + actual.getEstado().getEtiqueta() + ".");
            }
            for (Integer idPaquete : rutaDAO.idsPaquetesDeRuta(cn, idRuta)) {
                paqueteDAO.buscarPorIdBloqueando(cn, idPaquete);
                paqueteDAO.actualizarEstadoSi(cn, idPaquete,
                        EstadoPaquete.ASIGNADO_A_RUTA, EstadoPaquete.EN_BODEGA);
            }
            if (!rutaDAO.actualizarEstadoSi(cn, idRuta, EstadoRuta.PLANIFICADA, EstadoRuta.CANCELADA)) {
                throw new NegocioException("Otra terminal ya modifico esta ruta.");
            }
            return actual;
        });

        auditoria().registrar("CANCELAR_RUTA", "rutas", String.valueOf(idRuta),
                "codigo=" + ruta.getCodigo() + ", motivo=" + motivo);
    }

    // ============================================================ CONSULTAS

    public List<Ruta> listarRutasActivas() throws NegocioException {
        return listarRutasPorEstado(EstadoRuta.EN_CURSO);
    }

    public List<Ruta> listarRutasPorEstado(EstadoRuta estado) throws NegocioException {
        try (Connection cn = ConexionBD.obtenerConexion()) {
            List<Ruta> rutas = rutaDAO.listarPorEstado(cn, estado);
            for (Ruta ruta : rutas) {
                ruta.setPaquetes(paqueteDAO.listarPorRuta(cn, ruta.getIdRuta()));
            }
            return rutas;
        } catch (SQLException e) {
            throw new NegocioException("Error de base de datos al listar rutas: " + e.getMessage(), e);
        }
    }

    public Ruta consultarRutaConPaquetes(int idRuta) throws NegocioException {
        try (Connection cn = ConexionBD.obtenerConexion()) {
            Ruta ruta = rutaDAO.buscarPorId(cn, idRuta);
            if (ruta == null) {
                throw new NegocioException("No existe una ruta con id " + idRuta + ".");
            }
            ruta.setPaquetes(paqueteDAO.listarPorRuta(cn, idRuta));
            return ruta;
        } catch (SQLException e) {
            throw new NegocioException("Error de base de datos al consultar la ruta: " + e.getMessage(), e);
        }
    }

    /** Vehiculos que pueden usarse hoy: disponibles Y con conductor asignado activo. */
    public List<Vehiculo> listarVehiculosOperativos() throws NegocioException {
        List<Vehiculo> operativos = new ArrayList<>();
        for (Vehiculo v : flotaController.obtenerVehiculosDisponibles()) {
            Conductor c = conductorController.obtenerConductorAsignadoA(v.getIdVehiculo());
            if (c != null && c.getEstado() == EstadoConductor.ACTIVO) {
                operativos.add(v);
            }
        }
        return operativos;
    }

    public Conductor conductorDe(int idVehiculo) throws NegocioException {
        return conductorController.obtenerConductorAsignadoA(idVehiculo);
    }

    // ================================================================ APOYO

    private String generarCodigoRuta(Connection cn, LocalDate fecha) throws SQLException, NegocioException {
        for (int intento = 0; intento < 10; intento++) {
            String codigo = "RUT-" + fecha.format(FORMATO_CODIGO) + "-"
                    + String.format("%04d", ThreadLocalRandom.current().nextInt(10000));
            if (rutaDAO.buscarPorCodigo(cn, codigo) == null) {
                return codigo;
            }
        }
        throw new NegocioException("No se pudo generar un codigo de ruta unico. Intentelo de nuevo.");
    }
}
