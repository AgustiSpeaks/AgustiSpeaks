package com.tunombre.agustispeaks.data.model

/**
 * Pictograma / palabra que se añade a la frase en construcción al tocarlo.
 *
 * @param text palabra o expresión que se lee por TTS y se muestra bajo el icono.
 * @param icon nombre lógico del icono (se resuelve en la capa de UI).
 * @param categoryId categoría a la que pertenece.
 * @param placeIds lugares en los que aparece este pictograma.
 * @param isFavorite marcado como favorito por el usuario.
 * @param isCustom creado por quien usa la app, desde la propia categoría (no
 *        viene de la semilla). Se ve en todos los lugares que muestran su
 *        categoría, por eso [placeIds] queda vacío.
 * @param gifUrl (uso futuro) GIF asociado que se mostrará al seleccionarlo.
 *        Por ahora solo se almacena; no hay lógica de reproducción todavía.
 * @param photo foto que reemplaza al ícono: nombre del archivo en
 *        [com.tunombre.agustispeaks.data.repository.UserPhotos], o null.
 * @param isHidden ocultado en modo edición. Fuera de ese modo no se muestra.
 */
data class Pictogram(
    val id: String,
    val text: String,
    val icon: String,
    val categoryId: String,
    val placeIds: List<String>,
    val isFavorite: Boolean = false,
    val isCustom: Boolean = false,
    val gifUrl: String? = null,
    val photo: String? = null,
    val isHidden: Boolean = false
)
