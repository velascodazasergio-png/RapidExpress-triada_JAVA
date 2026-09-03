/**
 * Excepciones propias del sistema.
 *
 * <p>Contiene {@link com.rapidexpress.excepcion.NegocioException}, la unica
 * excepcion de regla de negocio del proyecto. La lanza la capa {@code service}
 * cuando una operacion es invalida (placa repetida, vehiculo En Ruta, capacidad
 * excedida, etc.) y la capa {@code view} la captura para mostrar el mensaje al
 * usuario sin stack trace. Es una clase compartida por los tres modulos.</p>
 */
package com.rapidexpress.excepcion;
