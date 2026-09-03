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

    // Crea una hoja de ruta diaria.
    public Ruta crearHojaDeRuta(LocalDate fecha, int idVehiculo, List<Integer> idsPaquetes, String observaciones)
            throws NegocioException {
        return rutaService.crearHojaDeRuta(fecha, idVehiculo, idsPaquetes, observaciones);
    }

    // Inicia una ruta planificada.
    public Ruta iniciarRuta(int idRuta) throws NegocioException {
        return rutaService.iniciarRuta(idRuta);
    }

    // Marca un paquete como Entregado.
    public void registrarEntrega(String codigoSeguimiento) throws NegocioException {
        rutaService.registrarEntrega(codigoSeguimiento);
    }

    // Marca un paquete como Devuelto.
    public void registrarDevolucion(String codigoSeguimiento) throws NegocioException {
        rutaService.registrarDevolucion(codigoSeguimiento);
    }

    // Finaliza una ruta En Curso.
    public void finalizarRuta(int idRuta) throws NegocioException {
        rutaService.finalizarRuta(idRuta);
    }

    // Cancela una ruta planificada.
    public void cancelarRuta(int idRuta, String motivo) throws NegocioException {
        rutaService.cancelarRuta(idRuta, motivo);
    }

    // Lista las rutas En Curso.
    public List<Ruta> listarRutasActivas() throws NegocioException {
        return rutaService.listarRutasActivas();
    }

    // Lista las rutas que estan en un estado.
    public List<Ruta> listarRutasPorEstado(EstadoRuta estado) throws NegocioException {
        return rutaService.listarRutasPorEstado(estado);
    }

    // Devuelve una ruta con sus paquetes.
    public Ruta consultarRuta(int idRuta) throws NegocioException {
        return rutaService.consultarRutaConPaquetes(idRuta);
    }

    /** Vehiculos disponibles que ademas tienen conductor asignado y activo. */
    public List<Vehiculo> listarVehiculosOperativos() throws NegocioException {
        return rutaService.listarVehiculosOperativos();
    }

    // Devuelve el conductor asignado a un vehiculo.
    public Conductor conductorDe(int idVehiculo) throws NegocioException {
        return rutaService.conductorDe(idVehiculo);
    }
}
