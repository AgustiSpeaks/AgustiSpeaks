# Bitácora de verificación — octubre de 2026

Un renglón por criterio. Cada resultado tiene que poder reproducirse: por eso
lleva el commit del código medido, el dispositivo y dónde está la evidencia.

Umbrales fijados por el autor el **5-10-2026**, antes de cualquier medición. No
se cambian después de ver un resultado: si un criterio no se cumple, se anota
"no cumple" y ese es el dato.

Dispositivo de pruebas: Samsung Galaxy A57, Android 16 (API 36).

---

## Criterios

| # | Criterio | Umbral | Fecha | Commit | Dispositivo | Android | Resultado | Evidencia |
|---|---|---|---|---|---|---|---|---|
| 1 | Correspondencia motor ↔ catálogo | 100 %, sin registros sobrantes | | | | | | |
| 2 | Vocabulario alcanzable | 0 categorías o pictogramas sin acceso | | | | | | |
| 3 | Equivalencia catálogo ↔ motor | 0 desvíos fuera de tolerancia (1 % del recorrido, piso 0,002) | | | | | | |
| 4 | Cobertura de representación | 0 valores fuera de rango, 0 excepciones, 0 no finitos | | | | | | |
| 5 | Resolución del corpus | ≥ 90 % | | | | | | |
| 6 | Colisión léxica (pares trampa) | 0 | | | | | | |
| 7 | Cobertura semántica | medido, sin umbral | | | | | | |
| 8 | Distinguibilidad | medido, sin umbral | | | | | | |
| 9 | Emisión de la oración completa | 100 % de los pictogramas | | | | | | |
| 10 | Latencia pictograma → voz | mediana ≤ 300 ms; ningún caso > 1.000 ms (50 pulsaciones, frases nuevas de 1 a 6 pictogramas; arranque en frío informado aparte) | | | | | | |
| 11 | Funcionamiento sin red | 100 % de las funciones en modo avión | | | | | | |
| 12 | Permisos | 0 permisos de red o cámara en el manifiesto combinado | | | | | | |
| 13 | Persistencia | 100 % de ajustes y pictogramas propios tras cerrar desde recientes | | | | | | |
| 14 | Accesibilidad | 0 objetivos < 48 dp en pictogramas y «Decir»; contraste ≥ 4,5:1; el resto se reporta | | | | | | |
| 15 | Pictograma propio con foto | 100 % de los pasos: crear, ver, decir, reabrir, eliminar (el archivo debe desaparecer) | | | | | | |
| 16 | Resguardo de las fotografías | 0 fotos fuera del almacenamiento privado o incluidas en el respaldo automático | | | | | | |

---

## Cómo se llena cada columna

| Columna | Qué va |
|---|---|
| Fecha | día de la medición, `AAAA-MM-DD` |
| Commit | hash corto del código medido (`git log --oneline -1`). Si había cambios sin commitear al medir, la medición no vale: se commitea y se repite |
| Dispositivo | `Galaxy A57` para los criterios en el teléfono; `JVM` para las pruebas automatizadas (1 a 9), que no usan dispositivo |
| Android | `16 (API 36)`, o `—` en las pruebas de la JVM |
| Resultado | **cumple** / **no cumple** con la cifra medida. Los criterios 7 y 8 no llevan umbral: se anota **medido** y la cifra |
| Evidencia | ruta al archivo: `salidas-2026-10/…` para las automatizadas, `datos/…` para la latencia, y la carpeta de capturas para las manuales |

## Notas

Hechos conocidos antes de medir, para que no se lean como sorpresas en la
tabla. Los detalles están en [estado-2026-10-05.md](estado-2026-10-05.md), §8.

| # | Nota |
|---|---|
| 4 | `HOT` se corrigió en `842b0f9` antes de medir. Sin ese cambio, el criterio fallaba por la extensión de brazo negativa |
| 15 | La foto se borra en el momento de eliminar el pictograma desde `d4147ab`. Antes desaparecía recién en el siguiente arranque de la app |
| 16 | Las reglas de respaldo están vacías: con el código actual, fotos y datos **entran** en la copia automática. La etapa 5 propone el cambio, que espera la confirmación del autor |
