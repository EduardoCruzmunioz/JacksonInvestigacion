import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import models.Persona;

import java.io.IOException;
import java.util.Objects;
import java.util.Scanner;

/**
 * Demostracion de generacion manual de JSON (concatenacion directa de String)
 * e interpretacion mediante el modelo de arbol (Tree Model / JsonNode) de Jackson.
 */
public class JSONTiempoReal {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Construye una cadena JSON manualmente a partir de los datos individuales.
     * Nota: Este enfoque es propenso a errores si los textos contienen comillas dobles sin escapar.
     *
     * @param nombre    Nombre de la persona.
     * @param apellidos Apellidos de la persona.
     * @param edad      Edad en anos.
     * @return Cadena con el documento JSON generado.
     */
    public static String generarJson(String nombre, String apellidos, int edad) {
        return "{ \"nombre\": \"" + nombre + "\", "
                + "\"apellidos\": \"" + apellidos + "\", "
                + "\"edad\": " + edad + " }";
    }

    /**
     * Interpreta una cadena de texto en formato JSON y devuelve el nodo raiz del arbol DOM.
     *
     * @param json Cadena de texto con el JSON a interpretar.
     * @return JsonNode raiz para navegar por sus campos.
     * @throws IOException Si la sintaxis del JSON es invalida o esta vacio.
     */
    public static JsonNode interpretarJson(String json) throws IOException {
        Objects.requireNonNull(json, "La cadena JSON no puede ser nula");
        if (json.isBlank()) {
            throw new IllegalArgumentException("La cadena JSON no puede estar vacia");
        }
        return MAPPER.readTree(json);
    }

    /**
     * Extrae un objeto de dominio Persona a partir del arbol JsonNode interpretado.
     *
     * @param raiz Nodo raiz que contiene los campos requeridos.
     * @return Instancia de Persona con los valores extraidos.
     */
    public static Persona mapearDesdeNodo(JsonNode raiz) {
        Objects.requireNonNull(raiz, "El nodo JSON raiz no puede ser nulo");
        String nombre = raiz.hasNonNull("nombre") ? raiz.get("nombre").asText() : "";
        String apellidos = raiz.hasNonNull("apellidos") ? raiz.get("apellidos").asText() : "";
        int edad = raiz.hasNonNull("edad") ? raiz.get("edad").asInt() : 0;

        return new Persona(nombre, apellidos, edad);
    }

    static void main() throws Exception {
        Scanner sc = new Scanner(System.in);

        // 1. GENERACION: el JSON se construye como texto a partir de lo que escribe el usuario
        System.out.print("Nombre: ");
        String nombre = sc.nextLine();
        System.out.print("Apellidos: ");
        String apellidos = sc.nextLine();
        System.out.print("Edad: ");
        int edad = Integer.parseInt(sc.nextLine());

        String json = generarJson(nombre, apellidos, edad);
        System.out.println("\nJSON generado:\n" + json);

        // 2. INTERPRETACION: se lee el texto y se accede a cada campo via JsonNode
        JsonNode raiz = interpretarJson(json);

        System.out.println("\nJSON interpretado:");
        System.out.println("nombre = " + raiz.get("nombre").asText());
        System.out.println("apellidos = " + raiz.get("apellidos").asText());
        System.out.println("edad = " + raiz.get("edad").asInt());
    }
}
