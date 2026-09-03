package com.rapidexpress.model;

/**
 * Estados validos de un conductor.
 * EN_RUTA no aparece en el enunciado del modulo 2 pero si en el modulo 4
 * ("al iniciar una ruta el estado del conductor debe cambiar a En Ruta"),
 * por eso se incluye aqui. Solo lo asigna el sistema, nunca el usuario.
 */
public enum EstadoConductor {

    ACTIVO("Activo"),
    DE_VACACIONES("De Vacaciones"),
    INACTIVO("Inactivo"),
    EN_RUTA("En Ruta");

    private final String etiqueta;

    EstadoConductor(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public static EstadoConductor desdeEtiqueta(String etiqueta) {
        for (EstadoConductor e : values()) {
            if (e.etiqueta.equalsIgnoreCase(etiqueta)) {
                return e;
            }
        }
        throw new IllegalArgumentException("Estado de conductor no valido: " + etiqueta);
    }

    @Override
    public String toString() {
        return etiqueta;
    }
}
