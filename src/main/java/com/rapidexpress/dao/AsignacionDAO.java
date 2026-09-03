package com.rapidexpress.dao;

import com.rapidexpress.model.Asignacion;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** Acceso a datos de la tabla 'asignaciones_conductor_vehiculo'. */
public class AsignacionDAO {

    private static final String SELECT_BASE =
            "SELECT a.id_asignacion, a.id_conductor, a.id_vehiculo, a.fecha_inicio, a.fecha_fin, "
          + "       c.nombre_completo, v.placa "
          + "FROM asignaciones_conductor_vehiculo a "
          + "JOIN conductores c ON c.id_conductor = a.id_conductor "
          + "JOIN vehiculos  v ON v.id_vehiculo  = a.id_vehiculo ";

    public int insertar(Connection cn, Asignacion a) throws SQLException {
        String sql = "INSERT INTO asignaciones_conductor_vehiculo (id_conductor, id_vehiculo, fecha_inicio) "
                   + "VALUES (?, ?, ?)";
        try (PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, a.getIdConductor());
            ps.setInt(2, a.getIdVehiculo());
            ps.setTimestamp(3, Timestamp.valueOf(a.getFechaInicio()));
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    a.setIdAsignacion(rs.getInt(1));
                }
            }
        }
        return a.getIdAsignacion();
    }

    /** Cierra la asignacion vigente marcando la fecha de finalizacion. */
    public boolean cerrar(Connection cn, int idAsignacion) throws SQLException {
        String sql = "UPDATE asignaciones_conductor_vehiculo SET fecha_fin = NOW() "
                   + "WHERE id_asignacion = ? AND fecha_fin IS NULL";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idAsignacion);
            return ps.executeUpdate() > 0;
        }
    }

    public Asignacion buscarVigentePorConductor(Connection cn, int idConductor) throws SQLException {
        String sql = SELECT_BASE + "WHERE a.id_conductor = ? AND a.fecha_fin IS NULL";
        return buscarUno(cn, sql, idConductor);
    }

    public Asignacion buscarVigentePorVehiculo(Connection cn, int idVehiculo) throws SQLException {
        String sql = SELECT_BASE + "WHERE a.id_vehiculo = ? AND a.fecha_fin IS NULL";
        return buscarUno(cn, sql, idVehiculo);
    }

    public List<Asignacion> listarVigentes(Connection cn) throws SQLException {
        String sql = SELECT_BASE + "WHERE a.fecha_fin IS NULL ORDER BY v.placa";
        List<Asignacion> lista = new ArrayList<>();
        try (PreparedStatement ps = cn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public List<Asignacion> listarHistorialPorVehiculo(Connection cn, int idVehiculo) throws SQLException {
        String sql = SELECT_BASE + "WHERE a.id_vehiculo = ? ORDER BY a.fecha_inicio DESC";
        return listar(cn, sql, idVehiculo);
    }

    public List<Asignacion> listarHistorialPorConductor(Connection cn, int idConductor) throws SQLException {
        String sql = SELECT_BASE + "WHERE a.id_conductor = ? ORDER BY a.fecha_inicio DESC";
        return listar(cn, sql, idConductor);
    }

    private Asignacion buscarUno(Connection cn, String sql, int parametro) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, parametro);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    private List<Asignacion> listar(Connection cn, String sql, int parametro) throws SQLException {
        List<Asignacion> lista = new ArrayList<>();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, parametro);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    private Asignacion mapear(ResultSet rs) throws SQLException {
        Asignacion a = new Asignacion();
        a.setIdAsignacion(rs.getInt("id_asignacion"));
        a.setIdConductor(rs.getInt("id_conductor"));
        a.setIdVehiculo(rs.getInt("id_vehiculo"));
        a.setFechaInicio(rs.getTimestamp("fecha_inicio").toLocalDateTime());
        Timestamp fin = rs.getTimestamp("fecha_fin");
        a.setFechaFin(fin == null ? null : fin.toLocalDateTime());
        a.setNombreConductor(rs.getString("nombre_completo"));
        a.setPlacaVehiculo(rs.getString("placa"));
        return a;
    }
}
