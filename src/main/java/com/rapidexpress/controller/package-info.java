/**
 * Controladores: la unica puerta de entrada al sistema para las vistas y para
 * los demas modulos.
 *
 * <p>Cada controlador envuelve un servicio y expone una API estable. No exponen
 * DAOs ni conexiones y no contienen reglas de negocio; solo adaptan parametros
 * (por ejemplo, arman un {@link com.rapidexpress.model.Vehiculo} a partir de
 * datos sueltos) y delegan.</p>
 *
 * <p>Fronteras entre modulos:</p>
 * <ul>
 *   <li>El modulo de rutas (Persona 2) consume flota y personal solo a traves de
 *       {@link com.rapidexpress.controller.FlotaController} y
 *       {@link com.rapidexpress.controller.ConductorController} &mdash; nunca
 *       hace {@code UPDATE} directo sobre {@code vehiculos} o {@code conductores}.</li>
 *   <li>Los metodos que reciben una {@link java.sql.Connection} son variantes
 *       transaccionales: el llamador los invoca dentro de SU transaccion para que
 *       la operacion completa sea atomica.</li>
 * </ul>
 */
package com.rapidexpress.controller;
