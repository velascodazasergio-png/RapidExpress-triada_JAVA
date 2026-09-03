package com.rapidexpress.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Punto único de acceso a la base de datos MySQL alojada en la nube.
 *
 * <p>Las credenciales se leen de {@code src/main/resources/db.properties}, que
 * está excluido del control de versiones. Nunca se escriben en el código.</p>
 *
 * <p>Se entrega una conexión nueva por operación en lugar de mantener una
 * abierta y compartida: el enunciado exige que las consultas concurrentes desde
 * la terminal vean un estado consistente, y una conexión compartida entre hilos
 * arrastraría el estado transaccional de uno al otro.</p>
 */
public final class ConexionBD {

    private static final String ARCHIVO_CONFIG = "db.properties";
    private static Properties config;

    private ConexionBD() {
        // Clase de utilidad: no se instancia.
    }

    private static synchronized Properties config() {
        if (config == null) {
            config = new Properties();
            try (InputStream in = ConexionBD.class.getClassLoader()
                    .getResourceAsStream(ARCHIVO_CONFIG)) {
                if (in == null) {
                    throw new IllegalStateException(
                            "No se encontró " + ARCHIVO_CONFIG + " en el classpath. "
                          + "Copia db.properties.example y completa las credenciales.");
                }
                config.load(in);
            } catch (IOException e) {
                throw new IllegalStateException("No fue posible leer " + ARCHIVO_CONFIG, e);
            }
        }
        return config;
    }

    /**
     * Abre una conexión nueva. Quien la solicita es responsable de cerrarla,
     * preferiblemente con try-with-resources.
     */
    public static Connection obtener() throws SQLException {
        Properties p = config();
        return DriverManager.getConnection(
                p.getProperty("db.url"),
                p.getProperty("db.user"),
                p.getProperty("db.password"));
    }

    /** Comprueba la conectividad al arrancar la aplicación. */
    public static boolean probar() {
        try (Connection c = obtener()) {
            return c.isValid(5);
        } catch (SQLException e) {
            System.err.println("Error de conexión: " + e.getMessage());
            return false;
        }
    }
}
