package com.tunombre.agustispeaks.data.repository

import com.tunombre.agustispeaks.data.model.Category
import com.tunombre.agustispeaks.data.model.CustomCategory
import com.tunombre.agustispeaks.data.model.Customization
import com.tunombre.agustispeaks.data.model.EditableKind
import com.tunombre.agustispeaks.data.model.ItemOverride
import com.tunombre.agustispeaks.data.model.Place
import org.json.JSONArray
import org.json.JSONObject

/**
 * Cómo se guarda lo hecho en modo edición: un solo objeto JSON, para que cada
 * cambio se escriba de una vez y nunca quede a medias entre dos claves.
 *
 * ```json
 * {
 *   "lugares":    [{"id": "lugar_<UUID>", "nombre": "Casa de la abuela", "foto": "<UUID>.jpg",
 *                   "color": "#FFE0B2", "categorias": ["social", "lugares"]}],
 *   "categorias": [{"id": "categoria_<UUID>", "nombre": "Mis juguetes", "foto": "<UUID>.jpg",
 *                   "color": "#C8E6C9", "lugar": "colegio"}],
 *   "cambios":    [{"tipo": "pictograma", "id": "agua", "nombre": "Agüita", "foto": "<UUID>.jpg",
 *                   "oculto": false}]
 * }
 * ```
 *
 * Funciones puras, sin `Context` ni DataStore, como [CustomPictogramJson]: se
 * pueden probar en la JVM con el mismo código que guarda en el teléfono.
 */
internal object CustomizationJson {

    private const val PLACES = "lugares"
    private const val CATEGORIES = "categorias"
    private const val OVERRIDES = "cambios"

    /**
     * Lee el objeto. Es tolerante: un JSON roto da una personalización vacía en
     * vez de romper la app, y un elemento incompleto se saltea sin llevarse a
     * los demás.
     */
    fun parse(json: String?): Customization {
        if (json.isNullOrBlank()) return Customization()
        return runCatching {
            val o = JSONObject(json)
            Customization(
                places = objects(o.optJSONArray(PLACES)).mapNotNull { place(it) },
                categories = objects(o.optJSONArray(CATEGORIES)).mapNotNull { category(it) },
                overrides = objects(o.optJSONArray(OVERRIDES)).mapNotNull { override(it) }
            )
        }.getOrDefault(Customization())
    }

    fun write(c: Customization): String = JSONObject().apply {
        put(PLACES, JSONArray().apply {
            c.places.forEach { p ->
                put(JSONObject().apply {
                    put("id", p.id)
                    put("nombre", p.name)
                    put("color", p.colorHex)
                    put("categorias", JSONArray(p.categoryIds))
                    // Sin foto la clave no se escribe: optString de un null
                    // guardado devolvería el texto "null".
                    p.photo?.let { put("foto", it) }
                })
            }
        })
        put(CATEGORIES, JSONArray().apply {
            c.categories.forEach { cc ->
                put(JSONObject().apply {
                    put("id", cc.category.id)
                    put("nombre", cc.category.name)
                    put("color", cc.category.colorHex)
                    put("lugar", cc.placeId)
                    cc.category.photo?.let { put("foto", it) }
                })
            }
        })
        put(OVERRIDES, JSONArray().apply {
            c.overrides.forEach { ov ->
                put(JSONObject().apply {
                    put("tipo", kindName(ov.kind))
                    put("id", ov.id)
                    ov.name?.let { put("nombre", it) }
                    ov.photo?.let { put("foto", it) }
                    if (ov.hidden) put("oculto", true)
                })
            }
        })
    }.toString()

    private fun objects(arr: JSONArray?): List<JSONObject> =
        if (arr == null) emptyList() else (0 until arr.length()).mapNotNull { arr.optJSONObject(it) }

    private fun JSONObject.text(key: String): String? = optString(key).takeIf { it.isNotBlank() }

    private fun place(o: JSONObject): Place? {
        val id = o.text("id") ?: return null
        val name = o.text("nombre") ?: return null
        val categories = o.optJSONArray("categorias")
        return Place(
            id = id,
            name = name,
            icon = "",
            colorHex = o.text("color") ?: DEFAULT_COLOR,
            categoryIds = if (categories == null) emptyList()
            else (0 until categories.length()).mapNotNull { categories.optString(it).takeIf { s -> s.isNotBlank() } },
            photo = o.text("foto"),
            isCustom = true
        )
    }

    private fun category(o: JSONObject): CustomCategory? {
        val id = o.text("id") ?: return null
        val name = o.text("nombre") ?: return null
        val placeId = o.text("lugar") ?: return null
        return CustomCategory(
            category = Category(
                id = id,
                name = name,
                icon = "",
                colorHex = o.text("color") ?: DEFAULT_COLOR,
                photo = o.text("foto"),
                isCustom = true
            ),
            placeId = placeId
        )
    }

    private fun override(o: JSONObject): ItemOverride? {
        val kind = kindOf(o.text("tipo")) ?: return null
        val id = o.text("id") ?: return null
        val ov = ItemOverride(
            kind = kind,
            id = id,
            name = o.text("nombre"),
            photo = o.text("foto"),
            hidden = o.optBoolean("oculto", false)
        )
        return ov.takeUnless { it.isEmpty }
    }

    private fun kindName(kind: EditableKind): String = when (kind) {
        EditableKind.PLACE -> "lugar"
        EditableKind.CATEGORY -> "categoria"
        EditableKind.PICTOGRAM -> "pictograma"
    }

    private fun kindOf(name: String?): EditableKind? = when (name) {
        "lugar" -> EditableKind.PLACE
        "categoria" -> EditableKind.CATEGORY
        "pictograma" -> EditableKind.PICTOGRAM
        else -> null
    }

    private const val DEFAULT_COLOR = "#E0E0E0"
}
