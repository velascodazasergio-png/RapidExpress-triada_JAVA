package com.rapidexpress.model;

/**
 * Cliente del sistema (Persona 2).
 *
 * Decision de diseno: UNA sola tabla en vez de "remitentes" y "destinatarios"
 * separadas, porque la misma persona puede enviar un paquete y recibir otro.
 */
public class Cliente {

    private int idCliente;
    private String documento;
    private String nombre;
    private String telefono;
    private String email;
    private String direccion;
    private String ciudad;

    public Cliente() {
    }

    public Cliente(String documento, String nombre, String telefono, String email,
                   String direccion, String ciudad) {
        this.documento = documento;
        this.nombre = nombre;
        this.telefono = telefono;
        this.email = email;
        this.direccion = direccion;
        this.ciudad = ciudad;
    }

    public int getIdCliente() { return idCliente; }
    public void setIdCliente(int idCliente) { this.idCliente = idCliente; }

    public String getDocumento() { return documento; }
    public void setDocumento(String documento) {
        this.documento = documento == null ? null : documento.trim();
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }

    public String getCiudad() { return ciudad; }
    public void setCiudad(String ciudad) { this.ciudad = ciudad; }

    @Override
    public String toString() {
        return String.format("[%d] %-32s doc: %-14s %s", idCliente, nombre, documento, ciudad);
    }
}
