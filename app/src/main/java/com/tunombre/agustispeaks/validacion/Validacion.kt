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
 * No toca el ciclo de dibujo. Registra una línea por frase hablada; el dibujo
 * corre veinticuatro veces por segundo y no es lugar para esto.
 *
 * ### Cómo leer las mediciones
 *
 * En Logcat, filtrar por el tag `VALIDACION`. La diferencia ya viene calculada
 * en milisegundos: no hay que restar instantes a mano.
 *
 * ```
 * VALIDACION latencia_ms=31 frase="Yo quiero agua"
 * ```
 *
 * Tres regímenes conviven en esas líneas y conviene separarlos al analizar: el
 * arranque en frío de la sesión, que ronda el medio segundo; la síntesis nueva,
 * alrededor de 30 ms; y una frase que el motor ya tiene cacheada, menos de 20.
 * Promediarlos juntos no describe ninguno de los tres.
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

    /**
     * Una pulsación pendiente de medir.
     *
     * Los tres datos viajan juntos en un objeto inmutable, y no como tres
     * campos sueltos. Con campos sueltos, un `onStart` que llega desde el hilo
     * del motor de voz puede leer el instante de una pulsación y la frase de
     * otra, y la resta sale contra el reloj equivocado. Eso produjo dos
     * mediciones de 5 y 10 ms en la primera tanda: valores que no alcanzan
     * para que un sintetizador arranque.
     */
    private class Pendiente(
        val nanos: Long,
        val frase: String,
        /** Id de la locución, cuando `speak` ya la creó. */
        val locucion: String?
    )

    // Escrita desde el hilo principal, leída desde un hilo del motor de voz.
    @Volatile
    private var pendiente: Pendiente? = null

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
        pendiente = Pendiente(System.nanoTime(), frase, locucion = null)
    }

    /**
     * Ata la pulsación pendiente al id que el motor de voz le asignó.
     *
     * Es lo que permite descartar el `onStart` de una locución vieja: sin el
     * id no hay forma de saber si el sonido que arranca es el de esta pulsación
     * o el de la anterior, que `QUEUE_FLUSH` acaba de cortar.
     */
    fun locucionCreada(id: String) {
        if (!midiendo) return
        val p = pendiente ?: return
        pendiente = Pendiente(p.nanos, p.frase, id)
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
    fun vozIniciada(id: String?) {
        if (!midiendo) return
        val p = pendiente ?: return
        // El sonido que arranca tiene que ser el de ESTA pulsación. Si el id no
        // coincide, es el `onStart` rezagado de una locución que ya se corto:
        // medirla daría una latencia contra el reloj equivocado.
        if (id == null || id != p.locucion) return
        pendiente = null
        val ms = (System.nanoTime() - p.nanos) / 1_000_000
        Log.i(TAG, "VALIDACION latencia_ms=$ms frase=\"${p.frase}\"")
    }

}
