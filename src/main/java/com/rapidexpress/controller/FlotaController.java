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

    // Registra un vehiculo nuevo.
    public int registrarVehiculo(String placa, String marca, String modelo, int anio, BigDecimal capacidadKg)
            throws NegocioException {
        Vehiculo v = new Vehiculo(placa, marca, modelo, anio, capacidadKg);
        return flotaService.registrarVehiculo(v);
    }

    // Actualiza los datos de un vehiculo.
    public void actualizarVehiculo(Vehiculo v) throws NegocioException {
        flotaService.actualizarVehiculo(v);
    }

    // Elimina un vehiculo.
    public void eliminarVehiculo(int idVehiculo) throws NegocioException {
        flotaService.eliminarVehiculo(idVehiculo);
    }

    // Busca un vehiculo por su id.
    public Vehiculo buscarVehiculo(int idVehiculo) throws NegocioException {
        return flotaService.buscarPorId(idVehiculo);
    }

    // Busca un vehiculo por su placa.
    public Vehiculo buscarVehiculoPorPlaca(String placa) throws NegocioException {
        return flotaService.buscarPorPlaca(placa);
    }

    // Lista todos los vehiculos.
    public List<Vehiculo> listarVehiculos() throws NegocioException {
        return flotaService.listarVehiculos();
    }

    // Lista los vehiculos que estan en un estado.
    public List<Vehiculo> listarVehiculosPorEstado(EstadoVehiculo estado) throws NegocioException {
        return flotaService.listarPorEstado(estado);
    }

    // Cambia el estado de un vehiculo.
    public void cambiarEstadoVehiculo(int idVehiculo, EstadoVehiculo estado) throws NegocioException {
        flotaService.cambiarEstadoVehiculo(idVehiculo, estado);
    }

    // --- Mantenimientos ---

    // Registra un mantenimiento para un vehiculo.
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

    // Finaliza un mantenimiento abierto.
    public void finalizarMantenimiento(int idMantenimiento, LocalDate fechaFin, BigDecimal costo)
            throws NegocioException {
        flotaService.finalizarMantenimiento(idMantenimiento, fechaFin, costo);
    }

    // Devuelve el historial de mantenimientos de un vehiculo.
    public List<Mantenimiento> historialMantenimientos(int idVehiculo) throws NegocioException {
        return flotaService.historialMantenimientos(idVehiculo);
    }

    // Lista los mantenimientos sin finalizar.
    public List<Mantenimiento> listarMantenimientosAbiertos() throws NegocioException {
        return flotaService.listarMantenimientosAbiertos();
    }

    // --- API para el modulo de Rutas (Persona 2) ---

    // Devuelve los vehiculos disponibles.
    public List<Vehiculo> obtenerVehiculosDisponibles() throws NegocioException {
        return flotaService.obtenerVehiculosDisponibles();
    }

    // Pone el vehiculo y su conductor En Ruta.
    public int iniciarOperacion(int idVehiculo) throws NegocioException {
        return flotaService.iniciarOperacion(idVehiculo);
    }

    // Devuelve el vehiculo y su conductor a Disponible/Activo.
    public void finalizarOperacion(int idVehiculo) throws NegocioException {
        flotaService.finalizarOperacion(idVehiculo);
    }

    // --- Variantes transaccionales: el modulo de rutas las llama dentro de SU
    //     transaccion para que iniciar/cerrar una ruta sea una sola unidad ACID.

    // Busca un vehiculo bloqueando la fila (para transacciones).
    public Vehiculo buscarVehiculoBloqueando(Connection cn, int idVehiculo) throws SQLException {
        return flotaService.buscarPorIdBloqueando(cn, idVehiculo);
    }

    // Pone el vehiculo y su conductor En Ruta.
    public int iniciarOperacion(Connection cn, int idVehiculo) throws NegocioException, SQLException {
        return flotaService.iniciarOperacion(cn, idVehiculo);
    }

    // Devuelve el vehiculo y su conductor a Disponible/Activo.
    public void finalizarOperacion(Connection cn, int idVehiculo) throws NegocioException, SQLException {
        flotaService.finalizarOperacion(cn, idVehiculo);
    }
}
