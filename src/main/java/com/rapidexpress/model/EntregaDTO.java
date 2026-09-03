package com.rapidexpress.model.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Una entrega realizada por un conductor.
 * Alimenta el reporte "entregas por conductor en un rango de fechas".
 */
public record EntregaDTO(
        String conductor,
        String codigoRuta,
        LocalDate fechaRuta,
        String placa,
        String codigoSeguimiento,
        String descripcion,
        double pesoKg,
        String direccionDestino,
        String resultado,
        LocalDateTime fechaEntrega) {
}
