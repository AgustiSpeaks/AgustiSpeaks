package com.tunombre.agustispeaks.tesis

import java.io.File

/**
 * Un apéndice de la tesis: se imprime en la consola **y** se guarda en
 * `docs/tesis/salidas/`.
 *
 * El archivo no es una comodidad. La consola de Gradle recorta la salida larga,
 * y la tabla de las 97 comparaciones de paridad entra justo en el tamaño donde
 * lo que se pierde es el final, que es donde están los desvíos. Un apéndice que
 * solo existe en la consola es un apéndice que a veces no se puede leer.
 *
 * ### Por qué en `docs/` y no en `build/`
 *
 * Porque `build/` está ignorado por git, y lo que la tesis necesita es poder
 * citar un resultado contra el commit que lo produjo. Guardado acá, cada corrida
 * deja los apéndices al lado del código que los generó, y la fila de la bitácora
 * de verificación apunta a algo que existe. El costo es que correr las pruebas
 * ensucia el árbol de trabajo; para este proyecto eso es lo que se busca.
 *
 * Se guarda **siempre**, incluso cuando la prueba falla. Una corrida que falla
 * es justo la que hay que poder leer entera.
 *
 * La ruta es relativa a `app/`, que es el directorio de trabajo de las pruebas
 * de la JVM. Es la misma convención que ya usa `PoseCatalogDumpTest`.
 */
class Apendice(private val nombre: String) {

    private val buffer = StringBuilder()

    fun linea(s: String = "") {
        println(s)
        buffer.appendLine(s)
    }

    /** Llamalo antes de la aserción, o un fallo se lleva el archivo con él. */
    fun guardar() {
        val archivo = File("../docs/tesis/salidas/$nombre.txt")
        archivo.parentFile?.mkdirs()
        archivo.writeText(buffer.toString())
        println("── Apéndice guardado en ${archivo.absolutePath}")
    }
}
