package com.tunombre.agustispeaks.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tunombre.agustispeaks.avatar.AvatarOptions
import com.tunombre.agustispeaks.data.model.AvatarConfig
import com.tunombre.agustispeaks.data.model.Pictogift
import com.tunombre.agustispeaks.data.model.Pictogram
import com.tunombre.agustispeaks.data.model.TextSizeOption
import com.tunombre.agustispeaks.data.model.UserSettings
import com.tunombre.agustispeaks.data.repository.UserPhotos
import com.tunombre.agustispeaks.data.repository.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * ViewModel con ámbito de Activity: configuración de la app, avatar,
 * historial de frases recientes y pictogifts favoritos. Compartido vía
 * [LocalAppViewModel].
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

    val favorites: StateFlow<List<Pictogift>> = repo.favorites
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val customPictograms: StateFlow<List<Pictogram>> = repo.customPictograms
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    /** Momento en que arrancó este proceso; ver [UserPhotos.cleanUp]. */
    private val startedAt = System.currentTimeMillis()

    init {
        viewModelScope.launch { _avatarDraft.value = repo.avatarConfig.first() }
        viewModelScope.launch(Dispatchers.IO) {
            // Las fotos de los pictogifts y las de los pictogramas propios
            // viven en la misma carpeta. Si faltara uno de los dos grupos acá,
            // la limpieza borraría sus fotos en el próximo arranque.
            val inUse = repo.favorites.first().mapNotNull { it.photo }.toSet() +
                repo.customPictograms.first().mapNotNull { it.photo }
            UserPhotos.cleanUp(app, inUse, startedAt)
        }
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

    /* -------- Favoritos -------- */

    fun toggleFavorite(gift: Pictogift) = launch { repo.toggleFavorite(gift) }

    /**
     * Devuelve a favoritos los recién quitados, cada uno a la posición que
     * tenía. [removed] va en el orden en que se quitaron.
     */
    fun restoreFavorites(removed: List<Pair<Pictogift, Int>>) = launch { repo.restoreFavorites(removed) }

    /**
     * Crea un pictogift nuevo con la foto de [uri] y lo guarda en favoritos.
     * Siempre es uno nuevo: si [phrase] ya tenía otro con foto, ese queda
     * intacto. [onDone] recibe el pictogift, o null si la imagen no se pudo leer.
     *
     * Corre en el ámbito del ViewModel y no en el de la pantalla, para que la
     * foto termine de guardarse aunque se cierre la tarjeta a mitad de camino.
     */
    fun addPhoto(phrase: String, uri: Uri, onDone: (Pictogift?) -> Unit) {
        viewModelScope.launch {
            val name = withContext(Dispatchers.IO) { UserPhotos.save(getApplication<Application>(), uri) }
            val gift = name?.let { Pictogift.withPhoto(phrase, it) }
            if (gift != null) repo.addFavorite(gift)
            onDone(gift)
        }
    }

    /* -------- Pictogramas propios -------- */

    /**
     * Copia la foto de [uri] a la carpeta de la app. [onDone] recibe el nombre
     * del archivo, o null si la imagen no se pudo leer. Se guarda apenas se
     * elige, antes de "Guardar", para poder mostrarla en el formulario; si el
     * formulario se cancela, la limpieza del próximo arranque la borra.
     */
    fun savePhoto(uri: Uri, onDone: (String?) -> Unit) {
        viewModelScope.launch {
            val name = withContext(Dispatchers.IO) { UserPhotos.save(getApplication<Application>(), uri) }
            onDone(name)
        }
    }

    fun addCustomPictogram(categoryId: String, text: String, photo: String) = launch {
        repo.addCustomPictogram(
            Pictogram(
                id = "propio:" + UUID.randomUUID(),
                text = text.trim(),
                icon = "",
                categoryId = categoryId,
                placeIds = emptyList(),
                isCustom = true,
                photo = photo
            )
        )
    }

    /**
     * Borra el pictograma propio y, en el mismo momento, su foto.
     *
     * A diferencia de los favoritos, acá no hay "Deshacer" que necesite la foto
     * después: la confirmación se pide antes de borrar. Así, quien borra un
     * pictograma sabe que su foto dejó de estar en el teléfono.
     *
     * Primero el registro y después el archivo: si borrar el archivo fallara,
     * queda huérfano y la limpieza del próximo arranque lo borra igual. Nunca
     * al revés, que dejaría un pictograma apuntando a una foto inexistente.
     *
     * Antes de borrar se comprueba que nada más use el mismo archivo. Hoy cada
     * pictograma propio tiene el suyo y ningún favorito lo comparte, pero si
     * eso cambiara, borrarlo dejaría a otro elemento sin foto.
     */
    fun removeCustomPictogram(pictogram: Pictogram) = launch {
        repo.removeCustomPictogram(pictogram.id)
        val photo = pictogram.photo ?: return@launch
        val stillUsed = repo.favorites.first().any { it.photo == photo } ||
            repo.customPictograms.first().any { it.photo == photo }
        if (!stillUsed) {
            withContext(Dispatchers.IO) { UserPhotos.delete(getApplication<Application>(), photo) }
        }
    }

    private fun launch(block: suspend () -> Unit) {
        viewModelScope.launch { block() }
    }
}
