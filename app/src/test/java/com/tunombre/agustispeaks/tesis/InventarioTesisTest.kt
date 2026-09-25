package com.tunombre.agustispeaks.tesis

import com.tunombre.agustispeaks.avatar.AvatarAction
import com.tunombre.agustispeaks.avatar.Prop
import com.tunombre.agustispeaks.avatar.catalog.AnimationParser
import com.tunombre.agustispeaks.avatar.catalog.AnimationRecord
import com.tunombre.agustispeaks.avatar.catalog.ProceduralPoses
import com.tunombre.agustispeaks.data.seed.SeedData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.util.Locale

/**
 * La hoja de cifras de la tesis. No prueba comportamiento: **cuenta**.
 *
 * Cada número que el informe afirma sobre el tamaño del sistema sale de acá y no
 * de un conteo a mano, que es lo que se desactualiza en silencio. La salida está
 * pensada para copiarse tal cual a Word: texto plano, columnas alineadas y orden
 * determinista.
 *
 * Requiere que `src/main/assets/animaciones.json` exista. Si no está, corré
 * primero `PoseCatalogDumpTest`. Que esta prueba falle por catálogo ausente es
 * correcto: sin catálogo no hay nada que inventariar.
 *
 * `gradlew :app:testDebugUnitTest --tests "*InventarioTesisTest*"`
 */
class InventarioTesisTest {

    private val ap = Apendice("E-inventario")

    private val asset = File("src/main/assets/animaciones.json")
    private val fuenteResolvedor =
        File("src/main/java/com/tunombre/agustispeaks/avatar/ActionResolver.kt")

    /**
     * Por qué cada acción procedural no se puede declarar como fotogramas.
     *
     * El texto está transcrito de la documentación de [ProceduralPoses]; no hay
     * forma de leerlo de la máquina porque es prosa. Lo que sí se verifica es la
     * **cobertura**: si alguien agrega una acción procedural y no escribe acá su
     * motivo, la prueba falla. Una tabla de documentación que el test mantiene
     * honesta vale bastante más que una que nadie revisa.
     */
    private val motivos: Map<String, String> = mapOf(
        "dolor" to "temblor rápido: 8 ciclos por bucle (señal `fast`)",
        "descompuesto" to "temblor rápido: 8 ciclos por bucle (señal `fast`)",
        "frio" to "tiritón: 16 ciclos por bucle (señal `shiver`)",
        "nervioso" to "tiritón: 16 ciclos por bucle (señal `shiver`)",
        "miedo" to "tiritón: 16 ciclos por bucle (señal `shiver`)",
        "comer" to "masticación: 7 ciclos rectificados (señal `chew`)",
        "dibujar" to "trazo: 9 ciclos por bucle (sin 18πt)",
        "escuchar" to "diente de sierra: la fase salta en el borde del bucle",
        "despues" to "diente de sierra: la fase salta en el borde del bucle",
        "antes" to "diente de sierra: la fase salta en el borde del bucle",
        "remedio" to "mandíbula que salta entre dos valores fijos, no interpolable",
        "vestirse" to "la pose se bifurca según la prenda: erguido o agachado"
    )

    // El `finally` no es adorno: varias aserciones viven dentro de las
    // secciones, así que sin él un fallo a mitad de camino se llevaría el
    // archivo del apéndice, que es justo lo que hay que poder leer.
    @Test
    fun inventario() = try {
        medir()
    } finally {
        ap.guardar()
    }

    private fun medir() {
        assertTrue(
            "No existe ${asset.absolutePath}. Corré PoseCatalogDumpTest primero.",
            asset.exists()
        )
        assertTrue(
            "No se encontró ${fuenteResolvedor.absolutePath}.",
            fuenteResolvedor.exists()
        )

        val leido = AnimationParser.parse(asset.readText())
        assertTrue(
            "El catálogo tiene problemas de lectura: ${leido.problemas}",
            leido.problemas.isEmpty()
        )
        val registros = leido.records

        linea("APÉNDICE E — INVENTARIO DEL MOTOR Y CATÁLOGO DE ACCIONES")
        linea("AgustiSpeaks · generado por InventarioTesisTest")
        linea("Catálogo: src/main/assets/animaciones.json")
        linea("")

        catalogo(registros)
        procedurales(registros)
        objetos()
        vocabulario()
        reglas(registros)
        tablaDeAcciones(registros)

        linea("")
        linea("Fin del apéndice E.")
    }

    /* ------------------------------------------------------------------ */
    /*  1. Catálogo                                                        */
    /* ------------------------------------------------------------------ */

    private fun catalogo(registros: List<AnimationRecord>) {
        val declarativas = registros.filter { it.procedural == null }
        val fotogramasPorAccion = declarativas.map { it.fotogramas.size }
        val total = registros.sumOf { it.fotogramas.size }

        titulo("1. CATÁLOGO DE ANIMACIONES")
        dato("Acciones en el motor (enum AvatarAction)", AvatarAction.entries.size)
        dato("Registros en el catálogo", registros.size)
        dato("  declarativas (fotogramas)", declarativas.size)
        dato("  procedurales (generador Kotlin)", registros.size - declarativas.size)
        linea("")
        dato("Fotogramas declarados, total", total)
        dato("  mínimo por acción declarativa", fotogramasPorAccion.minOrNull() ?: 0)
        dato("  promedio por acción declarativa", promedio(fotogramasPorAccion))
        dato("  máximo por acción declarativa", fotogramasPorAccion.maxOrNull() ?: 0)
        linea("")
        dato("Tamaño del catálogo, en bytes", miles(asset.length()))
        dato("Bytes por fotograma", division(asset.length(), total))

        // Un registro por acción, sin sobrantes ni faltantes. Si esto se rompe,
        // el catálogo y el motor dejaron de estar en correspondencia y ninguna
        // otra cifra de este informe significa lo que dice.
        val ids = registros.map { it.id }.toSet()
        val esperados = AvatarAction.entries.map { it.name.lowercase() }.toSet()
        assertEquals("Acciones sin registro en el catálogo", emptySet<String>(), esperados - ids)
        assertEquals("Registros que no corresponden a ninguna acción", emptySet<String>(), ids - esperados)
    }

    /* ------------------------------------------------------------------ */
    /*  2. Procedurales                                                    */
    /* ------------------------------------------------------------------ */

    private fun procedurales(registros: List<AnimationRecord>) {
        val conGenerador = registros.filter { it.procedural != null }

        titulo("2. ACCIONES PROCEDURALES Y SU MOTIVO")
        linea("Son las que no se pueden describir con una cantidad razonable de")
        linea("fotogramas. El registro solo lleva el nombre del generador; el")
        linea("movimiento vive en Kotlin.")
        linea("")
        linea(
            "ACCIÓN".padEnd(14) + "GENERADOR".padEnd(14) +
                "MOTIVO POR EL QUE NO SE DECLARA"
        )
        linea("-".repeat(78))
        for (r in conGenerador.sortedBy { it.id }) {
            val nombre = r.procedural.orEmpty()
            val motivo = motivos[nombre]
            assertTrue(
                "La acción procedural '$nombre' no tiene motivo documentado en " +
                    "InventarioTesisTest.motivos",
                motivo != null
            )
            linea(r.id.padEnd(14) + nombre.padEnd(14) + motivo.orEmpty())
        }
        linea("-".repeat(78))
        dato("Total de acciones procedurales", conGenerador.size)

        // El catálogo y la tabla de generadores tienen que decir lo mismo.
        assertEquals(
            "El catálogo y ProceduralPoses no coinciden",
            ProceduralPoses.names(),
            conGenerador.mapNotNull { it.procedural }.toSet()
        )
    }

    /* ------------------------------------------------------------------ */
    /*  3. Objetos                                                         */
    /* ------------------------------------------------------------------ */

    private fun objetos() {
        titulo("3. OBJETOS (PROPS)")
        dato("Objetos distintos", Prop.entries.size)
        linea("")
        val nombres = Prop.entries.map { it.name }.sorted()
        for (fila in nombres.chunked(4)) {
            linea("  " + fila.joinToString("") { it.padEnd(18) }.trimEnd())
        }
    }

    /* ------------------------------------------------------------------ */
    /*  4. Vocabulario                                                     */
    /* ------------------------------------------------------------------ */

    private fun vocabulario() {
        val nucleo = SeedData.coreWords
        val tematicos = SeedData.pictograms

        titulo("4. VOCABULARIO")
        dato("Lugares", SeedData.places.size)
        dato("Categorías temáticas", SeedData.categories.size)
        dato("Palabras núcleo (visibles en todo lugar)", nucleo.size)
        dato("Pictogramas temáticos", tematicos.size)
        dato("TOTAL de pictogramas distintos", nucleo.size + tematicos.size)
        linea("")
        linea("CATEGORÍA".padEnd(22) + "PICTOGRAMAS".padStart(12) + "LUGARES".padStart(10))
        linea("-".repeat(44))
        for (c in SeedData.categories) {
            val cuantos = tematicos.count { it.categoryId == c.id }
            val lugares = SeedData.places.count { c.id in it.categoryIds }
            linea(c.name.padEnd(22) + cuantos.toString().padStart(12) + lugares.toString().padStart(10))
        }
        linea("-".repeat(44))
        linea("núcleo".padEnd(22) + nucleo.size.toString().padStart(12) + "todos".padStart(10))
        linea("-".repeat(44))
        linea("TOTAL".padEnd(22) + (nucleo.size + tematicos.size).toString().padStart(12))
        linea("")
        linea("Nota: un pictograma temático aparece en todo lugar cuya lista de")
        linea("categorías incluya la suya, así que el mismo pictograma se muestra en")
        linea("varios lugares. El TOTAL cuenta pictogramas distintos, no apariciones.")

        // Una categoría que ningún lugar expone es vocabulario muerto: sus
        // pictogramas existen en la semilla y no hay forma de tocarlos en el
        // tablero. Le pasó esto a "lugares" y estuvo así hasta que este mismo
        // conteo lo sacó a la luz. La afirmación vale la pena sostenerla:
        // declarar una palabra y poder decirla no son lo mismo.
        val huerfanas = SeedData.categories
            .filter { c -> SeedData.places.none { c.id in it.categoryIds } }
            .map { it.id }
        assertEquals(
            "Categorías que ningún lugar expone: sus pictogramas son inalcanzables",
            emptyList<String>(), huerfanas
        )

        val vacias = SeedData.categories
            .filter { c -> tematicos.none { it.categoryId == c.id } }
            .map { it.id }
        assertEquals("Categorías sin ningún pictograma", emptyList<String>(), vacias)
    }

    /* ------------------------------------------------------------------ */
    /*  5. Reglas de disparo                                               */
    /* ------------------------------------------------------------------ */

    private fun reglas(registros: List<AnimationRecord>) {
        val enCatalogo = registros.count {
            it.palabras.isNotEmpty() || it.raices.isNotEmpty()
        }

        // Se cuentan leyendo el código fuente como texto. Es la única forma de
        // que la cifra siga siendo cierta cuando alguien agregue una regla: un
        // conteo a mano envejece sin avisar.
        val texto = fuenteResolvedor.readText()
        val cuerpo = texto.substringBefore("private fun R(")
        val sitios = Regex("return R\\(").findAll(cuerpo).count()
        val respaldo = Regex("return R\\(AvatarAction\\.TALK\\)").findAll(cuerpo).count()
        val enCodigo = sitios - respaldo

        titulo("5. REGLAS DE DISPARO")
        linea("Una regla es una rama que mapea una frase a una acción. Se cuentan")
        linea("los `return R(...)` de ActionResolver.resolve(), sin el respaldo final.")
        linea("")
        dato("Reglas en el catálogo (animaciones.json)", enCatalogo)
        dato("Reglas en código (ActionResolver)", enCodigo)
        dato("  de ellas, duplicadas en el catálogo", enCatalogo)
        dato("Reglas distintas en total", enCodigo)
        dato("Migradas al catálogo, en por ciento", porciento(enCatalogo, enCodigo))
        linea("")
        linea("Tablas auxiliares: no eligen la acción sino el matiz.")
        dato("  alimento nombrado (foodProp)", ramas(texto, "private fun foodProp", "private fun garment", "->\\s*Prop\\."))
        dato("  prenda nombrada (garment)", ramas(texto, "private fun garment", "private fun bodyPart", "->\\s*Prop\\."))
        dato("  parte del cuerpo (bodyPart)", ramas(texto, "private fun bodyPart", "private fun normalize", "->\\s*BodyPart\\."))
    }

    private fun ramas(texto: String, desde: String, hasta: String, patron: String): Int {
        val trozo = texto.substringAfter(desde).substringBefore(hasta)
        return Regex(patron).findAll(trozo).count()
    }

    /* ------------------------------------------------------------------ */
    /*  6. Tabla completa                                                  */
    /* ------------------------------------------------------------------ */

    private fun tablaDeAcciones(registros: List<AnimationRecord>) {
        val porId = registros.associateBy { it.id }

        titulo("6. CATÁLOGO COMPLETO DE ACCIONES")
        linea("En el orden en que están declaradas en el enum AvatarAction.")
        linea("")
        linea(
            "#".padStart(3) + "  " + "ACCIÓN".padEnd(14) + "BUCLE(ms)".padStart(10) +
                "  " + "TIPO".padEnd(13) + "FOTOGRAMAS".padStart(11)
        )
        linea("-".repeat(56))
        AvatarAction.entries.forEachIndexed { i, accion ->
            val r = porId.getValue(accion.name.lowercase())
            val tipo = if (r.procedural == null) "declarativa" else "procedural"
            linea(
                (i + 1).toString().padStart(3) + "  " +
                    accion.name.padEnd(14) +
                    accion.loopMillis.toString().padStart(10) + "  " +
                    tipo.padEnd(13) +
                    r.fotogramas.size.toString().padStart(11)
            )
        }
        linea("-".repeat(56))
        linea(
            "".padStart(3) + "  " + "TOTAL".padEnd(14) +
                "".padStart(10) + "  " + "".padEnd(13) +
                registros.sumOf { it.fotogramas.size }.toString().padStart(11)
        )
        linea("")
        linea("Bucle: duración de una vuelta completa de la animación.")
        linea("Las procedurales llevan 0 fotogramas por definición: su movimiento")
        linea("es una función, no una lista de instantes.")
    }

    /* ------------------------------------------------------------------ */
    /*  Formato                                                            */
    /* ------------------------------------------------------------------ */

    private fun linea(s: String) = ap.linea(s)

    private fun titulo(s: String) {
        linea("")
        linea("=".repeat(78))
        linea(s)
        linea("=".repeat(78))
    }

    private fun dato(etiqueta: String, valor: Any) =
        linea("  " + etiqueta.padEnd(46, '.') + " " + valor.toString().padStart(8))

    private fun promedio(xs: List<Int>): String {
        if (xs.isEmpty()) return "0"
        return String.format(Locale.ROOT, "%.2f", xs.sum().toDouble() / xs.size)
    }

    private fun division(a: Long, b: Int): String {
        if (b == 0) return "0"
        return String.format(Locale.ROOT, "%.1f", a.toDouble() / b)
    }

    private fun porciento(parte: Int, total: Int): String {
        if (total == 0) return "0,0 %"
        return String.format(Locale.ROOT, "%.1f %%", 100.0 * parte / total)
    }

    private fun miles(n: Long): String =
        n.toString().reversed().chunked(3).joinToString(".").reversed()
}
