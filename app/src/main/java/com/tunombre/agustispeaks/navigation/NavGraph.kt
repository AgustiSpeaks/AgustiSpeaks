package com.tunombre.agustispeaks.navigation

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.tunombre.agustispeaks.phrase.LocalPhraseController
import com.tunombre.agustispeaks.ui.board.BoardScreen
import com.tunombre.agustispeaks.ui.placeselection.PlaceSelectionScreen
import com.tunombre.agustispeaks.ui.recents.RecentsScreen
import com.tunombre.agustispeaks.ui.settings.SettingsScreen

/**
 * Grafo de navegación principal.
 *
 * **Toda acción de navegación pasa por [ifActive].** Sin eso, tocar "volver"
 * dos veces rápido sacaba también la pantalla de elección de lugar, y la app
 * quedaba en blanco: sin ninguna pantalla en la pila. Lo mismo con tocar dos
 * veces un lugar, que apilaba dos tableros.
 *
 * @param windowSizeClass se propaga a las pantallas para adaptar las grillas
 *        al tamaño real de pantalla.
 */
@Composable
fun NavGraph(
    navController: NavHostController,
    windowSizeClass: WindowSizeClass,
    startDestination: String = Destination.PlaceSelection.route
) {
    val phraseController = LocalPhraseController.current

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Destination.PlaceSelection.route) { entry ->
            PlaceSelectionScreen(
                windowSizeClass = windowSizeClass,
                onPlaceSelected = { placeId ->
                    ifActive(entry) {
                        phraseController.clear()
                        navController.navigate(Destination.Board.createRoute(placeId))
                    }
                },
                onOpenRecents = { ifActive(entry) { navController.navigate(Destination.Recents.createRoute()) } },
                onOpenFavorites = {
                    ifActive(entry) { navController.navigate(Destination.Recents.createRoute(favorites = true)) }
                },
                onOpenSettings = { ifActive(entry) { navController.navigate(Destination.Settings.route) } }
            )
        }

        composable(
            route = Destination.Board.route,
            arguments = listOf(
                navArgument(Destination.Board.ARG_PLACE_ID) { type = NavType.StringType }
            )
        ) { entry ->
            BoardScreen(
                onBack = { ifActive(entry) { navController.popBackStack() } },
                onOpenRecents = { ifActive(entry) { navController.navigate(Destination.Recents.createRoute()) } },
                onOpenFavorites = {
                    ifActive(entry) { navController.navigate(Destination.Recents.createRoute(favorites = true)) }
                },
                onOpenSettings = { ifActive(entry) { navController.navigate(Destination.Settings.route) } }
            )
        }

        composable(
            route = Destination.Recents.route,
            arguments = listOf(
                navArgument(Destination.Recents.ARG_FAVORITES) {
                    type = NavType.BoolType
                    defaultValue = false
                }
            )
        ) { entry ->
            RecentsScreen(
                startOnFavorites = entry.arguments?.getBoolean(Destination.Recents.ARG_FAVORITES) ?: false,
                onBack = { ifActive(entry) { navController.popBackStack() } }
            )
        }

        composable(Destination.Settings.route) { entry ->
            SettingsScreen(onBack = { ifActive(entry) { navController.popBackStack() } })
        }
    }
}

/**
 * Ejecuta [action] solo si [entry] es la pantalla activa.
 *
 * Una pantalla deja de estar activa en el instante en que se navega desde ella
 * —antes de que termine la transición—, y la que llega recién lo está cuando
 * su transición termina. Así, el segundo toque de un doble toque, o un toque en
 * plena transición, se descarta en vez de navegar desde una pantalla que ya se
 * está yendo.
 */
private inline fun ifActive(entry: NavBackStackEntry, action: () -> Unit) {
    if (entry.lifecycle.currentState == Lifecycle.State.RESUMED) action()
}
