package com.tunombre.agustispeaks.navigation

/**
 * Rutas de Navigation-Compose de la app.
 *
 * - `place_selection` -> elección de lugar
 * - `board/{placeId}` -> tablero horizontal a dos paneles (Palabras | Categorías,
 *                         el detalle de categoría reemplaza el panel derecho in-place)
 * - `recents`         -> pictogramas usados recientemente
 * - `settings`        -> configuración
 */
sealed class Destination(val route: String) {

    data object PlaceSelection : Destination("place_selection")

    data object Board : Destination("board/{placeId}") {
        const val ARG_PLACE_ID = "placeId"
        fun createRoute(placeId: String): String = "board/$placeId"
    }

    data object Recents : Destination("recents")

    data object Settings : Destination("settings")
}
