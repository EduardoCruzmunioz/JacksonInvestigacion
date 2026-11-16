import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import models.Persona;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;


public class LectorJSON {
     static void main() {
        ObjectMapper mapper = new ObjectMapper();
        Path ruta = Paths.get("src", "main", "resources", "persona.json");

        try {
            ArrayList<Persona> personas = mapper.readValue(ruta.toFile(), new TypeReference<ArrayList<Persona>>() {});
            for (Persona p : personas) {
                System.out.println(p.getNombre() + " " + p.getApellidos() + ", " + p.getEdad());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}