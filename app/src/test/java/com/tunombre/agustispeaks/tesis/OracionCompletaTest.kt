package com.tunombre.agustispeaks.tesis

import com.tunombre.agustispeaks.data.model.Pictogram
import com.tunombre.agustispeaks.data.seed.SeedData
import com.tunombre.agustispeaks.phrase.PhraseController
import com.tunombre.agustispeaks.phrase.tapPictogram
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Criterio 9 de la bitácora — Emisión de la oración completa.
 *
 * La regla central del producto: al tocar un pictograma, el motor de voz recibe
 * **la oración acumulada entera**, no la palabra suelta. Se verifica para cada
 * pictograma de la semilla.
 *
 * ### Qué ejercita
 *
 * [tapPictogram], que es exactamente lo que llama el tablero al tocar un
 * pictograma, con el [PhraseController] real. Lo único de mentira es el motor
 * de voz: una función que anota lo que recibe. Así la prueba ve lo mismo que
 * vería `TtsManager.speak` en el teléfono.
 *
 * ### Dos recorridos, porque son dos formas de fallar distintas
 *
 * - **Aislado**: cada pictograma sobre una oración vacía. Detecta que algún
 *   texto se pierda o se transforme en el camino.
 * - **Acumulado**: todos seguidos sobre la misma oración. Detecta que se
 *   entregue solo la última palabra, que es justo lo que la regla prohíbe y lo
 *   que el recorrido aislado no puede ver, porque ahí oración y palabra
 *   coinciden.
 *
 * Un pictograma cumple si cumple en los dos.
 *
 * ### Qué no cubre
 *
 * Que el motor de voz de Android efectivamente pronuncie lo que recibe. Eso
 * necesita el teléfono y lo cubren la latencia (criterio 10) y el
 * funcionamiento sin red (criterio 11).
 */
class OracionCompletaTest {

    private val ap = Apendice("criterio-09-oracion")

    private data class Resultado(val pictograma: Pictogram, val aislado: String?, val acumulado: String?) {
        val cumple: Boolean get() = aislado == null && acumulado == null
    }

    @Test
    fun cadaPictogramaEntregaLaOracionCompleta() = try {
        medir()
    } finally {
        ap.guardar()
    }

    private fun medir() {
        val todos = SeedData.coreWords + SeedData.pictograms

        ap.encabezado("CRITERIO 9 — EMISIÓN DE LA ORACIÓN COMPLETA", "OracionCompletaTest")
        linea("Umbral: 100 % de los pictogramas entregan al motor de voz la oración")
        linea("acumulada completa.")
        linea("")

        val aislados = recorridoAislado(todos)
        val acumulados = recorridoAcumulado(todos)
        val resultados = todos.map { Resultado(it, aislados[it.id], acumulados[it.id]) }
        val cumplen = resultados.count { it.cumple }

        dato("Pictogramas de la semilla", todos.size)
        dato("  palabras núcleo", SeedData.coreWords.size)
        dato("  temáticos", SeedData.pictograms.size)
        dato("Cumplen en el recorrido aislado", todos.size - aislados.size)
        dato("Cumplen en el recorrido acumulado", todos.size - acumulados.size)
        dato("Cumplen en los dos", cumplen)
        linea("")

        val fallas = resultados.filterNot { it.cumple }
        if (fallas.isEmpty()) {
            linea("Sin fallas: cada pulsación entregó la oración entera, una sola vez,")
            linea("y Recientes recibió la misma oración que el motor de voz.")
        } else {
            linea("=".repeat(72))
            linea("FALLAS (${fallas.size})")
            linea("=".repeat(72))
            for (f in fallas) {
                linea("  \"${f.pictograma.text}\" (${f.pictograma.id})")
                f.aislado?.let { linea("      aislado: $it") }
                f.acumulado?.let { linea("      acumulado: $it") }
            }
        }

        val porcentaje = 100.0 * cumplen / todos.size
        ap.veredicto(
            9, cumplen == todos.size,
            String.format(java.util.Locale.ROOT, "%.1f %%", porcentaje) + " ($cumplen de ${todos.size} pictogramas)"
        )
        assertTrue(
            "${todos.size - cumplen} pictogramas no entregan la oración completa. " +
                "El umbral es el 100 %.",
            cumplen == todos.size
        )
    }

    /** Cada pictograma sobre una oración vacía. Devuelve la falla por id. */
    private fun recorridoAislado(todos: List<Pictogram>): Map<String, String> {
        val fallas = linkedMapOf<String, String>()
        for (p in todos) {
            val frase = PhraseController()
            val voz = mutableListOf<String>()
            val recientes = mutableListOf<String>()
            val devuelta = tapPictogram(p.text, frase, speak = { voz += it }, record = { recientes += it })

            val problema = when {
                voz.size != 1 -> "el motor de voz recibió ${voz.size} entregas, se esperaba 1"
                voz[0] != p.text -> "el motor de voz recibió \"${voz[0]}\", se esperaba \"${p.text}\""
                devuelta != voz[0] -> "tapPictogram devolvió \"$devuelta\" y entregó \"${voz[0]}\""
                recientes != voz -> "Recientes recibió $recientes, distinto de lo que se dijo"
                frase.currentPhrase() != p.text -> "la oración quedó en \"${frase.currentPhrase()}\""
                else -> null
            }
            if (problema != null) fallas[p.id] = problema
        }
        return fallas
    }

    /** Todos seguidos sobre la misma oración. Devuelve la falla por id. */
    private fun recorridoAcumulado(todos: List<Pictogram>): Map<String, String> {
        val fallas = linkedMapOf<String, String>()
        val frase = PhraseController()
        val voz = mutableListOf<String>()
        val tocados = mutableListOf<String>()

        for (p in todos) {
            val antes = voz.size
            tapPictogram(p.text, frase, speak = { voz += it }, record = {})
            tocados += p.text
            val esperada = tocados.joinToString(" ")

            val problema = when {
                voz.size != antes + 1 -> "el motor de voz recibió ${voz.size - antes} entregas, se esperaba 1"
                voz.last() == p.text && tocados.size > 1 -> "se entregó solo la última palabra, \"${p.text}\""
                voz.last() != esperada -> "se entregó \"${voz.last()}\", se esperaba la oración de ${tocados.size} pictogramas"
                else -> null
            }
            if (problema != null) fallas[p.id] = problema
        }
        return fallas
    }

    private fun linea(s: String) = ap.linea(s)

    private fun dato(etiqueta: String, valor: Any) =
        linea("  " + etiqueta.padEnd(40, '.') + " " + valor.toString().padStart(10))
}
