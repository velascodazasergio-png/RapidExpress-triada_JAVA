package com.rapidexpress;

import com.rapidexpress.util.ConexionBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO del reporte NUEVO "actividad por vehiculo".
 *
 * <p>Toda la agregacion la hace el motor en una sola consulta (COUNT, SUM,
 * CASE, COALESCE). Traer las filas crudas a Java para sumarlas alli
 * multiplicaria el trafico contra la base de datos: mismo criterio que usa
 * {@code ReporteDAO} en el modulo original.</p>
 *
 * <p>No necesita crear ninguna vista ni procedimiento: la consulta va
 * embebida. Si en el examen piden usar una VISTA, basta con mover este SQL a
 * un {@code CREATE OR REPLACE VIEW v_actividad_vehiculo AS ...} y cambiar el
 * FROM por {@code SELECT * FROM v_actividad_vehiculo}.</p>
 */
public class ReporteActividadDAO {

    private static final String SQL = """
            SELECT v.placa                                                        AS placa,
                   v.marca                                                        AS marca,
                   COUNT(DISTINCT r.id_ruta)                                       AS rutas_totales,
                   COUNT(DISTINCT CASE WHEN r.estado = 'Finalizada'
                                       THEN r.id_ruta END)                         AS rutas_finalizadas,
                   COUNT(CASE WHEN p.estado = 'Entregado' THEN 1 END)              AS paquetes_entregados,
                   ROUND(COALESCE(SUM(CASE WHEN p.estado = 'Entregado'
                                           THEN p.peso_kg END), 0), 2)             AS kg_entregados,
                   ROUND((SELECT COALESCE(SUM(m.costo), 0)
                          FROM mantenimientos m
                          WHERE m.id_vehiculo = v.id_vehiculo), 2)                 AS costo_mantenimiento
            FROM vehiculos v
            LEFT JOIN rutas r          ON r.id_vehiculo = v.id_vehiculo
            LEFT JOIN ruta_paquetes rp ON rp.id_ruta    = r.id_ruta
            LEFT JOIN paquetes p       ON p.id_paquete  = rp.id_paquete
            GROUP BY v.id_vehiculo, v.placa, v.marca
            ORDER BY kg_entregados DESC, rutas_finalizadas DESC
            """;

    /** Una fila por vehiculo con su resumen de actividad. */
    public List<ActividadVehiculoDTO> actividadPorVehiculo() throws SQLException {
        List<ActividadVehiculoDTO> lista = new ArrayList<>();
        try (Connection cn = ConexionBD.obtenerConexion();
             PreparedStatement ps = cn.prepareStatement(SQL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(new ActividadVehiculoDTO(
                        rs.getString("placa"),
                        rs.getString("marca"),
                        rs.getInt("rutas_totales"),
                        rs.getInt("rutas_finalizadas"),
                        rs.getInt("paquetes_entregados"),
                        rs.getDouble("kg_entregados"),
                        rs.getDouble("costo_mantenimiento")));
            }
        }
        return lista;
    }
}
