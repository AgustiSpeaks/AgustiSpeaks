package com.tunombre.agustispeaks.data.model

/**
 * Categoría de pictogramas (Acciones, Comida, Personas, Sentimientos…).
 * Se usa para los chips de filtro dentro del tablero.
 *
 * @param icon nombre lógico del icono (se resuelve en la capa de UI).
 * @param colorHex color pastel propio de la categoría, en formato "#RRGGBB".
 */
data class Category(
    val id: String,
    val name: String,
    val icon: String,
    val colorHex: String
)
