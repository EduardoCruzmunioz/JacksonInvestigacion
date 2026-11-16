# Guion de estudio: Jackson para Java

Todas las diapositivas seguidas. Cada bloque empieza con el número de la diapositiva y su título.

## Índice rápido

| Nº  | Diapositiva                           |
| --- | ------------------------------------- |
| 1   | [Portada](#d1)                        |
| 2   | [Agenda](#d2)                         |
| 3   | [¿Qué es JSON?](#d3)                  |
| 4   | [¿Qué es Jackson?](#d4)               |
| 5   | [Las tres formas de usarlo](#d5)      |
| 6   | [Añadir Jackson al proyecto](#d6)     |
| 7   | [Qué hace el proyecto](#d7)           |
| 8   | [La clase Persona](#d8)               |
| 9   | [Demo 1: leer y escribir](#d9)        |
| 10  | [Demo 2: JSON en tiempo real](#d10)   |
| 11  | [Demo 3: Streaming](#d11)             |
| 12  | [Tokens: cómo lee el streaming](#d12) |
| 13  | [Comparativa](#d13)                   |
| 14  | [¿Cuál uso en cada caso?](#d14)       |
| 15  | [Problemas que nos encontramos](#d15) |
| 16  | [Conclusiones y despedida](#d16)      |
| ·   | [Glosario para repasar](#glosario)    |

---

<a id="d1"></a>

## Diapositiva 1 · Portada

**En pantalla**

- Título: «Jackson» y el subtítulo «Cómo hacer que Java hable JSON».
- Una frase: una librería de Java para convertir objetos en JSON y leerlos de vuelta, con muy poco código.
- Un ejemplo de persona.json en una ventana tipo editor.
- Tus datos: nombre, asignatura y curso.

**Qué decir**

Hola a todos. Hoy os voy a enseñar Jackson, una librería de Java que nos ahorra mucho trabajo cuando queremos guardar y leer datos.

La idea es sencilla: Java trabaja con objetos y el JSON es texto, así que hace falta algo que traduzca entre los dos. Eso es Jackson, y lo vamos a ver con ejemplos de mi proyecto.

> **Idea clave:** Jackson es la librería que traduce entre Java y JSON.

---

<a id="d2"></a>

## Diapositiva 2 · Agenda

**En pantalla**

- Seis bloques: 1) JSON y Jackson, 2) Cómo añadir Jackson con Maven, 3) Qué hace el proyecto y la clase Persona, 4) Las tres demos, 5) Comparativa, cuál usar y problemas, 6) Conclusiones y despedida.

**Qué decir**

Antes de empezar, os cuento el orden. Primero veremos qué es JSON y qué es Jackson. Después cómo se añade a un proyecto y qué hace el mío.

Luego van las tres demos, la comparativa para decidir cuál usar, los problemas que me encontré y, para terminar, las conclusiones.

> **Idea clave:** La charla va de lo general (qué es) a lo práctico (demos) y acaba con conclusiones.

---

<a id="d3"></a>

## Diapositiva 3 · ¿Qué es JSON?

**En pantalla**

- Ejemplo de persona.json con nombre, apellidos y edad.
- Tres tarjetas: texto (lo leen personas y programas), ligero (ocupa poco), universal (sirve en ficheros, apps e internet).
- Frase: cada dato tiene un nombre (clave) y un valor.

```json
{
  "nombre": "Edu",
  "apellidos": "Cruz",
  "edad": 22
}
```

**Qué decir**

JSON es una forma de escribir datos como texto. Lo bueno es que lo entienden tanto las personas, porque se lee fácil, como los programas.

Fijaos en el ejemplo: cada dato tiene un nombre, que llamamos clave, y un valor. Aquí la clave «nombre» vale «Edu».

Se usa muchísimo para guardar datos en ficheros y para pasarlos entre aplicaciones o por internet.

> **Idea clave:** JSON = texto con claves y valores; ligero y universal.

---

<a id="d4"></a>

## Diapositiva 4 · ¿Qué es Jackson?

**En pantalla**

- Frase: el traductor entre Java y JSON.
- Dos ventanas para comparar: a mano frente a con Jackson.
- Debajo: sin Jackson, una comilla en el nombre rompe el JSON; con Jackson, él escribe y lee bien el JSON (y es de las librerías más usadas en Java).

```java
// A mano
String json = "{ \"nombre\": \"" + nombre + "\" }";

// Con Jackson
mapper.writeValue(f, personas);
Persona p = mapper.readValue(f, Persona.class);
```

**Qué decir**

Jackson es el traductor entre Java y JSON. Sin él, tendríamos que construir el texto a mano, juntando trozos de cadena, como en la ventana de la izquierda.

El problema es que es muy fácil romperlo: si el nombre lleva una comilla, el JSON deja de ser válido. Con Jackson basta una línea y él se encarga de escribir y leer bien el JSON.

> **Idea clave:** Jackson evita construir el JSON a mano y los errores que eso trae.

---

<a id="d5"></a>

## Diapositiva 5 · Las tres formas de usarlo

**En pantalla**

- Tres tarjetas con símbolos: { } Data binding, </> Árbol, ~> Streaming.
- Data binding: le das un objeto Persona y te devuelve el JSON, y al revés. Analogía: un traductor automático. El más cómodo.
- Árbol: cargas el JSON en memoria como un árbol de nodos y te mueves por él sin clases. Analogía: un mapa del JSON.
- Streaming: lees y escribes token a token sin tenerlo entero en memoria. Analogía: leer un libro página a página. El más rápido y ligero.

**Qué decir**

Jackson se puede usar de tres maneras, y en el proyecto he usado las tres.

La primera es data binding: es como tener un traductor automático. Le pasas un objeto Persona y te devuelve el JSON, y al revés. Es la más cómoda.

La segunda es el árbol: Jackson carga el JSON en memoria y tú te mueves por él buscando lo que necesitas, como con un mapa, sin necesidad de clases.

La tercera es streaming: se lee y se escribe pieza a pieza, como leer un libro página a página sin tenerlo entero en la cabeza. Es la más rápida y la que menos memoria gasta.

> **Idea clave:** Data binding = cómoda; árbol = flexible; streaming = rápida y ligera.

---

<a id="d6"></a>

## Diapositiva 6 · Añadir Jackson al proyecto

**En pantalla**

- Un trozo de pom.xml con la dependencia jackson-databind 2.22.4.
- Tres módulos: jackson-core (streaming: JsonParser y JsonGenerator), jackson-databind (árbol y data binding con ObjectMapper) y jackson-annotations (anotaciones para personalizar el JSON).

```xml
<dependency>
  <groupId>com.fasterxml.jackson.core</groupId>
  <artifactId>jackson-databind</artifactId>
  <version>2.22.4</version>
</dependency>
```

**Qué decir**

Para usar Jackson en un proyecto Maven solo hay que añadir esta dependencia al pom.xml y actualizar el proyecto.

Con jackson-databind ya basta, porque Maven descarga también el core, que trae el streaming, y las anotaciones. Es decir, una dependencia y tenemos las tres formas de uso.

> **Idea clave:** Una sola dependencia (databind) trae todo lo necesario.

---

<a id="d7"></a>

## Diapositiva 7 · Qué hace el proyecto

**En pantalla**

- Tres pasos: 01 Pedir datos (nombre, apellidos, edad), 02 Guardar en persona.json, 03 Leer y mostrar todo.
- Texto: los registros se acumulan, no se sobrescriben; se lee lo que había y se añade la persona nueva.
- Lo hicimos de tres formas distintas para compararlas.

**Qué decir**

Mi proyecto es un programa que pide los datos de una persona, los guarda en un fichero persona.json y después los lee y los muestra.

Lo importante es que los registros se acumulan. Cada vez que guardo una persona, el programa lee primero lo que ya había en el fichero, le añade la nueva y lo vuelve a escribir. Así no se pierde nada.

Y lo he hecho de tres formas distintas para poder compararlas, que es lo que vamos a ver ahora.

> **Idea clave:** El programa pide, guarda y lee personas, y acumula los registros.

---

<a id="d8"></a>

## Diapositiva 8 · La clase Persona

**En pantalla**

- Código de la clase Persona con nombre, apellidos y edad.
- Tres requisitos: constructor vacío (obligatorio, Jackson crea el objeto con él), getters y setters (para leer y rellenar cada campo), campos = claves (los mismos nombres que en el JSON).
- Solo la necesitan EscritorJSON y LectorJSON.

```java
public class Persona {
  private String nombre;
  private String apellidos;
  private int edad;

  public Persona() { }
  // constructor, getters y setters
}
```

**Qué decir**

Para la primera demo necesito una clase Persona con tres campos: nombre, apellidos y edad.

Jackson le pide tres cosas: un constructor vacío, porque con él crea el objeto; getters y setters, para leer y rellenar cada campo; y que los nombres de los campos coincidan con las claves del JSON.

Esta clase solo hace falta en las dos primeras partes del proyecto; las otras dos formas no la usan.

> **Idea clave:** Persona necesita constructor vacío, getters/setters y campos con el mismo nombre que las claves.

---

<a id="d9"></a>

## Diapositiva 9 · Demo 1: leer y escribir

**En pantalla**

- Código de EscritorJSON y LectorJSON: personas.add(nueva); mapper.writeValue(f, personas); y readValue con TypeReference.
- El fichero persona.json con varias personas dentro de una lista.
- Pie: la forma más cómoda; para acumular se lee la lista, se añade y se reescribe.

```java
// Escribir
personas.add(nueva);
mapper.writeValue(f, personas);

// Leer
personas = mapper.readValue(f,
  new TypeReference<ArrayList<Persona>>() {});
```

**Qué decir**

Esta es la forma más cómoda. Jackson convierte la lista de objetos Persona en JSON con una sola línea, writeValue, y la lee de vuelta con readValue.

Para acumular, primero leo la lista que había en el fichero, añado la persona nueva y escribo la lista completa otra vez.

El TypeReference le dice a Jackson que lo que lee es una lista de Persona, porque Java no puede saber ese tipo por sí solo.

Aquí haría la demo en vivo: ejecuto el escritor dos veces con personas distintas y después el lector, y se ve cómo crece el fichero.

> **Idea clave:** writeValue escribe la lista completa; readValue + TypeReference la lee.

---

<a id="d10"></a>

## Diapositiva 10 · Demo 2: JSON en tiempo real

**En pantalla**

- Captura de la consola: se piden nombre, apellidos y edad y se muestra el JSON generado.
- Código: JsonNode raiz = mapper.readTree(json); raiz.get("nombre").asText(); raiz.get("edad").asInt();
- Pie: no usamos la clase Persona; punto débil: una comilla en el nombre rompe el JSON.

```java
JsonNode raiz = mapper.readTree(json);
raiz.get("nombre").asText();
raiz.get("edad").asInt();
```

**Qué decir**

En esta versión no uso la clase Persona. El programa pide los datos por consola, construye el JSON como texto y después lo interpreta.

Para interpretarlo uso readTree, que carga el JSON como un árbol de nodos. Luego voy pidiendo cada campo por su nombre: asText para textos y asInt para números.

Es muy fácil de entender y va bien para probar, pero tiene un punto débil: como el JSON se construye a mano, una comilla en el nombre lo rompe.

> **Idea clave:** readTree + get("campo") permite leer el JSON sin crear clases.

---

<a id="d11"></a>

## Diapositiva 11 · Demo 3: Streaming

**En pantalla**

- Código con writeStartObject, writeStringField, writeNumberField y writeEndObject.
- Cuatro pasos: 01 Pedir datos, 02 Leer fichero anterior, 03 Escribir anteriores + nueva, 04 Leer y mostrar.
- Pie: el tiempo real de verdad; el más rápido y el que menos memoria gasta, a cambio de más código.

```java
gen.writeStartObject();
gen.writeStringField("nombre", nombre);
gen.writeNumberField("edad", edad);
gen.writeEndObject();
```

**Qué decir**

Esta es la forma de streaming, el «tiempo real» de verdad. Jackson escribe el JSON pieza a pieza: abro un objeto, escribo cada campo y lo cierro.

El flujo del programa es este: pido los datos, leo lo que había en el fichero, escribo lo anterior más la persona nueva, y por último leo todo y lo muestro.

Es la forma más rápida y la que menos memoria gasta, así que va bien para ficheros enormes. A cambio, hay que escribir más código y controlar cada paso.

> **Idea clave:** JsonGenerator escribe y JsonParser lee, token a token, sin crear objetos.

---

<a id="d12"></a>

## Diapositiva 12 · Tokens: cómo lee el streaming

**En pantalla**

- Un persona.json pequeño junto a una tabla Se lee / Token.
- { → START_OBJECT · "nombre" → FIELD_NAME · "Edu" → VALUE_STRING · 22 → VALUE_NUMBER_INT · } → END_OBJECT.
- Pie: el parser avanza con nextToken(); currentName() da el campo y getText() o getIntValue() el valor.

```json
{
  "nombre": "Edu",
  "edad": 22
}
```

**Qué decir**

Para entender el streaming hay que saber qué es un token. Cuando Jackson lee el JSON, no lo ve entero: lo recorre trozo a trozo, y cada trozo es un token.

Mirad la tabla: la llave de apertura es START_OBJECT, el nombre de un campo es FIELD_NAME, un texto es VALUE_STRING, un número es VALUE_NUMBER_INT y la llave de cierre es END_OBJECT.

El parser avanza con nextToken(). Con currentName() sé en qué campo estoy y con getText() o getIntValue() leo el valor.

> **Idea clave:** Streaming = ir leyendo tokens con nextToken() hasta el final del JSON.

---

<a id="d13"></a>

## Diapositiva 13 · Comparativa

**En pantalla**

- Tabla con tres columnas: Leer/escribir, JSONTiempoReal y Streaming.
- Comodidad: alta, alta, baja. Velocidad y memoria: media, media, máxima. Necesita clase Persona: sí, no, no. Seguro con comillas: sí, no, sí.
- Pie: para el día a día, data binding; para ficheros enormes, streaming; para probar rápido, el árbol.

**Qué decir**

Aquí comparo las tres formas. Leer y escribir con Persona y el árbol son muy cómodos; el streaming es más incómodo de programar, pero es el más rápido y el que menos memoria usa.

Sobre las comillas: la versión de JSONTiempoReal falla porque construye el JSON a mano; las otras dos escapan los caracteres especiales solas.

No hay una mejor que otra, depende de lo que necesites: día a día, data binding; ficheros enormes, streaming; probar rápido, el árbol.

> **Idea clave:** Comodidad frente a rendimiento: eso decide cuál usar.

---

<a id="d14"></a>

## Diapositiva 14 · ¿Cuál uso en cada caso?

**En pantalla**

- Tres tarjetas: Escritor + Lector, JSONTiempoReal y JacksonStreaming.
- Escritor + Lector: código corto y cómodo con tus clases; ideal para el día a día.
- JSONTiempoReal: muy fácil de entender; ideal para empezar y probar.
- JacksonStreaming: procesa el JSON pieza a pieza; ideal cuando importa el rendimiento.

**Qué decir**

Resumiendo para decidir. Si trabajas con tus propias clases, como Persona, lo más cómodo es el escritor y el lector con data binding. Es lo que usaría en el día a día.

Si solo quieres entender la idea o hacer una prueba rápida, JSONTiempoReal con el árbol es lo más sencillo.

Y si vas a manejar ficheros muy grandes o te importa el rendimiento, JacksonStreaming es la opción.

> **Idea clave:** Clases propias → data binding; probar → árbol; ficheros enormes → streaming.

---

<a id="d15"></a>

## Diapositiva 15 · Problemas que nos encontramos

**En pantalla**

- Tabla problema → solución:
- El fichero no se encontraba → ruta directa en vez de getResource.
- Los registros se sobrescribían → leer la lista, añadir y reescribir.
- Error al leer { } como lista [ ] → dejar el fichero como [ ] o aceptar valor único.
- Texto: los tres errores vienen de dar por hecho cómo estaba el fichero.

**Qué decir**

Por el camino me encontré tres problemas, y me parece lo más interesante porque ahí se aprende.

El primero: el programa no encontraba el fichero. Lo solucioné leyéndolo con la ruta directa en vez de usar getResource.

El segundo: los registros se sobrescribían. Se arregló leyendo la lista, añadiendo la persona y reescribiendo todo.

El tercero: un error al leer un objeto con llaves como si fuera una lista con corchetes. Se arregla dejando el fichero como una lista vacía o aceptando un valor único como lista. Los tres venían de dar por hecho cómo estaba el fichero.

> **Idea clave:** Leer primero, añadir y reescribir; y que el fichero sea una lista [ ].

---

<a id="d16"></a>

## Diapositiva 16 · Conclusiones y despedida

**En pantalla**

- Tres ideas: 01 Jackson convierte Java en JSON y al revés con poco código. 02 Tres formas: data binding, árbol y streaming. 03 Elegir: comodidad frente a rendimiento.
- Una ventana con «¡Gracias por vuestra atención!» y un mensaje final.

**Qué decir**

Para terminar, me quedo con tres ideas. Primera: Jackson convierte Java en JSON y al revés con muy poco código. Segunda: tiene tres formas de usarse, data binding, árbol y streaming. Y tercera: elegir una u otra depende de si buscas comodidad o rendimiento.

Para mí, la más práctica del día a día es data binding. Os animo a probar las tres y ver cuál os resulta más cómoda.

Ha sido un placer enseñaros Jackson. ¡Muchas gracias a todos!

> **Idea clave:** Tres ideas: poco código, tres formas, comodidad frente a rendimiento.

---

<a id="glosario"></a>

## Glosario para repasar

| Término                  | Qué significa                                                                                       |
| ------------------------ | --------------------------------------------------------------------------------------------------- |
| **JSON**                 | Formato de texto para guardar datos con claves y valores.                                           |
| **Jackson**              | Librería de Java que convierte objetos en JSON y JSON en objetos.                                   |
| **ObjectMapper**         | Clase principal de Jackson para leer y escribir JSON (writeValue, readValue, readTree).             |
| **Data binding**         | Convertir JSON a objetos Java y al revés de forma automática.                                       |
| **Árbol (JsonNode)**     | El JSON cargado en memoria como nodos por los que te mueves con get().                              |
| **Streaming**            | Leer y escribir el JSON pieza a pieza con JsonParser y JsonGenerator.                               |
| **Token**                | Cada trozo que el parser lee: START_OBJECT, FIELD_NAME, VALUE_STRING...                             |
| **TypeReference**        | Indica a Jackson el tipo genérico, por ejemplo ArrayList<Persona></persona>.                        |
| **copyCurrentStructure** | Copia del parser al generador el objeto actual completo (usado para conservar personas anteriores). |
