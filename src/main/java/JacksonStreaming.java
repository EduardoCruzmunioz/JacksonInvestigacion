import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class JacksonStreaming {
     static void main() throws IOException {
        JsonFactory factory = new JsonFactory();
        Scanner s = new Scanner(System.in);

        Path ruta = Paths.get("src", "main", "resources", "persona.json");
        Files.createDirectories(ruta.getParent());

        // Pedimos los datos antes de tocar el fichero
        System.out.print("Introduce el nombre: ");
        String nombre = s.nextLine();
        System.out.print("Introduce los apellidos: ");
        String apellidos = s.nextLine();
        System.out.print("Introduce la edad: ");
        int edad = Integer.parseInt(s.nextLine());

        // Guardamos el contenido anterior (no se puede leer y escribir el mismo fichero a la vez)
        String anterior = Files.exists(ruta) ? Files.readString(ruta) : "";

        // reescribimos lo anterior + la persona nueva
        try (JsonGenerator gen = factory.createGenerator(ruta.toFile(), JsonEncoding.UTF8)) {
            gen.useDefaultPrettyPrinter();
            gen.writeStartArray();

            if (!anterior.isBlank()) {
                try (JsonParser viejo = factory.createParser(anterior)) {
                    JsonToken t = viejo.nextToken();
                    if (t == JsonToken.START_ARRAY) {
                        while (viejo.nextToken() == JsonToken.START_OBJECT) {
                            gen.copyCurrentStructure(viejo);   // copia cada persona existente
                        }
                    } else if (t == JsonToken.START_OBJECT) {
                        gen.copyCurrentStructure(viejo);       // fichero antiguo con un solo objeto
                    }
                }
            }

            gen.writeStartObject();
            gen.writeStringField("nombre", nombre);
            gen.writeStringField("apellidos", apellidos);
            gen.writeNumberField("edad", edad);
            gen.writeEndObject();

            gen.writeEndArray();
        }
        System.out.print("JSON guardado en: " + ruta.toAbsolutePath());

        // se lee el fichero token a token
        System.out.print("\nJSON interpretado:");
        try (JsonParser parser = factory.createParser(ruta.toFile())) {
            parser.nextToken();
            while (parser.nextToken() == JsonToken.START_OBJECT) {
                while (parser.nextToken() != JsonToken.END_OBJECT) {
                    String campo = parser.currentName();
                    parser.nextToken();
                    switch (campo) {
                        case "nombre" -> System.out.println("nombre = " + parser.getText());
                        case "apellidos" -> System.out.println("apellidos = " + parser.getText());
                        case "edad" -> System.out.println("edad = " + parser.getIntValue());
                    }
                }
                System.out.println("---");
            }
        }
    }
}