/**
 * Utilidades compartidas por los tres modulos.
 *
 * <ul>
 *   <li>{@link com.rapidexpress.util.ConexionBD} &mdash; fabrica centralizada de
 *       conexiones JDBC. Lee las credenciales de
 *       {@code src/main/resources/database.properties} (fuera del control de
 *       versiones). Es una clase compartida: se fusiona una sola vez.</li>
 *   <li>{@link com.rapidexpress.util.TransaccionUtil} &mdash; ejecuta un bloque
 *       dentro de una transaccion con commit/rollback automatico y reintento
 *       ante deadlock. Sostiene el requisito de consistencia ante operaciones
 *       concurrentes desde varias terminales.</li>
 * </ul>
 */
package com.rapidexpress.util;
