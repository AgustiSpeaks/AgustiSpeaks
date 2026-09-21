package com.tunombre.agustispeaks.phrase

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Provee la instancia compartida de [PhraseController] a toda la app.
 * Se inicializa en MainActivity.
 */
val LocalPhraseController = staticCompositionLocalOf<PhraseController> {
    error("No se ha proporcionado un PhraseController mediante CompositionLocalProvider(LocalPhraseController provides ...).")
}
