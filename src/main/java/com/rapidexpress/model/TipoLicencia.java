package com.rapidexpress.model;

/** Categorias de licencia de conduccion admitidas para el personal de la flota. */
public enum TipoLicencia {

    A1, A2, B1, B2, B3, C1, C2, C3;

    /**
     * Convierte un texto ("C2", " c2 ") al enum, tolerando espacios y minusculas.
     *
     * @throws IllegalArgumentException si el texto es {@code null} o no es una categoria valida
     */
    public static TipoLicencia desdeTexto(String texto) {
        if (texto == null) {
            throw new IllegalArgumentException("El tipo de licencia es obligatorio.");
        }
        try {
            return valueOf(texto.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Tipo de licencia no valido: " + texto);
        }
    }
}
