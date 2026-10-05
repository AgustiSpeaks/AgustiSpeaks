# Material de tesis — AgustiSpeaks

Índice de los documentos de evidencia y los apéndices.

---

## Apéndices

| | Documento | Estado |
|---|---|---|
| A | [Estructura del proyecto](apendice-A-estructura.md) | completo |
| B | [Modelo de datos](apendice-B-modelo-de-datos.md) | completo |
| C | [Resolvedor de acción](apendice-C-resolvedor.md) | completo |
| D | [Modelo de pose e intérprete](apendice-D-pose-e-interprete.md) | completo |
| E | [Catálogo de acciones](apendice-E-catalogo.md) | completo, con salida medida |
| F | [Corpus y cobertura](apendice-F-corpus-y-cobertura.md) | completo, con salida medida |
| G | [Bitácora de verificación](apendice-G-bitacora.md) | 9 de 13 criterios verificados |
| ~~H~~ | ~~Codificador GIF89a~~ | **eliminado** |

**Sobre el Apéndice H.** El plan original incluía un apéndice sobre el
codificador GIF89a y su compresión LZW, escrito a mano para exportar la animación
como GIF. Esa función se quitó de la app: fallaba al abrir el selector de
aplicaciones y compartir un GIF no es algo que la app necesite para comunicar. El
código vive en el historial de git, pero ya no describe el producto que se
entrega, así que el apéndice se retira del plan.

## Evidencia

| Documento | Contenido |
|---|---|
| [Cifras estáticas](cifras-estaticas.md) | lo que se responde leyendo el código: versiones, modelo de pose, conteo de reglas |
| [Fases 2 y 3](fases-2-y-3.md) | registro consolidado de las dos fases de medición, con los hallazgos |
| [`salidas/`](salidas/) | salida cruda de cada prueba, regenerada en cada corrida |
| [`datos/`](datos/) | CSV listos para graficar |
| [Capturas](capturas.md) | las 32 capturas del capítulo 8, con qué revisar en cada una |

## Cómo regenerar todo

Las pruebas de la JVM corren con directorio de trabajo `app/`. El orden importa:
el volcado primero, porque genera el catálogo que las demás leen.

```
1. PoseCatalogDumpTest            regenera assets/animaciones.json
2. PoseCatalogParityTest          Apéndice G.1
3. InventarioTesisTest            Apéndice E
4. CoberturaRepresentacionTest    Apéndice G.2
5. CorpusResolvedorTest           Apéndices F.1 y F.1 bis
6. CoberturaSemanticaTest         Apéndice F.2
```

Para regenerar el catálogo hay que **borrar**
`app/src/main/assets/animaciones.json` primero: el volcado no pisa un archivo
existente, para no arruinar el trabajo de quien lo esté editando.

Cada prueba escribe su apéndice en `salidas/`, además de imprimirlo, y **lo
escribe también cuando falla**: una corrida que falla es justo la que hay que
poder leer entera.

## Estado de los criterios

| Verde | Rojo | Sin umbral | Pendientes |
|---|---|---|---|
| 6 | 1 | 2 | 4 |

El único rojo es la cobertura de representación, por el `reach` negativo de
`HOT`. Los cuatro pendientes son los manuales del Apéndice G: modo avión,
manifiesto combinado, persistencia y accesibilidad.

## Decisiones abiertas

| Decisión | Dónde se discute |
|---|---|
| `HOT`: ¿`w` es un error de copia o es deliberado? | fases-2-y-3, §2.5 |
| "Pipí" y "Caca" dan la misma animación | apéndice F.2 |
| "Quiero llorar" → `WANT` en vez de `CRY` | apéndice C.5 |
| Vestirse: ¿procedural o dos registros? | apéndice D.5 |
| ¿Prueba instrumentada de la capa Room? | sin decidir |
| ¿Los trece criterios son los correctos? | apéndice G |
