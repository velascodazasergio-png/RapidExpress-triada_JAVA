package com.rapidexpress.controller;

import com.rapidexpress.dao.AuditoriaDAO;
import com.rapidexpress.dao.ReporteDAO;
import com.rapidexpress.model.Auditoria;
import com.rapidexpress.model.dto.TablaReporte;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/** Consulta de la bitácora de auditoría. */
public class AuditoriaController {

    private final AuditoriaDAO dao;
    private final ReporteDAO reporteDAO;

    // Constructor por defecto: crea sus propios DAO.
    public AuditoriaController() {
        this(new AuditoriaDAO(), new ReporteDAO());
    }

    // Constructor con inyeccion de dependencias (util para pruebas).
    public AuditoriaController(AuditoriaDAO dao, ReporteDAO reporteDAO) {
        this.dao = dao;
        this.reporteDAO = reporteDAO;
    }

    // Devuelve los ultimos N eventos de auditoria.
    public List<Auditoria> ultimosEventos(int limite) throws SQLException {
        if (limite <= 0 || limite > 500) {
            throw new IllegalArgumentException("El límite debe estar entre 1 y 500.");
        }
        return dao.ultimos(limite);
    }

    // Devuelve los eventos de auditoria en un rango de fechas.
    public List<Auditoria> porPeriodo(LocalDate desde, LocalDate hasta, String operacion)
            throws SQLException {
        if (desde == null || hasta == null) {
            throw new IllegalArgumentException("Debe indicar las dos fechas del rango.");
        }
        if (hasta.isBefore(desde)) {
            throw new IllegalArgumentException(
                    "La fecha final no puede ser anterior a la inicial.");
        }
        return dao.porPeriodo(
                LocalDateTime.of(desde, LocalTime.MIN),
                LocalDateTime.of(hasta, LocalTime.MAX),
                operacion);
    }

    // Devuelve la traza de auditoria de una ruta.
    public List<Auditoria> trazaDeRuta(int idRuta) throws SQLException {
        return dao.porEntidad(Auditoria.Entidad.RUTA, String.valueOf(idRuta));
    }

    // Devuelve la traza de auditoria de un paquete.
    public List<Auditoria> trazaDePaquete(int idPaquete) throws SQLException {
        return dao.porEntidad(Auditoria.Entidad.PAQUETE, String.valueOf(idPaquete));
    }

    // Devuelve el resumen de actividad registrada.
    public TablaReporte actividad() throws SQLException {
        return reporteDAO.actividadAuditoria();
    }
}
