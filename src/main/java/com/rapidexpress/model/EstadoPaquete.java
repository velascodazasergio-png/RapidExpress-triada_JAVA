package com.rapidexpress.model;

/**
 * Ciclo de vida del paquete (Persona 2).
 * En Bodega -> Asignado a Ruta -> En Transito -> Entregado | Devuelto
 *
 * Igual que EstadoVehiculo y EstadoConductor, la etiqueta es exactamente el
 * valor guardado en el ENUM de MySQL, para que los reportes de la Persona 3
 * lean lo mismo que muestra la consola.
 */
public enum EstadoPaquete {

    EN_BODEGA("En Bodega"),
    ASIGNADO_A_RUTA("Asignado a Ruta"),
    EN_TRANSITO("En Transito"),
    ENTREGADO("Entregado"),
    DEVUELTO("Devuelto");

    private final String etiqueta;

    EstadoPaquete(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public static EstadoPaquete desdeEtiqueta(String etiqueta) {
        for (EstadoPaquete e : values()) {
            if (e.etiqueta.equalsIgnoreCase(etiqueta)) {
                return e;
            }
        }
        throw new IllegalArgumentException("Estado de paquete no valido: " + etiqueta);
    }

    /**
     * Reglas del ciclo de vida: define que transiciones son legales.
     * Un paquete solo se marca Devuelto si estaba En Transito: nunca desde
     * bodega (nunca salio) ni recien asignado a una ruta.
     */
    public boolean puedePasarA(EstadoPaquete destino) {
        switch (this) {
            case EN_BODEGA:       return destino == ASIGNADO_A_RUTA;
            case ASIGNADO_A_RUTA: return destino == EN_TRANSITO || destino == EN_BODEGA;
            case EN_TRANSITO:     return destino == ENTREGADO || destino == DEVUELTO;
            default:              return false;
        }
    }

    public boolean esFinal() {
        return this == ENTREGADO || this == DEVUELTO;
    }

    @Override
    public String toString() {
        return etiqueta;
    }
}
