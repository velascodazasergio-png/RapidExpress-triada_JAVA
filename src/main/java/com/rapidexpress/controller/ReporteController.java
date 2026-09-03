package com.rapidexpress.controller;

import com.rapidexpress.dao.ReporteDAO;
import com.rapidexpress.model.dto.EntregaDTO;
import com.rapidexpress.model.dto.HistorialRutaDTO;
import com.rapidexpress.model.dto.TablaReporte;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * Lógica del módulo de reportes.
 *
 * <p>Valida los parámetros antes de bajar al DAO y calcula los totales que la
 * vista muestra al pie de cada reporte. La vista no hace cuentas y el DAO no
 * valida: cada capa se queda en lo suyo.</p>
 */
public class ReporteController {

    private final ReporteDAO dao;

    // Constructor por defecto: crea su propio DAO.
    public ReporteController() {
        this(new ReporteDAO());
    }

    /** Constructor con inyección, útil para pruebas con un DAO simulado. */
    public ReporteController(ReporteDAO dao) {
        this.dao = dao;
    }

    // ---------------------------------------------------------------
    //  Reporte 1 · Entregas por conductor en un rango de fechas
    // ---------------------------------------------------------------
    public List<EntregaDTO> entregasPorConductor(int idConductor, LocalDate desde,
                                                 LocalDate hasta) throws SQLException {
        if (idConductor <= 0) {
            throw new IllegalArgumentException("El identificador del conductor no es válido.");
        }
        if (desde == null || hasta == null) {
            throw new IllegalArgumentException("Debe indicar las dos fechas del rango.");
        }
        if (hasta.isBefore(desde)) {
            throw new IllegalArgumentException(
                    "La fecha final no puede ser anterior a la inicial.");
        }
        return dao.entregasPorConductor(idConductor, desde, hasta);
    }

    /** Peso total movido en un conjunto de entregas. */
    public double pesoTotal(List<EntregaDTO> entregas) {
        return entregas.stream().mapToDouble(EntregaDTO::pesoKg).sum();
    }

    // ---------------------------------------------------------------
    //  Reporte 2 · Historial de rutas de un vehículo
    // ---------------------------------------------------------------
    public List<HistorialRutaDTO> historialVehiculo(int idVehiculo) throws SQLException {
        if (idVehiculo <= 0) {
            throw new IllegalArgumentException("El identificador del vehículo no es válido.");
        }
        return dao.historialRutasVehiculo(idVehiculo);
    }

    /** Ocupación promedio del vehículo a lo largo de su historial. */
    public double ocupacionPromedio(List<HistorialRutaDTO> historial) {
        return historial.stream()
                .mapToDouble(HistorialRutaDTO::ocupacionPct)
                .average()
                .orElse(0.0);
    }

    // ---------------------------------------------------------------
    //  Reportes complementarios
    // ---------------------------------------------------------------
    public TablaReporte estadoFlota() throws SQLException {
        return dao.estadoFlota();
    }

    // Devuelve el reporte de desempeno de conductores.
    public TablaReporte desempenoConductores() throws SQLException {
        return dao.desempenoConductores();
    }

    // Devuelve el reporte de ocupacion de rutas.
    public TablaReporte ocupacionRutas() throws SQLException {
        return dao.ocupacionRutas();
    }

    // Devuelve el reporte de paquetes por estado.
    public TablaReporte paquetesPorEstado() throws SQLException {
        return dao.paquetesPorEstado();
    }

    // Devuelve el reporte de seguimiento de un paquete.
    public TablaReporte seguimientoPaquete(String codigo) throws SQLException {
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("Debe indicar el código de seguimiento.");
        }
        return dao.seguimientoPaquete(codigo.trim().toUpperCase());
    }
}
