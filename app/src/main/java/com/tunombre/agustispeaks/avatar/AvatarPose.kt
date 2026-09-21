package com.tunombre.agustispeaks.avatar

/**
 * Pose del avatar de cuerpo completo en un instante. Todo relativo.
 * La interpretan `AvatarFigureRenderer` (persona y robot).
 */
data class AvatarPose(
    val bob: Float = 0f,            // rebote vertical (-1..1)
    val lean: Float = 0f,          // inclinación del cuerpo (-1..1)
    val headTilt: Float = 0f,      // grados
    val jaw: Float = 0f,           // 0..1 boca abierta (hablar/masticar)
    val expression: Expression? = null,
    val tears: Float = 0f,         // 0..1
    val sleeping: Boolean = false,
    val eyeClose: Float = 0f,      // 0..1

    val stance: Stance = Stance.STAND,
    val crouch: Float = 0f,        // 0 de pie .. 1 agachado/sentado
    val legSwing: Float = 0f,      // -1..1 (caminar/correr)

    val armL: ArmPose = ArmPose.DOWN,
    val armR: ArmPose = ArmPose.DOWN,
    val armSwing: Float = 0f,      // -1..1 balanceo natural de brazos
    val reach: Float = 0f,         // 0..1 extensión del brazo activo
    val handTarget: BodyPart? = null,

    val prop: Prop? = null,
    /**
     * Fase 0..1 de la animación del objeto, independiente del brazo. La usan la
     * puerta (cuánto está abierta) y la bocina (las ondas que se expanden).
     */
    val propPhase: Float = 0f,

    /** Símbolo de apoyo que explica la acción (flecha, corazón, ✓, ✗…). */
    val fx: Fx = Fx.NONE,
    /** 0..1 animación del símbolo (aparece/late). */
    val fxPulse: Float = 1f,
    /** Parte del cuerpo resaltada con un halo (dolor, señalar). */
    val highlight: BodyPart? = null
)

enum class Stance { STAND, WALK, RUN, SIT, JUMP, LIE }

enum class ArmPose {
    DOWN, SWING, WAVE, REACH_FWD, TO_TARGET, ON_HIP, PALM_OUT, THUMB_UP, THUMB_DOWN, HOLD_BOTH, RUB,
    POINT_FWD,  // señala hacia adelante (con dedo)
    POINT_AT,   // señala hacia handTarget sin tocarlo (con dedo)
    FINGER_UP,  // puño cerrado con el índice hacia arriba: "esperá un momento"
    CUP_EAR     // mano ahuecada al costado de la oreja: escuchar
}

enum class Expression { HAPPY, SAD, ANGRY, HURT, SURPRISED, NEUTRAL }

enum class Prop {
    TOOTHBRUSH, SHOWER, SOAP, COMB, CUP, SPOON, BALL, BOOK, PENCIL, BED, THERMOMETER, SYRINGE, BANDAGE, NOTE,
    /** Lavabo con canilla y agua cayendo. Es el escenario de lavarse las manos. */
    SINK,
    WATER, MILK, JUICE, TEA, COCOA,
    /** Comida genérica, cuando la frase no nombra un alimento concreto. */
    FOOD,
    /** Alimentos con forma propia: si pide sopa, tiene que verse una sopa. */
    BREAD, APPLE, BANANA, COOKIE, SOUP, NOODLES, RICE, CHICKEN, MEAT, EGG, YOGURT, CAKE, ICE_CREAM, CHEESE, PIZZA,
    /** Bocina con ondas de sonido, para escuchar. */
    HORN,
    /** Prendas: la silueta de cada una dice a qué parte del cuerpo va. */
    SHIRT, JACKET, PAJAMAS, PANTS, SHOES, SOCKS, HAT,
    /** Compras y escuela. */
    MONEY, CART, BAG, BASKET, BACKPACK, TABLE,
    /**
     * Objetos que se muestran en la mano, sin usarlos. [PENCIL] en cambio es el
     * equipo de dibujar: hoja apoyada y lápiz trazando.
     */
    NOTEBOOK, PENCIL_HELD,
    /** Escritorio armado: mesa, cuaderno apoyado y lápiz escribiendo. */
    DESK,
    /** Reloj junto a la cabeza. `propPhase` es la vuelta de las agujas. */
    CLOCK,
    TOWEL,
    /**
     * Otra persona, al lado del avatar. Cada una cambia en altura, pelo y ropa;
     * esas tres cosas son las que dejan distinguir a un abuelo de un hermano
     * sin escribir un segundo renderizador completo.
     */
    MOM, DAD, SISTER, BROTHER, GRANDMA, GRANDPA, AUNT, UNCLE, TEACHER, FRIEND,
    /** El bebé va en brazos, no parado al lado. */
    BABY,
    /** Juego. */
    TOYS, BLOCKS, DOLL,
    /** Escenario: se dibuja DETRÁS de la figura, antes que las piernas. */
    CHAIR, TOILET, DOOR, SWING, SLIDE,
    /** Cielo: también va detrás, ocupando la parte de arriba del lienzo. */
    SUN, MOON, RAIN, CLOUDY,
    /** Lugares: la silueta del edificio, detrás de la figura que camina. */
    HOUSE, SCHOOL, PARK, HOSPITAL, SHOP, KITCHEN, CAR
}

/** Objetos que van detrás del cuerpo en vez de en la mano. */
internal fun Prop.isBackdrop(): Boolean = when (this) {
    Prop.CHAIR, Prop.TOILET, Prop.DOOR, Prop.SWING, Prop.SLIDE,
    Prop.SUN, Prop.MOON, Prop.RAIN, Prop.CLOUDY,
    Prop.HOUSE, Prop.SCHOOL, Prop.PARK, Prop.HOSPITAL,
    Prop.SHOP, Prop.KITCHEN, Prop.CAR -> true
    else -> false
}

/**
 * Símbolo grande que acompaña a la figura para que la acción se entienda de un
 * vistazo, sin depender solo de la postura. Se dibuja en una "chapa" blanca
 * junto a la cabeza (ver `AvatarFx.kt`).
 */
enum class Fx {
    NONE,
    SPEECH,        // globo de diálogo con puntos: hablar
    MUSIC,         // notas: cantar
    SPEED,         // líneas de velocidad: caminar / correr
    JUMP_ARC,      // arco punteado: saltar
    PLUS,          // "+" grande: más
    MINUS,         // "−" grande: menos
    ARROW_OUT,     // flecha que sale del cuerpo: dar / abrir / querer
    ARROW_IN,      // flecha que entra al cuerpo: tomar / cerrar
    HEART,         // abrazo, beso, gracias
    PAIN,          // estallido rojo: dolor
    SWEAT,         // gotas: calor, nervios, miedo
    COLD,          // copo azul: frío
    QUESTION,      // "?": buscar
    EXCLAIM,       // "!": sorpresa, ayuda
    ANGER,         // cruz de enojo
    CHECK,         // "✓" verde: sí
    CROSS,         // "✗" rojo: no
    STOP_SIGN,     // octógono rojo: basta
    CLOCK,         // reloj: esperar
    SPARKLE,       // destellos: feliz, calma, risa
    BUBBLES,       // burbujas de jabón: lavarse, bañarse
    TARGET         // mira / círculo: mirar, señalar
}
