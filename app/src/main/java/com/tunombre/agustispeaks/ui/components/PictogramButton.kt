package com.tunombre.agustispeaks.ui.components

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.tunombre.agustispeaks.data.model.Pictogram
import com.tunombre.agustispeaks.ui.LocalUserSettings
import com.tunombre.agustispeaks.ui.icons.appIcon

private const val REPEAT_TAP_WINDOW_MS = 700L

/**
 * Botón de pictograma: icono grande arriba, texto en negrita debajo, sobre el
 * color de su categoría. Cuadrado y táctil.
 *
 * Respeta los ajustes de accesibilidad:
 * - tamaño (escala) según `UserSettings.textSize`
 * - "Activar al soltar" vs. activar al tocar
 * - "Ignorar toques repetidos" (anti rebote)
 */
@Composable
fun PictogramButton(
    pictogram: Pictogram,
    containerColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val settings = LocalUserSettings.current
    val scale = settings.textSize.scale

    val contentColor = if (containerColor.luminance() > 0.5f) Color(0xFF1B1B1B) else Color(0xFFFFFFFF)

    var lastFire by remember { mutableLongStateOf(0L) }
    fun fire() {
        val now = System.currentTimeMillis()
        if (settings.ignoreRepeatedTaps && now - lastFire < REPEAT_TAP_WINDOW_MS) return
        lastFire = now
        onClick()
    }

    val shapeModifier = modifier.aspectRatio(1f)

    if (settings.activateOnRelease) {
        Surface(
            onClick = { fire() },
            shape = MaterialTheme.shapes.large,
            color = containerColor,
            contentColor = contentColor,
            tonalElevation = 2.dp,
            shadowElevation = 2.dp,
            modifier = shapeModifier
        ) { PictogramContent(pictogram, scale) }
    } else {
        Surface(
            shape = MaterialTheme.shapes.large,
            color = containerColor,
            contentColor = contentColor,
            tonalElevation = 2.dp,
            shadowElevation = 2.dp,
            modifier = shapeModifier.pointerInput(pictogram.id) {
                detectTapGestures(onPress = { fire() })
            }
        ) { PictogramContent(pictogram, scale) }
    }
}

@Composable
private fun PictogramContent(pictogram: Pictogram, scale: Float) {
    val textStyle = MaterialTheme.typography.titleMedium
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 6.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically)
    ) {
        // La palabra manda: se mide primero y el icono se queda con lo que
        // sobra. Con un icono de tamaño fijo el contenido superaba la altura de
        // la tarjeta y, al estar centrado, se recortaba arriba y abajo.
        BoxWithConstraints(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            val side = minOf(maxWidth, maxHeight)
            if (side > 14.dp) {
                Icon(
                    imageVector = appIcon(pictogram.icon),
                    contentDescription = null,
                    modifier = Modifier.size(side)
                )
            }
        }
        Text(
            text = pictogram.text,
            style = textStyle.copy(fontSize = textStyle.fontSize * scale),
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
