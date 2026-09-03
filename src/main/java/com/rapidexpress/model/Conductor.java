package com.rapidexpress.model;

/** Entidad Conductor. Mapea la tabla 'conductores'. */
public class Conductor {

    private int idConductor;
    private String identificacion;
    private String nombreCompleto;
    private TipoLicencia tipoLicencia;
    private String contacto;
    private EstadoConductor estado;

    public Conductor() {
        this.estado = EstadoConductor.ACTIVO;
    }

    public Conductor(String identificacion, String nombreCompleto, TipoLicencia tipoLicencia, String contacto) {
        this();
        this.identificacion = identificacion;
        this.nombreCompleto = nombreCompleto;
        this.tipoLicencia = tipoLicencia;
        this.contacto = contacto;
    }

    public int getIdConductor() { return idConductor; }
    public void setIdConductor(int idConductor) { this.idConductor = idConductor; }

    public String getIdentificacion() { return identificacion; }
    public void setIdentificacion(String identificacion) {
        this.identificacion = identificacion == null ? null : identificacion.trim();
    }

    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }

    public TipoLicencia getTipoLicencia() { return tipoLicencia; }
    public void setTipoLicencia(TipoLicencia tipoLicencia) { this.tipoLicencia = tipoLicencia; }

    public String getContacto() { return contacto; }
    public void setContacto(String contacto) { this.contacto = contacto; }

    public EstadoConductor getEstado() { return estado; }
    public void setEstado(EstadoConductor estado) { this.estado = estado; }

    @Override
    public String toString() {
        return String.format("[%d] %s | %s | Lic. %s | Tel. %s | %s",
                idConductor, identificacion, nombreCompleto, tipoLicencia, contacto, estado);
    }
}
