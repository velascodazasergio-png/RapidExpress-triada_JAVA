package com.rapidexpress.dao;

import com.rapidexpress.model.dto.EntregaDTO;
import com.rapidexpress.model.dto.HistorialRutaDTO;
import com.rapidexpress.model.dto.TablaReporte;
import com.rapidexpress.util.ConexionBD;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Consultas de solo lectura del módulo de reportes.
 *
 * <p>Los dos reportes exigidos por el enunciado se resuelven con procedimientos
 * almacenados y los complementarios con vistas. En ambos casos la agregación la
 * hace el motor: traer las filas crudas a Java para sumarlas allí multiplicaría
 * el tráfico de red contra una base de datos que está en la nube.</p>
 */
public class ReporteDAO {

    // ---------------------------------------------------------------
    //  Reporte 1 · Entregas de un conductor en un rango de fechas
    // ---------------------------------------------------------------
    public List<EntregaDTO> entregasPorConductor(int idConductor, LocalDate desde,
                                                 LocalDate hasta) throws SQLException {
        List<EntregaDTO> lista = new ArrayList<>();
        try (Connection cn = ConexionBD.obtenerConexion();
             CallableStatement cs = cn.prepareCall("{CALL sp_entregas_por_conductor(?, ?, ?)}")) {

            cs.setInt(1, idConductor);
            cs.setDate(2, Date.valueOf(desde));
            cs.setDate(3, Date.valueOf(hasta));

            try (ResultSet rs = cs.executeQuery()) {
                while (rs.next()) {
                    Timestamp entrega = rs.getTimestamp("fecha_entrega");
                    lista.add(new EntregaDTO(
                            rs.getString("conductor"),
                            rs.getString("codigo_ruta"),
                            rs.getDate("fecha_programada").toLocalDate(),
                            rs.getString("placa"),
                            rs.getString("codigo_seguimiento"),
                            rs.getString("descripcion"),
                            rs.getDouble("peso_kg"),
                            rs.getString("direccion_destino"),
                            rs.getString("resultado"),
                            entrega == null ? null : entrega.toLocalDateTime()));
                }
            }
        }
        return lista;
    }

    // ---------------------------------------------------------------
    //  Reporte 2 · Historial de rutas de un vehículo
    // ---------------------------------------------------------------
    public List<HistorialRutaDTO> historialRutasVehiculo(int idVehiculo) throws SQLException {
        List<HistorialRutaDTO> lista = new ArrayList<>();
        try (Connection cn = ConexionBD.obtenerConexion();
             CallableStatement cs = cn.prepareCall("{CALL sp_historial_rutas_vehiculo(?)}")) {

            cs.setInt(1, idVehiculo);

            try (ResultSet rs = cs.executeQuery()) {
                while (rs.next()) {
                    // wasNull() se refiere SIEMPRE a la última columna leída, así que
                    // debe consultarse inmediatamente después de getInt y no al final.
                    int horas = rs.getInt("horas_ruta");
                    boolean horasEsNulo = rs.wasNull();

                    Timestamp ini = rs.getTimestamp("fecha_inicio");
                    Timestamp fin = rs.getTimestamp("fecha_fin");
                    lista.add(new HistorialRutaDTO(
                            rs.getString("placa"),
                            rs.getString("codigo_ruta"),
                            rs.getDate("fecha_programada").toLocalDate(),
                            rs.getString("estado_ruta"),
                            rs.getString("conductor"),
                            rs.getInt("paquetes"),
                            rs.getDouble("peso_kg"),
                            rs.getDouble("ocupacion_pct"),
                            ini == null ? null : ini.toLocalDateTime(),
                            fin == null ? null : fin.toLocalDateTime(),
                            horasEsNulo ? null : horas));
                }
            }
        }
        return lista;
    }

    // ---------------------------------------------------------------
    //  Reportes complementarios (vistas)
    // ---------------------------------------------------------------
    public TablaReporte estadoFlota() throws SQLException {
        return consultarVista("ESTADO GENERAL DE LA FLOTA",
                "SELECT * FROM v_estado_flota ORDER BY estado_vehiculo, placa");
    }

    // Devuelve el reporte de desempeno de conductores.
    public TablaReporte desempenoConductores() throws SQLException {
        return consultarVista("DESEMPEÑO DE CONDUCTORES",
                "SELECT * FROM v_desempeno_conductores ORDER BY paquetes_entregados DESC");
    }

    // Devuelve el reporte de ocupacion de rutas.
    public TablaReporte ocupacionRutas() throws SQLException {
        return consultarVista("OCUPACIÓN DE RUTAS",
                "SELECT * FROM v_ocupacion_rutas ORDER BY fecha_programada DESC");
    }

    // Devuelve el reporte de paquetes por estado.
    public TablaReporte paquetesPorEstado() throws SQLException {
        return consultarVista("PAQUETES POR ESTADO",
                "SELECT * FROM v_paquetes_por_estado ORDER BY cantidad DESC");
    }

    // Devuelve el reporte de actividad de auditoria.
    public TablaReporte actividadAuditoria() throws SQLException {
        return consultarVista("ACTIVIDAD REGISTRADA",
                "SELECT * FROM v_actividad_auditoria ORDER BY dia DESC, eventos DESC LIMIT 50");
    }

    /** Trazabilidad de un paquete por su código de seguimiento. */
    public TablaReporte seguimientoPaquete(String codigo) throws SQLException {
        try (Connection cn = ConexionBD.obtenerConexion();
             CallableStatement cs = cn.prepareCall("{CALL sp_seguimiento_paquete(?)}")) {
            cs.setString(1, codigo);
            try (ResultSet rs = cs.executeQuery()) {
                return TablaReporte.desde("SEGUIMIENTO DEL PAQUETE " + codigo, rs);
            }
        }
    }

    // Ejecuta una vista de solo lectura y la devuelve como tabla.
    private TablaReporte consultarVista(String titulo, String sql) throws SQLException {
        try (Connection cn = ConexionBD.obtenerConexion();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return TablaReporte.desde(titulo, rs);
        }
    }
}
