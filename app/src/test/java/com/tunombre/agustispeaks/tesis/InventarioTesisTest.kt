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
 * La hoja de cifras de la tesis, y los criterios 1 y 2 de la bitácora:
 *
 * - **Criterio 1**, correspondencia motor ↔ catálogo: un registro por acción,
 *   sin sobrantes ni faltantes.
 * - **Criterio 2**, vocabulario alcanzable: ninguna categoría ni ningún
 *   pictograma de la semilla sin un lugar desde donde tocarlo.
 *
 * El resto no prueba comportamiento: **cuenta**. Cada número que el informe
 * afirma sobre el tamaño del sistema sale de acá y no de un conteo a mano, que
 * es lo que se desactualiza en silencio. La salida está pensada para copiarse
 * tal cual a Word: texto plano, columnas alineadas y orden determinista.
 *
 * **No cuenta los pictogramas propios.** Se crean en el teléfono, en tiempo de
 * ejecución, y se guardan en el DataStore del dispositivo: no existen en el
 * código. Su verificación es manual (criterios 13 y 15), y la lógica de su
 * formato la cubre `PictogramasPropiosMetadatosTest`.
 *
 * Requiere que `src/main/assets/animaciones.json` exista. Si no está, corré
 * primero `PoseCatalogDumpTest`. Que esta prueba falle por catálogo ausente es
 * correcto: sin catálogo no hay nada que inventariar.
 *
 * `gradlew :app:testDebugUnitTest --tests "*InventarioTesisTest*"`
 */
class InventarioTesisTest {

    private val ap = Apendice("criterio-01-02-inventario")

    /** Raíz del código de la app, para contar archivos y líneas por paquete. */
    private val fuentes = File("src/main/java/com/tunombre/agustispeaks")

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

        ap.encabezado("INVENTARIO — CRITERIOS 1 Y 2, Y CIFRAS DEL SISTEMA", "InventarioTesisTest")
        linea("Catálogo: src/main/assets/animaciones.json")
        linea("")

        catalogo(registros)
        procedurales(registros)
        objetos()
        vocabulario()
        reglas(registros)
        codigo()
        tablaDeAcciones(registros)

        linea("")
        linea("Fin del inventario.")
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
        val faltantes = esperados - ids
        val sobrantes = ids - esperados
        linea("")
        dato("Acciones sin registro en el catálogo", faltantes.size)
        dato("Registros sin acción en el motor", sobrantes.size)
        dato("Correspondencia", porciento(esperados.size - faltantes.size, esperados.size))
        ap.veredicto(
            1, faltantes.isEmpty() && sobrantes.isEmpty(),
            "${esperados.size - faltantes.size} de ${esperados.size} acciones con registro, " +
                "${sobrantes.size} registros sobrantes"
        )
        assertEquals("Acciones sin registro en el catálogo", emptySet<String>(), faltantes)
        assertEquals("Registros que no corresponden a ninguna acción", emptySet<String>(), sobrantes)
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
        linea("")
        linea("Los pictogramas propios no están en esta cuenta: se crean en el")
        linea("teléfono, en tiempo de ejecución, y no existen en el código. Se ven")
        linea("en todos los lugares que muestran su categoría.")

        // Una categoría que ningún lugar expone es vocabulario muerto: sus
        // pictogramas existen en la semilla y no hay forma de tocarlos en el
        // tablero. Le pasó esto a "lugares" y estuvo así hasta que este mismo
        // conteo lo sacó a la luz. La afirmación vale la pena sostenerla:
        // declarar una palabra y poder decirla no son lo mismo.
        val huerfanas = SeedData.categories
            .filter { c -> SeedData.places.none { c.id in it.categoryIds } }
            .map { it.id }

        val vacias = SeedData.categories
            .filter { c -> tematicos.none { it.categoryId == c.id } }
            .map { it.id }

        // El umbral habla de categorías **y pictogramas**. Las palabras núcleo
        // están siempre a la vista, en el panel izquierdo; un temático es
        // alcanzable si al menos un lugar lo muestra.
        val pictogramasSinLugar = tematicos.filter { it.placeIds.isEmpty() }.map { it.id }

        linea("")
        dato("Categorías que ningún lugar muestra", huerfanas.size)
        dato("Categorías sin pictogramas", vacias.size)
        dato("Pictogramas temáticos sin ningún lugar", pictogramasSinLugar.size)
        ap.veredicto(
            2, huerfanas.isEmpty() && vacias.isEmpty() && pictogramasSinLugar.isEmpty(),
            "${huerfanas.size} categorías y ${pictogramasSinLugar.size} pictogramas sin acceso; " +
                "${vacias.size} categorías vacías"
        )

        assertEquals(
            "Categorías que ningún lugar expone: sus pictogramas son inalcanzables",
            emptyList<String>(), huerfanas
        )
        assertEquals("Categorías sin ningún pictograma", emptyList<String>(), vacias)
        assertEquals(
            "Pictogramas temáticos que ningún lugar muestra",
            emptyList<String>(), pictogramasSinLugar
        )
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
    /*  6. Código                                                          */
    /* ------------------------------------------------------------------ */

    /**
     * Archivos y líneas de Kotlin por paquete, para verificar las cifras que el
     * relevamiento del 5-10 contó leyendo el código. Se cuentan **todas** las
     * líneas, también las vacías, que es lo que muestra el editor.
     */
    private fun codigo() {
        assertTrue("No se encontró ${fuentes.absolutePath}.", fuentes.isDirectory)
        val archivos = fuentes.walkTopDown().filter { it.isFile && it.extension == "kt" }.toList()
        val porPaquete = archivos.groupBy { f ->
            f.parentFile.relativeTo(fuentes).path.replace(File.separatorChar, '.').ifEmpty { "(raíz)" }
        }.toSortedMap()

        titulo("6. CÓDIGO KOTLIN DE LA APP (src/main)")
        linea("PAQUETE".padEnd(24) + "ARCHIVOS".padStart(10) + "LÍNEAS".padStart(10))
        linea("-".repeat(44))
        var totalLineas = 0
        for ((paquete, lista) in porPaquete) {
            val lineas = lista.sumOf { it.readLines().size }
            totalLineas += lineas
            linea(paquete.padEnd(24) + lista.size.toString().padStart(10) + lineas.toString().padStart(10))
        }
        linea("-".repeat(44))
        linea("TOTAL".padEnd(24) + archivos.size.toString().padStart(10) + totalLineas.toString().padStart(10))
        linea("")
        linea("Se cuentan todas las líneas, incluidas las vacías.")
    }

    /* ------------------------------------------------------------------ */
    /*  7. Tabla completa                                                  */
    /* ------------------------------------------------------------------ */

    private fun tablaDeAcciones(registros: List<AnimationRecord>) {
        val porId = registros.associateBy { it.id }

        titulo("7. CATÁLOGO COMPLETO DE ACCIONES")
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
