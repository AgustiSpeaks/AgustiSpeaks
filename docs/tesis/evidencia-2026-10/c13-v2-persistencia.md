# Criterio 13 — Persistencia (segundo intento)

Galaxy A57, Android 16 (API 36). App: versión 1.1 (versionCode 2), build de
depuración de `25099d3`, la misma que el criterio 11. Se hizo a continuación de
ese criterio, en modo edición.
Fecha: 2026-11-03, de 15:20 a 15:25 (hora del teléfono).

Protocolo: [`protocolo-c11-c13-2026-10.md`](../protocolo-c11-c13-2026-10.md),
fijado en `488711e` y subido a GitHub antes de medir.

**Umbral (sin cambios):** 100 % de los ajustes y pictogramas propios tras
cerrar desde recientes.

Los archivos llevan el prefijo `c13-v2-`; en las tablas se nombran sin él.

## Elementos

| # | Elemento | Valor antes del cierre | Clave en el DataStore |
|---|---|---|---|
| A1 | Tipo de avatar | Robot | `av_kind` |
| A2 | Tamaño de pictogramas | Grande | `text_size` |
| A3 | Modo oscuro | encendido | `dark_mode` |
| A4 | Velocidad de la voz | 27 % | `voice_rate` |
| P1 | Pictograma propio "Leche", en Bebidas, con foto de la galería | — | `custom_pictograms`, y su foto en `files/pictogifts/` |
| P2 | Pictograma propio "Test", en Juego, con foto de la cámara | — | `custom_pictograms`, y su foto en `files/pictogifts/` |

## Antes y después

| Momento | Hora | Archivos | Proceso de la app |
|---|---|---|---|
| Con los cambios hechos | 15:20 a 15:23 | capturas `1-antes-*.png` | — |
| Antes de cerrar | 15:23:16 | `estado-1-antes.txt`, `datastore-1-antes.pb` | PID 2597 |
| Cerrada desde recientes | — | ninguno: el paso 8 no se ejecutó | — |
| Reabierta | 15:23:54 | `estado-3-reabierta.txt`, `datastore-3-reabierta.pb` | PID 4924 |
| Después de reabrir | 15:24 | capturas `3-despues-*.png` | — |

Cada `estado-*.txt` tiene la hora del teléfono, el PID (`pidof`), la lista de
`files/datastore/` y `files/pictogifts/` con tamaños y fechas, y el DataStore
decodificado con [`herramientas/leer-datastore.ps1`](../herramientas/leer-datastore.ps1).
El `.pb` es la copia del archivo tal cual. El decodificador no muestra la huella
de la contraseña del modo edición, pero la copia `.pb` sí la contiene: es la de
una contraseña de prueba, puesta solo para estas sesiones. El autor revisó el
video: no muestra datos personales.

## Tabla de verificación

| Elemento | Antes: captura · DataStore | PID antes | PID después | Después: captura · DataStore |
|---|---|---|---|---|
| A1 Tipo de avatar | `1-antes-avatar.png` (15:20): Robot · `av_kind = ROBOT` | 2597 | 4924 | `3-despues-avatar.png` (15:24): Robot · `av_kind = ROBOT` |
| A2 Tamaño de pictogramas | `1-antes-config.png` (15:21): Grande · `text_size = GRANDE` | 2597 | 4924 | `3-despues-config.png` (15:24): Grande · `text_size = GRANDE` |
| A3 Modo oscuro | `1-antes-config.png` (15:21): encendido · `dark_mode = true` | 2597 | 4924 | `3-despues-config.png` (15:24): encendido · `dark_mode = true` |
| A4 Velocidad de la voz | `1-antes-voz.png` (15:21): 27 % · `voice_rate = 0.910424232` | 2597 | 4924 | `3-despues-voz.png` (15:24): 27 % · `voice_rate = 0.910424232` |
| P1 Pictograma en Bebidas | `1-antes-bebidas.png` (15:22): "Leche" con su foto · `propio:04ce93b0…`, foto `0663ff79….jpg` (73.327 bytes, 15:22) | 2597 | 4924 | `3-despues-bebidas.png` (15:24): lo mismo · lo mismo |
| P2 Pictograma en Juego | `1-antes-juego.png` (15:23): "Test" con su foto · `propio:dbace0e4…`, foto `1e4638c2….jpg` (224.692 bytes, 15:23) | 2597 | 4924 | `3-despues-juego.png` (15:24): lo mismo · lo mismo |

Además:

- **Las dos copias del DataStore son idénticas byte a byte**: 1.519 bytes y 26
  claves cada una, con el mismo SHA-256,
  `86C8D68FF1CAEE4A075B46D0306F45D19363E2C4F1B725018AA661F1129E915F`.
- `files/pictogifts/` tiene los mismos tres archivos antes y después, con el
  mismo tamaño y la misma fecha. El tercero es la foto de "Huesillo", del
  criterio 11; también se mantuvo, pero no es uno de los seis elementos.
- Al reabrir, el modo edición está apagado (`3-despues-lugares.png`). Así está
  diseñado: el modo no se guarda.

## El cierre

- **El proceso cambió: PID 2597 antes, 4924 después.** Un proceso conserva su
  PID mientras vive, así que el de las 15:23:54 es otro. Como dice el protocolo,
  "si el proceso es otro, todo lo que muestra salió del almacenamiento, no de la
  memoria".
- **El paso 8 no se ejecutó.** Por eso no hay un `estado-2-cerrada.txt` que
  muestre "ningún proceso" con la app cerrada. El cierre desde recientes se ve
  en el video entre 15:23:16 y 15:23:54; el autor lo revisó.
- Entre las dos lecturas pasaron 38 segundos, no el minuto que pide el
  protocolo. Igual las capturas de después tienen una hora posterior (15:24)
  a las de antes (15:20 a 15:23), que era lo que se buscaba.

## Otras diferencias con el protocolo

- **P1 se llama "Leche", que también es un pictograma de la app en Bebidas.**
  El protocolo pedía un nombre que no fuera palabra de la app. Se distingue por
  la foto, en las capturas, y por su id de pictograma propio, en el DataStore.
- **La velocidad cambió poco.** El valor normal es 33 % (1,0 en el DataStore);
  el que se mantuvo es 27 % (0,9104). Es distinto del de fábrica, pero el
  protocolo pedía "un valor bien distinto". Con los archivos no se puede saber
  si se cambió en este criterio o quedó del F9 del criterio 11 (`c11-v2-F9a`,
  29 %).

## Video

`sesion.mp4`: 4 min 44 s, video H.264 y audio Opus, grabado con scrcpy con los
toques marcados. Muestra la sesión completa: los cambios, la foto de la galería
de P1, la foto de la cámara de P2, el cierre desde recientes y la reapertura.

## Resultado

**Cumple: 6 de 6.** Los seis elementos tienen el mismo valor antes del cierre y
después de reabrir, en las capturas con hora y en el DataStore, y el proceso de
la app cambió en el medio.

La regla de resultado del protocolo también pide "ningún proceso" en
`estado-2-cerrada`, y ese paso no se ejecutó. El cierre queda probado por el
cambio de PID, que según el propio protocolo es lo que muestra que los valores
salieron del almacenamiento. Se anota igual que la parte B del criterio 16: un
paso sin realizar, a la vista, que no cambia lo que muestran los demás.
