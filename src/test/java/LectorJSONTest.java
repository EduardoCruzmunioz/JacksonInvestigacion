import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import models.Persona;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Suite de pruebas unitarias para LectorJSON.
 * Aplica tecnicas de diseno de Caja Negra (funcionales y limites)
 * y Caja Blanca (cobertura de ramas y caminos de excepcion).
 */
@DisplayName("Pruebas Unitarias - LectorJSON")
class LectorJSONTest {

    @Nested
    @DisplayName("Pruebas de Caja Negra (Funcionales y Valores Limite)")
    class PruebasCajaNegra {

        @Test
        @DisplayName("Deberia deserializar exitosamente la lista de 10 personas del recurso real")
        void deberiaLeerListaDePersonasDesdeFicheroReal() throws IOException {
            // Arrange: Ruta al recurso real del proyecto
            Path rutaRecurso = Paths.get("src", "main", "resources", "persona.json");

            // Act: Invocacion de la operacion de lectura
            List<Persona> personas = LectorJSON.leerPersonas(rutaRecurso);

            // Assert: Comprobacion de tamano y contenido representativo
            assertNotNull(personas, "La lista retornada no debe ser nula");
            assertEquals(10, personas.size(), "El fichero persona.json debe contener exactamente 10 registros");

            Persona primera = personas.get(0);
            assertEquals("Eduardo", primera.getNombre());
            assertEquals("Cruz Muñoz", primera.getApellidos());
            assertEquals(22, primera.getEdad());
        }

        @Test
        @DisplayName("Deberia soportar valores limite de edad (0 y 102) y caracteres especiales")
        void deberiaMapearCorrectamenteValoresLimiteYCaracteresEspeciales() throws IOException {
            // Arrange
            Path rutaRecurso = Paths.get("src", "main", "resources", "persona.json");

            // Act
            List<Persona> personas = LectorJSON.leerPersonas(rutaRecurso);

            // Assert: Busqueda y validacion de casos limite definidos en el JSON
            Persona recienNacido = personas.stream()
                    .filter(p -> p.getNombre().equals("Mateo"))
                    .findFirst()
                    .orElseThrow();
            assertEquals(0, recienNacido.getEdad(), "Debe interpretar correctamente el limite inferior de edad (0 anos)");

            Persona centenario = personas.stream()
                    .filter(p -> p.getNombre().equals("Antonio"))
                    .findFirst()
                    .orElseThrow();
            assertEquals(102, centenario.getEdad(), "Debe interpretar correctamente edades centenarias (>100 anos)");
            assertEquals("O'Connor Jiménez", centenario.getApellidos(), "Debe soportar caracteres especiales como apostrofes y tildes");
        }

        @Test
        @DisplayName("Deberia rechazar rutas nulas lanzando NullPointerException")
        void deberiaLanzarExcepcionSiRutaEsNula() {
            // Act & Assert
            NullPointerException excepcion = assertThrows(
                    NullPointerException.class,
                    () -> LectorJSON.leerPersonas(null)
            );
            assertTrue(excepcion.getMessage().contains("no puede ser nula"));
        }
    }

    @Nested
    @DisplayName("Pruebas de Caja Blanca (Ramas y Cobertura Estructural)")
    class PruebasCajaBlanca {

        @Test
        @DisplayName("Rama: Fichero no existe -> debe lanzar FileNotFoundException")
        void deberiaLanzarFileNotFoundExceptionCuandoFicheroNoExiste(@TempDir Path tempDir) {
            // Arrange: Generamos una ruta garantizada como inexistente dentro del directorio temporal
            Path rutaInexistente = tempDir.resolve("inexistente.json");

            // Act & Assert
            assertThrows(
                    FileNotFoundException.class,
                    () -> LectorJSON.leerPersonas(rutaInexistente)
            );
        }

        @Test
        @DisplayName("Rama: Formato inesperado (Objeto en lugar de Array) -> debe lanzar MismatchedInputException")
        void deberiaLanzarMismatchedInputExceptionCuandoJsonEsObjetoEnLugarDeArray(@TempDir Path tempDir) throws IOException {
            // Arrange: Simulamos el caso historico donde el fichero solo contenia un objeto { }
            Path ficheroObjeto = tempDir.resolve("objeto_individual.json");
            String contenidoObjeto = "{ \"nombre\": \"Angel\", \"apellidos\": \"Miranda\", \"edad\": 22 }";
            Files.writeString(ficheroObjeto, contenidoObjeto);

            // Act & Assert: TypeReference espera ArrayList<Persona>, por lo que debe fallar ante un Object Start Token
            assertThrows(
                    MismatchedInputException.class,
                    () -> LectorJSON.leerPersonas(ficheroObjeto)
            );
        }

        @Test
        @DisplayName("Rama: JSON malformado o corrupto -> debe lanzar IOException")
        void deberiaLanzarExcepcionCuandoJsonEstaCorrupto(@TempDir Path tempDir) throws IOException {
            // Arrange: Fichero con corrupcion sintactica
            Path ficheroCorrupto = tempDir.resolve("corrupto.json");
            Files.writeString(ficheroCorrupto, "[ { \"nombre\": \"Incompleto\" ... ");

            // Act & Assert
            assertThrows(
                    IOException.class,
                    () -> LectorJSON.leerPersonas(ficheroCorrupto)
            );
        }
    }
}
