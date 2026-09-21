package com.tunombre.agustispeaks.data.model

/**
 * Tamaño de los pictogramas (icono + texto) configurable desde Ajustes.
 * [scale] multiplica el tamaño base.
 */
enum class TextSizeOption(val label: String, val scale: Float) {
    NORMAL("Normal", 1.0f),
    GRANDE("Grande", 1.15f),
    MAS_GRANDE("Más grande", 1.3f),
    EXTRA("Extra", 1.5f),
    MAXIMO("Máximo", 1.75f)
}
