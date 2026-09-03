package com.rapidexpress.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Hoja de ruta diaria: un vehiculo + su conductor asignado + un conjunto de paquetes.
 *
 * El conductor NO se elige a mano: sale de la asignacion vigente que administra
 * el modulo de la Persona 1 (ConductorController.obtenerConductorAsignadoA).
 * Se guarda en la ruta para conservar el historico aunque la asignacion cambie.
 */
public class Ruta {

    private int idRuta;
    private String codigo;
    private LocalDate fecha;
    private int idVehiculo;
    private int idConductor;
    private EstadoRuta estado = EstadoRuta.PLANIFICADA;
    private String observaciones;
    private LocalDateTime iniciadaEn;
    private LocalDateTime finalizadaEn;

    private List<Paquete> paquetes = new ArrayList<>();

    // Datos de la flota traidos con JOIN, solo para mostrar
    private String placaVehiculo;
    private String nombreConductor;
    private BigDecimal capacidadVehiculoKg;

    public BigDecimal getPesoTotalKg() {
        BigDecimal total = BigDecimal.ZERO;
        for (Paquete p : paquetes) {
            total = total.add(p.getPesoKg());
        }
        return total;
    }

    public int getIdRuta() { return idRuta; }
    public void setIdRuta(int idRuta) { this.idRuta = idRuta; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public int getIdVehiculo() { return idVehiculo; }
    public void setIdVehiculo(int idVehiculo) { this.idVehiculo = idVehiculo; }

    public int getIdConductor() { return idConductor; }
    public void setIdConductor(int idConductor) { this.idConductor = idConductor; }

    public EstadoRuta getEstado() { return estado; }
    public void setEstado(EstadoRuta estado) { this.estado = estado; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    public LocalDateTime getIniciadaEn() { return iniciadaEn; }
    public void setIniciadaEn(LocalDateTime iniciadaEn) { this.iniciadaEn = iniciadaEn; }

    public LocalDateTime getFinalizadaEn() { return finalizadaEn; }
    public void setFinalizadaEn(LocalDateTime finalizadaEn) { this.finalizadaEn = finalizadaEn; }

    public List<Paquete> getPaquetes() { return paquetes; }
    public void setPaquetes(List<Paquete> paquetes) { this.paquetes = paquetes; }

    public String getPlacaVehiculo() { return placaVehiculo; }
    public void setPlacaVehiculo(String placaVehiculo) { this.placaVehiculo = placaVehiculo; }

    public String getNombreConductor() { return nombreConductor; }
    public void setNombreConductor(String nombreConductor) { this.nombreConductor = nombreConductor; }

    public BigDecimal getCapacidadVehiculoKg() { return capacidadVehiculoKg; }
    public void setCapacidadVehiculoKg(BigDecimal capacidadVehiculoKg) { this.capacidadVehiculoKg = capacidadVehiculoKg; }

    @Override
    public String toString() {
        return String.format("%-16s %s  veh %-8s cond %-28s %2d paq  %9s kg  %s",
                codigo, fecha,
                placaVehiculo == null ? String.valueOf(idVehiculo) : placaVehiculo,
                nombreConductor == null ? String.valueOf(idConductor) : recortar(nombreConductor, 28),
                paquetes.size(), getPesoTotalKg(), estado.getEtiqueta());
    }

    private String recortar(String texto, int max) {
        return texto.length() <= max ? texto : texto.substring(0, max - 3) + "...";
    }
}
