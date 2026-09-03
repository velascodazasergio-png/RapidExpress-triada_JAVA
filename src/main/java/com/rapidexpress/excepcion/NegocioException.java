package com.rapidexpress.excepcion;

/**
 * Excepcion de regla de negocio. La lanza la capa Service cuando una operacion
 * es invalida (placa repetida, vehiculo en ruta, conductor ya asignado, etc.).
 * La Vista la captura y muestra el mensaje al usuario sin volcar un stack trace.
 */
public class NegocioException extends Exception {

    private static final long serialVersionUID = 1L;

    // Crea la excepcion con un mensaje para el usuario.
    public NegocioException(String mensaje) {
        super(mensaje);
    }

    // Crea la excepcion con un mensaje y la causa original.
    public NegocioException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
