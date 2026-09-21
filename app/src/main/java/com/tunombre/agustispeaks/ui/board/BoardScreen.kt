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
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tunombre.agustispeaks.R
import com.tunombre.agustispeaks.data.model.Pictogram
import com.tunombre.agustispeaks.phrase.LocalPhraseController
import com.tunombre.agustispeaks.tts.LocalTtsManager
import com.tunombre.agustispeaks.ui.LocalAppViewModel
import com.tunombre.agustispeaks.ui.components.CategoryCard
import com.tunombre.agustispeaks.ui.components.PhrasePanel
import com.tunombre.agustispeaks.ui.components.PictogramGrid
import com.tunombre.agustispeaks.ui.util.adaptiveColumns
import com.tunombre.agustispeaks.ui.util.hexToColor

private val CoreWordColor = Color(0xFFE6ECF2)

/**
 * Tablero de un lugar, en horizontal, a dos paneles:
 * - Izquierda: "Palabras" núcleo (iguales en todos los lugares).
 * - Derecha: "Categorías" del lugar; al tocar una, el mismo panel cambia
 *   in-place a los pictogramas de esa categoría (con flecha para volver).
 *
 * Al tocar cualquier pictograma se añade a la frase y se lee por TTS la frase
 * completa construida hasta ese momento (QUEUE_FLUSH corta lo que suene).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoardScreen(
    onBack: () -> Unit,
    onOpenRecents: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BoardViewModel = viewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val phraseController = LocalPhraseController.current
    val tts = LocalTtsManager.current
    val appVm = LocalAppViewModel.current

    fun onTapPictogram(pictogram: Pictogram) {
        val phrase = phraseController.addWord(pictogram.text)
        tts.speak(phrase)
        appVm.recordPhrase(phrase)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(state.placeName.ifBlank { stringResource(R.string.board_title) }) },
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
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Default.Settings, stringResource(R.string.cd_open_settings))
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {

            PhrasePanel()

            Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                // Panel izquierdo: Palabras (fijo, no cambia).
                Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    PanelHeader(stringResource(R.string.tab_words))
                    BoxWithConstraints(modifier = Modifier.weight(1f)) {
                        PictogramGrid(
                            pictograms = state.coreWords,
                            columns = adaptiveColumns(maxWidth),
                            containerColorFor = { CoreWordColor },
                            onPictogramClick = ::onTapPictogram,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                VerticalDivider()

                // Panel derecho: Categorías, o el detalle de la categoría elegida.
                Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    val category = state.selectedCategory
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
                                items(state.categories, key = { it.id }) { cat ->
                                    CategoryCard(
                                        category = cat,
                                        onClick = { viewModel.selectCategory(cat.id) }
                                    )
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
                                pictograms = state.selectedCategoryPictograms,
                                columns = adaptiveColumns(maxWidth),
                                containerColorFor = { cardColor },
                                onPictogramClick = ::onTapPictogram,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }
    }
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
