# Criterio 13 — Persistencia

Galaxy A57, Android 16 (API 36). App: build de depuración de `6caa5e3`.
Fecha: 2026-10-11, alrededor de las 08:36 a 08:42.

**Umbral:** 100 % de los ajustes y pictogramas propios tras cerrar desde
recientes.

## Qué se cambió

| # | Qué | Naturaleza del dato | Antes | Después |
|---|---|---|---|---|
| A1 | Tipo de avatar | opción | Persona | Robot |
| A2 | Tamaño de pictogramas | escala | Normal | Grande |
| A3 | Modo oscuro | sí / no | apagado | encendido |
| A4 | Velocidad de la voz | número | 33 % | 12 % |
| P1 | Pictograma propio **"Mi taza"** | — | — | Cocina → Alimentos, con foto |
| P2 | Pictograma propio **"Imanes"** | — | — | Cocina → Acciones, con foto |

El protocolo proponía "Mi mochila" (Escuela) y "Pelota roja" (Juego), en
Colegio. Se usaron otros nombres y otro lugar; el criterio pide dos pictogramas
propios, y ninguno de los dos nombres coincide con palabras de la app. "Mi taza"
es un pictograma nuevo, distinto del que se creó y se borró en el criterio 15.

## Capturas

**Estado inicial y primeros cambios**, en modo claro. Según el autor, son unos
dos minutos anteriores a las demás; no muestran el reloj, porque en modo claro
los íconos de la barra de estado no se ven.

| Archivo | Qué muestra |
|---|---|
| `c13-01-antes-avatar-persona.jpg` | A1 = Persona |
| `c13-02-antes-config-normal.jpg` | A2 = Normal, A3 = apagado |
| `c13-03-antes-voz-33.jpg` | A4 = 33 % |
| `c13-04-cambio-avatar-robot.jpg` | A1 cambiado a Robot, todavía en modo claro |
| `c13-05-cambio-tamano-grande.jpg` | A2 cambiado a Grande, todavía en modo claro |
| `c13-06-config-arriba.jpg` | parte superior de Configuraciones |

**Con los cambios aplicados**, en modo oscuro, de 08:38 a 08:42:

| Archivo | Hora | Qué muestra |
|---|---|---|
| `c13-07-con-cambios-config-oscuro-grande.jpg` | 08:38 | A3 encendido, A2 Grande |
| `c13-08-con-cambios-voz-12-robot.jpg` | 08:38 | A4 12 %, A1 Robot |
| `c13-09-con-cambios-decir-yo-quiero.jpg` | 08:38 | la tarjeta de «Decir» con el robot |
| `c13-10-con-cambios-decir-mi-taza.jpg` | 08:39 | "Yo Quiero Mi taza": el robot señala la foto de P1 |
| `c13-11-con-cambios-decir-imanes.jpg` | 08:40 | "Yo Quiero Imanes": el robot señala la foto de P2 |
| `c13-12-con-cambios-alimentos-mi-taza.jpg` | 08:41 | P1 en Alimentos, con su foto |
| `c13-13-con-cambios-acciones-imanes.jpg` | 08:42 | P2 en Acciones, con su foto |

## Cierre desde recientes y reapertura

La app se cerró desde la vista de recientes y se volvió a abrir. **Las capturas
no permiten distinguir cuáles se tomaron después de reabrir.** La persistencia
de los seis elementos la comprobó el autor en el dispositivo, incluida la
velocidad de la voz, al oído: "todo funciona y se mantienen las
configuraciones". No se hizo la comprobación opcional con Logcat.

## Resultado

**Cumple: 6 de 6**, según la comprobación del autor en el dispositivo, con las
capturas como respaldo del estado inicial y del estado con los cambios.
