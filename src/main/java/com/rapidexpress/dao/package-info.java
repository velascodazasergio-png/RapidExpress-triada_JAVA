/**
 * Capa de acceso a datos (DAO): todo el SQL del sistema vive aqui.
 *
 * <p>Convenciones que respetan todos los DAO de los tres modulos:</p>
 * <ul>
 *   <li>Reciben la {@link java.sql.Connection} como parametro; nunca la abren ni
 *       la cierran. Asi la capa {@code service} puede agrupar varias operaciones
 *       en una misma transaccion.</li>
 *   <li>No validan reglas de negocio ni lanzan {@code NegocioException}: solo
 *       propagan {@link java.sql.SQLException}.</li>
 *   <li>Usan siempre {@link java.sql.PreparedStatement} con parametros, nunca
 *       concatenacion de cadenas.</li>
 *   <li>Los metodos {@code buscarPorIdBloqueando} ejecutan
 *       {@code SELECT ... FOR UPDATE} y solo deben llamarse dentro de una
 *       transaccion.</li>
 * </ul>
 */
package com.rapidexpress.dao;
