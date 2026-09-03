# Módulo 2 — Paquetes y Rutas (Nikolas) · Notas de integración

Complemento de `INTEGRACION.md` de la Persona 1. Explica cómo quedó conectado
el módulo de rutas con el de flota y qué falta para el merge final.

---

## 0. Estado tras la integración de módulos 1 + 2

- Los módulos 1 y 2 ya están fusionados en la rama `DevMic`. Todo compila con
  `javac`/`mvn` (JDK 17).
- **`iniciarRuta` / `finalizarRuta` ahora son una sola transacción ACID.** Se
  implementó lo que pedía la sección 5: `FlotaService` expone
  `iniciarOperacion(Connection, int)` y `finalizarOperacion(Connection, int)`, y
  `RutaService` las llama sobre su propia conexión. Se eliminó la lógica de
  compensación (`compensar` / `revertirCierre`).
- `crearHojaDeRuta` también entró completa en una transacción: bloquea el
  vehículo (`FlotaController.buscarVehiculoBloqueando`) y valida capacidad,
  estado y conductor dentro de ella. Ya no se pueden crear rutas "muertas".
- Orden de bloqueo unificado en todo el sistema: **ruta → vehículo → conductor
  → paquetes (id ascendente)**. Se ajustó `ConductorService.asignarConductorAVehiculo`
  para respetarlo.
- Base de datos: se generaron `database/1_schema_ddl.sql` y
  `database/2_data_dml.sql` **consolidados** (son los que nombra el README).
  Los parciales `*_persona1.sql` / `*_persona2.sql` se conservan como respaldo.
- Cambio de esquema en `rutas`: los `UNIQUE(id_vehiculo, fecha)` /
  `UNIQUE(id_conductor, fecha)` se reemplazaron por un índice parcial con una
  columna generada `activa` (igual que `asignaciones_conductor_vehiculo`), para
  que una ruta Cancelada o Finalizada libere la pareja vehículo/fecha.
- `EstadoPaquete`: se quitó la transición `En Bodega → Devuelto`. Y el cambio
  de estado manual (`PaqueteService.cambiarEstado`) rechaza paquetes que
  pertenezcan a una ruta activa.
- `pom.xml` → `<main.class>` apunta a `MainRutasPruebas` hasta que llegue el
  `Main` de la Persona 3.

---

## 1. Qué se respetó del contrato de la Persona 1

| Regla acordada | Cómo se cumple |
|---|---|
| Nadie fuera del módulo de flota toca sus DAO | `RutaService` solo usa `FlotaController` y `ConductorController` |
| Persona 2 nunca hace `UPDATE vehiculos SET estado = 'En Ruta'` | Se llama a `iniciarOperacion(idVehiculo)` / `finalizarOperacion(idVehiculo)` |
| `rutas` referencia `vehiculos(id_vehiculo)` y `conductores(id_conductor)` | Así quedaron las FK en `1_schema_ddl_persona2.sql` |
| Un solo `Scanner` en toda la app | Las dos vistas nuevas usan `ConsolaUtil` |
| Una sola `ConexionBD` | Se usa la de la Persona 1; la del módulo 2 se eliminó |
| Una sola clase de excepción de negocio | Se usa `NegocioException`; la del módulo 2 se eliminó |

Cambio de diseño que salió de leer su código: **el conductor de la hoja de ruta
ya no se escribe a mano**. Se toma de la asignación vigente del vehículo
(`obtenerConductorAsignadoA`) y se guarda en `rutas.id_conductor` para conservar
el histórico. Por eso el menú pide solo el vehículo, y solo muestra los que
están Disponibles **y** tienen conductor asignado activo.

---

## 2. Archivos nuevos

**Base de datos** (`database/`)
- `1_schema_ddl_persona2.sql` — `clientes`, `paquetes`, `rutas`, `ruta_paquetes`
- `2_data_dml_persona2.sql` — 26 clientes, 40 paquetes, 20 rutas, 34 líneas de ruta
- `9_pruebas_concurrencia.sql` — guion de la prueba con dos terminales + 3 consultas de consistencia

**Java** (`src/main/java/com/rapidexpress/`)
- `model/` — `Cliente`, `Paquete`, `Ruta`, `EstadoPaquete`, `EstadoRuta`
- `dao/` — `ClienteDAO`, `PaqueteDAO`, `RutaDAO`
- `service/` — `PaqueteService`, `RutaService`
- `controller/` — `PaqueteController`, `RutaController`
- `view/` — `PaqueteMenuView`, `RutaMenuView`
- `util/TransaccionUtil` — transacción con rollback y reintento ante deadlock
- `integracion/` — `AuditoriaGateway`, `AuditoriaConsola`, `Auditoria` (para la Persona 3)
- `MainRutasPruebas.java` — **temporal**, se borra en el merge final

---

## 3. Orden de ejecución de los scripts

```
1_schema_ddl_persona1.sql
2_data_dml_persona1.sql
1_schema_ddl_persona2.sql
2_data_dml_persona2.sql
```

El DML de la Persona 2 usa parejas vehículo–conductor que ya tienen asignación
vigente en los datos de la Persona 1 (vehículos 1, 2, 3, 4, 6, 7, 8, 9, 10, 11,
12 y 14). Si ella cambia su DML, hay que revisar esas parejas.

---

## 4. Lo que falta para la Persona 3

1. Implementar `AuditoriaGateway` con su `AuditoriaService` y agregar **una
   línea** al inicio de su `Main`, antes de mostrar cualquier menú:

   ```java
   Auditoria.usar(new AuditoriaService());
   ```

   Eventos que ya se emiten: `CREAR_PAQUETE`, `EDITAR_PAQUETE`,
   `CAMBIO_ESTADO_PAQUETE`, `CREAR_RUTA`, `INICIAR_RUTA`, `ENTREGAR_PAQUETE`,
   `DEVOLVER_PAQUETE`, `FINALIZAR_RUTA`, `CANCELAR_RUTA`.

2. Colgar los menús del principal:

   ```java
   case 3 -> new PaqueteMenuView().mostrar();
   case 4 -> new RutaMenuView().mostrar();
   ```

3. Sus dos reportes salen con SQL directo, sin tocar código Java:

   ```sql
   -- Entregas de un conductor en un rango de fechas
   SELECT c.nombre_completo, r.codigo, p.codigo_seguimiento, p.fecha_entrega
   FROM rutas r
   JOIN conductores c    ON c.id_conductor = r.id_conductor
   JOIN ruta_paquetes rp ON rp.id_ruta = r.id_ruta
   JOIN paquetes p       ON p.id_paquete = rp.id_paquete
   WHERE r.id_conductor = ? AND p.estado = 'Entregado'
     AND p.fecha_entrega BETWEEN ? AND ?
   ORDER BY p.fecha_entrega;

   -- Historial de rutas de un vehículo
   SELECT r.codigo, r.fecha, r.estado,
          COUNT(rp.id_paquete) AS paquetes,
          ROUND(SUM(p.peso_kg), 2) AS peso_total
   FROM rutas r
   LEFT JOIN ruta_paquetes rp ON rp.id_ruta = r.id_ruta
   LEFT JOIN paquetes p       ON p.id_paquete = rp.id_paquete
   WHERE r.id_vehiculo = ?
   GROUP BY r.id_ruta, r.codigo, r.fecha, r.estado
   ORDER BY r.fecha DESC;
   ```

4. En el merge final: borrar `MainFlotaPruebas.java` y `MainRutasPruebas.java`,
   y apuntar `<main.class>` del `pom.xml` a su `Main`.

---

## 5. MEJORA YA APLICADA — transacción única entre módulos

*(Antes esto era "pendiente / hablar con Michael". Se implementó en la
integración de módulos 1 + 2.)*

`FlotaService` ahora tiene dos versiones de cada operación:

```java
// Abren conexión, commit y cierre (uso normal desde el menú de flota)
public int  iniciarOperacion(int idVehiculo)  throws NegocioException;
public void finalizarOperacion(int idVehiculo) throws NegocioException;

// Operan sobre la conexión recibida, SIN commit ni cierre (uso desde rutas)
public int  iniciarOperacion(Connection cn, int idVehiculo)  throws NegocioException, SQLException;
public void finalizarOperacion(Connection cn, int idVehiculo) throws NegocioException, SQLException;
```

`RutaService.iniciarRuta` y `finalizarRuta` usan las versiones con `Connection`
dentro de su `TransaccionUtil.ejecutar(...)`. Flota + paquetes + ruta son una
sola unidad ACID: si algo falla, un único `ROLLBACK` deja todo como estaba y ya
no existe la lógica de compensación.

---

## 6. Checklist de pruebas cruzadas

- [ ] Crear vehículo y conductor desde el menú de flota, asignarlos entre sí.
- [ ] Crear hoja de ruta con ese vehículo → debe mostrar el conductor solo.
- [ ] Intentar ruta con vehículo En Mantenimiento → rechazada.
- [ ] Intentar ruta con vehículo sin conductor asignado → rechazada con mensaje claro.
- [ ] Sobrepasar la capacidad de carga → rechazada y **sin** crear la ruta.
- [ ] Iniciar ruta → verificar en el menú de flota que el vehículo quedó En Ruta.
- [ ] Marcar todas las entregas y finalizar → vehículo Disponible, conductor Activo.
- [ ] Intentar finalizar con paquetes pendientes → rechazada.
- [ ] Correr las 3 consultas de consistencia de `9_pruebas_concurrencia.sql` → 0 filas.

---

## 7. Git

```bash
git checkout develop && git pull origin develop
git checkout -b feature/paquetes-rutas

git add database/1_schema_ddl_persona2.sql database/2_data_dml_persona2.sql
git commit -m "feat(db): add DDL and seed data for packages and routes"

git add src/main/java/com/rapidexpress/model
git commit -m "feat(routes): add Package, Client and Route domain models"

git add src/main/java/com/rapidexpress/dao src/main/java/com/rapidexpress/util
git commit -m "feat(routes): implement JDBC DAOs and transactional helper"

git add src/main/java/com/rapidexpress/service src/main/java/com/rapidexpress/integracion
git commit -m "feat(routes): add capacity validation and concurrency control"

git add src/main/java/com/rapidexpress/controller src/main/java/com/rapidexpress/view
git commit -m "feat(routes): add CLI menus for packages and route planning"

git push -u origin feature/paquetes-rutas
```

Antes de pedir el merge: `git merge develop` **en tu rama** y resolver ahí los
conflictos, para que `develop` nunca se rompa.

---

## 8. Para la sustentación: la parte de concurrencia

El requisito era mantener consistencia ante consultas concurrentes desde la
terminal. La respuesta tiene cuatro capas:

1. **Transacción por operación** con `autocommit = false` y `READ COMMITTED`;
   si algo falla, rollback y no queda nada a medias (`TransaccionUtil`).
2. **Bloqueo pesimista**: `SELECT ... FOR UPDATE` sobre la ruta y cada paquete.
3. **Orden fijo de bloqueo**: ruta primero, luego los paquetes por id
   ascendente. Eso es lo que evita los deadlocks entre dos terminales.
4. **Escritura condicional**: los `UPDATE` llevan `AND estado = <esperado>` y se
   verifica cuántas filas cambiaron; si es 0, otro ganó la carrera y se aborta.

Más las restricciones de la BD como última red: `codigo_seguimiento` único, y
un vehículo/conductor por fecha en `rutas`. Y ante deadlock real de MySQL
(error 1213), `TransaccionUtil` reintenta hasta 3 veces antes de rendirse.
