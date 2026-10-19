package com.tunombre.agustispeaks.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.tunombre.agustispeaks.R
import com.tunombre.agustispeaks.data.model.Pictogram
import com.tunombre.agustispeaks.ui.edit.AddTile
import com.tunombre.agustispeaks.ui.edit.EditableTile

/**
 * Grilla adaptativa de pictogramas. Reutilizada por el panel "Palabras" del
 * tablero y por el detalle de categoría.
 *
 * @param editing modo edición: cada pictograma lleva su lápiz ([onEdit]) y los
 *        ocultos se ven atenuados.
 * @param onAdd si no es null, la grilla termina con una tarjeta "Agregar" para
 *        crear un pictograma propio. Solo se pasa en modo edición.
 */
@Composable
fun PictogramGrid(
    pictograms: List<Pictogram>,
    columns: Int,
    containerColorFor: (Pictogram) -> Color,
    onPictogramClick: (Pictogram) -> Unit,
    modifier: Modifier = Modifier,
    editing: Boolean = false,
    onEdit: (Pictogram) -> Unit = {},
    onAdd: (() -> Unit)? = null
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(pictograms, key = { it.id }) { pictogram ->
            EditableTile(
                editing = editing,
                hidden = pictogram.isHidden,
                name = pictogram.text,
                onEdit = { onEdit(pictogram) }
            ) {
                PictogramButton(
                    pictogram = pictogram,
                    containerColor = containerColorFor(pictogram),
                    onClick = { onPictogramClick(pictogram) }
                )
            }
        }
        if (onAdd != null) {
            item(key = "agregar") {
                AddTile(
                    label = stringResource(R.string.custom_add),
                    icon = Icons.Filled.AddPhotoAlternate,
                    aspectRatio = 1f,
                    onClick = onAdd
                )
            }
        }
    }
}
