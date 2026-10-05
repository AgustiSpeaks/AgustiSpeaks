# Apéndice D — Modelo de pose e intérprete

El avatar no tiene imágenes. En cada instante es un valor de 21 campos que el
renderizador convierte en primitivas vectoriales sobre un `Canvas` de Compose.

---

## D.1 La pose

`AvatarPose` tiene **21 campos**, y el intérprete los parte en dos grupos
disjuntos que lo cubren por completo.

### Doce campos continuos — se interpolan

| Campo | Rango declarado | Qué controla |
|---|---|---|
| `bob` | −1 a 1 | rebote vertical del cuerpo |
| `lean` | −1 a 1 | inclinación del cuerpo |
| `headTilt` | grados, sin tope | giro de la cabeza |
| `jaw` | 0 a 1 | apertura de la boca |
| `tears` | 0 a 1 | lágrimas |
| `eyeClose` | 0 a 1 | cierre de los párpados |
| `crouch` | 0 a 1 | de pie (0) a agachado (1) |
| `legSwing` | −1 a 1 | paso al caminar o correr |
| `armSwing` | −1 a 1 | balanceo de los brazos |
| `reach` | 0 a 1 | extensión del brazo activo |
| `propPhase` | 0 a 1 | fase del objeto, independiente del brazo |
| `fxPulse` | 0 a 1 | latido del símbolo de apoyo |

`fxPulse` es el único cuyo valor por defecto no es 0, sino 1: un símbolo sin
animación declarada se muestra entero, no invisible.

### Nueve campos discretos — se sostienen

| Campo | Tipo | Por defecto |
|---|---|---|
| `stance` | `Stance` | `STAND` |
| `armL` | `ArmPose` | `DOWN` |
| `armR` | `ArmPose` | `DOWN` |
| `expression` | `Expression?` | `null` |
| `prop` | `Prop?` | `null` |
| `fx` | `Fx` | `NONE` |
| `handTarget` | `BodyPart?` | `null` |
| `highlight` | `BodyPart?` | `null` |
| `sleeping` | `Boolean` | `false` |

### La partición es completa, y eso importa

12 + 9 = 21, que es exactamente el número de campos. **Ningún campo queda fuera
de los dos grupos.**

No es casualidad: que la unión de los dos enums cubra `AvatarPose` entero es lo
que garantiza que una animación del catálogo pueda expresar **cualquier** pose
que el motor procedural sea capaz de producir. Si un campo quedara afuera,
habría poses irrepresentables como datos, y la migración no podría ser completa
ni siquiera en principio.

### Por qué la separación es estructural y no una optimización

Un valor a medio camino entre dos posturas no existe: no hay una pose intermedia
entre "de pie" y "sentado", ni media expresión entre triste y contenta. Una
inclinación a medias, en cambio, sí existe.

Mezclar los dos caminos es el error clásico de este tipo de intérprete.

## D.2 Las cuatro curvas

Cada fotograma declara con qué curva sale hacia el siguiente. Con `u` la fracción
recorrida del segmento, de 0 a 1:

| Curva | Fórmula | Forma |
|---|---|---|
| `lineal` | `u` | velocidad constante |
| `coseno` | `0,5 − 0,5·cos(u·π)` | arranca suave, llega suave |
| `salida` | `sin(u·π/2)` | arranca rápido, se frena al llegar |
| `entrada` | `1 − cos(u·π/2)` | arranca suave, toma envión |

### Por qué hacen falta las cuatro

`coseno` reproduce exactamente la señal más usada del motor. Pero **medio coseno
no puede describir un cuarto de seno**, y buena parte del motor está hecha de
senos completos: el balanceo del cuerpo, el vaivén de la cabeza.

Con las cuatro curvas, un seno completo se describe **exacto con tres
fotogramas**. Con solo `lineal` y `coseno` hacían falta casi treinta.

Ese fue el cambio que volvió legible el catálogo: de 1.088 fotogramas a menos de
300 para las mismas animaciones.

## D.3 Cómo interpola

Cada campo tiene **su propia línea de tiempo**. Es lo que permite que un
fotograma declare solo `lean` sin congelar de paso el `bob` del vecino.

```
poseAt(t, resuelta):
    p ← t − piso(t)                     # 1,0 vuelve a 0,0; tolera t negativo

    si el registro tiene generador procedural:
        devolver generador(p, resuelta)  # los fotogramas se ignoran

    para cada campo continuo:  valor ← su pista interpolada en p
    para cada campo discreto:  valor ← su pista sostenida en p

    # La parte del cuerpo que nombró la frase gana sobre la del registro
    # El objeto que detectó el resolvedor gana sobre el del fotograma
    devolver AvatarPose(...)
```

Una **pista continua** busca el último punto declarado en `p` o antes, e
interpola hacia el siguiente con la curva de ese punto. Si el ciclo es cerrado,
el último punto interpola hacia el primero dando la vuelta.

Una **pista discreta** nunca interpola: sostiene el último valor fijado, y antes
del primer punto sostiene el que cierra el ciclo.

## D.4 Fragmento real

De `avatar/catalog/AnimationInterpreter.kt`. El segmento de una pista continua.

```kotlin
private fun seg(a: Float, b: Float, ease: Ease, t: Float, tA: Float, tB: Float): Float {
    val span = tB - tA
    // Dos fotogramas en el mismo instante: el segundo manda, sin dividir por cero.
    if (span <= 0f) return b
    val u = ((t - tA) / span).coerceIn(0f, 1f)
    val mediaVuelta = PI.toFloat()
    val cuartoDeVuelta = mediaVuelta / 2f
    val e = when (ease) {
        Ease.LINEAL -> u
        Ease.COSENO -> 0.5f - 0.5f * cos(u * mediaVuelta)
        Ease.SALIDA -> sin(u * cuartoDeVuelta)
        Ease.ENTRADA -> 1f - cos(u * cuartoDeVuelta)
    }
    return a + (b - a) * e
}
```

Y la pista discreta entera, que cabe en seis líneas:

```kotlin
internal class StepTrack(
    private val times: FloatArray,
    private val values: Array<Any?>
) {
    fun valueAt(t: Float): Any? {
        var i = -1
        for (k in times.indices) if (times[k] <= t) i = k else break
        return values[if (i < 0) values.size - 1 else i]
    }
}
```

## D.5 La escapatoria procedural

Doce de las 88 acciones no llevan fotogramas sino el **nombre de un generador en
Kotlin**. El registro solo guarda el nombre; el movimiento vive en código.

| Motivo | Acciones |
|---|---|
| Temblor rápido, 8 ciclos por bucle | dolor, descompuesto |
| Tiritón, 16 ciclos por bucle | frío, nervioso, miedo |
| Masticación, 7 ciclos rectificados | comer |
| Trazo, 9 ciclos por bucle | dibujar |
| Diente de sierra: la fase salta en el borde | escuchar, después, antes |
| Mandíbula que salta entre dos valores fijos | remedio |
| La pose se bifurca según la prenda | vestirse |

### El criterio, afinado

Las primeras cuatro filas son cuestión de **frecuencia**: ninguna cantidad
razonable de fotogramas describe dieciséis oscilaciones por ciclo, y forzarlo
terminaría inventando un lenguaje de programación dentro del JSON.

Las dos últimas son otra cosa, y son las interesantes. Interpolar entre dos
valores es lo contrario de **saltar** entre dos valores. Y vestirse es **una
animación simple que igual no se puede declarar**: su pose toma formas distintas
según dónde va la prenda, y un registro tiene una sola pista por campo.

De ahí el criterio que la tesis puede sostener:

> **La frontera entre datos y código no la marca la complejidad de la animación.
> La marca si lo que hace variar a la pose es un número o una categoría.**

Los generadores **delegan en el motor original**, no duplican una línea de su
matemática. Son idénticos por construcción, no por parecido.
