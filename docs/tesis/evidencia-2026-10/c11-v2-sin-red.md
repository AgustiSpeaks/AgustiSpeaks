# Criterio 11 — Funcionamiento sin red (segundo intento)

Galaxy A57, Android 16 (API 36). App: versión 1.1 (versionCode 2), build de
depuración de `25099d3`. Toda la sesión en modo edición.
Fecha: 2026-11-03, de 15:09:35 a 15:18:52 (hora del teléfono).

Protocolo: [`protocolo-c11-c13-2026-10.md`](../protocolo-c11-c13-2026-10.md),
fijado en `488711e` y subido a GitHub antes de medir.

**Umbral (sin cambios):** 100 % de las funciones en modo avión. En la 1.1 son
doce, F1 a F12.

Los archivos de esta sesión llevan el prefijo `c11-v2-`; en las tablas se
nombran sin él.

## Condiciones de red

El teléfono informa lo mismo al empezar (`red-inicio.txt`, 15:09:35) y al
terminar (`red-fin.txt`, 15:18:52):

| Comprobación | Salida |
|---|---|
| Modo avión: `settings global airplane_mode_on` | `1` |
| Modo avión: `cmd connectivity airplane-mode` | `enabled` |
| Wi-Fi: `cmd wifi status` | `Wifi is disabled` |
| Bluetooth: `settings global bluetooth_on` | `0` |
| Red por defecto: `dumpsys connectivity` | `Active default network: none` |
| Salida a internet: `ping 8.8.8.8` | `connect: Network is unreachable` |
| Build instalada | `versionName=1.1`, `versionCode=2`, `DEBUGGABLE` |
| Commit medido: `git log --oneline -1 -- app` | `25099d3 Modo edicion con contrasena` |

La línea "Wifi scanning is always available" es el ajuste de ubicación que
deja al teléfono buscar redes Wi-Fi para ubicarse; no conecta a ninguna. Con el
Wi-Fi apagado, sin red por defecto y el `ping` sin salida, el teléfono no tenía
red.

Todas las capturas muestran el avión en la barra de estado.

## Video

`sesion.mp4`: 9 min 0 s, video H.264 y audio Opus, grabado con scrcpy con los
toques marcados. Para ubicar un momento sirve la hora de la barra de estado,
que se ve en el video.

Algunas partes de F3, F5, F6, F8 y F11 quedan solo en el video. El autor lo
revisó; las horas están en la tabla de funciones.

## Voz

`voz-logcat.txt`: una fila por oración que el motor de voz empezó a decir (el
aviso `onStart`), todas del mismo proceso de la app (29777). Las columnas de
latencia y régimen son de la instrumentación del criterio 10 y no se usan acá.

| n | Hora | Oración | Función |
|---|---|---|---|
| 1 | 15:10:27.097 | Yo | F2 |
| 2 | 15:10:28.253 | Yo Quiero | F2 |
| 3 | 15:10:30.693 | Yo Quiero Agua | F2 |
| 4 a 6 | 15:10:54 a 15:10:56 | Yo · Yo Quiero · Yo Quiero Agua | la misma oración, armada de nuevo en Colegio: al cambiar de lugar la barra se vacía |
| 7 | 15:12:09.823 | Agua | — |
| 8 y 9 | 15:12:52 y 15:13:38 | Huesillo | F7 |
| 10 | 15:14:36.112 | Test | F8 |

## Fotos guardadas por la app

`fotos-*.txt`: el contenido de `files/pictogifts/`, el almacenamiento privado
de la app, leído con `run-as`.

| Archivo | Hora | Qué hay |
|---|---|---|
| `fotos-0-inicio.txt` | 15:09 | la carpeta vacía |
| `fotos-1-tras-F7.txt` | 15:12 | `9c34b68f….jpg` (199.261 bytes, 15:12): la foto de "Huesillo" |
| `fotos-2-tras-volver-F7.txt` | 15:13 | el mismo archivo: no se borró (ver F7) |
| `fotos-3-tras-crear-F8.txt` | 15:14 | se suma `983bfb2f….jpg` (146.400 bytes, 15:14): la foto de "Test" |
| `fotos-4-tras-borrar-F8.txt` | 15:15 | `983bfb2f….jpg` ya no está |
| `fotos-5-tras-crear-lugar.txt` | 15:16 | se suma `6c4fe8e2….jpg` (208.465 bytes, 15:16): la foto del lugar |
| `fotos-6-tras-crear-categoria.txt` | 15:17 | se suma `fa8e0159….jpg` (67.172 bytes, 15:17): la foto de la categoría |
| `fotos-7-tras-borrar-lugar.txt` | 15:17 | `6c4fe8e2….jpg` y `fa8e0159….jpg` ya no están |

## Funciones

| # | Función | Resultado | Respaldo |
|---|---|---|---|
| F1 | Abrir la app y elegir un lugar | **sí** | `F1a-abierta.png` (15:09). El lugar elegido se ve en `F2-yo-quiero-agua.png` (Cocina) y en `F3-categorias.png` (Colegio), las dos a las 15:10. `F1b-colegio.png` se tomó en la pantalla de lugares, antes de elegir |
| F2 | Tocar pictogramas: suena la voz y la oración crece | **sí** | filas 1 a 3 del registro de voz, y `F2-yo-quiero-agua.png` con la oración en la barra. Se hizo en Cocina; el protocolo decía Colegio |
| F3 | Navegar entre categorías | **sí** | `F3-categorias.png` (Colegio con sus categorías, 15:10) y el video entre 15:10:33 y 15:10:48 |
| F4 | «Decir»: la tarjeta con el avatar animado | **sí** | `F4-decir.png` (15:11): "Yo Quiero Agua", con el avatar tomando agua |
| F5 | Recientes: ver una frase dicha y volver a abrirla | **sí** | `F5-recientes.png` (15:11) con "Yo Quiero Agua" en la lista; la frase reabierta, en el video entre 15:11:16 y 15:11:36 |
| F6 | Favoritos: agregar una frase; quitarla y recuperarla con «Deshacer» | **no** | **Agregar no se hizo en esta sesión:** la frase ya era favorita, porque quedó de la sesión interrumpida (`c11-v2-intento-interrumpido/c11-v2-F6c-recuperado.png`, 14:42), y `F4-decir.png` ya ofrece «Quitar de favoritos». Quitar y Deshacer: en el video, entre 15:11:55 y 15:12:00. `F6a` y `F6b` son la misma imagen byte a byte, así que `F6b` se tomó antes de tocar la estrella; `F6c-recuperado.png` (15:12) muestra la frase de vuelta |
| F7 | Foto en un pictograma de la app; que el avatar la señale en «Decir»; volver al original | **no** | **Se hizo otra cosa.** Con «Agregar» se creó en Bebidas un pictograma propio, "Huesillo", con una foto de la galería (`F7a-huesillo-con-foto.png`, 15:12, y el archivo nuevo de `fotos-1`). El avatar señala esa foto en «Decir» (`F7b-decir-senala-foto.png`, 15:13). Agua, el pictograma de la app, no se tocó: el DataStore del criterio 13 guarda "Huesillo" entre los propios y ningún cambio a los de la app (`"cambios":[]`). Tampoco se volvió al original: `F7c-huesillo-original.png` muestra "Huesillo" todavía con su foto, y `fotos-2` todavía tiene el archivo |
| F8 | Pictograma propio: crearlo con la cámara, decirlo y borrarlo | **sí** | crear: `F8a-creado.png` (formulario con la foto, 15:14) y el archivo nuevo de `fotos-3`; la cámara, en el video. Decir: fila 10 del registro de voz y `F8b-dicho.png`. Borrar: `F8c-borrado.png` (la confirmación) y el archivo que ya no está en `fotos-4` |
| F9 | Ajustes: cambiar el avatar y la voz, y ver el cambio | **sí** | `F9a-cambiado.png` (15:18): Robot y velocidad 29 %, que normalmente está en 33 %; `F9b-restaurado.png`: de vuelta en Persona |
| F10 | Modo edición: entrar con la contraseña, y salir con la X | **sí** | `F10a-modo-edicion-activo.png` (15:10) y la franja del modo en `F1b`; `F10b-fuera-del-modo-edicion.png` (15:18), la pantalla de lugares sin la franja |
| F11 | Agregar un lugar y, dentro, una categoría, con fotos; borrar el lugar | **sí** | lugar: `F11a-lugar-creado.png` ("Lugar prueba" con su foto, 15:16) y el archivo nuevo de `fotos-5`. Categoría: `F11b-categoria-creada.png` (el formulario de "Test" con su foto, 15:17) y el archivo nuevo de `fotos-6`; guardada, en el video entre 15:17:10 y 15:17:31. Borrar el lugar: `F11c-lugar-borrado.png` (la confirmación) y `fotos-7`, sin los dos archivos |
| F12 | Ocultar un pictograma de la app y volver a mostrarlo | **sí** | `F12a-jugo-oculto.png`: Jugo atenuado, con el ojo tachado; `F12b-jugo-visible.png`: Jugo normal. Las dos a las 15:15 |

## Otras diferencias con el protocolo

No cambian el resultado:

- Algunos nombres son distintos de los del protocolo: el lugar se llamó "Lugar
  prueba", la categoría "Test", y las capturas de F7 llevan "huesillo" en vez
  de "agua".
- Las fotos del lugar y de la categoría no son la del objeto que pide la
  preparación. La del lugar es una foto de la galería del autor (el frente de
  una tienda), y la de la categoría es una captura de pantalla de la app.
- En las capturas de esta sesión no aparece el ícono de No molestar, que sí se
  ve en las de la sesión interrumpida.

## Datos personales

El autor revisó los videos de las dos sesiones: no muestran datos personales.
La contraseña que se escribe en el video es una de prueba, puesta solo para
estas sesiones.

## Sesión interrumpida

Una sesión anterior, de 14:38 a 14:44, se cortó al empezar F8: se perdió la
conexión USB con el teléfono, scrcpy dejó de grabar a las 14:44:44 y
`fotos-3-tras-crear-F8.txt` quedó vacío. Hasta ahí las condiciones eran las
mismas: modo avión, sin red, la misma build. Sus 18 archivos están en
`c11-v2-intento-interrumpido/` y **no se usan para el resultado**. Después de
reconectar, la sesión se repitió entera, desde el principio, a las 15:09.

## Resultado

**No cumple: 10 de 12 funciones respaldadas.** F1 a F5 y F8 a F12 quedan
respaldadas por archivos del teléfono y por el video, con el modo avión
comprobado al empezar y al terminar. No quedan respaldadas:

- **F6**, porque "agregar" no se hizo en esta sesión.
- **F7**, porque no se le puso la foto a un pictograma de la app ni se volvió
  al original.

Ninguna de las dos falló: se hicieron distinto de lo que fija el protocolo.
Ninguna operación de la sesión falló en modo avión. De F7 quedó respaldada una
parte: una foto de la galería en un pictograma, y el avatar que la señala en
«Decir».
