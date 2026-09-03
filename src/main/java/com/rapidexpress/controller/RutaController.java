package com.rapidexpress.controller;

import com.rapidexpress.excepcion.NegocioException;
import com.rapidexpress.model.Conductor;
import com.rapidexpress.model.EstadoRuta;
import com.rapidexpress.model.Ruta;
import com.rapidexpress.model.Vehiculo;
import com.rapidexpress.service.RutaService;

import java.time.LocalDate;
import java.util.List;

/**
 * Controlador del modulo de rutas. Unica puerta de entrada para la vista;
 * no expone DAOs ni conexiones. Todos los metodos propagan
 * {@link NegocioException} con un mensaje listo para mostrar al usuario.
 */
public class RutaController {

    private final RutaService rutaService = new RutaService();

    /**
     * Crea la hoja de ruta diaria. El conductor NO se pasa como parametro: se
     * toma de la asignacion vigente del vehiculo. Valida en una sola transaccion
     * que la carga no exceda la capacidad y que vehiculo, conductor y paquetes
     * esten disponibles.
     *
     * @param fecha        dia de la ruta; no puede ser anterior a hoy
     * @param idVehiculo   vehiculo Disponible y con conductor asignado activo
     * @param idsPaquetes  ids de paquetes En Bodega (se ignoran duplicados)
     * @param observaciones texto libre, opcional ({@code null} permitido)
     * @return la ruta creada, en estado Planificada
     * @throws NegocioException si falla una validacion; en ese caso no se crea nada
     */
    public Ruta crearHojaDeRuta(LocalDate fecha, int idVehiculo, List<Integer> idsPaquetes, String observaciones)
            throws NegocioException {
        return rutaService.crearHojaDeRuta(fecha, idVehiculo, idsPaquetes, observaciones);
    }

    /**
     * Inicia la ruta en una sola transaccion: pone vehiculo y conductor En Ruta,
     * los paquetes En Transito y la ruta En Curso. Si algo falla, rollback total.
     *
     * @return la ruta ya en estado En Curso, con sus paquetes
     * @throws NegocioException si la ruta no esta Planificada o otra terminal la modifico
     */
    public Ruta iniciarRuta(int idRuta) throws NegocioException {
        return rutaService.iniciarRuta(idRuta);
    }

    /**
     * Marca un paquete como Entregado. Debe venir de En Transito.
     *
     * @throws NegocioException si el paquete no existe o no esta En Transito
     */
    public void registrarEntrega(String codigoSeguimiento) throws NegocioException {
        rutaService.registrarEntrega(codigoSeguimiento);
    }

    /**
     * Marca un paquete como Devuelto (no se pudo entregar). Debe venir de En Transito.
     *
     * @throws NegocioException si el paquete no existe o no esta En Transito
     */
    public void registrarDevolucion(String codigoSeguimiento) throws NegocioException {
        rutaService.registrarDevolucion(codigoSeguimiento);
    }

    /**
     * Finaliza la ruta y devuelve vehiculo y conductor a Disponible/Activo, todo
     * en la misma transaccion.
     *
     * @throws NegocioException si la ruta no esta En Curso o quedan paquetes pendientes
     */
    public void finalizarRuta(int idRuta) throws NegocioException {
        rutaService.finalizarRuta(idRuta);
    }

    /**
     * Cancela una ruta que aun no inicia y devuelve sus paquetes a En Bodega.
     *
     * @param motivo texto libre que queda en la auditoria
     * @throws NegocioException si la ruta no esta Planificada
     */
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
