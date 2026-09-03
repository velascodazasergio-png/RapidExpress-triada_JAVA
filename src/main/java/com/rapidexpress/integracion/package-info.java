/**
 * Contrato con el modulo de auditoria (Persona 3).
 *
 * <p>Los modulos de paquetes y rutas solo avisan "paso esto" llamando a
 * {@link com.rapidexpress.integracion.AuditoriaGateway#registrar}. Donde se
 * escribe el registro (archivo, tabla {@code auditoria}, o ambos) es decision de
 * la Persona 3.</p>
 *
 * <p>Mientras esa implementacion no exista, {@link com.rapidexpress.integracion.Auditoria}
 * entrega por defecto {@link com.rapidexpress.integracion.AuditoriaConsola} y el
 * proyecto compila y corre. Al integrar, la Persona 3 ejecuta una unica linea
 * antes de mostrar los menus:
 * {@code Auditoria.usar(new AuditoriaService());}</p>
 */
package com.rapidexpress.integracion;
