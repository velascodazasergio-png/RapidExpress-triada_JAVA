<!--
Este bloque es TU aporte al README.md del equipo (tarea transversal de la
Persona 1). Cópialo tal cual dentro del README consolidado, entre la sección
"Diseño de la Base de Datos" y la "Guía de Uso" que redacta la Persona 2.
-->

## Instalación y Ejecución

### Requisitos previos

| Herramienta | Versión mínima | Verificación |
|---|---|---|
| JDK | 17 | `java -version` |
| Apache Maven | 3.8 | `mvn -v` |
| Git | 2.30 | `git --version` |
| Cliente `mysql` (o MySQL Workbench) | 8.0 | `mysql --version` |

> La base de datos ya está desplegada en la nube (ver paso 2). No hace falta
> instalar un servidor MySQL local, solo un cliente para ejecutar los scripts.

### 1. Clonar el repositorio

```bash
git clone https://github.com/Mic022/RapidExpress-triada.git
cd RapidExpress-triada
```

### 2. Base de datos en la nube (Railway)

El proyecto corre sobre una instancia **MySQL 8.0 gestionada en [Railway](https://railway.app)**.
La instancia del equipo ya está creada; estos son sus datos de conexión
(la contraseña se comparte por el canal privado del grupo, **no** va en el repo):

| Parámetro | Valor |
|---|---|
| Host | `hayabusa.proxy.rlwy.net` |
| Puerto | `27823` |
| Base de datos | `rapidexpress` |
| Usuario | `root` |
| Contraseña | *(en el canal privado del equipo)* |

> Railway crea por defecto una base llamada `railway`. **El proyecto usa
> `rapidexpress`**, no `railway`: al ejecutar los scripts y al configurar
> `db.url` hay que apuntar siempre a `rapidexpress`.

Para recrear la instancia desde cero (si hiciera falta):

1. En Railway: **New Project → Add MySQL**.
2. Abrir el servicio MySQL → pestaña **Variables**: ahí están `MYSQLHOST`,
   `MYSQLPORT`, `MYSQLUSER` y `MYSQLPASSWORD` internos.
3. Pestaña **Settings → Networking → Public Networking**: genera el host y
   puerto públicos (el `*.proxy.rlwy.net:XXXXX` que se usa desde fuera de Railway).
4. Crear la base del proyecto: `CREATE DATABASE rapidexpress;`

### 3. Ejecutar los scripts SQL

Desde la raíz del proyecto, con el cliente `mysql` (nótese el flag `-P` con el
puerto de Railway y el nombre de la base al final):

```bash
mysql -h hayabusa.proxy.rlwy.net -P 27823 -u root -p rapidexpress < database/1_schema_ddl.sql
mysql -h hayabusa.proxy.rlwy.net -P 27823 -u root -p rapidexpress < database/2_data_dml.sql
```

`1_schema_ddl.sql` crea todas las tablas y restricciones dentro de `rapidexpress`.
`2_data_dml.sql` la puebla con datos de prueba (mínimo 20 registros por entidad
principal). También pueden ejecutarse desde MySQL Workbench abriendo cada
archivo y presionando *Execute* (con la conexión apuntando a `rapidexpress`).

Verificación rápida:

```sql
USE rapidexpress;
SHOW TABLES;
SELECT COUNT(*) FROM vehiculos;
```

### 4. Configurar las credenciales de conexión

Las credenciales no están en el código fuente. Copie la plantilla y complétela:

```bash
cp src/main/resources/database.properties.example src/main/resources/database.properties
```

Contenido de `src/main/resources/database.properties` (reemplace `<contraseña>`
por la del canal privado del equipo):

```properties
db.url=jdbc:mysql://hayabusa.proxy.rlwy.net:27823/rapidexpress?useSSL=true&serverTimezone=America/Bogota&allowPublicKeyRetrieval=true
db.user=root
db.password=<contraseña>
```

> `database.properties` está incluido en `.gitignore`, por lo que nunca se
> sube al repositorio.

### 5. Compilar y ejecutar

```bash
mvn clean package
java -jar target/rapidexpress-management-system-1.0.0.jar
```

Alternativa sin empaquetar:

```bash
mvn compile exec:java -Dexec.mainClass="com.rapidexpress.Main"
```

Si la conexión es correcta, la aplicación muestra el menú principal en la
terminal. Si aparece un error de conexión, revise en orden: el host y el
puerto `27823` en `db.url`, que `database.properties` exista, y que la base
`rapidexpress` (no `railway`) tenga las tablas cargadas.

### Solución de problemas frecuentes

| Mensaje | Causa probable | Solución |
|---|---|---|
| `No se encontro database.properties` | No se copió la plantilla | Repetir el paso 4 |
| `Communications link failure` | Host o puerto erróneos, o sin red | Verificar `hayabusa.proxy.rlwy.net:27823` y la conexión a internet |
| `Access denied for user` | Contraseña incorrecta | Pedir la contraseña vigente en el canal del equipo (Railway la rota si se recrea la instancia) |
| `Unknown database 'rapidexpress'` | Se apuntó a `railway` o no se ejecutó el DDL | `CREATE DATABASE rapidexpress;` y repetir el paso 3 |
| `Table '...' doesn't exist` | El DDL se cargó en `railway` en vez de `rapidexpress` | Volver a ejecutar el paso 3 con `rapidexpress` al final del comando |
| `Public Key Retrieval is not allowed` | Falta un parámetro en la URL | Agregar `allowPublicKeyRetrieval=true` |
