# Guía de integración — Módulo Persona 1 (Flota + Personal)

Documento para ti y para tus dos compañeros. Explica **qué expone tu módulo**,
**qué necesita de los otros** y **en qué orden se hace el merge**.

---

## 1. Por qué tu módulo se integra fácil

Tu código respeta tres reglas que evitan el 90 % de los conflictos:

1. **Nadie fuera de tu módulo toca tus DAO.** Persona 2 y 3 solo llaman a
   `FlotaController` y `ConductorController`.
2. **Tus clases viven en paquetes propios** (`model`, `dao`, `service`,
   `controller`, `view`) con nombres que nadie más va a usar.
3. **Solo hay tres archivos compartidos** con los demás:
   `ConexionBD.java`, `NegocioException.java` y `ConsolaUtil.java`.
   Estos se acuerdan **antes** de empezar y se fusionan una sola vez.

---

## 2. Contrato público: lo que los otros módulos pueden llamar

Pásales esta tabla a tus compañeros. Es lo único que necesitan saber de ti.

### Para Persona 2 (Rutas) — `FlotaController`

| Método | Para qué lo usa | Devuelve |
|---|---|---|
| `obtenerVehiculosDisponibles()` | Mostrar vehículos elegibles al armar la hoja de ruta | `List<Vehiculo>` |
| `buscarVehiculo(int id)` | Leer `getCapacidadCargaKg()` y validar que la carga no se exceda | `Vehiculo` o `null` |
| `iniciarOperacion(int idVehiculo)` | Al iniciar la ruta: pone vehículo **y** conductor en `En Ruta` en una sola transacción | `int` id del conductor |
| `finalizarOperacion(int idVehiculo)` | Al cerrar la ruta: devuelve vehículo a `Disponible` y conductor a `Activo` | `void` |

### Para Persona 2 (Rutas) — `ConductorController`

| Método | Para qué lo usa | Devuelve |
|---|---|---|
| `obtenerConductorAsignadoA(int idVehiculo)` | Saber qué conductor va en la hoja de ruta | `Conductor` o `null` |
| `listarAsignacionesVigentes()` | Listar parejas conductor–vehículo listas para operar | `List<Asignacion>` |

**Regla de oro que debes dejar clara:** Persona 2 **nunca** hace
`UPDATE vehiculos SET estado = 'En Ruta'` por su cuenta. Llama a
`iniciarOperacion()`. Si cada módulo cambia estados por su lado, la regla
"un conductor no puede estar en dos vehículos" se rompe y no habrá forma
de saber quién la rompió.

Todos estos métodos lanzan `NegocioException` con un mensaje ya redactado
para el usuario. Persona 2 solo tiene que capturarla y mostrar el mensaje.

### Para Persona 3 (Reportes)

Persona 3 hace consultas SQL directas contra tus tablas (es lo normal en
reportes). Solo necesita saber los nombres reales de columnas:

- `vehiculos(id_vehiculo, placa, marca, modelo, anio_fabricacion, capacidad_carga_kg, estado)`
- `conductores(id_conductor, identificacion, nombre_completo, tipo_licencia, contacto, estado)`
- `mantenimientos(id_mantenimiento, id_vehiculo, tipo, descripcion, fecha_inicio, fecha_fin, costo, taller)`
- `asignaciones_conductor_vehiculo(id_asignacion, id_conductor, id_vehiculo, fecha_inicio, fecha_fin, activa)`

Para el reporte de "historial de rutas de un vehículo" y el de "entregas
por conductor en un rango de fechas", Persona 3 hace `JOIN` entre `rutas`
(de Persona 2) y tus tablas. No necesita tocar tu código Java.

---

## 3. Lo que tú necesitas de ellos

| Le pides a | Qué | Por qué |
|---|---|---|
| Persona 3 | Que el `Main.java` definitivo llame a `new FlotaMenuView().mostrar()` y `new ConductorMenuView().mostrar()` | Es todo lo que tiene que hacer para montar tus menús |
| Persona 3 | Que en el DDL consolidado tus tablas queden **antes** que `rutas` | `rutas` tiene FK hacia `vehiculos` y `conductores` |
| Persona 2 | Que su tabla `rutas` referencie `vehiculos(id_vehiculo)` y `conductores(id_conductor)`, no la placa ni la cédula | Las claves foráneas deben apuntar a la PK |
| Los dos | Que usen `ConsolaUtil.scanner()` y no creen su propio `Scanner` | Dos `Scanner` sobre `System.in` hacen que la CLI se salte lecturas |

Ese último punto es el bug clásico de los proyectos CLI en equipo.
M�ndalo por el grupo antes de que cada uno escriba su vista.

---

## 4. Orden de trabajo con Git

```bash
# 1. Partir de develop actualizado
git checkout develop
git pull origin develop

# 2. Tu rama
git checkout -b feature/gestion-flota-personal

# 3. Commits en inglés y descriptivos (mínimo 3-4 tuyos)
git add src/main/java/com/rapidexpress/model
git commit -m "feat(fleet): add Vehicle, Driver and Maintenance domain models"

git add src/main/java/com/rapidexpress/dao
git commit -m "feat(fleet): implement JDBC DAOs for vehicles, drivers and maintenance"

git add src/main/java/com/rapidexpress/service
git commit -m "feat(fleet): add business rules for vehicle status and driver assignment"

git add src/main/java/com/rapidexpress/controller src/main/java/com/rapidexpress/view
git commit -m "feat(fleet): add CLI menus and controllers for fleet and staff modules"

git add database/
git commit -m "feat(db): add DDL and seed data for fleet and staff tables"

# 4. Subir
git push -u origin feature/gestion-flota-personal
```

Antes de pedir el merge:

```bash
git checkout develop
git pull origin develop
git checkout feature/gestion-flota-personal
git merge develop        # resuelves conflictos TÚ, en tu rama
```

Así `develop` nunca se rompe y quien hace el merge final (Persona 3) recibe
tu rama limpia.

---

## 5. Secuencia recomendada de integración

1. **Semana 1 — acuerdo previo.** Alguien crea el repo con `.gitignore`,
   `pom.xml` y las tres clases compartidas (`ConexionBD`,
   `NegocioException`, `ConsolaUtil`) y las sube a `develop`.
   Si ya las subiste tú, los otros dos las borran de sus copias y usan las tuyas.
2. **Semana 2 — base de datos.** Tú entregas tu DDL a Persona 3, ella lo
   consolida con el de Persona 2 y ejecuta el script completo en la nube.
   A partir de ahí los tres trabajan contra la misma BD.
3. **Semana 3 — desarrollo en paralelo.** Cada uno en su rama.
   Tú terminas primero porque no dependes de nadie: aprovéchalo para
   entregarle temprano a Persona 2 el contrato de la sección 2.
4. **Semana 4 — merges.** Orden: tu rama primero (nadie depende de las otras
   para compilar), luego Persona 2, luego Persona 3 con el `Main` definitivo.

---

## 6. Checklist antes del merge final

- [ ] Borrar `MainFlotaPruebas.java` (el `Main` oficial es el de Persona 3).
- [ ] En `pom.xml`, cambiar `<main.class>` al `Main` de Persona 3.
- [ ] Verificar que solo exista **una** copia de `ConexionBD.java`.
- [ ] Verificar que ninguna vista cree su propio `new Scanner(System.in)`.
- [ ] Probar el flujo cruzado completo:
      registras vehículo → registras conductor → los asignas →
      Persona 2 crea la hoja de ruta e inicia la ruta →
      confirmas que tu menú muestra el vehículo en `En Ruta` →
      Persona 2 cierra la ruta → confirmas que volvió a `Disponible`.
- [ ] Confirmar que Persona 3 ve tus operaciones en el log de auditoría
      (si decide auditar también los cambios de estado de flota, te pedirá
      llamar a su `AuditoriaService` desde `FlotaService`; es una línea).

---

## 7. Decisiones de diseño que debes poder defender

Si en la sustentación te preguntan, estas son las tres respuestas que valen puntos:

**¿Por qué `asignaciones_conductor_vehiculo` es una tabla y no un campo en `vehiculos`?**
Porque con una tabla histórica puedes responder "quién manejó este camión en
marzo", que es justo lo que necesita el módulo de reportes. Un campo
`id_conductor` en `vehiculos` solo guarda el presente y pisa el pasado.

**¿Cómo garantizas que un conductor no esté en dos vehículos a la vez?**
En dos capas. En la BD, con una columna generada `activa` que vale 1 mientras
`fecha_fin` sea `NULL` y `NULL` cuando la asignación se cierra; como MySQL
ignora los `NULL` en los índices `UNIQUE`, la restricción
`UNIQUE(id_conductor, activa)` solo aplica sobre las asignaciones vigentes.
Y en la capa de servicio, con una transacción que bloquea ambas filas
(`SELECT ... FOR UPDATE`) antes de insertar.

**¿Por qué `En Ruta` también es estado del conductor si el enunciado solo lista
Activo / De Vacaciones / Inactivo?**
Porque el módulo 4 exige que al iniciar una ruta el conductor pase a `En Ruta`.
Se agregó al `ENUM` y solo lo asigna el sistema: el menú no deja ponerlo a mano.
