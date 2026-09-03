package com.rapidexpress.model;

import java.time.LocalDateTime;

/**
 * Evento registrado en la bitácora de auditoría.
 *
 * <p>Es inmutable a propósito: un registro de auditoría que puede modificarse
 * después de creado no sirve como evidencia de nada.</p>
 */
public class Auditoria {

    /** Operaciones críticas que el sistema registra. */
    public static final class Operacion {
        public static final String CREAR_PAQUETE      = "CREAR_PAQUETE";
        public static final String CAMBIAR_ESTADO_PAQUETE = "CAMBIAR_ESTADO_PAQUETE";
        public static final String CREAR_RUTA         = "CREAR_RUTA";
        public static final String INICIAR_RUTA       = "INICIAR_RUTA";
        public static final String FINALIZAR_RUTA     = "FINALIZAR_RUTA";
        public static final String CANCELAR_RUTA      = "CANCELAR_RUTA";
        public static final String ASIGNAR_CONDUCTOR  = "ASIGNAR_CONDUCTOR";
        public static final String CAMBIAR_ESTADO_VEHICULO = "CAMBIAR_ESTADO_VEHICULO";
        public static final String REGISTRAR_MANTENIMIENTO = "REGISTRAR_MANTENIMIENTO";

        private Operacion() { }
    }

    /** Entidades sobre las que se registra actividad. */
    public static final class Entidad {
        public static final String PAQUETE    = "PAQUETE";
        public static final String RUTA       = "RUTA";
        public static final String VEHICULO   = "VEHICULO";
        public static final String CONDUCTOR  = "CONDUCTOR";

        private Entidad() { }
    }

    private final long id;
    private final LocalDateTime fechaHora;
    private final String operacion;
    private final String entidad;
    private final String idEntidad;
    private final String usuario;
    private final String estadoAnterior;
    private final String estadoNuevo;
    private final String detalle;

    public Auditoria(long id, LocalDateTime fechaHora, String operacion, String entidad,
                     String idEntidad, String usuario, String estadoAnterior,
                     String estadoNuevo, String detalle) {
        this.id = id;
        this.fechaHora = fechaHora;
        this.operacion = operacion;
        this.entidad = entidad;
        this.idEntidad = idEntidad;
        this.usuario = usuario;
        this.estadoAnterior = estadoAnterior;
        this.estadoNuevo = estadoNuevo;
        this.detalle = detalle;
    }

    /** Constructor para eventos nuevos: el id y la fecha los pone el sistema. */
    public Auditoria(String operacion, String entidad, String idEntidad, String usuario,
                     String estadoAnterior, String estadoNuevo, String detalle) {
        this(0L, LocalDateTime.now(), operacion, entidad, idEntidad, usuario,
             estadoAnterior, estadoNuevo, detalle);
    }

    public long getId()                  { return id; }
    public LocalDateTime getFechaHora()  { return fechaHora; }
    public String getOperacion()         { return operacion; }
    public String getEntidad()           { return entidad; }
    public String getIdEntidad()         { return idEntidad; }
    public String getUsuario()           { return usuario; }
    public String getEstadoAnterior()    { return estadoAnterior; }
    public String getEstadoNuevo()       { return estadoNuevo; }
    public String getDetalle()           { return detalle; }

    /** Representación de una línea para el archivo de texto centralizado. */
    public String aLineaLog() {
        return String.format("%-20s | %-24s | %-10s | %-8s | %-20s | %-16s -> %-16s | %s",
                fechaHora.withNano(0),
                operacion,
                entidad,
                idEntidad,
                usuario,
                estadoAnterior == null ? "-" : estadoAnterior,
                estadoNuevo == null ? "-" : estadoNuevo,
                detalle == null ? "" : detalle);
    }

    @Override
    public String toString() {
        return aLineaLog();
    }
}
