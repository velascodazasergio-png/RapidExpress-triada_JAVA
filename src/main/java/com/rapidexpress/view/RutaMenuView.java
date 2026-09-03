package com.rapidexpress.view;

import com.rapidexpress.controller.PaqueteController;
import com.rapidexpress.controller.RutaController;
import com.rapidexpress.excepcion.NegocioException;
import com.rapidexpress.model.Conductor;
import com.rapidexpress.model.EstadoPaquete;
import com.rapidexpress.model.EstadoRuta;
import com.rapidexpress.model.Paquete;
import com.rapidexpress.model.Ruta;
import com.rapidexpress.model.Vehiculo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Vista (CLI) del modulo de planificacion y seguimiento de rutas.
 *
 * PUNTO DE INTEGRACION: el menu principal de la Persona 3 solo debe hacer
 *     new RutaMenuView().mostrar();
 */
public class RutaMenuView {

    private final RutaController controlador = new RutaController();
    private final PaqueteController paqueteControlador = new PaqueteController();

    public void mostrar() {
        boolean salir = false;
        while (!salir) {
            ConsolaUtil.titulo("Planificacion y seguimiento de rutas");
            System.out.println("  1. Crear hoja de ruta");
            System.out.println("  2. Iniciar ruta");
            System.out.println("  3. Monitorear rutas en curso");
            System.out.println("  4. Registrar entrega de un paquete");
            System.out.println("  5. Registrar devolucion de un paquete");
            System.out.println("  6. Finalizar ruta");
            System.out.println("  7. Cancelar ruta planificada");
            System.out.println("  8. Ver detalle de una ruta");
            System.out.println("  9. Listar rutas por estado");
            System.out.println("  0. Volver");
            ConsolaUtil.separador();

            int opcion = ConsolaUtil.leerEntero("Opcion");
            try {
                switch (opcion) {
                    case 1 -> crearRuta();
                    case 2 -> iniciarRuta();
                    case 3 -> monitorear();
                    case 4 -> registrarEntrega();
                    case 5 -> registrarDevolucion();
                    case 6 -> finalizarRuta();
                    case 7 -> cancelarRuta();
                    case 8 -> verDetalle();
                    case 9 -> listarPorEstado();
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

    private void crearRuta() throws NegocioException {
        ConsolaUtil.titulo("Crear hoja de ruta");

        List<Paquete> enBodega = paqueteControlador.listarPaquetesPorEstado(EstadoPaquete.EN_BODEGA);
        if (enBodega.isEmpty()) {
            ConsolaUtil.info("No hay paquetes En Bodega para asignar.");
            return;
        }
        List<Vehiculo> operativos = controlador.listarVehiculosOperativos();
        if (operativos.isEmpty()) {
            ConsolaUtil.info("No hay vehiculos Disponibles con conductor asignado y activo.");
            ConsolaUtil.info("Asigne un conductor desde el menu de personal (modulo de flota).");
            return;
        }

        System.out.println("Vehiculos operativos (Disponibles y con conductor asignado):");
        for (Vehiculo v : operativos) {
            Conductor c = controlador.conductorDe(v.getIdVehiculo());
            System.out.printf("  id=%-4d %-8s %-22s cap %8s kg  conductor: %s%n",
                    v.getIdVehiculo(), v.getPlaca(), v.getMarca() + " " + v.getModelo(),
                    v.getCapacidadCargaKg(), c == null ? "-" : c.getNombreCompleto());
        }
        ConsolaUtil.separador();

        System.out.println("Paquetes disponibles en bodega:");
        for (Paquete p : enBodega) {
            System.out.printf("  id=%-4d %s%n", p.getIdPaquete(), p);
        }
        ConsolaUtil.separador();

        LocalDate fecha = ConsolaUtil.leerFecha("Fecha de la ruta");
        int idVehiculo = ConsolaUtil.leerEntero("Id del vehiculo");
        ConsolaUtil.info("Ingrese los ids de los paquetes separados por coma. Ejemplo: 3,7,12");
        List<Integer> ids = leerIds(ConsolaUtil.leerTexto("Ids de paquetes"));
        if (ids == null) {
            return;
        }
        String observaciones = ConsolaUtil.leerTextoOpcional("Observaciones");

        Ruta ruta = controlador.crearHojaDeRuta(fecha, idVehiculo, ids, observaciones);
        ConsolaUtil.exito("Ruta " + ruta.getCodigo() + " creada con id " + ruta.getIdRuta() + ".");
        ConsolaUtil.info("Conductor asignado: " + ruta.getNombreConductor());
        ConsolaUtil.info("Carga: " + ruta.getPesoTotalKg() + " kg de " + ruta.getCapacidadVehiculoKg()
                + " kg de capacidad (queda "
                + ruta.getCapacidadVehiculoKg().subtract(ruta.getPesoTotalKg()) + " kg libre).");
    }

    private void iniciarRuta() throws NegocioException {
        ConsolaUtil.titulo("Iniciar ruta");
        List<Ruta> planificadas = controlador.listarRutasPorEstado(EstadoRuta.PLANIFICADA);
        imprimirRutas(planificadas);
        if (planificadas.isEmpty()) {
            return;
        }
        Ruta ruta = controlador.iniciarRuta(ConsolaUtil.leerEntero("Id de la ruta a iniciar"));
        ConsolaUtil.exito("Ruta " + ruta.getCodigo() + " iniciada.");
        ConsolaUtil.info("El vehiculo " + ruta.getPlacaVehiculo() + " y el conductor "
                + ruta.getNombreConductor() + " quedaron En Ruta.");
        ConsolaUtil.info(ruta.getPaquetes().size() + " paquete(s) pasaron a En Transito.");
    }

    private void monitorear() throws NegocioException {
        ConsolaUtil.titulo("Rutas en curso");
        List<Ruta> activas = controlador.listarRutasActivas();
        if (activas.isEmpty()) {
            ConsolaUtil.info("No hay rutas en curso en este momento.");
            return;
        }
        for (Ruta ruta : activas) {
            ConsolaUtil.separador();
            System.out.printf("id=%-4d %s%n", ruta.getIdRuta(), ruta);
            long entregados = ruta.getPaquetes().stream()
                    .filter(p -> p.getEstado() == EstadoPaquete.ENTREGADO).count();
            long devueltos = ruta.getPaquetes().stream()
                    .filter(p -> p.getEstado() == EstadoPaquete.DEVUELTO).count();
            System.out.printf("   Avance: %d entregados, %d devueltos, %d pendientes%n",
                    entregados, devueltos, ruta.getPaquetes().size() - entregados - devueltos);
            for (Paquete p : ruta.getPaquetes()) {
                System.out.println("     - " + p);
            }
        }
    }

    private void registrarEntrega() throws NegocioException {
        ConsolaUtil.titulo("Registrar entrega");
        controlador.registrarEntrega(ConsolaUtil.leerTexto("Codigo de seguimiento del paquete"));
        ConsolaUtil.exito("Paquete marcado como Entregado.");
    }

    private void registrarDevolucion() throws NegocioException {
        ConsolaUtil.titulo("Registrar devolucion");
        controlador.registrarDevolucion(ConsolaUtil.leerTexto("Codigo de seguimiento del paquete"));
        ConsolaUtil.exito("Paquete marcado como Devuelto.");
    }

    private void finalizarRuta() throws NegocioException {
        ConsolaUtil.titulo("Finalizar ruta");
        List<Ruta> activas = controlador.listarRutasActivas();
        imprimirRutas(activas);
        if (activas.isEmpty()) {
            return;
        }
        controlador.finalizarRuta(ConsolaUtil.leerEntero("Id de la ruta a finalizar"));
        ConsolaUtil.exito("Ruta finalizada. El vehiculo vuelve a Disponible y el conductor a Activo.");
    }

    private void cancelarRuta() throws NegocioException {
        ConsolaUtil.titulo("Cancelar ruta planificada");
        List<Ruta> planificadas = controlador.listarRutasPorEstado(EstadoRuta.PLANIFICADA);
        imprimirRutas(planificadas);
        if (planificadas.isEmpty()) {
            return;
        }
        int idRuta = ConsolaUtil.leerEntero("Id de la ruta a cancelar");
        if (!ConsolaUtil.confirmar("Los paquetes volveran a En Bodega. Confirma?")) {
            ConsolaUtil.info("Operacion cancelada.");
            return;
        }
        controlador.cancelarRuta(idRuta, ConsolaUtil.leerTexto("Motivo"));
        ConsolaUtil.exito("Ruta cancelada.");
    }

    private void verDetalle() throws NegocioException {
        ConsolaUtil.titulo("Detalle de ruta");
        Ruta ruta = controlador.consultarRuta(ConsolaUtil.leerEntero("Id de la ruta"));
        ConsolaUtil.separador();
        System.out.println("Codigo    : " + ruta.getCodigo());
        System.out.println("Fecha     : " + ruta.getFecha());
        System.out.println("Vehiculo  : " + ruta.getPlacaVehiculo());
        System.out.println("Conductor : " + ruta.getNombreConductor());
        System.out.println("Estado    : " + ruta.getEstado().getEtiqueta());
        System.out.println("Carga     : " + ruta.getPesoTotalKg() + " kg de "
                + ruta.getCapacidadVehiculoKg() + " kg");
        if (ruta.getObservaciones() != null) {
            System.out.println("Observ.   : " + ruta.getObservaciones());
        }
        System.out.println("Paquetes  :");
        for (Paquete p : ruta.getPaquetes()) {
            System.out.println("   - " + p);
        }
    }

    private void listarPorEstado() throws NegocioException {
        ConsolaUtil.titulo("Rutas por estado");
        EstadoRuta[] estados = EstadoRuta.values();
        for (int i = 0; i < estados.length; i++) {
            System.out.println("  " + (i + 1) + ". " + estados[i].getEtiqueta());
        }
        int opcion = ConsolaUtil.leerEntero("Estado");
        if (opcion < 1 || opcion > estados.length) {
            ConsolaUtil.error("Estado no valido.");
            return;
        }
        imprimirRutas(controlador.listarRutasPorEstado(estados[opcion - 1]));
    }

    private List<Integer> leerIds(String entrada) {
        List<Integer> ids = new ArrayList<>();
        for (String parte : entrada.split(",")) {
            String limpio = parte.trim();
            if (!limpio.isEmpty()) {
                try {
                    ids.add(Integer.parseInt(limpio));
                } catch (NumberFormatException e) {
                    ConsolaUtil.error("Id invalido: " + limpio);
                    return null;
                }
            }
        }
        return ids;
    }

    private void imprimirRutas(List<Ruta> rutas) {
        ConsolaUtil.separador();
        if (rutas.isEmpty()) {
            ConsolaUtil.info("No hay rutas para mostrar.");
            return;
        }
        for (Ruta ruta : rutas) {
            System.out.printf("id=%-4d %s%n", ruta.getIdRuta(), ruta);
        }
    }
}
