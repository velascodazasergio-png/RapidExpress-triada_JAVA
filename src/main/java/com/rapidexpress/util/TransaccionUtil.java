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

    /**
     * Trabajo a ejecutar dentro de la transaccion. Recibe la conexion ya
     * configurada (autocommit desactivado, aislamiento READ COMMITTED) y NO debe
     * hacer commit, rollback ni cerrarla: de eso se encarga {@link #ejecutar}.
     *
     * @param <T> tipo del resultado que devuelve la operacion
     */
    @FunctionalInterface
    public interface Bloque<T> {
        /**
         * @param cn conexion en curso, dentro de la transaccion
         * @return el resultado de la operacion (puede ser {@code null})
         * @throws SQLException     si falla una sentencia; provoca rollback
         * @throws NegocioException si una regla de negocio se incumple; provoca rollback
         */
        T ejecutar(Connection cn) throws SQLException, NegocioException;
    }

    /**
     * Ejecuta {@code bloque} dentro de una transaccion: si termina sin excepcion
     * hace commit y devuelve su resultado; si lanza cualquier excepcion hace
     * rollback. Ante un conflicto de bloqueo de MySQL (deadlock 1213 o lock wait
     * timeout 1205) reintenta hasta {@value #MAX_INTENTOS} veces con una espera
     * creciente antes de rendirse.
     *
     * @param descripcion texto corto que se inserta en el mensaje de error
     *                    ("crear la ruta", "iniciar la ruta"...)
     * @param bloque      operacion a ejecutar; ver {@link Bloque}
     * @param <T>         tipo del resultado
     * @return el valor devuelto por {@code bloque} tras el commit
     * @throws NegocioException si el bloque incumple una regla de negocio o si
     *                          el error de base de datos persiste tras los reintentos
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
