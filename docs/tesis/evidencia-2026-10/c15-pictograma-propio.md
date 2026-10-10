# Criterio 15 — Pictograma propio con foto

Galaxy A57, Android 16 (API 36). App: build de depuración de `6caa5e3`.
Fecha: 2026-10-10, entre las 08:14 y las 08:27.

**Umbral:** 100 % de los pasos (crear, ver, decir, reabrir, eliminar), y el
archivo de la foto tiene que desaparecer al eliminar.

## Pictogramas usados

| | Nombre | Categoría | Origen de la foto | Archivo en `files/pictogifts/` |
|---|---|---|---|---|
| P1 | **Peluchito** | Juego | cámara, desde la app | `a363c6df-5547-44d5-a0ed-504678ca7230.jpg`, 144 KB, 08:15 |
| P2 | **Mi taza** | Bebidas | galería | `b1bf1171-4481-4dff-b8e1-df1e13d035d2.jpg`, 107,6 KB, 08:20 |

Fotos de objetos, sin personas. El protocolo proponía "Camioncito" para P1; se
usó "Peluchito", que tampoco coincide con ninguna palabra de la app. El
resolvedor reconoce "peluche" solo como palabra completa, así que "peluchito"
no la activa.

Antes de empezar, la carpeta tenía 2 archivos de pruebas anteriores (del
2026-10-05), que no se tocaron.

## Pasos

| Paso | Qué se comprobó | Resultado | Evidencia |
|---|---|---|---|
| 15.1 | Carpeta antes | 2 archivos | `c15-01-carpeta-antes.png` |
| 15.3 | Crear P1 con la cámara; la vista previa muestra la foto y el color de Juego | **sí** | `c15-03-formulario-peluchito.jpg` |
| 15.3 | Aparece el archivo de P1 en el almacenamiento privado | **sí** (3 archivos) | `c15-02-carpeta-con-peluchito.png` |
| 15.4 | Crear P2 desde la galería | **sí** | `c15-05-formulario-mi-taza.jpg` |
| 15.5 | P1 al final de Juego, con su foto en lugar del ícono | **sí** | `c15-04-juego-con-peluchito.jpg` |
| 15.5 | P1 aparece también en Living, otro lugar con Juego | **sí** | `c15-07-living-con-peluchito.jpg` |
| 15.5 | Hay un archivo por cada pictograma | **sí**: `a363c6df…` y `b1bf1171…` | `c15-02-…`, `c15-08-…` |
| 15.6 | La voz dice "Quiero Peluchito" | **sí** (comprobado al oído por el autor) | — |
| 15.6 | La tarjeta de «Decir» muestra al avatar señalando la foto | **sí** | `c15-06-decir-peluchito-living.jpg` |
| 15.7 | Cerrar desde recientes y reabrir: los dos siguen, con su foto | **sí** (comprobado por el autor) | — |
| 15.8 | Eliminar P1: desaparece del tablero y **su archivo desaparece sin reiniciar la app** | **sí** | `c15-08-carpeta-sin-peluchito.png` |
| 15.9 | Eliminar P2: su archivo desaparece | **sí** | `c15-09-carpeta-sin-mi-taza.png` |

## Por qué se sabe que el archivo se borró en el momento

Las fechas de Device Explorer:

| Elemento | Fecha | Qué indica |
|---|---|---|
| `files/profileInstalled` | 08:22 | se reescribe cada vez que arranca la app: es la reapertura del paso 15.7 |
| carpeta `pictogifts`, captura de las 08:24 | 08:24 | se modificó al borrar P1 |
| carpeta `pictogifts`, captura de las 08:25 | 08:25 | se modificó al borrar P2 |

Entre las 08:22 y las 08:25 la app no volvió a arrancar: `profileInstalled`
sigue en 08:22 en las dos capturas. Los archivos desaparecieron **sin un
arranque de por medio**, así que los borró el borrado inmediato (`d4147ab`) y
no la limpieza que corre al arrancar.

## La galería al final

`c15-10-galeria-final.jpg` muestra la foto de la taza. Es la foto de origen que
el autor sacó antes con la cámara del teléfono para elegirla en el paso 15.4,
no una copia hecha por la app. La foto del peluche, sacada desde AgustiSpeaks,
no aparece en la galería: las imágenes del peluche que se ven ahí son capturas
de pantalla de la app.

## Resultado

**Cumple: 100 % de los pasos.** Crear (cámara y galería), ver (en dos lugares),
decir (voz y avatar señalando la foto), reabrir y eliminar, con el archivo de
la foto desaparecido en el momento.
