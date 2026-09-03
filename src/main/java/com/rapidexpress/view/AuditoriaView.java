package com.rapidexpress.view;

import com.rapidexpress.controller.AuditoriaController;
import com.rapidexpress.model.Auditoria;
import com.rapidexpress.util.AuditLogger;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/** Menú de consulta de la bitácora de auditoría. */
public class AuditoriaView {

    private final AuditoriaController controller = new AuditoriaController();

    // Muestra el menu y procesa las opciones del usuario.
    public void mostrar() {
        boolean salir = false;
        while (!salir) {
            ConsolaUtil.titulo("Módulo de auditoría");
            System.out.println("""
                      1. Últimos eventos registrados
                      2. Eventos por rango de fechas
                      3. Traza de una ruta
                      4. Traza de un paquete
                      5. Resumen de actividad
                      6. Ubicación del archivo de bitácora
                      0. Volver al menú principal
                    """);

            int opcion = ConsolaUtil.leerEntero("Opción");
            try {
                switch (opcion) {
                    case 1 -> listar("Últimos eventos",
                            controller.ultimosEventos(ConsolaUtil.leerEntero("¿Cuántos?")));
                    case 2 -> porPeriodo();
                    case 3 -> listar("Traza de la ruta",
                            controller.trazaDeRuta(ConsolaUtil.leerEntero("Id de la ruta")));
                    case 4 -> listar("Traza del paquete",
                            controller.trazaDePaquete(ConsolaUtil.leerEntero("Id del paquete")));
                    case 5 -> ConsolaUtil.imprimir(controller.actividad());
                    case 6 -> ConsolaUtil.aviso("Bitácora centralizada: " + AuditLogger.archivo());
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

    // Pide el rango de fechas y lista los eventos.
    private void porPeriodo() throws SQLException {
        LocalDate desde = ConsolaUtil.leerFecha("Fecha inicial");
        LocalDate hasta = ConsolaUtil.leerFecha("Fecha final");
        String op = ConsolaUtil.leerTexto("Operación (ENTER para todas)");
        listar("Eventos entre " + desde + " y " + hasta,
                controller.porPeriodo(desde, hasta, op));
    }

    // Imprime la lista de eventos con su total.
    private void listar(String titulo, List<Auditoria> eventos) {
        ConsolaUtil.titulo(titulo);
        if (eventos.isEmpty()) {
            ConsolaUtil.aviso("No hay eventos que coincidan con el criterio.");
            return;
        }
        for (Auditoria e : eventos) {
            System.out.println(" " + e.aLineaLog());
        }
        System.out.println("\n  " + eventos.size() + " evento(s).");
    }
}
