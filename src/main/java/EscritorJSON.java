import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import models.Persona;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Gestiona la persistencia y acumulacion de registros de personas en formato JSON
 * mediante el enfoque de Data Binding con Jackson ObjectMapper.
 */
public class EscritorJSON {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    /**
     * Agrega una persona al fichero JSON especificado preservando los registros previos.
     * Si el fichero no existe, crea la estructura de directorios y el array inicial.
     *
     * @param ruta  Ruta al fichero persona.json.
     * @param nueva Objeto Persona a persistir.
     * @return Lista inmutable o copia con todas las personas acumuladas tras la insercion.
     * @throws IOException Si ocurre un error de entrada/salida o serializacion.
     */
    public static List<Persona> agregarPersona(Path ruta, Persona nueva) throws IOException {
        Objects.requireNonNull(ruta, "La ruta del fichero no puede ser nula");
        Objects.requireNonNull(nueva, "El registro de persona a guardar no puede ser nulo");

        if (ruta.getParent() != null) {
            Files.createDirectories(ruta.getParent());
        }

        File archivo = ruta.toFile();
        ArrayList<Persona> personas = new ArrayList<>();

        if (archivo.exists() && archivo.length() > 0) {
            personas = MAPPER.readValue(archivo, new TypeReference<ArrayList<Persona>>() {});
        }

        personas.add(nueva);
        MAPPER.writeValue(archivo, personas);

        return personas;
    }

    static void main() {
        Path ruta = Paths.get("src", "main", "resources", "persona.json");
        Persona nueva = new Persona("Angel", "Miranda", 22);

        try {
            List<Persona> personas = agregarPersona(ruta, nueva);
            System.out.println("Registros en el fichero: " + personas.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
