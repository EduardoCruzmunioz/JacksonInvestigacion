# Memoria de Entrega: Pruebas y Documentación

> **Módulo**: Acceso a Datos / Manejo de Ficheros (UD02)  
> **Ciclo**: Desarrollo de Aplicaciones Multiplataforma (DAM)  
> **Alumnos**: Eduardo Cruz Muñoz y Ángel Miranda  
> **Repositorio del proyecto**: [https://github.com/EduardoCruzmunioz/JacksonInvestigacion](https://github.com/EduardoCruzmunioz/JacksonInvestigacion)  
> **Repositorio colaborativo grupal**: [https://github.com/Angeell04/edu_angel_actividadGrupos](https://github.com/Angeell04/edu_angel_actividadGrupos)

---

## Índice General

1. [Introducción y Tareas Seleccionadas](#1-introducción-y-tareas-seleccionadas)
2. [Aplicación 1: Persistencia y Consulta con Data Binding (`EscritorJSON` y `LectorJSON`)](#2-aplicación-1-persistencia-y-consulta-con-data-binding-escritorjson-y-lectorjson)
   - [2.1 Instrucciones de Ejecución](#21-instrucciones-de-ejecución)
   - [2.2 Código Documentado y Decisiones de Diseño](#22-código-documentado-y-decisiones-de-diseño)
   - [2.3 Tabla de Pruebas](#23-tabla-de-pruebas)
   - [2.4 Evidencias y Corrección de Errores](#24-evidencias-y-corrección-de-errores)
3. [Aplicación 2: Procesamiento Incremental con Streaming API (`JacksonStreaming`)](#3-aplicación-2-procesamiento-incremental-con-streaming-api-jacksonstreaming)
   - [3.1 Instrucciones de Ejecución](#31-instrucciones-de-ejecución)
   - [3.2 Código Documentado y Decisiones de Diseño](#32-código-documentado-y-decisiones-de-diseño)
   - [3.3 Tabla de Pruebas](#33-tabla-de-pruebas)
   - [3.4 Evidencias y Corrección de Errores](#34-evidencias-y-corrección-de-errores)
4. [Resumen Global de Aseguramiento de Calidad (JUnit 5 + Maven Surefire)](#4-resumen-global-de-aseguramiento-de-calidad-junit-5--maven-surefire)

---

## 1. Introducción y Tareas Seleccionadas

Para el apartado de investigación de la librería **JACKSON** (generación e interpretación de JSON en tiempo real), se han seleccionado las dos aplicaciones principales que gestionan el ciclo completo de lectura, escritura y acumulación persistente de registros en disco:

1. **Aplicación 1 (Data Binding de Alto Nivel)**: Compuesta por `EscritorJSON` y `LectorJSON`, apoyados en el modelo de dominio `models.Persona`.
2. **Aplicación 2 (Streaming API de Bajo Nivel)**: Implementada en `JacksonStreaming`, utilizando generador y parser basados en flujo de tokens continuos.

Ambas aplicaciones comparten el mismo almacén de datos físico (`src/main/resources/persona.json`), permitiendo contrastar de forma empírica la comodidad del enlace de datos frente al rendimiento y consumo de memoria del streaming.

---

## 2. Aplicación 1: Persistencia y Consulta con Data Binding (`EscritorJSON` y `LectorJSON`)

### 2.1 Instrucciones de Ejecución

* **Finalidad**: Permitir la persistencia acumulativa de registros de personas y su posterior consulta e instanciación directa en colecciones de objetos Java (`List<Persona>`), sin requerir serialización nativa de Java (`Serializable`).
* **Requisitos y Librerías**:
  * Java JDK 21 LTS o superior (desarrollado y verificado en OpenJDK 26).
  * Apache Maven 3.9+.
  * Dependencia: `com.fasterxml.jackson.core:jackson-databind:2.22.4`.
* **Ficheros de entrada**:
  * Ruta: `src/main/resources/persona.json`.
  * Formato: Array JSON (`[ { "nombre": "...", "apellidos": "...", "edad": ... }, ... ]`). Si el archivo no existe o está vacío (0 bytes), la aplicación lo inicializa automáticamente.
* **Pasos para ejecutarla**:
  1. **Desde IntelliJ IDEA**:
     * Para escribir: Abrir `src/main/java/EscritorJSON.java` y pulsar botón verde *Run 'EscritorJSON.main()'*.
     * Para leer: Abrir `src/main/java/LectorJSON.java` y pulsar botón verde *Run 'LectorJSON.main()'*.
  2. **Desde Terminal (PowerShell)**:
     ```powershell
     cd "C:\Users\educr\Desktop\JAVA\JacksonInvestigacion"
     $env:JAVA_HOME = "C:\Users\educr\.jdks\openjdk-26.0.1"
     # Compilar y ejecutar escritor
     mvn compile
     java -cp "target/classes;target/dependency/*;C:\Users\educr\.m2\repository\com\fasterxml\jackson\core\jackson-databind\2.22.4\jackson-databind-2.22.4.jar;C:\Users\educr\.m2\repository\com\fasterxml\jackson\core\jackson-core\2.22.4\jackson-core-2.22.4.jar;C:\Users\educr\.m2\repository\com\fasterxml\jackson\core\jackson-annotations\2.22.4\jackson-annotations-2.22.4.jar" EscritorJSON
     ```
* **Resultados que genera**:
  * En consola: `EscritorJSON` imprime el total de registros consolidados (`Registros en el fichero: N`). `LectorJSON` imprime cada registro formateado (`Nombre Apellidos, Edad`).
  * En disco: Actualiza `persona.json` agregando la nueva persona al final del array JSON con sangría e indentación legible (*pretty printing*).

---

### 2.2 Código Documentado y Decisiones de Diseño

#### A. Modelo de Dominio (`models/Persona.java`)

```java
package models;

import java.util.Objects;

/**
 * Representa la entidad Persona en el dominio del negocio.
 * Diseñada siguiendo el estandar JavaBean / POJO exigido por Jackson.
 */
public class Persona {
    private String nombre;
    private String apellidos;
    private int edad;

    // Decision de diseno: Constructor sin argumentos obligatorio.
    // Jackson requiere instanciar la clase por reflexion antes de invocar setters.
    public Persona() { }

    public Persona(String nombre, String apellidos, int edad) {
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.edad = edad;
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }

    public int getEdad() { return edad; }
    public void setEdad(int edad) { this.edad = edad; }

    // Decision de diseno: Implementacion de equals y hashCode
    // Permite verificar la igualdad exacta de objetos en los tests unitarios.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Persona persona = (Persona) o;
        return edad == persona.edad &&
                Objects.equals(nombre, persona.nombre) &&
                Objects.equals(apellidos, persona.apellidos);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nombre, apellidos, edad);
    }
}
```

#### B. Servicio de Escritura Acumulativa (`EscritorJSON.java`)

```java
public class EscritorJSON {
    // Reutilizacion del ObjectMapper configurado con salida indentada
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    /**
     * Anade una persona asegurando persistencia acumulativa.
     * Decision de diseno: Se lee la coleccion previa antes de escribir para no perder registros.
     */
    public static List<Persona> agregarPersona(Path ruta, Persona nueva) throws IOException {
        Objects.requireNonNull(ruta, "La ruta del fichero no puede ser nula");
        Objects.requireNonNull(nueva, "El registro de persona a guardar no puede ser nulo");

        if (ruta.getParent() != null) {
            Files.createDirectories(ruta.getParent());
        }

        File archivo = ruta.toFile();
        ArrayList<Persona> personas = new ArrayList<>();

        // Decision tecnica: TypeReference<ArrayList<Persona>> resuelve el 'type erasure' de Java
        if (archivo.exists() && archivo.length() > 0) {
            personas = MAPPER.readValue(archivo, new TypeReference<ArrayList<Persona>>() {});
        }

        personas.add(nueva);
        MAPPER.writeValue(archivo, personas);
        return personas;
    }
}
```

---

### 2.3 Tabla de Pruebas

| Caso / Tipo | Entrada Utilizada | Resultado Esperado | Resultado Obtenido | Valoración |
| :--- | :--- | :--- | :--- | :--- |
| **1. Funcionamiento Normal** (Round-Trip) | `Persona("Laura", "Gómez Serrano", 29)` sobre fichero existente | El registro se anexa al final de la lista, el fichero tiene $N+1$ elementos y `LectorJSON` lo recupera con `equals()` idéntico. | Registro guardado con formato indentado; `LectorJSON` recupera `Laura Gómez Serrano, 29`. | **CORRECTO** |
| **2. Caso Límite** (Valores Límite de Edad y Caracteres Especiales) | Recién nacido (`edad = 0`, `Mateo Peña Nieto`) y centenario (`edad = 102`, `Antonio O'Connor Jiménez`) | Deserialización exacta sin desbordamiento numérico ni problemas con caracteres especiales (tildes, eñes, apóstrofes). | Mapeo exacto: `edad = 0`, `edad = 102`, conservando tildes y apóstrofes bajo UTF-8. | **CORRECTO** |
| **3. Entrada Incorrecta / Fichero Inexistente** | Lectura sobre ruta garantizada como inexistente (`tempDir/inexistente.json`) | Interrupción controlada con lanzamiento explícito de `FileNotFoundException` (evitando NullPointerExceptions genéricos). | Lanza `FileNotFoundException: El archivo no existe en la ruta...` capturado en suite de tests. | **CORRECTO** |

---

### 2.4 Evidencias y Corrección de Errores

#### Evidencia de Ejecución en Consola (`LectorJSONTest` y `EscritorJSONTest`):

```text
[INFO] Running EscritorJSONTest$PruebasCajaBlanca
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running EscritorJSONTest$PruebasCajaNegra
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running LectorJSONTest$PruebasCajaBlanca
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running LectorJSONTest$PruebasCajaNegra
[INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
```

#### Errores Detectados y Corrección Aplicada:
1. **Error de Sobrescritura de Registros**:
   * *Diagnóstico*: En la versión inicial, `mapper.writeValue(f, persona)` escribía un único objeto, borrando las personas previas en cada ejecución.
   * *Corrección*: Se rediseñó el flujo a lectura previa con `TypeReference<ArrayList<Persona>>()`, adición a la lista en memoria y reescritura atómica de la lista completa.
   * *Comprobación*: Test `deberiaPreservarRegistrosPreviosAlAgregarNuevaPersona` validó que tras dos inserciones sucesivas coexisten ambos registros.
2. **Excepción `MismatchedInputException`**:
   * *Diagnóstico*: Si el archivo contenía un objeto `{ ... }` legacy, el lector fallaba esperando un array `[ ... ]`.
   * *Corrección*: Se normalizó el dataset de recursos a lista `[]` y se añadieron aserciones específicas en los tests.

---

## 3. Aplicación 2: Procesamiento Incremental con Streaming API (`JacksonStreaming`)

### 3.1 Instrucciones de Ejecución

* **Finalidad**: Generar y parsear documentos JSON de forma secuencial y en tiempo real token a token, consumiendo el mínimo de memoria RAM y permitiendo el procesado de flujos masivos de datos.
* **Requisitos y Librerías**:
  * Java JDK 21+ / OpenJDK 26.
  * Módulo `com.fasterxml.jackson.core` (`JsonFactory`, `JsonGenerator`, `JsonParser`, `JsonToken`).
* **Ficheros de entrada**:
  * `src/main/resources/persona.json` (o entrada interactiva por consola vía teclado).
* **Pasos para ejecutarla**:
  1. Ejecutar `JacksonStreaming.main()`.
  2. Introducir secuencialmente por consola los datos solicitados:
     * `Introduce el nombre:`
     * `Introduce los apellidos:`
     * `Introduce la edad:`
* **Resultados que genera**:
  * En disco: Escribe directamente mediante `JsonGenerator` el array JSON actualizado, escapando automáticamente caracteres reservados (como comillas dobles).
  * En consola: `JsonParser` recorre el stream evento por evento y muestra la reconstrucción de cada persona procesada.

---

### 3.2 Código Documentado y Decisiones de Diseño

#### Procesamiento en Streaming (`JacksonStreaming.java`)

```java
public class JacksonStreaming {
    private static final JsonFactory FACTORY = new JsonFactory();

    /**
     * Escribe la persona en streaming utilizando JsonGenerator.
     * Decision de diseno: Se lee el contenido anterior en un String antes de abrir el generator
     * para evitar colisiones de E/S bloqueantes en el mismo fichero.
     */
    public static void agregarPersonaStreaming(Path ruta, String nombre, String apellidos, int edad) throws IOException {
        Objects.requireNonNull(ruta, "La ruta del fichero no puede ser nula");

        String anterior = Files.exists(ruta) ? Files.readString(ruta) : "";

        try (JsonGenerator gen = FACTORY.createGenerator(ruta.toFile(), JsonEncoding.UTF8)) {
            gen.useDefaultPrettyPrinter();
            gen.writeStartArray();

            // Decision de diseno: copyCurrentStructure() clona nodos directamente
            // del parser al generador sin crear objetos intermedios en memoria RAM.
            if (!anterior.isBlank()) {
                try (JsonParser viejo = FACTORY.createParser(anterior)) {
                    JsonToken tokenInicial = viejo.nextToken();
                    if (tokenInicial == JsonToken.START_ARRAY) {
                        while (viejo.nextToken() == JsonToken.START_OBJECT) {
                            gen.copyCurrentStructure(viejo);
                        }
                    } else if (tokenInicial == JsonToken.START_OBJECT) {
                        // Compatibilidad con ficheros legacy de un unico objeto
                        gen.copyCurrentStructure(viejo);
                    }
                }
            }

            // Emision streaming de la nueva entidad
            gen.writeStartObject();
            gen.writeStringField("nombre", nombre);
            gen.writeStringField("apellidos", apellidos);
            gen.writeNumberField("edad", edad);
            gen.writeEndObject();

            gen.writeEndArray();
        }
    }

    /**
     * Lectura eficiente token a token con JsonParser y evaluacion con switch.
     */
    public static List<Persona> leerPersonasStreamingDesdeParser(JsonParser parser) throws IOException {
        List<Persona> personas = new ArrayList<>();
        parser.nextToken(); // Salta al primer token

        while (parser.nextToken() == JsonToken.START_OBJECT) {
            String nombre = "";
            String apellidos = "";
            int edad = 0;

            while (parser.nextToken() != JsonToken.END_OBJECT) {
                String campo = parser.currentName();
                parser.nextToken(); // Salta al token de valor

                switch (campo) {
                    case "nombre" -> nombre = parser.getText();
                    case "apellidos" -> apellidos = parser.getText();
                    case "edad" -> edad = parser.getIntValue();
                    // Decision de diseno: skipChildren() otorga tolerancia ante esquemas futuros
                    default -> parser.skipChildren();
                }
            }
            personas.add(new Persona(nombre, apellidos, edad));
        }
        return personas;
    }
}
```

---

### 3.3 Tabla de Pruebas

| Caso / Tipo | Entrada Utilizada | Resultado Esperado | Resultado Obtenido | Valoración |
| :--- | :--- | :--- | :--- | :--- |
| **1. Funcionamiento Normal** | `nombre="Mario"`, `apellidos="Torredelcampo"`, `edad=19` | Emisión continua del token `START_OBJECT`, campos escapados y cierre con `END_OBJECT`. | Fichero generado correctamente; `leerPersonasStreaming` devuelve `Persona("Mario", "Torredelcampo", 19)`. | **CORRECTO** |
| **2. Caso Límite** (Nombres con comillas dobles y caracteres UTF-8) | `nombre="Juan \"El Rápido\""`, `apellidos="Pérez"`, `edad=35` | A diferencia de la concatenación manual, `JsonGenerator` debe escapar automáticamente `\"` generando un JSON válido. | Parseo exitoso; el nombre se recupera íntegro como `Juan "El Rápido"` sin lanzar `JsonParseException`. | **CORRECTO** |
| **3. Entrada Incorrecta / Formato Legacy** | Fichero existente con formato de objeto único (`{ "nombre": "Angel", ... }`) | Migración automática a lista (`[ ... ]`) acumulando la nueva persona sin fallar por `MismatchedInputException`. | La rama `else if (tokenInicial == START_OBJECT)` duplica el objeto previo y añade el nuevo dentro de `[ ]`. | **CORRECTO** |

---

### 3.4 Evidencias y Corrección de Errores

#### Evidencia de Ejecución de Pruebas (`JacksonStreamingTest`):

```text
[INFO] Running JacksonStreamingTest$PruebasCajaBlanca
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0
[INFO] Running JacksonStreamingTest$PruebasCajaNegra
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0
```

#### Errores Detectados y Corrección Aplicada:
1. **Bloqueo de Fichero por Lectura y Escritura Concurrente**:
   * *Diagnóstico*: Al intentar abrir simultáneamente un `JsonParser` para leer el fichero físico y un `JsonGenerator` sobre la misma ruta, el sistema de ficheros de Windows arrojaba error de bloqueo de proceso.
   * *Corrección*: Se implementó la lectura previa del histórico a memoria (`Files.readString(ruta)`), cerrando el flujo antes de crear el `JsonGenerator`.
2. **Corrupción por Comillas en Entrada Manual**:
   * *Diagnóstico*: En `JSONTiempoReal`, la concatenación `"{\"nombre\": \"" + nombre + "\"}"` provocaba `JsonParseException` si el usuario ingresaba comillas.
   * *Corrección*: Se demostró en el test comparativo que `JacksonStreaming` resuelve este defecto de raíz al utilizar `gen.writeStringField()`, que sanitiza y escapa automáticamente cualquier carácter conflictivo.

---

## 4. Resumen Global de Aseguramiento de Calidad (JUnit 5 + Maven Surefire)

La verificación empírica de todo el sistema se consolida mediante **35 pruebas unitarias automatizadas** que cubren exhaustivamente las clases analizadas:

```text
[INFO] -------------------------------------------------------
[INFO]  T E S T S
[INFO] -------------------------------------------------------
[INFO] Running EscritorJSONTest (6 tests) -> 0 failures, 0 errors
[INFO] Running LectorJSONTest (6 tests)   -> 0 failures, 0 errors
[INFO] Running JSONTiempoRealTest (13 tests) -> 0 failures, 0 errors
[INFO] Running JacksonStreamingTest (10 tests) -> 0 failures, 0 errors
[INFO] 
[INFO] Results:
[INFO] Tests run: 35, Failures: 0, Errors: 0, Skipped: 0
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

Todas las pruebas están integradas en el pipeline de Maven y pueden ser reproducidas en cualquier estación de trabajo ejecutando `mvn test`.
