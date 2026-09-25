# Fases 2 y 3 — Inventario y validación

Registro consolidado de las dos fases de medición. Todas las cifras de este
documento salen de pruebas ejecutadas en Android Studio; ninguna está estimada.

Fecha de la corrida final: 2026-09-24.
Commits: `4eb02ce` (fase 2), `e719235`, `793ff86`, `5bb0c41` (arreglos de producto).

---

## 0. Estado de los criterios

Cifras de la corrida del 2026-09-24, la primera con los tres arreglos aplicados.

| Criterio | Instrumento | Resultado | Estado |
|---|---|---|---|
| Inventario del sistema | `InventarioTesisTest` | 88 acciones, 419 fotogramas | **verde** |
| Correspondencia motor ↔ catálogo | `InventarioTesisTest` | exacta, sin sobrantes | **verde** |
| Vocabulario alcanzable | `InventarioTesisTest` | 0 categorías huérfanas | **verde** |
| Resolución del corpus ≥ 90 % | `CorpusResolvedorTest` | **97,3 %** | **verde** |
| Colisión léxica = 0 | `CorpusResolvedorTest` | **0 de 10 pares** | **verde** |
| Equivalencia del catálogo | `PoseCatalogParityTest` | **0 desvíos** en 97 casos | **verde** |
| Cobertura semántica | `CoberturaSemanticaTest` | **100 %** | **medido, sin umbral** |
| Distinguibilidad | `CoberturaSemanticaTest` | 18 pares idénticos | **medido, sin umbral** |
| Cobertura de representación | `CoberturaRepresentacionTest` | 328 fuera de rango, todos `HOT` | **rojo, documentado** |

Ocho de nueve en verde. El único rojo es `HOT`, un defecto conocido que se dejó
sin arreglar a propósito porque su lectura correcta no está decidida (ver 2.5).

### Qué cambió en la reverificación

| Criterio | Antes | Después | Por qué |
|---|---|---|---|
| Equivalencia | 9 desvíos | **0** | vestirse pasó a procedural |
| Casos con error exactamente cero | 14 | **18** | idem: vestirse y sus 3 variantes |
| Representación | 627 fuera de rango | **328** | el salto dejó de pasarse de 1 |
| Máximo de `bob` | 1,4300 | **1,0000** | idem, exacto |
| Pares indistinguibles | 21 | **18** | libro, carrito y plata |
| Acciones distintas en Compras | 3 | **5** | idem |
| Reglas del resolvedor | 118 | **121** | partir verbo y sustantivo suma reglas |

---

## 1. Fase 2 — Inventario (Apéndice E)

`InventarioTesisTest` no prueba comportamiento: cuenta. Cada número que el informe
afirma sobre el tamaño del sistema sale de ahí y no de un conteo a mano.

### 1.1 El catálogo de animaciones

| Medición | Valor |
|---|---|
| Acciones en el motor | 88 |
| Registros en el catálogo | 88 |
| — declarativas (fotogramas) | 76 |
| — procedurales (generador Kotlin) | 12 |
| Fotogramas declarados, total | 419 |
| — mínimo por acción declarativa | 2 |
| — promedio por acción declarativa | 5,51 |
| — máximo por acción declarativa | 38 (`jump`) |
| Tamaño del catálogo | 40.048 bytes |
| Bytes por fotograma | 95,6 |

**Dato de diseño:** 24 de las 76 acciones declarativas usan el mínimo de 2
fotogramas, casi un tercio del catálogo. Dos fotogramas en un ciclo cerrado es una
sola oscilación entre dos valores. Son conceptos que se comunican con una postura
sostenida y no con movimiento: dar, tomar, gracias, perdón, sí, abrir, cerrar,
triste, llorar. En CAA buena parte del significado la carga la pose, y el motor lo
refleja sin que nadie lo haya planificado así.

### 1.2 Vocabulario

| Grupo | Cantidad |
|---|---|
| Lugares | 8 |
| Categorías temáticas | 15 |
| Palabras núcleo | 25 |
| Pictogramas temáticos | 145 |
| **Total de pictogramas distintos** | **170** |
| Objetos (props) | 85 |

### 1.3 Reglas de disparo

| Ubicación | Reglas |
|---|---|
| Catálogo (`animaciones.json`) | 4 |
| Código (`ActionResolver`) | 121 |
| Migradas al catálogo | **3,3 %** |

Más tres tablas auxiliares que eligen el matiz y no la acción: alimento (15),
prenda (7), parte del cuerpo (17). En total, **160 decisiones léxicas**.

Las reglas subieron de 118 a 121 al separar cada sustantivo de su verbo. Vale
anotarlo: **arreglar la distinguibilidad cuesta reglas.** Partir "pagar" de
"plata" no es refinar una regla existente sino escribir una segunda, y eso empuja
en la dirección contraria a la migración al catálogo.

La documentación previa decía "aproximadamente sesenta". El número real es más del
doble. La afirmación defendible es que la migración de disparadores está
**demostrada pero no completada**: el mecanismo funciona sobre 4 de 118.

### 1.4 Método de conteo que conviene citar

Las reglas del resolvedor **se cuentan leyendo el archivo fuente como texto**: la
prueba abre `ActionResolver.kt` y cuenta los `return R(...)`, descontando el
respaldo final. Parece raro en un test, y es deliberado: un conteo a mano envejece
en silencio cuando alguien agrega una regla, y este número va al informe.

Los motivos de las acciones procedurales sí están transcritos a mano, porque son
prosa y no hay de dónde leerlos. Lo que la prueba verifica es la **cobertura**: si
alguien agrega una procedural y no escribe su motivo, la prueba falla. Una tabla de
documentación que el test mantiene honesta vale más que una que nadie revisa.

### 1.5 Verificación cruzada con la Fase 1

Las **25 cifras** que la Fase 1 había contado a mano leyendo el código coinciden
todas con el conteo de la máquina: las 5 del vocabulario, las 15 por categoría, las
2 de reglas y las 3 de tablas auxiliares. Ninguna discrepancia.

Eso habilita a afirmar en la tesis que las cifras están medidas por dos métodos
independientes, que era el objetivo de haberlas contado dos veces.

### 1.6 Hallazgo: diez pictogramas inalcanzables

El inventario contó, por categoría, en cuántos lugares aparece. La categoría
**"Lugares" daba 0**.

Estaba definida, tenía 10 pictogramas asignados —casa, baño, cocina, escuela,
plaza, hospital, tienda, auto, afuera, adentro— y **ningún lugar la listaba en sus
categorías**. Existían en la semilla y no había forma de tocarlos en el tablero.
Con ellos quedaba invisible la acción `GO_TO` y los siete edificios dibujados para
representarlos.

El vocabulario real era **170 declarados, 160 alcanzables**.

Arreglado en `e719235`: la categoría se expone en los ocho lugares. La regla que se
aplicó, y que quedó escrita en el código: el vocabulario de destinos se necesita
justamente desde donde uno está, así que restringir desde dónde se puede pedir
"quiero ir a casa" no ahorra nada.

**Y se agregó un guardia**: la prueba ahora falla si alguna categoría no está
expuesta en ningún lugar, o si alguna queda sin pictogramas. El bug no era difícil
de arreglar; era difícil de *ver*. El comentario que quedó al lado resume el
principio: **declarar una palabra y poder decirla no son lo mismo.**

---

## 2. Fase 3 — Validación

### 2.1 Apéndice F.1 — Corpus del resolvedor

| Medición | Valor |
|---|---|
| Oraciones en el corpus | 110 |
| Resueltas según lo esperado | 107 |
| Desvíos | 3 |
| **Resolución** | **97,3 %** |
| Mínimo exigido | 90,0 % |

**La regla que hace que esto sea una prueba y no un espejo:** la acción esperada de
cada oración está decidida por el significado de la oración, **no** copiando lo que
el resolvedor devuelve. Si estuviera copiada, el corpus pasaría siempre y no
mediría nada: sería el resolvedor comparándose consigo mismo, que es la misma
trampa de muestreo que ya había costado cara en el volcado del catálogo.

De eso se sigue que un fallo no es necesariamente un error de la prueba. Puede ser
el resolvedor el que está equivocado.

#### Los tres desvíos

| Oración | Esperada | Obtenida | Diagnóstico |
|---|---|---|---|
| "No quiero comer" | `NO` | `EAT` | el verbo *comer* (sección 4) se evalúa antes que la negación (sección 7) |
| "Buscar mi juguete" | `SEARCH` | `TOYS` | el sustantivo *juguete* (sección 3.8) se evalúa antes que el verbo *buscar* (sección 4) |
| "Quiero llorar" | `CRY` | `WANT` | el genérico *quiero* (sección 7) se evalúa antes que las emociones (sección 8) |

Los tres son el mismo tipo de problema: **orden de evaluación**, no vocabulario
faltante. Y los tres apuntan en direcciones distintas, lo que muestra que no hay un
orden único que los resuelva todos. Es el costo de un resolvedor por reglas
ordenadas, y es el argumento más fuerte a favor de que el orden sea explícito y
verificado en vez de emergente.

El tercero no estaba previsto: lo encontró la prueba. Cualquier frase de la forma
"quiero + emoción" cae en `WANT`.

#### Las nueve decisiones de producto del corpus

Se imprimen aparte, al final del apéndice, para que queden a la vista de quien
revise en lugar de escondidas entre las 110 filas.

| Oración | Decisión | Razón |
|---|---|---|
| "Yo quiero agua" | `DRINK` | la app arma la oración entera: manda el verbo con su objeto, no el pronombre |
| "No quiero comer" | `NO` | el mensaje es el rechazo; mostrar a alguien comiendo dice lo contrario |
| "Buscar mi juguete" | `SEARCH` | el verbo es el mensaje; el juguete es el objeto de la búsqueda |
| "Una curita" | `TOUCH_PART` | nombrarla es señalar dónde duele |
| "Jabón" | `SHOW` | nombrar un objeto es mostrarlo; usarlo es lavarse las manos |
| "Quiero ir al baño" | `TOILET` | el mensaje es la necesidad, no el traslado |
| "¿Cómo estás? Bien" | `ASK` | la pregunta manda sobre la respuesta |
| "El libro" | `SHOW` | mismo criterio que lápiz y cuaderno |
| "La plata" | `SHOW` | **corregida**: la esperaba en `PAY`, lo cual contradecía el criterio aplicado a lápiz y cuaderno |

La última merece una nota metodológica. No se cambió para que la prueba pasara: se
cambió porque **la expectativa original era inconsistente con un principio que el
propio corpus ya aplicaba en otras dos filas**. La corrección quedó escrita en la
nota del caso para que sea auditable en el apéndice, en lugar de desaparecer.

### 2.2 Apéndice F.1 (bis) — Pares trampa

**Criterio: 0 colisiones léxicas. Resultado: 0 de 10 pares.**

Cada par tiene que resolverse a dos acciones distintas. Que colapsen en una es una
colisión: dos significados con la misma animación. Existen porque quitar las tildes
vuelve iguales a "cómo" y "como", y porque varias reglas buscan subcadenas.

| Par A | → | Par B | → | Por qué es trampa |
|---|---|---|---|---|
| Estoy triste | `SAD` | Quiero té | `DRINK` | "te" es subcadena de "triste" |
| ¿Cómo estás? | `ASK` | Quiero comer | `EAT` | sin tildes son la misma palabra |
| Ya está | `DONE` | Ahora | `NOW` | "ya" como disparador robaría "ya está" |
| Buenas tardes | `HELLO` | Más tarde | `LATER` | "tarde" es saludo y es momento del día |
| Buenos días | `HELLO` | Es de día | `DAY` | el saludo lleva la s |
| El bebé | `CRADLE` | Quiero beber | `DRINK` | "bebe" vive dentro de "beber" |
| Pararme | `STAND` | Parar | `STOP` | comparten la raíz |
| No toques | `NO` | Quiero tocar | `TAP` | la negación tiene que ganarle al verbo |
| Yo | `TOUCH_PART` | Yo quiero agua | `DRINK` | el pronombre solo significa algo solo |
| Mi cabeza | `TOUCH_PART` | Mi mochila | `DRESS` | "mi" no decide por sí mismo |

Los diez separan. Esto es lo que convierte el orden de evaluación del resolvedor en
una **propiedad de corrección verificada** y no en un detalle de implementación.

### 2.3 Apéndice F.2 — Cobertura semántica y distinguibilidad

**Cobertura: 100 %. 0 de 170 pictogramas caen en la acción genérica `TALK`.**
La línea base era 59 de 170.

| Categoría | Pictogramas | Genérica | Propia | Acciones distintas | Pares idénticos |
|---|---|---|---|---|---|
| núcleo | 25 | 0 | 25 | 21 | 6 |
| Alimentos | 14 | 0 | 14 | 1 | 1 |
| Bebidas | 5 | 0 | 5 | 1 | 0 |
| Acciones | 12 | 0 | 12 | 12 | 0 |
| Sentimientos | 9 | 0 | 9 | 8 | 1 |
| Dolor | 12 | 0 | 12 | 8 | 1 |
| Partes del cuerpo | 12 | 0 | 12 | 1 | 1 |
| Ropa | 8 | 0 | 8 | 1 | 1 |
| Higiene | 8 | 0 | 8 | 6 | 1 |
| Tiempo | 9 | 0 | 9 | 7 | 2 |
| Familia | 9 | 0 | 9 | 2 | 0 |
| Juego | 10 | 0 | 10 | 9 | 1 |
| Lugares | 10 | 0 | 10 | 3 | 0 |
| Escuela | 11 | 0 | 11 | 7 | 0 |
| Compras | 6 | 0 | 6 | 5 | 0 |
| Social | 10 | 0 | 10 | 8 | 3 |
| **Total** | **170** | **0** | **170** | | **18** |

#### Por qué mide dos cosas y no una

**Cobertura** es cuántos pictogramas tienen animación propia. **Distinguibilidad**
es si dos pictogramas distintos de una misma categoría producen exactamente la
misma animación. Las dos pueden ir en direcciones opuestas: **una categoría puede
tener cobertura perfecta y ser inútil igual**, si todos sus pictogramas se ven
iguales. Ya había pasado: "juego" resolvía entero a `PLAY`, con cobertura del 100 %,
y el tobogán, la hamaca y los juguetes se veían idénticos.

Cobertura del 100 % con 18 pares idénticos es exactamente la tensión que la prueba
existe para mostrar. Una sola métrica habría contado una película más linda.

#### Los 18 pares, clasificados

**Sinónimos aceptables (11):** Manzana/Fruta, Feliz/Contento, Campera/Abrigo,
Dibujar/Pintar, los tres saludos entre sí (3 pares), los tres rechazos entre sí
(3 pares), Estoy mareado/Tengo náuseas.

**Significados distintos colapsados (7):**

| Par | Animación | Tipo |
|---|---|---|
| Pipí = Caca | `toilet` | dos necesidades distintas |
| Boca = Dientes | `touch_part +tooth` | dos partes del cuerpo |
| Hoy = Ahora | `now` | matiz temporal |
| Mañana = Después | `later` | matiz temporal |
| Tú / Esto / Aquí | `point` | persona, objeto, lugar (3 pares) |

#### Los tres que se arreglaron, y lo que costaron

La medición anterior tenía tres pares más, y **formaban un patrón**: Libro = Leer,
Carrito = Comprar, Plata = Pagar. Los tres eran sustantivo contra verbo, y los tres
eran una inconsistencia propia — el mismo principio ya se había aplicado a lápiz,
cuaderno y escribir, y no se había extendido al resto.

| Palabra | Ahora resuelve a |
|---|---|
| "libro" | `SHOW` + libro cerrado (`BOOK_HELD`) |
| "plata" | `SHOW` + billetes |
| "carrito" | `POINT` + carrito quieto (`CART_STILL`) |

"Carrito" no usa `SHOW` por una razón que salió del renderizador y no del diseño:
**la manija del carrito se dibuja como una línea que llega hasta la mano**. Con la
mano en alto, el carrito habría quedado colgando de una barra diagonal. Un carrito
no es un objeto de mano, y nombrar algo grande es señalarlo. `CART_STILL` comparte
la caja, la rejilla y las ruedas con `CART`, y cambia solo la manija.

Efecto medible del arreglo: Compras pasó de 3 a **5 animaciones distintas** sobre
6 pictogramas, y Escuela y Compras quedaron en 0 pares idénticos.

Costo medible: las reglas del resolvedor subieron de 118 a 121. **Arreglar la
distinguibilidad cuesta reglas**, porque partir un sustantivo de su verbo es
escribir una regla nueva, no refinar una existente.

**"Pipí = Caca" sigue pendiente**, y es el que más importa de los siete que quedan:
para quien cuida al chico esas dos cosas no son lo mismo.

#### Nota metodológica

La prueba **copia** la lógica de precedencia de `AvatarCatalog.resolve` en vez de
llamarla, porque el original carga el catálogo desde un `Context` de Android que no
existe en una prueba de la JVM. Es una duplicación que puede desincronizarse, y en
la tesis conviene que figure como tal y no como si midiera el objeto real.

### 2.4 Apéndice G.1 — Equivalencia del catálogo contra el motor

| Medición | Valor |
|---|---|
| Acciones del motor | 88 |
| Casos comparados | 97 |
| Fases por caso | 501 |
| Comparaciones de pose | 48.597 |
| Casos con error exactamente cero | 18 |
| Casos con algún desvío medible | 79 |
| **Desvíos fuera de tolerancia** | **0** |

Tolerancia: 1 % del recorrido del campo en esa acción, con piso 0,002. Los campos
discretos no toleran nada: una expresión o un objeto en la mano o son el mismo o no.

#### La independencia de las grillas, demostrada

El volcado muestrea en `t = k/4096`. La verificación usa 501 fases corridas 0,37 de
paso. Que no toque ningún punto de generación **no es una esperanza**:

> `(k + 0.37)/501 = j/4096` exigiría `4096k + 1515,52 = 501j`, y un entero no tiene
> parte decimal.

Está garantizado por aritmética, no por elegir números que parecen distintos. Es el
punto que hace que la verificación valga algo: una verificación cuyo muestreo no es
independiente del que produjo los datos no verifica nada.

#### Los 9 desvíos que encontró, y que ya no están

La corrida del 21 de septiembre dio **9 desvíos, los nueve de vestirse**. Se
conservan acá porque el diagnóstico es el hallazgo, no el número.

| Caso | Campo | Error | Tolera |
|---|---|---|---|
| dress +foot +shoes | `headTilt` | 20,00 | 0,002 |
| dress +leg +pants | `headTilt` | 20,00 | 0,002 |
| dress +foot +shoes | `crouch` | 0,50 | 0,0022 |
| dress +leg +pants | `crouch` | 0,50 | 0,0022 |
| dress +foot +shoes | `lean` | 0,12 | 0,002 |
| dress +leg +pants | `lean` | 0,12 | 0,002 |
| dress +head +hat | `highlight` | motor `null`, catálogo `HEAD` | 0 |
| dress +foot +shoes | `highlight` | motor `null`, catálogo `FOOT` | 0 |
| dress +leg +pants | `highlight` | motor `null`, catálogo `LEG` | 0 |

Los números cuadran exactamente con el código. `DRESS` se bifurca según dónde va la
prenda:

```kotlin
val abajo = destino == BodyPart.FOOT || destino == BodyPart.LEG
crouch   = if (abajo) 0.28f + 0.22f * up else 0f
headTilt = if (abajo) 15f else -5f
lean     = if (abajo) 0.12f * up else 0f
```

`headTilt`: 15 contra −5 da 20. `crouch`: de 0,28 a 0,50 contra 0 da 0,50.
`lean`: de 0 a 0,12 contra 0 da 0,12.

El `recorrido 0,00000` del `headTilt` es la firma del problema: **dentro de cada
rama el valor es constante**, así que no hay curva que refinar. El volcado capturó
la rama de arriba, porque muestrea el caso sin parte del cuerpo, y la de abajo quedó
sin representar.

#### Son dos defectos

**Uno, una pose con dos formas.** Un registro tiene una sola pista por campo. No
puede llevar las dos ramas a la vez.

**Dos, de modelo.** `usaParteDelCuerpo` es un booleano haciendo dos trabajos: "la
parte viaja al destino de la mano" y "la parte se resalta con un halo". Dolor y
tocar quieren las dos cosas; vestirse quiere solo la primera. De ahí los tres
desvíos de `highlight`: el catálogo le dibujaba un halo al pie cuando te ponés los
zapatos.

#### El arreglo, y el hallazgo que deja

Vestirse pasa a **procedural**, siguiendo el precedente de comer y remedio: un `if`
sobre un valor discreto es lo contrario de interpolar.

> Vestirse es **una animación simple que igual no se puede declarar.** No la saca
> del catálogo la cantidad de movimiento sino la naturaleza de su parámetro: la
> parte del cuerpo es categórica, no continua, y el intérprete solo sabe interpolar.
> **La frontera entre datos y código no la marca la complejidad; la marca si lo que
> hace variar a la pose es un número o una categoría.**

Eso es más preciso que "las complicadas van en Kotlin", que era como se venía
contando.

No se partió `usaParteDelCuerpo` en dos flags: con vestirse procedural, ese camino
del intérprete no se ejecuta para ella y el flag nuevo no tendría consumidor.
Agregar una abstracción que nadie usa es peor que documentar el límite. Queda
anotado como limitación conocida del modelo.

**Alternativa no tomada:** partir vestirse en dos registros —arriba y abajo— y que
el resolvedor elija cuál vía `ResolvedAction.catalogId`. La devolvería al catálogo
editable, al costo de que una acción tenga dos ids.

### 2.5 Apéndice G.2 — Cobertura de representación

| Medición | Valor |
|---|---|
| Acciones evaluadas | 88 |
| Fases por acción | 1.030 (1.024 dentro del ciclo + 6 fuera) |
| Poses evaluadas | 90.640 |
| Campos revisados por pose | 12 |
| **Comprobaciones** | **1.087.680** |
| Excepciones | 0 |
| Valores no finitos | 0 |
| `loopMillis` no positivos | 0 |
| **Valores fuera de rango** | **328** |

Los 328 son **una sola acción y un solo campo**: el `reach` negativo de `HOT`. Los
299 restantes de la medición anterior eran el salto, y desaparecieron.

Los rangos contra los que verifica salen de los comentarios de `AvatarPose`: `jaw`
entre 0 y 1, `bob` entre −1 y 1, y así. Es un contrato que el propio tipo declara,
no un criterio inventado para la ocasión.

**Por qué evalúa también fuera de `[0, 1]`:** el ciclo de dibujo calcula la fase a
partir del reloj y nada garantiza que caiga en el intervalo. `poseAt` lo contempla,
y una prueba que solo mirara el intervalo bonito dejaría sin verificar justo el caso
que ocurre en un dispositivo real cuando una animación arranca a mitad de bucle.

#### Recorrido real de cada campo

| Campo | Declarado | Mínimo | Máximo |
|---|---|---|---|
| `bob` | [−1, 1] | −0,2200 | **1,0000** |
| `lean` | [−1, 1] | −0,4200 | 0,4200 |
| `headTilt` | sin tope | −18,0000 | 18,0000 |
| `jaw` | [0, 1] | 0,0000 | 1,0000 |
| `tears` | [0, 1] | 0,0000 | 1,0000 |
| `eyeClose` | [0, 1] | 0,0000 | 1,0000 |
| `crouch` | [0, 1] | 0,0000 | 1,0000 |
| `legSwing` | [−1, 1] | −1,0000 | 1,0000 |
| `armSwing` | [−1, 1] | −1,0000 | 1,0000 |
| `reach` | [0, 1] | **−0,3000** | 1,0000 |
| `propPhase` | [0, 1] | 0,0000 | 1,0000 |
| `fxPulse` | [0, 1] | 0,8200 | 1,0000 |

#### `JUMP`, `bob` hasta 1,43 — arreglado y verificado

La fórmula era `(up − 0.35) * 2.2`, que en `up = 1` da 1,43. Y no era inocuo:

```
línea 43:  val dy = -pose.bob * H * 0.04f          ← sin límite
línea 47:  val lift = pose.bob.coerceIn(0f, 1f)    ← con límite
```

**Los dos consumidores de `bob` no se ponían de acuerdo sobre su rango.** La figura
subía según el valor crudo; la sombra lo recortaba en 1. En lo alto del salto el
avatar seguía subiendo y la sombra dejaba de achicarse. El `coerceIn` puesto en un
lugar y no en el otro es la huella típica de un rango que nadie sostiene.

Arreglo: `(up − 0.35) / 0.65`, que normaliza el tramo de ascenso a exactamente
0..1 y de paso dice qué significa el número — **la fracción del ascenso ya
recorrida**. Un `2.2` no dice nada.

Verificado: el máximo de `bob` sobre las 88 acciones quedó en **1,0000 exacto**, y
los 299 valores fuera de rango del salto desaparecieron. El arreglo tampoco rompió
la equivalencia: `jump` quedó en 37,89 % del margen de tolerancia.

Efecto visual declarado: el punto más alto del salto baja un 30 %, de 5,7 % de la
altura del lienzo a 4 %. **No verificado en pantalla.**

#### `HOT`, `reach` hasta −0,30 — pendiente

`reach = 0.35 + 0.65·w`, con `w = sin(2πt)` entre −1 y 1, da `[−0,30, 1,00]`. Un
tercio del ciclo con extensión de brazo negativa, que como concepto no existe.

Dato que no alcanza para decidir: `DRINK` usa `0.35 + 0.65·up`, **las mismas dos
constantes con la otra señal**, y `up` va de 0 a 1. Puede ser que `HOT` haya salido
de copiar `DRINK` cambiando la señal sin recalcular. Pero también puede ser
deliberado: abanicarse ES un vaivén, y `w` da el vaivén que `up` no da.

Arreglo propuesto y no aplicado: `0.5 + 0.5·w`, que conserva el vaivén completo y
queda en `[0, 1]`.

---

## 3. Hallazgos transferibles

Los que valen para la tesis más allá de esta app.

**1. La frontera datos/código la marca el tipo del parámetro, no la complejidad.**
Vestirse es una animación simple e indeclarable, porque lo que la hace variar es una
categoría y el intérprete solo interpola. Comer y remedio son el mismo caso con otro
disfraz.

**2. Una verificación cuyo muestreo no es independiente del que produjo los datos no
verifica nada.** Acá está demostrado por aritmética, no asumido.

**3. Una prueba cuyas expectativas se copian del sistema es un espejo.** El corpus
define la acción esperada por el significado de la oración; de ahí que un fallo
pueda ser del resolvedor y no de la prueba. Las decisiones opinables se listan
aparte para que sean auditables.

**4. Dos métricas que se oponen dicen más que una que se ve bien.** Cobertura del
100 % y 21 pares idénticos describen el sistema; cualquiera de las dos sola lo
falsea.

**5. Vocabulario declarado no es vocabulario alcanzable.** Diez pictogramas
existieron sin ser tocables hasta que un conteo por categoría los encontró.

**6. Un booleano que hace dos trabajos se rompe en el tercer caso de uso.**
`usaParteDelCuerpo` servía para dolor y tocar, y falló en vestirse.

**7. Un rango documentado que nadie verifica no es un contrato.** Dos consumidores
de `bob` asumían cosas distintas, y el `coerceIn` defensivo en uno solo de los dos
era la evidencia.

---

## 4. Cambios de producto derivados de la medición

Todos en commits separados de la evidencia, para que en el historial se distinga qué
es medición y qué es cambio de comportamiento.

| Cambio | Origen | Commit |
|---|---|---|
| Categoría "lugares" expuesta en los 8 lugares | inventario | `e719235` |
| `JUMP` respeta el rango de `bob` | cobertura de representación | `5bb0c41` |
| "libro" → `SHOW`, "plata" → `SHOW`, "carrito" → `POINT` | distinguibilidad | `5bb0c41` |
| Vestirse pasa a procedural | equivalencia | `5bb0c41` |

**Cuatro de los cinco defectos arreglados los encontró la instrumentación, no una
persona mirando la app.** Ninguno se había manifestado como un síntoma visible que
alguien hubiera reportado.

---

## 5. Pendientes

| Pendiente | Origen |
|---|---|
| `HOT`: `reach` negativo — único criterio en rojo | G.2 |
| "Pipí" y "Caca" producen la misma animación | F.2 |
| "Quiero llorar" → `WANT` en vez de `CRY` | F.1 |
| `usaParteDelCuerpo` conflaciona destino y halo | G.1 |
| Vestirse: ¿procedural o dos registros? | G.1 |
| Verificar en pantalla la altura del salto y los dos dibujos nuevos | — |

---

## 6. Cómo reproducir

Las pruebas de la JVM corren con directorio de trabajo `app/`. Orden obligatorio:
el volcado primero, porque genera el catálogo que las demás leen.

```
1. PoseCatalogDumpTest            regenera assets/animaciones.json
2. PoseCatalogParityTest          Apéndice G.1
3. InventarioTesisTest            Apéndice E
4. CoberturaRepresentacionTest    Apéndice G.2
5. CorpusResolvedorTest           Apéndices F.1 y F.1 bis
6. CoberturaSemanticaTest         Apéndice F.2
```

Cada prueba escribe su apéndice en `docs/tesis/salidas/`, además de imprimirlo.
Se guarda **también cuando la prueba falla**, que es justo la corrida que hay que
poder leer entera. Van a `docs/` y no a `build/` porque `build/` está ignorado por
git, y la tesis necesita citar un resultado contra el commit que lo produjo.

Para regenerar el catálogo hay que **borrar** `app/src/main/assets/animaciones.json`
primero: el volcado no pisa un archivo existente, para no arruinar el trabajo de
quien lo esté editando.
