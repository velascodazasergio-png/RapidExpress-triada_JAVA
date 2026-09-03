package com.rapidexpress.util;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Fabrica centralizada de conexiones JDBC hacia la base de datos MySQL
 * alojada en la nube. Lee las credenciales de
 * {@code src/main/resources/database.properties} (que esta en {@code .gitignore})
 * para no dejarlas escritas dentro del codigo fuente.
 *
 * <p>La configuracion se carga una sola vez, de forma perezosa y sincronizada,
 * la primera vez que alguien pide una conexion.</p>
 *
 * <p>NOTA DE INTEGRACION: esta clase es compartida por los tres modulos; existe
 * una unica copia y los demas modulos solo la consumen.</p>
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

    /**
     * Devuelve una conexion nueva. Quien la pide es responsable de cerrarla,
     * idealmente con try-with-resources.
     *
     * @return una conexion JDBC recien abierta
     * @throws SQLException           si no se puede establecer la conexion
     * @throws IllegalStateException  si falta o no se puede leer {@code database.properties}
     */
    public static Connection obtenerConexion() throws SQLException {
        cargarConfiguracion();
        return DriverManager.getConnection(url, usuario, password);
    }

    /**
     * Prueba rapida de conectividad, util al arrancar la aplicacion. No propaga
     * excepciones: imprime el error en {@code System.err} y devuelve {@code false}.
     *
     * @return {@code true} si se pudo abrir y la conexion quedo utilizable
     */
    public static boolean probarConexion() {
        try (Connection cn = obtenerConexion()) {
            return cn != null && !cn.isClosed();
        } catch (Exception e) {
            System.err.println("No fue posible conectar con la base de datos: " + e.getMessage());
            return false;
        }
    }
}
