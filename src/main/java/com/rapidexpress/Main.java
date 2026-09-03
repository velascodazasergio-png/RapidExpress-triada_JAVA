package com.rapidexpress;

import com.rapidexpress.integracion.Auditoria;
import com.rapidexpress.integracion.AuditoriaPersistente;
import com.rapidexpress.util.AuditLogger;
import com.rapidexpress.util.ConexionBD;
import com.rapidexpress.view.AuditoriaView;
import com.rapidexpress.view.ConductorMenuView;
import com.rapidexpress.view.ConsolaUtil;
import com.rapidexpress.view.FlotaMenuView;
import com.rapidexpress.view.PaqueteMenuView;
import com.rapidexpress.view.ReporteView;
import com.rapidexpress.view.RutaMenuView;

/**
 * Punto de entrada unico del sistema: integra los modulos de los tres
 * integrantes bajo un solo menu principal. Es la clase que referencia el
 * {@code pom.xml} ({@code exec.mainClass} y el manifiesto del jar).
 *
 * <p>Reemplaza a las clases {@code MainFlotaPruebas} / {@code MainRutasPruebas},
 * que solo servian para probar cada modulo por separado.</p>
 */
public final class Main {

    // Constructor privado: clase de arranque, no se instancia.
    private Main() {
    }

    // Arranca la aplicacion: verifica la conexion, activa la auditoria y muestra el menu.
    public static void main(String[] args) {
        ConsolaUtil.titulo("RapidExpress - Gestion de flotas, rutas y paqueteria");

        if (!ConexionBD.probarConexion()) {
            ConsolaUtil.error("No se pudo conectar con la base de datos. "
                    + "Revise src/main/resources/database.properties.");
            return;
        }
        ConsolaUtil.exito("Conexion con la base de datos establecida.");

        // Auditoria persistente (archivo + tabla) para todos los modulos.
        Auditoria.usar(new AuditoriaPersistente());
        AuditLogger.establecerUsuario(ConsolaUtil.leerTexto("Identifiquese (usuario del operador)"));

        boolean salir = false;
        while (!salir) {
            ConsolaUtil.titulo("Menu principal");
            System.out.println("  1. Flota de vehiculos");
            System.out.println("  2. Personal (conductores)");
            System.out.println("  3. Paquetes y envios");
            System.out.println("  4. Planificacion y seguimiento de rutas");
            System.out.println("  5. Reportes");
            System.out.println("  6. Auditoria");
            System.out.println("  0. Salir");
            ConsolaUtil.separador();

            switch (ConsolaUtil.leerEntero("Opcion")) {
                case 1 -> new FlotaMenuView().mostrar();
                case 2 -> new ConductorMenuView().mostrar();
                case 3 -> new PaqueteMenuView().mostrar();
                case 4 -> new RutaMenuView().mostrar();
                case 5 -> new ReporteView().mostrar();
                case 6 -> new AuditoriaView().mostrar();
                case 0 -> salir = true;
                default -> ConsolaUtil.error("Opcion no valida.");
            }
        }
        ConsolaUtil.info("Sesion finalizada. Hasta luego.");
    }
}
