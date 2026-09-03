package com.rapidexpress.service;

import com.rapidexpress.dao.AsignacionDAO;
import com.rapidexpress.dao.ConductorDAO;
import com.rapidexpress.dao.VehiculoDAO;
import com.rapidexpress.excepcion.NegocioException;
import com.rapidexpress.model.*;
import com.rapidexpress.util.ConexionBD;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.List;

/**
 * Reglas de negocio del personal de conduccion y de la asignacion
 * conductor - vehiculo.
 */
public class ConductorService {

    private final ConductorDAO conductorDAO = new ConductorDAO();
    private final VehiculoDAO vehiculoDAO = new VehiculoDAO();
    private final AsignacionDAO asignacionDAO = new AsignacionDAO();

    // =================================================================
    // CRUD de conductores
    // =================================================================

    public int registrarConductor(Conductor c) throws NegocioException {
        validarDatos(c);
        try (Connection cn = ConexionBD.obtenerConexion()) {
            if (conductorDAO.buscarPorIdentificacion(cn, c.getIdentificacion()) != null) {
                throw new NegocioException("Ya existe un conductor con la identificacion " + c.getIdentificacion() + ".");
            }
            c.setEstado(EstadoConductor.ACTIVO);
            return conductorDAO.insertar(cn, c);
        } catch (SQLException e) {
            throw new NegocioException("Error de base de datos al registrar el conductor: " + e.getMessage(), e);
        }
    }

    public void actualizarConductor(Conductor c) throws NegocioException {
        validarDatos(c);
        try (Connection cn = ConexionBD.obtenerConexion()) {
            if (conductorDAO.buscarPorId(cn, c.getIdConductor()) == null) {
                throw new NegocioException("No existe un conductor con id " + c.getIdConductor() + ".");
            }
            Conductor otro = conductorDAO.buscarPorIdentificacion(cn, c.getIdentificacion());
            if (otro != null && otro.getIdConductor() != c.getIdConductor()) {
                throw new NegocioException("La identificacion " + c.getIdentificacion() + " ya pertenece a otro conductor.");
            }
            conductorDAO.actualizar(cn, c);
        } catch (SQLException e) {
            throw new NegocioException("Error de base de datos al actualizar el conductor: " + e.getMessage(), e);
        }
    }

    public void eliminarConductor(int idConductor) throws NegocioException {
        try (Connection cn = ConexionBD.obtenerConexion()) {
            Conductor c = obtenerObligatorio(cn, idConductor);
            if (c.getEstado() == EstadoConductor.EN_RUTA) {
                throw new NegocioException("No se puede eliminar un conductor que esta En Ruta.");
            }
            if (asignacionDAO.buscarVigentePorConductor(cn, idConductor) != null) {
                throw new NegocioException("El conductor tiene un vehiculo asignado. Libere la asignacion primero.");
            }
            conductorDAO.eliminar(cn, idConductor);
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new NegocioException("No se puede eliminar el conductor porque tiene historial de "
                    + "asignaciones asociado.", e);
        } catch (SQLException e) {
            throw new NegocioException("Error de base de datos al eliminar el conductor: " + e.getMessage(), e);
        }
    }

    public Conductor buscarPorId(int idConductor) throws NegocioException {
        try (Connection cn = ConexionBD.obtenerConexion()) {
            return conductorDAO.buscarPorId(cn, idConductor);
        } catch (SQLException e) {
            throw new NegocioException("Error de base de datos: " + e.getMessage(), e);
        }
    }

    public Conductor buscarPorIdentificacion(String identificacion) throws NegocioException {
        try (Connection cn = ConexionBD.obtenerConexion()) {
            return conductorDAO.buscarPorIdentificacion(cn, identificacion);
        } catch (SQLException e) {
            throw new NegocioException("Error de base de datos: " + e.getMessage(), e);
        }
    }

    public List<Conductor> listarConductores() throws NegocioException {
        try (Connection cn = ConexionBD.obtenerConexion()) {
            return conductorDAO.listarTodos(cn);
        } catch (SQLException e) {
            throw new NegocioException("Error de base de datos: " + e.getMessage(), e);
        }
    }

    public List<Conductor> listarPorEstado(EstadoConductor estado) throws NegocioException {
        try (Connection cn = ConexionBD.obtenerConexion()) {
            return conductorDAO.listarPorEstado(cn, estado);
        } catch (SQLException e) {
            throw new NegocioException("Error de base de datos: " + e.getMessage(), e);
        }
    }

    /** Cambio manual de estado. En Ruta solo lo asigna el sistema al iniciar una ruta. */
    public void cambiarEstadoConductor(int idConductor, EstadoConductor nuevoEstado) throws NegocioException {
        if (nuevoEstado == EstadoConductor.EN_RUTA) {
            throw new NegocioException("El estado En Ruta solo se asigna automaticamente al iniciar una ruta.");
        }
        try (Connection cn = ConexionBD.obtenerConexion()) {
            Conductor c = obtenerObligatorio(cn, idConductor);
            if (c.getEstado() == EstadoConductor.EN_RUTA) {
                throw new NegocioException("El conductor esta En Ruta; no se puede cambiar su estado manualmente.");
            }
            if (c.getEstado() == nuevoEstado) {
                throw new NegocioException("El conductor ya se encuentra en estado " + nuevoEstado + ".");
            }
            if (nuevoEstado != EstadoConductor.ACTIVO
                    && asignacionDAO.buscarVigentePorConductor(cn, idConductor) != null) {
                throw new NegocioException("El conductor tiene un vehiculo asignado. "
                        + "Libere la asignacion antes de ponerlo en " + nuevoEstado + ".");
            }
            conductorDAO.actualizarEstado(cn, idConductor, nuevoEstado);
        } catch (SQLException e) {
            throw new NegocioException("Error de base de datos al cambiar el estado: " + e.getMessage(), e);
        }
    }

    // =================================================================
    // Asignacion conductor - vehiculo
    // =================================================================

    /**
     * Asigna un conductor Activo a un vehiculo Disponible.
     * Reglas: ni el conductor ni el vehiculo pueden tener una asignacion vigente.
     * Se ejecuta en una transaccion con bloqueo de ambas filas para que dos
     * terminales no puedan asignar el mismo conductor al mismo tiempo.
     */
    public int asignarConductorAVehiculo(int idConductor, int idVehiculo) throws NegocioException {
        Connection cn = null;
        try {
            cn = ConexionBD.obtenerConexion();
            cn.setAutoCommit(false);

            // Orden de bloqueo fijo en todo el sistema: vehiculo antes que conductor
            // (igual que FlotaService.iniciarOperacion y RutaService.iniciarRuta).
            Vehiculo v = vehiculoDAO.buscarPorIdBloqueando(cn, idVehiculo);
            if (v == null) {
                throw new NegocioException("No existe un vehiculo con id " + idVehiculo + ".");
            }
            Conductor c = conductorDAO.buscarPorIdBloqueando(cn, idConductor);
            if (c == null) {
                throw new NegocioException("No existe un conductor con id " + idConductor + ".");
            }
            if (c.getEstado() != EstadoConductor.ACTIVO) {
                throw new NegocioException("El conductor " + c.getNombreCompleto()
                        + " no esta Activo (esta " + c.getEstado() + ").");
            }
            if (v.getEstado() != EstadoVehiculo.DISPONIBLE) {
                throw new NegocioException("El vehiculo " + v.getPlaca()
                        + " no esta Disponible (esta " + v.getEstado() + ").");
            }
            if (asignacionDAO.buscarVigentePorConductor(cn, idConductor) != null) {
                throw new NegocioException("El conductor ya esta asignado a otro vehiculo.");
            }
            if (asignacionDAO.buscarVigentePorVehiculo(cn, idVehiculo) != null) {
                throw new NegocioException("El vehiculo ya tiene un conductor asignado.");
            }
            int id = asignacionDAO.insertar(cn, new Asignacion(idConductor, idVehiculo));
            cn.commit();
            return id;
        } catch (SQLException e) {
            revertir(cn);
            throw new NegocioException("Error de base de datos al asignar el conductor: " + e.getMessage(), e);
        } catch (NegocioException e) {
            revertir(cn);
            throw e;
        } finally {
            cerrar(cn);
        }
    }

    /** Cierra la asignacion vigente de un conductor. */
    public void liberarAsignacionDeConductor(int idConductor) throws NegocioException {
        try (Connection cn = ConexionBD.obtenerConexion()) {
            Asignacion a = asignacionDAO.buscarVigentePorConductor(cn, idConductor);
            if (a == null) {
                throw new NegocioException("El conductor no tiene ninguna asignacion vigente.");
            }
            Vehiculo v = vehiculoDAO.buscarPorId(cn, a.getIdVehiculo());
            if (v != null && v.getEstado() == EstadoVehiculo.EN_RUTA) {
                throw new NegocioException("No se puede liberar la asignacion: el vehiculo esta En Ruta.");
            }
            asignacionDAO.cerrar(cn, a.getIdAsignacion());
        } catch (SQLException e) {
            throw new NegocioException("Error de base de datos al liberar la asignacion: " + e.getMessage(), e);
        }
    }

    public List<Asignacion> listarAsignacionesVigentes() throws NegocioException {
        try (Connection cn = ConexionBD.obtenerConexion()) {
            return asignacionDAO.listarVigentes(cn);
        } catch (SQLException e) {
            throw new NegocioException("Error de base de datos: " + e.getMessage(), e);
        }
    }

    public List<Asignacion> historialPorVehiculo(int idVehiculo) throws NegocioException {
        try (Connection cn = ConexionBD.obtenerConexion()) {
            return asignacionDAO.listarHistorialPorVehiculo(cn, idVehiculo);
        } catch (SQLException e) {
            throw new NegocioException("Error de base de datos: " + e.getMessage(), e);
        }
    }

    // =================================================================
    // API PUBLICA PARA OTROS MODULOS
    // =================================================================

    /** Conductor que actualmente maneja ese vehiculo, o null si no hay ninguno. */
    public Conductor obtenerConductorAsignadoA(int idVehiculo) throws NegocioException {
        try (Connection cn = ConexionBD.obtenerConexion()) {
            return obtenerConductorAsignadoA(cn, idVehiculo);
        } catch (SQLException e) {
            throw new NegocioException("Error de base de datos: " + e.getMessage(), e);
        }
    }

    /** Igual que el anterior pero sobre la conexion recibida (para transacciones ajenas). */
    public Conductor obtenerConductorAsignadoA(Connection cn, int idVehiculo) throws SQLException {
        Asignacion a = asignacionDAO.buscarVigentePorVehiculo(cn, idVehiculo);
        return a == null ? null : conductorDAO.buscarPorId(cn, a.getIdConductor());
    }

    /** Conductores Activos con vehiculo asignado: candidatos para una hoja de ruta. */
    public List<Asignacion> obtenerParejasDisponiblesParaRuta() throws NegocioException {
        return listarAsignacionesVigentes();
    }

    // =================================================================
    // Apoyo interno
    // =================================================================

    private Conductor obtenerObligatorio(Connection cn, int idConductor) throws SQLException, NegocioException {
        Conductor c = conductorDAO.buscarPorId(cn, idConductor);
        if (c == null) {
            throw new NegocioException("No existe un conductor con id " + idConductor + ".");
        }
        return c;
    }

    private void validarDatos(Conductor c) throws NegocioException {
        if (c.getIdentificacion() == null || !c.getIdentificacion().matches("[0-9]{6,15}")) {
            throw new NegocioException("La identificacion debe ser numerica (entre 6 y 15 digitos).");
        }
        if (c.getNombreCompleto() == null || c.getNombreCompleto().isBlank()) {
            throw new NegocioException("El nombre completo es obligatorio.");
        }
        if (c.getTipoLicencia() == null) {
            throw new NegocioException("El tipo de licencia es obligatorio.");
        }
        if (c.getContacto() == null || !c.getContacto().matches("[0-9+ -]{7,20}")) {
            throw new NegocioException("El numero de contacto no es valido.");
        }
    }

    private void revertir(Connection cn) {
        if (cn != null) {
            try { cn.rollback(); } catch (SQLException ignored) { }
        }
    }

    private void cerrar(Connection cn) {
        if (cn != null) {
            try { cn.setAutoCommit(true); cn.close(); } catch (SQLException ignored) { }
        }
    }
}
