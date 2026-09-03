package com.rapidexpress.util;

import com.rapidexpress.excepcion.NegocioException;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Ejecuta un bloque de codigo dentro de una transaccion, con commit/rollback
 * automatico y reintento ante deadlock. (Persona 2)
 *
 * Es la pieza que sostiene el requisito de "consistencia ante consultas
 * concurrentes" del modulo de rutas: varias terminales pueden estar moviendo
 * los mismos paquetes al mismo tiempo.
 *
 * Usa la ConexionBD compartida del equipo; no abre su propia configuracion.
 */
public final class TransaccionUtil {

    private static final int MAX_INTENTOS = 3;

    private TransaccionUtil() {
    }

    @FunctionalInterface
    public interface Bloque<T> {
        T ejecutar(Connection cn) throws SQLException, NegocioException;
    }

    /**
     * @param descripcion texto corto para el mensaje de error ("crear la ruta")
     */
    public static <T> T ejecutar(String descripcion, Bloque<T> bloque) throws NegocioException {
        SQLException ultimoError = null;

        for (int intento = 1; intento <= MAX_INTENTOS; intento++) {
            Connection cn = null;
            try {
                cn = ConexionBD.obtenerConexion();
                cn.setAutoCommit(false);
                cn.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);

                T resultado = bloque.ejecutar(cn);

                cn.commit();
                return resultado;
            } catch (SQLException e) {
                revertir(cn);
                ultimoError = e;
                if (!esConflictoDeBloqueo(e) || intento == MAX_INTENTOS) {
                    throw new NegocioException("Error de base de datos al " + descripcion + ": " + e.getMessage(), e);
                }
                dormir(60L * intento);
            } catch (NegocioException | RuntimeException e) {
                revertir(cn);
                throw e;
            } finally {
                cerrar(cn);
            }
        }
        throw new NegocioException("Error de base de datos al " + descripcion + ": " + ultimoError.getMessage(),
                ultimoError);
    }

    /** 1213 = deadlock, 1205 = lock wait timeout. Son reintentables. */
    private static boolean esConflictoDeBloqueo(SQLException e) {
        int codigo = e.getErrorCode();
        return codigo == 1213 || codigo == 1205 || "40001".equals(e.getSQLState());
    }

    private static void revertir(Connection cn) {
        if (cn != null) {
            try {
                cn.rollback();
            } catch (SQLException ignorada) {
                System.err.println("Fallo el rollback: " + ignorada.getMessage());
            }
        }
    }

    private static void cerrar(Connection cn) {
        if (cn != null) {
            try {
                cn.close();
            } catch (SQLException ignorada) {
                // sin accion
            }
        }
    }

    private static void dormir(long milis) {
        try {
            Thread.sleep(milis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
