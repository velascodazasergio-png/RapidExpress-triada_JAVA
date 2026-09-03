/**
 * Vistas de linea de comandos (CLI).
 *
 * <p>Cada {@code *MenuView} dibuja un menu, lee la entrada del usuario con
 * {@link com.rapidexpress.view.ConsolaUtil} y llama al controlador
 * correspondiente. Capturan {@link com.rapidexpress.excepcion.NegocioException}
 * y muestran solo el mensaje, sin volcar el stack trace.</p>
 *
 * <p>Punto de integracion: el Main definitivo (Persona 3) monta cada modulo con
 * una sola linea, por ejemplo {@code new RutaMenuView().mostrar();}</p>
 *
 * <p>Regla critica para la CLI en equipo: existe un unico
 * {@link java.util.Scanner} sobre {@code System.in}, expuesto por
 * {@link com.rapidexpress.view.ConsolaUtil#scanner()}. Ninguna vista debe crear
 * el suyo o la consola empieza a saltarse lecturas.</p>
 */
package com.rapidexpress.view;
