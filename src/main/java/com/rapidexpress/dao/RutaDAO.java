package com.rapidexpress.dao;

import com.rapidexpress.model.EstadoRuta;
import com.rapidexpress.model.Ruta;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Acceso a datos de "rutas" y "ruta_paquetes". (Persona 2) */
public class RutaDAO {

    /** El JOIN a vehiculos/conductores es solo para mostrar placa y nombre. */
    private static final String SELECT_BASE =
            "SELECT r.*, v.placa AS placa_vehiculo, v.capacidad_carga_kg, c.nombre_completo AS nombre_conductor "
          + "FROM rutas r "
          + "JOIN vehiculos v   ON v.id_vehiculo  = r.id_vehiculo "
          + "JOIN conductores c ON c.id_conductor = r.id_conductor ";

    public int insertar(Connection cn, Ruta r) throws SQLException {
        String sql = "INSERT INTO rutas (codigo, fecha, id_vehiculo, id_conductor, estado, observaciones) "
                   + "VALUES (?,?,?,?,?,?)";
        try (PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, r.getCodigo());
            ps.setDate(2, Date.valueOf(r.getFecha()));
            ps.setInt(3, r.getIdVehiculo());
            ps.setInt(4, r.getIdConductor());
            ps.setString(5, r.getEstado().getEtiqueta());
            ps.setString(6, r.getObservaciones());
            ps.executeUpdate();
            try (ResultSet claves = ps.getGeneratedKeys()) {
                if (claves.next()) {
                    r.setIdRuta(claves.getInt(1));
                }
            }
        }
        return r.getIdRuta();
    }

    public void agregarPaquete(Connection cn, int idRuta, int idPaquete, int ordenEntrega) throws SQLException {
        String sql = "INSERT INTO ruta_paquetes (id_ruta, id_paquete, orden_entrega) VALUES (?,?,?)";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idRuta);
            ps.setInt(2, idPaquete);
            ps.setInt(3, ordenEntrega);
            ps.executeUpdate();
        }
    }

    public Ruta buscarPorId(Connection cn, int idRuta) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement(SELECT_BASE + "WHERE r.id_ruta = ?")) {
            ps.setInt(1, idRuta);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    public Ruta buscarPorCodigo(Connection cn, String codigo) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement(SELECT_BASE + "WHERE r.codigo = ?")) {
            ps.setString(1, codigo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    /** Lee y BLOQUEA la ruta hasta el commit. Solo dentro de transaccion. */
    public Ruta buscarPorIdBloqueando(Connection cn, int idRuta) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement("SELECT * FROM rutas WHERE id_ruta = ? FOR UPDATE")) {
            ps.setInt(1, idRuta);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapearSimple(rs) : null;
            }
        }
    }

    public List<Ruta> listarPorEstado(Connection cn, EstadoRuta estado) throws SQLException {
        List<Ruta> rutas = new ArrayList<>();
        try (PreparedStatement ps = cn.prepareStatement(
                SELECT_BASE + "WHERE r.estado = ? ORDER BY r.fecha DESC, r.id_ruta DESC")) {
            ps.setString(1, estado.getEtiqueta());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rutas.add(mapear(rs));
                }
            }
        }
        return rutas;
    }

    public List<Ruta> listarPorFecha(Connection cn, LocalDate fecha) throws SQLException {
        List<Ruta> rutas = new ArrayList<>();
        try (PreparedStatement ps = cn.prepareStatement(SELECT_BASE + "WHERE r.fecha = ? ORDER BY r.id_ruta")) {
            ps.setDate(1, Date.valueOf(fecha));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rutas.add(mapear(rs));
                }
            }
        }
        return rutas;
    }

    /** Cambio de estado condicional: evita que dos terminales inicien la misma ruta. */
    public boolean actualizarEstadoSi(Connection cn, int idRuta,
                                      EstadoRuta esperado, EstadoRuta nuevo) throws SQLException {
        String sql = "UPDATE rutas SET estado = ?, "
                   + "iniciada_en = CASE WHEN ? = 'En Curso' THEN NOW() ELSE iniciada_en END, "
                   + "finalizada_en = CASE WHEN ? IN ('Finalizada','Cancelada') THEN NOW() ELSE finalizada_en END "
                   + "WHERE id_ruta = ? AND estado = ?";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, nuevo.getEtiqueta());
            ps.setString(2, nuevo.getEtiqueta());
            ps.setString(3, nuevo.getEtiqueta());
            ps.setInt(4, idRuta);
            ps.setString(5, esperado.getEtiqueta());
            return ps.executeUpdate() == 1;
        }
    }

    /** Deja constancia del conductor que realmente salio, si la asignacion cambio. */
    public void actualizarConductor(Connection cn, int idRuta, int idConductor) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement("UPDATE rutas SET id_conductor = ? WHERE id_ruta = ?")) {
            ps.setInt(1, idConductor);
            ps.setInt(2, idRuta);
            ps.executeUpdate();
        }
    }

    /** Ids de los paquetes de la ruta, ORDENADOS: clave para no generar deadlocks. */
    public List<Integer> idsPaquetesDeRuta(Connection cn, int idRuta) throws SQLException {
        List<Integer> ids = new ArrayList<>();
        try (PreparedStatement ps = cn.prepareStatement(
                "SELECT id_paquete FROM ruta_paquetes WHERE id_ruta = ? ORDER BY id_paquete")) {
            ps.setInt(1, idRuta);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ids.add(rs.getInt(1));
                }
            }
        }
        return ids;
    }

    /** true si el vehiculo ya tiene una ruta activa (Planificada o En Curso) esa fecha. */
    public boolean vehiculoTieneRutaActivaEnFecha(Connection cn, int idVehiculo, LocalDate fecha) throws SQLException {
        String sql = "SELECT 1 FROM rutas WHERE id_vehiculo = ? AND fecha = ? "
                   + "AND estado IN ('Planificada','En Curso')";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idVehiculo);
            ps.setDate(2, Date.valueOf(fecha));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /** true si el conductor ya tiene una ruta activa (Planificada o En Curso) esa fecha. */
    public boolean conductorTieneRutaActivaEnFecha(Connection cn, int idConductor, LocalDate fecha) throws SQLException {
        String sql = "SELECT 1 FROM rutas WHERE id_conductor = ? AND fecha = ? "
                   + "AND estado IN ('Planificada','En Curso')";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idConductor);
            ps.setDate(2, Date.valueOf(fecha));
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /** true si el paquete ya esta en una ruta que aun no termino. */
    public boolean paqueteYaTieneRutaActiva(Connection cn, int idPaquete) throws SQLException {
        String sql = "SELECT 1 FROM ruta_paquetes rp JOIN rutas r ON r.id_ruta = rp.id_ruta "
                   + "WHERE rp.id_paquete = ? AND r.estado IN ('Planificada','En Curso')";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idPaquete);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /** Suma del peso de los paquetes de una ruta, calculada en la BD. */
    public BigDecimal pesoTotalDeRuta(Connection cn, int idRuta) throws SQLException {
        String sql = "SELECT COALESCE(SUM(p.peso_kg), 0) FROM ruta_paquetes rp "
                   + "JOIN paquetes p ON p.id_paquete = rp.id_paquete WHERE rp.id_ruta = ?";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idRuta);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getBigDecimal(1) : BigDecimal.ZERO;
            }
        }
    }

    public int contarPaquetesPendientes(Connection cn, int idRuta) throws SQLException {
        String sql = "SELECT COUNT(*) FROM ruta_paquetes rp JOIN paquetes p ON p.id_paquete = rp.id_paquete "
                   + "WHERE rp.id_ruta = ? AND p.estado NOT IN ('Entregado','Devuelto')";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idRuta);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private Ruta mapear(ResultSet rs) throws SQLException {
        Ruta r = mapearSimple(rs);
        r.setPlacaVehiculo(rs.getString("placa_vehiculo"));
        r.setNombreConductor(rs.getString("nombre_conductor"));
        r.setCapacidadVehiculoKg(rs.getBigDecimal("capacidad_carga_kg"));
        return r;
    }

    private Ruta mapearSimple(ResultSet rs) throws SQLException {
        Ruta r = new Ruta();
        r.setIdRuta(rs.getInt("id_ruta"));
        r.setCodigo(rs.getString("codigo"));
        r.setFecha(rs.getDate("fecha").toLocalDate());
        r.setIdVehiculo(rs.getInt("id_vehiculo"));
        r.setIdConductor(rs.getInt("id_conductor"));
        r.setEstado(EstadoRuta.desdeEtiqueta(rs.getString("estado")));
        r.setObservaciones(rs.getString("observaciones"));
        Timestamp iniciada = rs.getTimestamp("iniciada_en");
        if (iniciada != null) {
            r.setIniciadaEn(iniciada.toLocalDateTime());
        }
        Timestamp finalizada = rs.getTimestamp("finalizada_en");
        if (finalizada != null) {
            r.setFinalizadaEn(finalizada.toLocalDateTime());
        }
        return r;
    }
}
