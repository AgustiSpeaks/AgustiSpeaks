package com.tunombre.agustispeaks.avatar

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import com.tunombre.agustispeaks.data.model.AvatarConfig
import com.tunombre.agustispeaks.data.model.AvatarKind
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

private val PANEL = Color(0xFF23232E)
private val LED_A = Color(0xFF8CE0FF)
private val LED_B = Color(0xFF63D9A5)

/**
 * Avatar de cuerpo completo ejecutando una acción (persona o robot), con el
 * objeto correspondiente (cepillo, ducha, jabón, taza, pelota, libro…) y el
 * símbolo que explica la acción (flecha, corazón, "?", ✓/✗…).
 *
 * El robot se dibuja con extremidades de acero, articulaciones visibles y
 * dedos de acero; la ropa, el pantalón y los zapatos usan los colores
 * elegidos en ambos tipos de avatar.
 */
fun DrawScope.drawAvatarFigure(config: AvatarConfig, pose: AvatarPose) {
    if (pose.stance == Stance.LIE) {
        drawLying(config, pose)
        return
    }
    val robot = config.kind == AvatarKind.ROBOT
    val W = size.width
    val H = size.height
    val cx = W / 2f + pose.lean * W * 0.05f
    val dy = -pose.bob * H * 0.04f

    /* Sombra en el piso: queda quieta aunque el avatar rebote o salte. */
    run {
        val lift = pose.bob.coerceIn(0f, 1f)
        val sw2 = W * 0.30f * (1f - 0.35f * lift)
        drawOval(
            Color.Black.copy(alpha = 0.13f - 0.06f * lift),
            Offset(cx - sw2 / 2f, H * 0.94f),
            Size(sw2, H * 0.028f)
        )
    }

    translate(top = dy) {
        val hip = H * (0.56f + pose.crouch * 0.10f)
        val shoulder = H * (0.30f + pose.crouch * 0.07f)
        val headC = Offset(cx, H * (0.17f + pose.crouch * 0.05f))
        val k = H * 0.075f
        val ground = H * 0.95f
        val sw = W * 0.11f
        val hw = W * 0.07f
        val lt = H * 0.05f

        val skin = avColor(config.skinColor)
        val cloth = avColor(config.clothingColor)
        val pants = avColor(config.pantsColor)
        val shoe = avColor(config.shoeColor)
        val trim = avColor(config.robotColor)
        val edge = lt * 0.24f

        fun part(p: BodyPart): Offset = when (p) {
            BodyPart.HEAD, BodyPart.HAIR -> Offset(cx, headC.y - k)
            BodyPart.FOREHEAD -> Offset(cx, headC.y - k * 0.6f)
            BodyPart.EYE -> Offset(cx - k * 0.4f, headC.y - k * 0.05f)
            BodyPart.NOSE -> Offset(cx, headC.y + k * 0.15f)
            BodyPart.CHEEK -> Offset(cx + k * 0.6f, headC.y + k * 0.1f)
            BodyPart.EAR -> Offset(cx + k, headC.y + k * 0.1f)
            BodyPart.MOUTH, BodyPart.TOOTH -> Offset(cx, headC.y + k * 0.5f)
            BodyPart.THROAT -> Offset(cx, shoulder - H * 0.02f)
            BodyPart.CHEST -> Offset(cx, shoulder + H * 0.06f)
            BodyPart.BELLY -> Offset(cx, hip - H * 0.04f)
            BodyPart.SHOULDER -> Offset(cx + sw, shoulder)
            BodyPart.ARM -> Offset(cx + sw + W * 0.02f, shoulder + H * 0.10f)
            BodyPart.HAND -> Offset(cx + W * 0.02f, hip + H * 0.03f)
            BodyPart.LEG -> Offset(cx + hw, (hip + ground) / 2f)
            BodyPart.FOOT -> Offset(cx + hw, ground - H * 0.02f)
            BodyPart.BACK -> Offset(cx - k * 0.2f, shoulder + H * 0.08f)
        }

        /* -------- escenario: va detrás de todo el cuerpo -------- */
        pose.prop?.takeIf { it.isBackdrop() }
            ?.let { drawBackdrop(it, pose.propPhase, cx, W, H, shoulder, hip, ground, edge) }

        /* -------- piernas -------- */
        val hipL = Offset(cx - hw, hip)
        val hipR = Offset(cx + hw, hip)
        val bendY = pose.crouch * H * 0.04f
        val legPts: List<Offset> = when (pose.stance) {
            Stance.SIT -> listOf(
                Offset(cx - hw - W * 0.03f, hip + H * 0.01f), Offset(cx - hw + W * 0.03f, hip + H * 0.20f),
                Offset(cx + hw + W * 0.03f, hip + H * 0.01f), Offset(cx + hw - W * 0.03f, hip + H * 0.20f)
            )
            // Marcha DE FRENTE, no de perfil: una rodilla sube y el pie se
            // despega, la otra pierna queda plantada. De frente el paso se lee
            // por la altura del pie, no por cuánto se separan las piernas.
            Stance.WALK, Stance.RUN -> {
                val amp = if (pose.stance == Stance.RUN) 1f else 0.6f
                val liftL = pose.legSwing.coerceAtLeast(0f) * amp
                val liftR = (-pose.legSwing).coerceAtLeast(0f) * amp
                listOf(
                    Offset(cx - hw - W * 0.02f * liftL, (hip + ground) / 2f - H * 0.055f * liftL),
                    Offset(cx - hw - W * 0.035f * liftL, ground - H * 0.13f * liftL),
                    Offset(cx + hw + W * 0.02f * liftR, (hip + ground) / 2f - H * 0.055f * liftR),
                    Offset(cx + hw + W * 0.035f * liftR, ground - H * 0.13f * liftR)
                )
            }
            else -> listOf(
                Offset(cx - hw, (hip + ground) / 2f - bendY), Offset(cx - hw, ground - bendY),
                Offset(cx + hw, (hip + ground) / 2f - bendY), Offset(cx + hw, ground - bendY)
            )
        }
        val (kneeL, footL, kneeR, footR) = legPts

        /** Muslo con placa del color del pantalón; pantorrilla de acero en el robot. */
        fun legSeg(a: Offset, b: Offset, upper: Boolean) {
            if (robot) {
                limb(a, b, STEEL_D, lt)
                limb(a, b, if (upper) pants else STEEL, lt * 0.66f)
                drawCircle(JOINT_C, lt * 0.48f, a)
                drawCircle(JOINT_C, lt * 0.48f, b)
            } else {
                outlinedLimb(a, b, pants, lt)
            }
        }
        legSeg(hipL, kneeL, true); legSeg(kneeL, footL, false)
        legSeg(hipR, kneeR, true); legSeg(kneeR, footR, false)

        listOf(footL, footR).forEach { f ->
            if (robot) {
                drawRoundRect(shoe, Offset(f.x - lt * 0.95f, f.y - lt * 0.5f), Size(lt * 2.1f, lt * 1.05f), CornerRadius(lt * 0.25f))
                drawRoundRect(STEEL_D, Offset(f.x - lt * 0.95f, f.y + lt * 0.35f), Size(lt * 2.1f, lt * 0.24f), CornerRadius(lt * 0.1f))
                drawRoundRect(OUTLINE, Offset(f.x - lt * 0.95f, f.y - lt * 0.5f), Size(lt * 2.1f, lt * 1.05f), CornerRadius(lt * 0.25f), style = Stroke(edge))
            } else {
                drawOval(shoe, Offset(f.x - lt, f.y - lt * 0.4f), Size(lt * 2.2f, lt))
                drawOval(OUTLINE, Offset(f.x - lt, f.y - lt * 0.4f), Size(lt * 2.2f, lt), style = Stroke(edge))
            }
        }

        /* -------- brazos -------- */
        fun armHand(side: Int, ap: ArmPose): Offset {
            val sh = Offset(cx + side * sw, shoulder)
            val down = Offset(sh.x + side * W * 0.02f, hip + H * 0.02f)
            return when (ap) {
                ArmPose.DOWN -> down
                ArmPose.SWING -> {
                    val a = (if (side < 0) pose.armSwing else -pose.armSwing)
                    Offset(down.x + a * W * 0.11f, down.y - abs(a) * H * 0.02f)
                }
                ArmPose.WAVE -> Offset(sh.x + side * W * 0.07f + (pose.reach - 0.5f) * W * 0.06f, sh.y - H * 0.15f)
                ArmPose.REACH_FWD -> lerp(down, Offset(cx + side * W * 0.05f, shoulder + H * 0.06f), pose.reach.coerceIn(0f, 1f))
                ArmPose.POINT_FWD -> lerp(down, Offset(cx + side * W * 0.08f, shoulder + H * 0.07f), pose.reach.coerceIn(0f, 1f))
                ArmPose.TO_TARGET -> lerp(down, part(pose.handTarget ?: BodyPart.CHEST), pose.reach.coerceIn(0f, 1f))
                ArmPose.POINT_AT -> lerp(down, part(pose.handTarget ?: BodyPart.HEAD), (pose.reach * 0.6f).coerceIn(0f, 1f))
                ArmPose.ON_HIP -> Offset(cx + side * hw * 1.3f, hip - H * 0.01f)
                ArmPose.PALM_OUT -> Offset(sh.x + side * W * 0.03f, shoulder + H * 0.02f - pose.reach * H * 0.03f)
                ArmPose.THUMB_UP, ArmPose.THUMB_DOWN -> Offset(cx + side * W * 0.06f, shoulder + H * 0.11f)
                // La separación depende de reach: así "más" junta las manos y
                // "menos" las abre, y el abrazo cierra de verdad por delante.
                ArmPose.HOLD_BOTH -> Offset(
                    cx + side * W * (0.105f - 0.065f * pose.reach.coerceIn(0f, 1f)),
                    shoulder + H * 0.13f + (1f - pose.reach) * H * 0.04f
                )
                // Frotarse las manos: van POR DEBAJO de la cadera, fuera de la
                // silueta del torso, y `reach` las junta y las separa en vez de
                // correrlas a las dos para el mismo lado. El desnivel cruzado
                // hace que una pase por encima de la otra.
                ArmPose.RUB -> {
                    val r = pose.reach.coerceIn(0f, 1f)
                    Offset(
                        cx + side * W * (0.014f + 0.048f * r),
                        hip + H * 0.045f + side * H * 0.016f * (r - 0.5f)
                    )
                }
                ArmPose.FINGER_UP -> Offset(cx + side * W * 0.085f, shoulder + H * 0.01f - pose.reach * H * 0.03f)
                ArmPose.CUP_EAR -> Offset(cx + side * k * 1.35f, headC.y + k * 0.08f)
            }
        }

        /** Prolonga el antebrazo. Es la dirección de reserva cuando la mano ya llegó. */
        fun alongForearm(side: Int, hand: Offset): Offset {
            val sh = Offset(cx + side * sw, shoulder)
            return Offset(hand.x + (hand.x - sh.x), hand.y + (hand.y - sh.y))
        }

        /** Hacia dónde "mira" la mano: define en qué dirección salen los dedos. */
        fun handToward(side: Int, ap: ArmPose, hand: Offset): Offset = when (ap) {
            ArmPose.POINT_FWD -> Offset(hand.x + side * W * 0.12f, hand.y + H * 0.045f)
            ArmPose.POINT_AT -> part(pose.handTarget ?: BodyPart.HEAD)
            ArmPose.TO_TARGET -> {
                val tgt = part(pose.handTarget ?: BodyPart.CHEST)
                // Con reach = 1 la mano coincide con el objetivo y no habría
                // dirección: ahí los dedos siguen la línea del antebrazo.
                if ((tgt - hand).getDistance() > lt * 0.6f) tgt else alongForearm(side, hand)
            }
            // Palma de frente: los dedos van HACIA ARRIBA, no de costado. Es la
            // diferencia entre la señal de alto y una mano de perfil saludando.
            ArmPose.PALM_OUT -> Offset(hand.x + side * W * 0.02f, hand.y - H * 0.14f)
            ArmPose.THUMB_UP -> Offset(hand.x, hand.y - H * 0.12f)
            ArmPose.THUMB_DOWN -> Offset(hand.x, hand.y + H * 0.12f)
            ArmPose.WAVE -> Offset(hand.x + side * W * 0.03f, hand.y - H * 0.09f)
            // Brazo extendido al frente: la palma se ve de lleno y los dedos
            // suben. Con la figura de frente, unos dedos horizontales se leen
            // como una mano de canto.
            ArmPose.REACH_FWD -> Offset(hand.x + side * W * 0.05f, hand.y - H * 0.12f)
            ArmPose.HOLD_BOTH, ArmPose.RUB -> Offset(cx, hand.y - H * 0.015f)
            ArmPose.ON_HIP -> Offset(cx, hand.y)
            ArmPose.FINGER_UP -> Offset(hand.x, hand.y - H * 0.16f)
            // Mano ahuecada: los dedos se curvan hacia adelante y arriba.
            ArmPose.CUP_EAR -> Offset(hand.x + side * W * 0.035f, hand.y - H * 0.06f)
            else -> Offset(hand.x, hand.y + H * 0.09f)
        }

        /** Gesto de la mano que corresponde a cada pose de brazo. */
        fun handShape(ap: ArmPose): HandShape = when (ap) {
            // Puño con el índice levantado: el gesto de "esperá un momento".
            ArmPose.POINT_FWD, ArmPose.POINT_AT, ArmPose.FINGER_UP -> HandShape.POINT
            ArmPose.THUMB_UP -> HandShape.THUMB_UP
            ArmPose.THUMB_DOWN -> HandShape.THUMB_DOWN
            ArmPose.PALM_OUT, ArmPose.WAVE, ArmPose.REACH_FWD -> HandShape.OPEN
            ArmPose.HOLD_BOTH, ArmPose.RUB, ArmPose.CUP_EAR -> HandShape.GRIP
            // Con objeto en la mano se cierra; sin objeto es una mano apoyada.
            ArmPose.TO_TARGET -> if (pose.prop != null) HandShape.GRIP else HandShape.OPEN
            else -> HandShape.RELAXED
        }

        /**
         * Codo. No está sobre la recta hombro-mano: se separa hacia afuera y
         * abajo cuanto más plegado está el brazo. Sin esto el brazo solo se
         * acorta y estira, y gestos como tocar o llevarse la mano a la boca se
         * ven rígidos.
         */
        fun elbow(sh: Offset, hand: Offset, side: Int): Offset {
            val straight = lerp(sh, hand, 0.45f)
            val bend = (1f - (hand - sh).getDistance() / (H * 0.32f)).coerceIn(0f, 1f)
            return Offset(straight.x + side * W * 0.06f * bend, straight.y + H * 0.038f * bend)
        }

        fun drawArm(side: Int, ap: ArmPose): Offset {
            val sh = Offset(cx + side * sw, shoulder)
            val hand = armHand(side, ap)
            val mid = elbow(sh, hand, side)
            val toward = handToward(side, ap, hand)

            if (robot) {
                limb(sh, mid, OUTLINE, lt * 1.3f)
                limb(sh, mid, STEEL_D, lt)
                limb(sh, mid, cloth, lt * 0.66f)
                limb(mid, hand, OUTLINE, lt * 1.1f)
                limb(mid, hand, STEEL_D, lt * 0.85f)
                limb(mid, hand, STEEL, lt * 0.55f)
                drawCircle(trim, lt * 0.55f, sh)
                drawCircle(JOINT_C, lt * 0.45f, mid)
                drawHand(handShape(ap), hand, toward, lt, side, skin, edge, metal = true)
                return hand
            }

            outlinedLimb(sh, mid, cloth, lt)
            outlinedLimb(mid, hand, skin, lt * 0.85f)
            drawHand(handShape(ap), hand, toward, lt, side, skin, edge, metal = false)
            return hand
        }

        /**
         * El brazo izquierdo normalmente va DETRÁS del torso: da profundidad y
         * es lo correcto para un brazo que cuelga al costado. Pero un brazo que
         * trabaja delante del cuerpo desaparecería debajo del tronco, así que
         * esas poses se posponen hasta después de dibujarlo.
         */
        fun trabajaAdelante(ap: ArmPose) = ap == ArmPose.RUB

        if (!trabajaAdelante(pose.armL)) drawArm(-1, pose.armL)

        /* -------- torso -------- */
        if (robot) {
            drawRoundRect(
                cloth,
                Offset(cx - sw, shoulder - H * 0.005f),
                Size(sw * 2f, hip - shoulder + H * 0.012f),
                CornerRadius(k * 0.34f)
            )
            listOf(-1f, 1f).forEach { s ->
                drawRoundRect(
                    trim,
                    Offset(cx + s * sw - k * 0.44f, shoulder - k * 0.14f),
                    Size(k * 0.88f, k * 0.54f),
                    CornerRadius(k * 0.24f)
                )
            }
            val panel = Offset(cx - k * 0.52f, shoulder + H * 0.035f)
            drawRoundRect(PANEL, panel, Size(k * 1.04f, k * 0.76f), CornerRadius(k * 0.12f))
            drawCircle(LED_A, k * 0.13f, Offset(panel.x + k * 0.3f, panel.y + k * 0.38f))
            drawCircle(LED_B, k * 0.13f, Offset(panel.x + k * 0.74f, panel.y + k * 0.38f))
            drawRoundRect(STEEL_D, Offset(cx - hw * 1.55f, hip - H * 0.025f), Size(hw * 3.1f, H * 0.035f), CornerRadius(k * 0.16f))
            drawRoundRect(
                OUTLINE,
                Offset(cx - sw, shoulder - H * 0.005f),
                Size(sw * 2f, hip - shoulder + H * 0.012f),
                CornerRadius(k * 0.34f),
                style = Stroke(edge)
            )
            limb(Offset(cx, shoulder), Offset(cx, headC.y + k * 0.9f), STEEL_D, lt * 0.95f)
            limb(Offset(cx, shoulder), Offset(cx, headC.y + k * 0.9f), STEEL, lt * 0.55f)
        } else {
            outlinedLimb(Offset(cx, shoulder), Offset(cx, headC.y + k * 0.9f), skin, lt * 0.9f)
            val torso = Path().apply {
                moveTo(cx - sw, shoulder)
                lineTo(cx + sw, shoulder)
                lineTo(cx + hw * 1.4f, hip)
                lineTo(cx - hw * 1.4f, hip)
                close()
            }
            drawPath(torso, cloth)
            drawPath(torso, OUTLINE, style = Stroke(edge))
        }

        /* -------- cabeza -------- */
        rotate(degrees = pose.headTilt, pivot = headC) {
            if (robot) robotHead(config, pose, headC, k) else personHead(config, pose, headC, k)
        }

        /* -------- brazos de delante + objeto -------- */
        if (trabajaAdelante(pose.armL)) drawArm(-1, pose.armL)
        val rHand = drawArm(1, pose.armR)

        pose.prop?.let {
            drawProp(it, pose, rHand, headC, k, cx, W, H, shoulder, hip, ground, part(pose.handTarget ?: BodyPart.ARM))
        }

        /* -------- halo de la parte nombrada y símbolo de la acción -------- */
        pose.highlight?.let { drawHighlight(part(it), k, pose.fx == Fx.PAIN, pose.fxPulse) }
        drawFx(pose.fx, pose.fxPulse, cx, headC, k, shoulder, hip, ground)
    }
}

private fun DrawScope.drawProp(
    prop: Prop,
    pose: AvatarPose,
    hand: Offset,
    headC: Offset,
    k: Float,
    cx: Float,
    W: Float,
    H: Float,
    shoulder: Float,
    hip: Float,
    ground: Float,
    target: Offset
) {
    val mouth = Offset(cx, headC.y + k * 0.5f)
    when (prop) {
        Prop.FOOD, Prop.BREAD, Prop.APPLE, Prop.BANANA, Prop.COOKIE, Prop.SOUP, Prop.NOODLES,
        Prop.RICE, Prop.CHICKEN, Prop.MEAT, Prop.EGG, Prop.YOGURT, Prop.CAKE, Prop.ICE_CREAM,
        Prop.CHEESE, Prop.PIZZA -> drawFood(prop, hand, k)
        Prop.SHIRT, Prop.JACKET, Prop.PAJAMAS, Prop.PANTS,
        Prop.SHOES, Prop.SOCKS, Prop.HAT -> drawGarment(prop, hand, k)

        Prop.MONEY -> {
            // Tres billetes escalonados: uno solo se lee como un papel.
            repeat(3) { i ->
                val d = k * 0.09f * i
                val esquina = Offset(hand.x - k * 0.34f + d, hand.y - k * 0.22f - d * 0.5f)
                val tam = Size(k * 0.72f, k * 0.40f)
                drawRoundRect(Color(0xFF81C784), esquina, tam, CornerRadius(k * 0.05f))
                drawRoundRect(OUTLINE, esquina, tam, CornerRadius(k * 0.05f), style = Stroke(k * 0.06f))
            }
            drawCircle(Color(0xFFFFF59D), k * 0.11f, Offset(hand.x + k * 0.06f, hand.y - k * 0.10f))
            drawCircle(OUTLINE, k * 0.11f, Offset(hand.x + k * 0.06f, hand.y - k * 0.10f), style = Stroke(k * 0.05f))
        }

        Prop.CART -> {
            val cc = cx + W * 0.21f
            val techo = hip - H * 0.05f
            val alto = H * 0.11f
            val esquina = Offset(cc - k * 0.80f, techo)
            val tam = Size(k * 1.60f, alto)
            drawRoundRect(Color(0xFFB0BEC5), esquina, tam, CornerRadius(k * 0.10f))
            // Rejilla: sin ella el carrito es una caja.
            repeat(3) { i ->
                val x = cc - k * 0.40f + k * 0.40f * i
                drawLine(OUTLINE, Offset(x, techo + k * 0.06f), Offset(x, techo + alto - k * 0.06f), k * 0.05f)
            }
            drawRoundRect(OUTLINE, esquina, tam, CornerRadius(k * 0.10f), style = Stroke(k * 0.08f))
            drawLine(Color(0xFF607D8B), Offset(cc - k * 0.80f, techo), hand, k * 0.14f, cap = StrokeCap.Round)
            drawCircle(OUTLINE, k * 0.17f, Offset(cc - k * 0.45f, techo + alto + k * 0.17f))
            drawCircle(OUTLINE, k * 0.17f, Offset(cc + k * 0.45f, techo + alto + k * 0.17f))
        }

        Prop.BAG -> {
            val techo = hand.y + k * 0.24f
            drawArc(
                OUTLINE, 200f, 140f, false,
                Offset(hand.x - k * 0.26f, techo - k * 0.24f), Size(k * 0.52f, k * 0.44f),
                style = Stroke(k * 0.07f)
            )
            val esquina = Offset(hand.x - k * 0.44f, techo)
            val tam = Size(k * 0.88f, k * 0.98f)
            drawRoundRect(Color(0xFFFFB74D), esquina, tam, CornerRadius(k * 0.06f))
            drawRoundRect(OUTLINE, esquina, tam, CornerRadius(k * 0.06f), style = Stroke(k * 0.07f))
        }

        Prop.BASKET -> {
            val techo = hand.y + k * 0.30f
            drawArc(
                OUTLINE, 180f, 180f, false,
                Offset(hand.x - k * 0.30f, techo - k * 0.30f), Size(k * 0.60f, k * 0.60f),
                style = Stroke(k * 0.07f)
            )
            val esquina = Offset(hand.x - k * 0.48f, techo)
            val tam = Size(k * 0.96f, k * 0.62f)
            drawRoundRect(Color(0xFFBCAAA4), esquina, tam, CornerRadius(k * 0.09f))
            repeat(2) { i ->
                val y = techo + k * 0.20f + k * 0.21f * i
                drawLine(OUTLINE, Offset(hand.x - k * 0.44f, y), Offset(hand.x + k * 0.44f, y), k * 0.04f)
            }
            drawRoundRect(OUTLINE, esquina, tam, CornerRadius(k * 0.09f), style = Stroke(k * 0.07f))
        }

        Prop.BACKPACK -> {
            val esquina = Offset(hand.x - k * 0.42f, hand.y - k * 0.46f)
            val tam = Size(k * 0.84f, k * 0.98f)
            drawRoundRect(Color(0xFF66BB6A), esquina, tam, CornerRadius(k * 0.18f))
            drawRoundRect(OUTLINE, esquina, tam, CornerRadius(k * 0.18f), style = Stroke(k * 0.07f))
            val bolsillo = Offset(hand.x - k * 0.28f, hand.y + k * 0.06f)
            drawRoundRect(Color(0xFF43A047), bolsillo, Size(k * 0.56f, k * 0.34f), CornerRadius(k * 0.08f))
            drawRoundRect(OUTLINE, bolsillo, Size(k * 0.56f, k * 0.34f), CornerRadius(k * 0.08f), style = Stroke(k * 0.06f))
            // Correa: es lo que la distingue de una caja verde.
            drawArc(
                OUTLINE, 200f, 140f, false,
                Offset(hand.x - k * 0.30f, hand.y - k * 0.64f), Size(k * 0.60f, k * 0.40f),
                style = Stroke(k * 0.07f)
            )
        }

        // Cuaderno cerrado, sostenido en la mano para mostrarlo.
        Prop.NOTEBOOK -> {
            val esquina = Offset(hand.x - k * 0.52f, hand.y - k * 0.70f)
            val tam = Size(k * 1.04f, k * 1.30f)
            drawRoundRect(Color(0xFFFFF8E1), esquina, tam, CornerRadius(k * 0.06f))
            repeat(3) { i ->
                val y = hand.y - k * 0.34f + k * 0.28f * i
                drawLine(
                    Color(0xFFBDBDBD), Offset(hand.x - k * 0.26f, y), Offset(hand.x + k * 0.36f, y),
                    k * 0.05f
                )
            }
            drawRoundRect(OUTLINE, esquina, tam, CornerRadius(k * 0.06f), style = Stroke(k * 0.07f))
            // Espiral al costado: separa el cuaderno de un libro.
            repeat(4) { i ->
                drawCircle(
                    OUTLINE, k * 0.08f,
                    Offset(hand.x - k * 0.52f, hand.y - k * 0.50f + k * 0.30f * i),
                    style = Stroke(k * 0.045f)
                )
            }
        }

        // Lápiz solo, en alto. Sin hoja: acá se nombra el objeto, no se usa.
        Prop.PENCIL_HELD -> {
            val punta = Offset(hand.x + k * 0.22f, hand.y + k * 0.70f)
            val tope = Offset(hand.x - k * 0.20f, hand.y - k * 0.66f)
            // El contorno va debajo y más grueso; el color encima lo recorta.
            drawLine(OUTLINE, tope, punta, k * 0.33f, cap = StrokeCap.Butt)
            drawLine(Color(0xFFF4B400), tope, punta, k * 0.26f, cap = StrokeCap.Butt)
            drawCircle(Color(0xFF6D4C41), k * 0.13f, punta)
            drawRoundRect(
                Color(0xFFEF9A9A),
                Offset(tope.x - k * 0.14f, tope.y - k * 0.06f), Size(k * 0.28f, k * 0.22f),
                CornerRadius(k * 0.06f)
            )
        }

        // Reloj junto a la cabeza. La vuelta de las agujas es la que dice si el
        // tiempo va hacia adelante o hacia atrás, porque la postura sola no
        // alcanza para separar "ayer" de "mañana".
        Prop.CLOCK -> {
            val c = Offset(cx - W * 0.31f, headC.y - k * 0.15f)
            val r = k * 0.88f
            drawCircle(Color(0xFFFFF8E1), r, c)
            drawCircle(OUTLINE, r, c, style = Stroke(k * 0.11f))
            repeat(12) { i ->
                val a = i * 30f * PI.toFloat() / 180f
                drawLine(
                    OUTLINE,
                    Offset(c.x + sin(a) * r * 0.74f, c.y - cos(a) * r * 0.74f),
                    Offset(c.x + sin(a) * r * 0.90f, c.y - cos(a) * r * 0.90f),
                    k * 0.05f
                )
            }
            val vuelta = pose.propPhase * 2f * PI.toFloat()
            // Minutero: una vuelta entera por ciclo. Horario: media vuelta.
            drawLine(
                OUTLINE, c,
                Offset(c.x + sin(vuelta) * r * 0.68f, c.y - cos(vuelta) * r * 0.68f),
                k * 0.08f, cap = StrokeCap.Round
            )
            drawLine(
                OUTLINE, c,
                Offset(c.x + sin(vuelta * 0.5f) * r * 0.44f, c.y - cos(vuelta * 0.5f) * r * 0.44f),
                k * 0.11f, cap = StrokeCap.Round
            )
            drawCircle(OUTLINE, k * 0.10f, c)
        }

        Prop.MOM, Prop.DAD, Prop.SISTER, Prop.BROTHER, Prop.GRANDMA, Prop.GRANDPA,
        Prop.AUNT, Prop.UNCLE, Prop.TEACHER, Prop.FRIEND ->
            acompanante(prop)?.let { drawCompanion(it, cx, W, H, ground, k) }

        // El bebé va en brazos, envuelto, con un mechón asomando.
        Prop.BABY -> {
            val c = Offset(cx, shoulder + H * 0.13f)
            val manta = Offset(c.x - k * 0.56f, c.y - k * 0.26f)
            val tam = Size(k * 1.12f, k * 0.56f)
            drawRoundRect(Color(0xFFB3E5FC), manta, tam, CornerRadius(k * 0.26f))
            drawRoundRect(OUTLINE, manta, tam, CornerRadius(k * 0.26f), style = Stroke(k * 0.07f))
            val cabeza = Offset(c.x - k * 0.50f, c.y - k * 0.06f)
            drawCircle(Color(0xFFFFCC9E), k * 0.30f, cabeza)
            drawCircle(OUTLINE, k * 0.30f, cabeza, style = Stroke(k * 0.07f))
            drawArc(
                Color(0xFF8D6E63), 200f, 140f, true,
                Offset(cabeza.x - k * 0.30f, cabeza.y - k * 0.32f), Size(k * 0.60f, k * 0.40f)
            )
            drawLine(
                OUTLINE, Offset(cabeza.x - k * 0.12f, cabeza.y + k * 0.02f),
                Offset(cabeza.x + k * 0.02f, cabeza.y + k * 0.02f), k * 0.05f, cap = StrokeCap.Round
            )
        }

        Prop.TOWEL -> {
            val esquina = Offset(hand.x - k * 0.46f, hand.y - k * 0.20f)
            val tam = Size(k * 0.92f, k * 1.25f)
            drawRoundRect(Color(0xFF80DEEA), esquina, tam, CornerRadius(k * 0.12f))
            repeat(2) { i ->
                val y = hand.y + k * 0.30f + k * 0.26f * i
                drawLine(
                    Color(0xFF26A69A), Offset(hand.x - k * 0.46f, y), Offset(hand.x + k * 0.46f, y),
                    k * 0.09f
                )
            }
            drawRoundRect(OUTLINE, esquina, tam, CornerRadius(k * 0.12f), style = Stroke(k * 0.07f))
        }

        // Escritorio: mesa, cuaderno apoyado y el lápiz que baja hasta el papel.
        Prop.DESK -> {
            val techo = hip + H * 0.035f
            drawLine(Color(0xFF8D6E63), Offset(cx - W * 0.27f, techo), Offset(cx - W * 0.27f, ground), k * 0.15f)
            drawLine(Color(0xFF8D6E63), Offset(cx + W * 0.27f, techo), Offset(cx + W * 0.27f, ground), k * 0.15f)
            val tapa = Offset(cx - W * 0.36f, techo)
            val tamTapa = Size(W * 0.72f, H * 0.032f)
            drawRoundRect(Color(0xFFA1887F), tapa, tamTapa, CornerRadius(k * 0.06f))
            drawRoundRect(OUTLINE, tapa, tamTapa, CornerRadius(k * 0.06f), style = Stroke(k * 0.07f))

            val libreta = Offset(cx - k * 0.66f, techo - k * 0.36f)
            val tamLibreta = Size(k * 1.32f, k * 0.38f)
            drawRoundRect(Color(0xFFFFF8E1), libreta, tamLibreta, CornerRadius(k * 0.05f))
            repeat(2) { i ->
                val y = techo - k * 0.25f + k * 0.13f * i
                drawLine(Color(0xFFBDBDBD), Offset(cx - k * 0.50f, y), Offset(cx + k * 0.54f, y), k * 0.035f)
            }
            drawRoundRect(OUTLINE, libreta, tamLibreta, CornerRadius(k * 0.05f), style = Stroke(k * 0.06f))

            drawLine(
                Color(0xFFF4B400), hand, Offset(hand.x - k * 0.10f, techo - k * 0.20f),
                k * 0.13f, cap = StrokeCap.Round
            )
        }

        Prop.TOYS -> {
            val y = ground - H * 0.02f
            val pelota = Offset(cx - W * 0.21f, y - k * 0.32f)
            drawCircle(Color(0xFFFF8A65), k * 0.32f, pelota)
            drawCircle(OUTLINE, k * 0.32f, pelota, style = Stroke(k * 0.06f))
            val cubo = Offset(cx + W * 0.11f, y - k * 0.54f)
            drawRoundRect(Color(0xFF4DB6AC), cubo, Size(k * 0.54f, k * 0.54f), CornerRadius(k * 0.08f))
            drawRoundRect(OUTLINE, cubo, Size(k * 0.54f, k * 0.54f), CornerRadius(k * 0.08f), style = Stroke(k * 0.06f))
            val aro = Offset(cx - W * 0.02f, y - k * 0.24f)
            drawCircle(Color(0xFFFFD54F), k * 0.24f, aro)
            drawCircle(OUTLINE, k * 0.24f, aro, style = Stroke(k * 0.06f))
        }

        Prop.BLOCKS -> {
            val colores = listOf(Color(0xFFEF5350), Color(0xFF66BB6A), Color(0xFF42A5F5))
            val lado = k * 0.56f
            repeat(3) { i ->
                val esquina = Offset(cx + W * 0.17f - lado / 2f, ground - H * 0.02f - lado * (i + 1))
                drawRoundRect(colores[i], esquina, Size(lado, lado), CornerRadius(k * 0.08f))
                drawRoundRect(OUTLINE, esquina, Size(lado, lado), CornerRadius(k * 0.08f), style = Stroke(k * 0.06f))
            }
            // El que lleva en la mano: sin él no se ve que está apilando.
            val enMano = Offset(hand.x - lado / 2f, hand.y - lado / 2f)
            drawRoundRect(Color(0xFFFFCA28), enMano, Size(lado, lado), CornerRadius(k * 0.08f))
            drawRoundRect(OUTLINE, enMano, Size(lado, lado), CornerRadius(k * 0.08f), style = Stroke(k * 0.06f))
        }

        Prop.DOLL -> {
            val c = Offset(cx, shoulder + H * 0.13f)
            val manta = Offset(c.x - k * 0.52f, c.y - k * 0.24f)
            drawRoundRect(Color(0xFFF48FB1), manta, Size(k * 1.04f, k * 0.52f), CornerRadius(k * 0.24f))
            drawRoundRect(OUTLINE, manta, Size(k * 1.04f, k * 0.52f), CornerRadius(k * 0.24f), style = Stroke(k * 0.07f))
            val cabeza = Offset(c.x - k * 0.46f, c.y - k * 0.06f)
            drawCircle(Color(0xFFFFE0B2), k * 0.27f, cabeza)
            drawCircle(OUTLINE, k * 0.27f, cabeza, style = Stroke(k * 0.07f))
            // Ojo cerrado: el bebé duerme, y eso lo separa de un bulto de tela.
            drawLine(
                OUTLINE, Offset(cabeza.x - k * 0.10f, cabeza.y - k * 0.02f),
                Offset(cabeza.x + k * 0.04f, cabeza.y - k * 0.02f), k * 0.05f, cap = StrokeCap.Round
            )
        }

        Prop.TABLE -> {
            val techo = hip + H * 0.015f
            val tapa = Offset(cx - W * 0.34f, techo)
            val tam = Size(W * 0.68f, H * 0.035f)
            drawLine(Color(0xFF8D6E63), Offset(cx - W * 0.26f, techo), Offset(cx - W * 0.26f, ground), k * 0.15f)
            drawLine(Color(0xFF8D6E63), Offset(cx + W * 0.26f, techo), Offset(cx + W * 0.26f, ground), k * 0.15f)
            drawRoundRect(Color(0xFFA1887F), tapa, tam, CornerRadius(k * 0.06f))
            drawRoundRect(OUTLINE, tapa, tam, CornerRadius(k * 0.06f), style = Stroke(k * 0.07f))
        }

        Prop.CUP -> glass(hand, k, Color(0xFF74C0FC))
        Prop.WATER -> glass(hand, k, Color(0xFF4FC3F7))
        Prop.MILK -> glass(hand, k, Color(0xFFF7F7F7))
        Prop.JUICE -> glass(hand, k, Color(0xFFFFA726))
        Prop.TEA -> teaCup(hand, k)
        Prop.COCOA -> mug(hand, k, Color(0xFF8D5A3B))
        Prop.SPOON -> {
            drawLine(Color(0xFFB0B0B8), hand, mouth, k * 0.12f, cap = StrokeCap.Round)
            drawOval(Color(0xFFB0B0B8), Offset(mouth.x - k * 0.24f, mouth.y - k * 0.14f), Size(k * 0.48f, k * 0.28f))
        }
        Prop.TOOTHBRUSH -> {
            drawLine(Color(0xFF43C0E0), hand, Offset(mouth.x, mouth.y + k * 0.05f), k * 0.14f, cap = StrokeCap.Round)
            drawRoundRect(Color.White, Offset(mouth.x - k * 0.26f, mouth.y - k * 0.12f), Size(k * 0.52f, k * 0.17f), CornerRadius(k * 0.05f))
        }
        Prop.SHOWER -> {
            drawRoundRect(Color(0xFF9AA6B2), Offset(cx - k * 0.8f, H * 0.03f), Size(k * 1.6f, k * 0.45f), CornerRadius(k * 0.12f))
            val water = Color(0xFF7FC7F5).copy(alpha = 0.55f)
            repeat(7) { i ->
                val x = cx - k * 0.7f + k * 0.24f * i
                drawLine(water, Offset(x, H * 0.09f), Offset(x, headC.y + k * 0.4f), k * 0.07f)
            }
        }
        // El jabón va en la mano, para poder mostrarlo como objeto suelto.
        Prop.SOAP -> {
            val esquina = Offset(hand.x - k * 0.44f, hand.y - k * 0.26f)
            val tam = Size(k * 0.88f, k * 0.52f)
            drawRoundRect(Color(0xFFB5EAD7), esquina, tam, CornerRadius(k * 0.17f))
            drawRoundRect(OUTLINE, esquina, tam, CornerRadius(k * 0.17f), style = Stroke(k * 0.07f))
            drawLine(
                Color.White, Offset(hand.x - k * 0.22f, hand.y - k * 0.11f),
                Offset(hand.x + k * 0.04f, hand.y - k * 0.11f), k * 0.08f, cap = StrokeCap.Round
            )
        }

        /**
         * Lavabo. Todo él vive POR DEBAJO de la cadera: el avatar está parado
         * detrás del mueble, como frente a un lavabo de verdad. Esa sola regla
         * es la que evita que la pileta o la canilla crucen por encima del
         * cuerpo, que es lo que las hacía leerse como una caja sobre el pecho.
         */
        Prop.SINK -> {
            val borde = hip + H * 0.085f
            val ancho = W * 0.40f
            val izq = cx - ancho / 2f
            val alto = H * 0.13f

            // Canilla primero, para que la pileta le tape el pie y parezca
            // salir de atrás del mueble. El poste va por fuera de la silueta
            // del torso y el codo del caño queda debajo de la cadera.
            val poste = cx + ancho * 0.38f
            val altoCano = hip + H * 0.022f
            drawLine(Color(0xFF90A4AE), Offset(poste, borde), Offset(poste, altoCano), k * 0.16f, cap = StrokeCap.Round)
            drawLine(Color(0xFF90A4AE), Offset(poste, altoCano), Offset(cx, altoCano), k * 0.16f, cap = StrokeCap.Round)

            // Chorro: cae justo entre las manos. Semitransparente, para leerse
            // como agua por delante y no como una barra que las tapa.
            drawLine(
                Color(0xFF4FC3F7).copy(alpha = 0.5f),
                Offset(cx, altoCano + k * 0.1f), Offset(cx, borde + k * 0.3f),
                k * 0.15f
            )

            // Mesada y pileta. El hueco interior más oscuro es lo que la hace
            // leer como bacha; sin él es una mesa blanca.
            drawRoundRect(Color(0xFFECEFF1), Offset(izq, borde), Size(ancho, alto), CornerRadius(k * 0.22f))
            drawRoundRect(
                Color(0xFFCFD8DC),
                Offset(izq + k * 0.3f, borde + k * 0.18f),
                Size(ancho - k * 0.6f, alto - k * 0.5f),
                CornerRadius(k * 0.16f)
            )
            drawRoundRect(OUTLINE, Offset(izq, borde), Size(ancho, alto), CornerRadius(k * 0.22f), style = Stroke(k * 0.07f))

            // Jaboncito apoyado en la punta de la mesada, lejos de las manos.
            val jabon = Offset(izq + k * 0.18f, borde - k * 0.26f)
            val tamJabon = Size(k * 0.58f, k * 0.28f)
            drawRoundRect(Color(0xFFB5EAD7), jabon, tamJabon, CornerRadius(k * 0.1f))
            drawRoundRect(OUTLINE, jabon, tamJabon, CornerRadius(k * 0.1f), style = Stroke(k * 0.06f))
        }
        Prop.COMB -> {
            drawRoundRect(Color(0xFF9B7EDE), Offset(hand.x - k * 0.4f, hand.y - k * 0.14f), Size(k * 0.8f, k * 0.28f), CornerRadius(k * 0.06f))
            repeat(5) { i ->
                val x = hand.x - k * 0.32f + k * 0.16f * i
                drawLine(Color(0xFF9B7EDE), Offset(x, hand.y + k * 0.14f), Offset(x, hand.y + k * 0.34f), k * 0.05f)
            }
        }
        Prop.BALL -> {
            val by = ground - H * 0.05f - abs(pose.reach - 0.5f) * H * 0.12f
            val at = Offset(cx + W * 0.16f, by)
            drawCircle(Color(0xFFFF8FA3), k * 0.55f, at)
            drawArc(
                Color.White.copy(alpha = 0.7f), 200f, 140f, false,
                Offset(at.x - k * 0.4f, at.y - k * 0.4f), Size(k * 0.8f, k * 0.8f),
                style = Stroke(k * 0.1f)
            )
        }
        Prop.BOOK -> {
            val by = shoulder + H * 0.12f
            drawPath(Path().apply {
                moveTo(cx, by - k * 0.55f); lineTo(cx - k * 1.25f, by - k * 0.22f)
                lineTo(cx - k * 1.25f, by + k * 0.68f); lineTo(cx, by + k * 0.38f); close()
            }, Color(0xFFFFF3B0))
            drawPath(Path().apply {
                moveTo(cx, by - k * 0.55f); lineTo(cx + k * 1.25f, by - k * 0.22f)
                lineTo(cx + k * 1.25f, by + k * 0.68f); lineTo(cx, by + k * 0.38f); close()
            }, Color(0xFFFFE0B2))
            repeat(3) { i ->
                val y = by - k * 0.02f + k * 0.2f * i
                drawLine(Color(0xFFBFA98A), Offset(cx - k * 1.05f, y), Offset(cx - k * 0.25f, y), k * 0.05f)
                drawLine(Color(0xFFBFA98A), Offset(cx + k * 0.25f, y), Offset(cx + k * 1.05f, y), k * 0.05f)
            }
        }
        Prop.PENCIL -> {
            drawRoundRect(Color.White, Offset(cx - k * 0.7f, hip - H * 0.02f), Size(k * 1.4f, k * 1.0f), CornerRadius(k * 0.06f))
            drawLine(Color(0xFFF4B400), hand, Offset(hand.x - k * 0.55f, hand.y + k * 0.55f), k * 0.12f, cap = StrokeCap.Round)
        }
        Prop.THERMOMETER -> {
            drawLine(Color.White, hand, Offset(cx - k * 0.1f, headC.y - k * 0.5f), k * 0.14f, cap = StrokeCap.Round)
            drawCircle(Color(0xFFE35D6A), k * 0.14f, Offset(cx - k * 0.1f, headC.y - k * 0.5f))
        }
        Prop.SYRINGE -> {
            drawLine(Color(0xFFDADFE3), Offset(hand.x + k * 0.6f, hand.y - k * 0.6f), target, k * 0.16f, cap = StrokeCap.Round)
            drawLine(Color(0xFF9AA6B2), target, Offset(target.x - k * 0.1f, target.y + k * 0.2f), k * 0.06f)
        }
        Prop.BANDAGE ->
            drawRoundRect(Color(0xFFF5D6A8), Offset(target.x - k * 0.32f, target.y - k * 0.2f), Size(k * 0.64f, k * 0.4f), CornerRadius(k * 0.07f))
        Prop.NOTE -> listOf(Offset(headC.x + k * 1.4f, headC.y - k), Offset(headC.x + k * 2.0f, headC.y - k * 1.6f)).forEach {
            drawCircle(Color(0xFF5B8DEF), k * 0.22f, it)
            drawLine(Color(0xFF5B8DEF), Offset(it.x + k * 0.2f, it.y), Offset(it.x + k * 0.2f, it.y - k * 0.7f), k * 0.07f)
        }
        // Bocina apuntando al avatar, con ondas que salen hacia la oreja.
        Prop.HORN -> {
            val hx = cx - W * 0.31f
            val hy = headC.y + k * 0.5f
            drawRoundRect(Color(0xFF6C737F), Offset(hx - k * 0.62f, hy - k * 0.3f), Size(k * 0.5f, k * 0.6f), CornerRadius(k * 0.1f))
            val cone = Path().apply {
                moveTo(hx - k * 0.14f, hy - k * 0.3f)
                lineTo(hx + k * 0.6f, hy - k * 0.85f)
                lineTo(hx + k * 0.6f, hy + k * 0.85f)
                lineTo(hx - k * 0.14f, hy + k * 0.3f)
                close()
            }
            drawPath(cone, Color(0xFFE08A3C))
            drawPath(cone, OUTLINE, style = Stroke(k * 0.09f))
            drawRoundRect(OUTLINE, Offset(hx - k * 0.62f, hy - k * 0.3f), Size(k * 0.5f, k * 0.6f), CornerRadius(k * 0.1f), style = Stroke(k * 0.08f))
            // Tres ondas desfasadas: nacen en la boca del cono y se abren.
            repeat(3) { i ->
                val f = ((pose.propPhase + i / 3f) % 1f)
                val r = k * (0.55f + f * 1.5f)
                drawArc(
                    Color(0xFF5B8DEF).copy(alpha = (1f - f) * 0.85f),
                    -46f, 92f, false,
                    Offset(hx + k * 0.6f - r, hy - r), Size(r * 2f, r * 2f),
                    style = Stroke(k * 0.13f, cap = StrokeCap.Round)
                )
            }
        }
        // Escenario y cama: no van en la mano, se dibujan aparte.
        Prop.BED, Prop.CHAIR, Prop.TOILET, Prop.DOOR, Prop.SWING, Prop.SLIDE,
        Prop.SUN, Prop.MOON, Prop.RAIN, Prop.CLOUDY,
        Prop.HOUSE, Prop.SCHOOL, Prop.PARK, Prop.HOSPITAL,
        Prop.SHOP, Prop.KITCHEN, Prop.CAR -> Unit
    }
}

/**
 * Silla, inodoro y puerta. Van DETRÁS de la figura: el cuerpo tapa el centro y
 * solo se ven los bordes, que es justo lo que hace falta para que "sentarse" no
 * se confunda con "ir al baño", y para que la mano quede sobre el picaporte.
 *
 * @param phase 0..1; en la puerta es cuánto está abierta.
 */
private fun DrawScope.drawBackdrop(
    prop: Prop,
    phase: Float,
    cx: Float,
    W: Float,
    H: Float,
    shoulder: Float,
    hip: Float,
    ground: Float,
    edge: Float
) {
    when (prop) {
        // Lugares. Todos ocupan el mismo hueco a la derecha, con la misma base,
        // para que al cambiar de palabra solo cambie el edificio y el chico
        // pueda compararlos: lo que distingue la escuela del hospital es la
        // bandera contra la cruz, no que uno esté más arriba que el otro.
        Prop.HOUSE, Prop.SCHOOL, Prop.HOSPITAL, Prop.SHOP,
        Prop.PARK, Prop.KITCHEN, Prop.CAR -> {
            val bx = cx + W * 0.26f
            val base = ground
            val ancho = W * 0.46f
            val alto = H * 0.34f
            val muro = Offset(bx - ancho / 2f, base - alto)
            val tamMuro = Size(ancho, alto)

            fun caja(color: Color) {
                drawRoundRect(color, muro, tamMuro, CornerRadius(edge))
                drawRoundRect(OUTLINE, muro, tamMuro, CornerRadius(edge), style = Stroke(edge))
            }
            fun puerta() {
                val p = Offset(bx - ancho * 0.11f, base - alto * 0.42f)
                val t = Size(ancho * 0.22f, alto * 0.42f)
                drawRoundRect(Color(0xFF6D4C41), p, t, CornerRadius(edge * 0.8f))
                drawRoundRect(OUTLINE, p, t, CornerRadius(edge * 0.8f), style = Stroke(edge * 0.8f))
            }
            fun ventanas(n: Int) {
                repeat(n) { i ->
                    val v = Offset(bx - ancho * 0.36f + ancho * 0.30f * i, base - alto * 0.82f)
                    val t = Size(ancho * 0.19f, alto * 0.22f)
                    drawRoundRect(Color(0xFFFFF59D), v, t, CornerRadius(edge * 0.6f))
                    drawRoundRect(OUTLINE, v, t, CornerRadius(edge * 0.6f), style = Stroke(edge * 0.7f))
                }
            }

            when (prop) {
                Prop.HOUSE -> {
                    caja(Color(0xFFFFE0B2))
                    val techo = Path().apply {
                        moveTo(bx - ancho * 0.60f, base - alto)
                        lineTo(bx, base - alto * 1.42f)
                        lineTo(bx + ancho * 0.60f, base - alto)
                        close()
                    }
                    drawPath(techo, Color(0xFFD35D5D))
                    drawPath(techo, OUTLINE, style = Stroke(edge))
                    puerta()
                    ventanas(1)
                }
                Prop.SCHOOL -> {
                    caja(Color(0xFFCE93D8))
                    puerta()
                    ventanas(3)
                    // Mástil con bandera: es la marca de la escuela.
                    val mastil = bx + ancho * 0.40f
                    drawLine(OUTLINE, Offset(mastil, base - alto), Offset(mastil, base - alto * 1.45f), edge)
                    drawRoundRect(
                        Color(0xFF42A5F5), Offset(mastil, base - alto * 1.45f),
                        Size(ancho * 0.24f, alto * 0.16f), CornerRadius(edge * 0.5f)
                    )
                }
                Prop.HOSPITAL -> {
                    caja(Color(0xFFF5F5F5))
                    puerta()
                    ventanas(3)
                    val cruzC = Offset(bx, base - alto * 0.78f)
                    val brazo = ancho * 0.30f
                    val grosor = ancho * 0.10f
                    drawRoundRect(Color(0xFFE53935), Offset(cruzC.x - brazo / 2f, cruzC.y - grosor / 2f), Size(brazo, grosor), CornerRadius(edge * 0.4f))
                    drawRoundRect(Color(0xFFE53935), Offset(cruzC.x - grosor / 2f, cruzC.y - brazo / 2f), Size(grosor, brazo), CornerRadius(edge * 0.4f))
                }
                Prop.SHOP -> {
                    caja(Color(0xFFFFF3E0))
                    puerta()
                    // Toldo a rayas: la marca del negocio.
                    val toldoY = base - alto * 0.94f
                    repeat(5) { i ->
                        val x = bx - ancho * 0.50f + ancho * 0.20f * i
                        drawRoundRect(
                            if (i % 2 == 0) Color(0xFFE53935) else Color(0xFFFFFFFF),
                            Offset(x, toldoY), Size(ancho * 0.20f, alto * 0.14f), CornerRadius(edge * 0.3f)
                        )
                    }
                    drawRoundRect(
                        OUTLINE, Offset(bx - ancho * 0.50f, toldoY),
                        Size(ancho, alto * 0.14f), CornerRadius(edge * 0.3f), style = Stroke(edge * 0.8f)
                    )
                }
                Prop.PARK -> {
                    // Árbol y banco, no edificio.
                    val tronco = bx + ancho * 0.10f
                    drawLine(Color(0xFF6D4C41), Offset(tronco, base), Offset(tronco, base - alto * 0.70f), edge * 3f)
                    drawCircle(Color(0xFF66BB6A), ancho * 0.30f, Offset(tronco, base - alto * 0.92f))
                    drawCircle(OUTLINE, ancho * 0.30f, Offset(tronco, base - alto * 0.92f), style = Stroke(edge))
                    val banco = Offset(bx - ancho * 0.62f, base - alto * 0.22f)
                    drawRoundRect(Color(0xFF8D6E63), banco, Size(ancho * 0.50f, alto * 0.08f), CornerRadius(edge * 0.4f))
                    drawRoundRect(OUTLINE, banco, Size(ancho * 0.50f, alto * 0.08f), CornerRadius(edge * 0.4f), style = Stroke(edge * 0.7f))
                    drawLine(OUTLINE, Offset(bx - ancho * 0.54f, base - alto * 0.14f), Offset(bx - ancho * 0.54f, base), edge * 1.4f)
                    drawLine(OUTLINE, Offset(bx - ancho * 0.20f, base - alto * 0.14f), Offset(bx - ancho * 0.20f, base), edge * 1.4f)
                }
                Prop.KITCHEN -> {
                    // Mesada con hornalla y olla: la cocina es lo que pasa ahí,
                    // no el cuarto vacío.
                    val mesada = Offset(bx - ancho * 0.52f, base - alto * 0.46f)
                    drawRoundRect(Color(0xFFB0BEC5), mesada, Size(ancho * 1.04f, alto * 0.46f), CornerRadius(edge))
                    drawRoundRect(OUTLINE, mesada, Size(ancho * 1.04f, alto * 0.46f), CornerRadius(edge), style = Stroke(edge))
                    val olla = Offset(bx - ancho * 0.22f, base - alto * 0.76f)
                    drawRoundRect(Color(0xFF546E7A), olla, Size(ancho * 0.44f, alto * 0.30f), CornerRadius(edge * 0.6f))
                    drawRoundRect(OUTLINE, olla, Size(ancho * 0.44f, alto * 0.30f), CornerRadius(edge * 0.6f), style = Stroke(edge * 0.8f))
                    repeat(3) { i ->
                        val x = bx - ancho * 0.14f + ancho * 0.14f * i
                        drawLine(
                            Color(0xFFE0E0E0), Offset(x, base - alto * 0.80f),
                            Offset(x, base - alto * 0.98f), edge * 1.2f, cap = StrokeCap.Round
                        )
                    }
                }
                else -> {
                    // Auto: carrocería, techo y ruedas.
                    val cuerpo = Offset(bx - ancho * 0.60f, base - alto * 0.42f)
                    drawRoundRect(Color(0xFF42A5F5), cuerpo, Size(ancho * 1.20f, alto * 0.34f), CornerRadius(edge * 2f))
                    val techo = Offset(bx - ancho * 0.32f, base - alto * 0.68f)
                    drawRoundRect(Color(0xFF42A5F5), techo, Size(ancho * 0.66f, alto * 0.28f), CornerRadius(edge * 2f))
                    drawRoundRect(Color(0xFFB3E5FC), Offset(techo.x + edge, techo.y + edge), Size(ancho * 0.66f - edge * 2f, alto * 0.20f), CornerRadius(edge))
                    drawRoundRect(OUTLINE, techo, Size(ancho * 0.66f, alto * 0.28f), CornerRadius(edge * 2f), style = Stroke(edge))
                    drawRoundRect(OUTLINE, cuerpo, Size(ancho * 1.20f, alto * 0.34f), CornerRadius(edge * 2f), style = Stroke(edge))
                    drawCircle(OUTLINE, alto * 0.12f, Offset(bx - ancho * 0.34f, base - alto * 0.06f))
                    drawCircle(OUTLINE, alto * 0.12f, Offset(bx + ancho * 0.34f, base - alto * 0.06f))
                }
            }
        }

        Prop.SUN -> {
            val c = Offset(cx + W * 0.30f, H * 0.14f)
            val r = W * 0.085f
            repeat(8) { i ->
                val a = i * 45f * PI.toFloat() / 180f
                drawLine(
                    Color(0xFFFFB300),
                    Offset(c.x + sin(a) * r * 1.35f, c.y - cos(a) * r * 1.35f),
                    Offset(c.x + sin(a) * r * 1.90f, c.y - cos(a) * r * 1.90f),
                    edge * 2.2f, cap = StrokeCap.Round
                )
            }
            drawCircle(Color(0xFFFFD54F), r, c)
            drawCircle(OUTLINE, r, c, style = Stroke(edge))
        }

        Prop.MOON -> {
            val c = Offset(cx + W * 0.30f, H * 0.14f)
            val r = W * 0.085f
            // Luna creciente: un círculo lleno y otro del color del fondo encima.
            drawCircle(Color(0xFFFFF176), r, c)
            drawCircle(OUTLINE, r, c, style = Stroke(edge))
            drawCircle(Color(0xFF3B3355), r * 0.86f, Offset(c.x + r * 0.46f, c.y - r * 0.18f))
            listOf(
                Offset(cx - W * 0.30f, H * 0.10f),
                Offset(cx - W * 0.14f, H * 0.20f),
                Offset(cx + W * 0.10f, H * 0.07f)
            ).forEach { estrella ->
                drawCircle(Color(0xFFFFF9C4), edge * 1.6f, estrella)
            }
        }

        Prop.RAIN, Prop.CLOUDY -> {
            val nubes = listOf(
                Offset(cx - W * 0.20f, H * 0.14f) to W * 0.10f,
                Offset(cx + W * 0.02f, H * 0.11f) to W * 0.13f,
                Offset(cx + W * 0.24f, H * 0.15f) to W * 0.10f
            )
            nubes.forEach { (c, r) -> drawCircle(OUTLINE, r + edge, c) }
            nubes.forEach { (c, r) -> drawCircle(Color(0xFFCFD8DC), r, c) }
            if (prop == Prop.RAIN) {
                repeat(7) { i ->
                    val x = cx - W * 0.26f + W * 0.085f * i
                    val y0 = H * 0.24f + (i % 3) * H * 0.035f
                    drawLine(
                        Color(0xFF4FC3F7), Offset(x, y0), Offset(x - W * 0.012f, y0 + H * 0.10f),
                        edge * 1.8f, cap = StrokeCap.Round
                    )
                }
            }
        }

        Prop.SWING -> {
            val barra = H * 0.07f
            val asiento = hip + H * 0.055f
            val soga = W * 0.12f
            drawLine(
                Color(0xFF8D6E63), Offset(cx - W * 0.36f, barra), Offset(cx + W * 0.36f, barra),
                edge * 2.6f, cap = StrokeCap.Round
            )
            drawLine(Color(0xFF6D4C41), Offset(cx - soga, barra), Offset(cx - soga, asiento), edge * 1.5f)
            drawLine(Color(0xFF6D4C41), Offset(cx + soga, barra), Offset(cx + soga, asiento), edge * 1.5f)
            val tabla = Offset(cx - soga - W * 0.035f, asiento)
            val tam = Size(soga * 2f + W * 0.07f, H * 0.026f)
            drawRoundRect(Color(0xFF5D4037), tabla, tam, CornerRadius(edge))
            drawRoundRect(OUTLINE, tabla, tam, CornerRadius(edge), style = Stroke(edge))
        }

        Prop.SLIDE -> {
            // Rampa que baja de izquierda a derecha, con la escalera del lado alto.
            val alto = H * 0.40f
            val rampa = Path().apply {
                moveTo(cx - W * 0.40f, ground - alto)
                lineTo(cx + W * 0.34f, ground - H * 0.04f)
                lineTo(cx + W * 0.34f, ground)
                lineTo(cx - W * 0.40f, ground)
                close()
            }
            drawPath(rampa, Color(0xFF64B5F6))
            drawPath(rampa, OUTLINE, style = Stroke(edge))
            drawLine(
                Color(0xFF8D6E63), Offset(cx - W * 0.46f, ground), Offset(cx - W * 0.46f, ground - alto),
                edge * 2f, cap = StrokeCap.Round
            )
            repeat(3) { i ->
                val y = ground - alto * 0.26f * (i + 1)
                drawLine(Color(0xFF8D6E63), Offset(cx - W * 0.46f, y), Offset(cx - W * 0.38f, y), edge * 1.6f)
            }
        }

        Prop.DOOR -> {
            val open = phase.coerceIn(0f, 1f)
            val x0 = cx + W * 0.13f          // borde libre con la puerta cerrada
            val x1 = cx + W * 0.45f          // bisagra, lado lejano
            val top = H * 0.13f

            // Marco y hueco que queda a la vista al abrir.
            drawRoundRect(Color(0xFF6F5B45), Offset(x0 - W * 0.025f, top - H * 0.02f), Size(x1 - x0 + W * 0.05f, ground - top + H * 0.02f), CornerRadius(W * 0.012f))
            drawRoundRect(Color(0xFF2E3238), Offset(x0, top), Size(x1 - x0, ground - top), CornerRadius(W * 0.008f))

            // Hoja: se angosta a medida que gira hacia el observador.
            val freeX = x1 - (x1 - x0) * (1f - open * 0.68f)
            val grow = open * H * 0.022f
            val panel = Path().apply {
                moveTo(freeX, top - grow)
                lineTo(x1, top)
                lineTo(x1, ground)
                lineTo(freeX, ground + grow)
                close()
            }
            drawPath(panel, Color(0xFFC9A87C))
            drawPath(panel, OUTLINE, style = Stroke(edge))
            // Cuarterón, para que se lea como puerta y no como tabla.
            val inset = (x1 - freeX) * 0.22f
            if (inset > W * 0.006f) {
                drawRoundRect(
                    Color(0xFFB08F63),
                    Offset(freeX + inset, top + H * 0.06f),
                    Size((x1 - freeX) - inset * 2f, (ground - top) * 0.62f),
                    CornerRadius(W * 0.008f),
                    style = Stroke(edge * 0.8f)
                )
            }
            // Picaporte sobre el borde libre, a la altura del hombro.
            drawCircle(Color(0xFFD8C27A), edge * 2.2f, Offset(freeX + (x1 - freeX) * 0.14f, shoulder + H * 0.05f))
            drawCircle(OUTLINE, edge * 2.2f, Offset(freeX + (x1 - freeX) * 0.14f, shoulder + H * 0.05f), style = Stroke(edge * 0.7f))
        }
        Prop.CHAIR -> {
            val wood = Color(0xFFCBB79B)
            val back = Offset(cx - W * 0.14f, hip - H * 0.21f)
            val backSize = Size(W * 0.28f, H * 0.22f)
            drawRoundRect(wood, back, backSize, CornerRadius(W * 0.03f))
            drawRoundRect(OUTLINE, back, backSize, CornerRadius(W * 0.03f), style = Stroke(edge))

            listOf(-0.145f, 0.145f).forEach { f ->
                drawLine(wood.dark(0.78f), Offset(cx + W * f, hip + H * 0.05f), Offset(cx + W * f, ground), W * 0.03f, cap = StrokeCap.Round)
            }
            val seat = Offset(cx - W * 0.17f, hip + H * 0.015f)
            val seatSize = Size(W * 0.34f, H * 0.035f)
            drawRoundRect(wood.dark(0.9f), seat, seatSize, CornerRadius(W * 0.014f))
            drawRoundRect(OUTLINE, seat, seatSize, CornerRadius(W * 0.014f), style = Stroke(edge))
        }
        Prop.TOILET -> {
            val porcelain = Color(0xFFF1F4F8)
            val tank = Offset(cx - W * 0.12f, hip - H * 0.20f)
            val tankSize = Size(W * 0.24f, H * 0.20f)
            drawRoundRect(porcelain, tank, tankSize, CornerRadius(W * 0.025f))
            drawRoundRect(OUTLINE, tank, tankSize, CornerRadius(W * 0.025f), style = Stroke(edge))
            drawCircle(Color(0xFFB9C2CC), W * 0.022f, Offset(cx, hip - H * 0.165f))

            val base = Offset(cx - W * 0.09f, hip + H * 0.04f)
            val baseSize = Size(W * 0.18f, (ground - hip - H * 0.04f).coerceAtLeast(H * 0.02f))
            drawRoundRect(porcelain.dark(0.94f), base, baseSize, CornerRadius(W * 0.02f))
            drawRoundRect(OUTLINE, base, baseSize, CornerRadius(W * 0.02f), style = Stroke(edge))

            val bowl = Offset(cx - W * 0.18f, hip + H * 0.005f)
            val bowlSize = Size(W * 0.36f, H * 0.05f)
            drawRoundRect(porcelain, bowl, bowlSize, CornerRadius(W * 0.07f))
            drawRoundRect(OUTLINE, bowl, bowlSize, CornerRadius(W * 0.07f), style = Stroke(edge))
        }
        else -> Unit
    }
}

/**
 * Rasgos de la otra persona que aparece al lado del avatar.
 *
 * Tres variables alcanzan para distinguir a las once: **altura, pelo y ropa**.
 * Un abuelo es alto y canoso, un hermano es bajo, el bebé va en brazos. Esto no
 * pretende ser un segundo avatar configurable —sería duplicar el renderizador
 * entero— sino lo mínimo para que el chico sepa a quién está nombrando.
 */
private data class Acompanante(
    val altura: Float,
    val pelo: Color,
    val peloLargo: Boolean,
    val ropa: Color,
    val baston: Boolean = false,
    val libro: Boolean = false
)

private fun acompanante(prop: Prop): Acompanante? = when (prop) {
    Prop.MOM -> Acompanante(1.00f, Color(0xFF6D4C41), true, Color(0xFFE57373))
    Prop.DAD -> Acompanante(1.06f, Color(0xFF4E342E), false, Color(0xFF64B5F6))
    Prop.SISTER -> Acompanante(0.72f, Color(0xFF8D6E63), true, Color(0xFFBA68C8))
    Prop.BROTHER -> Acompanante(0.76f, Color(0xFF5D4037), false, Color(0xFF81C784))
    Prop.GRANDMA -> Acompanante(0.92f, Color(0xFFCFD8DC), true, Color(0xFFF06292), baston = true)
    Prop.GRANDPA -> Acompanante(0.96f, Color(0xFFCFD8DC), false, Color(0xFF90A4AE), baston = true)
    Prop.AUNT -> Acompanante(0.98f, Color(0xFF3E2723), true, Color(0xFFFFB74D))
    Prop.UNCLE -> Acompanante(1.03f, Color(0xFF795548), false, Color(0xFF9575CD))
    Prop.TEACHER -> Acompanante(1.00f, Color(0xFF37474F), true, Color(0xFF4DB6AC), libro = true)
    Prop.FRIEND -> Acompanante(0.80f, Color(0xFFFF8A65), false, Color(0xFFFFD54F))
    else -> null
}

/** La otra persona, parada a la derecha del avatar. */
private fun DrawScope.drawCompanion(
    a: Acompanante,
    cx: Float,
    W: Float,
    H: Float,
    ground: Float,
    k: Float
) {
    val x = cx + W * 0.30f
    val alto = H * 0.54f * a.altura
    val cabezaR = k * 0.60f * a.altura
    val cabezaY = ground - alto + cabezaR
    val hombro = cabezaY + cabezaR * 1.25f
    val cadera = ground - alto * 0.36f
    val borde = k * 0.09f
    val piel = Color(0xFFFFCC9E)

    // Piernas y brazos: línea gruesa oscura, que es como se dibuja el avatar.
    drawLine(OUTLINE, Offset(x - k * 0.20f, cadera), Offset(x - k * 0.24f, ground), borde * 2.4f, cap = StrokeCap.Round)
    drawLine(OUTLINE, Offset(x + k * 0.20f, cadera), Offset(x + k * 0.24f, ground), borde * 2.4f, cap = StrokeCap.Round)
    drawLine(OUTLINE, Offset(x - k * 0.40f * a.altura, hombro + k * 0.10f), Offset(x - k * 0.62f * a.altura, cadera), borde * 2f, cap = StrokeCap.Round)
    drawLine(OUTLINE, Offset(x + k * 0.40f * a.altura, hombro + k * 0.10f), Offset(x + k * 0.62f * a.altura, cadera), borde * 2f, cap = StrokeCap.Round)

    val torso = Offset(x - k * 0.42f * a.altura, hombro)
    val tamTorso = Size(k * 0.84f * a.altura, cadera - hombro)
    drawRoundRect(a.ropa, torso, tamTorso, CornerRadius(k * 0.18f))
    drawRoundRect(OUTLINE, torso, tamTorso, CornerRadius(k * 0.18f), style = Stroke(borde))

    // El pelo largo va detrás de la cabeza, así que se dibuja antes.
    if (a.peloLargo) {
        val melena = Offset(x - cabezaR * 1.08f, cabezaY - cabezaR * 0.45f)
        val tamMelena = Size(cabezaR * 2.16f, cabezaR * 2.45f)
        drawRoundRect(a.pelo, melena, tamMelena, CornerRadius(cabezaR * 0.62f))
        drawRoundRect(OUTLINE, melena, tamMelena, CornerRadius(cabezaR * 0.62f), style = Stroke(borde))
    }

    drawCircle(piel, cabezaR, Offset(x, cabezaY))
    drawCircle(OUTLINE, cabezaR, Offset(x, cabezaY), style = Stroke(borde))
    val capa = Offset(x - cabezaR, cabezaY - cabezaR)
    drawArc(a.pelo, 180f, 180f, true, capa, Size(cabezaR * 2f, cabezaR * 1.55f))
    drawArc(OUTLINE, 180f, 180f, false, capa, Size(cabezaR * 2f, cabezaR * 1.55f), style = Stroke(borde))

    drawCircle(OUTLINE, cabezaR * 0.14f, Offset(x - cabezaR * 0.34f, cabezaY + cabezaR * 0.06f))
    drawCircle(OUTLINE, cabezaR * 0.14f, Offset(x + cabezaR * 0.34f, cabezaY + cabezaR * 0.06f))
    drawArc(
        OUTLINE, 20f, 140f, false,
        Offset(x - cabezaR * 0.44f, cabezaY + cabezaR * 0.12f), Size(cabezaR * 0.88f, cabezaR * 0.62f),
        style = Stroke(borde * 0.8f)
    )

    if (a.baston) {
        drawLine(
            Color(0xFF8D6E63), Offset(x + k * 0.64f * a.altura, cadera - k * 0.1f),
            Offset(x + k * 0.72f * a.altura, ground), borde * 1.9f, cap = StrokeCap.Round
        )
    }
    if (a.libro) {
        val libro = Offset(x - k * 0.86f, cadera - k * 0.30f)
        val tamLibro = Size(k * 0.58f, k * 0.44f)
        drawRoundRect(Color(0xFFFFF8E1), libro, tamLibro, CornerRadius(k * 0.04f))
        drawRoundRect(OUTLINE, libro, tamLibro, CornerRadius(k * 0.04f), style = Stroke(borde * 0.8f))
    }
}

/**
 * Prendas de ropa, centradas en [c]. Cada una tiene silueta propia, no el mismo
 * rectángulo en otro color: la forma es lo que dice a qué parte del cuerpo va,
 * y eso es justamente lo que el chico tiene que poder leer de un vistazo.
 */
private fun DrawScope.drawGarment(prop: Prop, c: Offset, k: Float) {
    val borde = k * 0.07f

    fun pieza(x: Float, y: Float, ancho: Float, alto: Float, radio: Float, color: Color) {
        val esquina = Offset(c.x + k * x, c.y + k * y)
        val tamano = Size(k * ancho, k * alto)
        drawRoundRect(color, esquina, tamano, CornerRadius(k * radio))
        drawRoundRect(OUTLINE, esquina, tamano, CornerRadius(k * radio), style = Stroke(borde))
    }

    when (prop) {
        Prop.SHIRT, Prop.PAJAMAS, Prop.JACKET -> {
            val color = when (prop) {
                Prop.SHIRT -> Color(0xFF4FC3F7)
                Prop.PAJAMAS -> Color(0xFFB39DDB)
                else -> Color(0xFF7E57C2)
            }
            pieza(-0.72f, -0.30f, 0.34f, 0.34f, 0.08f, color)   // manga izquierda
            pieza(0.38f, -0.30f, 0.34f, 0.34f, 0.08f, color)    // manga derecha
            pieza(-0.42f, -0.34f, 0.84f, 0.78f, 0.10f, color)   // cuerpo
            // Cuello: sin él la silueta se lee como una almohada.
            drawArc(
                OUTLINE, 20f, 140f, false,
                Offset(c.x - k * 0.18f, c.y - k * 0.44f), Size(k * 0.36f, k * 0.28f),
                style = Stroke(borde)
            )
            when (prop) {
                // Lunares, para que el pijama no sea una remera de otro color.
                Prop.PAJAMAS -> repeat(3) { i ->
                    drawCircle(Color.White, k * 0.07f, Offset(c.x - k * 0.2f + k * 0.2f * i, c.y + k * 0.08f))
                }
                // Cierre al medio: es lo que separa una campera de una remera.
                Prop.JACKET -> drawLine(
                    Color(0xFFFFD54F),
                    Offset(c.x, c.y - k * 0.30f), Offset(c.x, c.y + k * 0.40f),
                    borde, cap = StrokeCap.Round
                )
                else -> Unit
            }
        }

        Prop.PANTS -> {
            val color = Color(0xFF5C6BC0)
            pieza(-0.38f, -0.12f, 0.34f, 0.66f, 0.06f, color)   // pierna izquierda
            pieza(0.04f, -0.12f, 0.34f, 0.66f, 0.06f, color)    // pierna derecha
            pieza(-0.40f, -0.36f, 0.80f, 0.32f, 0.06f, color)   // cintura
        }

        Prop.SHOES -> {
            val color = Color(0xFFEF6C00)
            pieza(-0.34f, -0.28f, 0.44f, 0.42f, 0.14f, color)   // empeine
            pieza(-0.34f, 0.02f, 0.78f, 0.24f, 0.10f, color)    // punta
            pieza(-0.36f, 0.22f, 0.82f, 0.14f, 0.06f, Color(0xFF4E342E)) // suela
        }

        Prop.SOCKS -> {
            val color = Color(0xFFFFF176)
            pieza(-0.18f, -0.34f, 0.36f, 0.50f, 0.08f, color)   // caña
            pieza(-0.18f, 0.04f, 0.60f, 0.28f, 0.12f, color)    // pie
            pieza(-0.20f, -0.40f, 0.40f, 0.14f, 0.05f, Color(0xFFEF9A9A)) // puño
        }

        Prop.HAT -> {
            val color = Color(0xFFEF5350)
            val copa = Offset(c.x - k * 0.40f, c.y - k * 0.36f)
            val tamano = Size(k * 0.80f, k * 0.72f)
            drawArc(color, 180f, 180f, true, copa, tamano)
            drawArc(OUTLINE, 180f, 180f, false, copa, tamano, style = Stroke(borde))
            pieza(-0.58f, -0.04f, 1.16f, 0.18f, 0.09f, color)   // visera
        }

        else -> Unit
    }
}

/**
 * El alimento que el chico está pidiendo, dibujado en la mano. Cada uno tiene
 * su propia forma: si pide sopa se ve una sopa, no una galleta genérica.
 */
private fun DrawScope.drawFood(prop: Prop, c: Offset, k: Float) {
    val e = k * 0.085f
    val porcelain = Color(0xFFF1F4F8)

    /** Cuenco con contenido: base de sopa, fideos y arroz. */
    fun bowl(content: Color, mound: Boolean) {
        val bw = k * 1.34f
        val bh = k * 0.82f
        if (mound) {
            val top = Offset(c.x - bw * 0.42f, c.y - bh * 0.74f)
            val topSize = Size(bw * 0.84f, bh * 0.76f)
            drawArc(content, 180f, 180f, true, top, topSize)
            drawArc(OUTLINE, 180f, 180f, false, top, topSize, style = Stroke(e))
        } else {
            val rim = Offset(c.x - bw / 2f, c.y - bh * 0.46f)
            val rimSize = Size(bw, bh * 0.46f)
            drawOval(content, rim, rimSize)
            drawOval(OUTLINE, rim, rimSize, style = Stroke(e))
        }
        val body = Path().apply {
            moveTo(c.x - bw / 2f, c.y - bh * 0.23f)
            lineTo(c.x + bw / 2f, c.y - bh * 0.23f)
            quadraticTo(c.x, c.y + bh * 1.0f, c.x - bw / 2f, c.y - bh * 0.23f)
            close()
        }
        drawPath(body, porcelain)
        drawPath(body, OUTLINE, style = Stroke(e))
    }

    fun steam() {
        val s = Color(0xFFBFD3E3)
        listOf(-0.3f, 0.26f).forEach { dx ->
            drawPath(Path().apply {
                moveTo(c.x + k * dx, c.y - k * 0.62f)
                quadraticTo(c.x + k * (dx + 0.22f), c.y - k * 0.92f, c.x + k * dx, c.y - k * 1.2f)
            }, s, style = Stroke(k * 0.1f, cap = StrokeCap.Round))
        }
    }

    when (prop) {
        Prop.SOUP -> { bowl(Color(0xFFE39B3D), mound = false); steam() }
        Prop.NOODLES -> {
            bowl(Color(0xFFF0D98A), mound = false)
            listOf(-0.34f, 0f, 0.34f).forEach { dx ->
                drawPath(Path().apply {
                    moveTo(c.x + k * dx, c.y - k * 0.36f)
                    quadraticTo(c.x + k * (dx + 0.26f), c.y - k * 0.78f, c.x + k * (dx - 0.06f), c.y - k * 1.02f)
                }, Color(0xFFE8CB6A), style = Stroke(k * 0.13f, cap = StrokeCap.Round))
            }
        }
        Prop.RICE -> {
            bowl(Color(0xFFFBFBF7), mound = true)
            listOf(-0.22f to -0.5f, 0.1f to -0.62f, 0.26f to -0.42f).forEach { (dx, dy) ->
                drawCircle(Color(0xFFD8D8CE), k * 0.07f, Offset(c.x + k * dx, c.y + k * dy))
            }
        }
        Prop.BREAD -> {
            val w = k * 1.35f
            val h = k * 0.78f
            drawOval(Color(0xFFD9A15E), Offset(c.x - w / 2f, c.y - h / 2f), Size(w, h))
            drawOval(OUTLINE, Offset(c.x - w / 2f, c.y - h / 2f), Size(w, h), style = Stroke(e))
            listOf(-0.26f, 0.06f).forEach { dx ->
                drawLine(
                    Color(0xFFB07C3E),
                    Offset(c.x + k * dx, c.y - k * 0.16f),
                    Offset(c.x + k * (dx + 0.22f), c.y + k * 0.12f),
                    k * 0.09f, cap = StrokeCap.Round
                )
            }
        }
        Prop.APPLE -> {
            drawCircle(Color(0xFFE04B4B), k * 0.56f, Offset(c.x, c.y + k * 0.08f))
            drawCircle(OUTLINE, k * 0.56f, Offset(c.x, c.y + k * 0.08f), style = Stroke(e))
            drawLine(Color(0xFF7A5230), Offset(c.x, c.y - k * 0.4f), Offset(c.x + k * 0.1f, c.y - k * 0.78f), k * 0.11f, cap = StrokeCap.Round)
            drawPath(Path().apply {
                moveTo(c.x + k * 0.08f, c.y - k * 0.62f)
                quadraticTo(c.x + k * 0.62f, c.y - k * 0.9f, c.x + k * 0.5f, c.y - k * 0.42f)
                quadraticTo(c.x + k * 0.26f, c.y - k * 0.4f, c.x + k * 0.08f, c.y - k * 0.62f)
                close()
            }, Color(0xFF5BA85B))
        }
        Prop.BANANA -> {
            val body = Path().apply {
                moveTo(c.x - k * 0.62f, c.y - k * 0.34f)
                quadraticTo(c.x - k * 0.1f, c.y + k * 0.78f, c.x + k * 0.68f, c.y + k * 0.1f)
                quadraticTo(c.x + k * 0.2f, c.y + k * 0.4f, c.x - k * 0.44f, c.y - k * 0.44f)
                close()
            }
            drawPath(body, Color(0xFFF2CE4A))
            drawPath(body, OUTLINE, style = Stroke(e))
            drawCircle(Color(0xFF8A6B2E), k * 0.1f, Offset(c.x - k * 0.56f, c.y - k * 0.38f))
        }
        Prop.CHICKEN -> {
            drawOval(Color(0xFFC98A4B), Offset(c.x - k * 0.62f, c.y - k * 0.38f), Size(k * 1.0f, k * 0.86f))
            drawOval(OUTLINE, Offset(c.x - k * 0.62f, c.y - k * 0.38f), Size(k * 1.0f, k * 0.86f), style = Stroke(e))
            drawLine(Color(0xFFF2EDE2), Offset(c.x + k * 0.26f, c.y - k * 0.02f), Offset(c.x + k * 0.62f, c.y - k * 0.34f), k * 0.22f, cap = StrokeCap.Round)
            drawCircle(Color(0xFFF2EDE2), k * 0.19f, Offset(c.x + k * 0.66f, c.y - k * 0.38f))
            drawCircle(OUTLINE, k * 0.19f, Offset(c.x + k * 0.66f, c.y - k * 0.38f), style = Stroke(e * 0.8f))
        }
        Prop.MEAT -> {
            val cut = Path().apply {
                moveTo(c.x - k * 0.66f, c.y - k * 0.1f)
                quadraticTo(c.x - k * 0.4f, c.y - k * 0.62f, c.x + k * 0.28f, c.y - k * 0.48f)
                quadraticTo(c.x + k * 0.78f, c.y - k * 0.34f, c.x + k * 0.56f, c.y + k * 0.24f)
                quadraticTo(c.x + k * 0.1f, c.y + k * 0.62f, c.x - k * 0.5f, c.y + k * 0.34f)
                close()
            }
            drawPath(cut, Color(0xFFB4574F))
            drawPath(cut, OUTLINE, style = Stroke(e))
            drawPath(Path().apply {
                moveTo(c.x - k * 0.34f, c.y - k * 0.02f)
                quadraticTo(c.x, c.y - k * 0.24f, c.x + k * 0.36f, c.y - k * 0.04f)
            }, Color(0xFFE2B7A8), style = Stroke(k * 0.11f, cap = StrokeCap.Round))
        }
        Prop.EGG -> {
            val white = Path().apply {
                moveTo(c.x - k * 0.7f, c.y)
                quadraticTo(c.x - k * 0.66f, c.y - k * 0.56f, c.x - k * 0.1f, c.y - k * 0.5f)
                quadraticTo(c.x + k * 0.36f, c.y - k * 0.66f, c.x + k * 0.6f, c.y - k * 0.2f)
                quadraticTo(c.x + k * 0.84f, c.y + k * 0.3f, c.x + k * 0.24f, c.y + k * 0.42f)
                quadraticTo(c.x - k * 0.44f, c.y + k * 0.56f, c.x - k * 0.7f, c.y)
                close()
            }
            drawPath(white, Color(0xFFFDFDF6))
            drawPath(white, OUTLINE, style = Stroke(e))
            drawCircle(Color(0xFFF5B92E), k * 0.3f, Offset(c.x - k * 0.02f, c.y - k * 0.04f))
            drawCircle(OUTLINE, k * 0.3f, Offset(c.x - k * 0.02f, c.y - k * 0.04f), style = Stroke(e * 0.8f))
        }
        Prop.YOGURT -> {
            val cup = Path().apply {
                moveTo(c.x - k * 0.46f, c.y - k * 0.42f)
                lineTo(c.x + k * 0.46f, c.y - k * 0.42f)
                lineTo(c.x + k * 0.34f, c.y + k * 0.52f)
                lineTo(c.x - k * 0.34f, c.y + k * 0.52f)
                close()
            }
            drawPath(cup, Color(0xFFF7F7FB))
            drawPath(cup, OUTLINE, style = Stroke(e))
            drawRoundRect(Color(0xFFE86A9A), Offset(c.x - k * 0.54f, c.y - k * 0.56f), Size(k * 1.08f, k * 0.2f), CornerRadius(k * 0.06f))
            drawRoundRect(OUTLINE, Offset(c.x - k * 0.54f, c.y - k * 0.56f), Size(k * 1.08f, k * 0.2f), CornerRadius(k * 0.06f), style = Stroke(e * 0.8f))
            drawLine(Color(0xFFB0B0B8), Offset(c.x + k * 0.16f, c.y - k * 0.4f), Offset(c.x + k * 0.46f, c.y - k * 1.0f), k * 0.1f, cap = StrokeCap.Round)
        }
        Prop.CAKE -> {
            val slice = Path().apply {
                moveTo(c.x - k * 0.58f, c.y + k * 0.5f)
                lineTo(c.x - k * 0.58f, c.y - k * 0.24f)
                lineTo(c.x + k * 0.58f, c.y - k * 0.24f)
                lineTo(c.x + k * 0.58f, c.y + k * 0.5f)
                close()
            }
            drawPath(slice, Color(0xFFF0CE96))
            drawLine(Color(0xFFF7E3EC), Offset(c.x - k * 0.58f, c.y + k * 0.14f), Offset(c.x + k * 0.58f, c.y + k * 0.14f), k * 0.18f)
            drawPath(slice, OUTLINE, style = Stroke(e))
            drawRoundRect(Color(0xFFF7E3EC), Offset(c.x - k * 0.62f, c.y - k * 0.44f), Size(k * 1.24f, k * 0.24f), CornerRadius(k * 0.1f))
            drawRoundRect(OUTLINE, Offset(c.x - k * 0.62f, c.y - k * 0.44f), Size(k * 1.24f, k * 0.24f), CornerRadius(k * 0.1f), style = Stroke(e * 0.8f))
            drawCircle(Color(0xFFD9455A), k * 0.17f, Offset(c.x, c.y - k * 0.62f))
            drawCircle(OUTLINE, k * 0.17f, Offset(c.x, c.y - k * 0.62f), style = Stroke(e * 0.8f))
        }
        Prop.ICE_CREAM -> {
            val cone = Path().apply {
                moveTo(c.x - k * 0.42f, c.y - k * 0.02f)
                lineTo(c.x + k * 0.42f, c.y - k * 0.02f)
                lineTo(c.x, c.y + k * 0.86f)
                close()
            }
            drawPath(cone, Color(0xFFD9A15E))
            drawPath(cone, OUTLINE, style = Stroke(e))
            drawCircle(Color(0xFFFDF3E0), k * 0.32f, Offset(c.x - k * 0.2f, c.y - k * 0.24f))
            drawCircle(Color(0xFFF7B7C8), k * 0.34f, Offset(c.x + k * 0.18f, c.y - k * 0.3f))
            drawCircle(Color(0xFFF7DE9B), k * 0.3f, Offset(c.x - k * 0.02f, c.y - k * 0.66f))
            listOf(
                Triple(-0.2f, -0.24f, 0.32f), Triple(0.18f, -0.3f, 0.34f), Triple(-0.02f, -0.66f, 0.3f)
            ).forEach { (dx, dy, r) ->
                drawCircle(OUTLINE, k * r, Offset(c.x + k * dx, c.y + k * dy), style = Stroke(e * 0.8f))
            }
        }
        Prop.CHEESE -> {
            val wedge = Path().apply {
                moveTo(c.x - k * 0.66f, c.y + k * 0.42f)
                lineTo(c.x + k * 0.66f, c.y + k * 0.42f)
                lineTo(c.x + k * 0.1f, c.y - k * 0.5f)
                close()
            }
            drawPath(wedge, Color(0xFFF2C94C))
            drawPath(wedge, OUTLINE, style = Stroke(e))
            listOf(-0.24f to 0.18f, 0.2f to 0.22f, 0.02f to -0.06f).forEach { (dx, dy) ->
                drawCircle(Color(0xFFD9A92E), k * 0.11f, Offset(c.x + k * dx, c.y + k * dy))
            }
        }
        Prop.PIZZA -> {
            val slice = Path().apply {
                moveTo(c.x, c.y - k * 0.66f)
                lineTo(c.x + k * 0.56f, c.y + k * 0.56f)
                quadraticTo(c.x, c.y + k * 0.84f, c.x - k * 0.56f, c.y + k * 0.56f)
                close()
            }
            drawPath(slice, Color(0xFFE0B36B))
            drawPath(slice, OUTLINE, style = Stroke(e))
            drawPath(Path().apply {
                moveTo(c.x - k * 0.5f, c.y + k * 0.46f)
                quadraticTo(c.x, c.y + k * 0.72f, c.x + k * 0.5f, c.y + k * 0.46f)
            }, Color(0xFFC08A46), style = Stroke(k * 0.14f, cap = StrokeCap.Round))
            listOf(-0.16f to 0.08f, 0.18f to 0.26f, 0.0f to -0.24f).forEach { (dx, dy) ->
                drawCircle(Color(0xFFB33A3A), k * 0.12f, Offset(c.x + k * dx, c.y + k * dy))
            }
        }
        // Galleta: también es la comida genérica cuando la frase no nombra nada concreto.
        else -> {
            drawCircle(Color(0xFFD9A15E), k * 0.52f, c)
            drawCircle(OUTLINE, k * 0.52f, c, style = Stroke(e))
            listOf(-0.2f to -0.16f, 0.18f to -0.06f, -0.02f to 0.2f).forEach { (dx, dy) ->
                drawCircle(Color(0xFF6B4423), k * 0.09f, Offset(c.x + k * dx, c.y + k * dy))
            }
        }
    }
}

/** Vaso transparente con líquido de color adentro. */
private fun DrawScope.glass(hand: Offset, k: Float, liquid: Color) {
    val x = hand.x
    val y = hand.y
    drawRoundRect(Color(0xFFB0C4DE).copy(alpha = 0.25f), Offset(x - k * 0.36f, y - k * 0.6f), Size(k * 0.72f, k * 1.0f), CornerRadius(k * 0.06f))
    drawRoundRect(liquid, Offset(x - k * 0.3f, y - k * 0.18f), Size(k * 0.6f, k * 0.5f), CornerRadius(k * 0.05f))
    drawRoundRect(Color(0xFFDCE6EE), Offset(x - k * 0.36f, y - k * 0.6f), Size(k * 0.72f, k * 1.0f), CornerRadius(k * 0.06f), style = Stroke(k * 0.06f))
}

/** Taza con bolsita de té colgando. */
private fun DrawScope.teaCup(hand: Offset, k: Float) {
    val x = hand.x
    val y = hand.y
    drawRoundRect(Color(0xFFF3EDE3), Offset(x - k * 0.34f, y - k * 0.25f), Size(k * 0.68f, k * 0.6f), CornerRadius(k * 0.08f))
    drawArc(Color(0xFFF3EDE3), -80f, 180f, false, Offset(x + k * 0.28f, y - k * 0.15f), Size(k * 0.3f, k * 0.4f), style = Stroke(k * 0.07f))
    drawLine(Color(0xFFBFA98A), Offset(x + k * 0.1f, y - k * 0.25f), Offset(x + k * 0.1f, y + k * 0.05f), k * 0.03f)
    drawRoundRect(Color(0xFFC9A66B), Offset(x + k * 0.02f, y + k * 0.05f), Size(k * 0.16f, k * 0.18f), CornerRadius(k * 0.03f))
}

/** Taza (mug) con líquido caliente adentro y vapor. */
private fun DrawScope.mug(hand: Offset, k: Float, liquid: Color) {
    val x = hand.x
    val y = hand.y
    drawRoundRect(Color(0xFFEDE7DF), Offset(x - k * 0.34f, y - k * 0.28f), Size(k * 0.68f, k * 0.62f), CornerRadius(k * 0.08f))
    drawRoundRect(liquid, Offset(x - k * 0.26f, y - k * 0.2f), Size(k * 0.52f, k * 0.16f), CornerRadius(k * 0.04f))
    drawArc(Color(0xFFEDE7DF), -80f, 180f, false, Offset(x + k * 0.28f, y - k * 0.18f), Size(k * 0.32f, k * 0.42f), style = Stroke(k * 0.08f))
    val steam = Color.White.copy(alpha = 0.4f)
    drawLine(steam, Offset(x - k * 0.1f, y - k * 0.4f), Offset(x - k * 0.05f, y - k * 0.7f), k * 0.05f, cap = StrokeCap.Round)
    drawLine(steam, Offset(x + k * 0.1f, y - k * 0.4f), Offset(x + k * 0.15f, y - k * 0.7f), k * 0.05f, cap = StrokeCap.Round)
}

private fun DrawScope.drawLying(config: AvatarConfig, pose: AvatarPose) {
    val W = size.width
    val H = size.height
    val robot = config.kind == AvatarKind.ROBOT

    drawRoundRect(Color(0xFFCBB79B), Offset(W * 0.06f, H * 0.52f), Size(W * 0.88f, H * 0.42f), CornerRadius(H * 0.03f))
    drawRoundRect(Color(0xFFF0F0F5), Offset(W * 0.08f, H * 0.50f), Size(W * 0.26f, H * 0.18f), CornerRadius(H * 0.03f)) // almohada

    val headC = Offset(W * 0.26f, H * 0.58f)
    val k = H * 0.075f
    if (robot) robotHead(config, pose.copy(sleeping = true), headC, k) else personHead(config, pose.copy(sleeping = true), headC, k)

    // cuerpo bajo la manta
    drawRoundRect(avColor(config.clothingColor), Offset(W * 0.34f, H * 0.6f), Size(W * 0.56f, H * 0.22f), CornerRadius(H * 0.04f))

    drawZzz(Offset(W * 0.42f, H * 0.4f), H * 0.06f)
}
