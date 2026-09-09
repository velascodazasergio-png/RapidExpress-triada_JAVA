/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.rapidexpress.model.dto;

/**
 *
 * @author camper
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


