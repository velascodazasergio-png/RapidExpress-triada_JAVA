package com.rapidexpress.examen;

import com.rapidexpress.model.EstadoVehiculo;
import com.rapidexpress.model.Vehiculo;
import com.rapidexpress.model.dto.TablaReporte;
import com.rapidexpress.util.ConexionBD;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Coleccion de CONSULTAS de solo lectura de ejemplo.
 *
 * <p>Todas siguen la misma receta que el resto de DAOs del proyecto:
 * {@code try-with-resources}, parametros con {@code ?} (nunca concatenados),
 * devuelven objetos del dominio o un {@link TablaReporte} generico, y propagan
 * {@link SQLException} para que la capa de arriba decida como mostrarla.</p>
 */
public class ConsultaDAO {

    /**
     * Devuelve los vehiculos cuya capacidad de carga es mayor o igual a
     * {@code kg}, ordenados de mayor a menor capacidad.
     */
    public List<Vehiculo> vehiculosConCapacidadMinima(BigDecimal kg) throws SQLException {
        String sql = "SELECT id_vehiculo, placa, marca, modelo, anio_fabricacion, capacidad_carga_kg, estado "
                   + "FROM vehiculos WHERE capacidad_carga_kg >= ? ORDER BY capacidad_carga_kg DESC";
        List<Vehiculo> lista = new ArrayList<>();
        try (Connection cn = ConexionBD.obtenerConexion();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setBigDecimal(1, kg);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Vehiculo v = new Vehiculo();
                    v.setIdVehiculo(rs.getInt("id_vehiculo"));
                    v.setPlaca(rs.getString("placa"));
                    v.setMarca(rs.getString("marca"));
                    v.setModelo(rs.getString("modelo"));
                    v.setAnioFabricacion(rs.getInt("anio_fabricacion"));
                    v.setCapacidadCargaKg(rs.getBigDecimal("capacidad_carga_kg"));
                    v.setEstado(EstadoVehiculo.desdeEtiqueta(rs.getString("estado")));
                    lista.add(v);
                }
            }
        }
        return lista;
    }

    /**
     * Lista los conductores en estado Activo que hoy no tienen ningun vehiculo
     * asignado (patron LEFT JOIN + IS NULL: "los que NO tienen ...").
     */
    public TablaReporte conductoresSinVehiculo() throws SQLException {
        String sql = """
                SELECT c.id_conductor        AS id,
                       c.identificacion      AS identificacion,
                       c.nombre_completo     AS nombre,
                       c.tipo_licencia       AS licencia,
                       c.contacto            AS contacto
                FROM conductores c
                LEFT JOIN asignaciones_conductor_vehiculo a
                       ON a.id_conductor = c.id_conductor AND a.fecha_fin IS NULL
                WHERE a.id_asignacion IS NULL
                  AND c.estado = 'Activo'
                ORDER BY c.nombre_completo
                """;
        return ejecutarSinParametros("CONDUCTORES ACTIVOS SIN VEHICULO ASIGNADO", sql);
    }

    /** Ejecuta un SELECT sin parametros y lo envuelve en un {@link TablaReporte}. */
    private TablaReporte ejecutarSinParametros(String titulo, String sql) throws SQLException {
        try (Connection cn = ConexionBD.obtenerConexion();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return TablaReporte.desde(titulo, rs);
        }
    }
}
