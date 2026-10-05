package com.tunombre.agustispeaks.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * Grosor de brazos y piernas. Del orden del de los íconos de Material, para
 * que la figura tenga el mismo peso que las de las otras tarjetas.
 */
private const val MIEMBRO = 2.6f

/** El color da igual: el ícono se tiñe entero al dibujarse, como los de Material. */
private val Tinta = SolidColor(Color.Black)

/**
 * Ícono de la categoría Juego: un chico arrodillado empujando un autito.
 *
 * Silueta sólida de un solo color, como el resto de los íconos de la app. Las
 * piezas se separan por huecos y no por contornos: la cabeza flota sobre los
 * hombros, y las ruedas quedan apenas debajo de la carrocería, que es como se
 * lee un auto en un pictograma de un color.
 *
 * Grilla de 24 × 24, como los íconos de Material.
 */
internal val NinoJugando: ImageVector by lazy {
    ImageVector.Builder(
        name = "NinoJugando",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        // Cabeza, separada del cuerpo.
        path(fill = Tinta) { circulo(12.2f, 4.4f, 2.6f) }

        // Pierna doblada (sentado sobre los talones) y torso inclinado hacia el auto.
        miembro {
            moveTo(2.4f, 20.4f)  // pie
            lineTo(10.6f, 20.4f) // rodilla
            lineTo(5.6f, 18.2f)  // cadera
            lineTo(9.3f, 9.4f)   // hombros
        }

        // Brazo estirado hacia el auto.
        miembro {
            moveTo(9.8f, 10.8f)
            lineTo(14.2f, 15.2f)
        }

        // Autito: carrocería con cabina, y las ruedas un poco más abajo.
        path(fill = Tinta) {
            moveTo(15.0f, 19.0f)
            lineTo(15.0f, 18.6f)
            quadTo(15.0f, 17.6f, 16.0f, 17.6f)
            lineTo(16.6f, 17.6f)
            curveTo(16.8f, 15.2f, 20.0f, 15.2f, 20.2f, 17.6f)
            lineTo(21.4f, 17.6f)
            quadTo(22.4f, 17.6f, 22.4f, 18.6f)
            lineTo(22.4f, 19.0f)
            close()
        }
        path(fill = Tinta) { circulo(17.0f, 20.7f, 1.3f) }
        path(fill = Tinta) { circulo(20.4f, 20.7f, 1.3f) }
    }.build()
}

private fun ImageVector.Builder.miembro(linea: PathBuilder.() -> Unit) {
    path(
        stroke = Tinta,
        strokeLineWidth = MIEMBRO,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round,
        pathBuilder = linea
    )
}

private fun PathBuilder.circulo(cx: Float, cy: Float, r: Float) {
    moveTo(cx - r, cy)
    arcTo(r, r, 0f, true, true, cx + r, cy)
    arcTo(r, r, 0f, true, true, cx - r, cy)
    close()
}
