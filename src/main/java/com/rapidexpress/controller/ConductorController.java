package com.rapidexpress.controller;

import com.rapidexpress.excepcion.NegocioException;
import com.rapidexpress.model.Asignacion;
import com.rapidexpress.model.Conductor;
import com.rapidexpress.model.EstadoConductor;
import com.rapidexpress.model.TipoLicencia;
import com.rapidexpress.service.ConductorService;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Controlador del modulo de personal (conductores) y sus asignaciones.
 *
 * <p>Unica puerta de entrada para las vistas y para el modulo de rutas: no
 * expone DAOs ni conexiones. Todos los metodos propagan
 * {@link NegocioException} con un mensaje ya redactado para el usuario.</p>
 */
public class ConductorController {

    private final ConductorService conductorService = new ConductorService();

    /**
     * @return el id generado para el nuevo conductor
     * @throws NegocioException si los datos no son validos o la identificacion ya existe
     */
    public int registrarConductor(String identificacion, String nombre, TipoLicencia licencia, String contacto)
            throws NegocioException {
        return conductorService.registrarConductor(new Conductor(identificacion, nombre, licencia, contacto));
    }

    public void actualizarConductor(Conductor c) throws NegocioException {
        conductorService.actualizarConductor(c);
    }

    public void eliminarConductor(int idConductor) throws NegocioException {
        conductorService.eliminarConductor(idConductor);
    }

    public Conductor buscarConductor(int idConductor) throws NegocioException {
        return conductorService.buscarPorId(idConductor);
    }

    public Conductor buscarConductorPorIdentificacion(String identificacion) throws NegocioException {
        return conductorService.buscarPorIdentificacion(identificacion);
    }

    public List<Conductor> listarConductores() throws NegocioException {
        return conductorService.listarConductores();
    }

    public List<Conductor> listarConductoresPorEstado(EstadoConductor estado) throws NegocioException {
        return conductorService.listarPorEstado(estado);
    }

    public void cambiarEstadoConductor(int idConductor, EstadoConductor estado) throws NegocioException {
        conductorService.cambiarEstadoConductor(idConductor, estado);
    }

    // --- Asignaciones ---

    /**
     * Asigna un conductor Activo a un vehiculo Disponible dentro de una
     * transaccion que bloquea ambas filas, de modo que dos terminales no puedan
     * asignar el mismo conductor a la vez. Ni el conductor ni el vehiculo pueden
     * tener ya una asignacion vigente.
     *
     * @return el id de la asignacion creada
     * @throws NegocioException si alguno no existe, no esta en el estado correcto
     *                          o ya tiene una asignacion vigente
     */
    public int asignarConductorAVehiculo(int idConductor, int idVehiculo) throws NegocioException {
        return conductorService.asignarConductorAVehiculo(idConductor, idVehiculo);
    }

    /**
     * Cierra la asignacion vigente del conductor.
     *
     * @throws NegocioException si no tiene asignacion vigente o el vehiculo esta En Ruta
     */
    public void liberarAsignacion(int idConductor) throws NegocioException {
        conductorService.liberarAsignacionDeConductor(idConductor);
    }

    public List<Asignacion> listarAsignacionesVigentes() throws NegocioException {
        return conductorService.listarAsignacionesVigentes();
    }

    public List<Asignacion> historialAsignacionesPorVehiculo(int idVehiculo) throws NegocioException {
        return conductorService.historialPorVehiculo(idVehiculo);
    }

    // --- API para el modulo de Rutas (Persona 2) ---

    /**
     * Conductor que actualmente maneja ese vehiculo segun la asignacion vigente.
     *
     * @return el conductor asignado, o {@code null} si el vehiculo no tiene ninguno
     */
    public Conductor obtenerConductorAsignadoA(int idVehiculo) throws NegocioException {
        return conductorService.obtenerConductorAsignadoA(idVehiculo);
    }

    /** Variante sobre una conexion en curso: la usa el modulo de rutas dentro de su transaccion. */
    public Conductor obtenerConductorAsignadoA(Connection cn, int idVehiculo) throws SQLException {
        return conductorService.obtenerConductorAsignadoA(cn, idVehiculo);
    }
}
