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
        "dolor" to "temblor rapido: 8 ciclos por bucle (senal `fast`)",
        "descompuesto" to "temblor rapido: 8 ciclos por bucle (senal `fast`)",
        "frio" to "tiriton: 16 ciclos por bucle (senal `shiver`)",
        "nervioso" to "tiriton: 16 ciclos por bucle (senal `shiver`)",
        "miedo" to "tiriton: 16 ciclos por bucle (senal `shiver`)",
        "comer" to "masticacion: 7 ciclos rectificados (senal `chew`)",
        "dibujar" to "trazo: 9 ciclos por bucle (sin 18πt)",
        "escuchar" to "diente de sierra: la fase salta en el borde del bucle",
        "despues" to "diente de sierra: la fase salta en el borde del bucle",
        "antes" to "diente de sierra: la fase salta en el borde del bucle",
        "remedio" to "mandibula que salta entre dos valores fijos, no interpolable"
    )

    @Test
    fun inventario() {
        assertTrue(
            "No existe ${asset.absolutePath}. Corre PoseCatalogDumpTest primero.",
            asset.exists()
        )
        assertTrue(
            "No se encontro ${fuenteResolvedor.absolutePath}.",
            fuenteResolvedor.exists()
        )

        val leido = AnimationParser.parse(asset.readText())
        assertTrue(
            "El catalogo tiene problemas de lectura: ${leido.problemas}",
            leido.problemas.isEmpty()
        )
        val registros = leido.records

        linea("APENDICE E - INVENTARIO DEL MOTOR Y CATALOGO DE ACCIONES")
        linea("AgustiSpeaks - generado por InventarioTesisTest")
        linea("Catalogo: src/main/assets/animaciones.json")
        linea("")

        catalogo(registros)
        procedurales(registros)
        objetos()
        vocabulario()
        reglas(registros)
        tablaDeAcciones(registros)

        linea("")
        linea("Fin del apendice E.")
    }

    /* ------------------------------------------------------------------ */
    /*  1. Catalogo                                                        */
    /* ------------------------------------------------------------------ */

    private fun catalogo(registros: List<AnimationRecord>) {
        val declarativas = registros.filter { it.procedural == null }
        val fotogramasPorAccion = declarativas.map { it.fotogramas.size }
        val total = registros.sumOf { it.fotogramas.size }

        titulo("1. CATALOGO DE ANIMACIONES")
        dato("Acciones en el motor (enum AvatarAction)", AvatarAction.entries.size)
        dato("Registros en el catalogo", registros.size)
        dato("  declarativas (fotogramas)", declarativas.size)
        dato("  procedurales (generador Kotlin)", registros.size - declarativas.size)
        linea("")
        dato("Fotogramas declarados, total", total)
        dato("  minimo por accion declarativa", fotogramasPorAccion.minOrNull() ?: 0)
        dato("  promedio por accion declarativa", promedio(fotogramasPorAccion))
        dato("  maximo por accion declarativa", fotogramasPorAccion.maxOrNull() ?: 0)
        linea("")
        dato("Tamano del catalogo, en bytes", miles(asset.length()))
        dato("Bytes por fotograma", division(asset.length(), total))

        // Un registro por accion, sin sobrantes ni faltantes. Si esto se rompe,
        // el catalogo y el motor dejaron de estar en correspondencia y ninguna
        // otra cifra de este informe significa lo que dice.
        val ids = registros.map { it.id }.toSet()
        val esperados = AvatarAction.entries.map { it.name.lowercase() }.toSet()
        assertEquals("Acciones sin registro en el catalogo", emptySet<String>(), esperados - ids)
        assertEquals("Registros que no corresponden a ninguna accion", emptySet<String>(), ids - esperados)
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
            "ACCION".padEnd(14) + "GENERADOR".padEnd(14) +
                "MOTIVO POR EL QUE NO SE DECLARA"
        )
        linea("-".repeat(78))
        for (r in conGenerador.sortedBy { it.id }) {
            val nombre = r.procedural.orEmpty()
            val motivo = motivos[nombre]
            assertTrue(
                "La accion procedural '$nombre' no tiene motivo documentado en " +
                    "InventarioTesisTest.motivos",
                motivo != null
            )
            linea(r.id.padEnd(14) + nombre.padEnd(14) + motivo.orEmpty())
        }
        linea("-".repeat(78))
        dato("Total de acciones procedurales", conGenerador.size)

        // El catalogo y la tabla de generadores tienen que decir lo mismo.
        assertEquals(
            "El catalogo y ProceduralPoses no coinciden",
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
        dato("Categorias tematicas", SeedData.categories.size)
        dato("Palabras nucleo (visibles en todo lugar)", nucleo.size)
        dato("Pictogramas tematicos", tematicos.size)
        dato("TOTAL de pictogramas distintos", nucleo.size + tematicos.size)
        linea("")
        linea("CATEGORIA".padEnd(22) + "PICTOGRAMAS".padStart(12) + "LUGARES".padStart(10))
        linea("-".repeat(44))
        for (c in SeedData.categories) {
            val cuantos = tematicos.count { it.categoryId == c.id }
            val lugares = SeedData.places.count { c.id in it.categoryIds }
            linea(c.name.padEnd(22) + cuantos.toString().padStart(12) + lugares.toString().padStart(10))
        }
        linea("-".repeat(44))
        linea("nucleo".padEnd(22) + nucleo.size.toString().padStart(12) + "todos".padStart(10))
        linea("-".repeat(44))
        linea("TOTAL".padEnd(22) + (nucleo.size + tematicos.size).toString().padStart(12))
        linea("")
        linea("Nota: un pictograma tematico aparece en todo lugar cuya lista de")
        linea("categorias incluya la suya, asi que el mismo pictograma se muestra en")
        linea("varios lugares. El TOTAL cuenta pictogramas distintos, no apariciones.")
    }

    /* ------------------------------------------------------------------ */
    /*  5. Reglas de disparo                                               */
    /* ------------------------------------------------------------------ */

    private fun reglas(registros: List<AnimationRecord>) {
        val enCatalogo = registros.count {
            it.palabras.isNotEmpty() || it.raices.isNotEmpty()
        }

        // Se cuentan leyendo el codigo fuente como texto. Es la unica forma de
        // que la cifra siga siendo cierta cuando alguien agregue una regla: un
        // conteo a mano envejece sin avisar.
        val texto = fuenteResolvedor.readText()
        val cuerpo = texto.substringBefore("private fun R(")
        val sitios = Regex("return R\\(").findAll(cuerpo).count()
        val respaldo = Regex("return R\\(AvatarAction\\.TALK\\)").findAll(cuerpo).count()
        val enCodigo = sitios - respaldo

        titulo("5. REGLAS DE DISPARO")
        linea("Una regla es una rama que mapea una frase a una accion. Se cuentan")
        linea("los `return R(...)` de ActionResolver.resolve(), sin el respaldo final.")
        linea("")
        dato("Reglas en el catalogo (animaciones.json)", enCatalogo)
        dato("Reglas en codigo (ActionResolver)", enCodigo)
        dato("  de ellas, duplicadas en el catalogo", enCatalogo)
        dato("Reglas distintas en total", enCodigo)
        dato("Migradas al catalogo, en por ciento", porciento(enCatalogo, enCodigo))
        linea("")
        linea("Tablas auxiliares: no eligen la accion sino el matiz.")
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

        titulo("6. CATALOGO COMPLETO DE ACCIONES")
        linea("En el orden en que estan declaradas en el enum AvatarAction.")
        linea("")
        linea(
            "#".padStart(3) + "  " + "ACCION".padEnd(14) + "BUCLE(ms)".padStart(10) +
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
        linea("Bucle: duracion de una vuelta completa de la animacion.")
        linea("Las procedurales llevan 0 fotogramas por definicion: su movimiento")
        linea("es una funcion, no una lista de instantes.")
    }

    /* ------------------------------------------------------------------ */
    /*  Formato                                                            */
    /* ------------------------------------------------------------------ */

    private fun linea(s: String) = println(s)

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
