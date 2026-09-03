package com.rapidexpress.util;

import com.rapidexpress.dao.AuditoriaDAO;
import com.rapidexpress.model.Auditoria;


import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.SQLException;

/**
 * Registro centralizado de auditoría.
 *
 * <p>Cada operación crítica se escribe en dos destinos: el archivo de texto
 * {@code logs/auditoria.log}, que el enunciado exige, y la tabla
 * {@code auditoria}, que es la que consultan los reportes. El archivo sobrevive
 * a una caída de la base de datos; la tabla permite filtrar y agregar. Ninguno
 * de los dos reemplaza al otro.</p>
 *
 * <p>El método de registro es {@code synchronized} porque el enunciado admite
 * varias terminales operando a la vez y dos hilos escribiendo en el mismo
 * archivo sin coordinación producen líneas entrelazadas.</p>
 *
 * <p>Un fallo al auditar nunca interrumpe la operación de negocio: se informa
 * por {@code System.err} y el flujo continúa. Perder una línea de bitácora es
 * malo; dejar una ruta a medio iniciar porque no se pudo escribir el log, peor.</p>
 */
public final class AuditLogger {

    private static final Path ARCHIVO = Paths.get("logs", "auditoria.log");
    private static final AuditoriaDAO DAO = new AuditoriaDAO();

    private static String usuarioActual = "SISTEMA";

    // Constructor privado: clase de utilidad, no se instancia.
    private AuditLogger() { }

    /** Identifica al operador de la sesión; aparece en cada evento registrado. */
    public static synchronized void establecerUsuario(String usuario) {
        usuarioActual = (usuario == null || usuario.isBlank()) ? "SISTEMA" : usuario.trim();
    }

    // Devuelve el usuario de la sesion actual.
    public static synchronized String usuarioActual() {
        return usuarioActual;
    }

    /**
     * Registra un evento con cambio de estado.
     *
     * @param operacion      constante de {@link Auditoria.Operacion}
     * @param entidad        constante de {@link Auditoria.Entidad}
     * @param idEntidad      identificador del registro afectado
     * @param estadoAnterior estado previo, o {@code null} si la entidad se acaba de crear
     * @param estadoNuevo    estado resultante
     * @param detalle        descripción legible de lo ocurrido
     */
    public static synchronized void registrar(String operacion, String entidad, Object idEntidad,
                                              String estadoAnterior, String estadoNuevo,
                                              String detalle) {
        Auditoria evento = new Auditoria(operacion, entidad, String.valueOf(idEntidad),
                usuarioActual, estadoAnterior, estadoNuevo, detalle);
        escribirArchivo(evento);
        escribirBaseDatos(evento);
    }

    /** Variante para operaciones que no representan un cambio de estado. */
    public static synchronized void registrar(String operacion, String entidad, Object idEntidad,
                                              String detalle) {
        registrar(operacion, entidad, idEntidad, null, null, detalle);
    }

    // Anade el evento al archivo de bitacora.
    private static void escribirArchivo(Auditoria evento) {
        try {
            Path carpeta = ARCHIVO.getParent();
            if (carpeta != null && !Files.exists(carpeta)) {
                Files.createDirectories(carpeta);
            }
            if (!Files.exists(ARCHIVO)) {
                Files.writeString(ARCHIVO,
                        "# RapidExpress · bitácora de auditoría" + System.lineSeparator()
                      + "# fecha_hora | operacion | entidad | id | usuario | estados | detalle"
                      + System.lineSeparator(),
                        StandardCharsets.UTF_8, StandardOpenOption.CREATE);
            }
            Files.writeString(ARCHIVO, evento.aLineaLog() + System.lineSeparator(),
                    StandardCharsets.UTF_8, StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println("[auditoría] no se pudo escribir el archivo: " + e.getMessage());
        }
    }

    // Inserta el evento en la tabla de auditoria.
    private static void escribirBaseDatos(Auditoria evento) {
        try {
            DAO.insertar(evento);
        } catch (SQLException e) {
            System.err.println("[auditoría] no se pudo registrar en la base de datos: "
                    + e.getMessage());
        }
    }

    /** Ruta del archivo centralizado, para mostrarla desde la vista. */
    public static Path archivo() {
        return ARCHIVO.toAbsolutePath();
    }
}
