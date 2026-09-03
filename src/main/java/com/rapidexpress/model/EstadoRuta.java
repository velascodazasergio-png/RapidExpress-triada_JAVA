package com.rapidexpress.model;

/** Estados de una hoja de ruta diaria (Persona 2). */
public enum EstadoRuta {

    PLANIFICADA("Planificada"),
    EN_CURSO("En Curso"),
    FINALIZADA("Finalizada"),
    CANCELADA("Cancelada");

    private final String etiqueta;

    EstadoRuta(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    // Devuelve el valor del enum a partir de su etiqueta.
    public static EstadoRuta desdeEtiqueta(String etiqueta) {
        for (EstadoRuta e : values()) {
            if (e.etiqueta.equalsIgnoreCase(etiqueta)) {
                return e;
            }
        }
        throw new IllegalArgumentException("Estado de ruta no valido: " + etiqueta);
    }

    // Representacion legible para mostrar en consola.
    @Override
    public String toString() {
        return etiqueta;
    }
}
