#!/usr/bin/env bash
# =====================================================================
#  RapidExpress - compila y ejecuta el sistema SIN necesidad de Maven
# =====================================================================
#  Requisitos: solo un JDK 17 o superior (javac y java en el PATH).
#
#  Uso:
#    ./run.sh              Compila y arranca la aplicacion (Main).
#    ./run.sh --package    Ademas genera target/rapidexpress.jar ejecutable.
#    ./run.sh --clean      Borra target/ y recompila desde cero.
#    ./run.sh --skip-build Ejecuta lo ya compilado sin recompilar.
# =====================================================================
set -euo pipefail
cd "$(dirname "$0")"

MAIN_CLASS='com.rapidexpress.Main'
CONNECTOR_VER='8.4.0'
CONNECTOR_JAR="lib/mysql-connector-j-${CONNECTOR_VER}.jar"
CONNECTOR_URL="https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/${CONNECTOR_VER}/mysql-connector-j-${CONNECTOR_VER}.jar"
SRC_DIR='src/main/java'
RES_DIR='src/main/resources'
OUT_DIR='target/classes'

PACKAGE=0 ; CLEAN=0 ; SKIP_BUILD=0
for arg in "$@"; do
  case "$arg" in
    --package)    PACKAGE=1 ;;
    --clean)      CLEAN=1 ;;
    --skip-build) SKIP_BUILD=1 ;;
    *) echo "Opcion desconocida: $arg" >&2 ; exit 2 ;;
  esac
done

command -v java  >/dev/null || { echo "Falta 'java' en el PATH. Instale un JDK 17+." >&2 ; exit 1 ; }
# Deriva el JDK de java.home para que 'jar' funcione aunque el PATH solo tenga el JRE/shim.
JAVA_HOME_DETECTED="$(java -XshowSettings:properties -version 2>&1 | sed -n 's/.*java\.home = //p' | head -1)"
if [ -n "$JAVA_HOME_DETECTED" ] && [ -x "$JAVA_HOME_DETECTED/bin/javac" ]; then
  JAVAC="$JAVA_HOME_DETECTED/bin/javac" ; JAVA="$JAVA_HOME_DETECTED/bin/java" ; JAR="$JAVA_HOME_DETECTED/bin/jar"
else
  command -v javac >/dev/null || { echo "Falta un JDK completo (javac/jar). Instale un JDK 17+." >&2 ; exit 1 ; }
  JAVAC=javac ; JAVA=java ; JAR=jar
fi

[ "$CLEAN" -eq 1 ] && rm -rf target

if [ ! -f "$CONNECTOR_JAR" ]; then
  echo "==> Descargando MySQL Connector/J ${CONNECTOR_VER} ..."
  mkdir -p lib
  if command -v curl >/dev/null; then curl -fSL "$CONNECTOR_URL" -o "$CONNECTOR_JAR"
  else wget -O "$CONNECTOR_JAR" "$CONNECTOR_URL" ; fi
fi

[ -f "$RES_DIR/database.properties" ] || \
  echo "AVISO: falta $RES_DIR/database.properties (copie el .example y complete credenciales)." >&2

if [ "$SKIP_BUILD" -eq 0 ]; then
  echo "==> Compilando fuentes Java ..."
  rm -rf "$OUT_DIR" ; mkdir -p "$OUT_DIR"
  find "$SRC_DIR" -name '*.java' > /tmp/rapidexpress_sources.txt
  "$JAVAC" -encoding UTF-8 -d "$OUT_DIR" -cp "$CONNECTOR_JAR" @/tmp/rapidexpress_sources.txt
  [ -d "$RES_DIR" ] && cp -R "$RES_DIR"/. "$OUT_DIR"/ 2>/dev/null || true
  echo "==> Compilacion OK."
fi

if [ "$PACKAGE" -eq 1 ]; then
  echo "==> Empaquetando target/rapidexpress.jar ..."
  mkdir -p target/lib
  cp "$CONNECTOR_JAR" target/lib/
  printf 'Main-Class: %s\nClass-Path: lib/mysql-connector-j-%s.jar .\n' "$MAIN_CLASS" "$CONNECTOR_VER" > /tmp/rapidexpress.mf
  "$JAR" --create --file target/rapidexpress.jar --manifest /tmp/rapidexpress.mf -C "$OUT_DIR" .
  echo "==> Jar listo: target/rapidexpress.jar  (ejecutar: java -jar target/rapidexpress.jar ; usa target/lib/)"
fi

echo "==> Iniciando $MAIN_CLASS ..."
exec "$JAVA" -cp "$OUT_DIR:$CONNECTOR_JAR" "$MAIN_CLASS"
