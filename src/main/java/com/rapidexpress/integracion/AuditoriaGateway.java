package com.rapidexpress.integracion;

/**
 * CONTRATO con el modulo de la Persona 3 (auditoria).
 *
 * El modulo de rutas solo avisa "paso esto"; donde se escribe (archivo .txt,
 * tabla auditoria, o ambos) es decision de la Persona 3.
 *
 * Mientras su clase no exista se usa AuditoriaConsola y el proyecto compila.
 * Al integrar: su AuditoriaService implementa esta interfaz y se pasa al
 * constructor de PaqueteService y RutaService. Una sola linea de cambio.
 */
public interface AuditoriaGateway {

    /**
     * @param accion     verbo corto: CREAR_PAQUETE, INICIAR_RUTA, ENTREGAR_PAQUETE...
     * @param entidad    tabla afectada: "paquetes", "rutas"
     * @param idEntidad  identificador del registro afectado
     * @param detalle    texto libre con el antes/despues
     */
    void registrar(String accion, String entidad, String idEntidad, String detalle);
}
