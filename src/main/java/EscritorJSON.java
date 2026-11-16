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


public class EscritorJSON {
     static void main() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        Path ruta = Paths.get("src", "main", "resources", "persona.json");
        File f = ruta.toFile();

        Persona nueva = new Persona("Angel", "Miranda", 22);

        try {
            Files.createDirectories(ruta.getParent());

            ArrayList<Persona> personas = new ArrayList<>();
            if (f.exists() && f.length() > 0) {
                personas = mapper.readValue(f, new TypeReference<ArrayList<Persona>>() {});
            }

            personas.add(nueva);
            mapper.writeValue(f, personas);

            System.out.println("Registros en el fichero: " + personas.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}