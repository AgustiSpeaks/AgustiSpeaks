package com.tunombre.agustispeaks.validacion

import android.util.Log
import com.tunombre.agustispeaks.BuildConfig

/**
 * Instrumentación de medición para el capítulo de validación de la tesis.
 *
 * Es el **único** archivo de la app que existe para medir y no para funcionar.
 * Está concentrado acá a propósito: cuando la medición termine, se borra este
 * archivo y se quitan las cuatro llamadas que lo usan. No hay lógica de la app
 * mezclada con lógica de medición en ningún otro lado.
 *
 * ### Doble compuerta
 *
 * Nada se registra si no se cumplen las dos condiciones: que sea una build de
 * depuración y que [ACTIVA] esté en `true`.
 *
 * `BuildConfig.DEBUG` es la que importa, porque es automática: una build de
 * release no mide aunque alguien se olvide de bajar el interruptor. [ACTIVA] es
 * para poder apagarlo a mano sin recompilar en modo release, cuando se quiere
 * usar la app sin ensuciar el Logcat.
 *
 * ### Qué no hace
 *
 * No toca el ciclo de dibujo. Registra una línea por frase hablada y una por GIF
 * exportado; el dibujo corre veinticuatro veces por segundo y no es lugar para
 * esto.
 *
 * ### Cómo leer las mediciones
 *
 * En Logcat, filtrar por el tag `VALIDACION`. La diferencia ya viene calculada
 * en milisegundos: no hay que restar instantes a mano.
 *
 * ```
 * VALIDACION latencia_ms=184 frase="Yo quiero agua"
 * VALIDACION gif_bytes=41233 gif_fotogramas=24
 * ```
 *
 * El token `VALIDACION` se repite dentro del mensaje, además de estar en el tag.
 * Es redundante a propósito: así un `logcat` volcado a archivo se puede filtrar
 * con un buscador de texto, sin depender de que la herramienta entienda tags.
 */
object Validacion {

    /** Interruptor manual. Ponelo en `false` para dejar de registrar. */
    private const val ACTIVA = true

    private const val TAG = "VALIDACION"

    private val midiendo: Boolean
        get() = BuildConfig.DEBUG && ACTIVA

    // Escritas desde el hilo principal, leídas desde un hilo del motor de voz.
    @Volatile
    private var instanteDePulsacion: Long = 0L

    @Volatile
    private var fraseEnCurso: String? = null

    /**
     * Momento en que el chico tocó un pictograma, con la frase acumulada que eso
     * produjo.
     *
     * Se llama después de armar la frase y antes de pedir la voz. Entre la
     * pulsación y este punto hay una concatenación de cadenas, del orden de
     * microsegundos: irrelevante frente a una latencia que se mide en
     * milisegundos, y a cambio la frase que queda registrada es exactamente la
     * que se va a pronunciar.
     */
    fun pulsacion(frase: String) {
        if (!midiendo) return
        instanteDePulsacion = System.nanoTime()
        fraseEnCurso = frase
    }

    /**
     * El motor de voz empezó a sonar. Calcula y registra la latencia.
     *
     * **La marca se consume.** Sin eso, cualquier otra cosa que hable —el botón
     * "Decir", la prueba de voz de Ajustes, una frase de Recientes— registraría
     * una latencia medida contra una pulsación vieja, y las veinte mediciones
     * dejarían de ser homogéneas. Solo se mide el camino pictograma → voz, que
     * es el que la tesis afirma.
     */
    fun vozIniciada() {
        if (!midiendo) return
        val frase = fraseEnCurso ?: return
        fraseEnCurso = null
        val ms = (System.nanoTime() - instanteDePulsacion) / 1_000_000
        Log.i(TAG, "VALIDACION latencia_ms=$ms frase=\"$frase\"")
    }

    /** Tamaño y cantidad de fotogramas del GIF recién escrito. */
    fun gifExportado(bytes: Long, fotogramas: Int) {
        if (!midiendo) return
        Log.i(TAG, "VALIDACION gif_bytes=$bytes gif_fotogramas=$fotogramas")
    }
}
