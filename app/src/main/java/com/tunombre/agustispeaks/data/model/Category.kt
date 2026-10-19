package com.tunombre.agustispeaks.data.model

/**
 * Categoría de pictogramas (Acciones, Comida, Personas, Sentimientos…).
 * Se usa para los chips de filtro dentro del tablero.
 *
 * @param icon nombre lógico del icono (se resuelve en la capa de UI).
 * @param colorHex color pastel propio de la categoría, en formato "#RRGGBB".
 * @param photo foto que reemplaza al ícono, puesta en modo edición, o null.
 * @param isCustom creada en modo edición, no viene de la semilla.
 * @param isHidden ocultada en modo edición. Fuera de ese modo no se muestra.
 */
data class Category(
    val id: String,
    val name: String,
    val icon: String,
    val colorHex: String,
    val photo: String? = null,
    val isCustom: Boolean = false,
    val isHidden: Boolean = false
)
