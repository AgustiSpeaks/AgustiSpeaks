# Material de tesis — AgustiSpeaks

Evidencia de la versión de octubre de 2026. La tesis cita **solo** mediciones
hechas sobre el código actual; el plan que lo rige es
[ENCARGO-2026-10-05.md](ENCARGO-2026-10-05.md).

---

## Documentos vigentes

| Documento | Contenido | Estado |
|---|---|---|
| [Encargo del 5-10-2026](ENCARGO-2026-10-05.md) | plan de evidencia, reglas de trabajo y los 16 criterios con sus umbrales | vigente |
| [Estado al 5-10-2026](estado-2026-10-05.md) | relevamiento del código: pictogramas propios, permisos, respaldo, pruebas, cifras | etapa 1, completo |
| [Bitácora de octubre](bitacora-2026-10.md) | los 16 criterios, uno por fila, con fecha, commit y evidencia de cada medición | completa: los 16 criterios medidos al 11-10; segundo intento del 11 y el 13 el 3-11 |
| [Cifras estáticas](cifras-estaticas.md) | versiones de las bibliotecas y modelo de pose, leídos del código el 20-09 | **solo vigentes** las secciones 1 (versiones) y 2 (modelo de pose). Las cifras de vocabulario, reglas y archivos las reemplaza el relevamiento del 5-10 |
| [Protocolo manual](protocolo-manual-2026-10.md) | criterios 11 a 16: pasos, qué tiene que pasar y qué captura guardar | ejecutado el 10-10 y el 11-10 |
| [Protocolo de los criterios 11 y 13, segundo intento](protocolo-c11-c13-2026-10.md) | evidencia reproducible sobre la versión 1.1, en modo edición: grabación con audio, salidas de `adb`, capturas por `adb`, PID y DataStore antes y después de cerrar | ejecutado el 3-11: el 11 no cumple (10 de 12), el 13 cumple (6 de 6) |
| [Resumen del segundo intento](resumen-c11-c13-v2.md) | criterios 11 y 13: qué cambió respecto del primer intento, qué queda respaldado y qué no, y las filas para la Tabla 15 | 3-11 |
| `herramientas/leer-datastore.ps1` | decodifica la copia del DataStore que guarda el protocolo del criterio 13; no muestra la huella de la contraseña | probado con un archivo de muestra, y usado con el DataStore del teléfono el 3-11 |
| `evidencia-2026-10/` | capturas y salidas que prueban los criterios 11 a 16 | completo: un `.md` por criterio con sus capturas. El segundo intento del 11 y el 13 lleva el prefijo `c11-v2-` y `c13-v2-`; la sesión cortada del 11 está en `c11-v2-intento-interrumpido/` |
| [Capturas](capturas.md) | las 32 capturas de ilustración del capítulo, con el bloque de pictogramas propios | actualizado el 6-10 |
| `capturas-2026-10/` | esas capturas | se crea al sacarlas |
| `salidas-2026-10/` | salida de cada prueba automatizada | se crea la primera vez que corren las pruebas |
| [Protocolo de latencia](protocolo-latencia-2026-10.md) | criterio 10: preparación, las 51 pulsaciones en orden y cómo copiar el resultado | ejecutado: 4 intentos, el 4 válido |
| `datos/latencias-2026-10*.csv` | una fila por pulsación: `n,frase,pictogramas,latencia_ms,regimen`. Un archivo por intento (`-intento-N`), y `latencias-2026-10.csv` con la combinación de los intentos 2 y 3 | intentos 1 a 3 del 6-10; se cita el 4, del 11-10 sobre `c106b6e` |

## Orden de las pruebas en Android Studio

Pruebas de la JVM, criterios 1 a 9. No usan el teléfono.

### Antes de correrlas

1. **Commiteá todo.** La bitácora pide el commit del código medido, y una
   medición hecha con cambios sin commitear no se puede reproducir.
2. Anotá el hash del último commit que tocó la app: `git log --oneline -1 -- app`.
3. **No borres `app/src/main/assets/animaciones.json`** ni corras
   `PoseCatalogDumpTest`. El catálogo instalado es el que se mide, y desde
   `842b0f9` tiene `HOT` corregido a mano.

### Cómo correr cada una

Abrí el archivo de la prueba y tocá la flecha verde al lado del nombre de la
clase → **Run**. Están en `app/src/test/java/com/tunombre/agustispeaks/`.

| Orden | Prueba | Carpeta | Criterio | Salida en `salidas-2026-10/` |
|---|---|---|---|---|
| 1 | `InventarioTesisTest` | `tesis/` | 1 y 2 | `criterio-01-02-inventario.txt` |
| 2 | `PoseCatalogParityTest` | `avatar/catalog/` | 3 | `criterio-03-equivalencia.txt` |
| 3 | `CoberturaRepresentacionTest` | `tesis/` | 4 | `criterio-04-representacion.txt` |
| 4 | `CorpusResolvedorTest` (sus dos pruebas) | `tesis/` | 5 y 6 | `criterio-05-corpus.txt`, `criterio-06-trampas.txt` |
| 5 | `CoberturaSemanticaTest` | `tesis/` | 7 y 8 | `criterio-07-08-cobertura.txt` |
| 6 | `OracionCompletaTest` | `tesis/` | 9 | `criterio-09-oracion.txt` |
| 7 | `PictogramasPropiosMetadatosTest` | `tesis/` | complementa 13 y 15 | `pictogramas-propios-metadatos.txt` |

El orden sigue la numeración de la bitácora. Ninguna depende de otra: todas leen
el catálogo instalado, que ya existe. Si preferís correrlas todas juntas: clic
derecho en la carpeta `app/src/test` → **Run 'Tests in …'**. Las salidas son
las mismas.

### Después de correrlas

1. Cada salida termina con una línea `>>> CRITERIO n: …`. Esa línea va a la
   columna "Resultado" de la [bitácora](bitacora-2026-10.md).
2. **Una prueba en rojo no se arregla tocando la prueba.** El fallo es el dato:
   se anota "no cumple" y la salida queda como evidencia.
3. Commiteá las salidas en un commit propio, separado del código, para que se
   distinga qué es medición y qué es cambio.

`PictogramCoverageTest` y `CatalogMergeTest` también están en la carpeta de
pruebas, pero no corresponden a ningún criterio. Pueden correr o no.

---

## Evidencia retirada el 5-10-2026

La evidencia de septiembre se midió sobre una versión de la app que ya no
existe: tenía exportación a GIF y no tenía pictogramas propios con foto, ni
favoritos, ni el panel de palabras reducido. **No se cita en la tesis.** Se
retiró del árbol de trabajo, pero no se borró del historial.

**Sigue disponible completa en el commit `66006df`** (5-10-2026, "Estado al
5-10-2026"), que es el último que la contiene. Para leer un archivo sin cambiar
de rama:

```
git show 66006df:docs/tesis/fases-2-y-3.md
```

| Retirado | Qué era | Por qué se retira |
|---|---|---|
| `salidas/` (6 archivos) | salida cruda de las pruebas del 21 al 24-09 | medida sobre el código anterior |
| `datos/latencias.csv` | 25 mediciones de latencia del 26-09 | medida con la app anterior, en otra sesión, y sin el protocolo de 50 pulsaciones del encargo |
| `datos/README.md` | descripción de esas 25 mediciones | describía un archivo que ya no está |
| `fases-2-y-3.md` | registro consolidado de las mediciones de septiembre | sus cifras corresponden al código anterior |
| `apendice-A-estructura.md` | estructura de paquetes | cifras de archivos y líneas de septiembre |
| `apendice-B-modelo-de-datos.md` | entidades y campos | anterior a los pictogifts y a los pictogramas propios |
| `apendice-C-resolvedor.md` | resolvedor de acciones | no se usa en el plan de octubre |
| `apendice-D-pose-e-interprete.md` | modelo de pose | no se usa en el plan de octubre |
| `apendice-E-catalogo.md` | inventario del catálogo | cifras medidas en septiembre |
| `apendice-F-corpus-y-cobertura.md` | corpus y cobertura semántica | cifras medidas en septiembre (170 pictogramas, 25 núcleo) |
| `apendice-G-bitacora.md` | bitácora de 13 criterios con resultados | criterios y resultados reemplazados por los 16 del encargo |

`salidas/` y `fases-2-y-3.md` también están en el commit `94cf5c6` (24-09). Los
apéndices y `datos/` se escribieron después y **solo** están en `66006df`.

## Historial reescrito el 31-10-2026

Antes de publicar el repositorio se reescribió el historial de git para quitar
cuatro notas de trabajo que no son evidencia y que la tesis no cita: un
traspaso de estado del 26-09 (`docs/tesis/`) y tres documentos de traspaso de
la raíz del proyecto, retirados del árbol el 5-10. Ya no están en ningún commit.

- **El código no cambió.** Se comparó cada uno de los commits con su original:
  fuera de `docs/` y de una línea del `.gitignore`, ninguna diferencia. Las
  mediciones citan el mismo código que se midió.
- **Los hashes sí cambiaron**, porque un hash depende de todo el contenido del
  commit. Los que citan estos documentos ya son los nuevos, incluido el nombre
  de `c14-sugerencias-6caa5e3.md`.
- Fechas, autor y orden de los commits se conservan. Hay un commit menos: uno
  que solo renombraba documentos quedó vacío y se quitó.

## Cambios de producto hechos al empezar octubre

Antes de medir, y en commits separados de la evidencia:

| Commit | Cambio | Criterio al que afecta |
|---|---|---|
| `842b0f9` | `HOT` deja de tener extensión de brazo negativa | 4 |
| `d4147ab` | la foto de un pictograma propio se borra en el momento de eliminarlo | 15 |
| `cbe46e9` | sin copia de seguridad: fotos y datos no entran ni en la nube ni en la transferencia a otro teléfono | 16 |

## Cambios de producto hechos durante la medición

Cada uno corrige algo que apareció al medir. Las mediciones anteriores siguen
citando el commit sobre el que se hicieron.

| Commit | Cambio | Qué lo motivó |
|---|---|---|
| `6caa5e3` | azul principal `#2E5AAC`, con contraste suficiente; pictogramas y categorías con nombre para el lector de pantalla | el criterio 14 no cumplía sobre `a0accaf` |
| `c106b6e` | las barras del sistema siguen el modo oscuro de la app: en modo claro, la hora y los íconos ya no quedan invisibles | error encontrado al medir el criterio 11 (no lo decide) |

## Pendiente, solo si hace falta

Decidido por el autor el 11-10: no se hace por ahora.

- Las 32 capturas de ilustración de [capturas.md](capturas.md).
- Dos hallazgos del criterio 14 que no lo deciden: los dos botones "Volver"
  con la misma descripción en la misma pantalla, y la pestaña "Recientes" con
  el mismo texto que el título.
