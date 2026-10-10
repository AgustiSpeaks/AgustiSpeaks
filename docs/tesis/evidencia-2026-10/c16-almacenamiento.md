# Criterio 16, parte C — dónde queda la foto sacada con la cámara

Galaxy A57, Android 16 (API 36). App: build de depuración de `6caa5e3`.
Fecha: 2026-10-10.

## Qué se hizo

Durante el criterio 15 se creó el pictograma propio **"Peluchito"** (Juego),
sacando la foto con la cámara desde la app, a las 08:15.

## Comprobaciones

| # | Comprobación | Resultado | Evidencia |
|---|---|---|---|
| C.1 | Galería antes de sacar la foto | registrada (08:14) | `c16-galeria-antes.jpg` |
| C.2 | La foto está en el almacenamiento privado de la app | **sí**: `/data/data/com.tunombre.agustispeaks/files/pictogifts/a363c6df-5547-44d5-a0ed-504678ca7230.jpg`, 144 KB, 2026-10-10 08:15. La carpeta pasó de 2 a 3 archivos | `c15-01-carpeta-antes.png`, `c15-02-carpeta-con-peluchito.png` |
| C.3 | No apareció ninguna foto nueva en la Galería | **sí**: las únicas imágenes nuevas son capturas de pantalla tomadas por el autor durante la prueba | `c16-galeria-despues.jpg` (08:16) |
| C.4 | `/sdcard/DCIM/Camera` no tiene un archivo de la hora de la foto | **sí**: ningún archivo creado en `DCIM/Camera` en los 90 minutos anteriores | listado de abajo |

## Listado de archivos creados en los últimos 90 minutos (C.4)

Leído con `adb shell find … -type f -mmin -90` a las 08:18:46 (hora del
teléfono). Se buscaron **solo** archivos recientes, para no listar el resto de
las fotos del dispositivo.

`/sdcard/DCIM`: 20 archivos, **todos** en `DCIM/Screenshots/`: capturas de
pantalla del autor durante las pruebas (Accessibility Scanner, Galería, Ajustes
y AgustiSpeaks), entre las 07:17 y las 08:16. **Ninguno en `DCIM/Camera/`.**

`/sdcard/Pictures`: 22 archivos en `Pictures/.thumbnails/`, que son las
miniaturas que genera la Galería para mostrar las imágenes del dispositivo. No
se abrieron, por ser imágenes personales. La foto del pictograma no puede tener
miniatura ahí: está en el almacenamiento privado de la app, que la Galería no
indexa.

## Otro archivo que conviene declarar

La cámara escribe primero en `cache/camara/captura.jpg`, dentro de la carpeta
privada de la app, y la app la copia achicada a `files/pictogifts/`. Ese
temporal queda en `cache/` hasta el próximo arranque de la app, donde se borra.
Es almacenamiento privado, y además `cache/` nunca entra en la copia de
seguridad de Android.

## Resultado de la parte C

**Cumple**: la foto quedó solo en el almacenamiento privado de la app, y la
cámara del Galaxy A57 no guardó una copia propia.
