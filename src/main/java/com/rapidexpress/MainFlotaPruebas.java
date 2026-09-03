package com.rapidexpress;

import com.rapidexpress.util.ConexionBD;
import com.rapidexpress.view.ConductorMenuView;
import com.rapidexpress.view.ConsolaUtil;
import com.rapidexpress.view.FlotaMenuView;

/**
 * Main TEMPORAL para probar el modulo de la Persona 1 de forma aislada,
 * antes de integrarlo con los otros dos modulos.
 *
 * >>> ESTA CLASE SE ELIMINA EN EL MERGE FINAL <<<
 * El Main definitivo lo aporta la Persona 3 (integracion).
 */
public class MainFlotaPruebas {

    public static void main(String[] args) {
        ConsolaUtil.titulo("RapidExpress - Modulo Flota y Personal (pruebas)");

        if (!ConexionBD.probarConexion()) {
            ConsolaUtil.error("Revise src/main/resources/database.properties antes de continuar.");
            return;
        }
        ConsolaUtil.exito("Conexion con la base de datos establecida.");

        boolean salir = false;
        while (!salir) {
            ConsolaUtil.titulo("Menu de pruebas");
            System.out.println("  1. Gestion de flota de vehiculos");
            System.out.println("  2. Gestion de personal (conductores)");
            System.out.println("  0. Salir");
            ConsolaUtil.separador();

            switch (ConsolaUtil.leerEntero("Opcion")) {
                case 1 -> new FlotaMenuView().mostrar();
                case 2 -> new ConductorMenuView().mostrar();
                case 0 -> salir = true;
                default -> ConsolaUtil.error("Opcion no valida.");
            }
        }
        ConsolaUtil.info("Hasta luego.");
    }
}
