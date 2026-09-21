package com.tunombre.agustispeaks.avatar

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.tunombre.agustispeaks.data.model.AvatarConfig

internal fun avColor(value: String): Color = runCatching {
    Color(("FF" + value.removePrefix("#")).toLong(16))
}.getOrDefault(Color(0xFFBDBDBD))

internal fun Color.dark(f: Float = 0.8f) = Color(red * f, green * f, blue * f, alpha)

internal val EYE_C = Color(0xFF2C2C34)
internal val LIP_C = Color(0xFF7A3B3B)
internal val TEAR_C = Color(0xFF7FC7F5)

/* Paleta de acero: extremidades y articulaciones del robot. */
internal val STEEL = Color(0xFFBCC4D0)
internal val STEEL_L = Color(0xFFE3E8EE)
internal val STEEL_D = Color(0xFF8A93A3)
internal val JOINT_C = Color(0xFF515A6B)

/**
 * Contorno oscuro de toda la figura. Es lo que sostiene el dibujo a tamaño de
 * miniatura: sin él las formas planas se funden entre sí y con el fondo.
 */
internal val OUTLINE = Color(0xFF1B1E25)

/** Segmento con contorno: traza oscura gruesa y encima la de color. */
internal fun DrawScope.outlinedLimb(a: Offset, b: Offset, color: Color, width: Float) {
    limb(a, b, OUTLINE, width * 1.34f)
    limb(a, b, color, width)
}

/** Forma de la mano. La comparten persona y robot: cambia el color, no el gesto. */
internal enum class HandShape { RELAXED, OPEN, GRIP, POINT, THUMB_UP, THUMB_DOWN }

/**
 * Mano con dedos, para persona (piel) y robot (acero). Los dedos son lo que
 * vuelve legibles señalar, agarrar, saludar y el pulgar arriba o abajo: con una
 * mano redonda todas esas acciones se ven igual.
 *
 * @param at muñeca, centro de la palma.
 * @param toward hacia dónde apuntan los dedos.
 * @param s escala, el mismo grosor base que usan los miembros.
 * @param side -1 brazo izquierdo, 1 derecho; define de qué lado sale el pulgar.
 * @param metal true para el robot: dedos de acero con nudillos marcados.
 */
internal fun DrawScope.drawHand(
    shape: HandShape,
    at: Offset,
    toward: Offset,
    s: Float,
    side: Int,
    skin: Color,
    edge: Float,
    metal: Boolean
) {
    val raw = toward - at
    // Sin dirección utilizable los dedos caen hacia abajo, que es la pose neutra.
    val d = if (raw.getDistance() < 0.0001f) Offset(0f, s) else raw
    val n = d.getDistance()
    val ux = d.x / n
    val uy = d.y / n
    val px = -uy
    val py = ux

    val palmC = if (metal) STEEL else skin
    val fingerC = if (metal) STEEL_L else skin
    val fw = s * 0.29f

    /** Punto a [across] de separación lateral y [along] hacia donde miran los dedos. */
    fun pt(across: Float, along: Float) =
        Offset(at.x + px * across + ux * along, at.y + py * across + uy * along)

    fun finger(across: Float, length: Float, fan: Float, width: Float = fw) {
        val base = pt(across, s * 0.22f)
        val tip = pt(across + fan, s * 0.22f + length)
        limb(base, tip, OUTLINE, width * 1.5f)
        limb(base, tip, fingerC, width)
        if (metal) drawCircle(JOINT_C, width * 0.4f, pt(across + fan * 0.45f, s * 0.22f + length * 0.45f))
    }

    fun thumb(length: Float, width: Float = fw * 1.15f) {
        val base = pt(side * s * 0.34f, -s * 0.08f)
        val dx = px * side * 0.9f + ux * 0.4f
        val dy = py * side * 0.9f + uy * 0.4f
        val m = Offset(dx, dy).getDistance().coerceAtLeast(0.001f)
        val tip = Offset(base.x + dx / m * length, base.y + dy / m * length)
        limb(base, tip, OUTLINE, width * 1.5f)
        limb(base, tip, fingerC, width)
    }

    when (shape) {
        // Puño cerrado con el pulgar grueso en la dirección de [toward].
        HandShape.THUMB_UP, HandShape.THUMB_DOWN -> {
            val tip = pt(0f, s * 1.62f)
            limb(at, tip, OUTLINE, s * 0.68f)
            limb(at, tip, fingerC, s * 0.46f)
            drawCircle(palmC, s * 0.86f, at)
            drawCircle(OUTLINE, s * 0.86f, at, style = Stroke(edge))
            listOf(-0.3f, 0.3f).forEach { f ->
                drawLine(OUTLINE.copy(alpha = 0.4f), pt(s * 0.5f, s * f), pt(-s * 0.5f, s * f), s * 0.1f, cap = StrokeCap.Round)
            }
            if (metal) drawCircle(JOINT_C, s * 0.2f, at)
            return
        }
        // Índice extendido y el resto recogido.
        HandShape.POINT -> {
            listOf(-0.44f, -0.15f, 0.14f).forEach { finger(s * it, s * 0.5f, 0f, fw * 0.95f) }
            thumb(s * 0.62f)
            finger(s * 0.44f, s * 2.0f, 0f, fw * 1.05f)
        }
        // Mano abierta: dedos largos y bien separados.
        HandShape.OPEN -> {
            finger(-s * 0.48f, s * 0.98f, -s * 0.4f)
            finger(-s * 0.17f, s * 1.24f, -s * 0.13f)
            finger(s * 0.15f, s * 1.26f, s * 0.13f)
            finger(s * 0.46f, s * 1.06f, s * 0.4f)
            thumb(s * 1.0f)
        }
        // Dedos recogidos, como sosteniendo algo.
        HandShape.GRIP -> {
            listOf(-0.45f, -0.15f, 0.15f, 0.45f).forEach { finger(s * it, s * 0.6f, s * it * 0.25f) }
            thumb(s * 0.68f)
        }
        HandShape.RELAXED -> {
            listOf(-0.45f, -0.15f, 0.15f, 0.45f).forEach { finger(s * it, s * 0.78f, s * it * 0.3f) }
            thumb(s * 0.78f)
        }
    }

    // La palma va al final: tapa la raíz de los dedos y deja el borde limpio.
    drawCircle(palmC, s * 0.74f, at)
    drawCircle(OUTLINE, s * 0.74f, at, style = Stroke(edge))
    if (metal) drawCircle(JOINT_C, s * 0.24f, at)
}

internal data class FaceKeys(val eyes: String, val brows: String, val mouth: String)

internal fun faceKeys(expr: Expression?, cfg: AvatarConfig): FaceKeys = when (expr) {
    Expression.HAPPY -> FaceKeys("happy", "raised", "grin")
    Expression.SAD -> FaceKeys("sleepy", "worried", "sad")
    Expression.ANGRY -> FaceKeys("normal", "angry", "neutral")
    Expression.HURT -> FaceKeys("sleepy", "worried", "open")
    Expression.SURPRISED -> FaceKeys("surprised", "raised", "open")
    Expression.NEUTRAL -> FaceKeys("normal", "normal", "neutral")
    null -> FaceKeys(cfg.eyes, cfg.eyebrows, cfg.mouth)
}

/** Cabeza de persona centrada en [c] con medio-ancho [k]. */
internal fun DrawScope.personHead(cfg: AvatarConfig, pose: AvatarPose, c: Offset, k: Float) {
    val skin = avColor(cfg.skinColor)
    val hair = avColor(cfg.hairColor)
    val fk = faceKeys(pose.expression, cfg)
    val closed = pose.sleeping || pose.eyeClose > 0.6f

    val edge = k * 0.11f
    listOf(-1f, 1f).forEach { s ->
        val ear = Offset(c.x + s * k, c.y + k * 0.1f)
        drawCircle(skin, k * 0.22f, ear)
        drawCircle(OUTLINE, k * 0.22f, ear, style = Stroke(edge * 0.85f))
    }
    drawOval(skin, Offset(c.x - k, c.y - k * 1.15f), Size(2f * k, 2.3f * k))
    drawOval(OUTLINE, Offset(c.x - k, c.y - k * 1.15f), Size(2f * k, 2.3f * k), style = Stroke(edge))

    val le = Offset(c.x - k * 0.42f, c.y - k * 0.05f)
    val re = Offset(c.x + k * 0.42f, c.y - k * 0.05f)
    if (closed) {
        listOf(le, re).forEach {
            drawArc(EYE_C, 200f, 140f, false, Offset(it.x - k * 0.22f, it.y - k * 0.17f), Size(k * 0.44f, k * 0.44f), style = Stroke(k * 0.09f))
        }
    } else drawEyeSet(fk.eyes, le, re, k)

    // Las cejas se dibujan siempre: son la mitad de la expresión.
    drawBrowSet(fk.brows, Offset(c.x - k * 0.42f, c.y - k * 0.52f), Offset(c.x + k * 0.42f, c.y - k * 0.52f), c.x, hair.dark(0.75f), k)

    drawLine(skin.dark(0.8f), Offset(c.x, c.y + k * 0.08f), Offset(c.x, c.y + k * 0.28f), k * 0.08f, cap = StrokeCap.Round)

    val m = Offset(c.x, c.y + k * 0.5f)
    if (pose.jaw > 0.05f) {
        val h = k * (0.2f + 0.62f * pose.jaw)
        val wMouth = k * (0.46f + 0.18f * pose.jaw)
        drawOval(LIP_C, Offset(m.x - wMouth / 2f, m.y - h / 2f), Size(wMouth, h))
        drawOval(OUTLINE, Offset(m.x - wMouth / 2f, m.y - h / 2f), Size(wMouth, h), style = Stroke(edge * 0.8f))
    } else drawMouthShape(fk.mouth, m, k)

    if (pose.tears > 0.05f) {
        val d = k * (0.1f + 0.1f * pose.tears)
        drawCircle(TEAR_C, d, Offset(le.x, le.y + k * 0.5f + k * 0.5f * pose.tears))
        drawCircle(TEAR_C, d, Offset(re.x, re.y + k * 0.5f + k * 0.5f * pose.tears))
    }

    drawHairShape(cfg.hairStyle, hair, c, k)

    if (pose.sleeping) drawZzz(Offset(c.x + k * 1.3f, c.y - k * 1.2f), k)
}

/**
 * Cabeza de robot centrada en [c] con medio-ancho [k]. La forma del casco y de
 * la carátula las decide `cfg.robotFace`; encima se dibuja `cfg.robotAccessory`.
 */
internal fun DrawScope.robotHead(cfg: AvatarConfig, pose: AvatarPose, c: Offset, k: Float) {
    val body = avColor(cfg.robotColor)
    val plate = Color(0xFF23232E)
    val glow = if (pose.sleeping) Color(0xFF3A6070) else Color(0xFF8CE0FF)
    val face = cfg.robotFace

    robotAccessoryBack(cfg.robotAccessory, body, glow, c, k)

    /* casco */
    when (face) {
        "porthole" -> {
            drawCircle(body, k * 1.18f, Offset(c.x, c.y - k * 0.05f))
            drawCircle(body.dark(0.85f), k * 1.18f, Offset(c.x, c.y - k * 0.05f), style = Stroke(k * 0.14f))
        }
        "helmet" -> drawPath(Path().apply {
            moveTo(c.x - k * 1.1f, c.y + k * 1.0f)
            lineTo(c.x - k * 1.1f, c.y - k * 0.15f)
            quadraticTo(c.x - k * 1.1f, c.y - k * 1.45f, c.x, c.y - k * 1.45f)
            quadraticTo(c.x + k * 1.1f, c.y - k * 1.45f, c.x + k * 1.1f, c.y - k * 0.15f)
            lineTo(c.x + k * 1.1f, c.y + k * 1.0f)
            close()
        }, body)
        "retro" -> {
            drawRoundRect(body, Offset(c.x - k * 1.12f, c.y - k * 1.25f), Size(k * 2.24f, k * 2.3f), CornerRadius(k * 0.16f))
            drawRoundRect(body.dark(0.82f), Offset(c.x - k * 1.12f, c.y + k * 0.78f), Size(k * 2.24f, k * 0.27f), CornerRadius(k * 0.08f))
        }
        else -> drawRoundRect(body, Offset(c.x - k * 1.05f, c.y - k * 1.2f), Size(k * 2.1f, k * 2.2f), CornerRadius(k * 0.4f))
    }

    /* orejas / laterales */
    drawRoundRect(body.dark(0.8f), Offset(c.x - k * 1.28f, c.y - k * 0.2f), Size(k * 0.26f, k * 0.6f), CornerRadius(k * 0.1f))
    drawRoundRect(body.dark(0.8f), Offset(c.x + k * 1.02f, c.y - k * 0.2f), Size(k * 0.26f, k * 0.6f), CornerRadius(k * 0.1f))

    /* carátula */
    when (face) {
        "visor" -> {
            drawRoundRect(plate, Offset(c.x - k * 0.95f, c.y - k * 0.62f), Size(k * 1.9f, k * 0.88f), CornerRadius(k * 0.44f))
            drawRoundRect(plate, Offset(c.x - k * 0.5f, c.y + k * 0.32f), Size(k * 1.0f, k * 0.5f), CornerRadius(k * 0.14f))
        }
        "porthole" -> {
            drawCircle(plate, k * 0.82f, Offset(c.x, c.y - k * 0.05f))
            repeat(6) { i ->
                val a = (i * 60f) * Math.PI.toFloat() / 180f
                drawCircle(
                    body.dark(0.7f), k * 0.08f,
                    Offset(c.x + k * 1.0f * kotlin.math.cos(a), c.y - k * 0.05f + k * 1.0f * kotlin.math.sin(a))
                )
            }
        }
        "retro" -> {
            drawRoundRect(plate, Offset(c.x - k * 0.88f, c.y - k * 0.8f), Size(k * 1.76f, k * 1.5f), CornerRadius(k * 0.12f))
            repeat(5) { i ->
                val y = c.y - k * 0.66f + k * 0.3f * i
                drawLine(glow.copy(alpha = 0.10f), Offset(c.x - k * 0.84f, y), Offset(c.x + k * 0.84f, y), k * 0.08f)
            }
        }
        "helmet" -> drawRoundRect(plate, Offset(c.x - k * 0.78f, c.y - k * 0.62f), Size(k * 1.56f, k * 1.18f), CornerRadius(k * 0.52f))
        else -> drawRoundRect(plate, Offset(c.x - k * 0.8f, c.y - k * 0.75f), Size(k * 1.6f, k * 1.35f), CornerRadius(k * 0.2f))
    }

    val le = Offset(c.x - k * 0.38f, c.y - k * 0.15f)
    val re = Offset(c.x + k * 0.38f, c.y - k * 0.15f)
    val eyeKind = if (face == "visor") "visor" else cfg.robotEyes
    when {
        pose.sleeping || pose.eyeClose > 0.6f ->
            listOf(le, re).forEach { drawLine(glow, Offset(it.x - k * 0.2f, it.y), Offset(it.x + k * 0.2f, it.y), k * 0.09f) }
        pose.expression == Expression.HAPPY || pose.expression == Expression.SURPRISED ->
            listOf(le, re).forEach { drawArc(glow, 200f, 140f, false, Offset(it.x - k * 0.2f, it.y - k * 0.15f), Size(k * 0.4f, k * 0.4f), style = Stroke(k * 0.09f)) }
        pose.expression == Expression.ANGRY -> {
            listOf(le, re).forEach { drawRoundRect(glow, Offset(it.x - k * 0.2f, it.y - k * 0.08f), Size(k * 0.4f, k * 0.24f), CornerRadius(k * 0.06f)) }
            drawLine(Color(0xFFE23B3B), Offset(le.x - k * 0.24f, le.y - k * 0.34f), Offset(le.x + k * 0.24f, le.y - k * 0.16f), k * 0.1f)
            drawLine(Color(0xFFE23B3B), Offset(re.x + k * 0.24f, re.y - k * 0.34f), Offset(re.x - k * 0.24f, re.y - k * 0.16f), k * 0.1f)
        }
        else -> drawRobotEyeSet(eyeKind, le, re, Offset(c.x - k * 0.7f, c.y - k * 0.2f), glow, k)
    }

    val m = Offset(c.x, c.y + k * 0.55f)
    if (pose.jaw > 0.05f) {
        drawRoundRect(glow, Offset(m.x - k * 0.3f, m.y - k * (0.1f + 0.35f * pose.jaw)), Size(k * 0.6f, k * (0.2f + 0.5f * pose.jaw)), CornerRadius(k * 0.05f))
    } else drawRobotMouthShape(cfg.robotMouth, m, plate, glow, k)

    robotAccessoryFront(cfg.robotAccessory, body, glow, c, k)

    if (pose.sleeping) drawZzz(Offset(c.x + k * 1.3f, c.y - k * 1.2f), k)
}

/** Accesorios que van DETRÁS del casco (antenas, radar, aro). */
private fun DrawScope.robotAccessoryBack(kind: String, body: Color, glow: Color, c: Offset, k: Float) {
    when (kind) {
        "antennas" -> listOf(-1f, 1f).forEach { s ->
            val top = Offset(c.x + s * k * 0.85f, c.y - k * 2.0f)
            drawLine(body.dark(0.8f), Offset(c.x + s * k * 0.55f, c.y - k * 1.15f), top, k * 0.11f, cap = StrokeCap.Round)
            drawCircle(glow, k * 0.19f, top)
        }
        "radar" -> {
            drawLine(body.dark(0.8f), Offset(c.x + k * 0.5f, c.y - k * 1.15f), Offset(c.x + k * 0.85f, c.y - k * 1.85f), k * 0.12f, cap = StrokeCap.Round)
            drawArc(
                STEEL, 200f, 160f, true,
                Offset(c.x + k * 0.25f, c.y - k * 2.25f), Size(k * 1.2f, k * 0.85f)
            )
            drawCircle(glow, k * 0.11f, Offset(c.x + k * 0.85f, c.y - k * 1.9f))
        }
        "halo" -> {
            drawOval(glow.copy(alpha = 0.35f), Offset(c.x - k * 0.9f, c.y - k * 1.9f), Size(k * 1.8f, k * 0.5f))
            drawOval(glow, Offset(c.x - k * 0.9f, c.y - k * 1.9f), Size(k * 1.8f, k * 0.5f), style = Stroke(k * 0.1f))
        }
        "none", "headphones", "goggles" -> Unit
        else -> { // "antenna"
            drawLine(body.dark(0.8f), Offset(c.x, c.y - k * 1.2f), Offset(c.x, c.y - k * 1.75f), k * 0.12f, cap = StrokeCap.Round)
            drawCircle(glow, k * 0.21f, Offset(c.x, c.y - k * 1.9f))
        }
    }
}

/** Accesorios que van DELANTE del casco (orejeras, gafas). */
private fun DrawScope.robotAccessoryFront(kind: String, body: Color, glow: Color, c: Offset, k: Float) {
    when (kind) {
        "headphones" -> {
            drawArc(
                STEEL_D, 190f, 160f, false,
                Offset(c.x - k * 1.32f, c.y - k * 1.7f), Size(k * 2.64f, k * 2.0f),
                style = Stroke(k * 0.2f, cap = StrokeCap.Round)
            )
            listOf(-1f, 1f).forEach { s ->
                val at = Offset(c.x + s * k * 1.22f, c.y - k * 0.1f)
                drawRoundRect(STEEL, Offset(at.x - k * 0.26f, at.y - k * 0.46f), Size(k * 0.52f, k * 0.92f), CornerRadius(k * 0.24f))
                drawRoundRect(body.dark(0.7f), Offset(at.x - k * 0.13f, at.y - k * 0.3f), Size(k * 0.26f, k * 0.6f), CornerRadius(k * 0.13f))
            }
        }
        "goggles" -> {
            drawLine(STEEL_D, Offset(c.x - k * 1.15f, c.y - k * 0.95f), Offset(c.x + k * 1.15f, c.y - k * 0.95f), k * 0.2f, cap = StrokeCap.Round)
            listOf(-1f, 1f).forEach { s ->
                val at = Offset(c.x + s * k * 0.46f, c.y - k * 0.98f)
                drawCircle(STEEL, k * 0.36f, at)
                drawCircle(glow.copy(alpha = 0.7f), k * 0.24f, at)
                drawCircle(STEEL_D, k * 0.36f, at, style = Stroke(k * 0.08f))
            }
        }
        else -> Unit
    }
}

/* ------- helpers de rasgos (relativos a un centro y a k) ------- */

/**
 * Ojos. "wide" y "surprised" llevan blanco alrededor del iris: es el único
 * rasgo que separa miedo y sorpresa del resto, que comparten todo lo demás.
 */
private fun DrawScope.drawEyeSet(kind: String, le: Offset, re: Offset, k: Float) {
    when (kind) {
        "wide" -> listOf(le, re).forEach {
            drawCircle(Color.White, k * 0.26f, it)
            drawCircle(OUTLINE, k * 0.26f, it, style = Stroke(k * 0.07f))
            drawCircle(EYE_C, k * 0.13f, it)
        }
        "happy" -> listOf(le, re).forEach {
            drawArc(EYE_C, 200f, 140f, false, Offset(it.x - k * 0.24f, it.y - k * 0.18f), Size(k * 0.48f, k * 0.48f), style = Stroke(k * 0.1f, cap = StrokeCap.Round))
        }
        "sleepy" -> listOf(le, re).forEach {
            drawLine(EYE_C, Offset(it.x - k * 0.22f, it.y), Offset(it.x + k * 0.22f, it.y), k * 0.1f, cap = StrokeCap.Round)
        }
        "surprised" -> listOf(le, re).forEach {
            drawCircle(Color.White, k * 0.23f, it)
            drawCircle(OUTLINE, k * 0.23f, it, style = Stroke(k * 0.07f))
            drawCircle(EYE_C, k * 0.11f, it)
        }
        else -> listOf(le, re).forEach { drawCircle(EYE_C, k * 0.16f, it) }
    }
}

private fun DrawScope.drawBrowSet(kind: String, lb: Offset, rb: Offset, midX: Float, color: Color, k: Float) {
    fun one(cc: Offset, inDy: Float, outDy: Float) {
        val inner = if (cc.x < midX) cc.x + k * 0.26f else cc.x - k * 0.26f
        val outer = if (cc.x < midX) cc.x - k * 0.26f else cc.x + k * 0.26f
        drawLine(color, Offset(inner, cc.y + inDy), Offset(outer, cc.y + outDy), k * 0.12f, cap = StrokeCap.Round)
    }
    when (kind) {
        "raised" -> { one(lb.copy(y = lb.y - k * 0.14f), 0f, 0f); one(rb.copy(y = rb.y - k * 0.14f), 0f, 0f) }
        "angry" -> { one(lb, k * 0.16f, -k * 0.1f); one(rb, k * 0.16f, -k * 0.1f) }
        "worried" -> { one(lb, -k * 0.14f, k * 0.11f); one(rb, -k * 0.14f, k * 0.11f) }
        else -> { one(lb, 0f, -k * 0.05f); one(rb, 0f, -k * 0.05f) }
    }
}

/**
 * Bocas. Las sonrisas van rellenas y no como arco fino: un contorno delgado se
 * pierde en la miniatura de Recientes.
 */
private fun DrawScope.drawMouthShape(kind: String, m: Offset, k: Float) {
    when (kind) {
        "grin" -> {
            val p = Path().apply {
                moveTo(m.x - k * 0.38f, m.y - k * 0.04f)
                quadraticTo(m.x, m.y + k * 0.56f, m.x + k * 0.38f, m.y - k * 0.04f)
                close()
            }
            drawPath(p, LIP_C)
            drawPath(p, OUTLINE, style = Stroke(k * 0.08f))
        }
        "neutral" -> drawLine(LIP_C, Offset(m.x - k * 0.32f, m.y), Offset(m.x + k * 0.32f, m.y), k * 0.12f, cap = StrokeCap.Round)
        "open" -> {
            drawOval(LIP_C, Offset(m.x - k * 0.22f, m.y - k * 0.2f), Size(k * 0.44f, k * 0.46f))
            drawOval(OUTLINE, Offset(m.x - k * 0.22f, m.y - k * 0.2f), Size(k * 0.44f, k * 0.46f), style = Stroke(k * 0.08f))
        }
        "sad" -> drawArc(LIP_C, 20f, 140f, false, Offset(m.x - k * 0.36f, m.y - k * 0.02f), Size(k * 0.72f, k * 0.62f), style = Stroke(k * 0.12f, cap = StrokeCap.Round))
        "smirk" -> drawPath(Path().apply {
            moveTo(m.x - k * 0.3f, m.y + k * 0.06f)
            quadraticTo(m.x + k * 0.1f, m.y + k * 0.3f, m.x + k * 0.34f, m.y - k * 0.12f)
        }, LIP_C, style = Stroke(k * 0.12f, cap = StrokeCap.Round))
        else -> {
            val p = Path().apply {
                moveTo(m.x - k * 0.34f, m.y - k * 0.06f)
                quadraticTo(m.x, m.y + k * 0.42f, m.x + k * 0.34f, m.y - k * 0.06f)
            }
            drawPath(p, LIP_C, style = Stroke(k * 0.13f, cap = StrokeCap.Round))
        }
    }
}

private fun DrawScope.drawHairShape(style: String, hair: Color, c: Offset, k: Float) {
    fun cap(bottomK: Float) = Path().apply {
        moveTo(c.x - k * 1.05f, c.y + k * bottomK)
        quadraticTo(c.x - k * 1.1f, c.y - k * 1.5f, c.x, c.y - k * 1.5f)
        quadraticTo(c.x + k * 1.1f, c.y - k * 1.5f, c.x + k * 1.05f, c.y + k * bottomK)
        quadraticTo(c.x + k * 0.6f, c.y - k * 0.7f, c.x, c.y - k * 0.7f)
        quadraticTo(c.x - k * 0.6f, c.y - k * 0.7f, c.x - k * 1.05f, c.y + k * bottomK)
        close()
    }
    when (style) {
        "buzz" -> drawPath(cap(-0.2f), hair.copy(alpha = 0.5f))
        "mohawk" -> drawPath(Path().apply {
            moveTo(c.x - k * 0.22f, c.y - k * 0.9f); quadraticTo(c.x, c.y - k * 2.2f, c.x + k * 0.22f, c.y - k * 0.9f); close()
        }, hair)
        "long" -> {
            drawRoundRect(hair, Offset(c.x - k * 1.15f, c.y - k * 0.9f), Size(k * 0.35f, k * 2.4f), CornerRadius(k * 0.15f))
            drawRoundRect(hair, Offset(c.x + k * 0.8f, c.y - k * 0.9f), Size(k * 0.35f, k * 2.4f), CornerRadius(k * 0.15f))
            drawPath(cap(0.1f), hair)
        }
        "curly" -> {
            listOf(-0.7f, -0.35f, 0f, 0.35f, 0.7f).forEach { drawCircle(hair, k * 0.4f, Offset(c.x + k * it, c.y - k * 1.1f)) }
            drawCircle(hair, k * 0.4f, Offset(c.x - k * 0.95f, c.y - k * 0.5f))
            drawCircle(hair, k * 0.4f, Offset(c.x + k * 0.95f, c.y - k * 0.5f))
        }
        "bun" -> { drawPath(cap(0.1f), hair); drawCircle(hair, k * 0.34f, Offset(c.x, c.y - k * 1.7f)) }
        "spiky" -> drawPath(Path().apply {
            moveTo(c.x - k, c.y - k * 0.6f)
            lineTo(c.x - k * 0.6f, c.y - k * 1.9f); lineTo(c.x - k * 0.2f, c.y - k * 0.9f)
            lineTo(c.x, c.y - k * 2.1f); lineTo(c.x + k * 0.2f, c.y - k * 0.9f)
            lineTo(c.x + k * 0.6f, c.y - k * 1.9f); lineTo(c.x + k, c.y - k * 0.6f)
            quadraticTo(c.x, c.y - k * 1.2f, c.x - k, c.y - k * 0.6f); close()
        }, hair)
        "side" -> drawPath(Path().apply {
            moveTo(c.x - k, c.y - k * 0.5f)
            quadraticTo(c.x - k * 0.7f, c.y - k * 1.7f, c.x + k, c.y - k * 1.2f)
            quadraticTo(c.x + k * 1.15f, c.y - k * 0.6f, c.x + k * 0.9f, c.y - k * 0.4f)
            quadraticTo(c.x + k * 0.1f, c.y - k * 1.1f, c.x - k * 0.5f, c.y - k * 0.5f)
            close()
        }, hair)
        else -> drawPath(cap(0.1f), hair)
    }
}

internal fun DrawScope.drawZzz(at: Offset, k: Float) {
    listOf(0f to 0f, 0.4f to -0.35f, 0.75f to -0.6f).forEach { (dx, dy) ->
        val a = Offset(at.x + k * dx, at.y + k * dy)
        val z = Path().apply {
            moveTo(a.x, a.y); lineTo(a.x + k * 0.3f, a.y)
            lineTo(a.x, a.y + k * 0.3f); lineTo(a.x + k * 0.3f, a.y + k * 0.3f)
        }
        drawPath(z, Color(0xFF6A6A78), style = Stroke(k * 0.06f))
    }
}

private fun DrawScope.drawRobotEyeSet(kind: String, le: Offset, re: Offset, visorTL: Offset, glow: Color, k: Float) {
    when (kind) {
        "square" -> listOf(le, re).forEach { drawRoundRect(glow, Offset(it.x - k * 0.16f, it.y - k * 0.16f), Size(k * 0.32f, k * 0.32f), CornerRadius(k * 0.06f)) }
        "line" -> listOf(le, re).forEach { drawLine(glow, Offset(it.x - k * 0.2f, it.y), Offset(it.x + k * 0.2f, it.y), k * 0.09f) }
        "happy" -> listOf(le, re).forEach { drawArc(glow, 200f, 140f, false, Offset(it.x - k * 0.2f, it.y - k * 0.15f), Size(k * 0.4f, k * 0.4f), style = Stroke(k * 0.09f)) }
        "visor" -> drawRoundRect(glow, visorTL, Size(k * 1.4f, k * 0.28f), CornerRadius(k * 0.14f))
        else -> listOf(le, re).forEach { drawCircle(glow, k * 0.18f, it) }
    }
}

private fun DrawScope.drawRobotMouthShape(kind: String, m: Offset, plate: Color, glow: Color, k: Float) {
    when (kind) {
        "smile" -> drawArc(glow, 200f, 140f, false, Offset(m.x - k * 0.36f, m.y - k * 0.28f), Size(k * 0.72f, k * 0.55f), style = Stroke(k * 0.09f))
        "line" -> drawLine(glow, Offset(m.x - k * 0.4f, m.y), Offset(m.x + k * 0.4f, m.y), k * 0.09f)
        "teeth" -> {
            drawRoundRect(glow, Offset(m.x - k * 0.4f, m.y - k * 0.14f), Size(k * 0.8f, k * 0.28f), CornerRadius(k * 0.04f))
            (1..3).forEach { i -> drawLine(plate, Offset(m.x - k * 0.4f + k * 0.2f * i, m.y - k * 0.14f), Offset(m.x - k * 0.4f + k * 0.2f * i, m.y + k * 0.14f), k * 0.05f) }
        }
        else -> (0..3).forEach { i -> drawLine(glow, Offset(m.x - k * 0.33f + k * 0.22f * i, m.y - k * 0.14f), Offset(m.x - k * 0.33f + k * 0.22f * i, m.y + k * 0.14f), k * 0.06f) }
    }
}

/** Segmento redondeado (brazo / pierna). */
internal fun DrawScope.limb(a: Offset, b: Offset, color: Color, width: Float) {
    drawLine(color, a, b, width, cap = StrokeCap.Round)
}
