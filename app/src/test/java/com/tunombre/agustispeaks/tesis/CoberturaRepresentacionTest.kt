package com.tunombre.agustispeaks.tesis

import com.tunombre.agustispeaks.avatar.AvatarAction
import com.tunombre.agustispeaks.avatar.AvatarPose
import com.tunombre.agustispeaks.avatar.ResolvedAction
import com.tunombre.agustispeaks.avatar.catalog.AnimationCatalog
import com.tunombre.agustispeaks.avatar.catalog.AnimationParser
import com.tunombre.agustispeaks.avatar.catalog.FloatField
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.util.Locale

/**
 * Criterio 4 de la bitácora — Cobertura de representación.
 *
 * Todas las acciones tienen que producir una pose válida en **toda** la grilla de
 * fases: sin lanzar excepciones, sin NaN ni infinitos, sin valores fuera del
 * rango que cada campo declara en [AvatarPose], y con una duración de bucle
 * positiva.
 *
 * ### Qué agrega esto sobre la prueba de equivalencia
 *
 * La equivalencia compara el catálogo contra el motor: si los dos estuvieran mal
 * de la misma manera, pasaría igual. Esta mira la pose **en sí misma**, contra el
 * contrato que el propio tipo declara en sus comentarios. Son preguntas distintas:
 * una es "¿dice lo mismo que antes?" y la otra "¿lo que dice tiene sentido?".
 *
 * ### Por qué también evalúa fuera de [0, 1]
 *
 * El ciclo de dibujo calcula la fase a partir del reloj y no hay nada que
 * garantice que caiga dentro del intervalo. `poseAt` lo contempla, y una prueba
 * que solo mirara el intervalo bonito dejaría sin verificar justo el caso que
 * ocurre en un dispositivo real cuando una animación arranca a mitad de bucle.
 */
class CoberturaRepresentacionTest {

    private val ap = Apendice("criterio-04-representacion")

    private val muestras = 1024

    /** Fase dentro del ciclo, cubriendo los dos extremos. */
    private fun fase(k: Int) = k / (muestras - 1).toFloat()

    /** Fases fuera del intervalo: el reloj no pide permiso. */
    private val fueraDeRango = listOf(-2.5f, -1f, -0.3f, 1.0001f, 1.7f, 3f)

    /**
     * Rango declarado de cada campo continuo, según los comentarios de
     * [AvatarPose]. `null` es un campo sin rango: `headTilt` va en grados y no
     * tiene tope documentado, así que de ese solo se exige que sea finito.
     */
    private val rangos: Map<FloatField, Pair<Float, Float>?> = mapOf(
        FloatField.BOB to (-1f to 1f),
        FloatField.LEAN to (-1f to 1f),
        FloatField.HEAD_TILT to null,
        FloatField.JAW to (0f to 1f),
        FloatField.TEARS to (0f to 1f),
        FloatField.EYE_CLOSE to (0f to 1f),
        FloatField.CROUCH to (0f to 1f),
        FloatField.LEG_SWING to (-1f to 1f),
        FloatField.ARM_SWING to (-1f to 1f),
        FloatField.REACH to (0f to 1f),
        FloatField.PROP_PHASE to (0f to 1f),
        FloatField.FX_PULSE to (0f to 1f)
    )

    private data class Problema(val accion: String, val detalle: String)

    @Test
    fun todasLasAccionesProducenUnaPoseValida() {
        val catalogo = cargarCatalogo()
        val problemas = mutableListOf<Problema>()

        // Extremos observados de cada campo, sobre el motor entero. No es un
        // criterio: es documentación del rango que el sistema usa de verdad.
        val minimos = HashMap<FloatField, Float>()
        val maximos = HashMap<FloatField, Float>()

        val fases = (0 until muestras).map { fase(it) } + fueraDeRango

        for (accion in AvatarAction.entries) {
            val r = ResolvedAction(accion)
            val animacion = catalogo.byId(accion.name.lowercase())
            if (animacion == null) {
                problemas += Problema(accion.name, "no tiene registro en el catálogo")
                continue
            }
            if (animacion.loopMillis <= 0) {
                problemas += Problema(accion.name, "loopMillis ${animacion.loopMillis} no es positivo")
            }
            if (accion.loopMillis <= 0) {
                problemas += Problema(accion.name, "loopMillis del enum ${accion.loopMillis} no es positivo")
            }

            for (t in fases) {
                val pose = try {
                    animacion.poseAt(t, r)
                } catch (e: Exception) {
                    problemas += Problema(accion.name, "excepción en t=${num(t)}: ${e::class.simpleName} ${e.message}")
                    continue
                }
                revisar(accion.name, t, pose, problemas, minimos, maximos)
            }
        }

        informe(fases.size, problemas, minimos, maximos)

        // El umbral nombra tres conteos. La aserción de abajo es más estricta:
        // también falla por un registro faltante o un bucle no positivo. Esos
        // casos se informan aparte en el detalle, y en la práctica ya los
        // descarta el criterio 1.
        val fueraDeRangoN = problemas.count { it.detalle.contains("fuera de") }
        val excepcionesN = problemas.count { it.detalle.startsWith("excepción") }
        val noFinitosN = problemas.count { it.detalle.contains("no es finito") }
        val otrosN = problemas.size - fueraDeRangoN - excepcionesN - noFinitosN
        ap.veredicto(
            4, fueraDeRangoN == 0 && excepcionesN == 0 && noFinitosN == 0,
            "$fueraDeRangoN fuera de rango, $excepcionesN excepciones, $noFinitosN no finitos" +
                if (otrosN > 0) " ($otrosN problemas de otro tipo)" else ""
        )
        ap.guardar()

        assertTrue(
            "${problemas.size} poses inválidas. Una pose fuera de rango no es un " +
                "detalle numérico: es un miembro dibujado donde no va.",
            problemas.isEmpty()
        )
    }

    /* ------------------------------------------------------------------ */

    private fun revisar(
        accion: String,
        t: Float,
        pose: AvatarPose,
        problemas: MutableList<Problema>,
        minimos: HashMap<FloatField, Float>,
        maximos: HashMap<FloatField, Float>
    ) {
        for (campo in FloatField.entries) {
            val v = campo.of(pose)

            if (v.isNaN() || v.isInfinite()) {
                problemas += Problema(accion, "${campo.key} no es finito en t=${num(t)}: $v")
                continue
            }
            minimos[campo] = minOf(minimos[campo] ?: v, v)
            maximos[campo] = maxOf(maximos[campo] ?: v, v)

            val rango = rangos[campo] ?: continue
            val (bajo, alto) = rango
            // Margen de un milésimo: el intérprete interpola en coma flotante y
            // un 1.0000001 al tocar el tope es ruido de representación, no un
            // valor fuera de rango.
            if (v < bajo - 0.001f || v > alto + 0.001f) {
                problemas += Problema(
                    accion,
                    "${campo.key} = ${num(v)} fuera de [$bajo, $alto] en t=${num(t)}"
                )
            }
        }
    }

    private fun informe(
        fasesPorAccion: Int,
        problemas: List<Problema>,
        minimos: Map<FloatField, Float>,
        maximos: Map<FloatField, Float>
    ) {
        ap.encabezado("CRITERIO 4 — COBERTURA DE REPRESENTACIÓN", "CoberturaRepresentacionTest")
        dato("Acciones evaluadas", AvatarAction.entries.size)
        dato("Fases por acción", fasesPorAccion)
        dato("  dentro del ciclo", muestras)
        dato("  fuera del intervalo [0, 1]", fueraDeRango.size)
        dato("Poses evaluadas", AvatarAction.entries.size * fasesPorAccion)
        dato("Campos revisados por pose", FloatField.entries.size)
        dato("Comprobaciones", AvatarAction.entries.size * fasesPorAccion * FloatField.entries.size)
        linea("")
        dato("Excepciones", problemas.count { it.detalle.startsWith("excepción") })
        dato("Valores no finitos", problemas.count { it.detalle.contains("no es finito") })
        dato("Valores fuera de rango", problemas.count { it.detalle.contains("fuera de") })
        dato("loopMillis no positivos", problemas.count { it.detalle.contains("loopMillis") })
        dato("TOTAL de problemas", problemas.size)

        linea("")
        linea("=".repeat(72))
        linea("RECORRIDO REAL DE CADA CAMPO, SOBRE LAS ${AvatarAction.entries.size} ACCIONES")
        linea("=".repeat(72))
        linea("No es un criterio: documenta cuánto del rango declarado se usa.")
        linea("")
        linea("CAMPO".padEnd(14) + "DECLARADO".padEnd(14) + "MÍNIMO".padStart(11) + "MÁXIMO".padStart(11))
        linea("-".repeat(50))
        for (campo in FloatField.entries) {
            val rango = rangos[campo]
            val declarado = if (rango == null) "sin tope" else "[${num1(rango.first)}, ${num1(rango.second)}]"
            linea(
                campo.key.padEnd(14) + declarado.padEnd(14) +
                    num(minimos[campo] ?: 0f).padStart(11) +
                    num(maximos[campo] ?: 0f).padStart(11)
            )
        }
        linea("-".repeat(50))

        linea("")
        if (problemas.isEmpty()) {
            linea("Sin problemas: las ${AvatarAction.entries.size} acciones producen una pose")
            linea("válida en toda la grilla, dentro y fuera del intervalo del ciclo.")
            return
        }
        linea("=".repeat(72))
        linea("PROBLEMAS (${problemas.size})")
        linea("=".repeat(72))
        // Solo el primero de cada acción y campo: un valor fuera de rango suele
        // repetirse en cientos de fases seguidas y la lista entera no agrega nada.
        problemas.distinctBy { it.accion + it.detalle.substringBefore(" en t=") }
            .forEach { linea("  ${it.accion.padEnd(14)}${it.detalle}") }
    }

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
        linea("  " + etiqueta.padEnd(40, '.') + " " + valor.toString().padStart(12))

    private fun num(v: Float): String = String.format(Locale.ROOT, "%.4f", v)

    private fun num1(v: Float): String = String.format(Locale.ROOT, "%.0f", v)
}
