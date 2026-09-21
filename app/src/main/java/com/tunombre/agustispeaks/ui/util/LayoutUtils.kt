package com.tunombre.agustispeaks.ui.util

import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Nº de columnas de la grilla de lugares según el ancho real de pantalla. */
fun placeGridColumns(width: WindowWidthSizeClass): Int = when (width) {
    WindowWidthSizeClass.Expanded -> 4
    WindowWidthSizeClass.Medium -> 3
    else -> 2
}

/**
 * Nº de columnas según el ancho real disponible (en dp), no el ancho de toda
 * la pantalla. Se usa para paneles que ocupan solo una parte del ancho (p. ej.
 * el tablero horizontal, con Palabras y Categorías lado a lado).
 */
fun adaptiveColumns(maxWidth: Dp, itemSize: Dp = 108.dp, min: Int = 2, max: Int = 5): Int =
    (maxWidth / itemSize).toInt().coerceIn(min, max)

/** Convierte "#RRGGBB" en [Color]. Devuelve gris claro si el formato es inválido. */
fun hexToColor(hex: String): Color = runCatching {
    Color(android.graphics.Color.parseColor(hex))
}.getOrDefault(Color(0xFFE0E0E0))
