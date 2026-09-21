package com.tunombre.agustispeaks.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.tunombre.agustispeaks.data.model.Pictogram

/**
 * Grilla adaptativa de pictogramas. Reutilizada por la pestaña "Palabras" del
 * tablero y por el detalle de categoría.
 */
@Composable
fun PictogramGrid(
    pictograms: List<Pictogram>,
    columns: Int,
    containerColorFor: (Pictogram) -> Color,
    onPictogramClick: (Pictogram) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(pictograms, key = { it.id }) { pictogram ->
            PictogramButton(
                pictogram = pictogram,
                containerColor = containerColorFor(pictogram),
                onClick = { onPictogramClick(pictogram) }
            )
        }
    }
}
