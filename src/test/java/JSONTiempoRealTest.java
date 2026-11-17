import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import models.Persona;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Suite de pruebas unitarias para JSONTiempoReal.
 * Evalua la generacion manual basada en Strings y la interpretacion mediante el modelo de arbol (JsonNode).
 */
@DisplayName("Pruebas Unitarias - JSONTiempoReal")
class JSONTiempoRealTest {

    @Nested
    @DisplayName("Pruebas de Caja Negra (Funcionalidad y Casos Limite)")
    class PruebasCajaNegra {

        @Test
        @DisplayName("Debería generar una cadena JSON valida con datos estandar")
        void deberiaGenerarCadenaJsonCorrectaConDatosEstandar() {
            // Arrange
            String nombre = "Eduardo";
            String apellidos = "Cruz Muñoz";
            int edad = 22;

            // Act
            String jsonGenerado = JSONTiempoReal.generarJson(nombre, apellidos, edad);

            // Assert
            assertTrue(jsonGenerado.contains("\"nombre\": \"Eduardo\""));
            assertTrue(jsonGenerado.contains("\"apellidos\": \"Cruz Muñoz\""));
            assertTrue(jsonGenerado.contains("\"edad\": 22"));
        }

        @Test
        @DisplayName("Debería interpretar fielmente los campos desde una cadena JSON valida")
        void deberiaInterpretarCamposCorrectamenteDesdeJsonValido() throws IOException {
            // Arrange
            String json = "{ \"nombre\": \"Ángel\", \"apellidos\": \"Miranda Ruiz\", \"edad\": 22 }";

            // Act
            JsonNode raiz = JSONTiempoReal.interpretarJson(json);
            Persona persona = JSONTiempoReal.mapearDesdeNodo(raiz);

            // Assert
            assertEquals("Ángel", raiz.get("nombre").asText());
            assertEquals("Miranda Ruiz", raiz.get("apellidos").asText());
            assertEquals(22, raiz.get("edad").asInt());
            assertEquals(new Persona("Ángel", "Miranda Ruiz", 22), persona);
        }

        @ParameterizedTest(name = "Edad limite probada: {0} anos")
        @ValueSource(ints = {0, 102})
        @DisplayName("Debería soportar valores limite de edad en la interpretacion (0 y 102 anos)")
        void deberiaSoportarValoresLimiteDeEdad(int edadLimite) throws IOException {
            // Arrange
            String json = JSONTiempoReal.generarJson("Test", "Limite", edadLimite);

            // Act
            JsonNode raiz = JSONTiempoReal.interpretarJson(json);

            // Assert
            assertEquals(edadLimite, raiz.get("edad").asInt());
        }

        @Test
        @DisplayName("Debería rechazar cadena JSON nula lanzando NullPointerException")
        void deberiaRechazarCadenaJsonNulaLanzandoNullPointerException() {
            assertThrows(NullPointerException.class, () -> JSONTiempoReal.interpretarJson(null));
        }

        @Test
        @DisplayName("Punto debil: deberia romperse la interpretacion cuando el texto contiene comillas dobles sin escapar")
        void deberiaRomperseLaInterpretacionCuandoElNombreContieneComillasDoblesSinEscapar() {
            // Arrange: Simulamos entrada de usuario con comillas internas
            String nombreConComillas = "Juan \"El Rápido\"";
            String jsonInvalido = JSONTiempoReal.generarJson(nombreConComillas, "Pérez", 30);

            // Act & Assert: La concatenacion manual no escapa comillas, rompiendo la sintaxis JSON
            assertThrows(
                    JsonParseException.class,
                    () -> JSONTiempoReal.interpretarJson(jsonInvalido),
                    "Debe fallar con JsonParseException al encontrarse con comillas no escapadas"
            );
        }
    }

    @Nested
    @DisplayName("Pruebas de Caja Blanca (Ramas y Modelo de Arbol)")
    class PruebasCajaBlanca {

        @ParameterizedTest
        @ValueSource(strings = {"", "   ", "\t\n"})
        @DisplayName("Rama: Cadenas vacias o en blanco -> debe lanzar IllegalArgumentException")
        void deberiaLanzarExcepcionCuandoCadenaJsonEstaVaciaOEnBlanco(String jsonEnBlanco) {
            assertThrows(
                    IllegalArgumentException.class,
                    () -> JSONTiempoReal.interpretarJson(jsonEnBlanco)
            );
        }

        @Test
        @DisplayName("Rama: Sintaxis truncada o incompleta -> debe lanzar JsonParseException")
        void deberiaLanzarJsonParseExceptionCuandoSintaxisJsonEstaIncompleta() {
            String jsonIncompleto = "{ \"nombre\": \"Carlos\", \"apellidos\": ";

            assertThrows(
                    JsonParseException.class,
                    () -> JSONTiempoReal.interpretarJson(jsonIncompleto)
            );
        }

        @Test
        @DisplayName("Rama: Verificacion de estructura -> el nodo raiz debe ser un objeto JSON (ObjectNode)")
        void deberiaIdentificarQueElNodoRaizEsDeTipoObjeto() throws IOException {
            String json = "{ \"nombre\": \"Ana\", \"apellidos\": \"López\", \"edad\": 25 }";

            JsonNode raiz = JSONTiempoReal.interpretarJson(json);

            assertTrue(raiz.isObject(), "El nodo raiz debe identificarse como un objeto");
            assertFalse(raiz.isArray(), "El nodo raiz no debe identificarse como array");
        }

        @Test
        @DisplayName("Rama: Acceso defensivo -> devolver nulo al consultar campos que no existen")
        void deberiaDevolverNuloAlConsultarCamposInexistentesEnElNodo() throws IOException {
            String json = "{ \"nombre\": \"Elena\", \"apellidos\": \"García\", \"edad\": 33 }";

            JsonNode raiz = JSONTiempoReal.interpretarJson(json);

            assertNull(raiz.get("telefono"), "Un campo inexistente debe devolver null en JsonNode");
            assertNull(raiz.get("direccion"));
        }

        @Test
        @DisplayName("Rama: Mapeo resiliente -> asignar valores por defecto si faltan campos en el nodo")
        void deberiaMapearCorrectamenteValoresPorDefectoSiFaltanCamposEnElNodo() throws IOException {
            // Arrange: JSON sin el campo 'edad' ni 'apellidos'
            String jsonParcial = "{ \"nombre\": \"SoloNombre\" }";
            JsonNode raiz = JSONTiempoReal.interpretarJson(jsonParcial);

            // Act
            Persona resultado = JSONTiempoReal.mapearDesdeNodo(raiz);

            // Assert
            assertEquals("SoloNombre", resultado.getNombre());
            assertEquals("", resultado.getApellidos(), "Campo ausente debe inicializarse como cadena vacia");
            assertEquals(0, resultado.getEdad(), "Edad ausente debe inicializarse en 0");
        }
    }
}
