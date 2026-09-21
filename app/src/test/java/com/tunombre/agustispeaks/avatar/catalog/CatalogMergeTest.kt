package com.tunombre.agustispeaks.avatar.catalog

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * La fusión de las capas del catálogo. Es la única lógica de la Fase 5 con
 * reglas propias: lo demás es Room haciendo de Room.
 *
 * Se prueba acá y no en el dispositivo porque `fusionar` es una función pura
 * sobre listas, sin nada de Android adentro. Que se pueda probar así fue la
 * razón de separarla del objeto que abre la base de datos.
 */
class CatalogMergeTest {

    private fun registro(id: String, loop: Int = 1000, prioridad: Int = 100) =
        AnimationRecord(
            id = id,
            loopMillis = loop,
            prioridad = prioridad,
            fotogramas = listOf(Keyframe(t = 0f))
        )

    @Test
    fun sinNadaGuardadoQuedaLaBaseIntacta() {
        val base = listOf(registro("a"), registro("b"), registro("c"))
        val salida = fusionar(base, emptyList())

        assertEquals(listOf("a", "b", "c"), salida.map { it.id })
        assertEquals(listOf(0, 1, 2), salida.map { it.orden })
    }

    @Test
    fun loGuardadoGanaAnteIdRepetido() {
        val base = listOf(registro("a", loop = 1000), registro("b", loop = 1000))
        val encima = listOf(registro("b", loop = 7777))

        val salida = fusionar(base, encima)

        assertEquals(listOf("a", "b"), salida.map { it.id })
        assertEquals(7777, salida.first { it.id == "b" }.loopMillis)
    }

    @Test
    fun unReemplazoConservaLaPosicionDelOriginal() {
        // Si corregir un registro lo mandara al final, cambiaría el desempate
        // entre reglas de igual prioridad: retocar el dibujo de una animación
        // podría alterar de paso qué frase la dispara. Ese acoplamiento es
        // justamente lo que esta prueba existe para impedir.
        val base = listOf(registro("a"), registro("b"), registro("c"))
        val encima = listOf(registro("a", loop = 5555))

        val salida = fusionar(base, encima)

        assertEquals(listOf("a", "b", "c"), salida.map { it.id })
        assertEquals(0, salida.first { it.id == "a" }.orden)
        assertEquals(5555, salida.first { it.id == "a" }.loopMillis)
    }

    @Test
    fun lasAnimacionesNuevasVanDespuesDeLasDelApk() {
        val base = listOf(registro("a"), registro("b"))
        val encima = listOf(registro("z"), registro("y"))

        val salida = fusionar(base, encima)

        assertEquals(listOf("a", "b", "z", "y"), salida.map { it.id })
        assertEquals(listOf(0, 1, 2, 3), salida.map { it.orden })
    }

    @Test
    fun elOrdenFusionadoDesempataPorPrioridadEnElCatalogo() {
        // Dos reglas que la misma frase podría disparar, con igual prioridad.
        // Gana la que viene primero en la lista fusionada.
        val base = listOf(
            registro("primera", prioridad = 5).copy(palabras = listOf("hola")),
            registro("segunda", prioridad = 5).copy(palabras = listOf("hola"))
        )
        val catalogo = AnimationCatalog(fusionar(base, emptyList()))

        assertEquals("primera", catalogo.match(" hola ")?.id)
    }

    @Test
    fun unaAnimacionGuardadaPuedeAdelantarseBajandoLaPrioridad() {
        val base = listOf(registro("vieja", prioridad = 5).copy(palabras = listOf("hola")))
        val encima = listOf(registro("nueva", prioridad = 1).copy(palabras = listOf("hola")))

        val catalogo = AnimationCatalog(fusionar(base, encima))

        assertEquals("nueva", catalogo.match(" hola ")?.id)
    }
}
