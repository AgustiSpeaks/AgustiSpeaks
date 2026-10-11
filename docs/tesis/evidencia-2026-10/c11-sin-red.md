# Criterio 11 — Funcionamiento sin red

Galaxy A57, Android 16 (API 36). App: build de depuración de `6caa5e3`.
Fecha: 2026-10-11, de 08:49 a 09:04 (hora del teléfono).

**Umbral:** 100 % de las funciones en modo avión. Las funciones F1 a F9 están
fijadas en `protocolo-manual-2026-10.md` desde antes de probar.

## Condiciones

- Modo avión, con Wi-Fi y Bluetooth apagados. El autor lo declara: "tenía todo
  apagado".
- En todas las capturas que muestran la barra de estado se ve el avión, y no
  aparecen los íconos de Wi-Fi ni de datos móviles. Para comparar: `c13-11`, del
  mismo día a las 08:40, todavía muestra "4G" y las barras de señal.
- Hubo dos rondas. En la primera (08:49 a 08:53), las capturas en modo claro
  no muestran la barra de estado (ver "Error encontrado"). Para que también se
  viera el avión, el autor repitió en modo oscuro las funciones de esas
  capturas, sin salir del modo avión (09:02 a 09:04).

## Orden de las capturas

Tres capturas no muestran la hora. Su orden sale de `c11-16`, la lista de
imágenes recientes del teléfono a las 09:04, que va de la más nueva a la más
vieja. De ahí salen dos cosas:

- En las dos rondas, la captura con la frase quitada de Favoritos es
  **anterior** a la que la muestra de vuelta: la segunda es posterior a tocar
  Deshacer.
- La captura de Ajustes en modo claro es anterior a las 08:49, así que muestra
  el estado al empezar. En Ajustes, el cambio a Robot y 18 % es anterior a la
  vuelta a Persona y 33 %.

## Capturas

**Primera ronda**

| Archivo | Hora | Función | Qué muestra |
|---|---|---|---|
| `c11-00-ajustes-al-empezar.jpg` | antes de las 08:49 | — | estado al empezar: avatar Persona, velocidad 33 % |
| `c11-01-decir-colegio.jpg` | 08:49 | F1, F2, F4 | Colegio; tarjeta de «Decir» con "Yo Quiero Me gusta Leche": el avatar sostiene el vaso |
| `c11-02-recientes-decir.jpg` | 08:49 | F5 | "Yo Quiero Mi taza", abierta desde Recientes |
| `c11-03-favoritos-quitado-deshacer.jpg` | entre 08:49 y 08:51 | F6 | "Se quitó de favoritos", con la cuenta regresiva en 4 y el botón Deshacer |
| `c11-04-favoritos-recuperado.jpg` | entre 08:49 y 08:51 | F6 | "Aaw" de vuelta en Favoritos |
| `c11-05-pictograma-propio-galeria.jpg` | 08:51 | F3 | formulario de "Pollo papas" en Alimentos, con la foto elegida de la galería |
| `c11-06-pictograma-propio-imanes.jpg` | 08:52 | F3, F8 | formulario de "Imanes" en Acciones; **al fondo, ya hay una casilla "Imanes"** |
| `c11-07-ajustes-cambio-robot-18.jpg` | 08:53 | F9 | avatar Robot y velocidad 18 %: el cambio se ve en la vista previa |
| `c11-08-ajustes-restaurado.jpg` | 08:53 | F9 | de vuelta en Persona y 33 % |

**Segunda ronda, en modo oscuro**

| Archivo | Hora | Función | Qué muestra |
|---|---|---|---|
| `c11-10-favoritos-quitado-deshacer.jpg` | 09:02 | F6 | "Se quitó de favoritos", con la cuenta regresiva en 4 y el botón Deshacer |
| `c11-11-favoritos-recuperado.jpg` | 09:02 | F6 | "Aaw" de vuelta en Favoritos |
| `c11-12-pictograma-propio-imanes-formulario.jpg` | 09:03 | F8 | formulario de "Imanes" con una foto nueva; **al fondo, Acciones ya no tiene la casilla "Imanes"** |
| `c11-13-pictograma-propio-imanes-acciones.jpg` | 09:03 | F8 | "Imanes" guardado en Acciones, con su foto |
| `c11-14-pictograma-propio-galeria-formulario.jpg` | 09:03 | F3 | formulario de "Pollo papas", con la foto de la galería |
| `c11-15-pictograma-propio-galeria-alimentos.jpg` | 09:04 | F3 | "Pollo papas" guardado en Alimentos, con su foto |
| `c11-16-imagenes-recientes-del-telefono.jpg` | 09:04 | F8 | las imágenes más recientes del teléfono: solo capturas de pantalla de la prueba, ninguna foto de los imanes |

Las tres capturas que pide el protocolo:

| Protocolo | Archivo |
|---|---|
| `c11-decir` | `c11-01-decir-colegio.jpg` |
| `c11-pictograma-propio` | `c11-13-pictograma-propio-imanes-acciones.jpg` |
| `c11-favoritos` | `c11-11-favoritos-recuperado.jpg` |

**Datos personales.** No se guardaron las dos capturas del selector de fotos
del sistema (08:51 y 09:03), porque entre las miniaturas se ven datos
personales del autor. No hacen falta: los formularios `c11-05` y `c11-14` ya
muestran la foto elegida. En `c11-16` se tapó con negro la parte de abajo de las
dos miniaturas de esas mismas capturas, donde están esos datos; el resto de la
imagen no se tocó.

**El borrado también funcionó sin red.** A las 08:52 (`c11-06`) Acciones tenía
una casilla "Imanes", y a las 09:03 (`c11-12`) ya no la tenía: se borró entre
las dos capturas, con el teléfono en modo avión.

**La foto de "Imanes" se sacó desde la app.** En `c11-16` no aparece ninguna
foto de los imanes, ni antes ni después de las 08:52 y las 09:03. Si se hubiera
sacado con la cámara del teléfono para elegirla de la galería, estaría en esa
lista. Coincide con el criterio 16: la foto sacada desde la app queda solo en su
almacenamiento privado.

## Funciones

| # | Función | Resultado | Cómo se sabe |
|---|---|---|---|
| F1 | Abrir la app y elegir un lugar | **sí** | `c11-01` |
| F2 | Tocar pictogramas: suena la voz y la oración crece | **sí** | la voz, comprobada al oído por el autor ("se escuchó la voz"); la oración, en `c11-01` |
| F3 | Navegar entre categorías | **sí** | Alimentos y Acciones, en `c11-05`, `c11-06`, `c11-13` y `c11-15` |
| F4 | «Decir»: la tarjeta con el avatar animado | **sí** | `c11-01` |
| F5 | Recientes: ver una frase y volver a abrirla | **sí** | `c11-02` |
| F6 | Favoritos: agregar; quitar y recuperar con Deshacer | **sí** | quitar y Deshacer: `c11-10` → `c11-11` y `c11-03` → `c11-04`, en ese orden. Agregar: declarado por el autor ("ya la había agregado como prueba"), sin captura |
| F7 | Pictogift con foto, desde la galería, en la tarjeta de «Decir» | **sí** | declarado por el autor, sin captura |
| F8 | Pictograma propio: crearlo con la cámara, decirlo y borrarlo | **sí** | cámara: declarado por el autor ("saqué la foto") y respaldado por `c11-16`; crear: `c11-13`; decir: comprobado al oído por el autor; borrar: `c11-06` frente a `c11-12` |
| F9 | Ajustes: cambiar el avatar y la voz, y ver el cambio | **sí** | `c11-07` y `c11-08` |

## Error encontrado (no decide el criterio)

Con la app en modo claro, los íconos de la barra de estado (hora, avión,
notificaciones) no se ven: quedan claros sobre fondo claro. Por eso `c11-00`,
`c11-03` y `c11-04` no muestran el avión, y por eso las capturas en modo claro
del criterio 13 no muestran la hora.

Causa: `MainActivity` llama a `enableEdgeToEdge()` sin argumentos, y así el
color de los íconos sigue el tema del teléfono, no el modo oscuro de la app.
Con el teléfono en modo oscuro y la app en modo claro, los íconos salen claros.
Está presente en `6caa5e3`, el código medido. Se corrigió después de esta
medición, en `c106b6e`: las barras del sistema siguen el modo oscuro de la app.
El autor lo comprobó en el teléfono: en modo claro ya no queda nada invisible.

## Resultado

**Cumple: 9 de 9 funciones en modo avión.** Seis tienen captura con el avión
visible (F1, F3, F4, F5, F6 y F9); F8 tiene captura de crear y de borrar. Se
apoyan en la declaración del autor: la voz (F2 y el "decir" de F8), el
"agregar" de F6, la cámara de F8 y F7 entera, que no tiene captura.
