package com.tunombre.agustispeaks.avatar.catalog

import com.tunombre.agustispeaks.avatar.Prop
import org.json.JSONArray
import org.json.JSONObject

/** Un registro que no se pudo leer. Se saltea ese, no el catálogo entero. */
private class CatalogError(mensaje: String) : Exception(mensaje)

/**
 * Lee un catálogo de animaciones desde JSON con `org.json`, que viene en
 * Android: cero dependencias nuevas.
 *
 * Criterio de errores: un registro roto se descarta y se anota, el resto sigue.
 * Un error de tipeo en una animación no puede dejar sin comunicarse a un chico
 * que está usando la app. Los mensajes nombran el registro, el fotograma y los
 * valores admitidos, porque quien los va a leer es un especialista, no un
 * programador.
 */
object AnimationParser {

    fun parse(json: String): CatalogParseResult {
        val registros = mutableListOf<AnimationRecord>()
        val problemas = mutableListOf<String>()

        val raiz = try {
            JSONArray(json)
        } catch (e: Exception) {
            return CatalogParseResult(
                emptyList(),
                listOf("El catálogo tiene que ser un arreglo JSON: ${e.message}")
            )
        }

        val vistos = mutableSetOf<String>()
        for (i in 0 until raiz.length()) {
            val obj = raiz.opt(i) as? JSONObject
            if (obj == null) {
                problemas += "Entrada $i: no es un objeto JSON."
                continue
            }
            try {
                val registro = leerRegistro(obj, i)
                if (!vistos.add(registro.id)) {
                    problemas += "${registro.id}: id repetido, se ignora la segunda aparición."
                    continue
                }
                registros += registro
            } catch (e: CatalogError) {
                problemas += e.message.orEmpty()
            }
        }
        return CatalogParseResult(registros, problemas)
    }

    private fun leerRegistro(o: JSONObject, orden: Int): AnimationRecord {
        val id = texto(o, "id").trim()
        if (id.isEmpty()) throw CatalogError("Entrada $orden: falta \"id\".")

        val loop = numero(o, "loopMillis", id)?.toInt()
            ?: throw CatalogError("$id: falta \"loopMillis\".")
        if (loop <= 0) throw CatalogError("$id: \"loopMillis\" tiene que ser mayor que cero.")

        val procedural = texto(o, "procedural").trim().takeIf { it.isNotEmpty() }
        if (procedural != null && !ProceduralPoses.exists(procedural)) {
            throw CatalogError(
                "$id: no existe el generador procedural \"$procedural\". " +
                    "Disponibles: ${ProceduralPoses.names().sorted().joinToString(", ")}"
            )
        }

        val fotogramas = mutableListOf<Keyframe>()
        (o.opt("fotogramas") as? JSONArray)?.let { arr ->
            for (n in 0 until arr.length()) {
                val f = arr.opt(n) as? JSONObject
                    ?: throw CatalogError("$id, fotograma $n: no es un objeto JSON.")
                fotogramas += leerFotograma(f, id, n)
            }
        }

        if (procedural == null && fotogramas.isEmpty()) {
            throw CatalogError("$id: no tiene ni \"fotogramas\" ni \"procedural\".")
        }

        val propDefecto = texto(o, "prop").trim().takeIf { it.isNotEmpty() }?.let {
            try {
                Prop.valueOf(it)
            } catch (e: IllegalArgumentException) {
                throw CatalogError("$id: \"prop\" = \"$it\" no es un objeto conocido.")
            }
        }

        return AnimationRecord(
            id = id,
            loopMillis = loop,
            prioridad = numero(o, "prioridad", id)?.toInt() ?: 100,
            palabras = lista(o, "palabras"),
            raices = lista(o, "raices"),
            ademas = lista(o, "ademas"),
            ademasRaices = lista(o, "ademasRaices"),
            excluye = lista(o, "excluye"),
            excluyeRaices = lista(o, "excluyeRaices"),
            usaParteDelCuerpo = booleano(o, "usaParteDelCuerpo", false),
            prop = propDefecto,
            procedural = procedural,
            cicloCerrado = booleano(o, "cicloCerrado", true),
            fotogramas = fotogramas,
            orden = orden
        )
    }

    private fun leerFotograma(o: JSONObject, id: String, n: Int): Keyframe {
        val t = numero(o, "t", id)
            ?: throw CatalogError("$id, fotograma $n: falta \"t\".")
        if (t < 0f || t > 1f) {
            throw CatalogError("$id, fotograma $n: \"t\" = $t está fuera de 0..1.")
        }

        val ease = texto(o, "ease").trim().takeIf { it.isNotEmpty() }?.let {
            Ease.desde(it) ?: throw CatalogError(
                "$id, fotograma $n: \"ease\" = \"$it\" no existe. Opciones: coseno, lineal."
            )
        }

        val floats = LinkedHashMap<FloatField, Float>()
        for (campo in FloatField.entries) {
            if (!o.has(campo.key) || o.isNull(campo.key)) continue
            floats[campo] = numero(o, campo.key, id)
                ?: throw CatalogError("$id, fotograma $n: \"${campo.key}\" no es un número.")
        }

        val steps = LinkedHashMap<StepField, Any?>()
        for (campo in StepField.entries) {
            if (!o.has(campo.key)) continue
            if (o.isNull(campo.key)) {
                // Declarado null a propósito: apaga el campo.
                steps[campo] = null
                continue
            }
            val crudo = o.get(campo.key).toString()
            steps[campo] = try {
                campo.parse(crudo)
            } catch (e: IllegalArgumentException) {
                throw CatalogError(
                    "$id, fotograma $n: \"${campo.key}\" = \"$crudo\" no es un valor válido. " +
                        "Opciones: ${campo.options.joinToString(", ")}"
                )
            }
        }

        return Keyframe(t = t, ease = ease, floats = floats, steps = steps)
    }

    /* -------- lectores tolerantes -------- */

    /**
     * `getString` y `getDouble` se comportan distinto en el `org.json` de
     * Android y en el de la JVM (uno convierte, el otro tira excepción). Estos
     * lectores usan solo `has`, `isNull` y `get`, que sí coinciden, para que el
     * mismo catálogo se lea igual en el test y en el teléfono.
     */
    private fun texto(o: JSONObject, clave: String): String {
        if (!o.has(clave) || o.isNull(clave)) return ""
        return o.get(clave).toString()
    }

    private fun numero(o: JSONObject, clave: String, id: String): Float? {
        if (!o.has(clave) || o.isNull(clave)) return null
        val crudo = o.get(clave)
        if (crudo is Number) return crudo.toFloat()
        return crudo.toString().toFloatOrNull()
            ?: throw CatalogError("$id: \"$clave\" = \"$crudo\" no es un número.")
    }

    private fun booleano(o: JSONObject, clave: String, porDefecto: Boolean): Boolean {
        if (!o.has(clave) || o.isNull(clave)) return porDefecto
        val crudo = o.get(clave)
        if (crudo is Boolean) return crudo
        return when (crudo.toString().lowercase()) {
            "true" -> true
            "false" -> false
            else -> porDefecto
        }
    }

    private fun lista(o: JSONObject, clave: String): List<String> {
        val arr = o.opt(clave) as? JSONArray ?: return emptyList()
        return (0 until arr.length())
            .mapNotNull { if (arr.isNull(it)) null else arr.get(it).toString().lowercase().trim() }
            .filter { it.isNotEmpty() }
    }
}
