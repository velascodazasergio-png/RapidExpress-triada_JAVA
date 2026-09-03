package com.rapidexpress.integracion;

import com.rapidexpress.model.Auditoria;
import com.rapidexpress.util.AuditLogger;

/**
 * Implementacion de {@link AuditoriaGateway} que SI persiste: delega en
 * {@link AuditLogger}, que escribe cada evento en el archivo de bitacora y en
 * la tabla {@code auditoria}. Es la que debe activar la Persona 3 en el Main:
 *
 * <pre>Auditoria.usar(new AuditoriaPersistente());</pre>
 *
 * <p>Normaliza el nombre de la entidad al vocabulario que consultan los
 * reportes y {@code AuditoriaController} ({@code "rutas"} -&gt; {@code "RUTA"},
 * etc.). Los servicios de la Persona 2 emiten el plural en minuscula; la traza
 * por entidad filtra por las constantes {@link Auditoria.Entidad}. Sin esta
 * conversion, {@code trazaDeRuta} / {@code trazaDePaquete} saldrian vacias.</p>
 */
public class AuditoriaPersistente implements AuditoriaGateway {

    // Registra el evento en archivo + base de datos a traves de AuditLogger.
    @Override
    public void registrar(String accion, String entidad, String idEntidad, String detalle) {
        AuditLogger.registrar(accion, normalizarEntidad(entidad), idEntidad, detalle);
    }

    // Lleva el nombre de la entidad a la forma canonica (singular, mayuscula).
    private static String normalizarEntidad(String entidad) {
        if (entidad == null) {
            return null;
        }
        return switch (entidad.trim().toUpperCase()) {
            case "RUTAS", "RUTA" -> Auditoria.Entidad.RUTA;
            case "PAQUETES", "PAQUETE" -> Auditoria.Entidad.PAQUETE;
            case "VEHICULOS", "VEHICULO" -> Auditoria.Entidad.VEHICULO;
            case "CONDUCTORES", "CONDUCTOR" -> Auditoria.Entidad.CONDUCTOR;
            default -> entidad.trim().toUpperCase();
        };
    }
}
