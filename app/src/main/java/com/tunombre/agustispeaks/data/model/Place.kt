package com.tunombre.agustispeaks.data.model

/**
 * Lugar en el que el niño puede estar (Colegio, Living, Cocina, Baño, Patio,
 * Dormitorio, Hospital, Tienda…). Determina qué categorías y pictogramas se
 * muestran en el tablero.
 *
 * @param icon nombre lógico del icono (se resuelve en la capa de UI).
 * @param colorHex color asociado, en formato "#RRGGBB".
 * @param categoryIds categorías disponibles en este lugar.
 * @param photo foto que reemplaza al ícono, puesta en modo edición, o null.
 * @param isCustom creado en modo edición, no viene de la semilla.
 * @param isHidden ocultado en modo edición. Fuera de ese modo no se muestra.
 */
data class Place(
    val id: String,
    val name: String,
    val icon: String,
    val colorHex: String,
    val categoryIds: List<String>,
    val photo: String? = null,
    val isCustom: Boolean = false,
    val isHidden: Boolean = false
)
