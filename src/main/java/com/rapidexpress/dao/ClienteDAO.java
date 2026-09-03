package com.rapidexpress.dao;

import com.rapidexpress.model.Cliente;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos de "clientes" (remitentes y destinatarios). (Persona 2)
 *
 * Sigue la misma convencion que los DAO de la Persona 1: reciben la Connection
 * desde afuera y no validan reglas de negocio.
 */
public class ClienteDAO {

    public int insertar(Connection cn, Cliente c) throws SQLException {
        String sql = "INSERT INTO clientes (documento, nombre, telefono, email, direccion, ciudad) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.getDocumento());
            ps.setString(2, c.getNombre());
            ps.setString(3, c.getTelefono());
            ps.setString(4, c.getEmail());
            ps.setString(5, c.getDireccion());
            ps.setString(6, c.getCiudad());
            ps.executeUpdate();
            try (ResultSet claves = ps.getGeneratedKeys()) {
                if (claves.next()) {
                    c.setIdCliente(claves.getInt(1));
                }
            }
        }
        return c.getIdCliente();
    }

    public Cliente buscarPorId(Connection cn, int idCliente) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement("SELECT * FROM clientes WHERE id_cliente = ?")) {
            ps.setInt(1, idCliente);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    public Cliente buscarPorDocumento(Connection cn, String documento) throws SQLException {
        try (PreparedStatement ps = cn.prepareStatement("SELECT * FROM clientes WHERE documento = ?")) {
            ps.setString(1, documento);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    public List<Cliente> listar(Connection cn) throws SQLException {
        List<Cliente> clientes = new ArrayList<>();
        try (PreparedStatement ps = cn.prepareStatement("SELECT * FROM clientes ORDER BY nombre");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                clientes.add(mapear(rs));
            }
        }
        return clientes;
    }

    public boolean actualizar(Connection cn, Cliente c) throws SQLException {
        String sql = "UPDATE clientes SET documento = ?, nombre = ?, telefono = ?, email = ?, "
                   + "direccion = ?, ciudad = ? WHERE id_cliente = ?";
        try (PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, c.getDocumento());
            ps.setString(2, c.getNombre());
            ps.setString(3, c.getTelefono());
            ps.setString(4, c.getEmail());
            ps.setString(5, c.getDireccion());
            ps.setString(6, c.getCiudad());
            ps.setInt(7, c.getIdCliente());
            return ps.executeUpdate() == 1;
        }
    }

    private Cliente mapear(ResultSet rs) throws SQLException {
        Cliente c = new Cliente();
        c.setIdCliente(rs.getInt("id_cliente"));
        c.setDocumento(rs.getString("documento"));
        c.setNombre(rs.getString("nombre"));
        c.setTelefono(rs.getString("telefono"));
        c.setEmail(rs.getString("email"));
        c.setDireccion(rs.getString("direccion"));
        c.setCiudad(rs.getString("ciudad"));
        return c;
    }
}
