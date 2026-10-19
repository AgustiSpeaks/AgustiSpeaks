package com.tunombre.agustispeaks.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tunombre.agustispeaks.avatar.AvatarOptions
import com.tunombre.agustispeaks.data.catalog.Catalog
import com.tunombre.agustispeaks.data.model.AvatarConfig
import com.tunombre.agustispeaks.data.model.Category
import com.tunombre.agustispeaks.data.model.CustomCategory
import com.tunombre.agustispeaks.data.model.EditableKind
import com.tunombre.agustispeaks.data.model.Pictogift
import com.tunombre.agustispeaks.data.model.Pictogram
import com.tunombre.agustispeaks.data.model.Place
import com.tunombre.agustispeaks.data.model.TextSizeOption
import com.tunombre.agustispeaks.data.model.UserSettings
import com.tunombre.agustispeaks.data.repository.EditPassword
import com.tunombre.agustispeaks.data.repository.SettingsRepository
import com.tunombre.agustispeaks.data.repository.UserPhotos
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * ViewModel con ámbito de Activity: configuración de la app, avatar,
 * historial de frases recientes, pictogifts favoritos, el vocabulario con lo
 * hecho en modo edición, y el modo edición mismo. Compartido vía
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

    /** Lugares, categorías y pictogramas: los de fábrica con lo hecho en modo edición. */
    val catalog: StateFlow<Catalog> = combine(repo.customization, repo.customPictograms) { c, p -> Catalog(c, p) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, Catalog())

    /**
     * Si ya se creó la contraseña del modo edición. Es null mientras se lee del
     * almacenamiento: así la primera pantalla no muestra un instante el
     * formulario de crearla a quien ya la tiene.
     */
    val hasPassword: StateFlow<Boolean?> = repo.editPasswordHash
        .map { it != null }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    /**
     * Modo edición. Vive solo en memoria, a propósito: cerrar la app lo apaga,
     * y un adulto que se olvida de salir no deja el tablero editable para la
     * próxima vez.
     */
    private val _editMode = MutableStateFlow(false)
    val editMode: StateFlow<Boolean> = _editMode.asStateFlow()

    /** Momento en que arrancó este proceso; ver [UserPhotos.cleanUp]. */
    private val startedAt = System.currentTimeMillis()

    init {
        viewModelScope.launch { _avatarDraft.value = repo.avatarConfig.first() }
        viewModelScope.launch(Dispatchers.IO) {
            // Todas las fotos viven en la misma carpeta. Si faltara un grupo en
            // [photosInUse], la limpieza borraría sus fotos en este arranque.
            UserPhotos.cleanUp(app, photosInUse(), startedAt)
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

    /* -------- Contraseña y modo edición -------- */

    /**
     * Crea la contraseña la primera vez. Si ya había una, no hace nada: para
     * cambiarla hay que saber la actual ([changePassword]).
     */
    fun createPassword(password: String) {
        if (!EditPassword.isValid(password)) return
        viewModelScope.launch {
            if (repo.editPasswordHash.first() != null) return@launch
            val hash = withContext(Dispatchers.Default) { EditPassword.hash(password) }
            repo.setEditPasswordHash(hash)
        }
    }

    /** Entra al modo edición si [password] es la correcta. [onResult] dice si lo era. */
    fun enterEditMode(password: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val ok = checkPassword(password)
            if (ok) _editMode.value = true
            onResult(ok)
        }
    }

    fun exitEditMode() {
        _editMode.value = false
    }

    /**
     * Cambia la contraseña si [current] es la actual. [onResult] dice si se
     * cambió: false es que la actual no era esa.
     */
    fun changePassword(current: String, new: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val ok = EditPassword.isValid(new) && checkPassword(current)
            if (ok) repo.setEditPasswordHash(withContext(Dispatchers.Default) { EditPassword.hash(new) })
            onResult(ok)
        }
    }

    private suspend fun checkPassword(password: String): Boolean {
        val stored = repo.editPasswordHash.first()
        return withContext(Dispatchers.Default) { EditPassword.verify(password, stored) }
    }

    /* -------- Fotos -------- */

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

    /* -------- Modo edición: pictogramas -------- */

    fun addCustomPictogram(categoryId: String, text: String, photo: String) = edit {
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
     * Cambia nombre y foto. Uno propio se reescribe; a uno de fábrica se le
     * guarda el cambio encima, y lo de fábrica queda intacto.
     *
     * @param photo la foto elegida, o null para dejar el ícono de fábrica.
     */
    fun editPictogram(pictogram: Pictogram, text: String, photo: String?) = edit {
        if (pictogram.isCustom) {
            repo.updateCustomPictogram(pictogram.copy(text = text.trim(), photo = photo ?: pictogram.photo))
        } else {
            overrideSeed(EditableKind.PICTOGRAM, pictogram.id, text, photo)
        }
    }

    /**
     * Borra el pictograma propio y, en el mismo momento, su foto.
     *
     * A diferencia de los favoritos, acá no hay "Deshacer" que necesite la foto
     * después: la confirmación se pide antes de borrar. Así, quien borra un
     * pictograma sabe que su foto dejó de estar en el teléfono.
     *
     * Primero el registro y después el archivo ([edit] borra lo que dejó de
     * usarse): si borrar el archivo fallara, queda huérfano y la limpieza del
     * próximo arranque lo borra igual. Nunca al revés, que dejaría un
     * pictograma apuntando a una foto inexistente.
     */
    fun removeCustomPictogram(pictogram: Pictogram) = edit {
        repo.removeCustomPictogram(pictogram.id)
    }

    /* -------- Modo edición: categorías -------- */

    /** Crea una categoría en [placeId]. Aparece solo en ese lugar. */
    fun addCategory(placeId: String, name: String, photo: String, colorHex: String) = edit {
        val category = Category(
            id = "categoria_" + UUID.randomUUID(),
            name = name.trim(),
            icon = "",
            colorHex = colorHex,
            photo = photo,
            isCustom = true
        )
        repo.updateCustomization { c -> c.copy(categories = c.categories + CustomCategory(category, placeId)) }
    }

    fun editCategory(category: Category, name: String, photo: String?, colorHex: String) = edit {
        if (category.isCustom) {
            repo.updateCustomization { c ->
                c.copy(categories = c.categories.map {
                    if (it.category.id != category.id) it
                    else it.copy(category = it.category.copy(
                        name = name.trim(),
                        photo = photo ?: it.category.photo,
                        colorHex = colorHex
                    ))
                })
            }
        } else {
            overrideSeed(EditableKind.CATEGORY, category.id, name, photo)
        }
    }

    /** Borra una categoría propia con todos sus pictogramas propios y sus fotos. */
    fun deleteCategory(category: Category) = edit {
        if (category.isCustom) repo.deleteCategoriesWithPictograms(setOf(category.id)) { it }
    }

    /* -------- Modo edición: lugares -------- */

    fun addPlace(name: String, photo: String, colorHex: String, categoryIds: List<String>) = edit {
        val place = Place(
            id = "lugar_" + UUID.randomUUID(),
            name = name.trim(),
            icon = "",
            colorHex = colorHex,
            categoryIds = categoryIds,
            photo = photo,
            isCustom = true
        )
        repo.updateCustomization { c -> c.copy(places = c.places + place) }
    }

    /**
     * @param categoryIds las categorías de fábrica que muestra. Solo cuenta en
     *        un lugar creado: en uno de fábrica no se cambian.
     */
    fun editPlace(place: Place, name: String, photo: String?, colorHex: String, categoryIds: List<String>) = edit {
        if (place.isCustom) {
            repo.updateCustomization { c ->
                c.copy(places = c.places.map {
                    if (it.id != place.id) it
                    else it.copy(
                        name = name.trim(),
                        photo = photo ?: it.photo,
                        colorHex = colorHex,
                        categoryIds = categoryIds
                    )
                })
            }
        } else {
            overrideSeed(EditableKind.PLACE, place.id, name, photo)
        }
    }

    /** Borra un lugar propio con sus categorías propias, sus pictogramas y sus fotos. */
    fun deletePlace(place: Place) = edit {
        if (!place.isCustom) return@edit
        val categoryIds = repo.customization.first().categories
            .filter { it.placeId == place.id }
            .map { it.category.id }
            .toSet()
        repo.deleteCategoriesWithPictograms(categoryIds) { c ->
            c.copy(places = c.places.filterNot { it.id == place.id })
        }
    }

    /* -------- Modo edición: lo de fábrica -------- */

    /** Oculta o vuelve a mostrar algo de fábrica. Lo propio no se oculta: se borra. */
    fun setHidden(kind: EditableKind, id: String, hidden: Boolean) = edit {
        repo.updateCustomization { c -> c.changeOverride(kind, id) { it.copy(hidden = hidden) } }
    }

    /** Devuelve nombre e ícono de fábrica. No cambia si está oculto. */
    fun restoreOriginal(kind: EditableKind, id: String) = edit {
        repo.updateCustomization { c -> c.changeOverride(kind, id) { it.copy(name = null, photo = null) } }
    }

    /**
     * Guarda el cambio de nombre y foto de algo de fábrica. Un nombre igual al
     * original no se guarda como cambio: así "Volver al original" no aparece
     * para algo que en realidad no cambió.
     */
    private suspend fun overrideSeed(kind: EditableKind, id: String, name: String, photo: String?) {
        val original = catalog.value.originalName(kind, id) ?: return
        val clean = name.trim()
        repo.updateCustomization { c ->
            c.changeOverride(kind, id) {
                it.copy(name = clean.takeIf { n -> n.isNotEmpty() && n != original }, photo = photo)
            }
        }
    }

    /**
     * Corre un cambio del modo edición y después borra **en el momento** las
     * fotos que dejó de usar: la de un pictograma, categoría o lugar borrado,
     * la que se reemplazó por otra, o la de algo que volvió a su ícono.
     *
     * Se comparan las fotos en uso antes y después, en vez de que cada cambio
     * diga cuáles soltó: así ninguno se olvida, y una foto que algo más sigue
     * usando nunca se borra.
     *
     * No hace nada fuera del modo edición.
     */
    private fun edit(block: suspend () -> Unit) {
        if (!_editMode.value) return
        viewModelScope.launch {
            val before = photosInUse()
            block()
            val gone = before - photosInUse()
            if (gone.isNotEmpty()) {
                withContext(Dispatchers.IO) {
                    gone.forEach { UserPhotos.delete(getApplication<Application>(), it) }
                }
            }
        }
    }

    /**
     * Todas las fotos vigentes: de favoritos, de pictogramas propios y de lo
     * creado o cambiado en modo edición.
     */
    private suspend fun photosInUse(): Set<String> =
        repo.favorites.first().mapNotNull { it.photo }.toSet() +
            repo.customization.first().photos() +
            repo.customPictograms.first().mapNotNull { it.photo }

    private fun launch(block: suspend () -> Unit) {
        viewModelScope.launch { block() }
    }
}
