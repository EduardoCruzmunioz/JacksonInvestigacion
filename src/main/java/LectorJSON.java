import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import models.Persona;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Servicio de lectura y deserializacion de colecciones JSON de personas
 * mediante Data Binding fuertemente tipado (Jackson ObjectMapper y TypeReference).
 */
public class LectorJSON {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Lee y deserializa la lista completa de personas contenidas en un fichero JSON.
     *
     * @param ruta Ruta al fichero JSON a consultar.
     * @return Lista tipada de objetos Persona deserializados.
     * @throws FileNotFoundException Si el fichero especificado no existe.
     * @throws IOException Si ocurre un error de lectura o el contenido no es un JSON compatible.
     */
    public static List<Persona> leerPersonas(Path ruta) throws IOException {
        Objects.requireNonNull(ruta, "La ruta del fichero no puede ser nula");

        File archivo = ruta.toFile();
        if (!archivo.exists()) {
            throw new FileNotFoundException("El archivo no existe en la ruta: " + ruta.toAbsolutePath());
        }

        return MAPPER.readValue(archivo, new TypeReference<ArrayList<Persona>>() {});
    }

    static void main() {
        Path ruta = Paths.get("src", "main", "resources", "persona.json");

        try {
            List<Persona> personas = leerPersonas(ruta);
            for (Persona p : personas) {
                System.out.println(p.getNombre() + " " + p.getApellidos() + ", " + p.getEdad());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
