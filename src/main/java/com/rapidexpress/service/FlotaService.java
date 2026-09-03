package com.rapidexpress.service;

import com.rapidexpress.dao.AsignacionDAO;
import com.rapidexpress.dao.ConductorDAO;
import com.rapidexpress.dao.MantenimientoDAO;
import com.rapidexpress.dao.VehiculoDAO;
import com.rapidexpress.excepcion.NegocioException;
import com.rapidexpress.model.*;
import com.rapidexpress.util.ConexionBD;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;

/**
 * Reglas de negocio de la flota de vehiculos y su mantenimiento.
 *
 * Aqui vive TODA la validacion: los DAO no validan y las vistas tampoco.
 */
public class FlotaService {

    private final VehiculoDAO vehiculoDAO = new VehiculoDAO();
    private final MantenimientoDAO mantenimientoDAO = new MantenimientoDAO();
    private final AsignacionDAO asignacionDAO = new AsignacionDAO();
    private final ConductorDAO conductorDAO = new ConductorDAO();

    // =================================================================
    // CRUD de vehiculos
    // =================================================================

    public int registrarVehiculo(Vehiculo v) throws NegocioException {
        validarDatosVehiculo(v);
        try (Connection cn = ConexionBD.obtenerConexion()) {
            if (vehiculoDAO.buscarPorPlaca(cn, v.getPlaca()) != null) {
                throw new NegocioException("Ya existe un vehiculo registrado con la placa " + v.getPlaca() + ".");
            }
            v.setEstado(EstadoVehiculo.DISPONIBLE);
            return vehiculoDAO.insertar(cn, v);
        } catch (SQLException e) {
            throw new NegocioException("Error de base de datos al registrar el vehiculo: " + e.getMessage(), e);
        }
    }

    public void actualizarVehiculo(Vehiculo v) throws NegocioException {
        validarDatosVehiculo(v);
        try (Connection cn = ConexionBD.obtenerConexion()) {
            Vehiculo actual = vehiculoDAO.buscarPorId(cn, v.getIdVehiculo());
            if (actual == null) {
                throw new NegocioException("No existe un vehiculo con id " + v.getIdVehiculo() + ".");
            }
            Vehiculo conMismaPlaca = vehiculoDAO.buscarPorPlaca(cn, v.getPlaca());
            if (conMismaPlaca != null && conMismaPlaca.getIdVehiculo() != v.getIdVehiculo()) {
                throw new NegocioException("La placa " + v.getPlaca() + " ya pertenece a otro vehiculo.");
            }
            vehiculoDAO.actualizar(cn, v);
        } catch (SQLException e) {
            throw new NegocioException("Error de base de datos al actualizar el vehiculo: " + e.getMessage(), e);
        }
    }

    public void eliminarVehiculo(int idVehiculo) throws NegocioException {
        try (Connection cn = ConexionBD.obtenerConexion()) {
            Vehiculo v = obtenerObligatorio(cn, idVehiculo);
            if (v.getEstado() == EstadoVehiculo.EN_RUTA) {
                throw new NegocioException("No se puede eliminar un vehiculo que esta En Ruta.");
            }
            if (asignacionDAO.buscarVigentePorVehiculo(cn, idVehiculo) != null) {
                throw new NegocioException("El vehiculo tiene un conductor asignado. Libere la asignacion primero.");
            }
            vehiculoDAO.eliminar(cn, idVehiculo);
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new NegocioException("No se puede eliminar el vehiculo porque tiene historial asociado "
                    + "(asignaciones o mantenimientos).", e);
        } catch (SQLException e) {
            throw new NegocioException("Error de base de datos al eliminar el vehiculo: " + e.getMessage(), e);
        }
    }

    public Vehiculo buscarPorId(int idVehiculo) throws NegocioException {
        try (Connection cn = ConexionBD.obtenerConexion()) {
            return vehiculoDAO.buscarPorId(cn, idVehiculo);
        } catch (SQLException e) {
            throw new NegocioException("Error de base de datos: " + e.getMessage(), e);
        }
    }

    public Vehiculo buscarPorPlaca(String placa) throws NegocioException {
        try (Connection cn = ConexionBD.obtenerConexion()) {
            return vehiculoDAO.buscarPorPlaca(cn, placa);
        } catch (SQLException e) {
            throw new NegocioException("Error de base de datos: " + e.getMessage(), e);
        }
    }

    public List<Vehiculo> listarVehiculos() throws NegocioException {
        try (Connection cn = ConexionBD.obtenerConexion()) {
            return vehiculoDAO.listarTodos(cn);
        } catch (SQLException e) {
            throw new NegocioException("Error de base de datos: " + e.getMessage(), e);
        }
    }

    public List<Vehiculo> listarPorEstado(EstadoVehiculo estado) throws NegocioException {
        try (Connection cn = ConexionBD.obtenerConexion()) {
            return vehiculoDAO.listarPorEstado(cn, estado);
        } catch (SQLException e) {
            throw new NegocioException("Error de base de datos: " + e.getMessage(), e);
        }
    }

    /**
     * Cambio de estado manual desde el menu de flota.
     * No permite mover manualmente un vehiculo que esta En Ruta ni ponerlo
     * En Ruta a mano: eso lo hace el modulo de rutas con iniciarOperacion().
     */
    public void cambiarEstadoVehiculo(int idVehiculo, EstadoVehiculo nuevoEstado) throws NegocioException {
        try (Connection cn = ConexionBD.obtenerConexion()) {
            Vehiculo v = obtenerObligatorio(cn, idVehiculo);
            if (v.getEstado() == nuevoEstado) {
                throw new NegocioException("El vehiculo ya se encuentra en estado " + nuevoEstado + ".");
            }
            if (v.getEstado() == EstadoVehiculo.EN_RUTA) {
                throw new NegocioException("El vehiculo esta En Ruta; el cambio de estado lo hace el modulo de rutas.");
            }
            if (nuevoEstado == EstadoVehiculo.EN_RUTA) {
                throw new NegocioException("El estado En Ruta solo se asigna al iniciar una ruta.");
            }
            if (nuevoEstado == EstadoVehiculo.DISPONIBLE
                    && mantenimientoDAO.contarAbiertosPorVehiculo(cn, idVehiculo) > 0) {
                throw new NegocioException("El vehiculo tiene un mantenimiento sin finalizar. Cierrelo primero.");
            }
            vehiculoDAO.actualizarEstado(cn, idVehiculo, nuevoEstado);
        } catch (SQLException e) {
            throw new NegocioException("Error de base de datos al cambiar el estado: " + e.getMessage(), e);
        }
    }

    // =================================================================
    // Mantenimientos
    // =================================================================

    /** Registra un mantenimiento y deja el vehiculo En Mantenimiento (transaccion). */
    public int registrarMantenimiento(Mantenimiento m) throws NegocioException {
        if (m.getDescripcion() == null || m.getDescripcion().isBlank()) {
            throw new NegocioException("La descripcion del mantenimiento es obligatoria.");
        }
        if (m.getFechaInicio() == null) {
            m.setFechaInicio(LocalDate.now());
        }
        Connection cn = null;
        try {
            cn = ConexionBD.obtenerConexion();
            cn.setAutoCommit(false);

            Vehiculo v = vehiculoDAO.buscarPorIdBloqueando(cn, m.getIdVehiculo());
            if (v == null) {
                throw new NegocioException("No existe un vehiculo con id " + m.getIdVehiculo() + ".");
            }
            if (v.getEstado() == EstadoVehiculo.EN_RUTA) {
                throw new NegocioException("No se puede enviar a mantenimiento un vehiculo que esta En Ruta.");
            }
            int id = mantenimientoDAO.insertar(cn, m);
            if (m.getFechaFin() == null) {
                vehiculoDAO.actualizarEstado(cn, v.getIdVehiculo(), EstadoVehiculo.EN_MANTENIMIENTO);
            }
            cn.commit();
            return id;
        } catch (SQLException e) {
            revertir(cn);
            throw new NegocioException("Error de base de datos al registrar el mantenimiento: " + e.getMessage(), e);
        } catch (NegocioException e) {
            revertir(cn);
            throw e;
        } finally {
            cerrar(cn);
        }
    }

    /** Cierra el mantenimiento y devuelve el vehiculo a Disponible si no quedan otros abiertos. */
    public void finalizarMantenimiento(int idMantenimiento, LocalDate fechaFin, BigDecimal costo)
            throws NegocioException {
        Connection cn = null;
        try {
            cn = ConexionBD.obtenerConexion();
            cn.setAutoCommit(false);

            Mantenimiento m = mantenimientoDAO.buscarPorId(cn, idMantenimiento);
            if (m == null) {
                throw new NegocioException("No existe un mantenimiento con id " + idMantenimiento + ".");
            }
            if (!m.estaAbierto()) {
                throw new NegocioException("El mantenimiento ya fue finalizado el " + m.getFechaFin() + ".");
            }
            if (fechaFin == null) {
                fechaFin = LocalDate.now();
            }
            if (fechaFin.isBefore(m.getFechaInicio())) {
                throw new NegocioException("La fecha de finalizacion no puede ser anterior a la de inicio.");
            }
            mantenimientoDAO.cerrar(cn, idMantenimiento, fechaFin, costo);

            if (mantenimientoDAO.contarAbiertosPorVehiculo(cn, m.getIdVehiculo()) == 0) {
                vehiculoDAO.actualizarEstado(cn, m.getIdVehiculo(), EstadoVehiculo.DISPONIBLE);
            }
            cn.commit();
        } catch (SQLException e) {
            revertir(cn);
            throw new NegocioException("Error de base de datos al finalizar el mantenimiento: " + e.getMessage(), e);
        } catch (NegocioException e) {
            revertir(cn);
            throw e;
        } finally {
            cerrar(cn);
        }
    }

    public List<Mantenimiento> historialMantenimientos(int idVehiculo) throws NegocioException {
        try (Connection cn = ConexionBD.obtenerConexion()) {
            obtenerObligatorio(cn, idVehiculo);
            return mantenimientoDAO.listarPorVehiculo(cn, idVehiculo);
        } catch (SQLException e) {
            throw new NegocioException("Error de base de datos: " + e.getMessage(), e);
        }
    }

    public List<Mantenimiento> listarMantenimientosAbiertos() throws NegocioException {
        try (Connection cn = ConexionBD.obtenerConexion()) {
            return mantenimientoDAO.listarAbiertos(cn);
        } catch (SQLException e) {
            throw new NegocioException("Error de base de datos: " + e.getMessage(), e);
        }
    }

    // =================================================================
    // API PUBLICA PARA OTROS MODULOS (la consume el modulo de Rutas)
    // =================================================================

    /** Vehiculos que el modulo de rutas puede usar para armar una hoja de ruta. */
    public List<Vehiculo> obtenerVehiculosDisponibles() throws NegocioException {
        return listarPorEstado(EstadoVehiculo.DISPONIBLE);
    }

    /**
     * Lee y BLOQUEA el vehiculo sobre la conexion recibida. Pensado para que el
     * modulo de rutas valide capacidad y estado dentro de SU transaccion.
     */
    public Vehiculo buscarPorIdBloqueando(Connection cn, int idVehiculo) throws SQLException {
        return vehiculoDAO.buscarPorIdBloqueando(cn, idVehiculo);
    }

    /**
     * Version transaccional de iniciarOperacion: opera sobre la conexion recibida
     * y NO hace commit ni la cierra. Permite que el modulo de rutas meta el cambio
     * de estado de la flota dentro de su propia transaccion (una sola unidad ACID,
     * sin logica de compensacion).
     *
     * @return el id del conductor que quedo En Ruta con ese vehiculo.
     */
    public int iniciarOperacion(Connection cn, int idVehiculo) throws NegocioException, SQLException {
        Vehiculo v = vehiculoDAO.buscarPorIdBloqueando(cn, idVehiculo);
        if (v == null) {
            throw new NegocioException("No existe un vehiculo con id " + idVehiculo + ".");
        }
        if (v.getEstado() != EstadoVehiculo.DISPONIBLE) {
            throw new NegocioException("El vehiculo " + v.getPlaca() + " no esta Disponible (esta " + v.getEstado() + ").");
        }
        Asignacion asignacion = asignacionDAO.buscarVigentePorVehiculo(cn, idVehiculo);
        if (asignacion == null) {
            throw new NegocioException("El vehiculo " + v.getPlaca() + " no tiene conductor asignado.");
        }
        Conductor c = conductorDAO.buscarPorIdBloqueando(cn, asignacion.getIdConductor());
        if (c.getEstado() != EstadoConductor.ACTIVO) {
            throw new NegocioException("El conductor " + c.getNombreCompleto() + " no esta Activo (esta " + c.getEstado() + ").");
        }
        vehiculoDAO.actualizarEstado(cn, idVehiculo, EstadoVehiculo.EN_RUTA);
        conductorDAO.actualizarEstado(cn, c.getIdConductor(), EstadoConductor.EN_RUTA);
        return c.getIdConductor();
    }

    /**
     * Marca el vehiculo y su conductor asignado como En Ruta, en una sola transaccion.
     * Lo invoca RutaService al iniciar una hoja de ruta.
     *
     * @return el id del conductor que quedo En Ruta con ese vehiculo.
     */
    public int iniciarOperacion(int idVehiculo) throws NegocioException {
        Connection cn = null;
        try {
            cn = ConexionBD.obtenerConexion();
            cn.setAutoCommit(false);
            int idConductor = iniciarOperacion(cn, idVehiculo);
            cn.commit();
            return idConductor;
        } catch (SQLException e) {
            revertir(cn);
            throw new NegocioException("Error de base de datos al iniciar la operacion: " + e.getMessage(), e);
        } catch (NegocioException e) {
            revertir(cn);
            throw e;
        } finally {
            cerrar(cn);
        }
    }

    /**
     * Version transaccional de finalizarOperacion: opera sobre la conexion recibida
     * y NO hace commit ni la cierra.
     */
    public void finalizarOperacion(Connection cn, int idVehiculo) throws NegocioException, SQLException {
        Vehiculo v = vehiculoDAO.buscarPorIdBloqueando(cn, idVehiculo);
        if (v == null) {
            throw new NegocioException("No existe un vehiculo con id " + idVehiculo + ".");
        }
        if (v.getEstado() != EstadoVehiculo.EN_RUTA) {
            throw new NegocioException("El vehiculo " + v.getPlaca() + " no esta En Ruta.");
        }
        vehiculoDAO.actualizarEstado(cn, idVehiculo, EstadoVehiculo.DISPONIBLE);

        Asignacion asignacion = asignacionDAO.buscarVigentePorVehiculo(cn, idVehiculo);
        if (asignacion != null) {
            conductorDAO.actualizarEstado(cn, asignacion.getIdConductor(), EstadoConductor.ACTIVO);
        }
    }

    /** Devuelve vehiculo y conductor a Disponible/Activo al finalizar la ruta. */
    public void finalizarOperacion(int idVehiculo) throws NegocioException {
        Connection cn = null;
        try {
            cn = ConexionBD.obtenerConexion();
            cn.setAutoCommit(false);
            finalizarOperacion(cn, idVehiculo);
            cn.commit();
        } catch (SQLException e) {
            revertir(cn);
            throw new NegocioException("Error de base de datos al finalizar la operacion: " + e.getMessage(), e);
        } catch (NegocioException e) {
            revertir(cn);
            throw e;
        } finally {
            cerrar(cn);
        }
    }

    // =================================================================
    // Apoyo interno
    // =================================================================

    private Vehiculo obtenerObligatorio(Connection cn, int idVehiculo) throws SQLException, NegocioException {
        Vehiculo v = vehiculoDAO.buscarPorId(cn, idVehiculo);
        if (v == null) {
            throw new NegocioException("No existe un vehiculo con id " + idVehiculo + ".");
        }
        return v;
    }

    private void validarDatosVehiculo(Vehiculo v) throws NegocioException {
        if (v.getPlaca() == null || !v.getPlaca().matches("[A-Z]{3}[0-9]{3}")) {
            throw new NegocioException("La placa debe tener el formato ABC123.");
        }
        if (v.getMarca() == null || v.getMarca().isBlank()) {
            throw new NegocioException("La marca es obligatoria.");
        }
        if (v.getModelo() == null || v.getModelo().isBlank()) {
            throw new NegocioException("El modelo es obligatorio.");
        }
        int anioActual = Year.now().getValue();
        if (v.getAnioFabricacion() < 1950 || v.getAnioFabricacion() > anioActual + 1) {
            throw new NegocioException("El anio de fabricacion debe estar entre 1950 y " + (anioActual + 1) + ".");
        }
        if (v.getCapacidadCargaKg() == null || v.getCapacidadCargaKg().compareTo(BigDecimal.ZERO) <= 0) {
            throw new NegocioException("La capacidad de carga debe ser mayor que cero.");
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
