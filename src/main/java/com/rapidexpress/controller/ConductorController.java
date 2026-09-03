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

    public int asignarConductorAVehiculo(int idConductor, int idVehiculo) throws NegocioException {
        return conductorService.asignarConductorAVehiculo(idConductor, idVehiculo);
    }

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

    public Conductor obtenerConductorAsignadoA(int idVehiculo) throws NegocioException {
        return conductorService.obtenerConductorAsignadoA(idVehiculo);
    }

    /** Variante sobre una conexion en curso: la usa el modulo de rutas dentro de su transaccion. */
    public Conductor obtenerConductorAsignadoA(Connection cn, int idVehiculo) throws SQLException {
        return conductorService.obtenerConductorAsignadoA(cn, idVehiculo);
    }
}
