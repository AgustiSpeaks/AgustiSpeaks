package com.tunombre.agustispeaks.tts

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Provee la instancia única de [TtsManager] a toda la jerarquía de Compose.
 * Se inicializa en MainActivity con:
 *
 * ```
 * CompositionLocalProvider(LocalTtsManager provides ttsManager) { ... }
 * ```
 */
val LocalTtsManager = staticCompositionLocalOf<TtsManager> {
    error("No se ha proporcionado un TtsManager mediante CompositionLocalProvider(LocalTtsManager provides ...).")
}
