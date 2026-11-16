import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import models.Persona;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

/**
 * Demostracion de procesamiento de alto rendimiento mediante la Streaming API de Jackson.
 * Utiliza JsonGenerator para emision continua de eventos y JsonParser para lectura token a token.
 */
public class JacksonStreaming {

    private static final JsonFactory FACTORY = new JsonFactory();

    /**
     * Agrega una nueva persona al fichero JSON especificado utilizando JsonGenerator.
     * Preserva los registros anteriores (tanto si estaban en formato Array como si era un objeto unico).
     *
     * @param ruta      Ruta al fichero destino.
     * @param nombre    Nombre de la persona.
     * @param apellidos Apellidos de la persona.
     * @param edad      Edad en anos.
     * @throws IOException Si ocurre un error de lectura o escritura en el flujo.
     */
    public static void agregarPersonaStreaming(Path ruta, String nombre, String apellidos, int edad) throws IOException {
        Objects.requireNonNull(ruta, "La ruta del fichero no puede ser nula");
        Objects.requireNonNull(nombre, "El nombre no puede ser nulo");
        Objects.requireNonNull(apellidos, "Los apellidos no pueden ser nulos");

        if (ruta.getParent() != null) {
            Files.createDirectories(ruta.getParent());
        }

        // Leemos el contenido anterior en memoria para no colisionar con el stream de salida
        String anterior = Files.exists(ruta) ? Files.readString(ruta) : "";

        try (JsonGenerator gen = FACTORY.createGenerator(ruta.toFile(), JsonEncoding.UTF8)) {
            gen.useDefaultPrettyPrinter();
            gen.writeStartArray();

            // Copiado selectivo del historico existente
            if (!anterior.isBlank()) {
                try (JsonParser viejo = FACTORY.createParser(anterior)) {
                    JsonToken tokenInicial = viejo.nextToken();
                    if (tokenInicial == JsonToken.START_ARRAY) {
                        while (viejo.nextToken() == JsonToken.START_OBJECT) {
                            gen.copyCurrentStructure(viejo);
                        }
                    } else if (tokenInicial == JsonToken.START_OBJECT) {
                        // Compatibilidad con ficheros heredados que solo contenian un unico objeto { }
                        gen.copyCurrentStructure(viejo);
                    }
                }
            }

            // Emision en streaming de la persona nueva
            gen.writeStartObject();
            gen.writeStringField("nombre", nombre);
            gen.writeStringField("apellidos", apellidos);
            gen.writeNumberField("edad", edad);
            gen.writeEndObject();

            gen.writeEndArray();
        }
    }

    /**
     * Lee e interpreta el fichero JSON token a token mediante JsonParser.
     *
     * @param ruta Ruta al fichero a procesar.
     * @return Lista de personas reconstruidas a partir de los eventos del flujo.
     * @throws FileNotFoundException Si el archivo no existe.
     * @throws IOException Si ocurre un fallo de E/S o corrupcion en los tokens.
     */
    public static List<Persona> leerPersonasStreaming(Path ruta) throws IOException {
        Objects.requireNonNull(ruta, "La ruta del fichero no puede ser nula");
        if (!Files.exists(ruta)) {
            throw new FileNotFoundException("El archivo especificado no existe: " + ruta.toAbsolutePath());
        }

        try (JsonParser parser = FACTORY.createParser(ruta.toFile())) {
            return leerPersonasStreamingDesdeParser(parser);
        }
    }

    /**
     * Procesa un flujo de tokens JSON para extraer instancias de Persona.
     *
     * @param parser JsonParser posicionado en el inicio del flujo.
     * @return Lista de personas parseadas.
     * @throws IOException Si la estructura del JSON es incompatible o corrupta.
     */
    public static List<Persona> leerPersonasStreamingDesdeParser(JsonParser parser) throws IOException {
        List<Persona> personas = new ArrayList<>();

        parser.nextToken(); // Avanza al primer token (esperado: START_ARRAY o START_OBJECT)
        while (parser.nextToken() == JsonToken.START_OBJECT) {
            String nombre = "";
            String apellidos = "";
            int edad = 0;

            while (parser.nextToken() != JsonToken.END_OBJECT) {
                String campo = parser.currentName();
                parser.nextToken(); // Avanza al valor del campo

                switch (campo) {
                    case "nombre" -> nombre = parser.getText();
                    case "apellidos" -> apellidos = parser.getText();
                    case "edad" -> edad = parser.getIntValue();
                    default -> parser.skipChildren(); // Resiliencia ante campos no esperados
                }
            }
            personas.add(new Persona(nombre, apellidos, edad));
        }

        return personas;
    }

    static void main() throws IOException {
        Scanner s = new Scanner(System.in);
        Path ruta = Paths.get("src", "main", "resources", "persona.json");

        // Pedimos los datos antes de tocar el fichero
        System.out.print("Introduce el nombre: ");
        String nombre = s.nextLine();
        System.out.print("Introduce los apellidos: ");
        String apellidos = s.nextLine();
        System.out.print("Introduce la edad: ");
        int edad = Integer.parseInt(s.nextLine());

        agregarPersonaStreaming(ruta, nombre, apellidos, edad);
        System.out.println("JSON guardado en: " + ruta.toAbsolutePath());

        // Se lee el fichero token a token
        System.out.println("\nJSON interpretado:");
        List<Persona> personas = leerPersonasStreaming(ruta);
        for (Persona p : personas) {
            System.out.println("nombre = " + p.getNombre());
            System.out.println("apellidos = " + p.getApellidos());
            System.out.println("edad = " + p.getEdad());
            System.out.println("---");
        }
    }
}
