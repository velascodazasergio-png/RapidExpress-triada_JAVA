# Guía de examen — RapidExpress

Material de práctica para el examen del proyecto. Cubre los tres tipos de
tarea que suelen pedir y que se resuelven en ~1 hora:

1. **Añadir una consulta** (filtro, JOIN, agregación).
2. **Verificar / validar datos** antes de guardar.
3. **Añadir un reporte** (consulta + controlador + vista).

Todo el código de ejemplo está en el paquete
[`com.rapidexpress.examen`](../src/main/java/com/rapidexpress/examen/) y **no
toca el resto del proyecto**. Compila con `.\run.ps1` (o `mvn clean package`).

---

## Archivos entregados

| Archivo | Qué demuestra |
|---|---|
| `ValidacionUtil.java` | Verificación de datos reutilizable: obligatorio, rango, formato (placa, email, documento, teléfono), fecha no futura, rango de fechas, valor dentro de un conjunto. Lanza `NegocioException`. |
| `ConsultaDAO.java` | 8 consultas de ejemplo, una por patrón de SQL: filtro con parámetro, `LEFT JOIN + IS NULL`, `GROUP BY + LIMIT`, rango de fechas, `GROUP BY + HAVING`, `SUM` con `LEFT JOIN`, agregación a `Map`, escalar `COUNT`. |
| `ActividadVehiculoDTO.java` | `record` inmutable para transportar una fila de reporte. |
| `ReporteActividadDAO.java` | Reporte nuevo resuelto con **una sola consulta agregada** (sin crear vistas). |
| `ReporteActividadController.java` | Valida entradas y calcula los totales del pie del reporte. |
| `MenuConsultasView.java` | Menú de consola que une todo. Captura `NegocioException` y `SQLException`. |
| `AppConsultas.java` | `main` para ejecutar el módulo solo, sin tocar `Main`. |

### Cómo ejecutar el módulo de examen

```powershell
# 1. compilar (usa run.ps1 una vez)
.\run.ps1 -SkipBuild            # o .\run.ps1 y sales del menú de Main

# 2. lanzar SOLO este módulo
java -cp "target\classes;lib\mysql-connector-j-8.4.0.jar" com.rapidexpress.examen.AppConsultas
```

En Linux/macOS cambia `;` por `:` en el classpath.

### Integrar en el menú principal (si lo piden)

En [`Main.java`](../src/main/java/com/rapidexpress/Main.java), añade una opción
al `switch` del menú:

```java
import com.rapidexpress.examen.MenuConsultasView;
// ...
System.out.println("  7. Consultas y validaciones (examen)");
// ...
case 7 -> new MenuConsultasView().mostrar();
```

Nada más: `MenuConsultasView` ya sigue el mismo contrato que las demás vistas
(`new XxxView().mostrar()`).

---

## Arquitectura del proyecto (recordatorio de 30 segundos)

```
Vista  ──►  Controller  ──►  DAO  ──►  BD
(consola)   (valida,        (SQL,     (MySQL)
             calcula)        mapea)
```

- La **vista** lee datos del usuario y muestra resultados. Nunca abre conexiones.
- El **controller** valida parámetros y hace las cuentas del resumen. No hace SQL.
- El **DAO** es el único que escribe SQL. No valida reglas ni imprime.
- `ConexionBD.obtenerConexion()` da una conexión nueva → ciérrala con
  `try-with-resources`.
- `NegocioException` = error de regla de negocio → la vista muestra el mensaje.
- `TransaccionUtil.ejecutar(...)` = varias operaciones de escritura en una
  transacción con rollback. Para solo lectura no hace falta.

---

## Receta 1 · Añadir una CONSULTA

**Dónde:** un método nuevo en un DAO (usa `ConsultaDAO` como plantilla).

**Pasos:**

1. Escribe el SQL con `?` para cada parámetro (nunca concatenes valores).
2. `try (Connection cn = ConexionBD.obtenerConexion(); PreparedStatement ps = cn.prepareStatement(sql))`.
3. `ps.setXxx(1, valor)` en orden.
4. `try (ResultSet rs = ps.executeQuery())` y recorre con `while (rs.next())`.
5. Decide el tipo de retorno:
   - lista de entidades → `List<Vehiculo>` mapeando campo a campo;
   - tabla genérica para imprimir → `return TablaReporte.desde(titulo, rs);`
   - un número → `rs.getInt(1)`.
6. Propaga `throws SQLException`.

**Ejemplo mínimo (filtro con parámetro):**

```java
public List<Vehiculo> vehiculosConCapacidadMinima(BigDecimal kg) throws SQLException {
    String sql = "SELECT ... FROM vehiculos WHERE capacidad_carga_kg >= ? ORDER BY ...";
    List<Vehiculo> lista = new ArrayList<>();
    try (Connection cn = ConexionBD.obtenerConexion();
         PreparedStatement ps = cn.prepareStatement(sql)) {
        ps.setBigDecimal(1, kg);
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) { /* mapear y lista.add(...) */ }
        }
    }
    return lista;
}
```

**Truco para reportes rápidos:** si solo hay que *mostrar* el resultado y no
operar con él, devuelve `TablaReporte.desde(titulo, rs)` y en la vista llama
`ConsolaUtil.imprimir(tabla)`. Te ahorras crear un DTO y el formateo.

**Patrones de SQL y cuándo usarlos:**

| Necesitas... | SQL | Ejemplo en `ConsultaDAO` |
|---|---|---|
| Filtrar filas | `WHERE columna = ?` | `vehiculosConCapacidadMinima` |
| "Los que NO tienen..." | `LEFT JOIN ... WHERE x.id IS NULL` | `conductoresSinVehiculo` |
| Contar / sumar por grupo | `GROUP BY` + `COUNT/SUM` | `costoMantenimientoPorVehiculo` |
| Ranking / top N | `GROUP BY ... ORDER BY total DESC LIMIT ?` | `topClientesRemitentes` |
| Filtrar por el total | `GROUP BY ... HAVING COUNT(...) >= ?` | `destinatariosFrecuentes` |
| Entre dos fechas | `WHERE DATE(col) BETWEEN ? AND ?` | `paquetesEntregadosEntre` |
| Incluir grupos con 0 | `LEFT JOIN` + `COALESCE(SUM(x),0)` | `costoMantenimientoPorVehiculo` |
| ¿Existe? | `SELECT COUNT(*) ... WHERE ... = ?` | `existePlaca` |

---

## Receta 2 · VERIFICAR / VALIDAR datos

**Dónde:** en el **controller** o **service**, antes de llamar al DAO de
escritura. Usa `ValidacionUtil`.

**Pasos:**

1. Por cada campo, llama al método de `ValidacionUtil` adecuado.
2. Estos métodos **devuelven el valor normalizado** (trim, mayúsculas) → úsalo.
3. Para reglas contra la BD (placa/documento duplicado), consulta primero:
   `if (dao.existePlaca(placa)) throw new NegocioException("Ya existe...");`
4. `throws NegocioException`. La vista ya lo captura y muestra el mensaje.

**Ejemplo (validar un vehículo):**

```java
String placa      = ValidacionUtil.placa(entradaPlaca);            // ABC123
String marca      = ValidacionUtil.textoMax("marca", entradaMarca, 50);
int anio          = ValidacionUtil.anioFabricacion(entradaAnio);   // 1950..año+1
BigDecimal capKg  = ValidacionUtil.positivo("capacidad", entradaCap);
if (consultaDAO.existePlaca(placa))
    throw new NegocioException("Ya existe un vehiculo con la placa " + placa + ".");
```

**Métodos disponibles en `ValidacionUtil`:**

`texto`, `textoMax`, `enteroEnRango`, `positivo`, `noNegativo`, `placa`,
`email`, `documento`, `telefono`, `anioFabricacion`, `fechaNoFutura`,
`rangoFechas`, `opcion(campo, valor, permitidos...)`.

**Si piden una validación nueva**, añádela a `ValidacionUtil` con la misma
forma: recibe `(String campo, X valor)`, lanza `NegocioException` con mensaje
claro y devuelve el valor limpio.

---

## Receta 3 · Añadir un REPORTE completo

**Dónde:** 3–4 archivos. Copia el trío `ActividadVehiculoDTO` +
`ReporteActividadDAO` + `ReporteActividadController` y una opción en la vista.

**Pasos:**

1. **DTO** (`record`): un campo por columna del reporte. Métodos derivados
   opcionales (`porcentajeFinalizacion()`).
2. **DAO**: una consulta agregada (`GROUP BY`, `SUM`, `CASE WHEN`,
   `COALESCE`). Mapea cada fila al DTO. Devuelve `List<DTO>`.
   - *Si piden usar una VISTA:* mueve el SQL a
     `CREATE OR REPLACE VIEW v_xxx AS ...` en un `.sql` y deja el DAO con
     `SELECT * FROM v_xxx`.
   - *Si piden un PROCEDIMIENTO:* `cn.prepareCall("{CALL sp_xxx(?)}")` y
     `cs.setInt(1, ...)` (mira `ReporteDAO.entregasPorConductor`).
3. **Controller**: método `generar()` que baja al DAO + métodos de totales
   (`totalKg`, `vehiculoMasProductivo`) con `stream()`.
4. **Vista**: opción en el `switch`, imprime cabecera con `printf`, recorre la
   lista, imprime el pie con los totales del controller. Envuelve en
   `try/catch (SQLException)`.

**Esqueleto del método de vista:**

```java
private void reporteActividad() throws SQLException {
    List<ActividadVehiculoDTO> filas = controller.generar();
    ConsolaUtil.titulo("Actividad por vehiculo");
    if (filas.isEmpty()) { ConsolaUtil.aviso("Sin datos."); return; }
    System.out.printf(" %-8s | %10s%n", "PLACA", "KG");
    for (var a : filas) System.out.printf(" %-8s | %10.2f%n", a.placa(), a.kgEntregados());
    System.out.printf("%n  Total: %.2f kg%n", controller.totalKg(filas));
}
```

---

## Chuleta del esquema (nombres reales de tablas y columnas)

```
conductores(id_conductor, identificacion, nombre_completo, tipo_licencia,
            contacto, estado, fecha_registro)
    estado: 'Activo' | 'De Vacaciones' | 'Inactivo' | 'En Ruta'

vehiculos(id_vehiculo, placa, marca, modelo, anio_fabricacion,
          capacidad_carga_kg, estado)
    estado: 'Disponible' | 'En Ruta' | 'En Mantenimiento'

mantenimientos(id_mantenimiento, id_vehiculo, tipo, descripcion,
               fecha_inicio, fecha_fin, costo, taller)
    tipo: 'Preventivo' | 'Correctivo'   ·   fecha_fin NULL = abierto

asignaciones_conductor_vehiculo(id_asignacion, id_conductor, id_vehiculo,
               fecha_inicio, fecha_fin, activa)
    fecha_fin NULL = asignación vigente

clientes(id_cliente, documento, nombre, telefono, email, direccion, ciudad, creado_en)

paquetes(id_paquete, codigo_seguimiento, descripcion, peso_kg,
         largo_cm, ancho_cm, alto_cm, id_remitente, id_destinatario,
         direccion_origen, ciudad_origen, direccion_destino, ciudad_destino,
         estado, fecha_registro, fecha_entrega)
    estado: 'En Bodega' | 'Asignado a Ruta' | 'En Transito' | 'Entregado' | 'Devuelto'
    id_remitente / id_destinatario -> clientes.id_cliente

rutas(id_ruta, codigo, fecha, id_vehiculo, id_conductor, estado,
      observaciones, creada_en, iniciada_en, finalizada_en, activa)
    estado: 'Planificada' | 'En Curso' | 'Finalizada' | 'Cancelada'

ruta_paquetes(id_ruta, id_paquete, orden_entrega)   -- N:M rutas <-> paquetes

auditoria(id_auditoria, fecha_hora, operacion, entidad, id_entidad, usuario,
          estado_anterior, estado_nuevo, detalle)
```

Vistas y procedimientos ya existentes (en `database/3_schema_reportes_auditoria.sql`):
`v_estado_flota`, `v_desempeno_conductores`, `v_ocupacion_rutas`,
`v_paquetes_por_estado`, `v_actividad_auditoria`,
`sp_entregas_por_conductor`, `sp_historial_rutas_vehiculo`,
`sp_seguimiento_paquete`, `sp_auditoria_por_periodo`.

---

## Errores frecuentes (que cuestan puntos)

- **Concatenar valores en el SQL** en vez de usar `?`. Siempre `PreparedStatement`.
- **No cerrar** `Connection`/`ResultSet` → usa `try-with-resources`.
- `rs.wasNull()` se refiere a la **última** columna leída: consúltalo justo
  después del `getInt/getDouble`, no al final (ver `ReporteDAO.historialRutasVehiculo`).
- Las **etiquetas de estado** en SQL van con acento/espacio exactos:
  `'En Transito'`, `'Asignado a Ruta'`, `'De Vacaciones'`. En Java usa
  `EstadoX.desdeEtiqueta(...)` / `getEtiqueta()`.
- Un `LEFT JOIN` con condición en el `WHERE` sobre la tabla derecha se comporta
  como `INNER JOIN`. Pon la condición en el `ON` o filtra por `IS NULL`.
- `SUM(...)` de un conjunto vacío devuelve `NULL` → envuélvelo en
  `COALESCE(SUM(...), 0)`.
- Fechas: `java.time.LocalDate` ↔ `java.sql.Date.valueOf(fecha)` /
  `rs.getDate(...).toLocalDate()`. `LocalDateTime` ↔ `Timestamp.valueOf(...)`.
