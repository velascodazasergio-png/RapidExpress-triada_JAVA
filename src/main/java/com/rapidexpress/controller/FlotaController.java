package com.rapidexpress.controller;

import com.rapidexpress.excepcion.NegocioException;
import com.rapidexpress.model.EstadoVehiculo;
import com.rapidexpress.model.Mantenimiento;
import com.rapidexpress.model.Vehiculo;
import com.rapidexpress.service.FlotaService;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Controlador del modulo de flota. Es la unica puerta de entrada que deben
 * usar las vistas y los demas modulos: no expone DAOs ni conexiones.
 */
public class FlotaController {

    private final FlotaService flotaService = new FlotaService();

    // --- Vehiculos ---

    public int registrarVehiculo(String placa, String marca, String modelo, int anio, BigDecimal capacidadKg)
            throws NegocioException {
        Vehiculo v = new Vehiculo(placa, marca, modelo, anio, capacidadKg);
        return flotaService.registrarVehiculo(v);
    }

    public void actualizarVehiculo(Vehiculo v) throws NegocioException {
        flotaService.actualizarVehiculo(v);
    }

    public void eliminarVehiculo(int idVehiculo) throws NegocioException {
        flotaService.eliminarVehiculo(idVehiculo);
    }

    public Vehiculo buscarVehiculo(int idVehiculo) throws NegocioException {
        return flotaService.buscarPorId(idVehiculo);
    }

    public Vehiculo buscarVehiculoPorPlaca(String placa) throws NegocioException {
        return flotaService.buscarPorPlaca(placa);
    }

    public List<Vehiculo> listarVehiculos() throws NegocioException {
        return flotaService.listarVehiculos();
    }

    public List<Vehiculo> listarVehiculosPorEstado(EstadoVehiculo estado) throws NegocioException {
        return flotaService.listarPorEstado(estado);
    }

    public void cambiarEstadoVehiculo(int idVehiculo, EstadoVehiculo estado) throws NegocioException {
        flotaService.cambiarEstadoVehiculo(idVehiculo, estado);
    }

    // --- Mantenimientos ---

    public int registrarMantenimiento(int idVehiculo, Mantenimiento.Tipo tipo, String descripcion,
                                      LocalDate fechaInicio, String taller) throws NegocioException {
        Mantenimiento m = new Mantenimiento();
        m.setIdVehiculo(idVehiculo);
        m.setTipo(tipo);
        m.setDescripcion(descripcion);
        m.setFechaInicio(fechaInicio);
        m.setTaller(taller);
        return flotaService.registrarMantenimiento(m);
    }

    public void finalizarMantenimiento(int idMantenimiento, LocalDate fechaFin, BigDecimal costo)
            throws NegocioException {
        flotaService.finalizarMantenimiento(idMantenimiento, fechaFin, costo);
    }

    public List<Mantenimiento> historialMantenimientos(int idVehiculo) throws NegocioException {
        return flotaService.historialMantenimientos(idVehiculo);
    }

    public List<Mantenimiento> listarMantenimientosAbiertos() throws NegocioException {
        return flotaService.listarMantenimientosAbiertos();
    }

    // --- API para el modulo de Rutas (Persona 2) ---

    /** Vehiculos Disponibles que el modulo de rutas puede usar para armar una hoja de ruta. */
    public List<Vehiculo> obtenerVehiculosDisponibles() throws NegocioException {
        return flotaService.obtenerVehiculosDisponibles();
    }

    /**
     * Pone el vehiculo y su conductor asignado En Ruta, en una sola transaccion.
     *
     * @return el id del conductor que quedo En Ruta con ese vehiculo
     * @throws NegocioException si el vehiculo no esta Disponible o no tiene conductor activo
     */
    public int iniciarOperacion(int idVehiculo) throws NegocioException {
        return flotaService.iniciarOperacion(idVehiculo);
    }

    /** Devuelve vehiculo y conductor a Disponible/Activo al finalizar la ruta. */
    public void finalizarOperacion(int idVehiculo) throws NegocioException {
        flotaService.finalizarOperacion(idVehiculo);
    }

    // --- Variantes transaccionales: el modulo de rutas las llama dentro de SU
    //     transaccion para que iniciar/cerrar una ruta sea una sola unidad ACID.

    /**
     * Lee y BLOQUEA el vehiculo sobre la conexion recibida
     * ({@code SELECT ... FOR UPDATE}). Solo dentro de una transaccion.
     *
     * @return el vehiculo bloqueado, o {@code null} si no existe
     */
    public Vehiculo buscarVehiculoBloqueando(Connection cn, int idVehiculo) throws SQLException {
        return flotaService.buscarPorIdBloqueando(cn, idVehiculo);
    }

    /**
     * Variante de {@link #iniciarOperacion(int)} que opera sobre la conexion
     * recibida y NO hace commit ni la cierra.
     *
     * @return el id del conductor que quedo En Ruta con ese vehiculo
     */
    public int iniciarOperacion(Connection cn, int idVehiculo) throws NegocioException, SQLException {
        return flotaService.iniciarOperacion(cn, idVehiculo);
    }

    /**
     * Variante de {@link #finalizarOperacion(int)} que opera sobre la conexion
     * recibida y NO hace commit ni la cierra.
     */
    public void finalizarOperacion(Connection cn, int idVehiculo) throws NegocioException, SQLException {
        flotaService.finalizarOperacion(cn, idVehiculo);
    }
}
