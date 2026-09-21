package com.tunombre.agustispeaks.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/** Esquinas muy redondeadas: coherente con botones grandes y táctiles. */
val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(36.dp)
)

private val PastelColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,
    secondary = Secondary,
    onSecondary = OnSecondary,
    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = OnSecondaryContainer,
    tertiary = Tertiary,
    onTertiary = OnTertiary,
    tertiaryContainer = TertiaryContainer,
    onTertiaryContainer = OnTertiaryContainer,
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    outline = OutlineLight,
    error = ErrorLight,
    onError = OnErrorLight
)

private val DarkScheme = darkColorScheme(
    primary = Color(0xFF9EC3FF),
    onPrimary = Color(0xFF07305F),
    primaryContainer = Color(0xFF274A77),
    onPrimaryContainer = Color(0xFFD6E4FF),
    secondary = Color(0xFF9FD79E),
    onSecondary = Color(0xFF0B3D14),
    secondaryContainer = Color(0xFF2C5330),
    onSecondaryContainer = Color(0xFFD9F0D8),
    tertiary = Color(0xFFF0B7D6),
    onTertiary = Color(0xFF44122F),
    background = Color(0xFF15130F),
    onBackground = Color(0xFFEDE3DA),
    surface = Color(0xFF1E1B17),
    onSurface = Color(0xFFEDE3DA),
    surfaceVariant = Color(0xFF3A342E),
    onSurfaceVariant = Color(0xFFCFC5BB),
    outline = Color(0xFF998F85),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005)
)

private val HighContrastLightScheme = lightColorScheme(
    primary = HcPrimary,
    onPrimary = HcOnPrimary,
    primaryContainer = HcPrimary,
    onPrimaryContainer = HcOnPrimary,
    secondary = HcSecondary,
    onSecondary = HcOnPrimary,
    secondaryContainer = HcSurfaceVariant,
    onSecondaryContainer = HcOnSurface,
    tertiary = HcSecondary,
    onTertiary = HcOnPrimary,
    background = HcBackground,
    onBackground = HcOnBackground,
    surface = HcSurface,
    onSurface = HcOnSurface,
    surfaceVariant = HcSurfaceVariant,
    onSurfaceVariant = HcOnSurfaceVariant,
    outline = HcOutline,
    error = HcError,
    onError = HcOnPrimary
)

private val HighContrastDarkScheme = darkColorScheme(
    primary = Color(0xFFFFEB3B),
    onPrimary = Color(0xFF000000),
    primaryContainer = Color(0xFFFFEB3B),
    onPrimaryContainer = Color(0xFF000000),
    secondary = Color(0xFF80DEEA),
    onSecondary = Color(0xFF000000),
    background = Color(0xFF000000),
    onBackground = Color(0xFFFFFFFF),
    surface = Color(0xFF000000),
    onSurface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFF1F1F1F),
    onSurfaceVariant = Color(0xFFFFFFFF),
    outline = Color(0xFFFFFFFF),
    error = Color(0xFFFF8A80),
    onError = Color(0xFF000000)
)

/**
 * Tema Material 3 de la app.
 *
 * @param darkTheme fuerza el tema oscuro (desde Ajustes). Si es null usa el
 *        ajuste del sistema.
 * @param highContrast usa el esquema de alto contraste.
 */
@Composable
fun AgustiSpeaksTheme(
    darkTheme: Boolean? = null,
    highContrast: Boolean = false,
    content: @Composable () -> Unit
) {
    val isDark = darkTheme ?: isSystemInDarkTheme()

    val colorScheme = when {
        highContrast && isDark -> HighContrastDarkScheme
        highContrast -> HighContrastLightScheme
        isDark -> DarkScheme
        else -> PastelColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
