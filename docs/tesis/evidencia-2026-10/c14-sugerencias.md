# Criterio 14 — Accesibilidad: sugerencias de Accessibility Scanner

Galaxy A57, Android 16 (API 36). App: build de depuración de `a0accaf`, tamaño
de texto por defecto. Fecha: 2026-10-10, entre las 07:29 y las 07:33.

**Umbral:** 0 objetivos táctiles menores a 48 dp en los pictogramas y en
«Decir»; contraste de texto ≥ 4,5:1; el resto se reporta.

## Capturas

| Pantalla | Resultado sobre la app | Lista de sugerencias |
|---|---|---|
| 1. Colegio → Acciones | `c14-pantalla-1-acciones.jpg` (7) | `c14-lista-1-acciones.jpg` |
| 2. Colegio, lista de categorías | `c14-pantalla-2-categorias.jpg` (5) | `c14-lista-2-categorias.jpg` |
| 3. Tarjeta de «Decir» ("Yo Quiero") | `c14-pantalla-3-decir.jpg` (3) | `c14-lista-3-decir.jpg` |
| 4. Recientes / Favoritos | `c14-pantalla-4-recientes.jpg` (4) | `c14-lista-4-recientes.jpg` |

## Sugerencias, una por renglón

Las coordenadas son las que informa el Scanner, en píxeles de la pantalla en
horizontal (2340 × 1080).

| Pantalla | # | Tipo | Coordenadas | Elemento | Decide |
|---|---|---|---|---|---|
| 1 | 1 | Etiqueta del elemento | [34,961][356,1080] | pictograma del panel Palabras, cortado por el borde inferior | no |
| 1 | 2 | Etiqueta del elemento | [390,961][712,1080] | ídem | no |
| 1 | 3 | Etiqueta del elemento | [746,961][1067,1080] | ídem | no |
| 1 | 4 | Descripciones de elementos (repetida) | [1104,478][1239,613] | flecha ← del panel de categoría: "Volver", igual que la de la barra superior | no |
| 1 | 5–7 | **[pendiente de ver en la lista]** | — | por los recuadros: los tres pictogramas de Acciones cortados por el borde inferior | — |
| 2 | 1 | Etiqueta del elemento | [34,961][356,1080] | pictograma del panel Palabras, cortado | no |
| 2 | 2 | Etiqueta del elemento | [390,961][712,1080] | ídem | no |
| 2 | 3 | Etiqueta del elemento | [746,961][1067,1080] | ídem | no |
| 2 | 4 | Etiqueta del elemento | [1494,919][1816,1080] | tarjeta de categoría cortada (Familia) | no |
| 2 | 5 | **[pendiente de ver en la lista]** | — | por el recuadro: tarjeta de categoría cortada (Juego) | — |
| 3 | 1 | **Contraste del texto** | [1344,439][1776,507] | "Agregar a favoritos" | **sí** |
| 3 | 2 | **Contraste del texto** | [1418,608][1702,676] | "Agregar foto" | **sí** |
| 3 | 3 | **Contraste del texto** | [1467,777][1578,845] | "Listo" | **sí** |
| 4 | 1 | Descripciones de elementos (repetida) | [0,264][1102,399] | pestaña "Recientes", igual que el título de la pantalla | no |
| 4 | 2 | **Contraste del texto** | [500,303][677,360] | texto de la pestaña "Recientes" | **sí** |
| 4 | 3 | **Contraste del texto** | [1608,303][1772,360] | texto de la pestaña "Favoritos" | **sí** |
| 4 | 4 | Texto no expuesto | [34,433][2171,715] | fila de "Aaw": su foto es una captura de la app, y el Scanner leyó el texto "Nuevo pictograma en…" dentro de la imagen | no |

### Proporciones de contraste informadas por el Scanner

Leídas por el autor al desplegar cada sugerencia, el 2026-10-10:

| Elemento | Proporción actual | Proporción que sugiere el Scanner |
|---|---|---|
| "Agregar a favoritos" | **2,49:1** | 4,50:1 |
| "Agregar foto" | **2,49:1** | 4,50:1 |
| "Listo" | **3,23:1** | 4,50:1 |
| Pestañas "Recientes" y "Favoritos" | no se leyeron | — |

Las tres coinciden con el cálculo de WCAG sobre los colores del tema: el azul
`#5B8DEF` sobre el fondo de la tarjeta (`#E6E0E9`) da 2,49:1, y el texto blanco
sobre ese azul da 3,23:1.

## Lectura

**Objetivos táctiles menores a 48 dp: 0** en las sugerencias vistas (las
pendientes 1.5–1.7 y 2.5 están por confirmar).

**Contraste de texto por debajo de 4,5:1: 5.** Son textos de tamaño normal, y
para esos el Scanner usa 4,5:1 como límite: cada uno marcado está por debajo del
umbral. Los cinco son texto en el azul principal de la app, o texto blanco sobre
ese azul. Los textos de los pictogramas y de las tarjetas de categoría no
aparecen marcados.

**Se reportan, sin decidir:**

- **Etiquetas faltantes en elementos cortados (11 vistas o inferidas).** Solo
  aparecen en pictogramas y tarjetas que el borde de la pantalla corta por
  debajo: el nombre queda fuera de la vista y el botón se queda sin texto
  legible. Los que se ven enteros no aparecen. Un lector de pantalla los
  anunciaría sin nombre al llegar a ellos.
- **Descripciones repetidas (2):** dos botones "Volver" en la misma pantalla, y
  la pestaña "Recientes" con el mismo texto que el título.
- **Texto no expuesto (1):** artefacto de la foto de prueba, no de la app.

## Resultado

**No cumple**, por contraste: 5 textos por debajo de 4,5:1, el peor en 2,49:1.
La parte de objetivos táctiles cumple en lo visto.

## Qué pasó después

El autor decidió corregirlo como cambio de producto: el azul principal del modo
claro pasa a `#2E5AAC`, y pictogramas y tarjetas de categoría llevan su nombre
como descripción. **Esta medición queda como el "antes"**, sobre `a0accaf`, y
se reemplaza por una nueva sobre el commit del cambio. Por eso no se completaron
las sugerencias 1.5–1.7 y 2.5 ni las proporciones de las pestañas.
