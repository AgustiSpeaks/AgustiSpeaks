package com.tunombre.agustispeaks.ui.placeselection

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tunombre.agustispeaks.R
import com.tunombre.agustispeaks.data.model.Place
import com.tunombre.agustispeaks.ui.icons.AppIcon
import com.tunombre.agustispeaks.ui.util.hexToColor
import com.tunombre.agustispeaks.ui.util.placeGridColumns

/**
 * Pantalla inicial: grilla adaptativa de lugares. Al tocar uno se navega al
 * tablero de ese lugar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceSelectionScreen(
    windowSizeClass: WindowSizeClass,
    onPlaceSelected: (String) -> Unit,
    onOpenRecents: () -> Unit,
    onOpenFavorites: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PlaceSelectionViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val columns = placeGridColumns(windowSizeClass.widthSizeClass)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.place_selection_title)) },
                actions = {
                    IconButton(onClick = onOpenRecents) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = stringResource(R.string.cd_open_recents)
                        )
                    }
                    IconButton(onClick = onOpenFavorites) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = stringResource(R.string.cd_open_favorites)
                        )
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(R.string.cd_open_settings)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(state.places, key = { it.id }) { place ->
                PlaceCard(
                    place = place,
                    onClick = { onPlaceSelected(place.id) }
                )
            }
        }
    }
}

@Composable
private fun PlaceCard(
    place: Place,
    onClick: () -> Unit
) {
    val container = hexToColor(place.colorHex)
    val content = if (container.luminance() > 0.5f) Color(0xFF1B1B1B) else Color(0xFFFFFFFF)

    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        color = container,
        contentColor = content,
        tonalElevation = 2.dp,
        shadowElevation = 3.dp,
        modifier = Modifier.aspectRatio(1f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AppIcon(
                name = place.icon,
                modifier = Modifier.size(64.dp)
            )
            Text(
                text = place.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 10.dp)
            )
        }
    }
}
