package com.rapidexpress.util;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Fabrica centralizada de conexiones JDBC hacia la base de datos MySQL
 * alojada en la nube. Lee las credenciales de src/main/resources/database.properties
 * para no dejarlas escritas dentro del codigo fuente.
 *
 * NOTA DE INTEGRACION: esta clase es compartida por los tres modulos.
 * Se debe fusionar una sola vez en develop; los demas modulos solo la usan.
 */
public final class ConexionBD {

    private static final String ARCHIVO_CONFIG = "database.properties";

    private static String url;
    private static String usuario;
    private static String password;
    private static boolean configuracionCargada = false;

    private ConexionBD() {
    }

    private static synchronized void cargarConfiguracion() {
        if (configuracionCargada) {
            return;
        }
        Properties props = new Properties();
        try (InputStream entrada = ConexionBD.class.getClassLoader()
                .getResourceAsStream(ARCHIVO_CONFIG)) {
            if (entrada == null) {
                throw new IllegalStateException(
                        "No se encontro " + ARCHIVO_CONFIG + " en src/main/resources. "
                        + "Copie database.properties.example y complete sus credenciales.");
            }
            props.load(entrada);
        } catch (Exception e) {
            throw new IllegalStateException("Error leyendo la configuracion de BD: " + e.getMessage(), e);
        }
        url = props.getProperty("db.url");
        usuario = props.getProperty("db.user");
        password = props.getProperty("db.password");
        configuracionCargada = true;
    }

    /** Devuelve una conexion nueva. Quien la pide es responsable de cerrarla (try-with-resources). */
    public static Connection obtenerConexion() throws SQLException {
        cargarConfiguracion();
        return DriverManager.getConnection(url, usuario, password);
    }

    /** Prueba rapida de conectividad, util al arrancar la aplicacion. */
    public static boolean probarConexion() {
        try (Connection cn = obtenerConexion()) {
            return cn != null && !cn.isClosed();
        } catch (Exception e) {
            System.err.println("No fue posible conectar con la base de datos: " + e.getMessage());
            return false;
        }
    }
}
