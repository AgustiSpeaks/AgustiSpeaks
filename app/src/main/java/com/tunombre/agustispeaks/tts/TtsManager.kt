package com.tunombre.agustispeaks.tts

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import com.tunombre.agustispeaks.validacion.Validacion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import java.util.UUID

/**
 * Wrapper único sobre [TextToSpeech].
 *
 * Debe existir **una sola instancia viva** en toda la app (creada en
 * [com.tunombre.agustispeaks.MainActivity] y expuesta con [LocalTtsManager])
 * para no crear varios motores de síntesis de voz.
 *
 * Reproduce siempre con [TextToSpeech.QUEUE_FLUSH]: si llega una nueva frase
 * mientras se está hablando, se corta la anterior y empieza la nueva. Las
 * lecturas **no se encolan**.
 */
class TtsManager(context: Context) {

    private val appContext: Context = context.applicationContext

    private var tts: TextToSpeech? = null

    private var currentVolume: Float = 1.0f

    private val _state = MutableStateFlow(TtsState())
    val state: StateFlow<TtsState> = _state.asStateFlow()

    init {
        tts = TextToSpeech(appContext) { status ->
            val ok = status == TextToSpeech.SUCCESS
            if (ok) {
                tts?.language = resolveSpanishLocale()
                tts?.setOnUtteranceProgressListener(progressListener)
            }
            _state.value = _state.value.copy(isReady = ok, initFailed = !ok)
        }
    }

    private val progressListener = object : UtteranceProgressListener() {
        override fun onStart(utteranceId: String?) {
            _state.value = _state.value.copy(isSpeaking = true)
            // Medición de tesis. Este es el instante en que empieza a salir
            // sonido, que es el otro extremo de la latencia. Ver `Validacion`.
            Validacion.vozIniciada(utteranceId)
        }

        override fun onDone(utteranceId: String?) {
            _state.value = _state.value.copy(isSpeaking = false)
        }

        override fun onStop(utteranceId: String?, interrupted: Boolean) {
            _state.value = _state.value.copy(isSpeaking = false)
        }

        @Deprecated("Sustituido por onError(utteranceId, errorCode); se mantiene por compatibilidad")
        override fun onError(utteranceId: String?) {
            _state.value = _state.value.copy(isSpeaking = false)
        }

        override fun onError(utteranceId: String?, errorCode: Int) {
            _state.value = _state.value.copy(isSpeaking = false)
        }
    }

    /**
     * Habla el [text] completo recibido, cortando cualquier reproducción en
     * curso (QUEUE_FLUSH). Pensado para recibir la frase entera construida
     * hasta el momento, no solo la última palabra.
     */
    fun speak(text: String) {
        val engine = tts ?: return
        val clean = text.trim()
        if (clean.isEmpty()) return
        val params = Bundle().apply {
            putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, currentVolume.coerceIn(0f, 1f))
        }
        val locucion = UUID.randomUUID().toString()
        // Medición de tesis: ata la pulsación a esta locución, para poder
        // descartar el `onStart` de la que QUEUE_FLUSH acaba de cortar.
        Validacion.locucionCreada(locucion)
        engine.speak(clean, TextToSpeech.QUEUE_FLUSH, params, locucion)
    }

    /** Corta inmediatamente la reproducción actual. */
    fun stop() {
        tts?.stop()
        _state.value = _state.value.copy(isSpeaking = false)
    }

    /** Tono de voz (0.5 = grave, 2.0 = agudo). */
    fun setPitch(pitch: Float) {
        tts?.setPitch(pitch.coerceIn(0.5f, 2.0f))
    }

    /** Velocidad del habla (0.5 = lento, 2.0 = rápido). */
    fun setSpeechRate(rate: Float) {
        tts?.setSpeechRate(rate.coerceIn(0.5f, 2.0f))
    }

    /** Volumen de reproducción (0.0 a 1.0). Se aplica en cada [speak]. */
    fun setVolume(volume: Float) {
        currentVolume = volume.coerceIn(0f, 1f)
    }

    /** Aplica de golpe la configuración de voz guardada en Ajustes. */
    fun applySettings(pitch: Float, rate: Float, volume: Float, voiceId: String?) {
        setPitch(pitch)
        setSpeechRate(rate)
        setVolume(volume)
        setVoiceById(voiceId)
    }

    /** Voces de español instaladas en el sistema. */
    fun spanishVoices(): List<TtsVoice> {
        val engine = tts ?: return emptyList()
        return runCatching {
            engine.voices
                ?.filter { it.locale.language == "es" && !it.isNetworkConnectionRequired }
                ?.sortedBy { it.name }
                ?.map { TtsVoice(id = it.name, label = voiceLabel(it)) }
                ?: emptyList()
        }.getOrDefault(emptyList())
    }

    fun setVoiceById(voiceId: String?) {
        val engine = tts ?: return
        if (voiceId == null) return
        runCatching {
            engine.voices?.firstOrNull { it.name == voiceId }?.let { engine.voice = it }
        }
    }

    /** Libera el motor. Llamar desde onDestroy de la Activity. */
    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        _state.value = _state.value.copy(isReady = false, isSpeaking = false)
    }

    private fun voiceLabel(v: Voice): String {
        val country = v.locale.country.takeIf { it.isNotBlank() }?.let { " ($it)" } ?: ""
        return "Español$country · ${v.name.substringAfterLast('-').substringAfterLast('#')}"
    }

    private fun resolveSpanishLocale(): Locale {
        val engine = tts ?: return Locale("es")
        val candidates = listOf(
            Locale("es", "ES"),
            Locale("es", "US"),
            Locale("es", "MX"),
            Locale("es")
        )
        for (locale in candidates) {
            val availability = engine.isLanguageAvailable(locale)
            if (availability == TextToSpeech.LANG_AVAILABLE ||
                availability == TextToSpeech.LANG_COUNTRY_AVAILABLE ||
                availability == TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE
            ) {
                return locale
            }
        }
        return Locale("es")
    }
}

/** Estado observable del motor de voz. */
data class TtsState(
    val isReady: Boolean = false,
    val isSpeaking: Boolean = false,
    val initFailed: Boolean = false
)

/** Voz del sistema disponible para el TTS. */
data class TtsVoice(val id: String, val label: String)
