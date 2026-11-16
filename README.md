# Investigación Práctica: Librería Jackson en Java

> Proyecto práctico desarrollado para el ciclo formativo de **Desarrollo de Aplicaciones Multiplataforma (DAM)**.  
> **Unidad**: Acceso a Datos / Manejo de Ficheros (UD02).  
> **Tema**: Interpretación y generación de JSON en tiempo real con la librería **Jackson**.

---

## 📋 Descripción del Proyecto

El objetivo de este proyecto es explorar, implementar y comparar las **tres metodologías principales** que ofrece el ecosistema Jackson para procesar documentos JSON en Java:

1. **Data Binding (Enlace de datos)**: Mapeo automático bidireccional entre JSON y objetos Java (POJOs).
2. **Tree Model (Modelo de árbol / DOM)**: Representación jerárquica en memoria con `JsonNode` sin necesidad de clases intermedias.
3. **Streaming API (Procesamiento incremental)**: Lectura y escritura de bajo nivel basada en eventos y tokens (`JsonParser` / `JsonGenerator`), optimizada para alto rendimiento y mínimo consumo de memoria.

Todos los programas operan sobre una colección de personas (`src/main/resources/persona.json`) compuesta por campos `nombre`, `apellidos` y `edad`.

---

## 📁 Estructura del Repositorio

```text
jackson-investigacion/
├── docs/                                                # Documentación y material de soporte
│   ├── Cruz_Edu_Miranda_Angel_T2_JSON.pptx              # Presentación visual del proyecto
│   ├── guia_tecnica_jackson.md                          # Guía exhaustiva de la práctica, métodos y comparativa
│   └── pruebas_unitarias_caja_blanca_caja_negra.md      # Documentación técnica de las 35 pruebas unitarias
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   ├── models/
│   │   │   │   └── Persona.java                         # Modelo POJO (equals, hashCode, toString)
│   │   │   ├── EscritorJSON.java                        # Data Binding: Escritura y acumulación
│   │   │   ├── LectorJSON.java                          # Data Binding: Lectura tipada
│   │   │   ├── JSONTiempoReal.java                      # Tree Model: Entrada manual e interpretación
│   │   │   └── JacksonStreaming.java                    # Streaming API: Parser y Generator
│   │   └── resources/
│   │       └── persona.json                             # Dataset de personas (10 registros variados)
│   └── test/
│       └── java/
│           ├── EscritorJSONTest.java                    # Tests de Caja Blanca y Negra (Data Binding - Escritura)
│           ├── JacksonStreamingTest.java                # Tests de Caja Blanca y Negra (Streaming API)
│           ├── JSONTiempoRealTest.java                  # Tests de Caja Blanca y Negra (Tree Model)
│           └── LectorJSONTest.java                      # Tests de Caja Blanca y Negra (Data Binding - Lectura)
├── .gitignore                                           # Exclusiones de build y dependencias
├── pom.xml                                              # Configuración Maven con JUnit 5 Jupiter
└── README.md                                            # Documentación principal del proyecto
```

---

## 🛠️ Tecnologías y Requisitos

* **Lenguaje**: Java 21 / 25 / 26
* **Gestor de dependencias**: Maven 3.x
* **Librería principal**: `com.fasterxml.jackson.core:jackson-databind:2.22.4` (incluye `jackson-core` y `jackson-annotations`)
* **IDE recomendado**: IntelliJ IDEA

### Dependencia Maven (`pom.xml`)

```xml
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
    <version>2.22.4</version>
</dependency>
```

---

## 🔍 Análisis de los Componentes

### 1. Data Binding (`EscritorJSON.java` y `LectorJSON.java`)
* **Clase modelo**: `models.Persona` con constructor por defecto (imprescindible para Jackson) y métodos getter/setter.
* **Escritura acumulativa**: `EscritorJSON` lee el fichero existente usando `new TypeReference<ArrayList<Persona>>() {}`. Añade la nueva persona y reescribe el array con `mapper.writeValue(...)` aplicando formato legible con `SerializationFeature.INDENT_OUTPUT`.
* **Lectura**: `LectorJSON` recupera la lista completa y mapea los registros directamente a instancias de `Persona`.

### 2. Tree Model (`JSONTiempoReal.java`)
* Solicita datos al usuario por consola mediante `Scanner`.
* Construye el documento JSON como `String` mediante concatenación.
* Parsea el contenido con `mapper.readTree(json)` obteniendo un `JsonNode` navegable (`raiz.get("campo").asText()`).
* **Nota didáctica**: Permite evidenciar el riesgo de inyección o fallo sintáctico cuando la entrada del usuario contiene comillas dobles (`"`).

### 3. Streaming API (`JacksonStreaming.java`)
* Utiliza `JsonFactory`, `JsonGenerator` y `JsonParser`.
* **Preservación de registros**: Carga el fichero en memoria previa a la apertura del generator y utiliza `gen.copyCurrentStructure(viejo)` para duplicar las personas existentes sin necesidad de instanciar objetos.
* **Tolerancia a esquemas antiguos**: Admite tanto ficheros que arrancan con un array (`[`), como ficheros legacy con un único objeto (`{}`).
* **Lectura eficiente**: Recorre el flujo token a token (`JsonToken.START_OBJECT`, `FIELD_NAME`, etc.) accediendo a valores primitivos mediante `parser.getText()` y `parser.getIntValue()`.

---

## 📊 Comparativa Técnica

| Característica | Data Binding | Tree Model (`JsonNode`) | Streaming API |
| :--- | :--- | :--- | :--- |
| **Clases intermedias (POJO)** | Obligatorias | No requeridas | No requeridas |
| **Consumo de memoria** | Medio (árbol de objetos) | Medio / Alto (árbol DOM) | **Mínimo** (constante) |
| **Rendimiento** | Rápido | Rápido | **Ultra rápido** |
| **Facilidad de uso** | Alta e intuitiva | Media | Compleja (control granular) |
| **Ideal para** | APIs REST, CRUD típico | JSONs dinámicos / desconocidos | Ficheros gigantes, IoT, streaming |

---

## 🚀 Puesta en Marcha

1. **Clonar o abrir el proyecto**:
   Abrir la carpeta del proyecto en IntelliJ IDEA y permitir la sincronización del `pom.xml`.

2. **Ejecutar las clases**:
   Cada clase contiene su método `main` ejecutable de forma independiente:
   * Ejecutar `EscritorJSON` para añadir un registro a `persona.json`.
   * Ejecutar `LectorJSON` para consultar la lista de personas registradas.
   * Ejecutar `JSONTiempoReal` para probar la entrada interactiva por consola.
   * Ejecutar `JacksonStreaming` para probar el flujo de bajo nivel con preservación de histórico.

---

## 💡 Problemas Encontrados y Soluciones

* **Localización de recursos en runtime**:
  * *Problema*: `getResource(...)` o rutas relativas simples generaban inconsistencias según el directorio de trabajo del launcher.
  * *Solución*: Estandarización mediante `Paths.get("src", "main", "resources", "persona.json")`.
* **Sobrescritura involuntaria**:
  * *Problema*: Al serializar un nuevo objeto se perdían los anteriores.
  * *Solución*: Implementación de lectura previa (`TypeReference` en Data Binding y `copyCurrentStructure` en Streaming).
* **MismatchedInputException**:
  * *Problema*: Excepción al intentar deserializar un objeto individual `{}` como una lista `[]`.
  * *Solución*: Comprobación condicional del token de entrada (`JsonToken.START_ARRAY` vs `JsonToken.START_OBJECT`).

---

## 🧪 Pruebas Unitarias (Caja Blanca y Caja Negra)

El proyecto cuenta con una suite integral de **35 pruebas unitarias** bajo **JUnit 5 (Jupiter)** y **Maven Surefire**, aplicando diseño **Clean Code** (patrón AAA, nombres descriptivos en español y aislamiento de ficheros con `@TempDir`):

* **1. Data Binding (`LectorJSONTest` y `EscritorJSONTest` - 12 tests)**:
  * *Caja Negra*: Particiones de equivalencia sobre el dataset de 10 personas, valores límite (0 y 102 años), ciclo *Round-trip* completo, acumulación secuencial y rechazo de nulos.
  * *Caja Blanca*: Cobertura de ramas para creación de subdirectorios, inicialización ante ficheros vacíos (0 bytes), formato indentado `INDENT_OUTPUT` y excepciones (`FileNotFoundException`, `MismatchedInputException` ante formato objeto en vez de array, `IOException`).
* **2. Tree Model (`JSONTiempoRealTest` - 13 tests)**:
  * *Caja Negra*: Generación de cadenas, navegación nodo a nodo con `JsonNode.get()`, valores límite y **demostración empírica del punto débil** (rotura por `JsonParseException` cuando el nombre contiene comillas dobles sin escapar).
  * *Caja Blanca*: Detección de cadenas en blanco, sintaxis truncada, validación de tipo `ObjectNode`, campos ausentes (`null`) y mapeo resiliente con valores por defecto.
* **3. Streaming API (`JacksonStreamingTest` - 10 tests)**:
  * *Caja Negra*: Flujo bidireccional streaming con `JsonGenerator` y `JsonParser`, preservación secuencial y **demostración de la ventaja sobre el Tree Model** (`JsonGenerator` escapa comillas automáticamente sin corromper el documento).
  * *Caja Blanca*: Emisión del array inicial `START_ARRAY`, duplicación de arrays previos con `copyCurrentStructure`, migración transparente de ficheros antiguos de un solo objeto `{}` a lista `[]`, y resiliencia ante campos desconocidos con `skipChildren()`.

> Consulta la memoria exhaustiva, diagramas y matrices de prueba en [docs/pruebas_unitarias_caja_blanca_caja_negra.md](docs/pruebas_unitarias_caja_blanca_caja_negra.md).

### Ejecución de Pruebas

```powershell
mvn test
```

---

## 👥 Autores
* **Eduardo Cruz Muñoz**
* **Ángel Miranda**
