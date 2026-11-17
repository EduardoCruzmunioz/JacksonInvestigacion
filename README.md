# Investigación de la Librería Jackson en Java

> **Módulo**: Acceso a Datos / Manejo de Ficheros (UD02)  
> **Ciclo**: Desarrollo de Aplicaciones Multiplataforma (DAM)  
> **Tema**: Interpretación y generación de JSON en tiempo real con la librería **Jackson**  
> **Repositorio oficial**: [https://github.com/EduardoCruzmunioz/JacksonInvestigacion](https://github.com/EduardoCruzmunioz/JacksonInvestigacion)  
> **Repositorio colaborativo original**: [https://github.com/Angeell04/edu_angel_actividadGrupos](https://github.com/Angeell04/edu_angel_actividadGrupos)

---

## 📋 Información del Proyecto

Este repositorio contiene el desarrollo práctico y la investigación comparativa sobre el procesamiento de JSON en tiempo real en Java mediante la librería **Jackson**.

Se implementan y contrastan las **tres metodologías oficiales** ofrecidas por el ecosistema Jackson:

1. **Data Binding (Alto nivel)**: Serialización y deserialización automática bidireccional entre documentos JSON y objetos Java (`models.Persona`) utilizando `ObjectMapper` y `TypeReference`.
2. **Tree Model / Modelo de Árbol (Nivel intermedio)**: Representación jerárquica en memoria con `JsonNode` sin necesidad de clases POJO.
3. **Streaming API (Bajo nivel / Alto rendimiento)**: Lectura y escritura basada en eventos y flujo continuo de tokens (`JsonParser` y `JsonGenerator`), optimizada para mínimo consumo de memoria y procesamiento ultra rápido.

Todos los programas operan sobre una colección persistente en disco: `src/main/resources/persona.json`.

---

## 📁 Estructura del Repositorio

```text
JacksonInvestigacion/
├── docs/                                                # Documentación técnica y recursos
│   ├── Cruz_Edu_Miranda_Angel_T2_JSON.pptx              # Presentación en diapositivas para la defensa
│   └── documentacion.md                                 # Memoria técnica completa, guía y pruebas
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   ├── models/
│   │   │   │   └── Persona.java                         # Modelo POJO (constructores, getters/setters, equals/hashCode)
│   │   │   ├── EscritorJSON.java                        # Data Binding: Escritura y acumulación
│   │   │   ├── LectorJSON.java                          # Data Binding: Lectura tipada
│   │   │   ├── JSONTiempoReal.java                      # Tree Model: Entrada manual e interpretación
│   │   │   └── JacksonStreaming.java                    # Streaming API: Parser y Generator de bajo nivel
│   │   └── resources/
│   │       └── persona.json                             # Dataset acumulativo de personas (JSON Array)
│   └── test/
│       └── java/
│           ├── EscritorJSONTest.java                    # Tests Caja Blanca y Caja Negra (Escritura)
│           ├── LectorJSONTest.java                      # Tests Caja Blanca y Negra (Lectura)
│           ├── JSONTiempoRealTest.java                  # Tests Caja Blanca y Negra (Tree Model)
│           └── JacksonStreamingTest.java                # Tests Caja Blanca y Negra (Streaming API)
├── .gitignore                                           # Exclusiones de build, IDE y ficheros privados
├── pom.xml                                              # Configuración Maven con JUnit 5 Jupiter
└── README.md                                            # Información general del proyecto
```

---

## 🛠️ Tecnologías y Requisitos

* **Lenguaje**: Java 21 LTS / OpenJDK 26
* **Gestor de dependencias**: Apache Maven 3.9+
* **Librerías principales**:
  * `com.fasterxml.jackson.core:jackson-databind:2.22.4` (incluye `jackson-core` y `jackson-annotations`)
  * `org.junit.jupiter:junit-jupiter:5.11.3` (ámbito de pruebas)
* **IDE recomendado**: IntelliJ IDEA

---

## 🚀 Guía Rápida de Ejecución

### 1. Ejecutar las Aplicaciones (IntelliJ IDEA)
Cada clase contiene un método `main` ejecutable de forma independiente:
* **`EscritorJSON`**: Agrega un registro acumulándolo en `src/main/resources/persona.json`.
* **`LectorJSON`**: Lee e imprime por consola todas las personas almacenadas.
* **`JSONTiempoReal`**: Solicita interactivamente los datos por teclado y procesa el JSON en memoria.
* **`JacksonStreaming`**: Pide datos, acumula en streaming y parsea token a token.

### 2. Ejecutar la Suite de Pruebas Unitarias
El proyecto cuenta con **35 pruebas unitarias** (Caja Blanca y Caja Negra) ejecutables mediante Maven:

```powershell
mvn test
```

> **Resultado**: 35 tests ejecutados, 0 fallos, 0 errores (`BUILD SUCCESS`).

---

## 📖 Documentación Completa del Proyecto

Para consultar toda la memoria académica y técnica unificada, accede a:

👉 **[docs/documentacion.md](docs/documentacion.md)**

Allí encontrarás en detalle:
* Instrucciones pormenorizadas de ejecución y requisitos de entorno.
* Análisis de código documentado bajo estándares **Clean Code** y justificación de decisiones de diseño.
* Tablas de pruebas formales (casos normales, casos límite y entradas incorrectas / ficheros inexistentes).
* Matriz exhaustiva de las 35 pruebas unitarias de Caja Blanca y Caja Negra.
* Evidencias empíricas de ejecución con Maven Surefire.
* Historial de problemas encontrados y su resolución técnica.
* Comparativa multidimensional de las tres APIs de Jackson.

---

## 👥 Autores y Proyecto Colaborativo

* **Eduardo Cruz Muñoz** ([@EduardoCruzmunioz](https://github.com/EduardoCruzmunioz))
* **Ángel Miranda** ([@Angeell04](https://github.com/Angeell04))

> 🔗 Este proyecto forma parte de la actividad grupal de DAM realizada conjuntamente. Puedes consultar también el repositorio de trabajo colaborativo en: [Angeell04/edu_angel_actividadGrupos](https://github.com/Angeell04/edu_angel_actividadGrupos).
