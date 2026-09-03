package com.rapidexpress.integracion;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Implementacion provisional de AuditoriaGateway: imprime en consola. */
public class AuditoriaConsola implements AuditoriaGateway {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void registrar(String accion, String entidad, String idEntidad, String detalle) {
        System.out.printf("[AUDIT %s] %s %s#%s :: %s%n",
                LocalDateTime.now().format(FORMATO), accion, entidad, idEntidad, detalle);
    }
}
