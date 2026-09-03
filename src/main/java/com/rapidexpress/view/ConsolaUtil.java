package com.rapidexpress.view;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
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

    private ConsolaUtil() {
    }

    public static Scanner scanner() {
        return SCANNER;
    }

    public static void titulo(String texto) {
        System.out.println();
        System.out.println("=".repeat(62));
        System.out.println("  " + texto.toUpperCase());
        System.out.println("=".repeat(62));
    }

    public static void separador() {
        System.out.println("-".repeat(62));
    }

    public static void exito(String mensaje) {
        System.out.println("[OK] " + mensaje);
    }

    public static void error(String mensaje) {
        System.out.println("[ERROR] " + mensaje);
    }

    public static void info(String mensaje) {
        System.out.println("      " + mensaje);
    }

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

    public static String leerTextoOpcional(String etiqueta) {
        System.out.print(etiqueta + " (opcional, Enter para omitir): ");
        String valor = SCANNER.nextLine().trim();
        return valor.isEmpty() ? null : valor;
    }

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

    public static boolean confirmar(String pregunta) {
        System.out.print(pregunta + " (s/n): ");
        String valor = SCANNER.nextLine().trim().toLowerCase();
        return valor.equals("s") || valor.equals("si");
    }

    public static void pausa() {
        System.out.print("\nPresione Enter para continuar...");
        SCANNER.nextLine();
    }
}
