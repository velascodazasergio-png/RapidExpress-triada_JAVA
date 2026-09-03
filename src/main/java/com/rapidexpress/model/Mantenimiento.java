package com.rapidexpress.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Entidad Mantenimiento. Mapea la tabla 'mantenimientos' (1:N con vehiculos). */
public class Mantenimiento {

    /** Tipo de mantenimiento; coincide con el ENUM de la tabla. */
    public enum Tipo {
        PREVENTIVO("Preventivo"),
        CORRECTIVO("Correctivo");

        private final String etiqueta;

        Tipo(String etiqueta) { this.etiqueta = etiqueta; }

        public String getEtiqueta() { return etiqueta; }

        public static Tipo desdeEtiqueta(String etiqueta) {
            for (Tipo t : values()) {
                if (t.etiqueta.equalsIgnoreCase(etiqueta)) return t;
            }
            throw new IllegalArgumentException("Tipo de mantenimiento no valido: " + etiqueta);
        }

        @Override
        public String toString() { return etiqueta; }
    }

    private int idMantenimiento;
    private int idVehiculo;
    private Tipo tipo;
    private String descripcion;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private BigDecimal costo;
    private String taller;

    public Mantenimiento() {
        this.costo = BigDecimal.ZERO;
    }

    public int getIdMantenimiento() { return idMantenimiento; }
    public void setIdMantenimiento(int idMantenimiento) { this.idMantenimiento = idMantenimiento; }

    public int getIdVehiculo() { return idVehiculo; }
    public void setIdVehiculo(int idVehiculo) { this.idVehiculo = idVehiculo; }

    public Tipo getTipo() { return tipo; }
    public void setTipo(Tipo tipo) { this.tipo = tipo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public LocalDate getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDate fechaInicio) { this.fechaInicio = fechaInicio; }

    public LocalDate getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDate fechaFin) { this.fechaFin = fechaFin; }

    public BigDecimal getCosto() { return costo; }
    public void setCosto(BigDecimal costo) { this.costo = costo; }

    public String getTaller() { return taller; }
    public void setTaller(String taller) { this.taller = taller; }

    public boolean estaAbierto() { return fechaFin == null; }

    @Override
    public String toString() {
        return String.format("[%d] %s | %s | inicio: %s | fin: %s | $%s | %s",
                idMantenimiento, tipo, descripcion, fechaInicio,
                fechaFin == null ? "EN CURSO" : fechaFin, costo, taller == null ? "-" : taller);
    }
}
