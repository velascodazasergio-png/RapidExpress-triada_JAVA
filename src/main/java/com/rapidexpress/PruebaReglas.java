package com.rapidexpress;

import com.rapidexpress.controller.ConductorController;
import com.rapidexpress.controller.FlotaController;
import com.rapidexpress.excepcion.NegocioException;
import com.rapidexpress.model.EstadoVehiculo;
import com.rapidexpress.model.TipoLicencia;
import com.rapidexpress.util.ConexionBD;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Prueba NO interactiva de las reglas de negocio de la Persona 1.
 * Comprueba que lo que no debe permitirse, se niega:
 *   1. Dos vehiculos con la misma placa.
 *   2. El mismo conductor en dos vehiculos.
 *   3. Iniciar una ruta con un vehiculo sin conductor (y que el vehiculo
 *      siga Disponible: si quedara "a medias" habria un problema de
 *      transacciones).
 *
 * Crea datos con un sufijo aleatorio y los borra al terminar.
 *
 * >>> ESTA CLASE SE ELIMINA EN EL MERGE FINAL <<<
 */
public class PruebaReglas {

    private static final FlotaController flota = new FlotaController();
    private static final ConductorController cond = new ConductorController();
    private static int ok = 0;
    private static int ko = 0;

    public static void main(String[] args) throws Exception {
        String s = String.format("%03d", (int) (System.nanoTime() % 1000));
        String ident = "77" + String.format("%06d", (int) (System.nanoTime() % 1_000_000));

        System.out.println("=== Reglas de negocio (sufijo " + s + ") ===\n");

        // Regla 1: placa duplicada -> el segundo registro se niega
        String placa = "PRA" + s;
        int vA = flota.registrarVehiculo(placa, "Volvo", "FH", 2020, new BigDecimal("10000"));
        System.out.println("Vehiculo A registrado id=" + vA + " placa=" + placa);
        esperarError("Regla 1 - placa duplicada",
                () -> flota.registrarVehiculo(placa, "Scania", "R500", 2021, new BigDecimal("9000")));

        // Regla 2: el mismo conductor en dos vehiculos -> el segundo se niega
        int vB = flota.registrarVehiculo("PRB" + s, "Hino", "500", 2019, new BigDecimal("8000"));
        int c1 = cond.registrarConductor(ident, "Conductor Prueba", TipoLicencia.C2, "3001234567");
        System.out.println("\nVehiculo B id=" + vB + ", Conductor id=" + c1);
        cond.asignarConductorAVehiculo(c1, vA);
        System.out.println("Asignado conductor " + c1 + " -> vehiculo A (" + vA + ")");
        esperarError("Regla 2 - conductor ya asignado",
                () -> cond.asignarConductorAVehiculo(c1, vB));

        // Regla 3: iniciar ruta sin conductor -> se niega y el vehiculo sigue Disponible
        int vC = flota.registrarVehiculo("PRC" + s, "Isuzu", "NPR", 2022, new BigDecimal("5000"));
        System.out.println("\nVehiculo C id=" + vC + " (sin conductor), estado antes = "
                + flota.buscarVehiculo(vC).getEstado());
        esperarError("Regla 3 - iniciar ruta sin conductor",
                () -> flota.iniciarOperacion(vC));
        EstadoVehiculo despues = flota.buscarVehiculo(vC).getEstado();
        System.out.println("Estado despues del fallo = " + despues);
        comprobar("Regla 3 - vehiculo sigue Disponible tras el rollback",
                despues == EstadoVehiculo.DISPONIBLE);
        comprobar("Regla 3 - la BD confirma 'Disponible'",
                "Disponible".equals(estadoEnBD(vC)));

        System.out.println("\n=== RESULTADO: " + ok + " OK / " + ko + " FALLIDAS ===");

        limpiar(s, ident);
        System.exit(ko == 0 ? 0 : 1);
    }

    private interface Accion {
        void run() throws NegocioException;
    }

    private static void esperarError(String nombre, Accion a) {
        try {
            a.run();
            registrar(nombre, false, "no lanzo excepcion (se permitio algo que no debe)");
        } catch (NegocioException e) {
            registrar(nombre, true, "negado: \"" + e.getMessage() + "\"");
        }
    }

    private static void comprobar(String nombre, boolean condicion) {
        registrar(nombre, condicion, condicion ? "" : "condicion falsa");
    }

    private static void registrar(String nombre, boolean paso, String detalle) {
        if (paso) {
            ok++;
        } else {
            ko++;
        }
        System.out.println((paso ? "[OK]   " : "[FALLA]") + " " + nombre
                + (detalle.isEmpty() ? "" : "  -> " + detalle));
    }

    private static String estadoEnBD(int idVehiculo) throws Exception {
        try (Connection cn = ConexionBD.obtenerConexion();
             Statement st = cn.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT estado FROM vehiculos WHERE id_vehiculo = " + idVehiculo)) {
            return rs.next() ? rs.getString(1) : null;
        }
    }

    private static void limpiar(String s, String ident) {
        String[] sentencias = {
            "DELETE a FROM asignaciones_conductor_vehiculo a "
                    + "JOIN vehiculos v ON v.id_vehiculo = a.id_vehiculo WHERE v.placa LIKE 'PR_" + s + "'",
            "DELETE FROM mantenimientos WHERE id_vehiculo IN "
                    + "(SELECT id_vehiculo FROM vehiculos WHERE placa LIKE 'PR_" + s + "')",
            "DELETE FROM vehiculos WHERE placa LIKE 'PR_" + s + "'",
            "DELETE FROM conductores WHERE identificacion = '" + ident + "'"
        };
        try (Connection cn = ConexionBD.obtenerConexion(); Statement st = cn.createStatement()) {
            for (String q : sentencias) {
                st.executeUpdate(q);
            }
            System.out.println("(datos de prueba eliminados)");
        } catch (Exception e) {
            System.out.println("(no se pudo limpiar automaticamente: " + e.getMessage() + ")");
        }
    }
}
