package com.tunombre.agustispeaks.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tunombre.agustispeaks.avatar.AvatarOptions
import com.tunombre.agustispeaks.data.model.AvatarConfig
import com.tunombre.agustispeaks.data.model.TextSizeOption
import com.tunombre.agustispeaks.data.model.UserSettings
import com.tunombre.agustispeaks.data.repository.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel con ámbito de Activity: configuración de la app, avatar e
 * historial de frases recientes. Compartido vía [LocalAppViewModel].
 */
class AppViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = SettingsRepository(app)

    val userSettings: StateFlow<UserSettings> = repo.userSettings
        .stateIn(viewModelScope, SharingStarted.Eagerly, UserSettings())

    /** Avatar persistido (para la barra de frase y demás pantallas). */
    val avatarConfig: StateFlow<AvatarConfig> = repo.avatarConfig
        .stateIn(viewModelScope, SharingStarted.Eagerly, AvatarConfig())

    /** Copia de trabajo del avatar: se edita en memoria (preview instantánea). */
    private val _avatarDraft = MutableStateFlow(AvatarConfig())
    val avatarDraft: StateFlow<AvatarConfig> = _avatarDraft.asStateFlow()

    val recentPhrases: StateFlow<List<String>> = repo.recentPhrases
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    init {
        viewModelScope.launch { _avatarDraft.value = repo.avatarConfig.first() }
    }

    /* -------- Configuraciones -------- */

    fun setLanguage(v: String) = launch { repo.setLanguage(v) }
    fun setIgnoreRepeatedTaps(v: Boolean) = launch { repo.setIgnoreRepeatedTaps(v) }
    fun setActivateOnRelease(v: Boolean) = launch { repo.setActivateOnRelease(v) }
    fun setDarkMode(v: Boolean) = launch { repo.setDarkMode(v) }
    fun setHighContrast(v: Boolean) = launch { repo.setHighContrast(v) }
    fun setTextSize(v: TextSizeOption) = launch { repo.setTextSize(v) }

    /* -------- Voz -------- */

    fun setVoicePitch(v: Float) = launch { repo.setVoicePitch(v) }
    fun setVoiceRate(v: Float) = launch { repo.setVoiceRate(v) }
    fun setVoiceVolume(v: Float) = launch { repo.setVoiceVolume(v) }
    fun setVoiceId(v: String?) = launch { repo.setVoiceId(v) }

    /* -------- Avatar -------- */

    fun updateAvatar(transform: (AvatarConfig) -> AvatarConfig) {
        val next = transform(_avatarDraft.value)
        _avatarDraft.value = next
        launch { repo.setAvatarConfig(next) }
    }

    fun randomizeAvatar() = updateAvatar { c ->
        c.copy(
            skinColor = AvatarOptions.skinColors.random().hex,
            hairStyle = AvatarOptions.hairStyles.random().value,
            hairColor = AvatarOptions.hairColors.random().hex,
            eyebrows = AvatarOptions.eyebrows.random().value,
            eyes = AvatarOptions.eyes.random().value,
            mouth = AvatarOptions.mouths.random().value,
            clothing = AvatarOptions.clothing.random().value,
            clothingColor = AvatarOptions.clothingColors.random().hex,
            pantsColor = AvatarOptions.pantsColors.random().hex,
            shoeColor = AvatarOptions.shoeColors.random().hex,
            robotColor = AvatarOptions.robotColors.random().hex,
            robotEyes = AvatarOptions.robotEyes.random().value,
            robotMouth = AvatarOptions.robotMouths.random().value,
            robotFace = AvatarOptions.robotFaces.random().value,
            robotAccessory = AvatarOptions.robotAccessories.random().value
        )
    }

    /* -------- Recientes -------- */

    fun recordPhrase(phrase: String) = launch { repo.addRecentPhrase(phrase) }
    fun clearRecents() = launch { repo.clearRecents() }

    private fun launch(block: suspend () -> Unit) {
        viewModelScope.launch { block() }
    }
}
