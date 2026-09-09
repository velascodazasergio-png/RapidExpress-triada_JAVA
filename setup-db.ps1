<#
=====================================================================
 RapidExpress - prepara la base de datos y las credenciales
=====================================================================
 Levanta MySQL 8 en un contenedor Docker, ejecuta los scripts de
 database\ (esquema + reportes/auditoria + datos) y escribe
 src\main\resources\database.properties apuntando a ese MySQL.

 Uso:
   .\setup-db.ps1            Crea el contenedor y carga todo.
   .\setup-db.ps1 -Reset     Borra el contenedor y empieza de cero.
   .\setup-db.ps1 -Stop      Solo detiene el contenedor (los datos quedan).

 Requisito: Docker Desktop instalado y EN EJECUCION.
 Si prefieres un MySQL nativo o en la nube, ver docs/EXAMEN_GUIA.md
 o el README (seccion "Instalacion y ejecucion").
=====================================================================
#>
param(
    [switch]$Reset,
    [switch]$Stop
)

$ErrorActionPreference = 'Stop'
Set-Location -Path $PSScriptRoot

$Contenedor = 'rapidexpress-mysql'
$RootPass   = 'rapidexpress'
$Puerto     = 3306
$Imagen     = 'mysql:8.0'

function Fallo($msg) { Write-Host "ERROR: $msg" -ForegroundColor Red; exit 1 }

# --- Docker disponible? ------------------------------------------------
if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
    Fallo "Docker no esta instalado. Instala Docker Desktop o usa un MySQL nativo (ver README)."
}
docker info *> $null
if ($LASTEXITCODE -ne 0) {
    Fallo "Docker Desktop no esta en ejecucion. Abrelo, espera a que diga 'Engine running' y reintenta."
}

# --- Stop -----------------------------------------------------------
if ($Stop) {
    docker stop $Contenedor *> $null
    Write-Host "Contenedor detenido. Vuelve a arrancarlo con:  docker start $Contenedor" -ForegroundColor Yellow
    exit 0
}

# --- Reset --------------------------------------------------------
if ($Reset) {
    Write-Host "==> Eliminando contenedor anterior ..." -ForegroundColor Cyan
    docker rm -f $Contenedor *> $null
}

# --- Crear / arrancar el contenedor ---------------------------------
$existe = (docker ps -a --filter "name=^/$Contenedor$" --format '{{.Names}}')
if ($existe -eq $Contenedor) {
    Write-Host "==> El contenedor ya existe, arrancandolo ..." -ForegroundColor Cyan
    docker start $Contenedor *> $null
} else {
    Write-Host "==> Creando contenedor MySQL 8 ($Contenedor) ..." -ForegroundColor Cyan
    docker run --name $Contenedor `
        -e "MYSQL_ROOT_PASSWORD=$RootPass" `
        -e "MYSQL_DATABASE=rapidexpress" `
        -p "${Puerto}:3306" `
        -d $Imagen *> $null
    if ($LASTEXITCODE -ne 0) { Fallo "No se pudo crear el contenedor (puerto $Puerto ocupado?)." }
}

# --- Esperar a que MySQL acepte conexiones -------------------------
Write-Host "==> Esperando a que MySQL este listo ..." -ForegroundColor Cyan
$listo = $false
foreach ($i in 1..60) {
    docker exec $Contenedor mysqladmin ping -uroot "-p$RootPass" --silent *> $null
    if ($LASTEXITCODE -eq 0) { $listo = $true; break }
    Start-Sleep -Seconds 2
}
if (-not $listo) { Fallo "MySQL no respondio a tiempo. Revisa 'docker logs $Contenedor'." }

# --- Cargar los scripts SQL en orden ------------------------------
$scripts = @(
    'database\1_schema_ddl.sql',
    'database\3_schema_reportes_auditoria.sql',
    'database\2_data_dml.sql'
)
foreach ($s in $scripts) {
    if (-not (Test-Path $s)) { Fallo "No se encuentra $s" }
    $base = Split-Path $s -Leaf
    Write-Host "==> Ejecutando $s ..." -ForegroundColor Cyan
    # Se copia el archivo al contenedor y se ejecuta alli, para no depender
    # de la codificacion del pipe de PowerShell (rompe acentos en los datos).
    docker cp $s "${Contenedor}:/tmp/$base"
    if ($LASTEXITCODE -ne 0) { Fallo "No se pudo copiar $s al contenedor" }
    docker exec $Contenedor sh -c "mysql -uroot -p$RootPass --default-character-set=utf8mb4 < /tmp/$base"
    if ($LASTEXITCODE -ne 0) { Fallo "Fallo al ejecutar $s" }
}

# --- Escribir database.properties --------------------------------
$props = 'src\main\resources\database.properties'
@(
    "db.url=jdbc:mysql://localhost:$Puerto/rapidexpress?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Bogota&characterEncoding=UTF-8"
    "db.user=root"
    "db.password=$RootPass"
) | Set-Content -Path $props -Encoding ASCII
Write-Host "==> Escrito $props" -ForegroundColor Green

# --- Comprobacion final -----------------------------------------
$conteo = (docker exec $Contenedor mysql -uroot "-p$RootPass" -N -B -e `
    "SELECT CONCAT(t.n,'=',IFNULL(c.c,0)) FROM (SELECT 'vehiculos' n UNION SELECT 'conductores' UNION SELECT 'paquetes' UNION SELECT 'rutas' UNION SELECT 'auditoria') t LEFT JOIN (SELECT 'vehiculos' n, COUNT(*) c FROM rapidexpress.vehiculos UNION ALL SELECT 'conductores', COUNT(*) FROM rapidexpress.conductores UNION ALL SELECT 'paquetes', COUNT(*) FROM rapidexpress.paquetes UNION ALL SELECT 'rutas', COUNT(*) FROM rapidexpress.rutas UNION ALL SELECT 'auditoria', COUNT(*) FROM rapidexpress.auditoria) c ON c.n=t.n" 2>$null)

Write-Host ""
Write-Host "=====================================================================" -ForegroundColor Green
Write-Host " Base de datos lista. Filas cargadas:" -ForegroundColor Green
$conteo | ForEach-Object { Write-Host "   $_" }
Write-Host "=====================================================================" -ForegroundColor Green
Write-Host ""
Write-Host " Ahora ejecuta la aplicacion:" -ForegroundColor Cyan
Write-Host "   .\run.ps1" -ForegroundColor White
Write-Host ""
Write-Host " Para apagar el MySQL luego:  docker stop $Contenedor" -ForegroundColor DarkGray
Write-Host " Para volver a encenderlo:    docker start $Contenedor" -ForegroundColor DarkGray
