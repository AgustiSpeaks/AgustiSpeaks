# Capturas para el capítulo 8

Lista de qué fotografiar, en qué orden y qué debería verse. Está armada para
sacarlas **en una sola sesión**, sin tener que volver atrás a buscar una pantalla.

Son capturas para **ilustrar** el capítulo. Las capturas que sirven de
**evidencia** de los criterios 11 a 16 tienen su propia lista, en el
[protocolo manual](protocolo-manual-2026-10.md), y van a
`evidencia-2026-10/`. Estas van a `capturas-2026-10/`.

---

## Antes de empezar

- Dispositivo en **horizontal** (la app lo fuerza igual).
- Avatar en el tipo que vaya a ilustrar la tesis. Si va a aparecer el robot en
  alguna captura, dejarlo fijo: cambiar de tipo a mitad de sesión hace que las
  imágenes no se puedan comparar entre sí.
- **Apagar la instrumentación** si las capturas van a mostrar Logcat: poner
  `ACTIVA = false` en `validacion/Validacion.kt`. Para las capturas de pantalla
  normales da igual.
- Formato **PNG**, captura de pantalla completa del sistema.
- **Fotos de objetos, nunca de personas**, en todo lo que lleve foto:
  pictogramas propios y pictogifts.

Las animaciones son cíclicas: si una captura sale en un instante poco
representativo, tocar el pictograma otra vez y repetir. Para las acciones con
bucle largo —dormir, 3.800 ms; aburrido, 3.400— conviene esperar al momento en
que el gesto se lee mejor.

---

## Bloque 1 — Recorrido de la app (5 capturas)

| # | Pantalla | Qué hacer | Qué debe verse |
|---|---|---|---|
| 1 | Elección de lugar | abrir la app | las ocho tarjetas de lugar, cada una con su color |
| 2 | Tablero vacío | entrar a "Cocina" | los chips de categoría y la grilla de pictogramas |
| 3 | Frase en construcción | tocar "Yo", "Quiero", "Agua" | la barra con la oración acumulada completa |
| 4 | Tarjeta de "Decir" | tocar "Decir" | avatar animado a la izquierda, frase y botón a la derecha |
| 5 | Ajustes, pestaña Avatar | abrir Ajustes | los controles de personalización |

La captura 3 es la que ilustra **la regla central del producto**: la oración se
acumula y cada pictograma la lee entera. Conviene que la barra muestre las tres
palabras.

---

## Bloque 2 — Un pictograma por categoría (16 capturas)

Una por categoría, elegida porque muestra algo que las otras no. En todas: tocar
el pictograma, después "Decir", y capturar **la tarjeta con el avatar animado**.

| # | Categoría | Pictograma | Qué demuestra |
|---|---|---|---|
| 6 | núcleo | **Parar** | palma de frente, dedos hacia arriba, señal de alto |
| 7 | Alimentos | **Sopa** | el alimento tiene forma propia: se ve un plato con vapor |
| 8 | Bebidas | **Leche** | el objeto en la mano cambia según la bebida |
| 9 | Acciones | **Correr** | marcha de frente: la rodilla sube, el cuerpo rebota |
| 10 | Sentimientos | **Triste** | expresión, lágrimas y cuerpo hundido a la vez |
| 11 | Dolor | **Me duele la panza** | halo sobre la parte nombrada por la frase |
| 12 | Partes del cuerpo | **Manos** | el avatar se señala la parte, sin tocarla |
| 13 | Ropa | **Zapatos** | se agacha: el destino de la prenda cambia la postura |
| 14 | Higiene | **Lavarme las manos** | el lavabo, la canilla y el agua cayendo |
| 15 | Tiempo | **Noche** | la luna de fondo y los ojos entrecerrados |
| 16 | Familia | **Mamá** | la segunda figura al lado del avatar |
| 17 | Juego | **Tobogán** | escenario detrás de la figura |
| 18 | Lugares | **Casa** | camina y el edificio aparece detrás |
| 19 | Escuela | **Escribir** | sentado a la mesa, distinto de dibujar |
| 20 | Compras | **Comprar** | empuja el carrito caminando |
| 21 | Social | **Gracias** | mano al pecho y corazón |

---

## Bloque 3 — Las distinciones que sostiene la tesis (8 capturas, en 4 pares)

Estas son las más importantes del capítulo. Cada par muestra **dos conceptos que
antes se veían iguales**. Hay que sacarlas de a dos y presentarlas lado a lado.

| # | Par | Qué debe distinguirse |
|---|---|---|
| 22-23 | **Lápiz** / **Escribir** | nombrar un objeto contra usarlo: en alto contra sentado a la mesa |
| 24-25 | **Libro** / **Leer** | libro cerrado con lomo en la mano contra libro abierto leyendo |
| 26-27 | **Carrito** / **Comprar** | carrito quieto y el avatar señalándolo contra empujándolo |
| 28-29 | **Jabón** / **Lavarme las manos** | el jabón mostrado contra el lavabo en uso |

Estas cuatro parejas son la evidencia visual del principio **"nombrar un objeto y
usarlo son actos distintos"**, que es el hallazgo de diseño central. Sin las
capturas, el argumento queda en palabras.

---

## Bloque 4 — Pictogramas propios (3 capturas)

Muestran que cada familia puede ampliar el vocabulario con sus propias fotos.
Se hacen con un pictograma creado para la ocasión, por ejemplo **"Camioncito"**
en Juego, con la foto de un juguete. El nombre no puede coincidir con una
palabra de la app.

| # | Pantalla | Qué hacer | Qué debe verse |
|---|---|---|---|
| 30 | Crear | Colegio → Juego → **Agregar**, escribir el nombre y elegir la foto, **sin guardar todavía** | el formulario con el nombre, los botones de foto y la vista previa del pictograma con el color de Juego |
| 31 | En el tablero | guardar, y volver a la grilla de Juego | el pictograma propio al final de la categoría, con la foto en lugar del ícono |
| 32 | Diciendo la frase | tocar "Quiero", después el pictograma propio, y el botón de hablar | la tarjeta de «Decir» con **el avatar a la izquierda señalando la foto** |

La captura 32 es la que conecta esta función con el resto de la tesis: un
pictograma propio no tiene animación, pero al decirlo el avatar **señala** su
foto, así que se integra al mismo lenguaje visual que los demás.

---

## Lo que hay que mirar mientras se capturan

Tres cosas cambiaron hace poco y **nadie las vio todavía en pantalla**. Si algo
está mal, es el momento de detectarlo:

| Captura | Qué revisar |
|---|---|
| Cualquiera con salto | **La altura del salto bajó un 30 %.** Si el avatar apenas se despega, avisar: es un número. |
| 24 — Libro | El libro cerrado tiene que distinguirse del cuaderno: lomo en vez de espiral, y el canto de las hojas. |
| 26 — Carrito | El carrito tiene que verse **parado solo**, sin manija estirada hasta la mano, y el avatar señalándolo. |
| 14 — Lavarme las manos | Las manos no deben quedar tapadas por el borde de la pileta, y el chorro debe caer encima. |
| 16 — Mamá | Proporción de la segunda figura, y que el pelo largo no se lea como un casco. |
| 19 — Escribir | Que la tapa del escritorio no cubra la mano que escribe. |

---

## Resumen

**32 capturas**, en cuatro bloques, todas de ilustración. Se pueden sacar
seguidas en una sesión de unos veinticinco minutos. Al terminar, borrá el
pictograma propio del bloque 4.
