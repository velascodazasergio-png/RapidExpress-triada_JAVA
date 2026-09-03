package com.rapidexpress.view;

import com.rapidexpress.model.dto.TablaReporte;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

/**
 * Utilidades compartidas de la capa de vista: lectura validada desde teclado e
 * impresión de tablas con columnas alineadas.
 *
 * <p>Un único {@link Scanner} sobre {@code System.in} para toda la aplicación.
 * Abrir uno por menú y cerrarlo deja el flujo de entrada cerrado para los
 * demás, que es la causa más común de {@code NoSuchElementException} en
 * aplicaciones de consola.</p>
 */
public final class ConsolaUtil {

    private static final Scanner SC = new Scanner(System.in);
    private static final int ANCHO = 100;

    private ConsolaUtil() { }

    // ------------------------------------------------------------- entrada
    public static String leerTexto(String etiqueta) {
        System.out.print(etiqueta + ": ");
        return SC.nextLine().trim();
    }

    public static int leerEntero(String etiqueta) {
        while (true) {
            String entrada = leerTexto(etiqueta);
            try {
                return Integer.parseInt(entrada);
            } catch (NumberFormatException e) {
                System.out.println("  Valor no numérico. Intente de nuevo.");
            }
        }
    }

    public static LocalDate leerFecha(String etiqueta) {
        while (true) {
            String entrada = leerTexto(etiqueta + " (aaaa-mm-dd)");
            try {
                return LocalDate.parse(entrada);
            } catch (DateTimeParseException e) {
                System.out.println("  Formato inválido. Ejemplo: 2026-09-02");
            }
        }
    }

    public static void pausar() {
        System.out.print("\nPresione ENTER para continuar...");
        SC.nextLine();
    }

    // ------------------------------------------------------------- salida
    public static void titulo(String texto) {
        System.out.println();
        System.out.println("=".repeat(ANCHO));
        System.out.println("  " + texto.toUpperCase());
        System.out.println("=".repeat(ANCHO));
    }

    public static void subtitulo(String texto) {
        System.out.println();
        System.out.println("-- " + texto + " " + "-".repeat(Math.max(0, ANCHO - texto.length() - 4)));
    }

    public static void error(String mensaje) {
        System.out.println("\n  [!] " + mensaje);
    }

    public static void aviso(String mensaje) {
        System.out.println("\n  " + mensaje);
    }

    /** Imprime una tabla genérica ajustando el ancho de cada columna al contenido. */
    public static void imprimir(TablaReporte tabla) {
        titulo(tabla.getTitulo());

        if (tabla.estaVacia()) {
            aviso("La consulta no arrojó resultados.");
            return;
        }

        List<String> cab = tabla.getEncabezados();
        int columnas = cab.size();
        int[] ancho = new int[columnas];

        for (int i = 0; i < columnas; i++) {
            ancho[i] = cab.get(i).length();
        }
        for (String[] fila : tabla.getFilas()) {
            for (int i = 0; i < columnas; i++) {
                ancho[i] = Math.max(ancho[i], fila[i].length());
            }
        }

        imprimirFila(cab.toArray(new String[0]), ancho);
        StringBuilder sep = new StringBuilder();
        for (int a : ancho) {
            sep.append("-".repeat(a + 2)).append('+');
        }
        System.out.println(sep);

        for (String[] fila : tabla.getFilas()) {
            imprimirFila(fila, ancho);
        }
        System.out.println("\n  " + tabla.getFilas().size() + " registro(s).");
    }

    private static void imprimirFila(String[] valores, int[] ancho) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < valores.length; i++) {
            sb.append(' ').append(String.format("%-" + ancho[i] + "s", valores[i])).append(" |");
        }
        System.out.println(sb);
    }
}
