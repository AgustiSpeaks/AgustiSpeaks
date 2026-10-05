package com.tunombre.agustispeaks.navigation

/**
 * Rutas de Navigation-Compose de la app.
 *
 * - `place_selection` -> elección de lugar
 * - `board/{placeId}` -> tablero horizontal a dos paneles (Palabras | Categorías,
 *                         el detalle de categoría reemplaza el panel derecho in-place)
 * - `recents`         -> Recientes y Favoritos, en dos pestañas
 * - `settings`        -> configuración
 */
sealed class Destination(val route: String) {

    data object PlaceSelection : Destination("place_selection")

    data object Board : Destination("board/{placeId}") {
        const val ARG_PLACE_ID = "placeId"
        fun createRoute(placeId: String): String = "board/$placeId"
    }

    /**
     * Una sola pantalla con dos pestañas. El botón de Favoritos de la barra
     * superior abre esta misma pantalla, directamente en la pestaña Favoritos.
     */
    data object Recents : Destination("recents?favoritos={favoritos}") {
        const val ARG_FAVORITES = "favoritos"
        fun createRoute(favorites: Boolean = false): String = "recents?favoritos=$favorites"
    }

    data object Settings : Destination("settings")
}
