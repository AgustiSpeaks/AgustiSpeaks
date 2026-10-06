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
| 1 | Correspondencia motor ↔ catálogo | 100 %, sin registros sobrantes | 2026-10-06 | `a0accaf` | JVM | — | **cumple**: 88 de 88 acciones con registro, 0 sobrantes | `salidas-2026-10/criterio-01-02-inventario.txt` |
| 2 | Vocabulario alcanzable | 0 categorías o pictogramas sin acceso | 2026-10-06 | `a0accaf` | JVM | — | **cumple**: 0 categorías y 0 pictogramas sin acceso | `salidas-2026-10/criterio-01-02-inventario.txt` |
| 3 | Equivalencia catálogo ↔ motor | 0 desvíos fuera de tolerancia (1 % del recorrido, piso 0,002) | 2026-10-06 | `a0accaf` | JVM | — | **cumple**: 0 desvíos en 97 casos (48.597 comparaciones de pose) | `salidas-2026-10/criterio-03-equivalencia.txt` |
| 4 | Cobertura de representación | 0 valores fuera de rango, 0 excepciones, 0 no finitos | 2026-10-06 | `a0accaf` | JVM | — | **cumple**: 0 fuera de rango, 0 excepciones, 0 no finitos | `salidas-2026-10/criterio-04-representacion.txt` |
| 5 | Resolución del corpus | ≥ 90 % | 2026-10-06 | `a0accaf` | JVM | — | **cumple**: 97,3 % (107 de 110 oraciones) | `salidas-2026-10/criterio-05-corpus.txt` |
| 6 | Colisión léxica (pares trampa) | 0 | 2026-10-06 | `a0accaf` | JVM | — | **cumple**: 0 colisiones en 10 pares | `salidas-2026-10/criterio-06-trampas.txt` |
| 7 | Cobertura semántica | medido, sin umbral | 2026-10-06 | `a0accaf` | JVM | — | **medido**: 100 %, 0 de 168 pictogramas en la acción genérica | `salidas-2026-10/criterio-07-08-cobertura.txt` |
| 8 | Distinguibilidad | medido, sin umbral | 2026-10-06 | `a0accaf` | JVM | — | **medido**: 16 pares indistinguibles | `salidas-2026-10/criterio-07-08-cobertura.txt` |
| 9 | Emisión de la oración completa | 100 % de los pictogramas | 2026-10-06 | `a0accaf` | JVM | — | **cumple**: 100 % (168 de 168 pictogramas) | `salidas-2026-10/criterio-09-oracion.txt` |
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
| 5 | Los 3 desvíos son los mismos que en septiembre ("No quiero comer", "Buscar mi juguete", "Quiero llorar"): el resolvedor no cambió. Son de orden de evaluación, no de vocabulario |
| 8 | **La cifra depende de cómo están agrupadas las palabras**, porque los pares se cuentan dentro de cada categoría. Respecto de septiembre aparece "Ir" = "Caminar" (los dos en Acciones desde que "Ir" dejó de ser palabra núcleo) y desaparecen los tres pares de señalar (Tú, Esto, Aquí), que quedaron en categorías distintas. Las animaciones no cambiaron |
| 1–9 | Hubo una corrida anterior, el 5-10 a las 19:02, con el código todavía sin commitear. Dio exactamente los mismos resultados, pero no se cita: la medición válida es la del 6-10, sobre `a0accaf` |
| 16 | Hasta el 5-10 las reglas de respaldo estaban vacías, y fotos y datos entraban en la copia automática. El autor eligió excluir todo (opción A): `allowBackup="false"` más exclusiones en las dos vías de Android 12 o posterior, nube y transferencia entre teléfonos. Costo aceptado: al cambiar de teléfono se pierde lo personalizado. Un respaldo bien diseñado queda para otra fase |
