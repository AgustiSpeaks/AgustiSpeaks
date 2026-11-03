# Resumen del segundo intento de los criterios 11 y 13

Para actualizar en la tesis los puntos 8.1 y 8.4, la Tabla 15 y el capítulo 9.
El detalle está en la [bitácora](bitacora-2026-10.md) y en
`evidencia-2026-10/c11-v2-sin-red.md` y `c13-v2-persistencia.md`.

## Lo común a los dos

- **Versión medida: 1.1** (`25099d3`), con toda la sesión en modo edición. El
  primer intento midió la 1.0 (`6caa5e3`). Los demás criterios siguen medidos
  sobre la 1.0 (`c106b6e` y `6caa5e3`), así que la tesis tiene que decir qué
  versión mide cada criterio.
- **Fecha:** 2026-11-03, en el Galaxy A57 con Android 16, en una sola sesión:
  el 11 de 15:09 a 15:18 y el 13 de 15:20 a 15:25.
- **Protocolo fijado antes de medir:** `protocolo-c11-c13-2026-10.md`, en el
  commit `488711e`, subido a GitHub antes de la sesión. GitHub registra la hora
  de ese push en su servidor.
- **Cómo se respalda ahora:** salidas de `adb` guardadas en archivos, capturas
  tomadas por `adb` (no por el teléfono) y una grabación continua de la pantalla
  con audio, una por criterio. Ninguna función se apoya en la declaración del
  autor. Lo que solo se ve en el video lo revisó el autor, y las horas de cada
  tramo están en la evidencia para que se pueda comprobar.
- Una primera sesión del 11 se cortó por la conexión USB y se repitió entera.
  Queda guardada aparte y no se usa.

## Criterio 11 — Funcionamiento sin red

| | Primer intento | Segundo intento |
|---|---|---|
| Versión | 1.0 (`6caa5e3`) | 1.1 (`25099d3`), en modo edición |
| Fecha | 2026-10-11 | 2026-11-03 |
| Funciones | 9 (F1 a F9) | 12: F7 cambia por su equivalente en la 1.1, y se agregan F10, F11 y F12 |
| Condiciones de red | declaradas por el autor, y el avión visible en las capturas | comprobadas por `adb` al empezar y al terminar: modo avión, Wi-Fi y Bluetooth apagados, sin red por defecto, `ping` sin salida |
| Voz | comprobada al oído por el autor | registro del motor de voz con la hora (`onStart`) y el audio del video |
| Resultado | **cumple: 9 de 9**, con la voz, F7 y partes de F6 y F8 por declaración del autor | **no cumple: 10 de 12** respaldadas |

**Queda respaldado:** abrir y elegir un lugar, la voz al tocar pictogramas,
navegar entre categorías, «Decir», Recientes, el pictograma propio con la
cámara (crear, decir y borrar), Ajustes, entrar y salir del modo edición,
crear un lugar y una categoría con fotos y borrarlos, y ocultar y volver a
mostrar un pictograma de la app. En los pasos con fotos, la lista de archivos
del almacenamiento privado muestra la foto nueva al crear y su desaparición al
borrar.

**No queda respaldado:**

- **F6, Favoritos:** quitar y recuperar con «Deshacer» se ve en el video, pero
  "agregar" no se hizo en esta sesión, porque la frase ya era favorita.
- **F7, foto en un pictograma de la app:** en vez de ponerle la foto a Agua, se
  creó un pictograma propio con foto. Sí queda respaldado que el avatar señala
  esa foto en «Decir». No se volvió al original.

**Cómo leerlo:** ninguna operación falló en modo avión. Las dos funciones que
faltan no fallaron: se hicieron distinto de lo que fija el protocolo. Igual, el
umbral es el 100 % de las funciones respaldadas, y el resultado es **no
cumple**. Lo que no se puede afirmar con este intento es que las doce funciones
de la 1.1 andan sin red: dos de ellas no quedaron probadas como las define el
protocolo.

## Criterio 13 — Persistencia

| | Primer intento | Segundo intento |
|---|---|---|
| Versión | 1.0 (`6caa5e3`) | 1.1 (`25099d3`) |
| Fecha | 2026-10-11 | 2026-11-03 |
| Elementos | 6: tipo de avatar, tamaño, modo oscuro, velocidad de la voz, dos pictogramas propios con foto | los mismos 6 |
| Antes y después | las capturas no distinguían cuáles eran posteriores a reabrir; lo comprobó el autor | capturas con hora antes (15:20 a 15:23) y después (15:24), y el DataStore leído del teléfono antes y después |
| Que la app se cerró | declarado | el proceso cambió (PID 2597 → 4924); el cierre desde recientes, en el video |
| Resultado | **cumple: 6 de 6**, según la comprobación del autor | **cumple: 6 de 6** |

**Queda respaldado:** los seis valores, iguales antes del cierre y después de
reabrir, en las capturas y en el DataStore. Las dos copias del DataStore son
idénticas byte a byte. El proceso de la app es otro después de reabrir, así que
lo que muestra salió del almacenamiento y no de la memoria.

**No queda respaldado, o queda distinto de lo previsto:**

- La comprobación de "ningún proceso" con la app cerrada (paso 8) no se hizo.
  El cierre queda probado por el cambio de PID.
- La velocidad de la voz se mantuvo en 27 %, un valor cercano al de fábrica
  (33 %). Es distinto y se mantuvo, pero el protocolo pedía un valor "bien
  distinto".
- Uno de los pictogramas propios se llama "Leche", como uno de la app. Se
  distingue por su foto y por su id en el DataStore.

## Para la Tabla 15

Las dos filas, con las columnas de la bitácora:

| Criterio | Versión | Fecha | Resultado | Evidencia |
|---|---|---|---|---|
| 11. Funcionamiento sin red | 1.1 (`25099d3`) | 2026-11-03 | **no cumple**: 10 de 12 funciones respaldadas en modo avión; F6 y F7 no se hicieron como las define el protocolo | `evidencia-2026-10/c11-v2-sin-red.md` |
| 13. Persistencia | 1.1 (`25099d3`) | 2026-11-03 | **cumple**: 6 de 6 tras cerrar desde recientes, con el proceso reemplazado | `evidencia-2026-10/c13-v2-persistencia.md` |

Si la tabla tiene que conservar el primer intento, sus filas siguen en la
bitácora: el 11 cumple 9 de 9 y el 13 cumple 6 de 6, los dos sobre la 1.0 y con
partes declaradas por el autor.
