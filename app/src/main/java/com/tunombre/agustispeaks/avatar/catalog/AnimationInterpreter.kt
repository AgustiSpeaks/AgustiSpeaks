package com.tunombre.agustispeaks.avatar.catalog

import com.tunombre.agustispeaks.avatar.ArmPose
import com.tunombre.agustispeaks.avatar.AvatarPose
import com.tunombre.agustispeaks.avatar.BodyPart
import com.tunombre.agustispeaks.avatar.Expression
import com.tunombre.agustispeaks.avatar.Fx
import com.tunombre.agustispeaks.avatar.Prop
import com.tunombre.agustispeaks.avatar.ResolvedAction
import com.tunombre.agustispeaks.avatar.Stance
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.sin

/**
 * Pista de un campo continuo: los instantes en que fue declarado, sus valores y
 * con qué curva sale cada uno hacia el siguiente.
 *
 * Cada campo tiene su propia línea de tiempo. Eso es lo que permite que un
 * fotograma declare solo `lean` sin congelar de paso el `bob` del vecino.
 */
internal class FloatTrack(
    private val times: FloatArray,
    private val values: FloatArray,
    private val eases: Array<Ease>,
    private val closed: Boolean
) {
    fun valueAt(t: Float): Float {
        val n = times.size
        if (n == 1) return values[0]

        // Último punto declarado en t o antes.
        var i = -1
        for (k in 0 until n) {
            if (times[k] <= t) i = k else break
        }

        if (i < 0) {
            // Antes del primer punto: venimos del último, dando la vuelta.
            if (!closed) return values[0]
            return seg(values[n - 1], values[0], eases[n - 1], t + 1f, times[n - 1], times[0] + 1f)
        }
        if (i == n - 1) {
            if (!closed) return values[n - 1]
            return seg(values[n - 1], values[0], eases[n - 1], t, times[n - 1], times[0] + 1f)
        }
        return seg(values[i], values[i + 1], eases[i], t, times[i], times[i + 1])
    }

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
}

/**
 * Pista de un campo discreto. Nunca interpola: sostiene el último valor fijado.
 * Antes del primer punto sostiene el que cierra el ciclo, no un valor inventado.
 */
internal class StepTrack(
    private val times: FloatArray,
    private val values: Array<Any?>
) {
    fun valueAt(t: Float): Any? {
        var i = -1
        for (k in times.indices) {
            if (times[k] <= t) i = k else break
        }
        return values[if (i < 0) values.size - 1 else i]
    }
}

/**
 * Un [AnimationRecord] ya convertido en pistas. La conversión se hace una vez,
 * al cargar el catálogo; el ciclo de dibujo corre 24 veces por segundo y no es
 * lugar para recorrer listas de fotogramas ni construir mapas.
 */
class CompiledAnimation internal constructor(
    val record: AnimationRecord,
    private val floats: Map<FloatField, FloatTrack>,
    private val steps: Map<StepField, StepTrack>
) {
    val id: String get() = record.id
    val loopMillis: Int get() = record.loopMillis

    /** Cantidad de fotogramas declarados. Solo informativo. */
    val keyframeCount: Int get() = record.fotogramas.size

    fun poseAt(t: Float, resolved: ResolvedAction): AvatarPose {
        val p = t - floor(t) // 1.0 vuelve a 0.0; también tolera t negativo

        record.procedural?.let { nombre ->
            ProceduralPoses.generate(nombre, p, resolved)?.let { return it }
        }

        fun f(field: FloatField): Float = floats[field]?.valueAt(p) ?: field.default
        fun s(field: StepField): Any? = steps[field]?.valueAt(p) ?: field.defaultValue

        val delFotograma = s(StepField.PROP) as? Prop
        val objetivo = s(StepField.HAND_TARGET) as? BodyPart
        val resaltado = s(StepField.HIGHLIGHT) as? BodyPart
        // La parte del cuerpo que nombró la frase gana sobre la del registro.
        val parte = resolved.bodyPart.takeIf { record.usaParteDelCuerpo }

        return AvatarPose(
            bob = f(FloatField.BOB),
            lean = f(FloatField.LEAN),
            headTilt = f(FloatField.HEAD_TILT),
            jaw = f(FloatField.JAW),
            expression = s(StepField.EXPRESSION) as? Expression,
            tears = f(FloatField.TEARS),
            sleeping = s(StepField.SLEEPING) as? Boolean ?: false,
            eyeClose = f(FloatField.EYE_CLOSE),

            stance = s(StepField.STANCE) as? Stance ?: Stance.STAND,
            crouch = f(FloatField.CROUCH),
            legSwing = f(FloatField.LEG_SWING),

            armL = s(StepField.ARM_L) as? ArmPose ?: ArmPose.DOWN,
            armR = s(StepField.ARM_R) as? ArmPose ?: ArmPose.DOWN,
            armSwing = f(FloatField.ARM_SWING),
            reach = f(FloatField.REACH),
            handTarget = parte ?: objetivo,

            // Mismo orden de precedencia que `avatarPoseFor`: el objeto que
            // detectó el resolvedor gana, porque es el que sabe que pidió sopa.
            prop = resolved.prop ?: delFotograma ?: record.prop,
            propPhase = f(FloatField.PROP_PHASE),

            fx = s(StepField.FX) as? Fx ?: Fx.NONE,
            fxPulse = f(FloatField.FX_PULSE),
            highlight = parte ?: resaltado
        )
    }
}

internal fun AnimationRecord.compile(): CompiledAnimation {
    val ordenados = fotogramas.sortedBy { it.t }

    val floats = LinkedHashMap<FloatField, FloatTrack>()
    for (campo in FloatField.entries) {
        val puntos = ordenados.filter { it.floats.containsKey(campo) }
        if (puntos.isEmpty()) continue
        floats[campo] = FloatTrack(
            times = FloatArray(puntos.size) { puntos[it].t },
            values = FloatArray(puntos.size) { puntos[it].floats.getValue(campo) },
            eases = Array(puntos.size) { puntos[it].ease ?: Ease.COSENO },
            closed = cicloCerrado
        )
    }

    val steps = LinkedHashMap<StepField, StepTrack>()
    for (campo in StepField.entries) {
        val puntos = ordenados.filter { it.steps.containsKey(campo) }
        if (puntos.isEmpty()) continue
        steps[campo] = StepTrack(
            times = FloatArray(puntos.size) { puntos[it].t },
            values = Array(puntos.size) { puntos[it].steps[campo] }
        )
    }

    return CompiledAnimation(this, floats, steps)
}

/**
 * Id con el que se busca la animación de [r]: el que fijó el disparador del
 * catálogo, o el nombre de la acción en minúsculas, que es la convención con la
 * que se generó el volcado inicial.
 *
 * Vive acá, y no en `AvatarCatalog`, para que los tests de la JVM puedan usar
 * exactamente esta función en vez de una copia. Una convención de nombres
 * duplicada es una que en algún momento deja de coincidir consigo misma.
 */
fun catalogIdOf(r: ResolvedAction): String = r.catalogId ?: r.action.name.lowercase()

/**
 * El catálogo cargado y listo para usar. Resuelve por id (para animar) y por
 * frase (para disparar).
 */
class AnimationCatalog(records: List<AnimationRecord>) {

    private val porId: Map<String, CompiledAnimation> =
        records.associate { it.id to it.compile() }

    /**
     * Reglas en orden de evaluación: prioridad ascendente y, a igual prioridad,
     * el orden en que venían en el archivo. El orden importa de verdad: las
     * bebidas tienen que evaluarse antes que cualquier otra cosa porque "te" es
     * de dos letras, y los alimentos antes que el verbo comer o "quiero comer
     * pan" pierde el pan.
     */
    private val reglas: List<CompiledAnimation> = porId.values.sortedWith(
        compareBy({ it.record.prioridad }, { it.record.orden })
    )

    val size: Int get() = porId.size

    fun byId(id: String): CompiledAnimation? = porId[id]

    /** [normalized] con espacios al principio y al final. Ver [AnimationRecord.matches]. */
    fun match(normalized: String): CompiledAnimation? =
        reglas.firstOrNull { it.record.matches(normalized) }

    fun ids(): Set<String> = porId.keys

    companion object {
        val EMPTY = AnimationCatalog(emptyList())
    }
}
