package com.tunombre.agustispeaks.avatar.catalog

import com.tunombre.agustispeaks.avatar.AvatarAction
import com.tunombre.agustispeaks.avatar.AvatarPose
import com.tunombre.agustispeaks.avatar.ResolvedAction
import com.tunombre.agustispeaks.avatar.avatarPoseFor
import org.junit.Test
import java.io.File
import java.util.Locale
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/**
 * Generador del catálogo inicial. No verifica nada: su producto es el archivo.
 *
 * Recorre las 66 acciones, llama al `avatarPoseFor` que ya funciona y escribe el
 * JSON equivalente de cada una. El catálogo sale derivado del código que anda,
 * no transcrito a mano, que es donde se cuelan los errores.
 *
 * Se corre desde Android Studio con la flecha del margen, o con
 * `gradlew :app:testDebugUnitTest --tests "*PoseCatalogDumpTest*"`.
 * El resultado queda en `app/build/animaciones.json`.
 *
 * ### Por qué 33 fases y no 9
 *
 * Con nueve fases (t = 0, 0.125 … 1) la señal `fast`, que son ocho ciclos por
 * bucle, cae siempre en un múltiplo exacto de 2π, y el seno de un múltiplo de
 * 2π es cero. Las nueve muestras darían cero, las nueve: el volcado registraría
 * que lavarse las manos es quedarse quieto, y un test que comparara ese cero
 * contra el mismo cero pasaría. Esas acciones son procedurales justamente por
 * eso, y el muestreo es más denso para que ninguna otra caiga en la misma
 * trampa sin que nos demos cuenta.
 */
class PoseCatalogDumpTest {

    /**
     * Grilla de muestreo. Fina a propósito: los quiebres del motor no caen en
     * números redondos. El codo del salto está en t = 0.2015, donde `up` cruza
     * 0.35, y el parpadeo arranca en t = 0.9. Con una grilla gruesa el nodo cae
     * al lado del codo en vez de encima, y la curva sale redondeada donde
     * debería tener una esquina. Ese fue el desvío que encontró el test de
     * paridad, y se arregla acercando el nodo, no cambiando el formato.
     */
    private val muestras = 4097
    private fun fase(k: Int) = k / (muestras - 1).toFloat()

    /** Techo de nodos por campo. Si alguno lo toca, es que no se puede declarar. */
    private val maxNodos = 48

    private companion object {
        const val ASSET = "animaciones.json"
    }

    @Test
    fun volcarCatalogo() {
        val bloques = AvatarAction.entries.map { registro(it) }
        val json = bloques.joinToString(",\n", "[\n", "\n]\n")

        val salida = File("build/animaciones.json")
        salida.parentFile?.mkdirs()
        salida.writeText(json)

        println(json)
        println("──────────────────────────────────────────────")
        println("Catálogo escrito en ${salida.absolutePath}")

        // El que usa la app. Se copia solo la primera vez: a partir de ahí el
        // catálogo es de quien lo edita, y este generador no tiene por qué
        // pisarle el trabajo por haber apretado "Run" sin querer.
        val asset = File("src/main/assets/$ASSET")
        if (asset.exists()) {
            println("$ASSET ya existe y NO se tocó. Para regenerarlo, borralo y volvé a correr.")
        } else {
            asset.parentFile?.mkdirs()
            asset.writeText(json)
            println("Copiado a ${asset.absolutePath} (la app lo lee de ahí).")
        }

        resumen()
        idaYVuelta(json)
    }

    /* ---------------- disparadores migrados ---------------- */

    private data class Disparo(
        val prioridad: Int = 100,
        val palabras: List<String> = emptyList(),
        val raices: List<String> = emptyList()
    )

    /**
     * Primera tanda de disparadores que pasan de código a dato. Son las cuatro
     * reglas de la "sección 0" de `ActionResolver`, y se eligieron justamente
     * porque ya se evaluaban antes que todas las demás: moverlas al catálogo,
     * que también se consulta primero, no puede alterar ningún orden.
     *
     * La prioridad explícita 0, 1, 2, 3 reproduce el orden en que están escritas
     * allá. Dejarlas todas en la misma prioridad no alcanzaría: el desempate
     * sería el orden del enum, donde ASK va última, y entonces "¿cómo estás?
     * bien" caería en GOOD en vez de en ASK.
     *
     * Las reglas siguen existiendo en `ActionResolver`. Si el catálogo no carga,
     * estas frases se resuelven igual que siempre.
     */
    private val disparadores = mapOf(
        AvatarAction.ASK to Disparo(
            prioridad = 0,
            raices = listOf("como estas", "como te sientes", "como andas", "como te va", "que tal")
        ),
        AvatarAction.PLEASE to Disparo(prioridad = 1, raices = listOf("por favor")),
        AvatarAction.GOOD to Disparo(
            prioridad = 2,
            palabras = listOf("bien"),
            raices = listOf("estoy bien", "muy bien", "todo bien")
        ),
        AvatarAction.BAD to Disparo(
            prioridad = 3,
            palabras = listOf("mal"),
            raices = listOf("estoy mal", "me siento mal")
        )
    )

    private fun textos(xs: List<String>): String =
        xs.joinToString(", ", "[", "]") { "\"$it\"" }

    /* ---------------- un registro ---------------- */

    private fun registro(accion: AvatarAction): String {
        val id = accion.name.lowercase()
        val procedural = ProceduralPoses.byAction[accion]
        val usaParte = accion == AvatarAction.HURT ||
            accion == AvatarAction.TOUCH_PART ||
            accion == AvatarAction.DRESS

        val disparo = disparadores[accion] ?: Disparo()
        val cabecera = buildList {
            add("    \"id\": \"$id\"")
            add("    \"loopMillis\": ${accion.loopMillis}")
            add("    \"prioridad\": ${disparo.prioridad}")
            add("    \"palabras\": ${textos(disparo.palabras)}")
            add("    \"raices\": ${textos(disparo.raices)}")
            if (usaParte) add("    \"usaParteDelCuerpo\": true")
        }

        if (procedural != null) {
            return (cabecera + "    \"procedural\": \"$procedural\"" + "    \"fotogramas\": []")
                .joinToString(",\n", "  {\n", "\n  }")
        }

        val poses = Array(muestras) { avatarPoseFor(ResolvedAction(accion), fase(it)) }
        val fotogramas = fotogramas(poses)
        val cuerpo = if (fotogramas.isEmpty()) {
            "    \"fotogramas\": []"
        } else {
            "    \"fotogramas\": [\n" + fotogramas.joinToString(",\n") + "\n    ]"
        }
        return (cabecera + cuerpo).joinToString(",\n", "  {\n", "\n  }")
    }

    /* ---------------- campos continuos y discretos, por separado ---------------- */

    private fun fotogramas(poses: Array<AvatarPose>): List<String> {
        // Continuos: se interpolan, así que se reducen a los puntos que hacen
        // falta, y cada nodo se queda con la curva que mejor describe su tramo.
        val continuos = LinkedHashMap<FloatField, Map<Int, Float>>()
        val curvas = LinkedHashMap<FloatField, Map<Int, Ease>>()
        for (campo in FloatField.entries) {
            val vs = FloatArray(muestras) { campo.of(poses[it]) }
            if (vs.all { abs(it - campo.default) < 1e-6f }) continue
            if (vs.all { abs(it - vs[0]) < 1e-6f }) {
                continuos[campo] = mapOf(0 to vs[0])
                curvas[campo] = mapOf(0 to Ease.COSENO)
                continue
            }
            var kept = reducir(vs)
            // La curva de salida se calcula antes de soltar el nodo de t=1: el
            // último nodo que quede hereda la del tramo que iba hacia allá, que
            // es exactamente el tramo por el que el ciclo va a dar la vuelta.
            val salidas = HashMap<Int, Ease>()
            for (j in 0 until kept.size - 1) salidas[kept[j]] = mejorCurva(vs, kept[j], kept[j + 1])
            // Con ciclo cerrado el punto en t=1 lo cubre la vuelta al de t=0,
            // salvo que haya un salto real ahí (el parpadeo de "idle").
            if (kept.size > 1 && kept.last() == muestras - 1 && abs(vs[muestras - 1] - vs[0]) < 1e-6f) {
                kept = kept.dropLast(1)
            }
            continuos[campo] = kept.associateWith { vs[it] }
            curvas[campo] = kept.associateWith { salidas[it] ?: Ease.COSENO }
        }

        // Discretos: se sostienen, así que solo se anota dónde cambian.
        val discretos = LinkedHashMap<StepField, Map<Int, Any?>>()
        for (campo in StepField.entries) {
            val vs = Array<Any?>(muestras) { campo.of(poses[it]) }
            if (vs.all { it == campo.defaultValue }) continue
            val cambios = LinkedHashMap<Int, Any?>()
            cambios[0] = vs[0]
            for (k in 1 until muestras - 1) if (vs[k] != vs[k - 1]) cambios[k] = vs[k]
            discretos[campo] = cambios
        }

        val instantes = (continuos.values.flatMap { it.keys } + discretos.values.flatMap { it.keys })
            .distinct().sorted()

        // Un fotograma declara una sola curva, así que los campos que salen de
        // un mismo instante con curvas distintas van en objetos separados. El
        // intérprete arma la pista de cada campo por su cuenta, de modo que dos
        // fotogramas con la misma "t" conviven sin pisarse.
        val salida = mutableListOf<String>()
        for (k in instantes) {
            val porCurva = LinkedHashMap<Ease, MutableList<String>>()
            for (campo in FloatField.entries) {
                val v = continuos[campo]?.get(k) ?: continue
                val curva = curvas[campo]?.get(k) ?: Ease.COSENO
                porCurva.getOrPut(curva) { mutableListOf() } += "\"${campo.key}\": ${num(v)}"
            }
            val discretosAqui = mutableListOf<String>()
            for (campo in StepField.entries) {
                val m = discretos[campo] ?: continue
                if (m.containsKey(k)) discretosAqui += "\"${campo.key}\": ${valorJson(m[k])}"
            }

            if (porCurva.isEmpty()) {
                if (discretosAqui.isNotEmpty()) {
                    salida += "      { \"t\": ${num(fase(k))}, ${discretosAqui.joinToString(", ")} }"
                }
                continue
            }
            var primero = true
            for ((curva, campos) in porCurva) {
                val partes = mutableListOf("\"t\": ${num(fase(k))}")
                if (curva != Ease.COSENO) partes += "\"ease\": \"${curva.clave}\""
                partes += campos
                // Los discretos no dependen de la curva: van una sola vez.
                if (primero) {
                    partes += discretosAqui
                    primero = false
                }
                salida += "      { " + partes.joinToString(", ") + " }"
            }
        }
        return salida
    }

    /**
     * Se queda con los pocos puntos que hacen falta para describir la curva.
     * Sin esto cada acción saldría con 33 fotogramas de 21 campos y nadie
     * podría editarla a mano, que es justamente para lo que existe el catálogo.
     */
    private fun reducir(vs: FloatArray): List<Int> {
        val rango = (vs.maxOrNull() ?: 0f) - (vs.minOrNull() ?: 0f)
        val tolerancia = maxOf(0.001f, 0.004f * rango)

        val kept = sortedSetOf(0, vs.size - 1)

        // Los picos y los valles no son candidatos: son obligatorios. Cada
        // tramo va de un valor al otro sin pasarse, así que un máximo que no es
        // nodo no se puede reproducir por más puntos que se agreguen alrededor.
        // Sembrarlos primero es lo que hace que un seno completo salga exacto
        // con tres nodos.
        //
        // Buscar en cambio dónde el error es peor no alcanza, y falla de una
        // forma que engaña: en un seno el error máximo cae un paso al costado
        // del valle, así que el nodo se ancla en el lugar equivocado por un
        // margen mínimo y la reducción se pasa el resto del tiempo tapando el
        // agujero con puntos que no arreglan nada.
        for (k in 1 until vs.size - 1) {
            val entra = vs[k] - vs[k - 1]
            val sale = vs[k + 1] - vs[k]
            val pico = entra > 0f && sale <= 0f
            val valle = entra < 0f && sale >= 0f
            // Los bordes de una meseta son codos, no picos: el valor llega,
            // se queda quieto un rato y vuelve a moverse. Sin esta condición
            // el arranque de una meseta no se ancla y la curva la atraviesa
            // redondeada. Es lo que le pasaba al agachado del salto.
            val bordeDeMeseta = (entra == 0f) != (sale == 0f)
            if (pico || valle || bordeDeMeseta) kept.add(k)
        }

        // Recién ahora, donde todavía no ajuste, el punto peor predicho.
        while (kept.size < maxNodos) {
            val (indice, error) = peorPunto(vs, kept.toList())
            if (indice < 0 || error <= tolerancia) break
            kept.add(indice)
        }

        // Repaso: agregar un nodo puede volver redundante a otro anterior.
        val lista = ArrayList(kept)
        var i = 1
        while (i < lista.size - 1) {
            val prueba = ArrayList(lista).also { it.removeAt(i) }
            if (errorMaximo(vs, prueba) <= tolerancia) lista.removeAt(i) else i++
        }
        return lista
    }

    /** Índice peor predicho por la reconstrucción actual, y su error. */
    private fun peorPunto(vs: FloatArray, kept: List<Int>): Pair<Int, Float> {
        var indice = -1
        var peor = 0f
        recorrer(vs, kept) { k, error ->
            if (error > peor) {
                peor = error
                indice = k
            }
        }
        return indice to peor
    }

    private fun errorMaximo(vs: FloatArray, kept: List<Int>): Float {
        var peor = 0f
        recorrer(vs, kept) { _, error -> if (error > peor) peor = error }
        return peor
    }

    /**
     * Error de reconstrucción en cada muestra que no sea nodo, tramo por tramo
     * y con la curva que mejor le venga a cada uno. Es importante que la
     * reducción mida con la misma curva que después se va a escribir: si mide
     * con una y escribe otra, el catálogo sale con menos nodos de los que
     * necesita y nadie se entera hasta verlo moverse mal.
     */
    private fun recorrer(vs: FloatArray, kept: List<Int>, cuerpo: (Int, Float) -> Unit) {
        for (j in 0 until kept.size - 1) {
            val a = kept[j]
            val b = kept[j + 1]
            val curva = mejorCurva(vs, a, b)
            for (k in a + 1 until b) {
                val u = (k - a).toFloat() / (b - a).toFloat()
                cuerpo(k, abs(vs[a] + (vs[b] - vs[a]) * evaluar(curva, u) - vs[k]))
            }
        }
    }

    /** La de menor error entre las cuatro. Sin muestras intermedias, la de siempre. */
    private fun mejorCurva(vs: FloatArray, a: Int, b: Int): Ease {
        var mejor = Ease.COSENO
        var menor = Float.MAX_VALUE
        for (curva in Ease.entries) {
            var peor = 0f
            for (k in a + 1 until b) {
                val u = (k - a).toFloat() / (b - a).toFloat()
                peor = maxOf(peor, abs(vs[a] + (vs[b] - vs[a]) * evaluar(curva, u) - vs[k]))
            }
            if (peor < menor) {
                menor = peor
                mejor = curva
            }
        }
        return mejor
    }

    private fun evaluar(curva: Ease, u: Float): Float {
        val mediaVuelta = PI.toFloat()
        return when (curva) {
            Ease.LINEAL -> u
            Ease.COSENO -> 0.5f - 0.5f * cos(u * mediaVuelta)
            Ease.SALIDA -> sin(u * mediaVuelta / 2f)
            Ease.ENTRADA -> 1f - cos(u * mediaVuelta / 2f)
        }
    }

    /* ---------------- salida ---------------- */

    private fun valorJson(v: Any?): String = when (v) {
        null -> "null"
        is Boolean -> v.toString()
        is Enum<*> -> "\"${v.name}\""
        else -> "\"$v\""
    }

    private fun num(v: Float): String {
        // Cinco decimales: con la grilla fina, un nodo puede caer en 0.89990 y
        // redondear a 0.8999 movería el codo lo suficiente como para notarse.
        val s = String.format(Locale.US, "%.5f", v)
        val limpio = s.trimEnd('0').trimEnd('.').ifEmpty { "0" }
        return if (limpio == "-0") "0" else limpio
    }

    /* ---------------- informe ---------------- */

    private fun resumen() {
        val procedurales = AvatarAction.entries.count { ProceduralPoses.byAction.containsKey(it) }
        println("Acciones: ${AvatarAction.entries.size}")
        println("  declarativas: ${AvatarAction.entries.size - procedurales}")
        println("  procedurales: $procedurales -> ${ProceduralPoses.names().sorted().joinToString(", ")}")
    }

    /**
     * Vuelve a leer lo que se acaba de escribir. No compara poses todavía —eso
     * es la Fase 4— pero sí prueba que el parser entiende al generador.
     */
    private fun idaYVuelta(json: String) {
        val leido = AnimationParser.parse(json)
        println("Releído: ${leido.records.size} registros.")
        if (leido.problemas.isEmpty()) {
            println("Sin problemas de lectura.")
        } else {
            println("PROBLEMAS:")
            leido.problemas.forEach { println("  - $it") }
        }
        val catalogo = AnimationCatalog(leido.records)
        val fotogramas = leido.records.sumOf { it.fotogramas.size }
        println("Fotogramas totales: $fotogramas (promedio ${fotogramas / maxOf(1, leido.records.size)} por acción).")
        println("Catálogo compilado: ${catalogo.size} animaciones.")

        // Las más pesadas son las que un especialista no va a poder editar a
        // mano. Si alguna queda muy arriba, vale la pena mirarla de cerca.
        println("Más fotogramas:")
        leido.records.sortedByDescending { it.fotogramas.size }.take(8)
            .forEach { println("  ${it.fotogramas.size.toString().padStart(3)}  ${it.id}") }
    }
}
