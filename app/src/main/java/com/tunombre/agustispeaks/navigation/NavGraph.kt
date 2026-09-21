package com.tunombre.agustispeaks.navigation

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
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
        composable(Destination.PlaceSelection.route) {
            PlaceSelectionScreen(
                windowSizeClass = windowSizeClass,
                onPlaceSelected = { placeId ->
                    phraseController.clear()
                    navController.navigate(Destination.Board.createRoute(placeId))
                },
                onOpenRecents = { navController.navigate(Destination.Recents.route) },
                onOpenSettings = { navController.navigate(Destination.Settings.route) }
            )
        }

        composable(
            route = Destination.Board.route,
            arguments = listOf(
                navArgument(Destination.Board.ARG_PLACE_ID) { type = NavType.StringType }
            )
        ) {
            BoardScreen(
                onBack = { navController.popBackStack() },
                onOpenRecents = { navController.navigate(Destination.Recents.route) },
                onOpenSettings = { navController.navigate(Destination.Settings.route) }
            )
        }

        composable(Destination.Recents.route) {
            RecentsScreen(onBack = { navController.popBackStack() })
        }

        composable(Destination.Settings.route) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
