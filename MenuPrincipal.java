package com.rapidexpress.view;

import com.rapidexpress.util.AuditLogger;

/**
 * Menú raíz de la aplicación. Es el punto de integración de los tres módulos:
 * cada opción delega en la vista del módulo correspondiente y ninguna contiene
 * lógica de negocio propia.
 *
 * <p><b>Integración pendiente.</b> Las opciones 1 a 4 quedan conectadas en
 * cuanto las vistas de los otros dos módulos existan en el paquete
 * {@code com.rapidexpress.view}. Basta con descomentar la instancia y la
 * llamada correspondiente; la firma esperada es un método público
 * {@code mostrar()} sin parámetros, igual que en {@link ReporteView} y
 * {@link AuditoriaView}.</p>
 */
public class MenuPrincipal {

    // --- Vistas del módulo de flota y personal (Persona 1) -------------
    // private final VehiculoView vehiculoView   = new VehiculoView();
    // private final ConductorView conductorView = new ConductorView();

    // --- Vistas del módulo de paquetería y rutas (Persona 2) -----------
    // private final PaqueteView paqueteView = new PaqueteView();
    // private final RutaView rutaView       = new RutaView();

    // --- Vistas del módulo de reportes y auditoría (Persona 3) ---------
    private final ReporteView reporteView     = new ReporteView();
    private final AuditoriaView auditoriaView = new AuditoriaView();

    public void iniciar() {
        identificarOperador();

        boolean salir = false;
        while (!salir) {
            ConsolaUtil.titulo("RapidExpress · sistema de gestión de flotas y rutas");
            System.out.println("""
                      1. Gestión de la flota de vehículos
                      2. Gestión de personal (conductores)
                      3. Gestión de paquetes y envíos
                      4. Planificación y seguimiento de rutas
                      5. Reportes operativos
                      6. Auditoría del sistema
                      0. Salir
                    """);
            System.out.println("  Operador en sesión: " + AuditLogger.usuarioActual());

            int opcion = ConsolaUtil.leerEntero("Opción");
            switch (opcion) {
                case 1 -> pendiente("Gestión de la flota");        // vehiculoView.mostrar();
                case 2 -> pendiente("Gestión de personal");        // conductorView.mostrar();
                case 3 -> pendiente("Gestión de paquetes");        // paqueteView.mostrar();
                case 4 -> pendiente("Planificación de rutas");     // rutaView.mostrar();
                case 5 -> reporteView.mostrar();
                case 6 -> auditoriaView.mostrar();
                case 0 -> salir = confirmarSalida();
                default -> {
                    ConsolaUtil.error("Opción no reconocida.");
                    ConsolaUtil.pausar();
                }
            }
        }
    }

    /**
     * Pide el nombre del operador. La bitácora exige saber quién ejecutó cada
     * operación crítica; sin esto todos los eventos quedarían como SISTEMA.
     */
    private void identificarOperador() {
        ConsolaUtil.titulo("Inicio de sesión");
        String operador = ConsolaUtil.leerTexto("Nombre del operador");
        AuditLogger.establecerUsuario(operador);
        ConsolaUtil.aviso("Bienvenido, " + AuditLogger.usuarioActual() + ".");
    }

    private boolean confirmarSalida() {
        String respuesta = ConsolaUtil.leerTexto("¿Confirma que desea salir? (s/n)");
        if (respuesta.equalsIgnoreCase("s")) {
            ConsolaUtil.aviso("Sesión finalizada. Bitácora en " + AuditLogger.archivo());
            return true;
        }
        return false;
    }

    private void pendiente(String modulo) {
        ConsolaUtil.error(modulo + ": módulo aún no integrado en esta rama.");
        ConsolaUtil.pausar();
    }
}
