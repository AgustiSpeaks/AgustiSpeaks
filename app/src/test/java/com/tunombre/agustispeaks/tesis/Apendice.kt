package com.tunombre.agustispeaks.tesis

import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * La salida de una prueba de tesis: se imprime en la consola **y** se guarda en
 * `docs/tesis/salidas-2026-10/`.
 *
 * El archivo no es una comodidad. La consola de Gradle recorta la salida larga,
 * y la tabla de las 97 comparaciones de paridad entra justo en el tamaño donde
 * lo que se pierde es el final, que es donde están los desvíos. Una salida que
 * solo existe en la consola es una salida que a veces no se puede leer.
 *
 * ### Por qué en `docs/` y no en `build/`
 *
 * Porque `build/` está ignorado por git, y lo que la tesis necesita es poder
 * citar un resultado contra el commit que lo produjo. Guardado acá, cada corrida
 * deja su salida al lado del código que la generó, y la fila de la bitácora
 * apunta a algo que existe. El costo es que correr las pruebas ensucia el árbol
 * de trabajo; para este proyecto eso es lo que se busca.
 *
 * Se guarda **siempre**, incluso cuando la prueba falla. Una corrida que falla
 * es justo la que hay que poder leer entera.
 *
 * La ruta es relativa a `app/`, que es el directorio de trabajo de las pruebas
 * de la JVM. Es la misma convención que ya usa `PoseCatalogDumpTest`.
 *
 * @param nombre nombre del archivo, sin extensión. Empieza con el número de
 *        criterio de la bitácora (`criterio-03-equivalencia`), para que cada
 *        salida se corresponda con su fila.
 */
class Apendice(private val nombre: String) {

    private val buffer = StringBuilder()

    fun linea(s: String = "") {
        println(s)
        buffer.appendLine(s)
    }

    /**
     * Las tres primeras líneas de toda salida: qué mide, qué prueba la generó y
     * cuándo. La fecha de corrida no reemplaza al commit —ese va en la
     * bitácora—, pero permite distinguir dos corridas del mismo commit.
     */
    fun encabezado(titulo: String, prueba: String) {
        linea(titulo)
        linea("AgustiSpeaks · generado por $prueba")
        linea("Corrida: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
        linea("")
    }

    /**
     * La línea que se copia a la columna "Resultado" de la bitácora. Se imprime
     * **antes** de la aserción, para que quede escrita también cuando la prueba
     * falla.
     *
     * @param cumple `true` o `false` contra el umbral; `null` para los criterios
     *        sin umbral, que se informan como medidos.
     */
    fun veredicto(criterio: Int, cumple: Boolean?, detalle: String) {
        val estado = when (cumple) {
            true -> "CUMPLE"
            false -> "NO CUMPLE"
            null -> "MEDIDO (sin umbral)"
        }
        linea("")
        linea(">>> CRITERIO $criterio: $estado — $detalle")
    }

    /** Llamalo antes de la aserción, o un fallo se lleva el archivo con él. */
    fun guardar() {
        val archivo = File("../docs/tesis/salidas-2026-10/$nombre.txt")
        archivo.parentFile?.mkdirs()
        archivo.writeText(buffer.toString())
        println("── Salida guardada en ${archivo.absolutePath}")
    }
}
