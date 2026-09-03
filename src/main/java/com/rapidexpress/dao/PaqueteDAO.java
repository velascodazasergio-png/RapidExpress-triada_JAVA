package com.rapidexpress.dao;

import com.rapidexpress.model.EstadoPaquete;
import com.rapidexpress.model.Paquete;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/** Acceso a datos de "paquetes". (Persona 2) */
public class PaqueteDAO {

    private static final String SELECT_BASE =
            "SELECT p.*, r.nombre AS nombre_remitente, d.nombre AS nombre_destinatario "
          + "FROM paquetes p "
          + "JOIN clientes r ON r.id_cliente = p.id_remitente "
          + "JOIN clientes d ON d.id_cliente = p.id_destinatario ";

    public int insertar(Connection cn, Paquete p) throws SQLException {
        String sql = "INSERT INTO paquetes (codigo_seguimiento, descripcion, peso_kg, largo_cm, ancho_cm, alto_cm, "
                   + "id_remitente, id_destinatario, direccion_origen, ciudad_origen, direccion_destino, "
                   + "ciudad_destino, estado) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getCodigoSeguimiento());
            ps.setString(2, p.getDescripcion());
            ps.setBigDecimal(3, p.getPesoKg());
            ps.setBigDecimal(4, p.getLargoCm());
            ps.setBigDecimal(5, p.getAnchoCm());
            ps.setBigDecimal(6, p.getAltoCm());
            ps.setInt(7, p.getIdRemitente());
            ps.setInt(8, p.getIdDestinatario());
            ps.setString(9, p.getDireccionOrigen());
            ps.setString(10, p.getCiudadOrigen());
            ps.setString(11, p.getDireccionDestino());
            ps.setString(12, p.getCiudadDestino());
            ps.setString(13, p.getEstado().getEtiqueta());
            ps.executeUpdate();
            try (ResultSet claves = ps.getGeneratedKeys()) {
                if (claves.next()) {
                    p.setIdPaquete(claves.getInt(1));
                }
            }
        }
        return p.getIdPaquete();
    }

    public Paquete buscarPorId(Connection cn, int idPaquete) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement(SELECT_BASE + "WHERE p.id_paquete = ?")) {
            ps.setInt(1, idPaquete);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    public Paquete buscarPorCodigo(Connection cn, String codigo) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement(SELECT_BASE + "WHERE p.codigo_seguimiento = ?")) {
            ps.setString(1, codigo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    /**
     * Lee el paquete y lo BLOQUEA hasta el commit (SELECT ... FOR UPDATE).
     * Solo debe llamarse dentro de una transaccion.
     * Mismo patron que VehiculoDAO.buscarPorIdBloqueando de la Persona 1.
     */
    public Paquete buscarPorIdBloqueando(Connection cn, int idPaquete) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement("SELECT * FROM paquetes WHERE id_paquete = ? FOR UPDATE")) {
            ps.setInt(1, idPaquete);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapearSimple(rs) : null;
            }
        }
    }

    public List<Paquete> listar(Connection cn) throws SQLException {
        return consultar(cn, SELECT_BASE + "ORDER BY p.id_paquete DESC", null);
    }

    public List<Paquete> listarPorEstado(Connection cn, EstadoPaquete estado) throws SQLException {
        return consultar(cn, SELECT_BASE + "WHERE p.estado = ? ORDER BY p.id_paquete", estado.getEtiqueta());
    }

    public List<Paquete> listarPorRuta(Connection cn, int idRuta) throws SQLException {
        String sql = SELECT_BASE
                   + "JOIN ruta_paquetes rp ON rp.id_paquete = p.id_paquete "
                   + "WHERE rp.id_ruta = ? ORDER BY rp.orden_entrega, p.id_paquete";
        List<Paquete> paquetes = new ArrayList<>();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idRuta);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    paquetes.add(mapear(rs));
                }
            }
        }
        return paquetes;
    }

    /**
     * Cambio de estado CONDICIONAL: solo actualiza si el paquete sigue en el
     * estado esperado. Devuelve false si otra terminal ya lo movio, y entonces
     * el Service aborta la transaccion completa.
     */
    public boolean actualizarEstadoSi(Connection cn, int idPaquete,
                                      EstadoPaquete esperado, EstadoPaquete nuevo) throws SQLException {
        String sql = "UPDATE paquetes SET estado = ?, "
                   + "fecha_entrega = CASE WHEN ? = 'Entregado' THEN NOW() ELSE fecha_entrega END "
                   + "WHERE id_paquete = ? AND estado = ?";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, nuevo.getEtiqueta());
            ps.setString(2, nuevo.getEtiqueta());
            ps.setInt(3, idPaquete);
            ps.setString(4, esperado.getEtiqueta());
            return ps.executeUpdate() == 1;
        }
    }

    /** Solo se permite editar mientras el paquete siga En Bodega. */
    public boolean actualizarDatos(Connection cn, Paquete p) throws SQLException {
        String sql = "UPDATE paquetes SET descripcion = ?, peso_kg = ?, largo_cm = ?, ancho_cm = ?, alto_cm = ?, "
                   + "direccion_origen = ?, ciudad_origen = ?, direccion_destino = ?, ciudad_destino = ? "
                   + "WHERE id_paquete = ? AND estado = 'En Bodega'";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, p.getDescripcion());
            ps.setBigDecimal(2, p.getPesoKg());
            ps.setBigDecimal(3, p.getLargoCm());
            ps.setBigDecimal(4, p.getAnchoCm());
            ps.setBigDecimal(5, p.getAltoCm());
            ps.setString(6, p.getDireccionOrigen());
            ps.setString(7, p.getCiudadOrigen());
            ps.setString(8, p.getDireccionDestino());
            ps.setString(9, p.getCiudadDestino());
            ps.setInt(10, p.getIdPaquete());
            return ps.executeUpdate() == 1;
        }
    }

    public boolean existeCodigo(Connection cn, String codigo) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement(
                "SELECT 1 FROM paquetes WHERE codigo_seguimiento = ?")) {
            ps.setString(1, codigo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private List<Paquete> consultar(Connection cn, String sql, String parametro) throws SQLException {
        List<Paquete> paquetes = new ArrayList<>();
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            if (parametro != null) {
                ps.setString(1, parametro);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    paquetes.add(mapear(rs));
                }
            }
        }
        return paquetes;
    }

    private Paquete mapear(ResultSet rs) throws SQLException {
        Paquete p = mapearSimple(rs);
        p.setNombreRemitente(rs.getString("nombre_remitente"));
        p.setNombreDestinatario(rs.getString("nombre_destinatario"));
        return p;
    }

    private Paquete mapearSimple(ResultSet rs) throws SQLException {
        Paquete p = new Paquete();
        p.setIdPaquete(rs.getInt("id_paquete"));
        p.setCodigoSeguimiento(rs.getString("codigo_seguimiento"));
        p.setDescripcion(rs.getString("descripcion"));
        p.setPesoKg(rs.getBigDecimal("peso_kg"));
        p.setLargoCm(rs.getBigDecimal("largo_cm"));
        p.setAnchoCm(rs.getBigDecimal("ancho_cm"));
        p.setAltoCm(rs.getBigDecimal("alto_cm"));
        p.setIdRemitente(rs.getInt("id_remitente"));
        p.setIdDestinatario(rs.getInt("id_destinatario"));
        p.setDireccionOrigen(rs.getString("direccion_origen"));
        p.setCiudadOrigen(rs.getString("ciudad_origen"));
        p.setDireccionDestino(rs.getString("direccion_destino"));
        p.setCiudadDestino(rs.getString("ciudad_destino"));
        p.setEstado(EstadoPaquete.desdeEtiqueta(rs.getString("estado")));
        Timestamp registro = rs.getTimestamp("fecha_registro");
        if (registro != null) {
            p.setFechaRegistro(registro.toLocalDateTime());
        }
        Timestamp entrega = rs.getTimestamp("fecha_entrega");
        if (entrega != null) {
            p.setFechaEntrega(entrega.toLocalDateTime());
        }
        return p;
    }
}
