package com.tunombre.agustispeaks.ui.board

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tunombre.agustispeaks.R
import com.tunombre.agustispeaks.data.catalog.Catalog
import com.tunombre.agustispeaks.data.model.Category
import com.tunombre.agustispeaks.data.model.EditableKind
import com.tunombre.agustispeaks.data.model.Pictogram
import com.tunombre.agustispeaks.phrase.LocalPhraseController
import com.tunombre.agustispeaks.phrase.tapPictogram
import com.tunombre.agustispeaks.tts.LocalTtsManager
import com.tunombre.agustispeaks.ui.LocalAppViewModel
import com.tunombre.agustispeaks.ui.components.CATEGORY_CARD_RATIO
import com.tunombre.agustispeaks.ui.components.CategoryCard
import com.tunombre.agustispeaks.ui.components.PhrasePanel
import com.tunombre.agustispeaks.ui.components.PictogramGrid
import com.tunombre.agustispeaks.ui.edit.AddTile
import com.tunombre.agustispeaks.ui.edit.EditItemDialog
import com.tunombre.agustispeaks.ui.edit.EditModeBanner
import com.tunombre.agustispeaks.ui.edit.EditableTile
import com.tunombre.agustispeaks.ui.edit.NEW_ITEM_COLORS
import com.tunombre.agustispeaks.ui.edit.editActions
import com.tunombre.agustispeaks.ui.util.adaptiveColumns
import com.tunombre.agustispeaks.ui.util.hexToColor
import com.tunombre.agustispeaks.validacion.Validacion

private const val CORE_WORD_HEX = "#E6ECF2"
private val CoreWordColor = Color(0xFFE6ECF2)

/** Qué formulario del modo edición está abierto en el tablero. */
private sealed interface BoardEditor {
    data class NewPictogram(val category: Category) : BoardEditor
    data class EditPictogram(val pictogram: Pictogram, val colorHex: String) : BoardEditor
    data object NewCategory : BoardEditor
    data class EditCategory(val category: Category) : BoardEditor
}

/**
 * Tablero de un lugar, en horizontal, a dos paneles:
 * - Izquierda: "Palabras" núcleo (iguales en todos los lugares).
 * - Derecha: "Categorías" del lugar; al tocar una, el mismo panel cambia
 *   in-place a los pictogramas de esa categoría (con flecha para volver).
 *
 * Al tocar cualquier pictograma se añade a la frase y se lee por TTS la frase
 * completa construida hasta ese momento (QUEUE_FLUSH corta lo que suene).
 *
 * En modo edición funciona igual, y además cada palabra, categoría y
 * pictograma lleva un lápiz para cambiarlo, y las grillas de categorías y de
 * pictogramas terminan en "Agregar".
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoardScreen(
    onBack: () -> Unit,
    onOpenRecents: () -> Unit,
    onOpenFavorites: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BoardViewModel = viewModel()
) {
    val phraseController = LocalPhraseController.current
    val tts = LocalTtsManager.current
    val appVm = LocalAppViewModel.current
    val catalog by appVm.catalog.collectAsStateWithLifecycle()
    val editing by appVm.editMode.collectAsStateWithLifecycle()
    val selectedId by viewModel.selectedCategoryId.collectAsStateWithLifecycle()
    val placeId = viewModel.placeId

    val place = remember(catalog, placeId) { catalog.place(placeId) }
    val coreWords = remember(catalog, editing) { catalog.coreWords(includeHidden = editing) }
    val categories = remember(catalog, placeId, editing) { catalog.categoriesFor(placeId, includeHidden = editing) }
    // La categoría abierta, mientras siga existiendo y a la vista: si se borra,
    // o se oculta y se sale del modo edición, el panel vuelve solo a la lista.
    val category = categories.firstOrNull { it.id == selectedId }
    val pictograms = remember(catalog, category, editing) {
        category?.let { catalog.pictogramsFor(it.id, includeHidden = editing) }.orEmpty()
    }

    var editor by remember { mutableStateOf<BoardEditor?>(null) }

    // La regla central —la oración se acumula y se dice entera— vive en
    // `tapPictogram`, fuera de la pantalla, para que la prueba del criterio 9
    // verifique este mismo código.
    fun onTapPictogram(pictogram: Pictogram) {
        tapPictogram(
            text = pictogram.text,
            phrase = phraseController,
            speak = tts::speak,
            record = appVm::recordPhrase,
            // Medición de tesis: arranca el cronómetro de la latencia. Ver
            // `Validacion`; en builds de release no hace nada.
            onPress = Validacion::pulsacion
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(place?.name ?: stringResource(R.string.board_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onOpenRecents) {
                        Icon(Icons.Default.History, stringResource(R.string.cd_open_recents))
                    }
                    IconButton(onClick = onOpenFavorites) {
                        Icon(Icons.Default.Star, stringResource(R.string.cd_open_favorites))
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Default.Settings, stringResource(R.string.cd_open_settings))
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {

            if (editing) EditModeBanner(onExit = appVm::exitEditMode)

            PhrasePanel()

            Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                // Panel izquierdo: Palabras (fijo, no cambia). En modo edición se
                // pueden cambiar u ocultar, pero no se agregan: es el panel que
                // tiene que poder recorrerse de un vistazo.
                Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    PanelHeader(stringResource(R.string.tab_words))
                    BoxWithConstraints(modifier = Modifier.weight(1f)) {
                        PictogramGrid(
                            pictograms = coreWords,
                            columns = adaptiveColumns(maxWidth),
                            containerColorFor = { CoreWordColor },
                            onPictogramClick = ::onTapPictogram,
                            modifier = Modifier.fillMaxSize(),
                            editing = editing,
                            onEdit = { editor = BoardEditor.EditPictogram(it, CORE_WORD_HEX) }
                        )
                    }
                }

                VerticalDivider()

                // Panel derecho: Categorías, o el detalle de la categoría elegida.
                Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    if (category == null) {
                        PanelHeader(stringResource(R.string.tab_categories))
                        BoxWithConstraints(modifier = Modifier.weight(1f)) {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(adaptiveColumns(maxWidth, itemSize = 118.dp)),
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(categories, key = { it.id }) { cat ->
                                    EditableTile(
                                        editing = editing,
                                        hidden = cat.isHidden,
                                        name = cat.name,
                                        onEdit = { editor = BoardEditor.EditCategory(cat) }
                                    ) {
                                        CategoryCard(
                                            category = cat,
                                            onClick = { viewModel.selectCategory(cat.id) }
                                        )
                                    }
                                }
                                if (editing) {
                                    item(key = "agregar") {
                                        AddTile(
                                            label = stringResource(R.string.category_add),
                                            icon = Icons.Filled.CreateNewFolder,
                                            aspectRatio = CATEGORY_CARD_RATIO,
                                            onClick = { editor = BoardEditor.NewCategory }
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { viewModel.selectCategory(null) }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = stringResource(R.string.cd_back)
                                )
                            }
                            Text(
                                text = category.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        val cardColor = hexToColor(category.colorHex)
                        BoxWithConstraints(modifier = Modifier.weight(1f)) {
                            PictogramGrid(
                                pictograms = pictograms,
                                columns = adaptiveColumns(maxWidth),
                                containerColorFor = { cardColor },
                                onPictogramClick = ::onTapPictogram,
                                modifier = Modifier.fillMaxSize(),
                                editing = editing,
                                onEdit = { editor = BoardEditor.EditPictogram(it, category.colorHex) },
                                onAdd = if (editing) {
                                    { editor = BoardEditor.NewPictogram(category) }
                                } else {
                                    null
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Si se sale del modo edición con un formulario abierto, el formulario se va.
    val open = editor.takeIf { editing }
    val close = { editor = null }
    when (open) {
        null -> Unit
        is BoardEditor.NewPictogram -> EditItemDialog(
            title = stringResource(R.string.custom_title, open.category.name),
            initialName = "",
            initialPhoto = null,
            icon = "",
            colorHex = open.category.colorHex,
            photoRequired = true,
            nameHint = stringResource(R.string.custom_name_hint),
            onDismiss = close,
            onSave = { name, photo, _ ->
                if (photo != null) appVm.addCustomPictogram(open.category.id, name, photo)
                close()
            }
        )
        is BoardEditor.EditPictogram -> PictogramEditor(open.pictogram, open.colorHex, catalog, close)
        BoardEditor.NewCategory -> EditItemDialog(
            title = stringResource(R.string.category_new_title, place?.name.orEmpty()),
            initialName = "",
            initialPhoto = null,
            icon = "",
            colorHex = NEW_ITEM_COLORS[categories.size % NEW_ITEM_COLORS.size],
            photoRequired = true,
            colors = NEW_ITEM_COLORS,
            onDismiss = close,
            onSave = { name, photo, color ->
                if (photo != null) appVm.addCategory(placeId, name, photo, color)
                close()
            }
        )
        is BoardEditor.EditCategory -> CategoryEditor(open.category, catalog, close)
    }
}

@Composable
private fun PictogramEditor(pictogram: Pictogram, colorHex: String, catalog: Catalog, onClose: () -> Unit) {
    val appVm = LocalAppViewModel.current
    EditItemDialog(
        title = stringResource(R.string.edit_title, pictogram.text),
        initialName = pictogram.text,
        initialPhoto = pictogram.photo,
        icon = pictogram.icon,
        colorHex = colorHex,
        photoRequired = pictogram.isCustom,
        nameHint = stringResource(R.string.custom_name_hint),
        onDismiss = onClose,
        onSave = { name, photo, _ ->
            appVm.editPictogram(pictogram, name, photo)
            onClose()
        },
        actions = editActions(
            kind = EditableKind.PICTOGRAM,
            id = pictogram.id,
            name = pictogram.text,
            isCustom = pictogram.isCustom,
            isHidden = pictogram.isHidden,
            isChanged = catalog.isChanged(EditableKind.PICTOGRAM, pictogram.id),
            deleteText = stringResource(R.string.custom_delete_text),
            onDelete = { appVm.removeCustomPictogram(pictogram) },
            onClose = onClose
        ),
        note = if (pictogram.isCustom) null else stringResource(R.string.edit_seed_note)
    )
}

@Composable
private fun CategoryEditor(category: Category, catalog: Catalog, onClose: () -> Unit) {
    val appVm = LocalAppViewModel.current
    EditItemDialog(
        title = stringResource(R.string.edit_title, category.name),
        initialName = category.name,
        initialPhoto = category.photo,
        icon = category.icon,
        colorHex = category.colorHex,
        photoRequired = category.isCustom,
        // El color de una de fábrica no se cambia: lo comparte en todos los lugares.
        colors = if (category.isCustom) NEW_ITEM_COLORS else null,
        onDismiss = onClose,
        onSave = { name, photo, color ->
            appVm.editCategory(category, name, photo, color)
            onClose()
        },
        actions = editActions(
            kind = EditableKind.CATEGORY,
            id = category.id,
            name = category.name,
            isCustom = category.isCustom,
            isHidden = category.isHidden,
            isChanged = catalog.isChanged(EditableKind.CATEGORY, category.id),
            deleteText = stringResource(R.string.category_delete_text),
            onDelete = { appVm.deleteCategory(category) },
            onClose = onClose
        ),
        note = if (category.isCustom) null else stringResource(R.string.category_seed_note)
    )
}

@Composable
private fun PanelHeader(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 12.dp, top = 8.dp, bottom = 4.dp)
    )
}
