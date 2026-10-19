package com.tunombre.agustispeaks.ui.board

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.tunombre.agustispeaks.navigation.Destination
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Estado del tablero de un lugar, mostrado en horizontal a dos paneles:
 * - Izquierda: palabras núcleo (iguales en todos los lugares).
 * - Derecha: categorías del lugar; al tocar una, ese mismo panel cambia
 *   in-place a los pictogramas de esa categoría ([selectCategory]).
 *
 * Solo guarda qué lugar es y qué categoría está abierta. Las listas salen del
 * catálogo de [com.tunombre.agustispeaks.ui.AppViewModel], que suma lo hecho en
 * modo edición: así un cambio se ve en el tablero apenas se guarda.
 *
 * El `placeId` llega como argumento de navegación (SavedStateHandle).
 * La frase en construcción NO vive aquí: es compartida
 * ([com.tunombre.agustispeaks.phrase.PhraseController]).
 */
class BoardViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    val placeId: String =
        savedStateHandle.get<String>(Destination.Board.ARG_PLACE_ID).orEmpty()

    private val _selectedCategoryId = MutableStateFlow<String?>(null)
    val selectedCategoryId: StateFlow<String?> = _selectedCategoryId.asStateFlow()

    /** Abre (o cierra, con null) el detalle de una categoría en el panel derecho. */
    fun selectCategory(categoryId: String?) {
        _selectedCategoryId.value = categoryId
    }
}
