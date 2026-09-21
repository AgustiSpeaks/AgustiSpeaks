package com.tunombre.agustispeaks.data.model

/**
 * Lugar en el que el niño puede estar (Colegio, Living, Cocina, Baño, Patio,
 * Dormitorio, Hospital, Tienda…). Determina qué categorías y pictogramas se
 * muestran en el tablero.
 *
 * @param icon nombre lógico del icono (se resuelve en la capa de UI).
 * @param colorHex color asociado, en formato "#RRGGBB".
 * @param categoryIds categorías disponibles en este lugar.
 */
data class Place(
    val id: String,
    val name: String,
    val icon: String,
    val colorHex: String,
    val categoryIds: List<String>
)
