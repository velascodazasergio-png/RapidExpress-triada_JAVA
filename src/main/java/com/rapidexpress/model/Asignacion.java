package com.rapidexpress.model;

import java.time.LocalDateTime;

/**
 * Entidad Asignacion (conductor - vehiculo).
 * Mapea la tabla 'asignaciones_conductor_vehiculo'.
 * fechaFin == null significa que la asignacion esta vigente.
 */
public class Asignacion {

    private int idAsignacion;
    private int idConductor;
    private int idVehiculo;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;

    // Campos de apoyo para mostrar en pantalla (vienen de los JOIN, no de la tabla).
    private String nombreConductor;
    private String placaVehiculo;

    public Asignacion() {
    }

    public Asignacion(int idConductor, int idVehiculo) {
        this.idConductor = idConductor;
        this.idVehiculo = idVehiculo;
        this.fechaInicio = LocalDateTime.now();
    }

    public int getIdAsignacion() { return idAsignacion; }
    public void setIdAsignacion(int idAsignacion) { this.idAsignacion = idAsignacion; }

    public int getIdConductor() { return idConductor; }
    public void setIdConductor(int idConductor) { this.idConductor = idConductor; }

    public int getIdVehiculo() { return idVehiculo; }
    public void setIdVehiculo(int idVehiculo) { this.idVehiculo = idVehiculo; }

    public LocalDateTime getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDateTime fechaInicio) { this.fechaInicio = fechaInicio; }

    public LocalDateTime getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDateTime fechaFin) { this.fechaFin = fechaFin; }

    public String getNombreConductor() { return nombreConductor; }
    public void setNombreConductor(String nombreConductor) { this.nombreConductor = nombreConductor; }

    public String getPlacaVehiculo() { return placaVehiculo; }
    public void setPlacaVehiculo(String placaVehiculo) { this.placaVehiculo = placaVehiculo; }

    public boolean estaVigente() { return fechaFin == null; }

    @Override
    public String toString() {
        return String.format("[%d] %s -> %s | desde: %s | hasta: %s",
                idAsignacion,
                nombreConductor == null ? "conductor " + idConductor : nombreConductor,
                placaVehiculo == null ? "vehiculo " + idVehiculo : placaVehiculo,
                fechaInicio, fechaFin == null ? "VIGENTE" : fechaFin);
    }
}
