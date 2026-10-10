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
| 1 | Correspondencia motor ↔ catálogo | 100 %, sin registros sobrantes | 2026-10-10 | `6caa5e3` | JVM | — | **cumple**: 88 de 88 acciones con registro, 0 sobrantes | `salidas-2026-10/criterio-01-02-inventario.txt` |
| 2 | Vocabulario alcanzable | 0 categorías o pictogramas sin acceso | 2026-10-10 | `6caa5e3` | JVM | — | **cumple**: 0 categorías y 0 pictogramas sin acceso | `salidas-2026-10/criterio-01-02-inventario.txt` |
| 3 | Equivalencia catálogo ↔ motor | 0 desvíos fuera de tolerancia (1 % del recorrido, piso 0,002) | 2026-10-10 | `6caa5e3` | JVM | — | **cumple**: 0 desvíos en 97 casos (48.597 comparaciones de pose) | `salidas-2026-10/criterio-03-equivalencia.txt` |
| 4 | Cobertura de representación | 0 valores fuera de rango, 0 excepciones, 0 no finitos | 2026-10-10 | `6caa5e3` | JVM | — | **cumple**: 0 fuera de rango, 0 excepciones, 0 no finitos | `salidas-2026-10/criterio-04-representacion.txt` |
| 5 | Resolución del corpus | ≥ 90 % | 2026-10-10 | `6caa5e3` | JVM | — | **cumple**: 97,3 % (107 de 110 oraciones) | `salidas-2026-10/criterio-05-corpus.txt` |
| 6 | Colisión léxica (pares trampa) | 0 | 2026-10-10 | `6caa5e3` | JVM | — | **cumple**: 0 colisiones en 10 pares | `salidas-2026-10/criterio-06-trampas.txt` |
| 7 | Cobertura semántica | medido, sin umbral | 2026-10-10 | `6caa5e3` | JVM | — | **medido**: 100 %, 0 de 168 pictogramas en la acción genérica | `salidas-2026-10/criterio-07-08-cobertura.txt` |
| 8 | Distinguibilidad | medido, sin umbral | 2026-10-10 | `6caa5e3` | JVM | — | **medido**: 16 pares indistinguibles | `salidas-2026-10/criterio-07-08-cobertura.txt` |
| 9 | Emisión de la oración completa | 100 % de los pictogramas | 2026-10-10 | `6caa5e3` | JVM | — | **cumple**: 100 % (168 de 168 pictogramas) | `salidas-2026-10/criterio-09-oracion.txt` |
| 10 | Latencia pictograma → voz | mediana ≤ 300 ms; ningún caso > 1.000 ms (50 pulsaciones, frases nuevas de 1 a 6 pictogramas; arranque en frío informado aparte) | 2026-10-06 | `a0accaf` | Galaxy A57 | 16 (API 36) | **cumple**: mediana 73 ms, máximo 96 ms, 0 casos > 1.000 ms en 50 mediciones; arranque en frío 193 ms. **Combina los intentos 2 y 3** (ver notas) | `datos/latencias-2026-10.csv` |
| 11 | Funcionamiento sin red | 100 % de las funciones en modo avión | | | | | | |
| 12 | Permisos | 0 permisos de red o cámara en el manifiesto combinado | 2026-10-10 | `6caa5e3` | Galaxy A57 | 16 (API 36) | **cumple**: 0 permisos de red o cámara. Único `uses-permission`: el propio de la app, de nivel `signature`, que agrega `androidx.core`. El teléfono informa "No se requieren permisos" (captura con la build de `a0accaf`; el manifiesto combinado de `6caa5e3` es idéntico byte a byte) | `evidencia-2026-10/c12-c16-manifiesto.md`, `evidencia-2026-10/c12-permisos-telefono.jpg` |
| 13 | Persistencia | 100 % de ajustes y pictogramas propios tras cerrar desde recientes | | | | | | |
| 14 | Accesibilidad | 0 objetivos < 48 dp en pictogramas y «Decir»; contraste ≥ 4,5:1; el resto se reporta | 2026-10-10 | `6caa5e3` | Galaxy A57 | 16 (API 36) | **cumple**: 0 objetivos táctiles < 48 dp y 0 textos con contraste < 4,5:1 en las 4 pantallas. Se reportan 2 descripciones repetidas ("Volver" dos veces; la pestaña "Recientes" igual que el título) y 1 texto dentro de una foto de prueba. Antes del cambio de contraste (`a0accaf`) **no cumplía**: ver notas | `evidencia-2026-10/c14-sugerencias-6caa5e3.md` y las capturas `c14-despues-*.jpg` |
| 15 | Pictograma propio con foto | 100 % de los pasos: crear, ver, decir, reabrir, eliminar (el archivo debe desaparecer) | | | | | | |
| 16 | Resguardo de las fotografías | 0 fotos fuera del almacenamiento privado o incluidas en el respaldo automático | | | | | | |

---

## Cómo se llena cada columna

| Columna | Qué va |
|---|---|
| Fecha | día de la medición, `AAAA-MM-DD` |
| Commit | hash corto del código medido: el último commit que tocó la app, `git log --oneline -1 -- app`. Los commits que solo agregan documentos o salidas no cambian el código medido. Si había cambios sin commitear al medir, la medición no vale: se commitea y se repite |
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
| 10 | **Ninguna sesión individual cumplió el protocolo** (tres intentos, todos conservados en `datos/`). El autor decidió combinar los intentos 2 y 3 con una regla fijada antes de calcular, que no depende de los valores: cada una de las 51 oraciones del protocolo se toma del intento más reciente donde quedó medida como nueva. Resultado: 15 oraciones del intento 2 (incluido el arranque en frío) y 36 del intento 3, cada una medida una sola vez. Mismo día, mismo commit, mismo dispositivo, con 13 minutos entre sesiones. Ninguna de las 36 del intento 3 cae en el rango de la caché (5 a 11 ms), así que el motor no las sirvió de una sesión anterior. El CSV conserva el intento y el número de pulsación original de cada fila. **Las dos sesiones son comparables:** 36 oraciones quedaron medidas como nuevas en ambas, y la misma oración difiere de una sesión a la otra en 4 ms de mediana (16 ms como máximo); la mediana de esas 36 es 70 ms en el intento 2 y 74 ms en el 3 |
| 10 | La latencia crece con la oración: mediana de 61 ms con 1 pictograma, 65 con 2, 68 con 3, 76 con 4, 79 con 5 y 85,5 con 6. Unos 5 ms por pictograma, el costo de decir la oración entera en cada toque. Las oraciones repetidas, en cambio, tardan de 5 a 11 ms (caché del motor), y eso justifica que el protocolo exija oraciones nuevas |
| 14 | **Primera medición, sobre `a0accaf`: no cumplía.** Accessibility Scanner encontró 19 sugerencias, 5 de ellas de contraste de texto: 2,49:1 en "Agregar a favoritos" y "Agregar foto", 3,23:1 en "Listo", y las pestañas de Recientes/Favoritos, todos en el azul principal `#5B8DEF`. Se corrigió como cambio de producto en `6caa5e3` (azul `#2E5AAC`, y nombres en pictogramas y categorías para el lector de pantalla), y se volvió a medir: de 19 sugerencias a 3, ninguna de contraste ni de objetivo táctil. Evidencia de la primera medición: `evidencia-2026-10/c14-sugerencias.md` y `c14-pantalla-*`, `c14-lista-*` |
| 16 | Parte B (`bmgr`) **no realizable**: el servicio de respaldo está deshabilitado en el A57, y con el servicio deshabilitado Android responde "Backup is not allowed" a cualquier paquete, así que la respuesta no prueba la configuración de la app. No se activó el servicio. El criterio se apoya en la parte A (manifiesto) y en la C (galería y almacenamiento privado). Salida en `evidencia-2026-10/c16-bmgr.txt` |
| 11 | Dato a favor, que no reemplaza la prueba del criterio: la misma pantalla de Ajustes del criterio 12 informa **"Datos móviles: No se han usado datos"** para AgustiSpeaks (captura del 2026-10-10) |
| 1–9 | **Los nueve se midieron en una sola corrida, sin errores ni repeticiones**: el 10-10 a las 07:59, sobre `6caa5e3`, un minuto después del commit y con el árbol de trabajo limpio. Todas las pruebas pasaron en esa corrida. Corridas anteriores con exactamente los mismos veredictos, que no se citan: 6-10 12:54 sobre `a0accaf` (antes del cambio de contraste; sigue en el historial), y dos hechas por la compilación con el código todavía sin commitear (5-10 19:02 y 10-10 07:57). Entre `a0accaf` y `6caa5e3` solo cambia el conteo de líneas de `ui.components` y `ui.theme` |
| 16 | Hasta el 5-10 las reglas de respaldo estaban vacías, y fotos y datos entraban en la copia automática. El autor eligió excluir todo (opción A): `allowBackup="false"` más exclusiones en las dos vías de Android 12 o posterior, nube y transferencia entre teléfonos. Costo aceptado: al cambiar de teléfono se pierde lo personalizado. Un respaldo bien diseñado queda para otra fase |
