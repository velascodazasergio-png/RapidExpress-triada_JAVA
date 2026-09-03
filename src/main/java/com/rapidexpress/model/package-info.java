/**
 * Entidades del dominio y enumeraciones de estado.
 *
 * <p>Son objetos planos (POJO) que reflejan las tablas de la base de datos:
 * {@link com.rapidexpress.model.Vehiculo}, {@link com.rapidexpress.model.Conductor},
 * {@link com.rapidexpress.model.Mantenimiento} y
 * {@link com.rapidexpress.model.Asignacion} para el modulo de flota (Persona 1);
 * {@link com.rapidexpress.model.Cliente}, {@link com.rapidexpress.model.Paquete}
 * y {@link com.rapidexpress.model.Ruta} para el modulo de paquetes y rutas
 * (Persona 2).</p>
 *
 * <p>No contienen reglas de negocio (eso vive en {@code service}) ni acceso a
 * datos (eso vive en {@code dao}). Los enum de estado
 * ({@link com.rapidexpress.model.EstadoVehiculo},
 * {@link com.rapidexpress.model.EstadoConductor},
 * {@link com.rapidexpress.model.EstadoPaquete},
 * {@link com.rapidexpress.model.EstadoRuta}) exponen una etiqueta que coincide
 * exactamente con el valor del ENUM de MySQL, para que consola, base de datos y
 * reportes muestren siempre el mismo texto.</p>
 */
package com.rapidexpress.model;
