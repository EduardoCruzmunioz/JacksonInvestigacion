# Jackson: interpretación y generación de JSON en tiempo real

Documento de la práctica *"Investiga la librería JACKSON: interpretación / generación de JSON en tiempo real"*. Recoge cuatro programas y explica cómo funciona cada uno.

---

## Índice

1. [Introducción a Jackson](#1-introducción-a-jackson)
2. [Programa 1: `JSONTiempoReal` (JSON como texto + `readTree`)](#2-programa-1-jsontiemporeal-json-como-texto--readtree)
   - [2.1 Código](#21-código)
   - [2.2 Cómo funciona](#22-cómo-funciona)
   - [2.3 Ejemplo de ejecución](#23-ejemplo-de-ejecución)
   - [2.4 Limitaciones](#24-limitaciones)
3. [Programa 2: `JacksonStreaming` (Streaming API y fichero acumulativo)](#3-programa-2-jacksonstreaming-streaming-api-y-fichero-acumulativo)
   - [3.1 Código](#31-código)
   - [3.2 Piezas de Jackson que se usan](#32-piezas-de-jackson-que-se-usan)
   - [3.3 Paso a paso](#33-paso-a-paso)
   - [3.4 Evolución del fichero](#34-evolución-del-fichero)
   - [3.5 Resumen del flujo](#35-resumen-del-flujo)
4. [Programas 3 y 4: `EscritorJSON` y `LectorJSON` (data binding)](#4-programas-3-y-4-escritorjson-y-lectorjson-data-binding)
   - [4.1 La clase `Persona`](#41-la-clase-persona)
   - [4.2 `EscritorJSON`: código](#42-escritorjson-código)
   - [4.3 `EscritorJSON`: cómo funciona](#43-escritorjson-cómo-funciona)
   - [4.4 `LectorJSON`: código](#44-lectorjson-código)
   - [4.5 `LectorJSON`: cómo funciona](#45-lectorjson-cómo-funciona)
   - [4.6 Evolución del fichero y uso conjunto](#46-evolución-del-fichero-y-uso-conjunto)
   - [4.7 Errores típicos](#47-errores-típicos)
5. [Comparativa de los cuatro programas](#5-comparativa-de-los-cuatro-programas)
6. [Notas finales](#6-notas-finales)

---

## 1. Introducción a Jackson

Jackson es una librería de Java para trabajar con JSON. Ofrece tres formas de uso:

| Forma | Clases principales | Descripción |
|---|---|---|
| **Streaming API** | `JsonParser`, `JsonGenerator` | Lee/escribe el JSON token a token. Es la más rápida y la que menos memoria usa. |
| **Modelo de árbol** | `ObjectMapper.readTree`, `JsonNode` | Carga el JSON como un árbol de nodos que se recorre con `get(...)`. No necesita clases propias. |
| **Data binding** | `ObjectMapper.readValue` / `writeValue` | Convierte JSON a objetos Java y al revés. Se usa solo en `EscritorJSON` y `LectorJSON` (apartado 4). |

Los programas 1 y 2 (`JSONTiempoReal` y `JacksonStreaming`) trabajan **sin clases propias**: no hay clase `Persona`. Los programas 3 y 4 (`EscritorJSON` y `LectorJSON`) usan *data binding* con la clase `Persona`. En ningún caso se usa `Serializable` (serialización nativa de Java).

Dependencias necesarias (Maven):

```xml
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
    <version>2.22.4</version>
</dependency>
```

`jackson-databind` ya incluye `jackson-core` (Streaming API) y `jackson-annotations`.

---

## 2. Programa 1: `JSONTiempoReal` (JSON como texto + `readTree`)

Pide los datos por consola, **genera** el JSON como un `String` y después lo **interpreta** leyendo cada campo.

### 2.1 Código

```java
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Scanner;

public class JSONTiempoReal {
    public static void main(String[] args) throws Exception {
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
```

### 2.2 Cómo funciona

**Paso 1: Generación.**
Se leen tres datos con `Scanner`. La edad se convierte a `int` con `Integer.parseInt`. Después se concatenan las partes para formar el texto JSON:

- Las cadenas (`nombre`, `apellidos`) van entre comillas, escapadas con `\"` dentro del `String` de Java.
- La edad es un número, así que va **sin comillas**.

El resultado es un `String` con el JSON, que se muestra por pantalla. En este punto es solo texto: Java no sabe que es JSON.

**Paso 2: Interpretación.**

```java
ObjectMapper mapper = new ObjectMapper();
JsonNode raiz = mapper.readTree(json);
```

- `ObjectMapper.readTree` analiza el texto y construye un **árbol de nodos** en memoria. Si el texto no es JSON válido, lanza una excepción.
- `raiz` es un `JsonNode` que representa el objeto completo.
- `raiz.get("nombre")` devuelve el nodo del campo `nombre`. Sobre ese nodo se elige cómo leer el valor:
  - `asText()` para cadenas.
  - `asInt()` para números enteros.

### 2.3 Ejemplo de ejecución

```
Nombre: Edu
Apellidos: Cruz Muñoz
Edad: 22

JSON generado:
{ "nombre": "Edu", "apellidos": "Cruz Muñoz", "edad": 22 }

JSON interpretado:
nombre = Edu
apellidos = Cruz Muñoz
edad = 22
```

### 2.4 Limitaciones

- **El JSON se construye a mano.** Si el nombre contiene comillas (por ejemplo `O"Brien`) o una barra invertida, el texto resultante no es JSON válido y `readTree` fallará. Para generarlo de forma segura se usa `JsonGenerator` (programa 2).
- **No guarda nada en fichero**: el JSON solo existe mientras corre el programa.
- Solo maneja una persona por ejecución.
- `readTree` es del modelo de árbol (módulo `databind`), no del Streaming API puro, aunque no usa serialización a clases.

---

## 3. Programa 2: `JacksonStreaming` (Streaming API y fichero acumulativo)

Usa el **Streaming API** para guardar personas en `src/main/resources/persona.json`. Cada vez que se ejecuta:

1. Pide los datos de una persona por consola.
2. Guarda en el fichero las personas que ya había **más** la nueva.
3. Lee el fichero completo y muestra todas las personas.

### 3.1 Código

```java
import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class JacksonStreaming {
    public static void main(String[] args) throws IOException {
        JsonFactory factory = new JsonFactory();
        Scanner s = new Scanner(System.in);

        Path ruta = Paths.get("src", "main", "resources", "persona.json");
        Files.createDirectories(ruta.getParent());

        // 0. Pedimos los datos antes de tocar el fichero
        System.out.print("Introduce el nombre: ");
        String nombre = s.nextLine();
        System.out.print("Introduce los apellidos: ");
        String apellidos = s.nextLine();
        System.out.print("Introduce la edad: ");
        int edad = Integer.parseInt(s.nextLine());

        // 1. Guardamos el contenido anterior (no se puede leer y escribir el mismo fichero a la vez)
        String anterior = Files.exists(ruta) ? Files.readString(ruta) : "";

        // 2. GENERACIÓN: reescribimos lo anterior + la persona nueva
        try (JsonGenerator gen = factory.createGenerator(ruta.toFile(), JsonEncoding.UTF8)) {
            gen.useDefaultPrettyPrinter();
            gen.writeStartArray();

            if (!anterior.isBlank()) {
                try (JsonParser viejo = factory.createParser(anterior)) {
                    JsonToken t = viejo.nextToken();
                    if (t == JsonToken.START_ARRAY) {
                        while (viejo.nextToken() == JsonToken.START_OBJECT) {
                            gen.copyCurrentStructure(viejo);   // copia cada persona existente
                        }
                    } else if (t == JsonToken.START_OBJECT) {
                        gen.copyCurrentStructure(viejo);       // fichero antiguo con un solo objeto
                    }
                }
            }

            gen.writeStartObject();
            gen.writeStringField("nombre", nombre);
            gen.writeStringField("apellidos", apellidos);
            gen.writeNumberField("edad", edad);
            gen.writeEndObject();

            gen.writeEndArray();
        }
        System.out.println("JSON guardado en: " + ruta.toAbsolutePath());

        // 3. INTERPRETACIÓN: se lee el fichero token a token
        System.out.println("\nJSON interpretado:");
        try (JsonParser parser = factory.createParser(ruta.toFile())) {
            parser.nextToken(); // START_ARRAY
            while (parser.nextToken() == JsonToken.START_OBJECT) {
                while (parser.nextToken() != JsonToken.END_OBJECT) {
                    String campo = parser.currentName();
                    parser.nextToken();
                    switch (campo) {
                        case "nombre" -> System.out.println("nombre = " + parser.getText());
                        case "apellidos" -> System.out.println("apellidos = " + parser.getText());
                        case "edad" -> System.out.println("edad = " + parser.getIntValue());
                    }
                }
                System.out.println("---");
            }
        }
    }
}
```

### 3.2 Piezas de Jackson que se usan

| Clase | Para qué sirve |
|---|---|
| `JsonFactory` | Fábrica que crea generadores y parsers. |
| `JsonGenerator` | **Escribe** JSON elemento a elemento (`writeStartObject`, `writeStringField`...). |
| `JsonParser` | **Lee** JSON como una secuencia de tokens. |
| `JsonToken` | Tipo de cada token: `START_OBJECT`, `FIELD_NAME`, `VALUE_STRING`, `END_ARRAY`... |
| `JsonEncoding` | Codificación al escribir en fichero (`UTF8`). |

### 3.3 Paso a paso

#### Paso 0: Preparación y datos

```java
JsonFactory factory = new JsonFactory();
Scanner s = new Scanner(System.in);

Path ruta = Paths.get("src", "main", "resources", "persona.json");
Files.createDirectories(ruta.getParent());
```

- La ruta es relativa al directorio de trabajo (en IntelliJ, la raíz del proyecto).
- `createDirectories` crea la carpeta `resources` si no existe.
- Los datos se piden **antes** de abrir el generador. Así, si el usuario se equivoca (letras en la edad, `NumberFormatException`), el fichero no se queda a medias ni corrupto.

#### Paso 1: Guardar el contenido anterior en memoria

```java
String anterior = Files.exists(ruta) ? Files.readString(ruta) : "";
```

Al crear un `JsonGenerator` sobre un fichero, este se **vacía**. Si no se guardara antes su contenido, se perderían las personas anteriores. Además, no se puede leer y escribir el mismo fichero a la vez, por eso se lee entero a un `String` primero.

#### Paso 2: Generación (lo anterior + la persona nueva)

```java
try (JsonGenerator gen = factory.createGenerator(ruta.toFile(), JsonEncoding.UTF8)) {
    gen.useDefaultPrettyPrinter();
    gen.writeStartArray();
    ...
    gen.writeEndArray();
}
```

- `useDefaultPrettyPrinter()` genera el JSON con saltos de línea e indentación.
- `writeStartArray()` abre el `[` que envuelve a todas las personas y `writeEndArray()` lo cierra con `]`.
- El `try` con recursos cierra el generador al terminar, y es lo que vuelca el contenido al fichero.

**2.1 Copiar lo que ya había**

```java
if (!anterior.isBlank()) {
    try (JsonParser viejo = factory.createParser(anterior)) {
        JsonToken t = viejo.nextToken();
        if (t == JsonToken.START_ARRAY) {
            while (viejo.nextToken() == JsonToken.START_OBJECT) {
                gen.copyCurrentStructure(viejo);
            }
        } else if (t == JsonToken.START_OBJECT) {
            gen.copyCurrentStructure(viejo);
        }
    }
}
```

- Si no había nada (fichero nuevo o vacío), este bloque se omite.
- Se mira el **primer token** del contenido anterior:
  - `START_ARRAY`: el fichero ya era una lista. Mientras el siguiente token sea `START_OBJECT`, `copyCurrentStructure` copia ese objeto completo al generador. Al llegar a `END_ARRAY`, el bucle termina.
  - `START_OBJECT`: el fichero era de una versión antigua con **un solo objeto** suelto. Se copia y pasa a ser el primer elemento de la lista.
- `copyCurrentStructure` copia directamente del parser al generador, sin leer campo a campo.

**2.2 Añadir la persona nueva**

```java
gen.writeStartObject();
gen.writeStringField("nombre", nombre);
gen.writeStringField("apellidos", apellidos);
gen.writeNumberField("edad", edad);
gen.writeEndObject();
```

Se escribe el objeto nuevo campo a campo, justo después de los copiados. Jackson se encarga de escapar los caracteres especiales de las cadenas.

#### Paso 3: Interpretación (lectura token a token)

```java
try (JsonParser parser = factory.createParser(ruta.toFile())) {
    parser.nextToken(); // START_ARRAY
    while (parser.nextToken() == JsonToken.START_OBJECT) {
        while (parser.nextToken() != JsonToken.END_OBJECT) {
            String campo = parser.currentName();
            parser.nextToken();
            switch (campo) { ... }
        }
        System.out.println("---");
    }
}
```

Hay dos bucles anidados:

- **Bucle externo**: avanza de objeto en objeto. Termina cuando el token ya no es `START_OBJECT` (es decir, al llegar a `END_ARRAY`).
- **Bucle interno**: recorre los campos de un objeto hasta `END_OBJECT`. En cada vuelta:
  1. `currentName()` devuelve el nombre del campo (el parser está sobre un `FIELD_NAME`).
  2. `nextToken()` avanza al **valor** de ese campo.
  3. El `switch` decide cómo leerlo: `getText()` para cadenas y `getIntValue()` para números.

### 3.4 Evolución del fichero

Primera ejecución (`Edu Cruz`, 22):

```json
[ {
  "nombre" : "Edu",
  "apellidos" : "Cruz",
  "edad" : 22
} ]
```

Segunda ejecución (`Ana Pérez`, 30):

```json
[ {
  "nombre" : "Edu",
  "apellidos" : "Cruz",
  "edad" : 22
}, {
  "nombre" : "Ana",
  "apellidos" : "Pérez",
  "edad" : 30
} ]
```

### 3.5 Resumen del flujo

```
Pedir datos por consola
        │
        ▼
Leer fichero anterior a un String
        │
        ▼
Abrir JsonGenerator (vacía el fichero)
        │
        ├─ writeStartArray  [
        ├─ copiar personas anteriores (JsonParser + copyCurrentStructure)
        ├─ escribir persona nueva
        └─ writeEndArray    ]
        │
        ▼
Abrir JsonParser sobre el fichero
        │
        └─ recorrer tokens y mostrar cada persona
```

---

## 4. Programas 3 y 4: `EscritorJSON` y `LectorJSON` (data binding)

Esta pareja de programas trabaja con **objetos `Persona`**. `ObjectMapper` convierte automáticamente:

- **objeto Java → JSON** al escribir (`writeValue`)
- **JSON → objeto Java** al leer (`readValue`)

Los dos usan el mismo fichero, `src/main/resources/persona.json`, que contiene una **lista** de personas.

> Nota: esto es *data binding* de Jackson, no la serialización nativa de Java. `Persona` **no** necesita `implements Serializable`.

### 4.1 La clase `Persona`

Jackson necesita que la clase tenga **constructor vacío** y **getters/setters** para poder crear y rellenar los objetos. Ejemplo mínimo (en el paquete `models`):

```java
package models;

public class Persona {
    private String nombre;
    private String apellidos;
    private int edad;

    public Persona() { }   // obligatorio para Jackson

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
}
```

Los nombres de los campos (`nombre`, `apellidos`, `edad`) son los que aparecen como claves en el JSON, y coinciden con los que escribe `JacksonStreaming`, así que los cuatro programas pueden compartir el mismo fichero.

### 4.2 `EscritorJSON`: código

```java
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
```

### 4.3 `EscritorJSON`: cómo funciona

**1. Configuración del `ObjectMapper`**

```java
ObjectMapper mapper = new ObjectMapper();
mapper.enable(SerializationFeature.INDENT_OUTPUT);
```

`INDENT_OUTPUT` hace que el JSON se escriba con saltos de línea e indentación, para que sea legible.

**2. Ruta y persona nueva**

```java
Path ruta = Paths.get("src", "main", "resources", "persona.json");
File f = ruta.toFile();
Persona nueva = new Persona("Angel", "Miranda", 22);
```

La ruta es relativa al directorio de trabajo (en IntelliJ, la raíz del proyecto). La persona a añadir está fija en el código; para cambiarla hay que editar esa línea.

**3. Crear la carpeta si no existe**

```java
Files.createDirectories(ruta.getParent());
```

Evita errores si `src/main/resources` todavía no existe.

**4. Leer lo que ya hay**

```java
ArrayList<Persona> personas = new ArrayList<>();
if (f.exists() && f.length() > 0) {
    personas = mapper.readValue(f, new TypeReference<ArrayList<Persona>>() {});
}
```

- Se parte de una lista vacía.
- Si el fichero existe y no está vacío, `readValue` lo convierte en una `ArrayList<Persona>` con todas las personas anteriores.
- `TypeReference<ArrayList<Persona>>` es necesario porque, por el borrado de tipos genéricos de Java, Jackson no sabría de qué tipo son los elementos de la lista si se le pasara solo `ArrayList.class`.

**5. Añadir y volver a escribir**

```java
personas.add(nueva);
mapper.writeValue(f, personas);
```

- Se añade la persona nueva a la lista en memoria.
- `writeValue` escribe la lista **completa** en el fichero: crea el fichero si no existe y, si existe, lo **sobrescribe**. Como la lista ya contenía las personas anteriores, no se pierde nada.

**6. Resultado**

```java
System.out.println("Registros en el fichero: " + personas.size());
```

Muestra cuántas personas hay ya guardadas.

### 4.4 `LectorJSON`: código

```java
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
```

### 4.5 `LectorJSON`: cómo funciona

**1. Ruta del fichero**

```java
Path ruta = Paths.get("src", "main", "resources", "persona.json");
```

Es la misma ruta que usa `EscritorJSON`. Se lee directamente del disco (no con `getResource`) para ver siempre el contenido más reciente, sin tener que recompilar.

**2. Lectura de la lista**

```java
ArrayList<Persona> personas = mapper.readValue(ruta.toFile(), new TypeReference<ArrayList<Persona>>() {});
```

`readValue` interpreta el JSON `[ {...}, {...} ]` y crea un objeto `Persona` por cada elemento, rellenando sus campos mediante los setters.

**3. Recorrido**

```java
for (Persona p : personas) {
    System.out.println(p.getNombre() + " " + p.getApellidos() + ", " + p.getEdad());
}
```

Se recorre la lista y se imprime cada persona con sus getters.

**4. Errores de lectura**

El `catch (IOException e)` captura tanto un fichero inexistente como un JSON mal formado o que no coincide con la clase (`JsonProcessingException` es subclase de `IOException`).

### 4.6 Evolución del fichero y uso conjunto

Si se ejecuta `EscritorJSON` dos veces cambiando la persona entre ejecuciones (por ejemplo `Angel Miranda` y después `Edu Cruz`), el fichero queda:

```json
[ {
  "nombre" : "Angel",
  "apellidos" : "Miranda",
  "edad" : 22
}, {
  "nombre" : "Edu",
  "apellidos" : "Cruz",
  "edad" : 22
} ]
```

Y `LectorJSON` muestra:

```
Angel Miranda, 22
Edu Cruz, 22
```

Flujo de los dos programas:

```
EscritorJSON                               LectorJSON
────────────                               ──────────
leer lista existente (readValue)           leer lista (readValue)
        │                                          │
        ▼                                          ▼
añadir Persona nueva a la lista            recorrer la lista
        │                                          │
        ▼                                          ▼
escribir lista completa (writeValue)       imprimir cada Persona
        │
        ▼
   persona.json  ◄─────────── mismo fichero ───────────┘
```

### 4.7 Errores típicos

| Error | Causa | Solución |
|---|---|---|
| `MismatchedInputException ... from Object value (START_OBJECT)` | El fichero contiene un objeto suelto `{ ... }` (de una versión anterior) y se intenta leer como lista. | Dejar el fichero como `[]` o activar `DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY` en el `ObjectMapper`. |
| `UnrecognizedPropertyException` | El JSON tiene una clave que `Persona` no tiene (por ejemplo `apellido` en vez de `apellidos`). | Que las claves del JSON coincidan con los campos de `Persona`. |
| `InvalidDefinitionException ... no Creators` | `Persona` no tiene constructor vacío. | Añadir `public Persona() { }`. |
| `NoSuchFileException` en `LectorJSON` | Se ejecuta el lector antes de que exista el fichero, o desde otro directorio de trabajo. | Ejecutar primero `EscritorJSON` y lanzar desde la raíz del proyecto. |

---

## 5. Comparativa de los cuatro programas

| Aspecto | `JSONTiempoReal` | `JacksonStreaming` | `EscritorJSON` | `LectorJSON` |
|---|---|---|---|---|
| Función | Generar e interpretar en memoria | Generar, guardar e interpretar | Añadir persona y guardar | Leer y mostrar |
| API de Jackson | Árbol (`readTree`, `JsonNode`) | Streaming (`JsonGenerator`, `JsonParser`) | Data binding (`writeValue`, `readValue`) | Data binding (`readValue`) |
| Usa clase `Persona` | No | No | Sí | Sí |
| Destino / origen | `String` en memoria | Fichero `persona.json` | Fichero `persona.json` | Fichero `persona.json` |
| Acumula registros | No | Sí | Sí | No aplica |
| Seguro con comillas en los datos | No (JSON a mano) | Sí | Sí | Sí |
| Memoria | Árbol completo | Mínima (token a token) | Lista completa de objetos | Lista completa de objetos |
| Complejidad del código | Muy baja | Media | Baja | Baja |

---

## 6. Notas finales

- Ninguno de los programas usa la serialización nativa de Java: no hay `Serializable` ni `ObjectOutputStream`.
- `EscritorJSON` y `LectorJSON` sí usan *data binding* de Jackson, que requiere la clase `Persona`. Si el apartado de la práctica pide evitarlo, los programas válidos son `JSONTiempoReal` y `JacksonStreaming`.
- Si el JSON está mal formado, Jackson lanza una excepción (`JsonParseException`, subclase de `IOException`).
- La ruta `src/main/resources/persona.json` es relativa: depende del directorio desde el que se ejecute el programa.
- En versiones de Java anteriores a la 25, el método principal debe ser `public static void main(String[] args)`; con `static void main()` a secas solo funciona en versiones recientes.
