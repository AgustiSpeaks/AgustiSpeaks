package com.tunombre.agustispeaks.avatar

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import com.tunombre.agustispeaks.data.model.AvatarConfig
import com.tunombre.agustispeaks.data.model.AvatarKind

/**
 * Retrato (busto) del avatar: cabeza + hombros. Se usa en el editor y en la
 * barra de frase. Para las animaciones de cuerpo completo ver
 * [drawAvatarFigure].
 */
fun DrawScope.drawAvatar(config: AvatarConfig, pose: AvatarPose = AvatarPose()) {
    val s = size.minDimension
    val cx = size.width / 2f
    val headCenter = Offset(cx, size.height * 0.44f)
    val k = s * 0.26f

    translate(left = pose.lean * s * 0.03f, top = -pose.bob * s * 0.03f) {
        if (config.kind == AvatarKind.ROBOT) {
            // Chasis con el color de "ropa"; hombreras y cuello de acero.
            drawRoundRect(
                avColor(config.clothingColor),
                Offset(cx - s * 0.42f, size.height * 0.82f),
                Size(s * 0.84f, size.height),
                CornerRadius(s * 0.12f)
            )
            listOf(-1f, 1f).forEach { side ->
                drawRoundRect(
                    avColor(config.robotColor),
                    Offset(cx + side * s * 0.42f - s * 0.16f, size.height * 0.80f),
                    Size(s * 0.32f, s * 0.2f),
                    CornerRadius(s * 0.09f)
                )
            }
            drawLine(STEEL_D, Offset(cx, size.height * 0.68f), Offset(cx, size.height * 0.84f), s * 0.11f)
            drawLine(STEEL, Offset(cx, size.height * 0.68f), Offset(cx, size.height * 0.84f), s * 0.06f)
        } else {
            val cloth = avColor(config.clothingColor)
            val torso = Path().apply {
                moveTo(cx - s * 0.42f, size.height)
                lineTo(cx - s * 0.42f, size.height * 0.9f)
                quadraticTo(cx, size.height * 0.6f, cx + s * 0.42f, size.height * 0.9f)
                lineTo(cx + s * 0.42f, size.height)
                close()
            }
            drawLine(avColor(config.skinColor), Offset(cx, size.height * 0.62f), Offset(cx, size.height * 0.80f), s * 0.11f)
            drawPath(torso, cloth)
            drawPath(torso, OUTLINE, style = Stroke(s * 0.018f))
        }

        rotate(degrees = pose.headTilt, pivot = headCenter) {
            if (config.kind == AvatarKind.ROBOT) robotHead(config, pose, headCenter, k)
            else personHead(config, pose, headCenter, k)
        }
    }
}
