package com.tunombre.agustispeaks.avatar.catalog

import com.tunombre.agustispeaks.avatar.AvatarAction
import com.tunombre.agustispeaks.avatar.AvatarPose
import com.tunombre.agustispeaks.avatar.BodyPart
import com.tunombre.agustispeaks.avatar.Prop
import com.tunombre.agustispeaks.avatar.ResolvedAction
import com.tunombre.agustispeaks.avatar.avatarPoseFor
import org.junit.Assert.fail
import org.junit.Test
import java.io.File
import java.util.Locale
import kotlin.math.abs

/**
 * La red de seguridad del refactor. Compara, acción por acción y fase por fase,
 * la pose que produce el motor de siempre contra la que produce el intérprete
 * leyendo el catálogo que se le entrega a la app.
 *
 * ### Mide el archivo que se instala, no uno recién generado
 *
 * Lee `src/main/assets/animaciones.json`, que es el que entra en el APK. Si en
 * cambio regenerara el catálogo y lo comparara contra el motor, estaría
 * comprobando que el generador coincide consigo mismo, y pasaría aunque el
 * archivo instalado estuviera vacío o a medio editar.
 *
 * ### Mide en fases distintas de las que generaron el catálogo
 *
 * El volcado se armó muestreando en t = k/32. Verificar en esos mismos puntos
 * no probaría nada: son justamente los valores que quedaron escritos, así que
 * coincidirían por construcción y el error entre medio quedaría sin mirar.
 * Acá la grilla son 501 fases corridas 0.37 de paso, que no toca ningún k/32.
 *
 * ### Qué tolera y qué no
 *
 * Los campos continuos se comparan contra el 1% del recorrido que ese campo
 * tiene en esa acción, con un piso de 0.002. Un 1% es invisible tanto en una
 * fracción de 0 a 1 como en una inclinación de treinta y pico de grados.
 *
 * Los campos discretos **no toleran nada**. Una expresión, un objeto en la mano
 * o una postura o son la misma o no lo son.
 *
 * Si una acción se pasa, lo que hace falta son más fotogramas clave en ese
 * registro. Subir la tolerancia hasta que el test pase convertiría la red de
 * seguridad en un adorno.
 */
class PoseCatalogParityTest {

    private val muestras = 501
    private fun fase(k: Int) = (k + 0.37f) / muestras

    private val piso = 0.002f
    private val fraccion = 0.01f

    private data class Falla(
        val caso: String,
        val campo: String,
        val detalle: String,
        val error: Float,
        val tolerancia: Float
    ) {
        val margen: Float get() = if (tolerancia > 0f) error / tolerancia else 0f
    }

    @Test
    fun elCatalogoReproduceElMotor() {
        val catalogo = cargarCatalogo()
        val fallas = mutableListOf<Falla>()
        val medidas = mutableListOf<Falla>()

        for (accion in AvatarAction.entries) {
            for (caso in casos(accion)) {
                val etiqueta = etiqueta(caso)
                val animacion = catalogo.byId(catalogIdOf(caso))
                if (animacion == null) {
                    fallas += Falla(etiqueta, "registro", "no está en el catálogo", 1f, 0f)
                    continue
                }
                if (animacion.loopMillis != accion.loopMillis) {
                    fallas += Falla(
                        etiqueta, "loopMillis",
                        "catálogo ${animacion.loopMillis} contra motor ${accion.loopMillis}", 1f, 0f
                    )
                }

                val viejas = Array(muestras) { avatarPoseFor(caso, fase(it)) }
                val nuevas = Array(muestras) { animacion.poseAt(fase(it), caso) }

                medidas += continuos(etiqueta, viejas, nuevas)
                fallas += discretos(etiqueta, viejas, nuevas)
            }
        }

        fallas += medidas.filter { it.error > it.tolerancia }
        informe(medidas, fallas)

        if (fallas.isNotEmpty()) {
            fail(
                "${fallas.size} desvíos entre el catálogo y el motor. " +
                    "Los registros señalados necesitan más fotogramas clave; " +
                    "la tolerancia no se toca."
            )
        }
    }

    /* ---------------- comparaciones ---------------- */

    /** Peor desvío de cada campo continuo, se pase o no de la tolerancia. */
    private fun continuos(
        etiqueta: String,
        viejas: Array<AvatarPose>,
        nuevas: Array<AvatarPose>
    ): List<Falla> = FloatField.entries.mapNotNull { campo ->
        var minimo = Float.MAX_VALUE
        var maximo = -Float.MAX_VALUE
        var peor = 0f
        var dondePeor = 0
        for (k in 0 until muestras) {
            val v = campo.of(viejas[k])
            if (v < minimo) minimo = v
            if (v > maximo) maximo = v
            val e = abs(v - campo.of(nuevas[k]))
            if (e > peor) {
                peor = e
                dondePeor = k
            }
        }
        val recorrido = maximo - minimo
        // Un campo que el motor nunca mueve y el catálogo tampoco no aporta
        // nada al informe, pero si el catálogo lo mueve hay que verlo.
        if (recorrido == 0f && peor == 0f) return@mapNotNull null
        Falla(
            caso = etiqueta,
            campo = campo.key,
            detalle = "en t=${num(fase(dondePeor))} (recorrido ${num(recorrido)})",
            error = peor,
            tolerancia = maxOf(piso, fraccion * recorrido)
        )
    }

    /** Cualquier diferencia en un campo discreto es una falla, sin tolerancia. */
    private fun discretos(
        etiqueta: String,
        viejas: Array<AvatarPose>,
        nuevas: Array<AvatarPose>
    ): List<Falla> = StepField.entries.mapNotNull { campo ->
        for (k in 0 until muestras) {
            val viejo = campo.of(viejas[k])
            val nuevo = campo.of(nuevas[k])
            if (viejo != nuevo) {
                return@mapNotNull Falla(
                    caso = etiqueta,
                    campo = campo.key,
                    detalle = "en t=${num(fase(k))}: motor $viejo, catálogo $nuevo",
                    error = 1f,
                    tolerancia = 0f
                )
            }
        }
        null
    }

    /* ---------------- casos ---------------- */

    /**
     * Además de la acción pelada, los caminos que dependen de la frase: la parte
     * del cuerpo que viaja hasta la pose y el objeto que el resolvedor impone
     * por encima del que trae el registro. Sin estos casos, `usaParteDelCuerpo`
     * y la precedencia del objeto quedarían sin verificar, y son las dos reglas
     * que hacen que pedir sopa muestre una sopa.
     */
    private fun casos(accion: AvatarAction): List<ResolvedAction> = when (accion) {
        AvatarAction.HURT, AvatarAction.TOUCH_PART -> listOf(
            ResolvedAction(accion),
            ResolvedAction(accion, bodyPart = BodyPart.LEG),
            ResolvedAction(accion, bodyPart = BodyPart.BELLY)
        )
        AvatarAction.EAT -> listOf(
            ResolvedAction(accion),
            ResolvedAction(accion, prop = Prop.SOUP)
        )
        AvatarAction.DRINK -> listOf(
            ResolvedAction(accion),
            ResolvedAction(accion, prop = Prop.MILK)
        )
        // Vestirse cambia de postura según dónde va la prenda: agachado para
        // los zapatos, erguido para el gorro. Si solo se probara el caso pelado,
        // esa bifurcación quedaría sin verificar.
        AvatarAction.DRESS -> listOf(
            ResolvedAction(accion),
            ResolvedAction(accion, bodyPart = BodyPart.HEAD, prop = Prop.HAT),
            ResolvedAction(accion, bodyPart = BodyPart.FOOT, prop = Prop.SHOES),
            ResolvedAction(accion, bodyPart = BodyPart.LEG, prop = Prop.PANTS)
        )
        else -> listOf(ResolvedAction(accion))
    }

    private fun etiqueta(r: ResolvedAction): String = buildString {
        append(r.action.name.lowercase())
        r.bodyPart?.let { append(" +").append(it.name.lowercase()) }
        r.prop?.let { append(" +").append(it.name.lowercase()) }
    }

    /* ---------------- carga ---------------- */

    private fun cargarCatalogo(): AnimationCatalog {
        val archivo = File("src/main/assets/animaciones.json")
        if (!archivo.exists()) {
            fail(
                "No existe ${archivo.absolutePath}. " +
                    "Corré primero PoseCatalogDumpTest, que lo genera."
            )
        }
        val leido = AnimationParser.parse(archivo.readText())
        if (leido.problemas.isNotEmpty()) {
            leido.problemas.forEach { println("PROBLEMA DE LECTURA: $it") }
            fail("El catálogo instalado tiene ${leido.problemas.size} registros ilegibles.")
        }
        println("Catálogo bajo prueba: ${leido.records.size} registros, ${archivo.absolutePath}")
        return AnimationCatalog(leido.records)
    }

    /* ---------------- informe ---------------- */

    private fun informe(medidas: List<Falla>, fallas: List<Falla>) {
        println("Fases comparadas por acción: $muestras (grilla corrida, ninguna coincide con k/32)")
        println("Tolerancia: 1% del recorrido del campo, piso $piso")
        println()

        println("Los diez campos con menos aire:")
        println("  %-26s %-11s %9s %9s %7s".format(Locale.US, "caso", "campo", "error", "tolera", "usa"))
        medidas.sortedByDescending { it.margen }.take(10).forEach {
            println(
                "  %-26s %-11s %9s %9s %6.1f%%".format(
                    Locale.US, it.caso, it.campo, num(it.error), num(it.tolerancia), it.margen * 100f
                )
            )
        }
        println()

        if (fallas.isEmpty()) {
            println("Sin desvíos. El catálogo reproduce el motor en las 66 acciones.")
            return
        }

        println("DESVÍOS (${fallas.size}):")
        fallas.sortedByDescending { it.margen }.forEach {
            println("  ${it.caso} / ${it.campo}: error ${num(it.error)}, tolera ${num(it.tolerancia)}")
            println("      ${it.detalle}")
        }
        println()
        println("Cada uno de estos registros necesita más fotogramas clave.")
    }

    private fun num(v: Float): String = String.format(Locale.US, "%.5f", v)
}
