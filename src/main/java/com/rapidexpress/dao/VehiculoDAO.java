package com.rapidexpress.dao;

import com.rapidexpress.model.EstadoVehiculo;
import com.rapidexpress.model.Vehiculo;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos de la tabla 'vehiculos'.
 *
 * Todos los metodos reciben la Connection como parametro: asi la capa Service
 * puede agrupar varias operaciones dentro de una misma transaccion.
 */
public class VehiculoDAO {

    private static final String COLUMNAS =
            "id_vehiculo, placa, marca, modelo, anio_fabricacion, capacidad_carga_kg, estado";

    public int insertar(Connection cn, Vehiculo v) throws SQLException {
        String sql = "INSERT INTO vehiculos (placa, marca, modelo, anio_fabricacion, capacidad_carga_kg, estado) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, v.getPlaca());
            ps.setString(2, v.getMarca());
            ps.setString(3, v.getModelo());
            ps.setInt(4, v.getAnioFabricacion());
            ps.setBigDecimal(5, v.getCapacidadCargaKg());
            ps.setString(6, v.getEstado().getEtiqueta());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    v.setIdVehiculo(rs.getInt(1));
                }
            }
        }
        return v.getIdVehiculo();
    }

    public boolean actualizar(Connection cn, Vehiculo v) throws SQLException {
        String sql = "UPDATE vehiculos SET placa = ?, marca = ?, modelo = ?, anio_fabricacion = ?, "
                   + "capacidad_carga_kg = ? WHERE id_vehiculo = ?";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, v.getPlaca());
            ps.setString(2, v.getMarca());
            ps.setString(3, v.getModelo());
            ps.setInt(4, v.getAnioFabricacion());
            ps.setBigDecimal(5, v.getCapacidadCargaKg());
            ps.setInt(6, v.getIdVehiculo());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean actualizarEstado(Connection cn, int idVehiculo, EstadoVehiculo estado) throws SQLException {
        String sql = "UPDATE vehiculos SET estado = ? WHERE id_vehiculo = ?";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, estado.getEtiqueta());
            ps.setInt(2, idVehiculo);
            return ps.executeUpdate() > 0;
        }
    }

    public boolean eliminar(Connection cn, int idVehiculo) throws SQLException {
        String sql = "DELETE FROM vehiculos WHERE id_vehiculo = ?";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idVehiculo);
            return ps.executeUpdate() > 0;
        }
    }

    public Vehiculo buscarPorId(Connection cn, int idVehiculo) throws SQLException {
        String sql = "SELECT " + COLUMNAS + " FROM vehiculos WHERE id_vehiculo = ?";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idVehiculo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    public Vehiculo buscarPorPlaca(Connection cn, String placa) throws SQLException {
        String sql = "SELECT " + COLUMNAS + " FROM vehiculos WHERE placa = ?";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, placa == null ? null : placa.trim().toUpperCase());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    public List<Vehiculo> listarTodos(Connection cn) throws SQLException {
        String sql = "SELECT " + COLUMNAS + " FROM vehiculos ORDER BY placa";
        List<Vehiculo> lista = new ArrayList<>();
        try (PreparedStatement ps = cn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(mapear(rs));
            }
        }
        return lista;
    }

    public List<Vehiculo> listarPorEstado(Connection cn, EstadoVehiculo estado) throws SQLException {
        String sql = "SELECT " + COLUMNAS + " FROM vehiculos WHERE estado = ? ORDER BY placa";
        List<Vehiculo> lista = new ArrayList<>();
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

    /**
     * Igual que buscarPorId pero bloqueando la fila (SELECT ... FOR UPDATE).
     * Lo usa la capa Service dentro de transacciones para evitar que dos
     * operaciones concurrentes cambien el estado del mismo vehiculo a la vez.
     */
    public Vehiculo buscarPorIdBloqueando(Connection cn, int idVehiculo) throws SQLException {
        String sql = "SELECT " + COLUMNAS + " FROM vehiculos WHERE id_vehiculo = ? FOR UPDATE";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setInt(1, idVehiculo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    private Vehiculo mapear(ResultSet rs) throws SQLException {
        Vehiculo v = new Vehiculo();
        v.setIdVehiculo(rs.getInt("id_vehiculo"));
        v.setPlaca(rs.getString("placa"));
        v.setMarca(rs.getString("marca"));
        v.setModelo(rs.getString("modelo"));
        v.setAnioFabricacion(rs.getInt("anio_fabricacion"));
        v.setCapacidadCargaKg(rs.getBigDecimal("capacidad_carga_kg"));
        v.setEstado(EstadoVehiculo.desdeEtiqueta(rs.getString("estado")));
        return v;
    }
}
