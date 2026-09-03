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

/** Controlador del modulo de personal (conductores) y sus asignaciones. */
public class ConductorController {

    private final ConductorService conductorService = new ConductorService();

    // Registra un conductor nuevo.
    public int registrarConductor(String identificacion, String nombre, TipoLicencia licencia, String contacto)
            throws NegocioException {
        return conductorService.registrarConductor(new Conductor(identificacion, nombre, licencia, contacto));
    }

    // Actualiza los datos de un conductor.
    public void actualizarConductor(Conductor c) throws NegocioException {
        conductorService.actualizarConductor(c);
    }

    // Elimina un conductor.
    public void eliminarConductor(int idConductor) throws NegocioException {
        conductorService.eliminarConductor(idConductor);
    }

    // Busca un conductor por su id.
    public Conductor buscarConductor(int idConductor) throws NegocioException {
        return conductorService.buscarPorId(idConductor);
    }

    // Busca un conductor por su numero de identificacion.
    public Conductor buscarConductorPorIdentificacion(String identificacion) throws NegocioException {
        return conductorService.buscarPorIdentificacion(identificacion);
    }

    // Lista todos los conductores.
    public List<Conductor> listarConductores() throws NegocioException {
        return conductorService.listarConductores();
    }

    // Lista los conductores que estan en un estado.
    public List<Conductor> listarConductoresPorEstado(EstadoConductor estado) throws NegocioException {
        return conductorService.listarPorEstado(estado);
    }

    // Cambia el estado de un conductor.
    public void cambiarEstadoConductor(int idConductor, EstadoConductor estado) throws NegocioException {
        conductorService.cambiarEstadoConductor(idConductor, estado);
    }

    // --- Asignaciones ---

    // Asigna un conductor a un vehiculo.
    public int asignarConductorAVehiculo(int idConductor, int idVehiculo) throws NegocioException {
        return conductorService.asignarConductorAVehiculo(idConductor, idVehiculo);
    }

    // Libera la asignacion vigente de un conductor.
    public void liberarAsignacion(int idConductor) throws NegocioException {
        conductorService.liberarAsignacionDeConductor(idConductor);
    }

    // Lista las asignaciones conductor-vehiculo vigentes.
    public List<Asignacion> listarAsignacionesVigentes() throws NegocioException {
        return conductorService.listarAsignacionesVigentes();
    }

    // Devuelve el historial de asignaciones de un vehiculo.
    public List<Asignacion> historialAsignacionesPorVehiculo(int idVehiculo) throws NegocioException {
        return conductorService.historialPorVehiculo(idVehiculo);
    }

    // --- API para el modulo de Rutas (Persona 2) ---

    // Devuelve el conductor asignado a un vehiculo.
    public Conductor obtenerConductorAsignadoA(int idVehiculo) throws NegocioException {
        return conductorService.obtenerConductorAsignadoA(idVehiculo);
    }

    /** Variante sobre una conexion en curso: la usa el modulo de rutas dentro de su transaccion. */
    public Conductor obtenerConductorAsignadoA(Connection cn, int idVehiculo) throws SQLException {
        return conductorService.obtenerConductorAsignadoA(cn, idVehiculo);
    }
}
