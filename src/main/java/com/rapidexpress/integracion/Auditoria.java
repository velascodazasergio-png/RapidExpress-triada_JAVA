package com.rapidexpress.integracion;

/**
 * Punto unico donde el sistema decide QUIEN registra la auditoria.
 *
 * Por defecto imprime en consola. La Persona 3, en su Main definitivo, solo
 * tiene que ejecutar UNA linea antes de mostrar los menus:
 *
 *     Auditoria.usar(new AuditoriaService());   // su implementacion
 *
 * De esa forma las vistas siguen construyendose sin parametros
 * (new RutaMenuView().mostrar()) y nadie mas tiene que cambiar.
 */
public final class Auditoria {

    private static AuditoriaGateway actual = new AuditoriaConsola();

    // Constructor privado: clase de utilidad, no se instancia.
    private Auditoria() {
    }

    // Fija el gateway de auditoria que usara el sistema.
    public static void usar(AuditoriaGateway gateway) {
        if (gateway != null) {
            actual = gateway;
        }
    }

    // Devuelve el gateway de auditoria vigente.
    public static AuditoriaGateway actual() {
        return actual;
    }
}
