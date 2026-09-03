package com.rapidexpress.view;

import com.rapidexpress.model.dto.TablaReporte;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

/**
 * Utilidades de entrada/salida por consola.
 *
 * IMPORTANTE PARA LA INTEGRACION: existe un unico Scanner sobre System.in
 * para toda la aplicacion. Si cada modulo crea el suyo, el segundo se queda
 * sin datos y la CLI empieza a comportarse raro. Todas las vistas de los
 * tres modulos deben usar ConsolaUtil.scanner().
 */
public final class ConsolaUtil {

    private static final Scanner SCANNER = new Scanner(System.in);

    // Constructor privado: clase de utilidad, no se instancia.
    private ConsolaUtil() {
    }

    // Devuelve el unico Scanner de System.in que comparte toda la aplicacion.
    public static Scanner scanner() {
        return SCANNER;
    }

    // Imprime un titulo enmarcado en la consola.
    public static void titulo(String texto) {
        System.out.println();
        System.out.println("=".repeat(62));
        System.out.println("  " + texto.toUpperCase());
        System.out.println("=".repeat(62));
    }

    // Imprime una linea separadora.
    public static void separador() {
        System.out.println("-".repeat(62));
    }

    // Imprime un mensaje de exito ([OK]).
    public static void exito(String mensaje) {
        System.out.println("[OK] " + mensaje);
    }

    // Imprime un mensaje de error ([ERROR]).
    public static void error(String mensaje) {
        System.out.println("[ERROR] " + mensaje);
    }

    // Imprime un mensaje informativo con sangria.
    public static void info(String mensaje) {
        System.out.println("      " + mensaje);
    }

    // Imprime un aviso resaltado (para listados vacios o notas al usuario).
    public static void aviso(String mensaje) {
        System.out.println("  >> " + mensaje);
    }

    // Imprime una tabla de reporte: titulo, encabezados y filas con columnas alineadas.
    public static void imprimir(TablaReporte tabla) {
        titulo(tabla.getTitulo());
        if (tabla.estaVacia()) {
            aviso("El reporte no tiene datos.");
            return;
        }
        List<String> encabezados = tabla.getEncabezados();
        int columnas = encabezados.size();
        int[] ancho = new int[columnas];
        for (int i = 0; i < columnas; i++) {
            ancho[i] = encabezados.get(i).length();
        }
        for (String[] fila : tabla.getFilas()) {
            for (int i = 0; i < columnas; i++) {
                int largo = fila[i] == null ? 1 : fila[i].length();
                ancho[i] = Math.max(ancho[i], largo);
            }
        }
        StringBuilder separador = new StringBuilder();
        for (int i = 0; i < columnas; i++) {
            System.out.printf("%-" + (ancho[i] + 2) + "s", encabezados.get(i));
            separador.append("-".repeat(ancho[i] + 2));
        }
        System.out.println();
        System.out.println(separador);
        for (String[] fila : tabla.getFilas()) {
            for (int i = 0; i < columnas; i++) {
                System.out.printf("%-" + (ancho[i] + 2) + "s", fila[i] == null ? "-" : fila[i]);
            }
            System.out.println();
        }
        System.out.println("\n  " + tabla.getFilas().size() + " fila(s).");
    }

    // Pide un texto por consola y no acepta vacio.
    public static String leerTexto(String etiqueta) {
        while (true) {
            System.out.print(etiqueta + ": ");
            String valor = SCANNER.nextLine().trim();
            if (!valor.isEmpty()) {
                return valor;
            }
            error("El valor no puede estar vacio.");
        }
    }

    // Pide un texto por consola; ENTER devuelve null.
    public static String leerTextoOpcional(String etiqueta) {
        System.out.print(etiqueta + " (opcional, Enter para omitir): ");
        String valor = SCANNER.nextLine().trim();
        return valor.isEmpty() ? null : valor;
    }

    // Pide un numero entero por consola, reintentando si no es valido.
    public static int leerEntero(String etiqueta) {
        while (true) {
            System.out.print(etiqueta + ": ");
            try {
                return Integer.parseInt(SCANNER.nextLine().trim());
            } catch (NumberFormatException e) {
                error("Debe ingresar un numero entero.");
            }
        }
    }

    // Pide un numero decimal por consola, reintentando si no es valido.
    public static BigDecimal leerDecimal(String etiqueta) {
        while (true) {
            System.out.print(etiqueta + ": ");
            try {
                return new BigDecimal(SCANNER.nextLine().trim().replace(",", "."));
            } catch (NumberFormatException e) {
                error("Debe ingresar un numero (ej. 2500.50).");
            }
        }
    }

    /** Lee una fecha en formato AAAA-MM-DD; Enter devuelve la fecha de hoy. */
    public static LocalDate leerFecha(String etiqueta) {
        while (true) {
            System.out.print(etiqueta + " (AAAA-MM-DD, Enter = hoy): ");
            String valor = SCANNER.nextLine().trim();
            if (valor.isEmpty()) {
                return LocalDate.now();
            }
            try {
                return LocalDate.parse(valor);
            } catch (DateTimeParseException e) {
                error("Formato de fecha invalido. Ejemplo: 2026-08-29");
            }
        }
    }

    // Pide una confirmacion s/n y devuelve true si es afirmativa.
    public static boolean confirmar(String pregunta) {
        System.out.print(pregunta + " (s/n): ");
        String valor = SCANNER.nextLine().trim().toLowerCase();
        return valor.equals("s") || valor.equals("si");
    }

    // Espera a que el usuario pulse ENTER para continuar.
    public static void pausa() {
        System.out.print("\nPresione Enter para continuar...");
        SCANNER.nextLine();
    }

    // Alias de pausa(): espera a que el usuario pulse ENTER.
    public static void pausar() {
        pausa();
    }
}
