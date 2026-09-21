package com.tunombre.agustispeaks.phrase

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Estado de la frase en construcción, compartido por todas las pantallas
 * que usan [com.tunombre.agustispeaks.ui.components.PhrasePanel] (tablero,
 * recientes, etc.).
 *
 * Se instancia como ViewModel con ámbito de Activity (sobrevive a rotaciones)
 * y se expone a Compose mediante [LocalPhraseController].
 */
class PhraseController : ViewModel() {

    private val _words = MutableStateFlow<List<String>>(emptyList())
    val words: StateFlow<List<String>> = _words.asStateFlow()

    /** Frase completa actual, palabras separadas por espacio. */
    fun currentPhrase(): String = _words.value.joinToString(" ")

    /**
     * Añade [word] al final de la frase y devuelve la frase completa
     * resultante, para reproducirla entera por TTS.
     */
    fun addWord(word: String): String {
        _words.value = _words.value + word
        return currentPhrase()
    }

    /** Elimina la última palabra. */
    fun removeLast() {
        _words.value = _words.value.dropLast(1)
    }

    /** Reemplaza la frase entera (p. ej. al elegir una de "Recientes"). */
    fun setPhrase(phrase: String) {
        _words.value = phrase.split(" ").filter { it.isNotBlank() }
    }

    /** Limpia la frase por completo. */
    fun clear() {
        _words.value = emptyList()
    }
}
