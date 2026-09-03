package com.rapidexpress.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Paquete registrado en el sistema, identificado por un codigo de seguimiento unico. */
public class Paquete {

    private int idPaquete;
    private String codigoSeguimiento;
    private String descripcion;
    private BigDecimal pesoKg;
    private BigDecimal largoCm = BigDecimal.ZERO;
    private BigDecimal anchoCm = BigDecimal.ZERO;
    private BigDecimal altoCm = BigDecimal.ZERO;
    private int idRemitente;
    private int idDestinatario;
    private String direccionOrigen;
    private String ciudadOrigen;
    private String direccionDestino;
    private String ciudadDestino;
    private EstadoPaquete estado = EstadoPaquete.EN_BODEGA;
    private LocalDateTime fechaRegistro;
    private LocalDateTime fechaEntrega;

    // Solo lectura, se llenan con JOIN para mostrar en consola
    private String nombreRemitente;
    private String nombreDestinatario;

    public Paquete() {
    }

    public int getIdPaquete() { return idPaquete; }
    public void setIdPaquete(int idPaquete) { this.idPaquete = idPaquete; }

    public String getCodigoSeguimiento() { return codigoSeguimiento; }
    public void setCodigoSeguimiento(String codigoSeguimiento) { this.codigoSeguimiento = codigoSeguimiento; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public BigDecimal getPesoKg() { return pesoKg; }
    public void setPesoKg(BigDecimal pesoKg) { this.pesoKg = pesoKg; }

    public BigDecimal getLargoCm() { return largoCm; }
    public void setLargoCm(BigDecimal largoCm) { this.largoCm = largoCm; }

    public BigDecimal getAnchoCm() { return anchoCm; }
    public void setAnchoCm(BigDecimal anchoCm) { this.anchoCm = anchoCm; }

    public BigDecimal getAltoCm() { return altoCm; }
    public void setAltoCm(BigDecimal altoCm) { this.altoCm = altoCm; }

    public int getIdRemitente() { return idRemitente; }
    public void setIdRemitente(int idRemitente) { this.idRemitente = idRemitente; }

    public int getIdDestinatario() { return idDestinatario; }
    public void setIdDestinatario(int idDestinatario) { this.idDestinatario = idDestinatario; }

    public String getDireccionOrigen() { return direccionOrigen; }
    public void setDireccionOrigen(String direccionOrigen) { this.direccionOrigen = direccionOrigen; }

    public String getCiudadOrigen() { return ciudadOrigen; }
    public void setCiudadOrigen(String ciudadOrigen) { this.ciudadOrigen = ciudadOrigen; }

    public String getDireccionDestino() { return direccionDestino; }
    public void setDireccionDestino(String direccionDestino) { this.direccionDestino = direccionDestino; }

    public String getCiudadDestino() { return ciudadDestino; }
    public void setCiudadDestino(String ciudadDestino) { this.ciudadDestino = ciudadDestino; }

    public EstadoPaquete getEstado() { return estado; }
    public void setEstado(EstadoPaquete estado) { this.estado = estado; }

    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    public LocalDateTime getFechaEntrega() { return fechaEntrega; }
    public void setFechaEntrega(LocalDateTime fechaEntrega) { this.fechaEntrega = fechaEntrega; }

    public String getNombreRemitente() { return nombreRemitente; }
    public void setNombreRemitente(String nombreRemitente) { this.nombreRemitente = nombreRemitente; }

    public String getNombreDestinatario() { return nombreDestinatario; }
    public void setNombreDestinatario(String nombreDestinatario) { this.nombreDestinatario = nombreDestinatario; }

    @Override
    public String toString() {
        return String.format("%-16s %-26s %8s kg  %-14s -> %-14s %s",
                codigoSeguimiento, recortar(descripcion, 26), pesoKg,
                ciudadOrigen, ciudadDestino, estado.getEtiqueta());
    }

    private String recortar(String texto, int max) {
        if (texto == null) {
            return "";
        }
        return texto.length() <= max ? texto : texto.substring(0, max - 3) + "...";
    }
}
