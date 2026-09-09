package com.rapidexpress.examen;

/**
 * Una fila del reporte "actividad por vehiculo": cuanto ha trabajado cada
 * unidad de la flota y cuanto ha costado mantenerla.
 *
 * <p>Es un {@code record} inmutable, igual que {@code EntregaDTO} y
 * {@code HistorialRutaDTO} del modulo de reportes original.</p>
 */
public record ActividadVehiculoDTO(
        String placa,
        String marca,
        int rutasTotales,
        int rutasFinalizadas,
        int paquetesEntregados,
        double kgEntregados,
        double costoMantenimiento) {

    /** Porcentaje de rutas que llegaron a Finalizada. */
    public double porcentajeFinalizacion() {
        return rutasTotales == 0 ? 0.0 : rutasFinalizadas * 100.0 / rutasTotales;
    }
}
