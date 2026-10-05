package com.tunombre.agustispeaks.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tunombre.agustispeaks.data.model.Pictogift
import com.tunombre.agustispeaks.phrase.LocalPhraseController
import com.tunombre.agustispeaks.tts.LocalTtsManager
import com.tunombre.agustispeaks.ui.LocalAppViewModel

/**
 * Barra de frase + lógica de "Decir": al tocar Decir se relee la frase por
 * TTS, se guarda en Recientes y se abre el overlay con el avatar animado
 * haciendo la acción.
 */
@Composable
fun PhrasePanel(modifier: Modifier = Modifier) {
    val phraseController = LocalPhraseController.current
    val tts = LocalTtsManager.current
    val appVm = LocalAppViewModel.current
    val words by phraseController.words.collectAsStateWithLifecycle()
    val phrase = words.joinToString(" ")

    var overlay by remember { mutableStateOf<Pictogift?>(null) }

    PhraseBar(
        phrase = phrase,
        onSay = {
            if (phrase.isNotBlank()) {
                tts.speak(phrase)
                appVm.recordPhrase(phrase)
                overlay = Pictogift.fromText(phrase)
            }
        },
        onClear = {
            tts.stop()
            phraseController.clear()
        },
        modifier = modifier
    )

    overlay?.let { gift ->
        SpeakingOverlay(
            gift = gift,
            onDismiss = { overlay = null }
        )
    }
}
