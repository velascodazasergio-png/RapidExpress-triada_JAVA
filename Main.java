package com.rapidexpress;

import com.rapidexpress.util.ConexionBD;
import com.rapidexpress.view.MenuPrincipal;

/**
 * Punto de entrada del sistema.
 *
 * <p>Verifica la conectividad antes de mostrar el menú: es preferible fallar
 * aquí con un mensaje claro que dejar al operador descubrirlo a mitad de una
 * operación.</p>
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("""
                ======================================================================
                                    R A P I D   E X P R E S S
                          Sistema de Gestión de Flotas y Rutas · v1.0
                ======================================================================
                """);

        System.out.print("Verificando conexión con la base de datos... ");
        if (!ConexionBD.probar()) {
            System.out.println("FALLÓ");
            System.out.println("""
                    
                    Revise que:
                      · src/main/resources/db.properties exista y tenga las credenciales
                      · la instancia en la nube esté encendida
                      · su IP esté autorizada en el grupo de seguridad del proveedor
                    """);
            System.exit(1);
        }
        System.out.println("OK");

        new MenuPrincipal().iniciar();
    }
}
