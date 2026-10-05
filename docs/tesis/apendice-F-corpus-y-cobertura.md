# Apéndice F — Corpus y cobertura

Tres mediciones sobre el resolvedor: si entiende lo que se le dice, si distingue
significados parecidos, y si todo el vocabulario tiene una animación que lo
represente.

| | Instrumento | Salida |
|---|---|---|
| F.1 | `CorpusResolvedorTest.resolucionDelCorpus` | [`salidas/F1-corpus.txt`](salidas/F1-corpus.txt) |
| F.1 bis | `CorpusResolvedorTest.colisionesLexicas` | [`salidas/F1bis-trampas.txt`](salidas/F1bis-trampas.txt) |
| F.2 | `CoberturaSemanticaTest` | [`salidas/F2-cobertura.txt`](salidas/F2-cobertura.txt) |

**Última corrida:** 2026-09-24

---

## F.1 — Corpus del resolvedor

**Criterio: resolución ≥ 90 %. Resultado: 97,3 %.**

| Medición | Valor |
|---|---|
| Oraciones | 110 |
| Resueltas según lo esperado | 107 |
| Desvíos | 3 |

El corpus cubre las quince categorías del vocabulario más las palabras núcleo, e
incluye obligatoriamente los casos límite históricos del proyecto.

### La regla que hace que esto sea una prueba y no un espejo

La acción esperada de cada oración está decidida **por el significado de la
oración**, no copiando lo que el resolvedor devuelve hoy.

Si estuviera copiada, el corpus pasaría siempre y no mediría nada: sería el
resolvedor comparándose consigo mismo. Es la misma trampa de muestreo que ya
había costado cara en el volcado del catálogo, donde verificar en los mismos
puntos que generaron los datos habría dado un verde vacío.

De eso se sigue algo incómodo y correcto: **un fallo del corpus no es
necesariamente un error de la prueba.** Puede ser el resolvedor el que está
equivocado. Los tres desvíos que quedan son de esa clase, y están analizados en
el Apéndice C.5.

### Las nueve decisiones de producto

Nueve de las 110 oraciones tienen una acción esperada que es una decisión de
producto y no una lectura del diccionario. Se imprimen **aparte, al final de la
salida**, para que queden a la vista de quien revise en lugar de escondidas entre
las filas.

Una de ellas es una corrección: *"La plata"* se esperaba en `PAY` y se cambió a
`SHOW`. No se cambió para que la prueba pasara, sino porque la expectativa
original contradecía un principio que el propio corpus ya aplicaba en otras dos
filas. El motivo quedó escrito en la nota del caso, de modo que la corrección se
ve en el apéndice en lugar de desaparecer.

## F.1 bis — Pares trampa

**Criterio: 0 colisiones léxicas. Resultado: 0 de 10 pares.**

Cada par tiene que resolverse a **dos acciones distintas**. Que colapsen en una
es una colisión: dos significados que producen la misma animación, y por lo tanto
son indistinguibles para quien mira.

Los pares existen porque la normalización acerca peligrosamente palabras que no
tienen nada que ver: quitar las tildes vuelve iguales a *"cómo"* y *"como"*, y
varias reglas buscan subcadenas, así que *"te"* vive dentro de *"triste"* y
*"bebé"* dentro de *"beber"*.

Lo único que los separa es el orden de evaluación del resolvedor. Esta prueba es
la que convierte ese orden en una **propiedad de corrección verificada** y no en
un detalle de implementación que alguien podría reordenar sin darse cuenta.

## F.2 — Cobertura semántica y distinguibilidad

**Cobertura: 100 %. Distinguibilidad: 18 pares idénticos.**

Recorre los 170 pictogramas, resuelve cada uno como lo hace la app, e informa por
categoría.

| Medición | Valor |
|---|---|
| Pictogramas recorridos | 170 |
| Con animación propia | 170 |
| En la acción genérica `TALK` | **0** |
| Línea base (antes de esta etapa) | 59 de 170 en `TALK` |
| Pares indistinguibles | 18 |

### Por qué mide dos cosas y no una

**Cobertura** es cuántos pictogramas tienen animación propia en vez de caer en
`TALK`, que es mover la boca sin decir nada. **Distinguibilidad** es si dos
pictogramas distintos de una misma categoría producen exactamente la misma
animación.

Las dos pueden ir en direcciones opuestas. **Una categoría puede tener cobertura
perfecta y ser inútil igual**, si todos sus pictogramas se ven iguales. Ya había
pasado en este proyecto: la categoría juego resolvía entera a `PLAY`, con
cobertura del 100 %, y el tobogán, la hamaca y los juguetes se veían idénticos.

Cobertura del 100 % con 18 pares idénticos es exactamente esa tensión. Una sola
métrica habría contado una película más linda.

### Los 18 pares

**Once son sinónimos aceptables:** Manzana/Fruta, Feliz/Contento, Campera/Abrigo,
Dibujar/Pintar, los tres saludos entre sí, los tres rechazos entre sí, Estoy
mareado/Tengo náuseas.

**Siete son significados distintos colapsados:**

| Par | Animación |
|---|---|
| Pipí = Caca | `toilet` |
| Boca = Dientes | `touch_part +tooth` |
| Hoy = Ahora | `now` |
| Mañana = Después | `later` |
| Tú / Esto / Aquí | `point` (3 pares) |

**"Pipí = Caca" es el que más importa**: para quien cuida al chico esas dos cosas
no son lo mismo. Queda como limitación declarada.

### Sin umbral, a propósito

Esta prueba mide y no aprueba. El umbral se fija mirando el dominio, no la
salida, y fijarlo después de conocer el resultado sería elegir el número que la
prueba ya aprueba. Lo único que se verifica es que la medición pudo hacerse sobre
los 170.

### Nota metodológica

La prueba **copia** la lógica de precedencia de `AvatarCatalog.resolve` en vez de
llamarla, porque el original carga el catálogo desde un `Context` de Android que
no existe en una prueba de la JVM. Es una duplicación que puede desincronizarse,
y conviene que figure como tal y no como si midiera el objeto real.

## Cómo regenerar

```
gradlew :app:testDebugUnitTest --tests "*CorpusResolvedorTest*"
gradlew :app:testDebugUnitTest --tests "*CoberturaSemanticaTest*"
```
