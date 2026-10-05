# Datos para las figuras

Salidas crudas de medición, en CSV, listas para graficar.

---

## `latencias.csv` — figura 4

Latencia entre tocar un pictograma y el instante en que el motor de voz empieza
a emitir sonido. Medida por la instrumentación de `validacion/Validacion.kt`,
leída de Logcat con el tag `VALIDACION`.

| | |
|---|---|
| Fecha | 2026-09-26, 08:32–08:34 |
| Dispositivo | Samsung — *[CONFIRMAR modelo]* |
| Android | *[CONFIRMAR versión]* |
| Resolución | 2340 × 1080, densidad 2,8125 (del log) |
| Build | depuración |
| Mediciones | 25 |

### Los tres regímenes

La columna `regimen` es lo más importante del archivo. **Promediar las 25
mediciones juntas no describe ninguna situación real**, porque hay tres
comportamientos distintos mezclados:

| Régimen | n | Media | Mediana | Desvío | Mín | Máx |
|---|---|---|---|---|---|---|
| `arranque_frio` | 1 | 599 | — | — | 599 | 599 |
| `sintesis_nueva` | 19 | **31,6** | 30 | 4,5 | 25 | 39 |
| `cache` | 5 | 9,4 | 8 | 4,0 | 6 | 16 |

**`arranque_frio`** es la primera locución de la sesión, cuando el motor de voz
todavía está armando su tubería de síntesis. Ocurre una vez por sesión y hay que
declararlo por separado: esconder 599 ms dentro de una media sería deshonesto, y
tampoco es despreciable — es medio segundo la primera vez que el chico toca algo.

**`sintesis_nueva`** es el régimen normal y el que la tesis debería citar: una
frase que el motor no sintetizó antes en esta sesión. Es notablemente estable,
catorce milisegundos entre el mejor y el peor caso.

**`cache`** son frases repetidas. La clasificación no es una suposición: se ve
directamente en los datos, porque cada valor bajo corresponde a una frase ya
dicha antes en la misma tanda.

| Frase | Primera vez | Repeticiones |
|---|---|---|
| "Quiero" | 599 | 10 |
| "Ayuda" | 30 | 16 |
| "Gracias" | 39 | 6, 7, 8 |

El caché parece indexarse por el texto exacto de la locución: "Gracias" sola dio
39 ms aunque "Por favor Gracias" se había sintetizado dos pulsaciones antes.

### Lo que estos datos NO sostienen

Una tanda anterior sugería que la latencia crece unos **5 ms por pictograma
agregado**, lo cual tendría sentido: la regla central de la app es que cada
pictograma lee la oración acumulada entera, así que el motor sintetiza más texto
antes de emitir.

Esta tanda **no lo confirma ni lo refuta**: tiene 19 frases de un pictograma y
solo 3 de dos, y ninguna de tres o cuatro. Para afirmarlo en la tesis hace falta
una tanda dedicada, con secuencias que acumulen hasta cinco o seis pictogramas y
todas las frases nuevas.

---

## Pendientes

- **Figura 2 (cobertura por categoría, antes y después).** El "después" está
  medido: `docs/tesis/salidas/F2-cobertura.txt`. El "antes" **solo existe como
  total**, 59 de 170 pictogramas en la acción genérica. El desglose por categoría
  de la línea base no se guardó, y no se puede reconstruir porque el resolvedor
  cambió desde entonces. La medición es anterior a que el proyecto tuviera git.
- **Figura 3 (fotogramas por versión).** Los valores existen —1.088, 1.010, 736,
  279, 291, 421, 419— pero el orden entre 279 y 291 está inferido de un artefacto
  en disco, no verificado por historial. Ver `cifras-estaticas.md`, sección 4.
- **Tanda de acumulación** para decidir lo del incremento por pictograma.
