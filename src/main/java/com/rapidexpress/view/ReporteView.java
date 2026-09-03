package com.rapidexpress.view;

import com.rapidexpress.controller.ReporteController;
import com.rapidexpress.model.dto.EntregaDTO;
import com.rapidexpress.model.dto.HistorialRutaDTO;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/** Menú del módulo de reportes operativos. */
public class ReporteView {

    private final ReporteController controller = new ReporteController();

    public void mostrar() {
        boolean salir = false;
        while (!salir) {
            ConsolaUtil.titulo("Módulo de reportes");
            System.out.println("""
                      1. Entregas de un conductor por rango de fechas
                      2. Historial de rutas de un vehículo
                      3. Estado general de la flota
                      4. Desempeño de conductores
                      5. Ocupación de rutas
                      6. Paquetes por estado
                      7. Seguimiento de un paquete
                      0. Volver al menú principal
                    """);

            int opcion = ConsolaUtil.leerEntero("Opción");
            try {
                switch (opcion) {
                    case 1 -> entregasPorConductor();
                    case 2 -> historialVehiculo();
                    case 3 -> ConsolaUtil.imprimir(controller.estadoFlota());
                    case 4 -> ConsolaUtil.imprimir(controller.desempenoConductores());
                    case 5 -> ConsolaUtil.imprimir(controller.ocupacionRutas());
                    case 6 -> ConsolaUtil.imprimir(controller.paquetesPorEstado());
                    case 7 -> seguimientoPaquete();
                    case 0 -> salir = true;
                    default -> ConsolaUtil.error("Opción no reconocida.");
                }
            } catch (IllegalArgumentException e) {
                ConsolaUtil.error(e.getMessage());
            } catch (SQLException e) {
                ConsolaUtil.error("Error consultando la base de datos: " + e.getMessage());
            }

            if (!salir) {
                ConsolaUtil.pausar();
            }
        }
    }

    // ---------------------------------------------------------------
    private void entregasPorConductor() throws SQLException {
        int id = ConsolaUtil.leerEntero("Id del conductor");
        LocalDate desde = ConsolaUtil.leerFecha("Fecha inicial");
        LocalDate hasta = ConsolaUtil.leerFecha("Fecha final");

        List<EntregaDTO> entregas = controller.entregasPorConductor(id, desde, hasta);
        ConsolaUtil.titulo("Entregas del conductor " + id + " · " + desde + " a " + hasta);

        if (entregas.isEmpty()) {
            ConsolaUtil.aviso("El conductor no registra entregas en ese rango.");
            return;
        }

        System.out.printf(" %-12s | %-10s | %-8s | %-14s | %-34s | %8s | %-19s%n",
                "RUTA", "FECHA", "PLACA", "GUÍA", "DESTINO", "PESO KG", "ENTREGADO");
        System.out.println("-".repeat(122));
        for (EntregaDTO e : entregas) {
            System.out.printf(" %-12s | %-10s | %-8s | %-14s | %-34.34s | %8.2f | %-19s%n",
                    e.codigoRuta(), e.fechaRuta(), e.placa(), e.codigoSeguimiento(),
                    e.direccionDestino(), e.pesoKg(),
                    e.fechaEntrega() == null ? "-" : e.fechaEntrega().withNano(0));
        }
        System.out.printf("%n  Total: %d entrega(s) · %.2f kg movilizados%n",
                entregas.size(), controller.pesoTotal(entregas));
    }

    private void historialVehiculo() throws SQLException {
        int id = ConsolaUtil.leerEntero("Id del vehículo");
        List<HistorialRutaDTO> historial = controller.historialVehiculo(id);
        ConsolaUtil.titulo("Historial de rutas del vehículo " + id);

        if (historial.isEmpty()) {
            ConsolaUtil.aviso("El vehículo no tiene rutas registradas.");
            return;
        }

        System.out.printf(" %-12s | %-10s | %-12s | %-30s | %5s | %9s | %9s | %6s%n",
                "RUTA", "FECHA", "ESTADO", "CONDUCTOR", "PAQS", "PESO KG", "OCUP. %", "HORAS");
        System.out.println("-".repeat(118));
        for (HistorialRutaDTO h : historial) {
            System.out.printf(" %-12s | %-10s | %-12s | %-30.30s | %5d | %9.2f | %9.2f | %6s%n",
                    h.codigoRuta(), h.fechaProgramada(), h.estadoRuta(), h.conductor(),
                    h.paquetes(), h.pesoKg(), h.ocupacionPct(),
                    h.horasRuta() == null ? "-" : h.horasRuta());
        }
        System.out.printf("%n  %d ruta(s) · ocupación promedio %.2f%%%n",
                historial.size(), controller.ocupacionPromedio(historial));
    }

    private void seguimientoPaquete() throws SQLException {
        String codigo = ConsolaUtil.leerTexto("Código de seguimiento");
        ConsolaUtil.imprimir(controller.seguimientoPaquete(codigo));
    }
}
