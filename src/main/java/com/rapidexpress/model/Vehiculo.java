package com.rapidexpress.model;

import java.math.BigDecimal;

/** Entidad Vehiculo. Mapea la tabla 'vehiculos'. */
public class Vehiculo {

    private int idVehiculo;
    private String placa;
    private String marca;
    private String modelo;
    private int anioFabricacion;
    private BigDecimal capacidadCargaKg;
    private EstadoVehiculo estado;

    public Vehiculo() {
        this.estado = EstadoVehiculo.DISPONIBLE;
    }

    public Vehiculo(String placa, String marca, String modelo, int anioFabricacion, BigDecimal capacidadCargaKg) {
        this();
        this.placa = placa;
        this.marca = marca;
        this.modelo = modelo;
        this.anioFabricacion = anioFabricacion;
        this.capacidadCargaKg = capacidadCargaKg;
    }

    public int getIdVehiculo() { return idVehiculo; }
    public void setIdVehiculo(int idVehiculo) { this.idVehiculo = idVehiculo; }

    public String getPlaca() { return placa; }
    public void setPlaca(String placa) { this.placa = placa == null ? null : placa.trim().toUpperCase(); }

    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }

    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }

    public int getAnioFabricacion() { return anioFabricacion; }
    public void setAnioFabricacion(int anioFabricacion) { this.anioFabricacion = anioFabricacion; }

    public BigDecimal getCapacidadCargaKg() { return capacidadCargaKg; }
    public void setCapacidadCargaKg(BigDecimal capacidadCargaKg) { this.capacidadCargaKg = capacidadCargaKg; }

    public EstadoVehiculo getEstado() { return estado; }
    public void setEstado(EstadoVehiculo estado) { this.estado = estado; }

    @Override
    public String toString() {
        return String.format("[%d] %s | %s %s (%d) | %.2f kg | %s",
                idVehiculo, placa, marca, modelo, anioFabricacion, capacidadCargaKg, estado);
    }
}
