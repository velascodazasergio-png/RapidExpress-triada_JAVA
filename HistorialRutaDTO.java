package com.rapidexpress.model.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Una ruta dentro del historial de un vehículo.
 * Alimenta el reporte "historial de rutas de un vehículo".
 */
public record HistorialRutaDTO(
        String placa,
        String codigoRuta,
        LocalDate fechaProgramada,
        String estadoRuta,
        String conductor,
        int paquetes,
        double pesoKg,
        double ocupacionPct,
        LocalDateTime fechaInicio,
        LocalDateTime fechaFin,
        Integer horasRuta) {
}
