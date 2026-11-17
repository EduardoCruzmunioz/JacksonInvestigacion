import models.Persona;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Suite de pruebas unitarias para EscritorJSON.
 * Aplica tecnicas de Caja Negra (integracion funcional y round-trip)
 * y Caja Blanca (cobertura de ramas de persistencia y acumulacion).
 */
@DisplayName("Pruebas Unitarias - EscritorJSON")
class EscritorJSONTest {

    @Nested
    @DisplayName("Pruebas de Caja Negra (Funcionales y Ciclo de Vida)")
    class PruebasCajaNegra {

        @Test
        @DisplayName("Debería persistir y permitir recuperar fielmente los datos de una persona (Round-trip)")
        void deberiaEscribirYRecuperarPersonaEnCicloCompleto(@TempDir Path tempDir) throws IOException {
            // Arrange
            Path destino = tempDir.resolve("persona_test.json");
            Persona esperada = new Persona("Laura", "Gómez Serrano", 29);

            // Act: Escritura y posterior lectura directa
            EscritorJSON.agregarPersona(destino, esperada);
            List<Persona> recuperadas = LectorJSON.leerPersonas(destino);

            // Assert
            assertNotNull(recuperadas);
            assertEquals(1, recuperadas.size());
            assertEquals(esperada, recuperadas.get(0), "La persona recuperada debe coincidir exactamente en todos sus campos");
        }

        @Test
        @DisplayName("Debería acumular registros consecutivos sin sobrescribir el historico")
        void deberiaPreservarRegistrosPreviosAlAgregarNuevaPersona(@TempDir Path tempDir) throws IOException {
            // Arrange
            Path destino = tempDir.resolve("personas_acumuladas.json");
            Persona persona1 = new Persona("Carlos", "Sainz", 30);
            Persona persona2 = new Persona("Fernando", "Alonso", 43);

            // Act
            EscritorJSON.agregarPersona(destino, persona1);
            List<Persona> resultadoFinal = EscritorJSON.agregarPersona(destino, persona2);

            // Assert
            assertEquals(2, resultadoFinal.size(), "Deben coexistir ambos registros");
            assertEquals(persona1, resultadoFinal.get(0));
            assertEquals(persona2, resultadoFinal.get(1));
        }

        @Test
        @DisplayName("Debería rechazar parametros nulos con NullPointerException explicito")
        void deberiaRechazarArgumentosNulos(@TempDir Path tempDir) {
            Path destino = tempDir.resolve("test.json");
            Persona valida = new Persona("Test", "Test", 20);

            assertThrows(NullPointerException.class, () -> EscritorJSON.agregarPersona(null, valida));
            assertThrows(NullPointerException.class, () -> EscritorJSON.agregarPersona(destino, null));
        }
    }

    @Nested
    @DisplayName("Pruebas de Caja Blanca (Ramas de Creacion y Acumulacion)")
    class PruebasCajaBlanca {

        @Test
        @DisplayName("Rama: Subdirectorio y fichero inexistentes -> debe crearlos automaticamente")
        void deberiaCrearDirectorioYFicheroCuandoNoExisten(@TempDir Path tempDir) throws IOException {
            // Arrange: Ruta anidada en subdirectorios que aun no existen en disco
            Path subdirectorioAnidado = tempDir.resolve("subcarpeta").resolve("nivel2").resolve("personas.json");
            Persona nueva = new Persona("Elena", "Navas", 24);

            // Act
            List<Persona> personas = EscritorJSON.agregarPersona(subdirectorioAnidado, nueva);

            // Assert: Comprobacion fisica en el sistema de ficheros
            assertTrue(Files.exists(subdirectorioAnidado), "El fichero debe existir en disco");
            assertEquals(1, personas.size());
            assertEquals("Elena", personas.get(0).getNombre());
        }

        @Test
        @DisplayName("Rama: Fichero preexistente de 0 bytes (vacio) -> debe inicializar la lista sin fallar")
        void deberiaTratarFicheroVacioComoListaNueva(@TempDir Path tempDir) throws IOException {
            // Arrange: Creamos un fichero vacio de 0 bytes
            Path ficheroVacio = tempDir.resolve("vacio.json");
            Files.createFile(ficheroVacio);
            assertEquals(0, Files.size(ficheroVacio));

            Persona nueva = new Persona("Sara", "López", 35);

            // Act: Escritura sobre fichero vacio
            List<Persona> resultado = EscritorJSON.agregarPersona(ficheroVacio, nueva);

            // Assert: No debe lanzar MismatchedInputException ni EOFException
            assertEquals(1, resultado.size());
            assertEquals(nueva, resultado.get(0));
        }

        @Test
        @DisplayName("Rama: SerializationFeature.INDENT_OUTPUT -> debe generar JSON con formato e indentacion")
        void deberiaGenerarJsonFormateadoConSaltosDeLinea(@TempDir Path tempDir) throws IOException {
            // Arrange
            Path destino = tempDir.resolve("formateado.json");
            Persona nueva = new Persona("David", "Cano", 40);

            // Act
            EscritorJSON.agregarPersona(destino, nueva);
            String contenidoJson = Files.readString(destino);

            // Assert: Verificar presencia de saltos de linea y sangrado de Jackson Pretty Printer
            assertTrue(contenidoJson.contains("\n") || contenidoJson.contains("\r\n"), "El JSON debe contener saltos de linea");
            assertTrue(contenidoJson.contains("  \"nombre\" : \"David\""), "El JSON debe estar sangrado con espacios");
        }
    }
}
