import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Scanner;

public class JSONTiempoReal {
     static void main() throws Exception {
        Scanner sc = new Scanner(System.in);

        // 1. GENERACIÓN: el JSON se construye como texto a partir de lo que escribe el usuario
        System.out.print("Nombre: ");
        String nombre = sc.nextLine();
        System.out.print("Apellidos: ");
        String apellidos = sc.nextLine();
        System.out.print("Edad: ");
        int edad = Integer.parseInt(sc.nextLine());

        String json = "{ \"nombre\": \"" + nombre + "\", "
                    + "\"apellidos\": \"" + apellidos + "\", "
                    + "\"edad\": " + edad + " }";

        System.out.println("\nJSON generado:\n" + json);

        // 2. INTERPRETACIÓN: se lee el texto y se accede a cada campo
        ObjectMapper mapper = new ObjectMapper();
        JsonNode raiz = mapper.readTree(json);

        System.out.println("\nJSON interpretado:");
        System.out.println("nombre = " + raiz.get("nombre").asText());
        System.out.println("apellidos = " + raiz.get("apellidos").asText());
        System.out.println("edad = " + raiz.get("edad").asInt());
    }
}