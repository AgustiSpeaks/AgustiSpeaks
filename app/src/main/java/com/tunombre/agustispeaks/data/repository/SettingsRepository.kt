package com.tunombre.agustispeaks.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.tunombre.agustispeaks.data.model.AvatarConfig
import com.tunombre.agustispeaks.data.model.AvatarGender
import com.tunombre.agustispeaks.data.model.AvatarKind
import com.tunombre.agustispeaks.data.model.Pictogift
import com.tunombre.agustispeaks.data.model.Pictogram
import com.tunombre.agustispeaks.data.model.TextSizeOption
import com.tunombre.agustispeaks.data.model.UserSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private const val MAX_RECENTS = 30

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "agusti_settings")

/**
 * Persistencia de [UserSettings], [AvatarConfig], el historial de frases
 * recientes, los pictogifts favoritos y los pictogramas propios con DataStore
 * (Preferences).
 */
class SettingsRepository(context: Context) {

    private val ds = context.applicationContext.settingsDataStore

    val userSettings: Flow<UserSettings> = ds.data.map { p ->
        UserSettings(
            interfaceLanguage = p[K.LANGUAGE] ?: "es",
            ignoreRepeatedTaps = p[K.IGNORE_REPEATED_TAPS] ?: false,
            activateOnRelease = p[K.ACTIVATE_ON_RELEASE] ?: true,
            darkMode = p[K.DARK_MODE] ?: false,
            highContrast = p[K.HIGH_CONTRAST] ?: false,
            textSize = p[K.TEXT_SIZE]?.let { runCatching { TextSizeOption.valueOf(it) }.getOrNull() }
                ?: TextSizeOption.NORMAL,
            voicePitch = p[K.VOICE_PITCH] ?: 1.0f,
            voiceRate = p[K.VOICE_RATE] ?: 1.0f,
            voiceVolume = p[K.VOICE_VOLUME] ?: 1.0f,
            voiceId = p[K.VOICE_ID],
            gridColumns = p[K.GRID_COLUMNS]
        )
    }

    val avatarConfig: Flow<AvatarConfig> = ds.data.map { p ->
        val default = AvatarConfig()
        AvatarConfig(
            kind = p[K.AV_KIND]?.let { runCatching { AvatarKind.valueOf(it) }.getOrNull() } ?: default.kind,
            gender = p[K.AV_GENDER]?.let { runCatching { AvatarGender.valueOf(it) }.getOrNull() } ?: default.gender,
            skinColor = p[K.AV_SKIN] ?: default.skinColor,
            hairStyle = p[K.AV_HAIR_STYLE] ?: default.hairStyle,
            hairColor = p[K.AV_HAIR_COLOR] ?: default.hairColor,
            eyebrows = p[K.AV_EYEBROWS] ?: default.eyebrows,
            eyes = p[K.AV_EYES] ?: default.eyes,
            mouth = p[K.AV_MOUTH] ?: default.mouth,
            clothing = p[K.AV_CLOTHING] ?: default.clothing,
            clothingColor = p[K.AV_CLOTHING_COLOR] ?: default.clothingColor,
            pantsColor = p[K.AV_PANTS_COLOR] ?: default.pantsColor,
            shoeColor = p[K.AV_SHOE_COLOR] ?: default.shoeColor,
            robotColor = p[K.AV_ROBOT_COLOR] ?: default.robotColor,
            robotEyes = p[K.AV_ROBOT_EYES] ?: default.robotEyes,
            robotMouth = p[K.AV_ROBOT_MOUTH] ?: default.robotMouth,
            robotFace = p[K.AV_ROBOT_FACE] ?: default.robotFace,
            robotAccessory = p[K.AV_ROBOT_ACCESSORY] ?: default.robotAccessory
        )
    }

    /** Frases dichas recientemente (máx. 30), de la más reciente a la más antigua. */
    val recentPhrases: Flow<List<String>> = ds.data.map { parsePhrases(it[K.RECENTS]) }

    /** Registra una frase: la pone primera, quita duplicados exactos, recorta a 30. */
    suspend fun addRecentPhrase(phrase: String) {
        val clean = phrase.trim()
        if (clean.isEmpty()) return
        ds.edit { p ->
            val current = parsePhrases(p[K.RECENTS]).filterNot { it.equals(clean, ignoreCase = true) }
            p[K.RECENTS] = JSONArray(listOf(clean) + current.take(MAX_RECENTS - 1)).toString()
        }
    }

    suspend fun clearRecents() = ds.edit { it.remove(K.RECENTS) }

    /**
     * Pictogifts guardados, del último agregado al primero. Van en una clave
     * aparte de recientes a propósito: borrar el historial no puede llevarse lo
     * que alguien eligió guardar. Por lo mismo no tienen tope.
     */
    val favorites: Flow<List<Pictogift>> = ds.data.map { parseFavorites(it[K.FAVORITES]) }

    /** Guarda [gift], o lo quita si ya estaba. Se compara por id, no por texto. */
    suspend fun toggleFavorite(gift: Pictogift) {
        if (gift.phrase.isBlank()) return
        ds.edit { p ->
            val current = parseFavorites(p[K.FAVORITES])
            val without = current.filterNot { it.id == gift.id }
            val next = if (without.size < current.size) without else listOf(gift) + current
            p[K.FAVORITES] = favoritesJson(next)
        }
    }

    /** Guarda [gift] si todavía no estaba. Nunca lo quita. */
    suspend fun addFavorite(gift: Pictogift) {
        if (gift.phrase.isBlank()) return
        ds.edit { p ->
            val current = parseFavorites(p[K.FAVORITES])
            if (current.none { it.id == gift.id }) {
                p[K.FAVORITES] = favoritesJson(listOf(gift) + current)
            }
        }
    }

    /**
     * "Deshacer": devuelve los favoritos quitados, cada uno al lugar donde
     * estaba y no arriba de todo. [removed] va en el orden en que se quitaron,
     * con la posición que tenía cada uno en ese momento.
     *
     * Se reinsertan **del último quitado al primero**, y en una sola escritura.
     * Cada posición se midió sobre la lista sin los quitados antes que él, así
     * que en orden inverso cada una cae sobre la lista exacta en la que se midió
     * y la original queda reconstruida tal cual.
     */
    suspend fun restoreFavorites(removed: List<Pair<Pictogift, Int>>) {
        if (removed.isEmpty()) return
        ds.edit { p ->
            val list = parseFavorites(p[K.FAVORITES]).toMutableList()
            removed.asReversed().forEach { (gift, at) ->
                if (list.none { it.id == gift.id }) list.add(at.coerceIn(0, list.size), gift)
            }
            p[K.FAVORITES] = favoritesJson(list)
        }
    }

    /**
     * Pictogramas creados por quien usa la app, con nombre y foto. Se suman a
     * los de la semilla en su categoría. Se guardan en el orden en que se
     * crearon, y así se muestran: al final de la categoría.
     *
     * El formato vive en [CustomPictogramJson], fuera de esta clase, para que
     * se pueda probar en la JVM sin DataStore.
     */
    val customPictograms: Flow<List<Pictogram>> =
        ds.data.map { CustomPictogramJson.parse(it[K.CUSTOM_PICTOGRAMS]) }

    suspend fun addCustomPictogram(pictogram: Pictogram) {
        if (pictogram.text.isBlank()) return
        ds.edit { p -> p[K.CUSTOM_PICTOGRAMS] = CustomPictogramJson.add(p[K.CUSTOM_PICTOGRAMS], pictogram) }
    }

    suspend fun removeCustomPictogram(id: String) {
        ds.edit { p -> p[K.CUSTOM_PICTOGRAMS] = CustomPictogramJson.remove(p[K.CUSTOM_PICTOGRAMS], id) }
    }

    suspend fun setLanguage(v: String) = ds.edit { it[K.LANGUAGE] = v }
    suspend fun setIgnoreRepeatedTaps(v: Boolean) = ds.edit { it[K.IGNORE_REPEATED_TAPS] = v }
    suspend fun setActivateOnRelease(v: Boolean) = ds.edit { it[K.ACTIVATE_ON_RELEASE] = v }
    suspend fun setDarkMode(v: Boolean) = ds.edit { it[K.DARK_MODE] = v }
    suspend fun setHighContrast(v: Boolean) = ds.edit { it[K.HIGH_CONTRAST] = v }
    suspend fun setTextSize(v: TextSizeOption) = ds.edit { it[K.TEXT_SIZE] = v.name }
    suspend fun setVoicePitch(v: Float) = ds.edit { it[K.VOICE_PITCH] = v }
    suspend fun setVoiceRate(v: Float) = ds.edit { it[K.VOICE_RATE] = v }
    suspend fun setVoiceVolume(v: Float) = ds.edit { it[K.VOICE_VOLUME] = v }
    suspend fun setVoiceId(v: String?) = ds.edit {
        if (v == null) it.remove(K.VOICE_ID) else it[K.VOICE_ID] = v
    }

    suspend fun setAvatarConfig(c: AvatarConfig) = ds.edit { p ->
        p[K.AV_KIND] = c.kind.name
        p[K.AV_GENDER] = c.gender.name
        p[K.AV_SKIN] = c.skinColor
        p[K.AV_HAIR_STYLE] = c.hairStyle
        p[K.AV_HAIR_COLOR] = c.hairColor
        p[K.AV_EYEBROWS] = c.eyebrows
        p[K.AV_EYES] = c.eyes
        p[K.AV_MOUTH] = c.mouth
        p[K.AV_CLOTHING] = c.clothing
        p[K.AV_CLOTHING_COLOR] = c.clothingColor
        p[K.AV_PANTS_COLOR] = c.pantsColor
        p[K.AV_SHOE_COLOR] = c.shoeColor
        p[K.AV_ROBOT_COLOR] = c.robotColor
        p[K.AV_ROBOT_EYES] = c.robotEyes
        p[K.AV_ROBOT_MOUTH] = c.robotMouth
        p[K.AV_ROBOT_FACE] = c.robotFace
        p[K.AV_ROBOT_ACCESSORY] = c.robotAccessory
    }

    private fun parsePhrases(json: String?): List<String> {
        if (json.isNullOrBlank()) return emptyList()
        return runCatching {
            val arr = JSONArray(json)
            (0 until arr.length()).map { arr.getString(it) }
        }.getOrDefault(emptyList())
    }

    /**
     * Cada favorito es un objeto `{id, frase, foto}`. La primera versión de
     * favoritos guardaba solo el texto, así que un elemento que es un texto se
     * lee como pictogift sin foto: los favoritos viejos no se pierden, y pasan
     * al formato nuevo la próxima vez que se guarde uno.
     */
    private fun parseFavorites(json: String?): List<Pictogift> {
        if (json.isNullOrBlank()) return emptyList()
        return runCatching {
            val arr = JSONArray(json)
            (0 until arr.length()).mapNotNull { i ->
                when (val e = arr.get(i)) {
                    is String -> e.takeIf { it.isNotBlank() }?.let { Pictogift.fromText(it) }
                    is JSONObject -> {
                        val id = e.optString("id")
                        val phrase = e.optString("frase")
                        if (id.isBlank() || phrase.isBlank()) {
                            null
                        } else {
                            Pictogift(id, phrase, e.optString("foto").takeIf { it.isNotBlank() })
                        }
                    }
                    else -> null
                }
            }
        }.getOrDefault(emptyList())
    }

    private fun favoritesJson(gifts: List<Pictogift>): String {
        val arr = JSONArray()
        gifts.forEach { g ->
            arr.put(JSONObject().apply {
                put("id", g.id)
                put("frase", g.phrase)
                // Sin foto la clave no se escribe: optString de un null guardado
                // devolvería el texto "null" y parecería un nombre de archivo.
                g.photo?.let { put("foto", it) }
            })
        }
        return arr.toString()
    }

    private object K {
        val LANGUAGE = stringPreferencesKey("language")
        val IGNORE_REPEATED_TAPS = booleanPreferencesKey("ignore_repeated_taps")
        val ACTIVATE_ON_RELEASE = booleanPreferencesKey("activate_on_release")
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val HIGH_CONTRAST = booleanPreferencesKey("high_contrast")
        val TEXT_SIZE = stringPreferencesKey("text_size")
        val VOICE_PITCH = floatPreferencesKey("voice_pitch")
        val VOICE_RATE = floatPreferencesKey("voice_rate")
        val VOICE_VOLUME = floatPreferencesKey("voice_volume")
        val VOICE_ID = stringPreferencesKey("voice_id")
        val GRID_COLUMNS = intPreferencesKey("grid_columns")

        val AV_KIND = stringPreferencesKey("av_kind")
        val AV_GENDER = stringPreferencesKey("av_gender")
        val AV_ROBOT_COLOR = stringPreferencesKey("av_robot_color")
        val AV_ROBOT_EYES = stringPreferencesKey("av_robot_eyes")
        val AV_ROBOT_MOUTH = stringPreferencesKey("av_robot_mouth")
        val AV_ROBOT_FACE = stringPreferencesKey("av_robot_face")
        val AV_ROBOT_ACCESSORY = stringPreferencesKey("av_robot_accessory")
        val AV_SKIN = stringPreferencesKey("av_skin")
        val AV_HAIR_STYLE = stringPreferencesKey("av_hair_style")
        val AV_HAIR_COLOR = stringPreferencesKey("av_hair_color")
        val AV_EYEBROWS = stringPreferencesKey("av_eyebrows")
        val AV_EYES = stringPreferencesKey("av_eyes")
        val AV_MOUTH = stringPreferencesKey("av_mouth")
        val AV_CLOTHING = stringPreferencesKey("av_clothing")
        val AV_CLOTHING_COLOR = stringPreferencesKey("av_clothing_color")
        val AV_PANTS_COLOR = stringPreferencesKey("av_pants_color")
        val AV_SHOE_COLOR = stringPreferencesKey("av_shoe_color")

        val RECENTS = stringPreferencesKey("recents")
        val FAVORITES = stringPreferencesKey("favorites")
        val CUSTOM_PICTOGRAMS = stringPreferencesKey("custom_pictograms")
    }
}
