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

    // Registra un paquete nuevo.
    public Paquete registrarPaquete(Paquete paquete) throws NegocioException {
        return paqueteService.registrarPaquete(paquete);
    }

    // Registra un cliente nuevo.
    public Cliente registrarCliente(Cliente cliente) throws NegocioException {
        return paqueteService.registrarCliente(cliente);
    }

    // Busca un paquete por su codigo de seguimiento.
    public Paquete rastrear(String codigoSeguimiento) throws NegocioException {
        return paqueteService.rastrear(codigoSeguimiento);
    }

    // Lista todos los paquetes.
    public List<Paquete> listarPaquetes() throws NegocioException {
        return paqueteService.listarTodos();
    }

    // Lista los paquetes que estan en un estado.
    public List<Paquete> listarPaquetesPorEstado(EstadoPaquete estado) throws NegocioException {
        return paqueteService.listarPorEstado(estado);
    }

    // Lista todos los clientes.
    public List<Cliente> listarClientes() throws NegocioException {
        return paqueteService.listarClientes();
    }

    // Actualiza los datos de un paquete.
    public void actualizarPaquete(Paquete paquete) throws NegocioException {
        paqueteService.actualizarDatos(paquete);
    }

    // Cambia el estado de un paquete.
    public void cambiarEstadoPaquete(int idPaquete, EstadoPaquete nuevoEstado) throws NegocioException {
        paqueteService.cambiarEstado(idPaquete, nuevoEstado);
    }
}
