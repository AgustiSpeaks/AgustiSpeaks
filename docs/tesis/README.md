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
| [Bitácora de octubre](bitacora-2026-10.md) | los 16 criterios, uno por fila, con fecha, commit y evidencia de cada medición | abierta, sin mediciones |
| [Cifras estáticas](cifras-estaticas.md) | versiones de las bibliotecas y modelo de pose, leídos del código el 20-09 | **solo vigentes** las secciones 1 (versiones) y 2 (modelo de pose). Las cifras de vocabulario, reglas y archivos las reemplaza el relevamiento del 5-10 |
| [Capturas](capturas.md) | lista de capturas para el capítulo de validación | a actualizar en la etapa 5: todavía menciona el GIF |
| `salidas-2026-10/` | salida de cada prueba automatizada | se crea en la etapa 3 |
| `datos/` | CSV para las figuras | se crea en la etapa 4 |

## Orden de las pruebas en Android Studio

Pendiente: se escribe en la etapa 3, cuando las pruebas estén actualizadas.

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
| traspaso de estado del 26-09 | resumen del proyecto para retomarlo en otra conversación | describía un estado del proyecto que ya cambió |
| `apendice-A-estructura.md` | estructura de paquetes | cifras de archivos y líneas de septiembre |
| `apendice-B-modelo-de-datos.md` | entidades y campos | anterior a los pictogifts y a los pictogramas propios |
| `apendice-C-resolvedor.md` | resolvedor de acciones | no se usa en el plan de octubre |
| `apendice-D-pose-e-interprete.md` | modelo de pose | no se usa en el plan de octubre |
| `apendice-E-catalogo.md` | inventario del catálogo | cifras medidas en septiembre |
| `apendice-F-corpus-y-cobertura.md` | corpus y cobertura semántica | cifras medidas en septiembre (170 pictogramas, 25 núcleo) |
| `apendice-G-bitacora.md` | bitácora de 13 criterios con resultados | criterios y resultados reemplazados por los 16 del encargo |
| `/TESIS-PICTOGRAMAS-ANIMADOS.md` (raíz) | traspaso de contexto sobre la ampliación del vocabulario animado | describe el proyecto antes del 20-09 |
| `/TESIS-REFACTOR-CATALOGO.md` (raíz) | briefing para redactar la sección del refactor del catálogo | describe el proyecto antes del 20-09 |
| `/CONTEXTO-PROYECTO.md` (raíz) | documento de traspaso para otra sesión | describe el proyecto antes del 20-09; lo reemplaza [estado-2026-10-05.md](estado-2026-10-05.md) |

`salidas/`, `fases-2-y-3.md` y los tres documentos de la raíz también están en
el commit `94cf5c6` (24-09); los de la raíz, además, desde el commit inicial
`bddaae2`. Los apéndices, `datos/` y el traspaso del 26-09 se escribieron
después y **solo** están en `66006df`.

## Cambios de producto hechos al empezar octubre

Antes de medir, y en commits separados de la evidencia:

| Commit | Cambio | Criterio al que afecta |
|---|---|---|
| `842b0f9` | `HOT` deja de tener extensión de brazo negativa | 4 |
| `d4147ab` | la foto de un pictograma propio se borra en el momento de eliminarlo | 15 |
