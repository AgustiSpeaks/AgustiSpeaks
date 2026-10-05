package com.tunombre.agustispeaks.data.repository

import com.tunombre.agustispeaks.data.model.Pictogram
import org.json.JSONArray
import org.json.JSONObject

/**
 * Cómo se guardan los pictogramas propios: un arreglo JSON con un objeto por
 * pictograma, en el orden en que se crearon.
 *
 * ```json
 * [{"id": "propio:<UUID>", "categoria": "juego", "texto": "Oso", "foto": "<UUID>.jpg"}]
 * ```
 *
 * Son funciones puras, sin `Context` ni DataStore, a propósito: así se pueden
 * probar en la JVM. [SettingsRepository] las usa tal cual dentro de su
 * escritura en DataStore, de modo que la prueba ejercita el mismo código que
 * guarda los datos en el teléfono.
 */
internal object CustomPictogramJson {

    /**
     * Lee el arreglo. Es tolerante: un JSON roto da una lista vacía en vez de
     * romper la app, y un elemento sin id, categoría o texto se saltea sin
     * llevarse a los demás. La clave `foto` ausente es un pictograma sin foto.
     */
    fun parse(json: String?): List<Pictogram> {
        if (json.isNullOrBlank()) return emptyList()
        return runCatching {
            val arr = JSONArray(json)
            (0 until arr.length()).mapNotNull { i ->
                val o = arr.optJSONObject(i) ?: return@mapNotNull null
                val id = o.optString("id")
                val category = o.optString("categoria")
                val text = o.optString("texto")
                if (id.isBlank() || category.isBlank() || text.isBlank()) {
                    null
                } else {
                    Pictogram(
                        id = id,
                        text = text,
                        icon = "",
                        categoryId = category,
                        placeIds = emptyList(),
                        isCustom = true,
                        photo = o.optString("foto").takeIf { it.isNotBlank() }
                    )
                }
            }
        }.getOrDefault(emptyList())
    }

    fun write(pictograms: List<Pictogram>): String {
        val arr = JSONArray()
        pictograms.forEach { p ->
            arr.put(JSONObject().apply {
                put("id", p.id)
                put("categoria", p.categoryId)
                put("texto", p.text)
                // Sin foto la clave no se escribe: optString de un null guardado
                // devolvería el texto "null" y parecería un nombre de archivo.
                p.photo?.let { put("foto", it) }
            })
        }
        return arr.toString()
    }

    /** Agrega [pictogram] al final. Uno con texto en blanco no se agrega. */
    fun add(json: String?, pictogram: Pictogram): String {
        val current = parse(json)
        return write(if (pictogram.text.isBlank()) current else current + pictogram)
    }

    /** Quita el de [id]. Si no estaba, la lista queda igual. */
    fun remove(json: String?, id: String): String =
        write(parse(json).filterNot { it.id == id })
}
