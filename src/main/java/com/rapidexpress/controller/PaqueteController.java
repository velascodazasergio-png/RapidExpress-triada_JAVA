package com.rapidexpress.controller;

import com.rapidexpress.excepcion.NegocioException;
import com.rapidexpress.model.Cliente;
import com.rapidexpress.model.EstadoPaquete;
import com.rapidexpress.model.Paquete;
import com.rapidexpress.service.PaqueteService;

import java.util.List;

/**
 * Controlador del modulo de paquetes. Misma convencion que FlotaController:
 * unica puerta de entrada para las vistas y los demas modulos; no expone DAOs.
 */
public class PaqueteController {

    private final PaqueteService paqueteService = new PaqueteService();

    public Paquete registrarPaquete(Paquete paquete) throws NegocioException {
        return paqueteService.registrarPaquete(paquete);
    }

    public Cliente registrarCliente(Cliente cliente) throws NegocioException {
        return paqueteService.registrarCliente(cliente);
    }

    public Paquete rastrear(String codigoSeguimiento) throws NegocioException {
        return paqueteService.rastrear(codigoSeguimiento);
    }

    public List<Paquete> listarPaquetes() throws NegocioException {
        return paqueteService.listarTodos();
    }

    public List<Paquete> listarPaquetesPorEstado(EstadoPaquete estado) throws NegocioException {
        return paqueteService.listarPorEstado(estado);
    }

    public List<Cliente> listarClientes() throws NegocioException {
        return paqueteService.listarClientes();
    }

    public void actualizarPaquete(Paquete paquete) throws NegocioException {
        paqueteService.actualizarDatos(paquete);
    }

    public void cambiarEstadoPaquete(int idPaquete, EstadoPaquete nuevoEstado) throws NegocioException {
        paqueteService.cambiarEstado(idPaquete, nuevoEstado);
    }
}
