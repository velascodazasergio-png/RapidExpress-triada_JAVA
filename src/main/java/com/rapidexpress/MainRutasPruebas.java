package com.rapidexpress;

import com.rapidexpress.util.ConexionBD;
import com.rapidexpress.view.ConsolaUtil;
import com.rapidexpress.view.PaqueteMenuView;
import com.rapidexpress.view.RutaMenuView;

/**
 * Main TEMPORAL para probar el modulo de la Persona 2 (paquetes y rutas)
 * junto con el de la Persona 1.
 *
 * >>> ESTA CLASE SE ELIMINA EN EL MERGE FINAL <<<
 * El Main definitivo lo aporta la Persona 3 (integracion).
 */
public class MainRutasPruebas {

    public static void main(String[] args) {
        ConsolaUtil.titulo("RapidExpress - Modulo Paquetes y Rutas (pruebas)");

        if (!ConexionBD.probarConexion()) {
            ConsolaUtil.error("Revise src/main/resources/database.properties antes de continuar.");
            return;
        }
        ConsolaUtil.exito("Conexion con la base de datos establecida.");

        boolean salir = false;
        while (!salir) {
            ConsolaUtil.titulo("Menu de pruebas");
            System.out.println("  1. Gestion de paquetes y envios");
            System.out.println("  2. Planificacion y seguimiento de rutas");
            System.out.println("  0. Salir");
            ConsolaUtil.separador();

            switch (ConsolaUtil.leerEntero("Opcion")) {
                case 1 -> new PaqueteMenuView().mostrar();
                case 2 -> new RutaMenuView().mostrar();
                case 0 -> salir = true;
                default -> ConsolaUtil.error("Opcion no valida.");
            }
        }
        ConsolaUtil.info("Hasta luego.");
    }
}
