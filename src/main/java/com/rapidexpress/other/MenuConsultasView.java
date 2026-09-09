package com.rapidexpress.examen;

import com.rapidexpress.excepcion.NegocioException;
import com.rapidexpress.model.Vehiculo;
import com.rapidexpress.view.ConsolaUtil;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Menu de consola que reune las consultas de ejemplo, la verificacion de
 * datos y el reporte nuevo. Se puede lanzar desde {@link AppConsultas} o
 * enganchar en el menu principal (ver EXAMEN_GUIA.md, "Integrar en Main").
 *
 * <p>Sigue el mismo esqueleto que {@code ReporteView} / {@code AuditoriaView}:
 * bucle {@code while}, {@code switch} de opciones y captura de
 * {@link NegocioException} (validaciones) y {@link SQLException} (base de datos)
 * para que un error nunca tumbe la aplicacion.</p>
 */
public class MenuConsultasView {

    private final ConsultaDAO consultas = new ConsultaDAO();
    private final ReporteActividadController actividad = new ReporteActividadController();

    /** Muestra el menu en bucle y despacha cada opcion; centraliza el manejo de errores. */
    public void mostrar() {
        boolean salir = false;
        while (!salir) {
            ConsolaUtil.titulo("Consultas, validaciones y reportes (examen)");
            System.out.println("""
                      1. Vehiculos con capacidad minima (consulta con parametro)
                      2. Conductores activos sin vehiculo (LEFT JOIN + IS NULL)
                      3. Top N clientes remitentes (GROUP BY + LIMIT)
                      4. Paquetes entregados entre dos fechas (rango + JOIN)
                      5. Destinatarios frecuentes (GROUP BY + HAVING)
                      6. Costo de mantenimiento por vehiculo (SUM)
                      7. Conteo de paquetes por estado (agregacion a Map)
                      8. Verificar datos de un vehiculo (ValidacionUtil)
                      9. Reporte NUEVO: actividad por vehiculo
                      0. Volver
                    """);

            int opcion = ConsolaUtil.leerEntero("Opcion");
            try {
                switch (opcion) {
                    case 1 -> vehiculosPorCapacidad();
                    case 2 -> ConsolaUtil.imprimir(consultas.conductoresSinVehiculo());
                    case 3 -> ConsolaUtil.imprimir(
                            consultas.topClientesRemitentes(ConsolaUtil.leerEntero("Cuantos (N)")));
                    case 4 -> paquetesEntregados();
                    case 5 -> ConsolaUtil.imprimir(
                            consultas.destinatariosFrecuentes(ConsolaUtil.leerEntero("Minimo de paquetes")));
                    case 6 -> ConsolaUtil.imprimir(consultas.costoMantenimientoPorVehiculo());
                    case 7 -> conteoPorEstado();
                    case 8 -> verificarVehiculo();
                    case 9 -> reporteActividad();
                    case 0 -> salir = true;
                    default -> ConsolaUtil.error("Opcion no reconocida.");
                }
            } catch (NegocioException e) {
                ConsolaUtil.error(e.getMessage());
            } catch (SQLException e) {
                ConsolaUtil.error("Error consultando la base de datos: " + e.getMessage());
            }

            if (!salir) {
                ConsolaUtil.pausar();
            }
        }
    }

    /** Pide una capacidad minima y lista los vehiculos que la alcanzan. */
    private void vehiculosPorCapacidad() throws SQLException {
        BigDecimal kg = ConsolaUtil.leerDecimal("Capacidad minima en kg");
        List<Vehiculo> lista = consultas.vehiculosConCapacidadMinima(kg);
        ConsolaUtil.titulo("Vehiculos con capacidad >= " + kg + " kg");
        if (lista.isEmpty()) {
            ConsolaUtil.aviso("Ningun vehiculo cumple ese minimo.");
            return;
        }
        lista.forEach(v -> System.out.println("  " + v));
        System.out.println("\n  " + lista.size() + " vehiculo(s).");
    }

    /** Pide un rango de fechas, lo valida y muestra los paquetes entregados dentro de el. */
    private void paquetesEntregados() throws SQLException, NegocioException {
        LocalDate desde = ConsolaUtil.leerFecha("Fecha inicial");
        LocalDate hasta = ConsolaUtil.leerFecha("Fecha final");
        ValidacionUtil.rangoFechas(desde, hasta);
        ConsolaUtil.imprimir(consultas.paquetesEntregadosEntre(desde, hasta));
    }

    /** Muestra el conteo de paquetes por estado y su total, calculado en Java. */
    private void conteoPorEstado() throws SQLException {
        Map<String, Integer> conteo = consultas.conteoPaquetesPorEstado();
        ConsolaUtil.titulo("Paquetes por estado");
        if (conteo.isEmpty()) {
            ConsolaUtil.aviso("No hay paquetes registrados.");
            return;
        }
        int total = 0;
        for (Map.Entry<String, Integer> e : conteo.entrySet()) {
            System.out.printf("  %-18s %4d%n", e.getKey(), e.getValue());
            total += e.getValue();
        }
        System.out.printf("  %-18s %4d%n", "TOTAL", total);
    }

    /** Lee los datos de un vehiculo, los valida uno a uno y comprueba que la placa no exista. */
    private void verificarVehiculo() throws NegocioException, SQLException {
        String placa = ValidacionUtil.placa(ConsolaUtil.leerTexto("Placa (ABC123)"));
        String marca = ValidacionUtil.textoMax("marca", ConsolaUtil.leerTexto("Marca"), 50);
        String modelo = ValidacionUtil.textoMax("modelo", ConsolaUtil.leerTexto("Modelo"), 50);
        int anio = ValidacionUtil.anioFabricacion(ConsolaUtil.leerEntero("Anio de fabricacion"));
        BigDecimal capacidad = ValidacionUtil.positivo("capacidad", ConsolaUtil.leerDecimal("Capacidad kg"));

        if (consultas.existePlaca(placa)) {
            throw new NegocioException("Ya existe un vehiculo con la placa " + placa + ".");
        }

        ConsolaUtil.exito("Datos validos. Se podria registrar: "
                + placa + " | " + marca + " " + modelo + " (" + anio + ") | " + capacidad + " kg");
    }

    /** Imprime el reporte de actividad por vehiculo con su cabecera, filas y totales. */
    private void reporteActividad() throws SQLException {
        List<ActividadVehiculoDTO> filas = actividad.generar();
        ConsolaUtil.titulo("Actividad por vehiculo");
        if (filas.isEmpty()) {
            ConsolaUtil.aviso("No hay vehiculos registrados.");
            return;
        }
        System.out.printf(" %-8s | %-12s | %5s | %5s | %5s | %10s | %12s%n",
                "PLACA", "MARCA", "RUT", "FIN", "PAQ", "KG ENTREG.", "MANTEN. $");
        System.out.println("-".repeat(74));
        for (ActividadVehiculoDTO a : filas) {
            System.out.printf(" %-8s | %-12.12s | %5d | %5d | %5d | %10.2f | %12.2f%n",
                    a.placa(), a.marca(), a.rutasTotales(), a.rutasFinalizadas(),
                    a.paquetesEntregados(), a.kgEntregados(), a.costoMantenimiento());
        }
        System.out.printf("%n  %d vehiculo(s) | %d paquetes | %.2f kg | mantenimiento $%.2f | mas productivo: %s%n",
                filas.size(), actividad.totalPaquetes(filas), actividad.totalKg(filas),
                actividad.totalCostoMantenimiento(filas), actividad.vehiculoMasProductivo(filas));
    }
}
