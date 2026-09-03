package com.rapidexpress.dao;

import com.rapidexpress.model.Conductor;
import com.rapidexpress.model.EstadoConductor;
import com.rapidexpress.model.TipoLicencia;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** Acceso a datos de la tabla 'conductores'. */
public class ConductorDAO {

    private static final String COLUMNAS =
            "id_conductor, identificacion, nombre_completo, tipo_licencia, contacto, estado";

    // Inserta el registro y devuelve su id generado.
    public int insertar(Connection cn, Conductor c) throws SQLException {
        String sql = "INSERT INTO conductores (identificacion, nombre_completo, tipo_licencia, contacto, estado) "
                   + "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.getIdentificacion());
            ps.setString(2, c.getNombreCompleto());
            ps.setString(3, c.getTipoLicencia().name());
            ps.setString(4, c.getContacto());
            ps.setString(5, c.getEstado().getEtiqueta());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    c.setIdConductor(rs.getInt(1));
                }
            }
        }
        return c.getIdConductor();
    }

    // Actualiza el registro; devuelve si cambio alguna fila.
    public boolean actualizar(Connection cn, Conductor c) throws SQLException {
        String sql = "UPDATE conductores SET identificacion = ?, nombre_completo = ?, tipo_licencia = ?, "
                   + "contacto = ? WHERE id_conductor = ?";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, c.getIdentificacion());
            ps.setString(2, c.getNombreCompleto());
            ps.setString(3, c.getTipoLicencia().name());
            ps.setString(4, c.getContacto());
            ps.setInt(5, c.getIdConductor());
            return ps.executeUpdate() > 0;
        }
    }

    // Actualiza solo el estado del registro.
    public boolean actualizarEstado(Connection cn, int idConductor, EstadoConductor estado) throws SQLException {
        String sql = "UPDATE conductores SET estado = ? WHERE id_conductor = ?";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, estado.getEtiqueta());
            ps.setInt(2, idConductor);
            return ps.executeUpdate() > 0;
        }
    }

    // Elimina el registro por su id.
    public boolean eliminar(Connection cn, int idConductor) throws SQLException {
        String sql = "DELETE FROM conductores WHERE id_conductor = ?";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idConductor);
            return ps.executeUpdate() > 0;
        }
    }

    // Busca un registro por su id.
    public Conductor buscarPorId(Connection cn, int idConductor) throws SQLException {
        String sql = "SELECT " + COLUMNAS + " FROM conductores WHERE id_conductor = ?";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idConductor);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    // Busca un conductor por su identificacion.
    public Conductor buscarPorIdentificacion(Connection cn, String identificacion) throws SQLException {
        String sql = "SELECT " + COLUMNAS + " FROM conductores WHERE identificacion = ?";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, identificacion == null ? null : identificacion.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    // Lista todos los registros ordenados.
    public List<Conductor> listarTodos(Connection cn) throws SQLException {
        String sql = "SELECT " + COLUMNAS + " FROM conductores ORDER BY nombre_completo";
        List<Conductor> lista = new ArrayList<>();
        try (PreparedStatement ps = cn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    // Lista los registros filtrados por estado.
    public List<Conductor> listarPorEstado(Connection cn, EstadoConductor estado) throws SQLException {
        String sql = "SELECT " + COLUMNAS + " FROM conductores WHERE estado = ? ORDER BY nombre_completo";
        List<Conductor> lista = new ArrayList<>();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, estado.getEtiqueta());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapear(rs));
                }
            }
        }
        return lista;
    }

    /** Version con bloqueo de fila para uso dentro de transacciones. */
    public Conductor buscarPorIdBloqueando(Connection cn, int idConductor) throws SQLException {
        String sql = "SELECT " + COLUMNAS + " FROM conductores WHERE id_conductor = ? FOR UPDATE";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idConductor);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    // Convierte la fila actual del ResultSet en un objeto.
    private Conductor mapear(ResultSet rs) throws SQLException {
        Conductor c = new Conductor();
        c.setIdConductor(rs.getInt("id_conductor"));
        c.setIdentificacion(rs.getString("identificacion"));
        c.setNombreCompleto(rs.getString("nombre_completo"));
        c.setTipoLicencia(TipoLicencia.desdeTexto(rs.getString("tipo_licencia")));
        c.setContacto(rs.getString("contacto"));
        c.setEstado(EstadoConductor.desdeEtiqueta(rs.getString("estado")));
        return c;
    }
}
