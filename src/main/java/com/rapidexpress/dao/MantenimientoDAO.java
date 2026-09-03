package com.rapidexpress.dao;

import com.rapidexpress.model.Mantenimiento;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Acceso a datos de la tabla 'mantenimientos'. */
public class MantenimientoDAO {

    private static final String COLUMNAS =
            "id_mantenimiento, id_vehiculo, tipo, descripcion, fecha_inicio, fecha_fin, costo, taller";

    public int insertar(Connection cn, Mantenimiento m) throws SQLException {
        String sql = "INSERT INTO mantenimientos (id_vehiculo, tipo, descripcion, fecha_inicio, fecha_fin, costo, taller) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, m.getIdVehiculo());
            ps.setString(2, m.getTipo().getEtiqueta());
            ps.setString(3, m.getDescripcion());
            ps.setDate(4, Date.valueOf(m.getFechaInicio()));
            ps.setDate(5, m.getFechaFin() == null ? null : Date.valueOf(m.getFechaFin()));
            ps.setBigDecimal(6, m.getCosto() == null ? BigDecimal.ZERO : m.getCosto());
            ps.setString(7, m.getTaller());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    m.setIdMantenimiento(rs.getInt(1));
                }
            }
        }
        return m.getIdMantenimiento();
    }

    /** Cierra un mantenimiento abierto registrando fecha de finalizacion y costo real. */
    public boolean cerrar(Connection cn, int idMantenimiento, LocalDate fechaFin, BigDecimal costo)
            throws SQLException {
        String sql = "UPDATE mantenimientos SET fecha_fin = ?, costo = ? "
                   + "WHERE id_mantenimiento = ? AND fecha_fin IS NULL";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(fechaFin));
            ps.setBigDecimal(2, costo == null ? BigDecimal.ZERO : costo);
            ps.setInt(3, idMantenimiento);
            return ps.executeUpdate() > 0;
        }
    }

    public Mantenimiento buscarPorId(Connection cn, int idMantenimiento) throws SQLException {
        String sql = "SELECT " + COLUMNAS + " FROM mantenimientos WHERE id_mantenimiento = ?";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idMantenimiento);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    /** Historial completo de un vehiculo, del mas reciente al mas antiguo. */
    public List<Mantenimiento> listarPorVehiculo(Connection cn, int idVehiculo) throws SQLException {
        String sql = "SELECT " + COLUMNAS + " FROM mantenimientos WHERE id_vehiculo = ? "
                   + "ORDER BY fecha_inicio DESC, id_mantenimiento DESC";
        List<Mantenimiento> lista = new ArrayList<>();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idVehiculo);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    public List<Mantenimiento> listarAbiertos(Connection cn) throws SQLException {
        String sql = "SELECT " + COLUMNAS + " FROM mantenimientos WHERE fecha_fin IS NULL "
                   + "ORDER BY fecha_inicio";
        List<Mantenimiento> lista = new ArrayList<>();
        try (PreparedStatement ps = cn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public int contarAbiertosPorVehiculo(Connection cn, int idVehiculo) throws SQLException {
        String sql = "SELECT COUNT(*) FROM mantenimientos WHERE id_vehiculo = ? AND fecha_fin IS NULL";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idVehiculo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private Mantenimiento mapear(ResultSet rs) throws SQLException {
        Mantenimiento m = new Mantenimiento();
        m.setIdMantenimiento(rs.getInt("id_mantenimiento"));
        m.setIdVehiculo(rs.getInt("id_vehiculo"));
        m.setTipo(Mantenimiento.Tipo.desdeEtiqueta(rs.getString("tipo")));
        m.setDescripcion(rs.getString("descripcion"));
        m.setFechaInicio(rs.getDate("fecha_inicio").toLocalDate());
        Date fin = rs.getDate("fecha_fin");
        m.setFechaFin(fin == null ? null : fin.toLocalDate());
        m.setCosto(rs.getBigDecimal("costo"));
        m.setTaller(rs.getString("taller"));
        return m;
    }
}
