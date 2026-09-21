package com.tunombre.agustispeaks.avatar.catalog

import com.tunombre.agustispeaks.avatar.ArmPose
import com.tunombre.agustispeaks.avatar.AvatarPose
import com.tunombre.agustispeaks.avatar.BodyPart
import com.tunombre.agustispeaks.avatar.Expression
import com.tunombre.agustispeaks.avatar.Fx
import com.tunombre.agustispeaks.avatar.Prop
import com.tunombre.agustispeaks.avatar.Stance

/**
 * Curva con la que un fotograma sale hacia el siguiente.
 *
 * - `COSENO`: arranca suave y llega suave. Reproduce exactamente la señal `up`
 *   de `avatarPoseFor`, que es la más usada del motor.
 * - `LINEAL`: a velocidad constante. Para rampas con esquina dura, como el
 *   parpadeo.
 * - `SALIDA`: arranca rápido y llega suave, como un movimiento que se frena al
 *   llegar al tope.
 * - `ENTRADA`: arranca suave y llega rápido, como uno que toma envión.
 *
 * Las dos últimas existen porque medio coseno no puede describir un cuarto de
 * seno, y buena parte del motor está hecha de senos completos (el balanceo del
 * cuerpo, el vaivén de la cabeza). Con las cuatro, un seno completo se describe
 * exacto con tres fotogramas; con solo las dos primeras hacían falta casi treinta.
 */
enum class Ease(val clave: String) {
    COSENO("coseno"),
    LINEAL("lineal"),
    SALIDA("salida"),
    ENTRADA("entrada");

    companion object {
        fun desde(clave: String): Ease? =
            entries.firstOrNull { it.clave.equals(clave, ignoreCase = true) }
    }
}

/**
 * Campo de [AvatarPose] que **se interpola** entre fotogramas.
 *
 * La separación entre campos continuos y discretos es deliberada y estructural:
 * un color de pose que se mezcla a medias no existe, pero una inclinación a
 * medias sí. Mezclar los dos caminos es el error clásico de este intérprete.
 */
enum class FloatField(val key: String, val default: Float) {
    BOB("bob", 0f),
    LEAN("lean", 0f),
    HEAD_TILT("headTilt", 0f),
    JAW("jaw", 0f),
    TEARS("tears", 0f),
    EYE_CLOSE("eyeClose", 0f),
    CROUCH("crouch", 0f),
    LEG_SWING("legSwing", 0f),
    ARM_SWING("armSwing", 0f),
    REACH("reach", 0f),
    PROP_PHASE("propPhase", 0f),
    FX_PULSE("fxPulse", 1f);

    fun of(p: AvatarPose): Float = when (this) {
        BOB -> p.bob
        LEAN -> p.lean
        HEAD_TILT -> p.headTilt
        JAW -> p.jaw
        TEARS -> p.tears
        EYE_CLOSE -> p.eyeClose
        CROUCH -> p.crouch
        LEG_SWING -> p.legSwing
        ARM_SWING -> p.armSwing
        REACH -> p.reach
        PROP_PHASE -> p.propPhase
        FX_PULSE -> p.fxPulse
    }
}

/**
 * Campo de [AvatarPose] que **cambia de golpe**. Toma el valor del último
 * fotograma que lo fijó y lo conserva hasta que otro lo cambie.
 */
enum class StepField(val key: String) {
    STANCE("stance"),
    ARM_L("armL"),
    ARM_R("armR"),
    EXPRESSION("expression"),
    PROP("prop"),
    FX("fx"),
    HAND_TARGET("handTarget"),
    HIGHLIGHT("highlight"),
    SLEEPING("sleeping");

    fun of(p: AvatarPose): Any? = when (this) {
        STANCE -> p.stance
        ARM_L -> p.armL
        ARM_R -> p.armR
        EXPRESSION -> p.expression
        PROP -> p.prop
        FX -> p.fx
        HAND_TARGET -> p.handTarget
        HIGHLIGHT -> p.highlight
        SLEEPING -> p.sleeping
    }

    /** Valor que toma el campo cuando ningún fotograma lo declara. */
    val defaultValue: Any?
        get() = when (this) {
            STANCE -> Stance.STAND
            ARM_L, ARM_R -> ArmPose.DOWN
            EXPRESSION -> null
            PROP -> null
            FX -> Fx.NONE
            HAND_TARGET, HIGHLIGHT -> null
            SLEEPING -> false
        }

    /** Nombres admitidos. Solo se usa para que el error del parser sea útil. */
    val options: List<String>
        get() = when (this) {
            STANCE -> Stance.entries.map { it.name }
            ARM_L, ARM_R -> ArmPose.entries.map { it.name }
            EXPRESSION -> Expression.entries.map { it.name }
            PROP -> Prop.entries.map { it.name }
            FX -> Fx.entries.map { it.name }
            HAND_TARGET, HIGHLIGHT -> BodyPart.entries.map { it.name }
            SLEEPING -> listOf("true", "false")
        }

    /** Lanza [IllegalArgumentException] si el nombre no existe. */
    fun parse(raw: String): Any = when (this) {
        STANCE -> Stance.valueOf(raw)
        ARM_L, ARM_R -> ArmPose.valueOf(raw)
        EXPRESSION -> Expression.valueOf(raw)
        PROP -> Prop.valueOf(raw)
        FX -> Fx.valueOf(raw)
        HAND_TARGET, HIGHLIGHT -> BodyPart.valueOf(raw)
        SLEEPING -> when (raw.lowercase()) {
            "true" -> true
            "false" -> false
            else -> throw IllegalArgumentException(raw)
        }
    }
}

/**
 * Un instante declarado de la animación. Solo dice lo que cambia: lo que no
 * aparece se hereda.
 *
 * En [steps], que la clave **esté presente con valor null** no es lo mismo que
 * que la clave falte. Presente-con-null apaga el campo (por ejemplo, borra la
 * expresión de la cara); ausente significa "no opino, dejá lo que había". Sin
 * esa distinción no habría forma de apagar nada a mitad de un ciclo.
 */
data class Keyframe(
    val t: Float,
    val ease: Ease? = null,
    val floats: Map<FloatField, Float> = emptyMap(),
    val steps: Map<StepField, Any?> = emptyMap()
)

/**
 * Una animación como dato: cómo se dispara y cómo se mueve.
 *
 * Las seis listas de disparo siguen una sola regla: **sin sufijo es palabra
 * completa, con sufijo `Raices` es subcadena**. La frase dispara el registro si
 * coincide alguna de [palabras] o [raices]; además debe coincidir alguna de
 * [ademas] y alguna de [ademasRaices] si esas listas no están vacías; y no debe
 * coincidir ninguna de [excluye] ni [excluyeRaices].
 *
 * Las tres formas existen porque las reglas actuales del resolvedor las usan:
 * lavarse las manos es "lavar" Y "manos", y tocar es "tocar" PERO NO "no toques".
 */
data class AnimationRecord(
    val id: String,
    val loopMillis: Int,
    val prioridad: Int = 100,

    val palabras: List<String> = emptyList(),
    val raices: List<String> = emptyList(),
    val ademas: List<String> = emptyList(),
    val ademasRaices: List<String> = emptyList(),
    val excluye: List<String> = emptyList(),
    val excluyeRaices: List<String> = emptyList(),

    /**
     * Si la frase nombra una parte del cuerpo, la pose la usa como objetivo de
     * la mano y como zona resaltada. Es lo que hace que "me duele la panza"
     * señale la panza y no el pecho.
     */
    val usaParteDelCuerpo: Boolean = false,

    /** Objeto por defecto, si ningún fotograma declara uno. */
    val prop: Prop? = null,

    /**
     * Nombre de un generador en Kotlin, para las animaciones que son matemática
     * y no fotogramas. Ver [ProceduralPoses]. Si está, los fotogramas se ignoran.
     */
    val procedural: String? = null,

    /**
     * Si el último fotograma vuelve al primero. Casi siempre sí. Un diente de
     * sierra (una fase que va de 0 a 1 y salta) necesita false, o el intérprete
     * interpola el salto de vuelta y la animación se ve al revés.
     */
    val cicloCerrado: Boolean = true,

    val fotogramas: List<Keyframe> = emptyList(),

    /** Posición en el archivo. Desempata entre registros de igual prioridad. */
    val orden: Int = 0
) {
    /**
     * [t] tiene que venir normalizado (minúsculas, sin tildes) y con un espacio
     * al principio y otro al final, igual que en `ActionResolver`. Sin esos
     * espacios la coincidencia por palabra completa no funciona.
     */
    fun matches(t: String): Boolean {
        val dispara = palabras.any { t.contains(" $it ") } || raices.any { t.contains(it) }
        if (!dispara) return false
        if (ademas.isNotEmpty() && ademas.none { t.contains(" $it ") }) return false
        if (ademasRaices.isNotEmpty() && ademasRaices.none { t.contains(it) }) return false
        if (excluye.any { t.contains(" $it ") }) return false
        if (excluyeRaices.any { t.contains(it) }) return false
        return true
    }
}

/**
 * Lo que salió de leer un catálogo. Los registros rotos no tumban el archivo:
 * se saltean y se anotan en [problemas]. En una app de comunicación, un
 * catálogo con un error de tipeo tiene que seguir andando con el resto.
 */
data class CatalogParseResult(
    val records: List<AnimationRecord>,
    val problemas: List<String>
)

/**
 * Fusiona las dos capas del catálogo. [base] es lo que viene empaquetado en el
 * APK; [encima] son los registros que guardó el especialista. **Ante id
 * repetido gana [encima]**, que es lo que convierte a la base de datos en un
 * lugar donde corregir animaciones sin reinstalar la app.
 *
 * Un reemplazo conserva la posición del original en vez de irse al final. El
 * orden importa: es el desempate entre reglas de igual prioridad, así que si
 * corregir un registro lo mandara al fondo, corregir el dibujo de una animación
 * podría cambiar de paso qué frase la dispara.
 */
fun fusionar(
    base: List<AnimationRecord>,
    encima: List<AnimationRecord>
): List<AnimationRecord> {
    val reemplazos = encima.associateBy { it.id }
    val usados = mutableSetOf<String>()

    val resultado = mutableListOf<AnimationRecord>()
    for (registro in base) {
        val reemplazo = reemplazos[registro.id]
        if (reemplazo != null) {
            usados += registro.id
            resultado += reemplazo
        } else {
            resultado += registro
        }
    }
    // Los que no existían en la base van después, en el orden en que vinieron.
    for (registro in encima) if (registro.id !in usados) resultado += registro

    return resultado.mapIndexed { posicion, registro -> registro.copy(orden = posicion) }
}
