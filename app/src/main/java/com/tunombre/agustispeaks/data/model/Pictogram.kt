package com.tunombre.agustispeaks.data.model

/**
 * Pictograma / palabra que se añade a la frase en construcción al tocarlo.
 *
 * @param text palabra o expresión que se lee por TTS y se muestra bajo el icono.
 * @param icon nombre lógico del icono (se resuelve en la capa de UI).
 * @param categoryId categoría a la que pertenece.
 * @param placeIds lugares en los que aparece este pictograma.
 * @param isFavorite marcado como favorito por el usuario.
 * @param isCustom creado por el usuario desde Ajustes (no viene de la semilla).
 * @param gifUrl (uso futuro) GIF asociado que se mostrará al seleccionarlo.
 *        Por ahora solo se almacena; no hay lógica de reproducción todavía.
 */
data class Pictogram(
    val id: String,
    val text: String,
    val icon: String,
    val categoryId: String,
    val placeIds: List<String>,
    val isFavorite: Boolean = false,
    val isCustom: Boolean = false,
    val gifUrl: String? = null
)
