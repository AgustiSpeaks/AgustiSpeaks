package com.tunombre.agustispeaks.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tunombre.agustispeaks.R
import com.tunombre.agustispeaks.data.model.Pictogram

/**
 * Grilla adaptativa de pictogramas. Reutilizada por la pestaña "Palabras" del
 * tablero y por el detalle de categoría.
 *
 * @param onAdd si no es null, la grilla termina con una tarjeta "Agregar" para
 *        crear un pictograma propio.
 * @param onCustomLongClick mantener apretado un pictograma propio. Los de la
 *        semilla no reaccionan al toque largo: no se pueden borrar.
 */
@Composable
fun PictogramGrid(
    pictograms: List<Pictogram>,
    columns: Int,
    containerColorFor: (Pictogram) -> Color,
    onPictogramClick: (Pictogram) -> Unit,
    modifier: Modifier = Modifier,
    onAdd: (() -> Unit)? = null,
    onCustomLongClick: ((Pictogram) -> Unit)? = null
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
                onClick = { onPictogramClick(pictogram) },
                onLongClick = if (pictogram.isCustom && onCustomLongClick != null) {
                    { onCustomLongClick(pictogram) }
                } else {
                    null
                }
            )
        }
        if (onAdd != null) {
            item(key = "agregar") { AddPictogramTile(onClick = onAdd) }
        }
    }
}

/**
 * La tarjeta para crear un pictograma. Del mismo tamaño que un pictograma pero
 * sin relleno, solo con el borde: se distingue a simple vista de una palabra, y
 * nadie la toca creyendo que va a decir algo.
 */
@Composable
private fun AddPictogramTile(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.primary,
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.aspectRatio(1f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 6.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically)
        ) {
            BoxWithConstraints(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                // Más chico que un ícono de pictograma, para que no compita con ellos.
                val side = minOf(maxWidth, maxHeight) * 0.6f
                if (side > 14.dp) {
                    Icon(Icons.Filled.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(side))
                }
            }
            Text(
                text = stringResource(R.string.custom_add),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
