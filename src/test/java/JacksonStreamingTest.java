import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import models.Persona;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Suite de pruebas unitarias para JacksonStreaming.
 * Evalua la emision continua de eventos con JsonGenerator y el procesamiento token a token con JsonParser.
 */
@DisplayName("Pruebas Unitarias - JacksonStreaming")
class JacksonStreamingTest {

    @Nested
    @DisplayName("Pruebas de Caja Negra (Funcionalidad y Resiliencia)")
    class PruebasCajaNegra {

        @Test
        @DisplayName("Debería escribir y leer una persona correctamente en flujo Streaming")
        void deberiaEscribirYLeerPersonaEnFlujoStreaming(@TempDir Path tempDir) throws IOException {
            // Arrange
            Path destino = tempDir.resolve("streaming_persona.json");

            // Act
            JacksonStreaming.agregarPersonaStreaming(destino, "Mario", "Torredelcampo", 19);
            List<Persona> personas = JacksonStreaming.leerPersonasStreaming(destino);

            // Assert
            assertNotNull(personas);
            assertEquals(1, personas.size());
            Persona recuperada = personas.get(0);
            assertEquals("Mario", recuperada.getNombre());
            assertEquals("Torredelcampo", recuperada.getApellidos());
            assertEquals(19, recuperada.getEdad());
        }

        @Test
        @DisplayName("Debería acumular multiples personas preservando el orden secuencial de emision")
        void deberiaAcumularMultiplesPersonasPreservandoElOrdenSecuencial(@TempDir Path tempDir) throws IOException {
            // Arrange
            Path destino = tempDir.resolve("streaming_acumulado.json");

            // Act: Tres escrituras consecutivas
            JacksonStreaming.agregarPersonaStreaming(destino, "Primero", "Uno", 20);
            JacksonStreaming.agregarPersonaStreaming(destino, "Segundo", "Dos", 30);
            JacksonStreaming.agregarPersonaStreaming(destino, "Tercero", "Tres", 40);

            List<Persona> resultado = JacksonStreaming.leerPersonasStreaming(destino);

            // Assert
            assertEquals(3, resultado.size());
            assertEquals("Primero", resultado.get(0).getNombre());
            assertEquals("Segundo", resultado.get(1).getNombre());
            assertEquals("Tercero", resultado.get(2).getNombre());
        }

        @Test
        @DisplayName("Ventaja sobre concatenacion manual: deberia escapar comillas dobles sin romper el JSON")
        void deberiaManejarNombresConComillasSinRomperseAFavorDelGenerator(@TempDir Path tempDir) throws IOException {
            // Arrange: Nombre con comillas dobles internas
            Path destino = tempDir.resolve("comillas_seguras.json");
            String nombreConComillas = "Juan \"El Rápido\"";

            // Act: JsonGenerator escapa automaticamente las comillas al emitir el token de cadena
            JacksonStreaming.agregarPersonaStreaming(destino, nombreConComillas, "Pérez", 35);
            List<Persona> recuperadas = JacksonStreaming.leerPersonasStreaming(destino);

            // Assert
            assertEquals(1, recuperadas.size());
            assertEquals("Juan \"El Rápido\"", recuperadas.get(0).getNombre());
        }

        @Test
        @DisplayName("Debería soportar valores limite de edad (0 y 102 anos) y caracteres especiales en UTF-8")
        void deberiaSoportarValoresLimiteDeEdadYCaracteresEspeciales(@TempDir Path tempDir) throws IOException {
            // Arrange
            Path destino = tempDir.resolve("limites.json");

            // Act
            JacksonStreaming.agregarPersonaStreaming(destino, "Mateo", "Peña Nieto", 0);
            JacksonStreaming.agregarPersonaStreaming(destino, "Antonio", "O'Connor Jiménez", 102);

            List<Persona> recuperadas = JacksonStreaming.leerPersonasStreaming(destino);

            // Assert
            assertEquals(2, recuperadas.size());
            assertEquals(0, recuperadas.get(0).getEdad(), "Limite inferior: recién nacido (0 anos)");
            assertEquals("Peña Nieto", recuperadas.get(0).getApellidos(), "Debe soportar caracteres especiales como la eñe");
            assertEquals(102, recuperadas.get(1).getEdad(), "Limite superior: centenario (102 anos)");
            assertEquals("O'Connor Jiménez", recuperadas.get(1).getApellidos(), "Debe soportar apostrofes y tildes");
        }

        @Test
        @DisplayName("Debería rechazar parametros nulos con NullPointerException defensivo")
        void deberiaRechazarParametrosNulosConNullPointerException(@TempDir Path tempDir) {
            Path destino = tempDir.resolve("test.json");

            assertThrows(NullPointerException.class, () -> JacksonStreaming.agregarPersonaStreaming(null, "A", "B", 20));
            assertThrows(NullPointerException.class, () -> JacksonStreaming.agregarPersonaStreaming(destino, null, "B", 20));
            assertThrows(NullPointerException.class, () -> JacksonStreaming.agregarPersonaStreaming(destino, "A", null, 20));
        }
    }

    @Nested
    @DisplayName("Pruebas de Caja Blanca (Ramas de Tokens y Compatibilidad)")
    class PruebasCajaBlanca {

        @Test
        @DisplayName("Rama: Fichero previo inexistente o vacio -> debe crear el array inicial")
        void deberiaCrearArrayInicialCuandoFicheroNoExistePreviamente(@TempDir Path tempDir) throws IOException {
            Path destino = tempDir.resolve("nuevo_array.json");

            JacksonStreaming.agregarPersonaStreaming(destino, "Nuevo", "Registro", 25);

            String contenido = Files.readString(destino);
            assertTrue(contenido.trim().startsWith("["), "Debe abrir con token START_ARRAY '['");
            assertTrue(contenido.trim().endsWith("]"), "Debe cerrar con token END_ARRAY ']'");
        }

        @Test
        @DisplayName("Rama: Fichero previo es un Array [] -> debe clonar cada persona previa con copyCurrentStructure")
        void deberiaCopiarEstructurasCuandoFicheroPrevioContieneUnArray(@TempDir Path tempDir) throws IOException {
            // Arrange: Simulamos fichero previo con array de 1 elemento
            Path destino = tempDir.resolve("array_previo.json");
            String previo = "[\n  {\n    \"nombre\" : \"Previo1\",\n    \"apellidos\" : \"Test\",\n    \"edad\" : 20\n  }\n]";
            Files.writeString(destino, previo);

            // Act: Agregamos via Streaming
            JacksonStreaming.agregarPersonaStreaming(destino, "Nuevo2", "Test2", 30);
            List<Persona> personas = JacksonStreaming.leerPersonasStreaming(destino);

            // Assert: Ambos elementos deben coexistir
            assertEquals(2, personas.size());
            assertEquals("Previo1", personas.get(0).getNombre());
            assertEquals("Nuevo2", personas.get(1).getNombre());
        }

        @Test
        @DisplayName("Rama: Compatibilidad con fichero antiguo de objeto individual {} -> debe migrarlo a array sin fallar")
        void deberiaMigrarCorrectamenteFicheroAntiguoConUnSoloObjeto(@TempDir Path tempDir) throws IOException {
            // Arrange: Fichero legacy que comenzaba con START_OBJECT en vez de START_ARRAY
            Path destino = tempDir.resolve("fichero_legacy.json");
            String jsonObjetoUnico = "{\n  \"nombre\" : \"Angel\",\n  \"apellidos\" : \"Miranda\",\n  \"edad\" : 22\n}";
            Files.writeString(destino, jsonObjetoUnico);

            // Act: Agregar nueva persona sobre el fichero de objeto unico
            JacksonStreaming.agregarPersonaStreaming(destino, "Eduardo", "Cruz", 22);
            List<Persona> personas = JacksonStreaming.leerPersonasStreaming(destino);

            // Assert: Se migro a lista de 2 personas
            assertEquals(2, personas.size(), "Debe contener tanto la persona original como la nueva");
            assertEquals("Angel", personas.get(0).getNombre());
            assertEquals("Eduardo", personas.get(1).getNombre());
        }

        @Test
        @DisplayName("Rama: Fichero no existe en lectura -> debe lanzar FileNotFoundException")
        void deberiaLanzarFileNotFoundExceptionAlLeerFicheroInexistente(@TempDir Path tempDir) {
            Path inexistente = tempDir.resolve("no_existe.json");

            assertThrows(FileNotFoundException.class, () -> JacksonStreaming.leerPersonasStreaming(inexistente));
        }

        @Test
        @DisplayName("Rama: Resiliencia ante campos desconocidos -> parser debe saltar el campo sin fallar (skipChildren)")
        void deberiaIgnorarCamposDesconocidosRespetandoElParser() throws IOException {
            // Arrange: JSON que incluye un campo no mapeado ("email" y "direccion")
            String jsonConCamposExtra = """
                    [
                      {
                        "nombre": "Lucía",
                        "email": "lucia@ejemplo.com",
                        "apellidos": "Fernández",
                        "direccion": { "calle": "Gran Via", "numero": 10 },
                        "edad": 31
                      }
                    ]
                    """;

            // Act
            JsonFactory factory = new JsonFactory();
            try (JsonParser parser = factory.createParser(jsonConCamposExtra)) {
                List<Persona> resultado = JacksonStreaming.leerPersonasStreamingDesdeParser(parser);

                // Assert: Lee la persona omitiendo los campos extra sin lanzar excepciones
                assertEquals(1, resultado.size());
                Persona persona = resultado.get(0);
                assertEquals("Lucía", persona.getNombre());
                assertEquals("Fernández", persona.getApellidos());
                assertEquals(31, persona.getEdad());
            }
        }
    }
}
