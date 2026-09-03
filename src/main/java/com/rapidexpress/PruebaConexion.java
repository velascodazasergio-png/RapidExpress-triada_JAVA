package com.rapidexpress;

import com.rapidexpress.util.ConexionBD;

/**
 * Punto de control minimo: verifica que Java puede conectarse a la base de datos.
 * Debe imprimir "true". Si imprime "false", nada del resto del modulo funcionara.
 *
 * IMPORTANTE: esta clase se elimina en el merge final.
 */
public class PruebaConexion {

    public static void main(String[] args) {
        System.out.println(ConexionBD.probarConexion());
    }
}
