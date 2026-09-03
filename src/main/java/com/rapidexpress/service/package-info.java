/**
 * Capa de reglas de negocio.
 *
 * <p>Aqui vive TODA la validacion: los DAO no validan y las vistas tampoco.
 * Cada servicio traduce los errores de datos ({@link java.sql.SQLException}) y
 * las operaciones invalidas en {@link com.rapidexpress.excepcion.NegocioException}
 * con un mensaje ya redactado para el usuario final.</p>
 *
 * <p>Reparto por modulo:</p>
 * <ul>
 *   <li>{@link com.rapidexpress.service.FlotaService} y
 *       {@link com.rapidexpress.service.ConductorService} — flota, personal y la
 *       asignacion conductor&ndash;vehiculo (Persona 1).</li>
 *   <li>{@link com.rapidexpress.service.PaqueteService} y
 *       {@link com.rapidexpress.service.RutaService} — paquetes, clientes y el
 *       ciclo de vida de las hojas de ruta (Persona 2).</li>
 * </ul>
 *
 * <p>Las operaciones que cruzan modulos (iniciar/cerrar una ruta mueve tambien
 * el estado de la flota) se ejecutan en una unica transaccion mediante las
 * variantes que reciben una {@link java.sql.Connection} en curso, de modo que
 * todo sea una sola unidad ACID sin logica de compensacion.</p>
 */
package com.rapidexpress.service;
