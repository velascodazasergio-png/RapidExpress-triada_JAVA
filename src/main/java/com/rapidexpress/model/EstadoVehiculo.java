package com.rapidexpress.model;

/** Estados validos de un vehiculo. La etiqueta es exactamente el valor guardado en el ENUM de MySQL. */
public enum EstadoVehiculo {

    DISPONIBLE("Disponible"),
    EN_RUTA("En Ruta"),
    EN_MANTENIMIENTO("En Mantenimiento");

    private final String etiqueta;

    EstadoVehiculo(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public static EstadoVehiculo desdeEtiqueta(String etiqueta) {
        for (EstadoVehiculo e : values()) {
            if (e.etiqueta.equalsIgnoreCase(etiqueta)) {
                return e;
            }
        }
        throw new IllegalArgumentException("Estado de vehiculo no valido: " + etiqueta);
    }

    @Override
    public String toString() {
        return etiqueta;
    }
}
