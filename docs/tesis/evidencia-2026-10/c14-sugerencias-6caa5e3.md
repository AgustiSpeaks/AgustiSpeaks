# Criterio 14 — Accesibilidad, después del cambio de contraste (`6caa5e3`)

Galaxy A57, Android 16 (API 36). App: build de depuración de `6caa5e3`, tamaño
de texto por defecto. Fecha: 2026-10-10, entre las 08:01 y las 08:02.

**Umbral:** 0 objetivos táctiles menores a 48 dp en los pictogramas y en
«Decir»; contraste de texto ≥ 4,5:1; el resto se reporta.

La medición anterior, sobre `a0accaf`, está en
[`c14-sugerencias.md`](c14-sugerencias.md): 19 sugerencias, 5 de contraste.

## Capturas

| Pantalla | Captura de la app | Resultado del Scanner |
|---|---|---|
| 1. Colegio → Acciones | `c14-despues-pantalla-1-acciones.jpg` | 1 sugerencia; detalle en `c14-despues-detalle-1-volver.jpg` |
| 2. Colegio, lista de categorías | `c14-despues-pantalla-2-categorias-app.jpg` | `c14-despues-pantalla-2-sin-sugerencias.jpg` (08:01) |
| 3. Tarjeta de «Decir» ("Yo Quiero") | `c14-despues-pantalla-3-decir-app.jpg` | `c14-despues-pantalla-3-sin-sugerencias.jpg` (08:02) |
| 4. Recientes / Favoritos | `c14-despues-pantalla-4-recientes.jpg` | `c14-despues-lista-4-recientes.jpg` |

Las dos pantallas de "Sin sugerencias" del Scanner no indican a qué pantalla de
la app corresponden. Se asignan por la hora de la captura y por las capturas de
la app tomadas junto a cada una. Como las dos informan cero sugerencias, el
resultado no depende de la asignación.

## Sugerencias

| Pantalla | # | Tipo | Coordenadas | Elemento | Decide |
|---|---|---|---|---|---|
| 1 | 1 | Descripciones de elementos (repetida) | [1104,478][1239,613] | flecha ← del panel de categoría: "Volver", "idéntico al de otros 1 elementos" (la flecha de la barra superior) | no |
| 2 | — | ninguna | — | — | — |
| 3 | — | ninguna | — | — | — |
| 4 | 1 | Descripciones de elementos (repetida) | [0,264][1102,399] | pestaña "Recientes", igual que el título de la pantalla | no |
| 4 | 2 | Texto no expuesto | [34,433][2171,715] | fila de "Aaw": texto dentro de su foto, que es una captura de la app | no |

## Antes y después

| | `a0accaf` | `6caa5e3` |
|---|---|---|
| Objetivo táctil < 48 dp | 0 | **0** |
| Contraste de texto < 4,5:1 | 5 (peor: 2,49:1) | **0** |
| Etiqueta del elemento | 11 | **0** |
| Descripciones repetidas | 2 | 2 |
| Texto no expuesto | 1 | 1 |
| **Total** | **19** | **3** |

El cambio (`6caa5e3`): el azul principal del modo claro pasó de `#5B8DEF` a
`#2E5AAC`, y pictogramas y tarjetas de categoría llevan su nombre como
descripción.

## Resultado

**Cumple.** 0 objetivos táctiles menores a 48 dp y 0 textos con contraste
menor a 4,5:1. Se reportan 2 descripciones repetidas y 1 texto dentro de una
foto de prueba, que no deciden el criterio.
