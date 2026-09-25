package com.tunombre.agustispeaks.tesis

import com.tunombre.agustispeaks.avatar.ActionResolver
import com.tunombre.agustispeaks.avatar.AvatarAction
import com.tunombre.agustispeaks.avatar.BodyPart
import com.tunombre.agustispeaks.avatar.Prop
import com.tunombre.agustispeaks.avatar.ResolvedAction
import com.tunombre.agustispeaks.avatar.catalog.AnimationCatalog
import com.tunombre.agustispeaks.avatar.catalog.AnimationParser
import com.tunombre.agustispeaks.data.model.Pictogram
import com.tunombre.agustispeaks.data.seed.SeedData
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.util.Locale

/**
 * APÉNDICE F.2 — Cobertura semántica y distinguibilidad.
 *
 * Recorre los 170 pictogramas del vocabulario, resuelve cada uno como lo hace la
 * app e informa, por categoría, cuántos caen en la acción genérica y cuántas
 * animaciones distintas produce la categoría.
 *
 * ### Por qué mide dos cosas y no una
 *
 * **Cobertura** es cuántos pictogramas tienen una animación propia en vez de caer
 * en `TALK`, que es mover la boca y no decir nada. **Distinguibilidad** es si dos
 * pictogramas distintos de una misma categoría producen exactamente la misma
 * animación. Las dos pueden ir en direcciones opuestas: una categoría puede tener
 * cobertura perfecta y ser inútil igual, si todos sus pictogramas se ven idénticos.
 * Eso ya pasó acá: "juego" resolvía entero a `PLAY`, con cobertura del 100 %, y
 * el tobogán, la hamaca y los juguetes se veían exactamente iguales.
 *
 * ### Esta prueba informa, no juzga
 *
 * No hay umbral, a propósito. El umbral se fija mirando el dominio, no la salida,
 * y fijarlo después de conocer el resultado sería elegir el número que la prueba
 * ya aprueba. Lo único que se verifica acá es que la medición pudo hacerse.
 */
class CoberturaSemanticaTest {

    private val ap = Apendice("F2-cobertura")

    private val generica = AvatarAction.TALK

    /** Lo que se compara para decidir si dos pictogramas se ven igual. */
    private data class Animacion(
        val accion: AvatarAction,
        val parte: BodyPart?,
        val objeto: Prop?
    ) {
        override fun toString(): String = buildString {
            append(accion.name.lowercase())
            parte?.let { append(" +").append(it.name.lowercase()) }
            objeto?.let { append(" +").append(it.name.lowercase()) }
        }
    }

    private data class Fila(val pictograma: Pictogram, val animacion: Animacion)

    private lateinit var catalogo: AnimationCatalog

    @Test
    fun coberturaYDistinguibilidad() = try {
        medir()
    } finally {
        ap.guardar()
    }

    private fun medir() {
        catalogo = cargarCatalogo()

        linea("APÉNDICE F.2 — COBERTURA SEMÁNTICA Y DISTINGUIBILIDAD")
        linea("AgustiSpeaks · generado por CoberturaSemanticaTest")
        linea("Acción genérica: ${generica.name} (mover la boca sin decir nada)")
        linea("")
        linea("Sin umbral: esta prueba mide, no aprueba. El criterio se fija aparte.")
        linea("")

        // El vocabulario entero, con las palabras núcleo como grupo propio: no
        // pertenecen a ningún lugar porque están en todos.
        val grupos = linkedMapOf<String, List<Pictogram>>()
        grupos["núcleo"] = SeedData.coreWords
        for (c in SeedData.categories) {
            grupos[c.name] = SeedData.pictograms.filter { it.categoryId == c.id }
        }

        val resueltos = grupos.mapValues { (_, pics) ->
            pics.map { Fila(it, animacionDe(it.text)) }
        }

        resumen(resueltos)
        linea("")
        sinAnimacionPropia(resueltos)
        linea("")
        indistinguibles(resueltos)

        val total = resueltos.values.sumOf { it.size }
        assertTrue(
            "Se esperaban 170 pictogramas y se recorrieron $total.",
            total == SeedData.coreWords.size + SeedData.pictograms.size
        )
    }

    /* ------------------------------------------------------------------ */

    private fun resumen(datos: Map<String, List<Fila>>) {
        linea("=".repeat(76))
        linea("RESUMEN POR CATEGORÍA")
        linea("=".repeat(76))
        linea(
            "CATEGORÍA".padEnd(20) + "TOTAL".padStart(7) + "GENÉRICA".padStart(10) +
                "PROPIA".padStart(9) + "ACCIONES".padStart(10) + "PARES=".padStart(9)
        )
        linea("-".repeat(65))

        var total = 0
        var genericos = 0
        for ((nombre, filas) in datos) {
            val gen = filas.count { it.animacion.accion == generica }
            val distintas = filas.map { it.animacion.accion }.toSet().size
            val pares = paresIguales(filas).size
            total += filas.size
            genericos += gen
            linea(
                nombre.padEnd(20) + filas.size.toString().padStart(7) +
                    gen.toString().padStart(10) +
                    (filas.size - gen).toString().padStart(9) +
                    distintas.toString().padStart(10) +
                    pares.toString().padStart(9)
            )
        }
        linea("-".repeat(65))
        linea(
            "TOTAL".padEnd(20) + total.toString().padStart(7) +
                genericos.toString().padStart(10) +
                (total - genericos).toString().padStart(9)
        )
        linea("")
        dato("Pictogramas recorridos", total)
        dato("Con animación propia", total - genericos)
        dato("En la acción genérica", genericos)
        dato("Cobertura", porciento(total - genericos, total))
        linea("")
        linea("PROPIA = pictogramas con animación propia. ACCIONES = animaciones")
        linea("distintas que produce la categoría. PARES= = pares de pictogramas de")
        linea("esa categoría que producen exactamente la misma animación.")
    }

    private fun sinAnimacionPropia(datos: Map<String, List<Fila>>) {
        val caidos = datos.entries.flatMap { (nombre, filas) ->
            filas.filter { it.animacion.accion == generica }.map { nombre to it }
        }
        linea("=".repeat(76))
        linea("PICTOGRAMAS SIN ANIMACIÓN PROPIA (${caidos.size})")
        linea("=".repeat(76))
        if (caidos.isEmpty()) {
            linea("  Ninguno: los 170 pictogramas tienen animación propia.")
            return
        }
        linea("CATEGORÍA".padEnd(20) + "PICTOGRAMA")
        linea("-".repeat(60))
        for ((nombre, fila) in caidos) {
            linea(nombre.padEnd(20) + fila.pictograma.text)
        }
    }

    private fun indistinguibles(datos: Map<String, List<Fila>>) {
        linea("=".repeat(76))
        linea("PARES INDISTINGUIBLES DENTRO DE UNA MISMA CATEGORÍA")
        linea("=".repeat(76))
        linea("Dos pictogramas con la misma acción, la misma parte del cuerpo y el")
        linea("mismo objeto: para quien mira, son la misma imagen.")
        linea("")

        var totalPares = 0
        for ((nombre, filas) in datos) {
            val pares = paresIguales(filas)
            if (pares.isEmpty()) continue
            totalPares += pares.size
            linea("$nombre (${pares.size}):")
            for ((a, b, animacion) in pares) {
                linea("  \"$a\" = \"$b\"".padEnd(52) + animacion)
            }
            linea("")
        }
        if (totalPares == 0) {
            linea("  Ninguno.")
            linea("")
        }
        dato("Pares indistinguibles, total", totalPares)
    }

    /**
     * Pares no ordenados de una misma categoría con animación idéntica. Con n
     * pictogramas colapsados en una sola animación salen n(n−1)/2 pares, que es
     * lo correcto: el problema crece más rápido que la cantidad de pictogramas
     * afectados, y el número tiene que reflejarlo.
     */
    private fun paresIguales(filas: List<Fila>): List<Triple<String, String, Animacion>> {
        val salida = mutableListOf<Triple<String, String, Animacion>>()
        for (i in filas.indices) {
            for (j in i + 1 until filas.size) {
                if (filas[i].animacion == filas[j].animacion) {
                    salida += Triple(
                        filas[i].pictograma.text,
                        filas[j].pictograma.text,
                        filas[i].animacion
                    )
                }
            }
        }
        return salida
    }

    /* ------------------------------------------------------------------ */

    /**
     * Reproduce `AvatarCatalog.resolve`: primero las reglas del catálogo, por
     * prioridad, y después `ActionResolver`. Es una copia y no una llamada
     * porque el original carga el catálogo desde un `Context` de Android, que no
     * existe en una prueba de la JVM. Si esa precedencia cambiara en la app,
     * esta copia habría que cambiarla también.
     */
    private fun animacionDe(frase: String): Animacion {
        if (frase.isBlank()) return Animacion(AvatarAction.IDLE, null, null)
        val texto = ActionResolver.normalized(frase)

        catalogo.match(texto)?.let { animacion ->
            val registro = animacion.record
            return Animacion(
                accion = porNombre[registro.id] ?: generica,
                parte = if (registro.usaParteDelCuerpo) ActionResolver.bodyPartOf(texto) else null,
                objeto = registro.prop
            )
        }
        val r: ResolvedAction = ActionResolver.resolve(frase)
        return Animacion(r.action, r.bodyPart, r.prop)
    }

    private val porNombre: Map<String, AvatarAction> =
        AvatarAction.entries.associateBy { it.name.lowercase() }

    private fun cargarCatalogo(): AnimationCatalog {
        val archivo = File("src/main/assets/animaciones.json")
        assertTrue(
            "No existe ${archivo.absolutePath}. Corré PoseCatalogDumpTest primero.",
            archivo.exists()
        )
        val leido = AnimationParser.parse(archivo.readText())
        assertTrue(
            "El catálogo tiene problemas de lectura: ${leido.problemas}",
            leido.problemas.isEmpty()
        )
        return AnimationCatalog(leido.records)
    }

    /* ------------------------------------------------------------------ */

    private fun linea(s: String) = ap.linea(s)

    private fun dato(etiqueta: String, valor: Any) =
        linea("  " + etiqueta.padEnd(40, '.') + " " + valor.toString().padStart(10))

    private fun porciento(parte: Int, total: Int): String {
        if (total == 0) return "0,0 %"
        return String.format(Locale.ROOT, "%.1f %%", 100.0 * parte / total)
    }
}
