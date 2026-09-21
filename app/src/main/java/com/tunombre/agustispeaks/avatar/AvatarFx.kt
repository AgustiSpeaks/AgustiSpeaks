package com.tunombre.agustispeaks.avatar

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Símbolos que acompañan al avatar para que la acción se entienda sin leer la
 * frase: flechas, corazón, "?", "!", ✓/✗, reloj, copo de nieve…
 *
 * La mayoría se dibujan dentro de una "chapa" blanca junto a la cabeza, que es
 * lo que los hace legibles incluso en miniatura. Los que son ambientales
 * (líneas de velocidad, burbujas, gotas) se dibujan sobre el cuerpo.
 */

private val C_GREEN = Color(0xFF2E9E5B)
private val C_RED = Color(0xFFE23B3B)
private val C_BLUE = Color(0xFF3F8FD4)
private val C_ICE = Color(0xFF63C0E8)
private val C_PINK = Color(0xFFE8557F)
private val C_AMBER = Color(0xFFF2A900)
private val C_VIOLET = Color(0xFF6C5CE7)
private val C_TEAL = Color(0xFF13919B)
private val C_ORANGE = Color(0xFFE07A17)
private val C_SLATE = Color(0xFF4A6B8A)

internal fun DrawScope.drawFx(
    fx: Fx,
    pulse: Float,
    cx: Float,
    head: Offset,
    k: Float,
    shoulder: Float,
    hip: Float,
    ground: Float
) {
    if (fx == Fx.NONE) return

    val badgeC = Offset(cx + k * 2.1f, head.y - k * 1.05f)
    val r = k * 0.95f * pulse

    when (fx) {
        Fx.NONE -> Unit

        /* ---- ambientales: van sobre el cuerpo, sin chapa ---- */

        Fx.SPEED -> {
            val y0 = shoulder + (hip - shoulder) * 0.3f
            listOf(0f, 0.34f, 0.68f).forEachIndexed { i, f ->
                val y = y0 + (hip - shoulder) * f
                val len = k * (1.5f - 0.3f * i) * pulse
                drawLine(
                    C_SLATE.copy(alpha = 0.45f),
                    Offset(cx - k * 1.7f - len, y),
                    Offset(cx - k * 1.7f, y),
                    k * 0.14f,
                    cap = StrokeCap.Round
                )
            }
        }

        Fx.JUMP_ARC -> {
            val top = ground - (ground - shoulder) * 0.85f
            repeat(9) { i ->
                val f = i / 8f
                val x = cx - k * 2.2f + k * 4.4f * f
                val y = ground - k * 0.4f - (1f - (2f * f - 1f) * (2f * f - 1f)) * (ground - top) * 0.5f
                drawCircle(C_VIOLET.copy(alpha = 0.35f), k * 0.11f, Offset(x, y))
            }
        }

        Fx.BUBBLES -> {
            val bub = Color.White.copy(alpha = 0.75f)
            listOf(
                Triple(-1.6f, -0.4f, 0.26f), Triple(1.5f, -0.9f, 0.32f),
                Triple(-1.2f, -1.6f, 0.2f), Triple(1.8f, 0.3f, 0.22f),
                Triple(0.9f, -1.9f, 0.16f)
            ).forEach { (fx2, fy, fr) ->
                val c = Offset(cx + k * fx2, shoulder + k * fy - k * pulse * 0.3f)
                drawCircle(bub, k * fr, c)
                drawCircle(C_ICE, k * fr, c, style = Stroke(k * 0.05f))
            }
        }

        Fx.SWEAT -> listOf(-1.5f to -0.9f, 1.5f to -1.1f, 1.7f to 0.1f).forEach { (fx2, fy) ->
            drop(Offset(head.x + k * fx2, head.y + k * fy + k * 0.3f * pulse), k * 0.3f, C_ICE)
        }

        Fx.SPARKLE -> listOf(
            Triple(-2.0f, -1.2f, 0.42f), Triple(2.0f, -1.4f, 0.55f), Triple(1.6f, 0.4f, 0.3f)
        ).forEach { (fx2, fy, fr) ->
            sparkle(Offset(head.x + k * fx2, head.y + k * fy), k * fr * pulse, C_AMBER)
        }

        Fx.MUSIC -> {
            note(Offset(head.x + k * 1.7f, head.y - k * 0.9f - k * 0.2f * pulse), k * 0.55f, C_VIOLET)
            note(Offset(head.x + k * 2.5f, head.y - k * 1.7f - k * 0.3f * pulse), k * 0.42f, C_PINK)
        }

        Fx.SPEECH -> {
            val bl = Offset(head.x + k * 1.2f, head.y - k * 1.9f)
            drawRoundRect(Color.White, bl, Size(k * 1.9f, k * 1.1f), CornerRadius(k * 0.34f))
            drawRoundRect(C_SLATE, bl, Size(k * 1.9f, k * 1.1f), CornerRadius(k * 0.34f), style = Stroke(k * 0.1f))
            drawPath(Path().apply {
                moveTo(bl.x + k * 0.25f, bl.y + k * 1.0f)
                lineTo(bl.x + k * 0.05f, bl.y + k * 1.55f)
                lineTo(bl.x + k * 0.7f, bl.y + k * 1.05f)
                close()
            }, Color.White)
            repeat(3) { i ->
                val a = if (i == ((pulse * 3f).toInt() % 3)) 1f else 0.35f
                drawCircle(C_SLATE.copy(alpha = a), k * 0.14f, Offset(bl.x + k * (0.5f + 0.45f * i), bl.y + k * 0.55f))
            }
        }

        /* ---- con chapa blanca: símbolo grande y nítido ---- */

        else -> {
            badge(badgeC, r, glyphTint(fx))
            glyph(fx, badgeC, r * 0.62f)
        }
    }
}

/** Halo sobre la parte del cuerpo nombrada (dolor en rojo, señalar en azul). */
internal fun DrawScope.drawHighlight(at: Offset, k: Float, pain: Boolean, pulse: Float) {
    val c = if (pain) C_RED else C_BLUE
    val r = k * (0.55f + 0.18f * pulse)
    drawCircle(c.copy(alpha = 0.18f), r, at)
    drawCircle(c, r, at, style = Stroke(k * 0.11f))
}

/* ------------------------------ chapa + glifos ---------------------------- */

private fun DrawScope.badge(c: Offset, r: Float, tint: Color) {
    drawCircle(Color(0x33000000), r * 1.04f, Offset(c.x + r * 0.07f, c.y + r * 0.09f))
    drawCircle(Color.White, r, c)
    drawCircle(tint, r, c, style = Stroke(r * 0.15f))
}

private fun glyphTint(fx: Fx): Color = when (fx) {
    Fx.CHECK, Fx.PLUS -> C_GREEN
    Fx.CROSS, Fx.PAIN, Fx.STOP_SIGN, Fx.ANGER -> C_RED
    Fx.MINUS -> C_ORANGE
    Fx.COLD -> C_ICE
    Fx.HEART -> C_PINK
    Fx.QUESTION, Fx.EXCLAIM, Fx.CLOCK -> C_VIOLET
    Fx.ARROW_OUT, Fx.ARROW_IN -> C_TEAL
    Fx.TARGET -> C_ORANGE
    else -> C_SLATE
}

private fun DrawScope.glyph(fx: Fx, c: Offset, r: Float) {
    val tint = glyphTint(fx)
    val w = r * 0.34f
    when (fx) {
        Fx.PLUS -> {
            drawRoundRect(tint, Offset(c.x - r * 0.85f, c.y - w / 2f), Size(r * 1.7f, w), CornerRadius(w / 2f))
            drawRoundRect(tint, Offset(c.x - w / 2f, c.y - r * 0.85f), Size(w, r * 1.7f), CornerRadius(w / 2f))
        }
        Fx.MINUS ->
            drawRoundRect(tint, Offset(c.x - r * 0.85f, c.y - w / 2f), Size(r * 1.7f, w), CornerRadius(w / 2f))

        Fx.CHECK -> {
            drawLine(tint, Offset(c.x - r * 0.75f, c.y), Offset(c.x - r * 0.15f, c.y + r * 0.6f), w, cap = StrokeCap.Round)
            drawLine(tint, Offset(c.x - r * 0.15f, c.y + r * 0.6f), Offset(c.x + r * 0.8f, c.y - r * 0.6f), w, cap = StrokeCap.Round)
        }
        Fx.CROSS -> {
            drawLine(tint, Offset(c.x - r * 0.7f, c.y - r * 0.7f), Offset(c.x + r * 0.7f, c.y + r * 0.7f), w, cap = StrokeCap.Round)
            drawLine(tint, Offset(c.x + r * 0.7f, c.y - r * 0.7f), Offset(c.x - r * 0.7f, c.y + r * 0.7f), w, cap = StrokeCap.Round)
        }

        Fx.ARROW_OUT -> arrow(Offset(c.x - r * 0.8f, c.y), Offset(c.x + r * 0.85f, c.y), w, tint)
        Fx.ARROW_IN -> arrow(Offset(c.x + r * 0.85f, c.y), Offset(c.x - r * 0.8f, c.y), w, tint)

        Fx.HEART -> heart(c, r * 0.9f, tint)

        Fx.EXCLAIM -> {
            drawLine(tint, Offset(c.x, c.y - r * 0.85f), Offset(c.x, c.y + r * 0.2f), w, cap = StrokeCap.Round)
            drawCircle(tint, w * 0.55f, Offset(c.x, c.y + r * 0.72f))
        }
        Fx.QUESTION -> {
            drawArc(
                tint, 170f, 220f, false,
                Offset(c.x - r * 0.62f, c.y - r * 0.92f), Size(r * 1.24f, r * 1.24f),
                style = Stroke(w, cap = StrokeCap.Round)
            )
            drawLine(tint, Offset(c.x + r * 0.16f, c.y - r * 0.02f), Offset(c.x, c.y + r * 0.26f), w, cap = StrokeCap.Round)
            drawCircle(tint, w * 0.55f, Offset(c.x, c.y + r * 0.76f))
        }

        Fx.CLOCK -> {
            drawCircle(tint, r * 0.82f, c, style = Stroke(w * 0.75f))
            drawLine(tint, c, Offset(c.x, c.y - r * 0.5f), w * 0.6f, cap = StrokeCap.Round)
            drawLine(tint, c, Offset(c.x + r * 0.42f, c.y + r * 0.1f), w * 0.6f, cap = StrokeCap.Round)
        }

        Fx.STOP_SIGN -> {
            val p = Path()
            repeat(8) { i ->
                val a = (PI / 8f + i * PI / 4f).toFloat()
                val pt = Offset(c.x + r * 0.95f * cos(a), c.y + r * 0.95f * sin(a))
                if (i == 0) p.moveTo(pt.x, pt.y) else p.lineTo(pt.x, pt.y)
            }
            p.close()
            drawPath(p, tint)
            drawRoundRect(Color.White, Offset(c.x - r * 0.55f, c.y - w * 0.45f), Size(r * 1.1f, w * 0.9f), CornerRadius(w * 0.3f))
        }

        Fx.ANGER -> {
            listOf(-1f, 1f).forEach { s ->
                drawLine(tint, Offset(c.x + s * r * 0.75f, c.y - r * 0.7f), Offset(c.x + s * r * 0.1f, c.y - r * 0.08f), w * 0.8f, cap = StrokeCap.Round)
                drawLine(tint, Offset(c.x + s * r * 0.1f, c.y - r * 0.08f), Offset(c.x + s * r * 0.75f, c.y + r * 0.55f), w * 0.8f, cap = StrokeCap.Round)
            }
        }

        Fx.PAIN -> {
            val p = Path()
            val pts = 9
            repeat(pts * 2) { i ->
                val rr = if (i % 2 == 0) r * 0.95f else r * 0.42f
                val a = (i * PI / pts - PI / 2f).toFloat()
                val pt = Offset(c.x + rr * cos(a), c.y + rr * sin(a))
                if (i == 0) p.moveTo(pt.x, pt.y) else p.lineTo(pt.x, pt.y)
            }
            p.close()
            drawPath(p, tint)
        }

        Fx.COLD -> {
            repeat(3) { i ->
                val a = (i * PI / 3f).toFloat()
                val d = Offset(r * 0.88f * cos(a), r * 0.88f * sin(a))
                drawLine(tint, Offset(c.x - d.x, c.y - d.y), Offset(c.x + d.x, c.y + d.y), w * 0.8f, cap = StrokeCap.Round)
            }
            drawCircle(tint, r * 0.22f, c)
        }

        Fx.TARGET -> {
            drawCircle(tint, r * 0.9f, c, style = Stroke(w * 0.7f))
            drawCircle(tint, r * 0.4f, c)
            listOf(0f, 90f, 180f, 270f).forEach { deg ->
                val a = (deg * PI / 180f).toFloat()
                drawLine(
                    tint,
                    Offset(c.x + r * 0.9f * cos(a), c.y + r * 0.9f * sin(a)),
                    Offset(c.x + r * 1.3f * cos(a), c.y + r * 1.3f * sin(a)),
                    w * 0.6f, cap = StrokeCap.Round
                )
            }
        }

        else -> Unit
    }
}

/* -------------------------------- primitivas ------------------------------ */

private fun DrawScope.arrow(from: Offset, to: Offset, w: Float, color: Color) {
    val d = to - from
    val len = d.getDistance().coerceAtLeast(0.001f)
    val ux = d.x / len
    val uy = d.y / len
    val shaft = Offset(to.x - ux * w * 1.6f, to.y - uy * w * 1.6f)
    drawLine(color, from, shaft, w, cap = StrokeCap.Round)
    val b = Offset(to.x - ux * w * 2.4f, to.y - uy * w * 2.4f)
    drawPath(Path().apply {
        moveTo(to.x, to.y)
        lineTo(b.x - uy * w * 1.5f, b.y + ux * w * 1.5f)
        lineTo(b.x + uy * w * 1.5f, b.y - ux * w * 1.5f)
        close()
    }, color)
}

private fun DrawScope.heart(c: Offset, r: Float, color: Color) {
    drawPath(Path().apply {
        moveTo(c.x, c.y + r * 0.82f)
        cubicTo(c.x - r * 1.45f, c.y - r * 0.25f, c.x - r * 0.52f, c.y - r * 1.15f, c.x, c.y - r * 0.32f)
        cubicTo(c.x + r * 0.52f, c.y - r * 1.15f, c.x + r * 1.45f, c.y - r * 0.25f, c.x, c.y + r * 0.82f)
        close()
    }, color)
}

private fun DrawScope.sparkle(c: Offset, r: Float, color: Color) {
    drawPath(Path().apply {
        moveTo(c.x, c.y - r)
        quadraticTo(c.x + r * 0.17f, c.y - r * 0.17f, c.x + r, c.y)
        quadraticTo(c.x + r * 0.17f, c.y + r * 0.17f, c.x, c.y + r)
        quadraticTo(c.x - r * 0.17f, c.y + r * 0.17f, c.x - r, c.y)
        quadraticTo(c.x - r * 0.17f, c.y - r * 0.17f, c.x, c.y - r)
        close()
    }, color)
}

private fun DrawScope.drop(c: Offset, r: Float, color: Color) {
    drawPath(Path().apply {
        moveTo(c.x, c.y - r * 1.4f)
        quadraticTo(c.x + r * 1.15f, c.y + r * 0.35f, c.x, c.y + r)
        quadraticTo(c.x - r * 1.15f, c.y + r * 0.35f, c.x, c.y - r * 1.4f)
        close()
    }, color)
}

private fun DrawScope.note(c: Offset, r: Float, color: Color) {
    drawOval(color, Offset(c.x - r * 0.62f, c.y - r * 0.42f), Size(r * 1.24f, r * 0.9f))
    drawLine(color, Offset(c.x + r * 0.58f, c.y), Offset(c.x + r * 0.58f, c.y - r * 1.9f), r * 0.26f, cap = StrokeCap.Round)
    drawPath(Path().apply {
        moveTo(c.x + r * 0.58f, c.y - r * 1.9f)
        quadraticTo(c.x + r * 1.5f, c.y - r * 1.6f, c.x + r * 1.35f, c.y - r * 0.95f)
        quadraticTo(c.x + r * 1.15f, c.y - r * 1.35f, c.x + r * 0.58f, c.y - r * 1.35f)
        close()
    }, color)
}
