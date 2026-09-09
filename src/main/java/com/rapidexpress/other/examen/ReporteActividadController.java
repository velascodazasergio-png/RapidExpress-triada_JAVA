package com.rapidexpress;

import com.rapidexpress.model.dto.ActividadVehiculoDTO;
import java.sql.SQLException;
import java.util.List;

/**
 * Controlador del reporte "actividad por vehiculo".
 *
 * <p>Mismo reparto de responsabilidades que {@code ReporteController}: valida
 * los parametros de entrada (aqui no hay), baja al DAO y calcula los totales
 * que la vista muestra al pie. La vista no hace cuentas; el DAO no valida.</p>
 */
public class ReporteActividadController {

    private final ReporteActividadDAO dao;

    // Constructor por defecto: crea su propio DAO.
    public ReporteActividadController() {
        this(new ReporteActividadDAO());
    }

    /** Constructor con inyeccion, util para pruebas con un DAO simulado. */
    public ReporteActividadController(ReporteActividadDAO dao) {
        this.dao = dao;
    }

    /** Filas del reporte, ya ordenadas por kg entregados. */
    public List<ActividadVehiculoDTO> generar() throws SQLExceptionn {
        return dao.actividadPorVehiculo();
    }

    /** Total de kilos entregados por toda la flota. */
    public double totalKg(List<ActividadVehiculoDTO> filas) {
        return filas.stream().mapToDouble(ActividadVehiculoDTO::kgEntregados).sum();
    }

    /** Total de paquetes entregados por toda la flota. */
    public int totalPaquetes(List<ActividadVehiculoDTO> filas) {
        return filas.stream().mapToInt(ActividadVehiculoDTO::paquetesEntregados).sum();
    }

    /** Costo total de mantenimiento de la flota. */
    public double totalCostoMantenimiento(List<ActividadVehiculoDTO> filas) {
        return filas.stream().mapToDouble(ActividadVehiculoDTO::costoMantenimiento).sum();
    }

    /** Placa del vehiculo mas productivo (mas kg entregados), o "-" si no hay datos. */
    public String vehiculoMasProductivo(List<ActividadVehiculoDTO> filas) {
        return filas.stream()
                .max((a, b) -> Double.compare(a.kgEntregados(), b.kgEntregados()))
                .map(ActividadVehiculoDTO::placa)
                .orElse("-");
    }
}
