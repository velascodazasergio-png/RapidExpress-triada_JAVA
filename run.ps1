<#
=====================================================================
 RapidExpress - compila y ejecuta el sistema SIN necesidad de Maven
=====================================================================
 Requisitos: solo un JDK 17 o superior (javac y java en el PATH).

 Uso:
   .\run.ps1              Compila y arranca la aplicacion (Main).
   .\run.ps1 -Package     Ademas genera target\rapidexpress.jar ejecutable.
   .\run.ps1 -Clean       Borra target\ y vuelve a compilar desde cero.
   .\run.ps1 -SkipBuild   Ejecuta lo ya compilado sin recompilar.

 La primera vez descarga MySQL Connector/J 8.4.0 a la carpeta lib\.
 Necesita database.properties en src\main\resources\ (vea el .example).
=====================================================================
#>
param(
    [switch]$Package,
    [switch]$Clean,
    [switch]$SkipBuild
)

$ErrorActionPreference = 'Stop'
Set-Location -Path $PSScriptRoot

$MainClass       = 'com.rapidexpress.Main'
$ConnectorVer    = '8.4.0'
$ConnectorJar    = "lib\mysql-connector-j-$ConnectorVer.jar"
$ConnectorUrl    = "https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/$ConnectorVer/mysql-connector-j-$ConnectorVer.jar"
$SrcDir          = 'src\main\java'
$ResDir          = 'src\main\resources'
$OutDir          = 'target\classes'

# Resuelve el JDK a partir de java.home (asi 'jar' funciona aunque el PATH
# solo tenga el shim de Oracle, que no incluye jar.exe).
if (-not (Get-Command java -ErrorAction SilentlyContinue)) {
    throw "No se encontro 'java' en el PATH. Instale un JDK 17+ (https://adoptium.net)."
}
$javaHome = $env:JAVA_HOME
if (-not $javaHome -or -not (Test-Path (Join-Path $javaHome 'bin\javac.exe'))) {
    # cmd /c fusiona stderr sin romper PowerShell; java escribe estas propiedades por stderr.
    foreach ($line in (cmd /c "java -XshowSettings:properties -version 2>&1")) {
        if ($line -match 'java\.home\s*=\s*(.+)$') { $javaHome = $Matches[1].Trim() }
    }
}
if (-not $javaHome -or -not (Test-Path (Join-Path $javaHome 'bin\javac.exe'))) {
    throw "No se pudo localizar un JDK completo (con javac/jar). Instale un JDK 17+ y/o defina JAVA_HOME."
}
$JAVAC = Join-Path $javaHome 'bin\javac.exe'
$JAVA  = Join-Path $javaHome 'bin\java.exe'
$JAR   = Join-Path $javaHome 'bin\jar.exe'
Write-Host "==> JDK: $javaHome" -ForegroundColor DarkGray

# --- 0. Limpieza opcional -------------------------------------------------
if ($Clean -and (Test-Path 'target')) {
    Write-Host '==> Limpiando target\ ...' -ForegroundColor Cyan
    Remove-Item 'target' -Recurse -Force
}

# --- 1. Dependencia: MySQL Connector/J ----------------------------------
if (-not (Test-Path $ConnectorJar)) {
    Write-Host "==> Descargando MySQL Connector/J $ConnectorVer ..." -ForegroundColor Cyan
    New-Item -ItemType Directory -Force -Path 'lib' | Out-Null
    try {
        Invoke-WebRequest -Uri $ConnectorUrl -OutFile $ConnectorJar -UseBasicParsing
    } catch {
        throw "No se pudo descargar el conector desde Maven Central. Descarguelo manualmente y coloquelo en $ConnectorJar`n$($_.Exception.Message)"
    }
}
Write-Host "==> Conector: $ConnectorJar" -ForegroundColor DarkGray

# --- 2. Aviso si faltan las credenciales -------------------------------
$propsFile = Join-Path $ResDir 'database.properties'
if (-not (Test-Path $propsFile)) {
    Write-Warning "No existe $propsFile."
    Write-Warning "Copie $ResDir\database.properties.example a database.properties y complete sus credenciales antes de usar la BD."
}

# --- 3. Compilacion ----------------------------------------------------
if (-not $SkipBuild) {
    Write-Host '==> Compilando fuentes Java ...' -ForegroundColor Cyan
    if (Test-Path $OutDir) { Remove-Item $OutDir -Recurse -Force }
    New-Item -ItemType Directory -Force -Path $OutDir | Out-Null

    $sources = Get-ChildItem -Path $SrcDir -Recurse -Filter *.java | ForEach-Object { $_.FullName }
    if (-not $sources) { throw "No se encontraron archivos .java en $SrcDir" }

    $argfile = Join-Path $env:TEMP "rapidexpress_sources_$PID.txt"
    # WriteAllLines => UTF-8 sin BOM; un BOM rompe el primer nombre del @argfile de javac.
    [System.IO.File]::WriteAllLines($argfile, [string[]]$sources)
    try {
        & $JAVAC -encoding UTF-8 -d $OutDir -cp $ConnectorJar "@$argfile"
        if ($LASTEXITCODE -ne 0) { throw "La compilacion fallo (javac devolvio $LASTEXITCODE)." }
    } finally {
        Remove-Item $argfile -ErrorAction SilentlyContinue
    }

    # Recursos (database.properties, etc.) al classpath de salida
    if (Test-Path $ResDir) {
        Copy-Item -Path (Join-Path $ResDir '*') -Destination $OutDir -Recurse -Force -ErrorAction SilentlyContinue
    }
    Write-Host '==> Compilacion OK.' -ForegroundColor Green
}

if (-not (Test-Path $OutDir)) {
    throw "No hay clases compiladas en $OutDir. Ejecute sin -SkipBuild."
}

# --- 4. Empaquetado opcional -----------------------------------------
if ($Package) {
    Write-Host '==> Empaquetando target\rapidexpress.jar ...' -ForegroundColor Cyan
    # El conector va a target\lib\ para que el jar sea autocontenido bajo target\.
    # Class-Path del manifiesto se resuelve relativo a la carpeta del jar (target\).
    New-Item -ItemType Directory -Force -Path 'target\lib' | Out-Null
    Copy-Item $ConnectorJar 'target\lib\' -Force
    $manifest = Join-Path $env:TEMP "rapidexpress_manifest_$PID.mf"
    @(
        "Main-Class: $MainClass"
        "Class-Path: lib/mysql-connector-j-$ConnectorVer.jar ."
        ''
    ) | Set-Content -Path $manifest -Encoding ASCII
    try {
        & $JAR --create --file target\rapidexpress.jar --manifest $manifest -C $OutDir .
        if ($LASTEXITCODE -ne 0) { throw "jar devolvio $LASTEXITCODE." }
    } finally {
        Remove-Item $manifest -ErrorAction SilentlyContinue
    }
    Write-Host '==> Jar listo: target\rapidexpress.jar' -ForegroundColor Green
    Write-Host '    Ejecutar:  java -jar target\rapidexpress.jar' -ForegroundColor DarkGray
    Write-Host '    (usa target\lib\; las credenciales van incluidas si ya existia database.properties al empaquetar,' -ForegroundColor DarkGray
    Write-Host '     o se pueden dejar en un database.properties dentro de target\)' -ForegroundColor DarkGray
}

# --- 5. Ejecucion ----------------------------------------------------
Write-Host "==> Iniciando $MainClass ...`n" -ForegroundColor Cyan
& $JAVA -cp "$OutDir;$ConnectorJar" $MainClass
exit $LASTEXITCODE
