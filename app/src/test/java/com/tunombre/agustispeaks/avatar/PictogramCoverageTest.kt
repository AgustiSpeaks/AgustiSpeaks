package com.tunombre.agustispeaks.avatar

import com.tunombre.agustispeaks.data.seed.SeedData
import org.junit.Test

/**
 * Informe de cobertura: qué pictograma resuelve a qué animación.
 *
 * No verifica nada ni falla nunca. Su producto es la lista de pictogramas que
 * caen en `TALK`, que es la acción por defecto del resolvedor: "no reconocí
 * esta palabra, así que el avatar habla". Un pictograma en esa lista es uno que
 * el chico toca y el avatar no actúa.
 *
 * Existe porque el vocabulario tiene más de cien palabras y rastrearlas a mano
 * contra sesenta reglas es lento y se presta a saltearse casos. La pregunta
 * "¿a cuáles les falta animación?" va a volver cada vez que se agregue
 * vocabulario, así que conviene que la conteste una máquina.
 */
class PictogramCoverageTest {

    @Test
    fun informeDeCobertura() {
        val todos = SeedData.coreWords + SeedData.pictograms

        val porCategoria = todos.groupBy { it.categoryId }
        val sinAnimacion = mutableListOf<Pair<String, String>>()

        println("=== Resolución de cada pictograma ===")
        for ((categoria, items) in porCategoria.entries.sortedBy { it.key }) {
            println()
            println("[$categoria]")
            for (p in items.sortedBy { it.text }) {
                val r = ActionResolver.resolve(p.text)
                val detalle = buildString {
                    append(r.action.name)
                    r.bodyPart?.let { append(" +").append(it.name) }
                    r.prop?.let { append(" +").append(it.name) }
                }
                val marca = if (r.action == AvatarAction.TALK) "  <-- SIN ANIMACION" else ""
                println("  %-24s %s%s".format(p.text, detalle, marca))
                if (r.action == AvatarAction.TALK) sinAnimacion += categoria to p.text
            }
        }

        println()
        println("======================================")
        println("Total de pictogramas: ${todos.size}")
        println("Sin animación propia: ${sinAnimacion.size}")
        println()
        for ((categoria, items) in sinAnimacion.groupBy { it.first }.entries.sortedBy { it.key }) {
            println("  $categoria (${items.size}): " + items.joinToString(", ") { it.second })
        }

        println()
        println("=== Acciones distintas en uso ===")
        val uso = todos.groupingBy { ActionResolver.resolve(it.text).action }.eachCount()
        uso.entries.sortedByDescending { it.value }.forEach { (accion, n) ->
            println("  %-14s %d".format(accion.name, n))
        }
        val sinUsar = AvatarAction.entries.filter { it !in uso.keys }
        println()
        println("Acciones que existen pero ningún pictograma dispara (${sinUsar.size}):")
        println("  " + sinUsar.joinToString(", ") { it.name })
    }
}
