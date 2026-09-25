package com.tunombre.agustispeaks.avatar

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/** Motor de movimiento del avatar. El detalle (parte del cuerpo) va en [ResolvedAction]. */
enum class AvatarAction(val loopMillis: Int) {
    IDLE(2600), TALK(1400),
    WANT(1500), MORE(1400), LESS(1400), GIVE(1700), TAKE(1600),
    EAT(1500), DRINK(1800),
    GO(1000), RUN(650), JUMP(750), DANCE(1000), SIT(2600), STAND(1800),
    SLEEP(3800), TIRED(2800),
    PLAY(900), LOOK(2000), POINT(2000), READ(2600), SCRIBBLE(1600), LISTEN(2400), SING(1200),
    // Higiene: bucles largos a propósito. Frotarse, cepillarse y enjabonarse
    // son gestos lentos; a 1100 ms el brazo iba y venía tan rápido que se leía
    // como un temblor en vez de como una acción.
    WASH_HANDS(2400), BRUSH_TEETH(2000), BATHE(2400), COMB(1400), TOILET(2600),
    HELP(1100), STOP(2200), WAIT(2400), TAP(1600), SEARCH(2200),
    HUG(1900), KISS(1600),
    HELLO(900), BYE(900), THANKS(2200), SORRY(2400), PLEASE(2200),
    /** Estado: "bien" / "mal", y la pregunta "¿cómo estás?". */
    GOOD(1500), BAD(2600), ASK(2000),
    YES(1200), NO(1000),
    OPEN(1500), CLOSE(1500),
    HURT(1300), TOUCH_PART(2400),
    COLD(900), HOT(900), FEVER(2600), SICK(2400), MEDICINE(1800), INJECTION(2000),
    HAPPY(1000), LAUGH(900), SAD(3000), CRY(2600), ANGRY(1600), NERVOUS(1400), SCARED(650),
    BORED(3400), CALM(3000), SURPRISED(1500),
    /** Vestirse. La prenda viaja en `prop` y la parte del cuerpo en `bodyPart`. */
    DRESS(1800),
    /** "Vení": el brazo llama hacia el cuerpo. */
    COME(1300),
    /** "Terminé": las dos palmas se abren hacia afuera. */
    DONE(1600),
    /** Pagar: extiende la mano con el dinero. */
    PAY(1600),
    /** Comprar: empuja el carrito caminando. */
    SHOP(1400),
    /** Llevar una bolsa o una canasta en la mano, caminando. */
    CARRY(1200),
    /* Juego. Cada una tiene su propia postura: antes eran todas PLAY y el
       tobogán, la hamaca y los juguetes se veían exactamente iguales. */
    TOYS(1800), BLOCKS(2000), CRADLE(2600), SWING(2200), SLIDE(1600),
    /** Mostrar un objeto en alto, sin usarlo: "esto es un lápiz". */
    SHOW(1800),
    /** Escribir sentado a la mesa, distinto de dibujar de pie. */
    WRITE(2200),
    /* Tiempo. El reloj y el cielo hacen casi todo el trabajo: son conceptos
       abstractos y la postura sola no alcanza para distinguir ayer de mañana. */
    NOW(1800), LATER(2000), BEFORE(2000),
    DAY(2000), NIGHT(3200), RAIN(2400), CLOUDY(2800),
    /** Estar con alguien: la otra persona al lado y el avatar saludándola. */
    WITH(1900),
    /** Ir a un lugar: camina y el sitio aparece detrás. */
    GO_TO(1100)
}

/** Resultado de interpretar una frase: acción + matices. */
data class ResolvedAction(
    val action: AvatarAction,
    val bodyPart: BodyPart? = null,
    val prop: Prop? = null,
    /**
     * Id de la animación en el catálogo, cuando la frase la disparó una regla
     * del catálogo y no una de [ActionResolver]. Si es null se busca por el
     * nombre de [action] en minúsculas, que es como el volcado generó los ids.
     *
     * Una animación nueva, agregada por un especialista, no tiene [AvatarAction]
     * propia: ahí este campo es el que manda y `action` queda de etiqueta.
     */
    val catalogId: String? = null
)

/** Pose del avatar (cuerpo completo) para [r] en la fase de bucle [t] (0..1). */
fun avatarPoseFor(r: ResolvedAction, t: Float): AvatarPose {
    val tau = 2f * PI.toFloat()
    val w = sin(t * tau)
    val w2 = sin(t * 2f * tau)
    val up = 0.5f - 0.5f * cos(t * tau)
    val fast = sin(t * 8f * tau)
    val chew = abs(sin(t * 14f * PI.toFloat())) * 0.7f
    val shiver = sin(t * 16f * tau)
    val blink = if (t > 0.9f) (t - 0.9f) / 0.1f else 0f
    val pulse = 0.82f + 0.18f * up
    // Dos ciclos suaves por bucle: sirve para gestos deliberados como tocar.
    val tap = 0.5f - 0.5f * cos(t * 2f * tau)
    // Tres pasadas lentas por bucle, para frotar, cepillar y enjabonarse. Antes
    // estos gestos usaban `fast`, que da dieciséis oscilaciones: el mismo error
    // que tenía "tocar", donde la mano tiritaba en vez de hacer el gesto.
    val rub = 0.5f - 0.5f * cos(t * 3f * tau)
    val part = r.bodyPart

    // Marcha DE FRENTE: el paso se lee por la rodilla que sube y el rebote del
    // cuerpo, no por la separación de las piernas. `lean` queda chico, apenas
    // el balanceo de peso de un lado al otro.
    fun walk(run: Boolean) = AvatarPose(
        stance = if (run) Stance.RUN else Stance.WALK,
        legSwing = w,
        armSwing = -w,
        armL = ArmPose.SWING, armR = ArmPose.SWING,
        bob = abs(w) * (if (run) 0.85f else 0.5f),
        lean = (if (run) 0.12f else 0.07f) * w,
        jaw = if (run) 0.45f else 0f,
        expression = if (run) Expression.HAPPY else null,
        fx = Fx.SPEED
    )

    val base = when (r.action) {
        AvatarAction.IDLE -> AvatarPose(bob = 0.22f * w, eyeClose = blink)
        AvatarAction.TALK -> AvatarPose(bob = 0.12f * w, jaw = 0.25f + abs(w2) * 0.6f, armSwing = 0.2f * w, armL = ArmPose.SWING, armR = ArmPose.SWING, fx = Fx.SPEECH)

        AvatarAction.WANT -> AvatarPose(armR = ArmPose.REACH_FWD, reach = 0.5f + 0.5f * up, jaw = abs(w2) * 0.35f, lean = 0.16f * up, fx = Fx.ARROW_OUT)
        // "Más" y "menos": las dos manos se juntan y se separan, como en la seña.
        AvatarAction.MORE -> AvatarPose(armL = ArmPose.HOLD_BOTH, armR = ArmPose.HOLD_BOTH, reach = 0.3f + 0.65f * up, expression = Expression.HAPPY, fx = Fx.PLUS)
        AvatarAction.LESS -> AvatarPose(armL = ArmPose.HOLD_BOTH, armR = ArmPose.HOLD_BOTH, reach = 0.95f - 0.65f * up, fx = Fx.MINUS)
        AvatarAction.GIVE -> AvatarPose(armR = ArmPose.REACH_FWD, reach = 0.45f + 0.55f * up, lean = 0.2f * up, fx = Fx.ARROW_OUT)
        AvatarAction.TAKE -> AvatarPose(armR = ArmPose.REACH_FWD, reach = 1f - up, lean = -0.16f * up, fx = Fx.ARROW_IN)

        // El alimento sube hacia la boca pero se detiene antes de taparla: tiene
        // que verse QUÉ está pidiendo. La boca queda abierta todo el ciclo.
        AvatarAction.EAT -> AvatarPose(
            armR = ArmPose.TO_TARGET, handTarget = BodyPart.MOUTH,
            reach = 0.15f + 0.55f * up,
            jaw = if (up > 0.5f) 0.45f + chew * 0.5f else 0.22f,
            headTilt = 6f * up, lean = 0.06f * up,
            prop = Prop.FOOD
        )
        AvatarAction.DRINK -> AvatarPose(armR = ArmPose.TO_TARGET, handTarget = BodyPart.MOUTH, reach = 0.35f + 0.65f * up, headTilt = -16f * up, prop = Prop.CUP)
        AvatarAction.MEDICINE -> AvatarPose(armR = ArmPose.TO_TARGET, handTarget = BodyPart.MOUTH, reach = up, jaw = if (up > 0.6f) 0.65f else 0.12f, prop = Prop.SPOON)

        AvatarAction.GO -> walk(false)
        AvatarAction.RUN -> walk(true)
        // `bob` es la fracción del ascenso ya recorrida, de 0 a 1. El factor
        // era 2.2, que lo llevaba hasta 1.43 y se salía del rango que declara
        // `AvatarPose`. No era inocuo: la figura sube por el valor crudo
        // (`dy = -bob * H * 0.04`) y la sombra lo recortaba en 1, así que en lo
        // alto del salto la figura seguía subiendo y la sombra dejaba de
        // achicarse. Dividir por 0.65 normaliza el tramo y además dice qué
        // significa el número.
        AvatarAction.JUMP -> AvatarPose(
            stance = Stance.JUMP,
            crouch = if (up < 0.35f) 1f - up * 2.8f else 0f,
            bob = if (up >= 0.35f) (up - 0.35f) / 0.65f else 0f,
            armL = ArmPose.WAVE, armR = ArmPose.WAVE,
            expression = Expression.HAPPY, fx = Fx.JUMP_ARC
        )
        AvatarAction.DANCE -> AvatarPose(bob = abs(w) * 0.6f, lean = 0.4f * w2, headTilt = 8f * w, armL = ArmPose.WAVE, armR = ArmPose.WAVE, armSwing = w, expression = Expression.HAPPY, fx = Fx.MUSIC)
        // Con silla e inodoro detrás, "sentarse" y "baño" dejan de ser la misma pose.
        AvatarAction.SIT -> AvatarPose(stance = Stance.SIT, crouch = 1f, bob = 0.04f * w, prop = Prop.CHAIR)
        AvatarAction.STAND -> AvatarPose(bob = 0.12f * w)

        AvatarAction.SLEEP -> AvatarPose(stance = Stance.LIE, sleeping = true, eyeClose = 1f, bob = 0.06f * w, prop = Prop.BED)
        AvatarAction.TIRED -> AvatarPose(eyeClose = 0.55f + 0.35f * w, headTilt = 14f, bob = 0.1f * w, crouch = 0.12f, expression = Expression.SAD)

        AvatarAction.PLAY -> AvatarPose(bob = 0.25f + abs(w) * 0.75f, armL = ArmPose.WAVE, armR = ArmPose.WAVE, reach = 0.6f + 0.4f * up, prop = Prop.BALL, expression = Expression.HAPPY, fx = Fx.SPARKLE)
        AvatarAction.LOOK -> AvatarPose(armR = ArmPose.POINT_FWD, reach = 1f, headTilt = 9f * w, expression = Expression.SURPRISED, lean = 0.12f, fx = Fx.TARGET)
        AvatarAction.POINT -> AvatarPose(armR = ArmPose.POINT_FWD, reach = 1f, headTilt = 6f * w, lean = 0.14f, fx = Fx.TARGET)
        AvatarAction.READ -> AvatarPose(armL = ArmPose.HOLD_BOTH, armR = ArmPose.HOLD_BOTH, reach = 0.75f, headTilt = 12f, eyeClose = 0.3f, prop = Prop.BOOK)
        AvatarAction.SCRIBBLE -> AvatarPose(armR = ArmPose.TO_TARGET, handTarget = BodyPart.BELLY, reach = 0.72f + 0.16f * sin(t * 18f * PI.toFloat()), headTilt = 10f, crouch = 0.1f, prop = Prop.PENCIL)
        // Mano ahuecada pegada a la oreja y una bocina que emite ondas.
        AvatarAction.LISTEN -> AvatarPose(
            armR = ArmPose.CUP_EAR, reach = 1f,
            headTilt = 6f * w, lean = -0.1f,
            expression = Expression.SURPRISED,
            prop = Prop.HORN, propPhase = t
        )
        AvatarAction.SING -> AvatarPose(jaw = 0.4f + abs(w) * 0.6f, headTilt = 8f * w, armL = ArmPose.WAVE, armR = ArmPose.WAVE, reach = 0.7f, expression = Expression.HAPPY, fx = Fx.MUSIC)

        // Las manos se frotan sobre el lavabo, con el agua cayendo encima. La
        // pileta es lo que separa esto de "me froto las manos porque tengo frío".
        AvatarAction.WASH_HANDS -> AvatarPose(
            armL = ArmPose.RUB, armR = ArmPose.RUB,
            reach = 0.18f + 0.64f * rub,
            bob = 0.05f * rub, crouch = 0.12f, headTilt = 9f,
            prop = Prop.SINK, fx = Fx.BUBBLES
        )
        AvatarAction.BRUSH_TEETH -> AvatarPose(armR = ArmPose.TO_TARGET, handTarget = BodyPart.MOUTH, reach = 0.95f, jaw = 0.35f + 0.28f * rub, prop = Prop.TOOTHBRUSH)
        // reach por debajo de 0.8 a propósito: con las dos manos al mismo punto,
        // más cerca del objetivo se superponen y se ve una sola.
        AvatarAction.BATHE -> AvatarPose(armL = ArmPose.TO_TARGET, armR = ArmPose.TO_TARGET, handTarget = BodyPart.HEAD, reach = 0.70f + 0.10f * rub, prop = Prop.SHOWER, expression = Expression.HAPPY, fx = Fx.BUBBLES)
        AvatarAction.COMB -> AvatarPose(armR = ArmPose.TO_TARGET, handTarget = BodyPart.HAIR, reach = 0.75f + 0.25f * w, headTilt = -4f * w, prop = Prop.COMB)
        AvatarAction.TOILET -> AvatarPose(stance = Stance.SIT, crouch = 0.9f, headTilt = 5f * w, expression = Expression.NEUTRAL, prop = Prop.TOILET)

        AvatarAction.HELP -> AvatarPose(armL = ArmPose.WAVE, armR = ArmPose.WAVE, reach = 0.6f + 0.4f * up, expression = Expression.SAD, bob = 0.2f * w, fx = Fx.EXCLAIM)
        AvatarAction.STOP -> AvatarPose(armR = ArmPose.PALM_OUT, reach = 1f, lean = -0.16f, expression = Expression.NEUTRAL, fx = Fx.STOP_SIGN)
        // Puño cerrado con el índice levantado: "esperá un momento".
        AvatarAction.WAIT -> AvatarPose(
            armR = ArmPose.FINGER_UP, reach = 0.7f + 0.3f * up,
            headTilt = 4f * w, bob = 0.06f * w, expression = Expression.NEUTRAL, fx = Fx.CLOCK
        )

        AvatarAction.HUG -> AvatarPose(armL = ArmPose.HOLD_BOTH, armR = ArmPose.HOLD_BOTH, reach = 0.75f + 0.25f * up, headTilt = 9f * w, expression = Expression.HAPPY, eyeClose = 0.4f, fx = Fx.HEART)
        AvatarAction.KISS -> AvatarPose(armR = ArmPose.TO_TARGET, handTarget = BodyPart.MOUTH, reach = up, expression = Expression.HAPPY, eyeClose = 0.5f, headTilt = 6f * up, fx = Fx.HEART)

        AvatarAction.HELLO, AvatarAction.BYE -> AvatarPose(armR = ArmPose.WAVE, reach = 0.5f + 0.5f * w, headTilt = 5f * w, expression = Expression.HAPPY, bob = 0.1f * abs(w))
        AvatarAction.THANKS -> AvatarPose(headTilt = 16f * up, armR = ArmPose.TO_TARGET, handTarget = BodyPart.CHEST, reach = 0.75f * up, crouch = 0.1f * up, expression = Expression.HAPPY, eyeClose = 0.5f * up, fx = Fx.HEART)
        AvatarAction.SORRY -> AvatarPose(headTilt = 16f * up, armR = ArmPose.TO_TARGET, handTarget = BodyPart.CHEST, reach = 0.7f, crouch = 0.15f * up, expression = Expression.SAD, eyeClose = 0.4f)
        // Manos juntas por delante y una reverencia corta.
        AvatarAction.PLEASE -> AvatarPose(armL = ArmPose.HOLD_BOTH, armR = ArmPose.HOLD_BOTH, reach = 0.95f, headTilt = 12f * up, crouch = 0.12f * up, eyeClose = 0.35f, expression = Expression.SAD)

        // "Bien" y "mal" tienen que demostrarse, no solo nombrarse: el cuerpo
        // entero cambia, no únicamente la cara.
        AvatarAction.GOOD -> AvatarPose(
            armL = ArmPose.THUMB_UP, armR = ArmPose.THUMB_UP, reach = 1f,
            bob = 0.25f + abs(w) * 0.6f, expression = Expression.HAPPY, fx = Fx.SPARKLE
        )
        AvatarAction.BAD -> AvatarPose(
            armL = ArmPose.THUMB_DOWN, armR = ArmPose.THUMB_DOWN, reach = 1f,
            crouch = 0.32f, headTilt = 12f, bob = -0.12f * up,
            expression = Expression.SAD, tears = 0.25f * up
        )
        // Palmas abiertas hacia afuera: el gesto de preguntar.
        AvatarAction.ASK -> AvatarPose(
            armL = ArmPose.PALM_OUT, armR = ArmPose.PALM_OUT, reach = 0.55f + 0.35f * up,
            headTilt = 12f * w, bob = 0.14f * up, expression = Expression.SURPRISED, fx = Fx.QUESTION
        )

        AvatarAction.YES -> AvatarPose(headTilt = 18f * up - 7f, armR = ArmPose.THUMB_UP, reach = 1f, expression = Expression.HAPPY, fx = Fx.CHECK)
        AvatarAction.NO -> AvatarPose(lean = 0.42f * w, headTilt = -7f * w, armR = ArmPose.THUMB_DOWN, reach = 1f, fx = Fx.CROSS)

        // Abrir y cerrar una puerta de verdad: el brazo queda en el picaporte y
        // la hoja gira. `propPhase` es cuánto está abierta.
        AvatarAction.OPEN -> AvatarPose(
            armR = ArmPose.REACH_FWD, reach = 0.88f + 0.12f * up,
            lean = 0.1f * up, propPhase = up, prop = Prop.DOOR, fx = Fx.ARROW_OUT
        )
        AvatarAction.CLOSE -> AvatarPose(
            armR = ArmPose.REACH_FWD, reach = 0.88f + 0.12f * (1f - up),
            lean = -0.08f * up, propPhase = 1f - up, prop = Prop.DOOR, fx = Fx.ARROW_IN
        )

        AvatarAction.HURT -> AvatarPose(
            armR = ArmPose.TO_TARGET, handTarget = part ?: BodyPart.CHEST,
            reach = 0.85f + 0.1f * w, expression = Expression.HURT, lean = 0.08f * fast, headTilt = 6f * w,
            fx = Fx.PAIN, highlight = part ?: BodyPart.CHEST
        )
        AvatarAction.TOUCH_PART -> AvatarPose(
            armR = ArmPose.POINT_AT, handTarget = part ?: BodyPart.HEAD,
            reach = 0.6f + 0.15f * up, headTilt = 4f * w, expression = Expression.NEUTRAL,
            highlight = part ?: BodyPart.HEAD
        )
        // Dos toques lentos por bucle. Antes usaba `fast`, que daba dieciséis
        // oscilaciones y se veía como un temblor, no como tocar.
        AvatarAction.TAP -> AvatarPose(
            armR = ArmPose.POINT_FWD, reach = 0.62f + 0.38f * tap,
            lean = 0.08f * tap, headTilt = 3f, expression = Expression.NEUTRAL
        )
        AvatarAction.SEARCH -> AvatarPose(
            armR = ArmPose.TO_TARGET, handTarget = BodyPart.FOREHEAD, reach = 0.95f,
            headTilt = 18f * w, lean = 0.16f * w, expression = Expression.SURPRISED, fx = Fx.QUESTION
        )

        AvatarAction.COLD -> AvatarPose(lean = 0.16f * shiver, bob = 0.1f * shiver, armL = ArmPose.HOLD_BOTH, armR = ArmPose.HOLD_BOTH, reach = 0.85f, crouch = 0.18f, expression = Expression.SAD, fx = Fx.COLD)
        AvatarAction.HOT -> AvatarPose(armR = ArmPose.WAVE, reach = 0.35f + 0.65f * w, headTilt = 8f * w, jaw = 0.3f, expression = Expression.SAD, fx = Fx.SWEAT)
        AvatarAction.FEVER -> AvatarPose(armR = ArmPose.TO_TARGET, handTarget = BodyPart.FOREHEAD, reach = 1f, expression = Expression.HURT, eyeClose = 0.45f, crouch = 0.1f, prop = Prop.THERMOMETER, fx = Fx.SWEAT)
        AvatarAction.SICK -> AvatarPose(armR = ArmPose.TO_TARGET, handTarget = BodyPart.BELLY, reach = 0.95f, expression = Expression.HURT, headTilt = 12f, crouch = 0.2f, lean = 0.1f * fast, highlight = BodyPart.BELLY, fx = Fx.SWEAT)
        AvatarAction.INJECTION -> AvatarPose(armR = ArmPose.TO_TARGET, handTarget = BodyPart.ARM, reach = 1f, expression = Expression.HURT, eyeClose = 0.5f, prop = Prop.SYRINGE, highlight = BodyPart.ARM)

        // Brazos arriba, no balanceándose: "feliz" tiene que verse distinto de "caminar".
        AvatarAction.HAPPY -> AvatarPose(bob = 0.2f + abs(w) * 0.8f, armL = ArmPose.WAVE, armR = ArmPose.WAVE, reach = 0.65f + 0.35f * up, expression = Expression.HAPPY, fx = Fx.SPARKLE)
        AvatarAction.LAUGH -> AvatarPose(bob = abs(w) * 0.8f, armR = ArmPose.TO_TARGET, handTarget = BodyPart.BELLY, reach = 0.7f, headTilt = -10f - 5f * up, expression = Expression.HAPPY, jaw = 0.45f + abs(w2) * 0.55f, fx = Fx.SPARKLE)
        AvatarAction.SAD -> AvatarPose(bob = -0.14f * up, headTilt = 12f, expression = Expression.SAD, tears = up * 0.5f, crouch = 0.28f)
        // Las dos manos a los ojos: una sola se confunde con "me duele el ojo".
        AvatarAction.CRY -> AvatarPose(headTilt = 10f, expression = Expression.SAD, tears = up, armL = ArmPose.TO_TARGET, armR = ArmPose.TO_TARGET, handTarget = BodyPart.EYE, reach = 0.72f, crouch = 0.3f, jaw = 0.3f)
        AvatarAction.ANGRY -> AvatarPose(
            armL = ArmPose.ON_HIP, armR = ArmPose.ON_HIP,
            expression = Expression.ANGRY, headTilt = 2f * w, bob = 0.05f * w,
            lean = 0.1f, fx = Fx.ANGER
        )
        AvatarAction.NERVOUS -> AvatarPose(
            armR = ArmPose.TO_TARGET, handTarget = BodyPart.HEAD, reach = 0.9f,
            expression = Expression.SAD, lean = 0.14f * shiver, bob = 0.07f * shiver,
            headTilt = 5f * shiver, fx = Fx.SWEAT
        )
        AvatarAction.SCARED -> AvatarPose(armL = ArmPose.PALM_OUT, armR = ArmPose.PALM_OUT, reach = 1f, expression = Expression.SURPRISED, lean = 0.14f * shiver, bob = 0.12f * shiver, crouch = 0.12f, jaw = 0.35f, fx = Fx.EXCLAIM)
        AvatarAction.BORED -> AvatarPose(armR = ArmPose.TO_TARGET, handTarget = BodyPart.CHEEK, reach = 1f, eyeClose = 0.5f, headTilt = 16f, crouch = 0.12f)
        AvatarAction.CALM -> AvatarPose(bob = 0.18f * w, eyeClose = 0.45f, expression = Expression.HAPPY, fx = Fx.SPARKLE)
        AvatarAction.SURPRISED -> AvatarPose(armR = ArmPose.TO_TARGET, handTarget = BodyPart.MOUTH, reach = 0.75f, expression = Expression.SURPRISED, jaw = 0.5f, bob = 0.12f * w, fx = Fx.EXCLAIM)

        // Vestirse. Las dos manos llevan la prenda hasta donde va puesta, y es
        // ese destino el que distingue una remera de un pantalón: la prenda
        // sola, quieta en el aire, se lee como "acá hay una remera" y no como
        // "me pongo la remera".
        AvatarAction.DRESS -> {
            val destino = part ?: BodyPart.CHEST
            val abajo = destino == BodyPart.FOOT || destino == BodyPart.LEG
            AvatarPose(
                armL = ArmPose.TO_TARGET, armR = ArmPose.TO_TARGET, handTarget = destino,
                // Tope por debajo de 0.8: con las dos manos al mismo punto, más
                // cerca del objetivo se superponen y se ve una sola.
                reach = 0.30f + 0.48f * up,
                crouch = if (abajo) 0.28f + 0.22f * up else 0f,
                headTilt = if (abajo) 15f else -5f,
                lean = if (abajo) 0.12f * up else 0f,
                expression = Expression.HAPPY,
                prop = Prop.SHIRT
            )
        }
        // El brazo se estira y se recoge hacia el pecho: el gesto de llamar.
        // La flecha que entra al cuerpo despeja la ambigüedad con "dar".
        AvatarAction.COME -> AvatarPose(
            armR = ArmPose.REACH_FWD, reach = 1f - 0.7f * up,
            lean = -0.12f * up, headTilt = 5f * w, bob = 0.08f * w,
            expression = Expression.HAPPY, fx = Fx.ARROW_IN
        )
        // Las dos palmas se abren hacia afuera: "ya está, no va más".
        AvatarAction.DONE -> AvatarPose(
            armL = ArmPose.PALM_OUT, armR = ArmPose.PALM_OUT,
            reach = 0.25f + 0.75f * up,
            lean = -0.08f * up, headTilt = -6f * up, bob = 0.12f * up,
            expression = Expression.HAPPY, fx = Fx.CHECK
        )

        // La mano se extiende con el dinero y el cuerpo la acompaña. La flecha
        // que sale lo separa de "quiero": acá el dinero se entrega, no se pide.
        AvatarAction.PAY -> AvatarPose(
            armR = ArmPose.REACH_FWD, reach = 0.40f + 0.60f * up,
            lean = 0.18f * up, headTilt = 4f * w, bob = 0.06f * w,
            expression = Expression.HAPPY,
            prop = Prop.MONEY, fx = Fx.ARROW_OUT
        )
        // Las dos manos juntas al frente, a la altura de la manija, caminando.
        AvatarAction.SHOP -> AvatarPose(
            stance = Stance.WALK, legSwing = w,
            armL = ArmPose.HOLD_BOTH, armR = ArmPose.HOLD_BOTH, reach = 0.92f,
            bob = abs(w) * 0.35f, lean = 0.14f,
            expression = Expression.HAPPY, prop = Prop.CART
        )
        // Un brazo balancea y el otro cuelga con el peso: así se lee que lleva
        // algo y no que va caminando con las manos vacías.
        AvatarAction.CARRY -> AvatarPose(
            stance = Stance.WALK, legSwing = w, armSwing = -w,
            armL = ArmPose.SWING, armR = ArmPose.DOWN,
            bob = abs(w) * 0.45f, lean = 0.05f * w,
            expression = Expression.HAPPY, prop = Prop.BAG
        )

        // En el piso, las dos manos abajo entre los juguetes. El tope de 0.68
        // es el de siempre: con las dos manos al mismo punto, más cerca se
        // superponen y se ve una sola.
        AvatarAction.TOYS -> AvatarPose(
            crouch = 0.72f,
            armL = ArmPose.TO_TARGET, armR = ArmPose.TO_TARGET, handTarget = BodyPart.FOOT,
            reach = 0.50f + 0.18f * up,
            headTilt = 14f, bob = 0.05f * w,
            expression = Expression.HAPPY, prop = Prop.TOYS, fx = Fx.SPARKLE
        )
        // Una sola mano, que baja y sube: es lo que convierte una pila de cubos
        // en la acción de apilarlos.
        AvatarAction.BLOCKS -> AvatarPose(
            crouch = 0.58f,
            armR = ArmPose.TO_TARGET, handTarget = BodyPart.BELLY,
            reach = 0.40f + 0.50f * up,
            headTilt = 12f, expression = Expression.HAPPY, prop = Prop.BLOCKS
        )
        // Acunar: el cuerpo se mece de lado a lado y los ojos se entrecierran.
        AvatarAction.CRADLE -> AvatarPose(
            armL = ArmPose.HOLD_BOTH, armR = ArmPose.HOLD_BOTH, reach = 0.55f,
            lean = 0.16f * w, headTilt = 10f * w, bob = 0.08f * w, eyeClose = 0.35f,
            expression = Expression.HAPPY, prop = Prop.DOLL, fx = Fx.HEART
        )
        // Hamaca: las manos arriba en las sogas y el cuerpo que va y viene.
        AvatarAction.SWING -> AvatarPose(
            stance = Stance.SIT, crouch = 0.85f,
            armL = ArmPose.WAVE, armR = ArmPose.WAVE, reach = 0.5f,
            lean = 0.34f * w, headTilt = -12f * w, bob = 0.14f * abs(w),
            expression = Expression.HAPPY, prop = Prop.SWING, fx = Fx.SPARKLE
        )
        // Tobogán: sentado, echado hacia atrás y con las manos en alto.
        AvatarAction.SLIDE -> AvatarPose(
            stance = Stance.SIT, crouch = 0.9f,
            armL = ArmPose.WAVE, armR = ArmPose.WAVE, reach = 0.75f + 0.25f * up,
            lean = -0.24f, headTilt = -8f, jaw = 0.35f + 0.2f * up,
            expression = Expression.HAPPY, prop = Prop.SLIDE, fx = Fx.SPEED
        )

        // Mostrar: el objeto sube a la altura de la cara y la cabeza se inclina
        // hacia él. Nombrar un lápiz y usarlo son dos cosas distintas, y el
        // chico que toca "lápiz" está nombrando.
        AvatarAction.SHOW -> AvatarPose(
            armR = ArmPose.WAVE, reach = 0.55f + 0.25f * up,
            headTilt = -9f - 5f * up, lean = -0.06f, bob = 0.07f * w,
            expression = Expression.HAPPY, fx = Fx.TARGET
        )
        // Escribir es sentado a la mesa. Dibujar, de pie. La postura es lo que
        // las separa, porque el gesto de la mano es casi el mismo.
        AvatarAction.WRITE -> AvatarPose(
            stance = Stance.SIT, crouch = 1f,
            armR = ArmPose.TO_TARGET, handTarget = BodyPart.BELLY,
            reach = 0.78f + 0.14f * sin(t * 6f * tau),
            headTilt = 17f, eyeClose = 0.3f,
            expression = Expression.NEUTRAL, prop = Prop.DESK
        )

        // Ahora / hoy: las agujas quietas y el dedo firme hacia abajo.
        AvatarAction.NOW -> AvatarPose(
            armR = ArmPose.POINT_FWD, reach = 1f,
            lean = 0.14f, headTilt = 6f, bob = 0.09f * w,
            expression = Expression.NEUTRAL,
            prop = Prop.CLOCK, fx = Fx.EXCLAIM
        )
        // Después / mañana: las agujas dan la vuelta hacia adelante y el brazo
        // se extiende. `propPhase = t` es un diente de sierra, y por eso esta
        // acción es procedural: el salto del final es invisible en una aguja
        // que da la vuelta entera, pero interpolarlo la haría girar al revés.
        AvatarAction.LATER -> AvatarPose(
            armR = ArmPose.REACH_FWD, reach = 0.5f + 0.5f * up,
            lean = 0.12f * up, headTilt = -4f,
            expression = Expression.HAPPY,
            prop = Prop.CLOCK, propPhase = t, fx = Fx.ARROW_OUT
        )
        // Ayer: las agujas van al revés y el cuerpo se echa atrás.
        AvatarAction.BEFORE -> AvatarPose(
            armR = ArmPose.REACH_FWD, reach = 1f - 0.5f * up,
            lean = -0.14f * up, headTilt = 9f,
            expression = Expression.NEUTRAL,
            prop = Prop.CLOCK, propPhase = 1f - t, fx = Fx.ARROW_IN
        )

        // Día: sol y brazos arriba. Noche: luna, ojos que se cierran y bostezo.
        // Que se distingan entre sí importa más que el detalle de cada una.
        AvatarAction.DAY -> AvatarPose(
            armL = ArmPose.WAVE, armR = ArmPose.WAVE, reach = 0.7f + 0.3f * up,
            bob = 0.18f + 0.22f * abs(w), headTilt = -6f,
            expression = Expression.HAPPY, prop = Prop.SUN, fx = Fx.SPARKLE
        )
        AvatarAction.NIGHT -> AvatarPose(
            eyeClose = 0.55f + 0.25f * up, headTilt = 12f,
            bob = 0.1f * w, crouch = 0.1f, jaw = 0.3f * up,
            expression = Expression.NEUTRAL, prop = Prop.MOON
        )
        // Lluvia: las manos se ponen sobre la cabeza y el cuerpo se encoge.
        AvatarAction.RAIN -> AvatarPose(
            armL = ArmPose.TO_TARGET, armR = ArmPose.TO_TARGET, handTarget = BodyPart.HEAD,
            reach = 0.66f, crouch = 0.26f, headTilt = 8f,
            lean = 0.06f * w, expression = Expression.SAD, prop = Prop.RAIN
        )
        // Nublado: mira el cielo, sin taparse. Es lo que lo separa de la lluvia.
        AvatarAction.CLOUDY -> AvatarPose(
            headTilt = -16f, bob = 0.1f * w, lean = -0.08f,
            armR = ArmPose.POINT_FWD, reach = 0.35f,
            expression = Expression.NEUTRAL, prop = Prop.CLOUDY
        )

        // La otra persona se dibuja al costado y el avatar la saluda, inclinado
        // hacia ella. El corazón es lo que separa "estoy con mamá" de "hola".
        AvatarAction.WITH -> AvatarPose(
            armR = ArmPose.WAVE, reach = 0.45f + 0.55f * up,
            headTilt = 7f * w, lean = 0.10f, bob = 0.10f * abs(w),
            expression = Expression.HAPPY, prop = Prop.MOM, fx = Fx.HEART
        )
        // Caminar con el lugar detrás. Es "vamos a casa", no "caminar".
        AvatarAction.GO_TO -> AvatarPose(
            stance = Stance.WALK, legSwing = w, armSwing = -w,
            armL = ArmPose.SWING, armR = ArmPose.SWING,
            bob = abs(w) * 0.5f, lean = 0.07f * w,
            expression = Expression.HAPPY, prop = Prop.HOUSE, fx = Fx.ARROW_OUT
        )
    }

    return base.copy(prop = r.prop ?: base.prop, fxPulse = pulse)
}
