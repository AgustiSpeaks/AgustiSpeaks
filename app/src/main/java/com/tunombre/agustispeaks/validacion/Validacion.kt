package com.tunombre.agustispeaks.validacion

import android.util.Log
import com.tunombre.agustispeaks.BuildConfig

/**
 * Instrumentación de medición para el capítulo de validación de la tesis
 * (criterio 10, latencia pictograma → voz).
 *
 * Es el **único** archivo de la app que existe para medir y no para funcionar.
 * Está concentrado acá a propósito: cuando la medición termine, se borra este
 * archivo y se quitan las llamadas que lo usan (`tapPictogram`, desde el
 * tablero, y dos en `TtsManager`). No hay lógica de la app mezclada con lógica
 * de medición en ningún otro lado.
 *
 * ### Doble compuerta
 *
 * Nada se registra si no se cumplen las dos condiciones: que sea una build de
 * depuración y que [ACTIVA] esté en `true`.
 *
 * `BuildConfig.DEBUG` es la que importa, porque es automática: una build de
 * release no mide aunque alguien se olvide de bajar el interruptor. [ACTIVA] es
 * para poder apagarlo a mano, cuando se quiere usar la app sin ensuciar el
 * Logcat.
 *
 * ### Qué mide exactamente
 *
 * Desde que el tablero procesa el toque hasta que el motor de voz avisa que
 * empezó la locución (`UtteranceProgressListener.onStart`). No mide el sonido
 * saliendo del parlante: eso necesitaría un micrófono externo. Es el último
 * instante que la app puede observar.
 *
 * ### Formato: una fila CSV por pulsación
 *
 * Cada mensaje con el tag `VALIDACION` es una fila CSV, sin nada más, para que
 * `adb logcat -v raw` la vuelque directamente a un archivo. La primera
 * pulsación del proceso escribe antes el encabezado.
 *
 * ```
 * n,frase,pictogramas,latencia_ms,regimen
 * 1,"Hola",1,612,arranque_frio
 * 2,"Yo",1,34,nueva
 * 3,"Yo Quiero",2,38,nueva
 * ```
 *
 * - `n` cuenta **pulsaciones**, no mediciones: si una pulsación no llega a
 *   sonar, su número falta y el hueco se ve en el CSV.
 * - `pictogramas` es cuántos pictogramas tiene la oración acumulada.
 * - `regimen` se calcula, no se supone: `arranque_frio` es la primera pulsación
 *   del proceso; `repetida`, una oración que ya se tocó en esta sesión, y que
 *   el motor de voz puede tener en caché (un error de protocolo, no un dato
 *   normal); `nueva`, todo lo demás.
 */
object Validacion {

    /** Interruptor manual. Ponelo en `false` para dejar de registrar. */
    private const val ACTIVA = true

    private const val TAG = "VALIDACION"

    private const val ENCABEZADO = "n,frase,pictogramas,latencia_ms,regimen"

    private val midiendo: Boolean
        get() = BuildConfig.DEBUG && ACTIVA

    /**
     * Una pulsación pendiente de medir.
     *
     * Los datos viajan juntos en un objeto inmutable, y no como campos sueltos.
     * Con campos sueltos, un `onStart` que llega desde el hilo del motor de voz
     * puede leer el instante de una pulsación y la frase de otra, y la resta
     * sale contra el reloj equivocado. Eso produjo dos mediciones de 5 y 10 ms
     * en septiembre: valores que no alcanzan para que un sintetizador arranque.
     */
    private class Pendiente(
        val n: Int,
        val nanos: Long,
        val frase: String,
        val pictogramas: Int,
        val regimen: String,
        /** Id de la locución, cuando `speak` ya la creó. */
        val locucion: String?
    ) {
        fun conLocucion(id: String) = Pendiente(n, nanos, frase, pictogramas, regimen, id)
    }

    // Escrita desde el hilo principal, leída desde un hilo del motor de voz.
    @Volatile
    private var pendiente: Pendiente? = null

    // Solo los toca `pulsacion`, que corre siempre en el hilo principal.
    private var pulsaciones = 0
    private val tocadas = HashSet<String>()

    /**
     * Momento en que se tocó un pictograma, con la oración acumulada que eso
     * produjo y cuántos pictogramas tiene.
     *
     * Se llama después de armar la oración y antes de pedir la voz. Entre el
     * toque y este punto hay una concatenación de cadenas, del orden de
     * microsegundos: irrelevante frente a una latencia que se mide en
     * milisegundos, y a cambio la frase que queda registrada es exactamente la
     * que se va a pronunciar.
     */
    fun pulsacion(frase: String, pictogramas: Int) {
        if (!midiendo) return
        pulsaciones += 1
        if (pulsaciones == 1) Log.i(TAG, ENCABEZADO)
        val regimen = when {
            pulsaciones == 1 -> "arranque_frio"
            frase in tocadas -> "repetida"
            else -> "nueva"
        }
        tocadas += frase
        pendiente = Pendiente(pulsaciones, System.nanoTime(), frase, pictogramas, regimen, locucion = null)
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
        pendiente = p.conLocucion(id)
    }

    /**
     * El motor de voz empezó la locución. Calcula y registra la fila.
     *
     * **La marca se consume.** Sin eso, cualquier otra cosa que hable —el botón
     * "Decir", la prueba de voz de Ajustes, una frase de Recientes— registraría
     * una latencia medida contra una pulsación vieja, y las mediciones dejarían
     * de ser homogéneas. Solo se mide el camino pictograma → voz, que es el que
     * la tesis afirma.
     */
    fun vozIniciada(id: String?) {
        if (!midiendo) return
        val p = pendiente ?: return
        // El sonido que arranca tiene que ser el de ESTA pulsación. Si el id no
        // coincide, es el `onStart` rezagado de una locución que ya se cortó:
        // medirla daría una latencia contra el reloj equivocado.
        if (id == null || id != p.locucion) return
        pendiente = null
        val ms = (System.nanoTime() - p.nanos) / 1_000_000
        Log.i(TAG, "${p.n},${csv(p.frase)},${p.pictogramas},$ms,${p.regimen}")
    }

    /** Entre comillas, con las comillas internas duplicadas, como pide CSV. */
    private fun csv(s: String) = "\"" + s.replace("\"", "\"\"") + "\""
}
