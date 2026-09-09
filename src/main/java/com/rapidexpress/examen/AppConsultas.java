package com.rapidexpress.examen;

import com.rapidexpress.util.ConexionBD;
import com.rapidexpress.view.ConsolaUtil;

/**
 * Arranque INDEPENDIENTE del modulo de examen: permite probar las consultas,
 * las validaciones y el reporte nuevo sin tocar {@code Main}.
 *
 * <p>Ejecutar con:</p>
 * <pre>
 *   .\run.ps1 -SkipBuild            (si ya compilaste)  -> ojo: run.ps1 lanza Main
 *   java -cp target\classes;lib\mysql-connector-j-8.4.0.jar com.rapidexpress.examen.AppConsultas
 * </pre>
 *
 * <p>O bien enganchar {@link MenuConsultasView} en el menu principal: ver
 * {@code docs/EXAMEN_GUIA.md}, seccion "Integrar en Main".</p>
 */
public final class AppConsultas {

    // Constructor privado: clase de arranque, no se instancia.
    private AppConsultas() {
    }

    /** Verifica la conexion a la base de datos y, si hay, abre el menu de consultas. */
    public static void main(String[] args) {
        ConsolaUtil.titulo("RapidExpress - Modulo de consultas (modo examen)");

        if (!ConexionBD.probarConexion()) {
            ConsolaUtil.error("No se pudo conectar con la base de datos. "
                    + "Revise src/main/resources/database.properties.");
            return;
        }
        ConsolaUtil.exito("Conexion establecida.");

        new MenuConsultasView().mostrar();
        ConsolaUtil.info("Fin del modulo de consultas.");
    }
}
