package com.tunombre.agustispeaks.ui.board

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.tunombre.agustispeaks.data.model.Category
import com.tunombre.agustispeaks.data.model.Pictogram
import com.tunombre.agustispeaks.data.seed.SeedData
import com.tunombre.agustispeaks.navigation.Destination
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class BoardUiState(
    val placeName: String = "",
    val coreWords: List<Pictogram> = emptyList(),
    val categories: List<Category> = emptyList(),
    val selectedCategory: Category? = null,
    val selectedCategoryPictograms: List<Pictogram> = emptyList()
)

/**
 * Estado del tablero de un lugar, mostrado en horizontal a dos paneles:
 * - Izquierda: palabras núcleo (iguales en todos los lugares).
 * - Derecha: categorías del lugar; al tocar una, ese mismo panel cambia
 *   in-place a los pictogramas de esa categoría ([selectCategory]).
 *
 * El `placeId` llega como argumento de navegación (SavedStateHandle).
 * La frase en construcción NO vive aquí: es compartida
 * ([com.tunombre.agustispeaks.phrase.PhraseController]).
 */
class BoardViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    private val placeId: String =
        savedStateHandle.get<String>(Destination.Board.ARG_PLACE_ID).orEmpty()

    private val _state = MutableStateFlow(
        BoardUiState(
            placeName = SeedData.placeById(placeId)?.name.orEmpty(),
            coreWords = SeedData.coreWords,
            categories = SeedData.categoriesForPlace(placeId)
        )
    )
    val state: StateFlow<BoardUiState> = _state.asStateFlow()

    /** Abre (o cierra, con null) el detalle de una categoría en el panel derecho. */
    fun selectCategory(categoryId: String?) {
        val category = categoryId?.let { SeedData.categoryById(it) }
        _state.value = _state.value.copy(
            selectedCategory = category,
            selectedCategoryPictograms = if (category != null) {
                SeedData.pictogramsFor(placeId, category.id)
            } else {
                emptyList()
            }
        )
    }
}
