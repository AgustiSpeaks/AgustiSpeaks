# Apéndice E — Catálogo de acciones

Inventario completo del motor de animación: cuántas acciones hay, cuáles son
datos y cuáles código, cuántos fotogramas ocupa cada una.

**Generado por:** `InventarioTesisTest`
**Salida cruda:** [`salidas/E-inventario.txt`](salidas/E-inventario.txt)
**Última corrida:** 2026-09-24

---

## Cómo regenerar

```
gradlew :app:testDebugUnitTest --tests "*InventarioTesisTest*"
```

Requiere que `app/src/main/assets/animaciones.json` exista. Si falta, correr
primero `PoseCatalogDumpTest`. Que la prueba falle por catálogo ausente es
correcto: sin catálogo no hay nada que inventariar.

La salida se escribe sola en `docs/tesis/salidas/E-inventario.txt`, con columnas
alineadas y orden determinista, lista para pegar en el informe.

## Lo que la prueba sostiene, además de contar

Tres aserciones. Si alguna falla, ninguna cifra del apéndice significa lo que dice.

1. **Correspondencia exacta** entre el enum de acciones y los registros del
   catálogo: ni sobrantes ni faltantes.
2. **Coincidencia** entre el catálogo y la tabla de generadores sobre qué es
   procedural.
3. **Cero problemas de lectura** del parser sobre el archivo instalado.

Y dos guardias sobre el vocabulario, agregados después de que el propio
inventario encontrara diez pictogramas inalcanzables:

4. Ninguna categoría sin exponer en algún lugar.
5. Ninguna categoría sin pictogramas.

## Resumen de la última corrida

| Medición | Valor |
|---|---|
| Acciones en el motor | 88 |
| Registros en el catálogo | 88 |
| — declarativas | 76 |
| — procedurales | 12 |
| Fotogramas declarados | 419 |
| — mínimo por acción | 2 |
| — promedio por acción | 5,51 |
| — máximo por acción | 38 (`jump`) |
| Tamaño del catálogo | 40.048 bytes |
| Bytes por fotograma | 95,6 |
| Objetos (props) | 85 |
| Pictogramas | 170 |
| Reglas de disparo en código | 121 |
| Reglas migradas al catálogo | 4 (3,3 %) |

## Secciones de la salida

La salida cruda trae seis secciones. Para el informe conviene tomar la 6 entera
—la tabla de las 88 acciones con su bucle, tipo y cantidad de fotogramas— y
resumir las demás.

| Sección | Contenido |
|---|---|
| 1 | Catálogo de animaciones: totales, mínimo, promedio, máximo, tamaño |
| 2 | Las 12 acciones procedurales, con el motivo de cada una |
| 3 | Los 85 objetos, en orden alfabético |
| 4 | Vocabulario: 170 pictogramas por categoría y por lugar |
| 5 | Reglas de disparo, en catálogo y en código |
| 6 | **Tabla completa de las 88 acciones** |

## Dos lecturas que no están en los números

**Un tercio del catálogo es casi estático.** 24 de las 76 acciones declarativas
usan el mínimo de 2 fotogramas, que en un ciclo cerrado es una sola oscilación
entre dos valores: dar, tomar, gracias, perdón, sí, abrir, cerrar, triste,
llorar. No es una carencia. En CAA buena parte del significado la carga la
postura sostenida, y el motor lo refleja sin que nadie lo haya planificado así.

**Los motivos de las procedurales los mantiene honestos la prueba.** El texto de
por qué cada una no se puede declarar está transcrito a mano, porque es prosa y
no hay de dónde leerlo. Lo que la prueba verifica es la cobertura: si alguien
agrega una acción procedural y no escribe su motivo, la prueba falla.
