package com.rapidexpress.controller;

import com.rapidexpress.excepcion.NegocioException;
import com.rapidexpress.model.Conductor;
import com.rapidexpress.model.EstadoRuta;
import com.rapidexpress.model.Ruta;
import com.rapidexpress.model.Vehiculo;
import com.rapidexpress.service.RutaService;

import java.time.LocalDate;
import java.util.List;

/** Controlador del modulo de rutas. */
public class RutaController {

    private final RutaService rutaService = new RutaService();

    public Ruta crearHojaDeRuta(LocalDate fecha, int idVehiculo, List<Integer> idsPaquetes, String observaciones)
            throws NegocioException {
        return rutaService.crearHojaDeRuta(fecha, idVehiculo, idsPaquetes, observaciones);
    }

    public Ruta iniciarRuta(int idRuta) throws NegocioException {
        return rutaService.iniciarRuta(idRuta);
    }

    public void registrarEntrega(String codigoSeguimiento) throws NegocioException {
        rutaService.registrarEntrega(codigoSeguimiento);
    }

    public void registrarDevolucion(String codigoSeguimiento) throws NegocioException {
        rutaService.registrarDevolucion(codigoSeguimiento);
    }

    public void finalizarRuta(int idRuta) throws NegocioException {
        rutaService.finalizarRuta(idRuta);
    }

    public void cancelarRuta(int idRuta, String motivo) throws NegocioException {
        rutaService.cancelarRuta(idRuta, motivo);
    }

    public List<Ruta> listarRutasActivas() throws NegocioException {
        return rutaService.listarRutasActivas();
    }

    public List<Ruta> listarRutasPorEstado(EstadoRuta estado) throws NegocioException {
        return rutaService.listarRutasPorEstado(estado);
    }

    public Ruta consultarRuta(int idRuta) throws NegocioException {
        return rutaService.consultarRutaConPaquetes(idRuta);
    }

    /** Vehiculos disponibles que ademas tienen conductor asignado y activo. */
    public List<Vehiculo> listarVehiculosOperativos() throws NegocioException {
        return rutaService.listarVehiculosOperativos();
    }

    public Conductor conductorDe(int idVehiculo) throws NegocioException {
        return rutaService.conductorDe(idVehiculo);
    }
}
