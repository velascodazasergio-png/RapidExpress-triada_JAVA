package com.rapidexpress.examen;

import com.rapidexpress.excepcion.NegocioException;

import java.math.BigDecimal;

/**
 * Utilidades de VERIFICACION DE DATOS reutilizables.
 *
 * <p>Cada metodo comprueba una condicion, lanza {@link NegocioException} con
 * un mensaje claro si no se cumple y, cuando aplica, devuelve el valor ya
 * normalizado (trim / mayusculas) para poder encadenarlo.</p>
 */
public final class ValidacionUtil {

    // Constructor privado: clase de utilidad, no se instancia.
    private ValidacionUtil() {
    }

    /** Exige que el texto no sea nulo ni vacio; devuelve el texto sin espacios sobrantes. */
    public static String texto(String campo, String valor) throws NegocioException {
        if (valor == null || valor.isBlank()) {
            throw new NegocioException("El campo '" + campo + "' es obligatorio.");
        }
        return valor.trim();
    }

    /** Como {@link #texto} pero ademas rechaza los textos mas largos que {@code max}. */
    public static String textoMax(String campo, String valor, int max) throws NegocioException {
        String v = texto(campo, valor);
        if (v.length() > max) {
            throw new NegocioException("El campo '" + campo + "' admite maximo " + max + " caracteres.");
        }
        return v;
    }

    /** Comprueba que un entero este dentro de [min, max] (ambos incluidos). */
    public static int enteroEnRango(String campo, int valor, int min, int max) throws NegocioException {
        if (valor < min || valor > max) {
            throw new NegocioException(
                    "El campo '" + campo + "' debe estar entre " + min + " y " + max + " (recibido " + valor + ").");
        }
        return valor;
    }

    /** Exige un decimal estrictamente mayor que cero (peso, capacidad, costo...). */
    public static BigDecimal positivo(String campo, BigDecimal valor) throws NegocioException {
        if (valor == null || valor.signum() <= 0) {
            throw new NegocioException("El campo '" + campo + "' debe ser un numero mayor que cero.");
        }
        return valor;
    }

    /** Exige un decimal mayor o igual que cero (permite el valor 0). */
    public static BigDecimal noNegativo(String campo, BigDecimal valor) throws NegocioException {
        if (valor == null || valor.signum() < 0) {
            throw new NegocioException("El campo '" + campo + "' no puede ser negativo.");
        }
        return valor;
    }
}
