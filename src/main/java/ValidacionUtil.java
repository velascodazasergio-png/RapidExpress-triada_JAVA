

import com.rapidexpress.excepcion.NegocioException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.regex.Pattern;

/**
 * Utilidades de VERIFICACION DE DATOS reutilizables.
 *
 * <p>Cada metodo comprueba una condicion, lanza {@link NegocioException} con
 * un mensaje claro si no se cumple y, cuando aplica, devuelve el valor ya
 * normalizado (trim / mayusculas) para poder encadenarlo.</p>
 */
public final class ValidacionUtil {

    // Formato ABC123 (tres letras + tres digitos). Se acepta con o sin guion.
    private static final Pattern PLACA        = Pattern.compile("^[A-Z]{3}[0-9]{3}$");
    private static final Pattern EMAIL        = Pattern.compile("^[\\w.+-]+@[\\w-]+\\.[\\w.-]+$");
    private static final Pattern SOLO_DIGITOS = Pattern.compile("^[0-9]{7,10}$");
    private static final Pattern TELEFONO     = Pattern.compile("^[0-9]{7,15}$");

    // Constructor privado: clase de utilidad, no se instancia.
    private ValidacionUtil() {
    }

    /** Exige que el texto no sea nulo ni vacio; devuelve el texto sin espacios sobrantes. */
    public static String texto(String campo, String valor) throws NegocioException {
        if (valor == null || valor.isBlank()) {
            throw new NegocioException("El campo '" + campo + "' es obligatorio.");
        }
        return valor.trim();
    }

    /** Como {@link #texto} pero ademas rechaza los textos mas largos que {@code max}. */
    public static String textoMax(String campo, String valor, int max) throws NegocioException {
        String v = texto(campo, valor);
        if (v.length() > max) {
            throw new NegocioException("El campo '" + campo + "' admite maximo " + max + " caracteres.");
        }
        return v;
    }

    /** Comprueba que un entero este dentro de [min, max] (ambos incluidos). */
    public static int enteroEnRango(String campo, int valor, int min, int max) throws NegocioException {
        if (valor < min || valor > max) {
            throw new NegocioException(
                    "El campo '" + campo + "' debe estar entre " + min + " y " + max + " (recibido " + valor + ").");
        }
        return valor;
    }

    /** Exige un decimal estrictamente mayor que cero (peso, capacidad, costo...). */
    public static BigDecimal positivo(String campo, BigDecimal valor) throws NegocioException {
        if (valor == null || valor.signum() <= 0) {
            throw new NegocioException("El campo '" + campo + "' debe ser un numero mayor que cero.");
        }
        return valor;
    }

    /** Exige un decimal mayor o igual que cero (permite el valor 0). */
    public static BigDecimal noNegativo(String campo, BigDecimal valor) throws NegocioException {
        if (valor == null || valor.signum() < 0) {
            throw new NegocioException("El campo '" + campo + "' no puede ser negativo.");
        }
        return valor;
    }

    /** Valida una placa ABC123; normaliza a mayusculas y quita guiones y espacios. */
    public static String placa(String valor) throws NegocioException {
        String v = texto("placa", valor).toUpperCase().replace("-", "").replace(" ", "");
        if (!PLACA.matcher(v).matches()) {
            throw new NegocioException("Placa invalida '" + valor + "'. Formato esperado: ABC123.");
        }
        return v;
    }

    /** Valida un correo con formato basico usuario@dominio.ext. */
    public static String email(String valor) throws NegocioException {
        String v = texto("email", valor);
        if (!EMAIL.matcher(v).matches()) {
            throw new NegocioException("Correo electronico invalido: '" + valor + "'.");
        }
        return v;
    }

    /** Valida un documento de identidad: solo digitos, entre 7 y 10. */
    public static String documento(String valor) throws NegocioException {
        String v = texto("documento", valor);
        if (!SOLO_DIGITOS.matcher(v).matches()) {
            throw new NegocioException("El documento debe tener entre 7 y 10 digitos.");
        }
        return v;
    }

    /** Valida un telefono: solo digitos, entre 7 y 15. */
    public static String telefono(String valor) throws NegocioException {
        String v = texto("telefono", valor).replace(" ", "");
        if (!TELEFONO.matcher(v).matches()) {
            throw new NegocioException("Telefono invalido: '" + valor + "'.");
        }
        return v;
    }

    /** Valida un anio de fabricacion coherente: entre 1950 y el proximo anio. */
    public static int anioFabricacion(int anio) throws NegocioException {
        int actual = LocalDate.now().getYear();
        return enteroEnRango("anio de fabricacion", anio, 1950, actual + 1);
    }

    /** Exige una fecha presente que no este en el futuro. */
    public static LocalDate fechaNoFutura(String campo, LocalDate fecha) throws NegocioException {
        if (fecha == null) {
            throw new NegocioException("La fecha '" + campo + "' es obligatoria.");
        }
        if (fecha.isAfter(LocalDate.now())) {
            throw new NegocioException("La fecha '" + campo + "' no puede ser futura.");
        }
        return fecha;
    }

    /** Exige un rango de fechas valido: ambas presentes y la final no anterior a la inicial. */
    public static void rangoFechas(LocalDate desde, LocalDate hasta) throws NegocioException {
        if (desde == null || hasta == null) {
            throw new NegocioException("Debe indicar las dos fechas del rango.");
        }
        if (hasta.isBefore(desde)) {
            throw new NegocioException("La fecha final (" + hasta + ") no puede ser anterior a la inicial ("
                    + desde + ").");
        }
    }

    /** Comprueba que un valor pertenezca al conjunto de opciones permitidas. */
    public static String opcion(String campo, String valor, String... permitidos) throws NegocioException {
        String v = texto(campo, valor);
        for (String p : permitidos) {
            if (p.equalsIgnoreCase(v)) {
                return p;
            }
        }
        throw new NegocioException("Valor '" + valor + "' no valido para '" + campo + "'. Opciones: "
                + String.join(", ", permitidos) + ".");
    }
}
