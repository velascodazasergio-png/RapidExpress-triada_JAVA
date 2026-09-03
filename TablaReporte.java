package com.rapidexpress.model.dto;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Proyección tabular genérica de un reporte.
 *
 * <p>Los reportes agregados (estado de la flota, desempeño de conductores,
 * ocupación de rutas) no son entidades del dominio: no se persisten, no tienen
 * identidad y no exponen comportamiento. Mapearlos a una clase por consulta
 * añadiría ocho ficheros que solo sirven para transportar cadenas hasta la
 * vista. Esta clase los representa como lo que son: encabezados y filas.</p>
 *
 * <p>Los dos reportes que el enunciado exige de forma explícita sí tienen DTO
 * propio ({@link EntregaDTO}, {@link HistorialRutaDTO}) porque el controlador
 * opera sobre sus campos y no solo los imprime.</p>
 */
public class TablaReporte {

    private final String titulo;
    private final List<String> encabezados;
    private final List<String[]> filas;

    public TablaReporte(String titulo, List<String> encabezados, List<String[]> filas) {
        this.titulo = titulo;
        this.encabezados = encabezados;
        this.filas = filas;
    }

    /** Construye la tabla a partir de cualquier ResultSet, sin conocer su forma. */
    public static TablaReporte desde(String titulo, ResultSet rs) throws SQLException {
        ResultSetMetaData meta = rs.getMetaData();
        int columnas = meta.getColumnCount();

        List<String> encabezados = new ArrayList<>(columnas);
        for (int i = 1; i <= columnas; i++) {
            encabezados.add(meta.getColumnLabel(i));
        }

        List<String[]> filas = new ArrayList<>();
        while (rs.next()) {
            String[] fila = new String[columnas];
            for (int i = 1; i <= columnas; i++) {
                Object valor = rs.getObject(i);
                fila[i - 1] = (valor == null) ? "-" : String.valueOf(valor);
            }
            filas.add(fila);
        }
        return new TablaReporte(titulo, encabezados, filas);
    }

    public String getTitulo()             { return titulo; }
    public List<String> getEncabezados()  { return encabezados; }
    public List<String[]> getFilas()      { return filas; }
    public boolean estaVacia()            { return filas.isEmpty(); }
}
