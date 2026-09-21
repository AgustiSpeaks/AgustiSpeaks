package com.tunombre.agustispeaks.ui.placeselection

import androidx.lifecycle.ViewModel
import com.tunombre.agustispeaks.data.model.Place
import com.tunombre.agustispeaks.data.seed.SeedData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class PlaceSelectionUiState(
    val places: List<Place> = emptyList()
)

/**
 * Expone la lista de lugares. Por ahora lee directamente de [SeedData];
 * cuando exista el repositorio + Room, se inyectará y combinará con los
 * lugares/pictogramas personalizados.
 */
class PlaceSelectionViewModel : ViewModel() {

    private val _state = MutableStateFlow(PlaceSelectionUiState(places = SeedData.places))
    val state: StateFlow<PlaceSelectionUiState> = _state.asStateFlow()
}
