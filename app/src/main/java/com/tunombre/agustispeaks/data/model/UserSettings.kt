package com.tunombre.agustispeaks.data.model

/**
 * Configuración de la app. Se persiste con DataStore (Preferences).
 *
 * @param interfaceLanguage idioma de la interfaz (por ahora solo "es").
 * @param ignoreRepeatedTaps ignora toques repetidos muy seguidos sobre el
 *        mismo pictograma (evita dobles activaciones accidentales).
 * @param activateOnRelease si true, el pictograma se activa al soltar el dedo;
 *        si false, se activa al tocar (respuesta inmediata).
 * @param darkMode fuerza el tema oscuro.
 * @param highContrast usa el esquema de alto contraste.
 * @param textSize tamaño de los pictogramas.
 * @param voicePitch tono de voz del TTS (1.0 = normal).
 * @param voiceRate velocidad del habla del TTS (1.0 = normal).
 * @param voiceVolume volumen del TTS (0.0 a 1.0).
 * @param voiceId id/nombre de la voz del sistema elegida (null = por defecto).
 * @param gridColumns nº de columnas de la grilla forzado por el usuario;
 *        si es null se calcula según el tamaño de pantalla.
 */
data class UserSettings(
    val interfaceLanguage: String = "es",
    val ignoreRepeatedTaps: Boolean = false,
    val activateOnRelease: Boolean = true,
    val darkMode: Boolean = false,
    val highContrast: Boolean = false,
    val textSize: TextSizeOption = TextSizeOption.NORMAL,
    val voicePitch: Float = 1.0f,
    val voiceRate: Float = 1.0f,
    val voiceVolume: Float = 1.0f,
    val voiceId: String? = null,
    val gridColumns: Int? = null
)
