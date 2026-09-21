package com.tunombre.agustispeaks.ui.theme

import androidx.compose.ui.graphics.Color

/* ---------------------------------------------------------------------------
 *  Paleta base pastel (esquema normal)
 * ------------------------------------------------------------------------- */

val Primary = Color(0xFF5B8DEF)
val OnPrimary = Color(0xFFFFFFFF)
val PrimaryContainer = Color(0xFFD6E4FF)
val OnPrimaryContainer = Color(0xFF0C2A5B)

val Secondary = Color(0xFF7FB77E)
val OnSecondary = Color(0xFFFFFFFF)
val SecondaryContainer = Color(0xFFD9F0D8)
val OnSecondaryContainer = Color(0xFF16351A)

val Tertiary = Color(0xFFE59FC4)
val OnTertiary = Color(0xFF3E1229)
val TertiaryContainer = Color(0xFFFBDDEC)
val OnTertiaryContainer = Color(0xFF3E1229)

val BackgroundLight = Color(0xFFFFF7F2)
val OnBackgroundLight = Color(0xFF231A16)
val SurfaceLight = Color(0xFFFFFFFF)
val OnSurfaceLight = Color(0xFF231A16)
val SurfaceVariantLight = Color(0xFFEDE5E0)
val OnSurfaceVariantLight = Color(0xFF4E453F)
val OutlineLight = Color(0xFF837B75)

val ErrorLight = Color(0xFFB3261E)
val OnErrorLight = Color(0xFFFFFFFF)

/* ---------------------------------------------------------------------------
 *  Esquema de alto contraste (accesibilidad)
 * ------------------------------------------------------------------------- */

val HcPrimary = Color(0xFF0033CC)
val HcOnPrimary = Color(0xFFFFFFFF)
val HcSecondary = Color(0xFF00504A)
val HcBackground = Color(0xFFFFFFFF)
val HcOnBackground = Color(0xFF000000)
val HcSurface = Color(0xFFFFFFFF)
val HcOnSurface = Color(0xFF000000)
val HcSurfaceVariant = Color(0xFFE0E0E0)
val HcOnSurfaceVariant = Color(0xFF000000)
val HcOutline = Color(0xFF000000)
val HcError = Color(0xFFB3261E)

/* ---------------------------------------------------------------------------
 *  Colores por categoría (pastel, uno distinto por categoría).
 *  Se asignan por índice o por id de categoría desde la capa de datos.
 * ------------------------------------------------------------------------- */

val CategoryColors: List<Color> = listOf(
    Color(0xFFFFB3BA), // rojo pastel
    Color(0xFFFFD9A8), // naranja pastel
    Color(0xFFFFF3B0), // amarillo pastel
    Color(0xFFB5EAD7), // verde menta
    Color(0xFFA8D8EA), // celeste
    Color(0xFFC7CEEA), // lavanda
    Color(0xFFE2C6F0), // violeta pastel
    Color(0xFFF6C6D8), // rosa pastel
    Color(0xFFD0E8C5), // verde lima suave
    Color(0xFFF0DAB1)  // arena
)

/** Devuelve un color de categoría de forma estable a partir de un id. */
fun categoryColorFor(id: String): Color =
    CategoryColors[(id.hashCode().mod(CategoryColors.size))]
