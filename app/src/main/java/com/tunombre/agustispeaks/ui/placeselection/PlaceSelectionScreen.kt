package com.tunombre.agustispeaks.ui.placeselection

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLocationAlt
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tunombre.agustispeaks.R
import com.tunombre.agustispeaks.data.catalog.Catalog
import com.tunombre.agustispeaks.data.model.EditableKind
import com.tunombre.agustispeaks.data.model.Place
import com.tunombre.agustispeaks.data.seed.SeedData
import com.tunombre.agustispeaks.ui.LocalAppViewModel
import com.tunombre.agustispeaks.ui.components.SavedPhoto
import com.tunombre.agustispeaks.ui.edit.AddTile
import com.tunombre.agustispeaks.ui.edit.CategoryChips
import com.tunombre.agustispeaks.ui.edit.EditItemDialog
import com.tunombre.agustispeaks.ui.edit.EditModeBanner
import com.tunombre.agustispeaks.ui.edit.EditableTile
import com.tunombre.agustispeaks.ui.edit.NEW_ITEM_COLORS
import com.tunombre.agustispeaks.ui.edit.editActions
import com.tunombre.agustispeaks.ui.icons.AppIcon
import com.tunombre.agustispeaks.ui.util.hexToColor
import com.tunombre.agustispeaks.ui.util.placeGridColumns

/** Qué formulario del modo edición está abierto en la elección de lugar. */
private sealed interface PlaceEditor {
    data object New : PlaceEditor
    data class Edit(val place: Place) : PlaceEditor
}

/**
 * Pantalla inicial: grilla adaptativa de lugares. Al tocar uno se navega al
 * tablero de ese lugar.
 *
 * En modo edición cada lugar lleva un lápiz para cambiarlo, y la grilla
 * termina en "Agregar lugar".
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceSelectionScreen(
    windowSizeClass: WindowSizeClass,
    onPlaceSelected: (String) -> Unit,
    onOpenRecents: () -> Unit,
    onOpenFavorites: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val appVm = LocalAppViewModel.current
    val catalog by appVm.catalog.collectAsStateWithLifecycle()
    val editing by appVm.editMode.collectAsStateWithLifecycle()
    val places = remember(catalog, editing) { catalog.places(includeHidden = editing) }
    val columns = placeGridColumns(windowSizeClass.widthSizeClass)

    var editor by remember { mutableStateOf<PlaceEditor?>(null) }

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
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            if (editing) EditModeBanner(onExit = appVm::exitEditMode)

            LazyVerticalGrid(
                columns = GridCells.Fixed(columns),
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(places, key = { it.id }) { place ->
                    EditableTile(
                        editing = editing,
                        hidden = place.isHidden,
                        name = place.name,
                        onEdit = { editor = PlaceEditor.Edit(place) }
                    ) {
                        PlaceCard(
                            place = place,
                            onClick = { onPlaceSelected(place.id) }
                        )
                    }
                }
                if (editing) {
                    item(key = "agregar") {
                        AddTile(
                            label = stringResource(R.string.place_add),
                            icon = Icons.Filled.AddLocationAlt,
                            aspectRatio = 1f,
                            onClick = { editor = PlaceEditor.New }
                        )
                    }
                }
            }
        }
    }

    // Si se sale del modo edición con un formulario abierto, el formulario se va.
    when (val open = editor.takeIf { editing }) {
        null -> Unit
        PlaceEditor.New -> PlaceForm(place = null, catalog = catalog, colorIndex = places.size, onClose = { editor = null })
        is PlaceEditor.Edit -> PlaceForm(place = open.place, catalog = catalog, colorIndex = 0, onClose = { editor = null })
    }
}

/**
 * Formulario de un lugar. Uno nuevo o propio elige color y qué categorías de
 * fábrica muestra; uno de fábrica solo cambia nombre y foto, o se oculta.
 */
@Composable
private fun PlaceForm(place: Place?, catalog: Catalog, colorIndex: Int, onClose: () -> Unit) {
    val appVm = LocalAppViewModel.current
    val ownsCategories = place == null || place.isCustom

    // Para uno nuevo, de entrada las categorías que tienen todos los lugares de
    // fábrica: lo que hace falta decir en cualquier parte.
    val everywhere = remember {
        SeedData.categories.filter { c -> SeedData.places.all { c.id in it.categoryIds } }.map { it.id }.toSet()
    }
    var chosen by remember { mutableStateOf(place?.categoryIds?.toSet() ?: everywhere) }
    val seedCategories = remember(catalog) { SeedData.categories.mapNotNull { catalog.category(it.id) } }

    EditItemDialog(
        title = if (place == null) stringResource(R.string.place_new_title)
        else stringResource(R.string.edit_title, place.name),
        initialName = place?.name.orEmpty(),
        initialPhoto = place?.photo,
        icon = place?.icon.orEmpty(),
        colorHex = place?.colorHex ?: NEW_ITEM_COLORS[colorIndex % NEW_ITEM_COLORS.size],
        photoRequired = ownsCategories,
        colors = if (ownsCategories) NEW_ITEM_COLORS else null,
        onDismiss = onClose,
        onSave = { name, photo, color ->
            // Las categorías en el orden de fábrica, no en el que se marcaron.
            val categoryIds = SeedData.categories.map { it.id }.filter { it in chosen }
            if (place == null) {
                if (photo != null) appVm.addPlace(name, photo, color, categoryIds)
            } else {
                appVm.editPlace(place, name, photo, color, categoryIds)
            }
            onClose()
        },
        actions = if (place == null) emptyList() else editActions(
            kind = EditableKind.PLACE,
            id = place.id,
            name = place.name,
            isCustom = place.isCustom,
            isHidden = place.isHidden,
            isChanged = catalog.isChanged(EditableKind.PLACE, place.id),
            deleteText = stringResource(R.string.place_delete_text),
            onDelete = { appVm.deletePlace(place) },
            onClose = onClose
        ),
        note = if (place != null && !place.isCustom) stringResource(R.string.edit_seed_note) else null,
        extra = if (ownsCategories) {
            {
                CategoryChips(
                    categories = seedCategories,
                    selected = chosen,
                    onToggle = { id -> chosen = if (id in chosen) chosen - id else chosen + id }
                )
            }
        } else {
            null
        }
    )
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
            val photo = place.photo
            if (photo != null) {
                // La foto usa todo el alto que deja el nombre.
                BoxWithConstraints(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    SavedPhoto(
                        name = photo,
                        contentDescription = null,
                        maxSide = 512,
                        modifier = Modifier.size(minOf(maxWidth, maxHeight)).clip(RoundedCornerShape(14.dp))
                    )
                }
            } else {
                AppIcon(
                    name = place.icon,
                    modifier = Modifier.size(64.dp)
                )
            }
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
