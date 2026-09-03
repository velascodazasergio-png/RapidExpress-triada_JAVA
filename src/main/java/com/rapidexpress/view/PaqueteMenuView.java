package com.rapidexpress.view;

import com.rapidexpress.controller.PaqueteController;
import com.rapidexpress.excepcion.NegocioException;
import com.rapidexpress.model.Cliente;
import com.rapidexpress.model.EstadoPaquete;
import com.rapidexpress.model.Paquete;

import java.util.List;

/**
 * Vista (CLI) del modulo de paquetes y envios.
 *
 * PUNTO DE INTEGRACION: el menu principal de la Persona 3 solo debe hacer
 *     new PaqueteMenuView().mostrar();
 */
public class PaqueteMenuView {

    private final PaqueteController controlador = new PaqueteController();

    public void mostrar() {
        boolean salir = false;
        while (!salir) {
            ConsolaUtil.titulo("Gestion de paquetes y envios");
            System.out.println("  1. Registrar cliente (remitente / destinatario)");
            System.out.println("  2. Listar clientes");
            System.out.println("  3. Registrar paquete");
            System.out.println("  4. Rastrear paquete por codigo");
            System.out.println("  5. Listar todos los paquetes");
            System.out.println("  6. Listar paquetes por estado");
            System.out.println("  7. Editar un paquete (solo En Bodega)");
            System.out.println("  8. Cambiar estado manualmente");
            System.out.println("  0. Volver");
            ConsolaUtil.separador();

            int opcion = ConsolaUtil.leerEntero("Opcion");
            try {
                switch (opcion) {
                    case 1 -> registrarCliente();
                    case 2 -> imprimirClientes(controlador.listarClientes());
                    case 3 -> registrarPaquete();
                    case 4 -> rastrear();
                    case 5 -> imprimirPaquetes(controlador.listarPaquetes());
                    case 6 -> listarPorEstado();
                    case 7 -> editarPaquete();
                    case 8 -> cambiarEstado();
                    case 0 -> salir = true;
                    default -> ConsolaUtil.error("Opcion no valida.");
                }
            } catch (NegocioException e) {
                ConsolaUtil.error(e.getMessage());
            }
            if (!salir) {
                ConsolaUtil.pausa();
            }
        }
    }

    private void registrarCliente() throws NegocioException {
        ConsolaUtil.titulo("Registrar cliente");
        Cliente cliente = new Cliente();
        cliente.setDocumento(ConsolaUtil.leerTexto("Documento"));
        cliente.setNombre(ConsolaUtil.leerTexto("Nombre completo"));
        cliente.setTelefono(ConsolaUtil.leerTextoOpcional("Telefono"));
        cliente.setEmail(ConsolaUtil.leerTextoOpcional("Email"));
        cliente.setDireccion(ConsolaUtil.leerTexto("Direccion"));
        cliente.setCiudad(ConsolaUtil.leerTexto("Ciudad"));

        Cliente guardado = controlador.registrarCliente(cliente);
        ConsolaUtil.exito("Cliente registrado con id " + guardado.getIdCliente() + ".");
    }

    private void registrarPaquete() throws NegocioException {
        ConsolaUtil.titulo("Registrar paquete");
        List<Cliente> clientes = controlador.listarClientes();
        if (clientes.isEmpty()) {
            ConsolaUtil.info("Primero debe registrar al menos dos clientes (remitente y destinatario).");
            return;
        }
        imprimirClientes(clientes);

        Paquete paquete = new Paquete();
        paquete.setIdRemitente(ConsolaUtil.leerEntero("Id del remitente"));
        paquete.setIdDestinatario(ConsolaUtil.leerEntero("Id del destinatario"));
        paquete.setDescripcion(ConsolaUtil.leerTexto("Descripcion del contenido"));
        paquete.setPesoKg(ConsolaUtil.leerDecimal("Peso (kg)"));
        paquete.setLargoCm(ConsolaUtil.leerDecimal("Largo (cm)"));
        paquete.setAnchoCm(ConsolaUtil.leerDecimal("Ancho (cm)"));
        paquete.setAltoCm(ConsolaUtil.leerDecimal("Alto (cm)"));
        paquete.setDireccionOrigen(ConsolaUtil.leerTexto("Direccion de origen"));
        paquete.setCiudadOrigen(ConsolaUtil.leerTexto("Ciudad de origen"));
        paquete.setDireccionDestino(ConsolaUtil.leerTexto("Direccion de destino"));
        paquete.setCiudadDestino(ConsolaUtil.leerTexto("Ciudad de destino"));

        Paquete guardado = controlador.registrarPaquete(paquete);
        ConsolaUtil.exito("Paquete registrado.");
        ConsolaUtil.info("Codigo de seguimiento: " + guardado.getCodigoSeguimiento());
    }

    private void rastrear() throws NegocioException {
        ConsolaUtil.titulo("Rastrear paquete");
        Paquete p = controlador.rastrear(ConsolaUtil.leerTexto("Codigo de seguimiento"));
        ConsolaUtil.separador();
        System.out.println("Codigo       : " + p.getCodigoSeguimiento());
        System.out.println("Descripcion  : " + p.getDescripcion());
        System.out.println("Peso         : " + p.getPesoKg() + " kg");
        System.out.println("Dimensiones  : " + p.getLargoCm() + " x " + p.getAnchoCm() + " x " + p.getAltoCm() + " cm");
        System.out.println("Remitente    : " + p.getNombreRemitente());
        System.out.println("Destinatario : " + p.getNombreDestinatario());
        System.out.println("Origen       : " + p.getDireccionOrigen() + ", " + p.getCiudadOrigen());
        System.out.println("Destino      : " + p.getDireccionDestino() + ", " + p.getCiudadDestino());
        System.out.println("Estado       : " + p.getEstado().getEtiqueta());
        System.out.println("Registrado   : " + p.getFechaRegistro());
        if (p.getFechaEntrega() != null) {
            System.out.println("Entregado    : " + p.getFechaEntrega());
        }
    }

    private void listarPorEstado() throws NegocioException {
        ConsolaUtil.titulo("Paquetes por estado");
        EstadoPaquete estado = elegirEstado();
        if (estado != null) {
            imprimirPaquetes(controlador.listarPaquetesPorEstado(estado));
        }
    }

    private void editarPaquete() throws NegocioException {
        ConsolaUtil.titulo("Editar paquete");
        Paquete p = controlador.rastrear(ConsolaUtil.leerTexto("Codigo de seguimiento"));
        ConsolaUtil.info("Estado actual: " + p.getEstado().getEtiqueta());

        p.setDescripcion(ConsolaUtil.leerTexto("Nueva descripcion"));
        p.setPesoKg(ConsolaUtil.leerDecimal("Nuevo peso (kg)"));
        p.setLargoCm(ConsolaUtil.leerDecimal("Largo (cm)"));
        p.setAnchoCm(ConsolaUtil.leerDecimal("Ancho (cm)"));
        p.setAltoCm(ConsolaUtil.leerDecimal("Alto (cm)"));
        p.setDireccionOrigen(ConsolaUtil.leerTexto("Direccion de origen"));
        p.setCiudadOrigen(ConsolaUtil.leerTexto("Ciudad de origen"));
        p.setDireccionDestino(ConsolaUtil.leerTexto("Direccion de destino"));
        p.setCiudadDestino(ConsolaUtil.leerTexto("Ciudad de destino"));

        controlador.actualizarPaquete(p);
        ConsolaUtil.exito("Paquete actualizado.");
    }

    private void cambiarEstado() throws NegocioException {
        ConsolaUtil.titulo("Cambiar estado de un paquete");
        Paquete p = controlador.rastrear(ConsolaUtil.leerTexto("Codigo de seguimiento"));
        ConsolaUtil.info("Estado actual: " + p.getEstado().getEtiqueta());
        EstadoPaquete nuevo = elegirEstado();
        if (nuevo != null) {
            controlador.cambiarEstadoPaquete(p.getIdPaquete(), nuevo);
            ConsolaUtil.exito("Estado actualizado a " + nuevo.getEtiqueta() + ".");
        }
    }

    private EstadoPaquete elegirEstado() {
        EstadoPaquete[] estados = EstadoPaquete.values();
        for (int i = 0; i < estados.length; i++) {
            System.out.println("  " + (i + 1) + ". " + estados[i].getEtiqueta());
        }
        int opcion = ConsolaUtil.leerEntero("Estado");
        if (opcion < 1 || opcion > estados.length) {
            ConsolaUtil.error("Estado no valido.");
            return null;
        }
        return estados[opcion - 1];
    }

    private void imprimirPaquetes(List<Paquete> paquetes) {
        ConsolaUtil.separador();
        if (paquetes.isEmpty()) {
            ConsolaUtil.info("No hay paquetes para mostrar.");
            return;
        }
        for (Paquete p : paquetes) {
            System.out.printf("id=%-4d %s%n", p.getIdPaquete(), p);
        }
        ConsolaUtil.info("Total: " + paquetes.size() + " paquete(s).");
    }

    private void imprimirClientes(List<Cliente> clientes) {
        ConsolaUtil.separador();
        if (clientes.isEmpty()) {
            ConsolaUtil.info("No hay clientes registrados.");
            return;
        }
        for (Cliente c : clientes) {
            System.out.println("  " + c);
        }
    }
}
