package com.rapidexpress.dao;

import com.rapidexpress.model.Auditoria;
import com.rapidexpress.util.ConexionBD;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Acceso a datos de la bitácora de auditoría. */
public class AuditoriaDAO {

    private static final String SQL_INSERT = """
            INSERT INTO auditoria
                (fecha_hora, operacion, entidad, id_entidad, usuario,
                 estado_anterior, estado_nuevo, detalle)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;

    private static final String SQL_ULTIMOS = """
            SELECT id_auditoria, fecha_hora, operacion, entidad, id_entidad,
                   usuario, estado_anterior, estado_nuevo, detalle
            FROM auditoria
            ORDER BY fecha_hora DESC
            LIMIT ?
            """;

    private static final String SQL_POR_ENTIDAD = """
            SELECT id_auditoria, fecha_hora, operacion, entidad, id_entidad,
                   usuario, estado_anterior, estado_nuevo, detalle
            FROM auditoria
            WHERE entidad = ? AND id_entidad = ?
            ORDER BY fecha_hora
            """;

    /** Persiste un evento. Devuelve el id generado. */
    public long insertar(Auditoria evento) throws SQLException {
        try (Connection cn = ConexionBD.obtenerConexion();
             PreparedStatement ps = cn.prepareStatement(SQL_INSERT,
                     PreparedStatement.RETURN_GENERATED_KEYS)) {

            ps.setTimestamp(1, Timestamp.valueOf(evento.getFechaHora()));
            ps.setString(2, evento.getOperacion());
            ps.setString(3, evento.getEntidad());
            ps.setString(4, evento.getIdEntidad());
            ps.setString(5, evento.getUsuario());
            setNullable(ps, 6, evento.getEstadoAnterior());
            setNullable(ps, 7, evento.getEstadoNuevo());
            setNullable(ps, 8, evento.getDetalle());
            ps.executeUpdate();

            try (ResultSet claves = ps.getGeneratedKeys()) {
                return claves.next() ? claves.getLong(1) : 0L;
            }
        }
    }

    /** Últimos {@code limite} eventos, del más reciente al más antiguo. */
    public List<Auditoria> ultimos(int limite) throws SQLException {
        try (Connection cn = ConexionBD.obtenerConexion();
             PreparedStatement ps = cn.prepareStatement(SQL_ULTIMOS)) {
            ps.setInt(1, limite);
            try (ResultSet rs = ps.executeQuery()) {
                return mapear(rs);
            }
        }
    }

    /** Traza completa de una entidad concreta. */
    public List<Auditoria> porEntidad(String entidad, String idEntidad) throws SQLException {
        try (Connection cn = ConexionBD.obtenerConexion();
             PreparedStatement ps = cn.prepareStatement(SQL_POR_ENTIDAD)) {
            ps.setString(1, entidad);
            ps.setString(2, idEntidad);
            try (ResultSet rs = ps.executeQuery()) {
                return mapear(rs);
            }
        }
    }

    /** Bitácora filtrada por periodo y, opcionalmente, por operación. */
    public List<Auditoria> porPeriodo(LocalDateTime desde, LocalDateTime hasta,
                                      String operacion) throws SQLException {
        try (Connection cn = ConexionBD.obtenerConexion();
             CallableStatement cs = cn.prepareCall("{CALL sp_auditoria_por_periodo(?, ?, ?)}")) {
            cs.setTimestamp(1, Timestamp.valueOf(desde));
            cs.setTimestamp(2, Timestamp.valueOf(hasta));
            setNullable(cs, 3, operacion);
            try (ResultSet rs = cs.executeQuery()) {
                return mapear(rs);
            }
        }
    }

    // Convierte la fila actual del ResultSet en un objeto.
    private List<Auditoria> mapear(ResultSet rs) throws SQLException {
        List<Auditoria> lista = new ArrayList<>();
        while (rs.next()) {
            Timestamp ts = rs.getTimestamp("fecha_hora");
            lista.add(new Auditoria(
                    rs.getLong("id_auditoria"),
                    ts == null ? null : ts.toLocalDateTime(),
                    rs.getString("operacion"),
                    rs.getString("entidad"),
                    rs.getString("id_entidad"),
                    rs.getString("usuario"),
                    rs.getString("estado_anterior"),
                    rs.getString("estado_nuevo"),
                    rs.getString("detalle")));
        }
        return lista;
    }

    private void setNullable(PreparedStatement ps, int indice, String valor) throws SQLException {
        if (valor == null || valor.isBlank()) {
            ps.setNull(indice, Types.VARCHAR);
        } else {
            ps.setString(indice, valor);
        }
    }
}
