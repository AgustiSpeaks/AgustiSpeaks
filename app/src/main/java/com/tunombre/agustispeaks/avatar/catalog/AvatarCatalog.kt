package com.tunombre.agustispeaks.avatar.catalog

import android.content.Context
import android.util.Log
import com.tunombre.agustispeaks.avatar.ActionResolver
import com.tunombre.agustispeaks.avatar.AvatarAction
import com.tunombre.agustispeaks.avatar.AvatarPose
import com.tunombre.agustispeaks.avatar.ResolvedAction
import com.tunombre.agustispeaks.avatar.avatarPoseFor

/**
 * El catálogo en funcionamiento. Es la única puerta por la que la app pregunta
 * "qué acción es esta frase" y "cómo se ve en el instante t".
 *
 * Tres capas, de menor a mayor precedencia:
 *
 * 1. `avatarPoseFor` y `ActionResolver`, el motor de siempre, que no se tocó.
 * 2. `assets/animaciones.json`, el catálogo empaquetado en el APK.
 * 3. La base de datos, con lo que agregó o corrigió el especialista.
 *
 * Y el mismo criterio en las tres funciones públicas: **si la capa de arriba no
 * tiene respuesta, contesta la de abajo**. Si el archivo de animaciones no se
 * puede abrir, o le falta un registro, o tiene un error de tipeo, la app sigue
 * andando exactamente como antes. En una app de comunicación esa garantía no es
 * una comodidad de desarrollo: es que el chico pueda seguir pidiendo agua
 * aunque nos hayamos equivocado nosotros.
 */
object AvatarCatalog {

    private const val TAG = "AvatarCatalog"
    private const val ASSET = "animaciones.json"

    @Volatile
    private var catalogo: AnimationCatalog = AnimationCatalog.EMPTY

    @Volatile
    private var base: List<AnimationRecord> = emptyList()

    @Volatile
    private var encima: List<AnimationRecord> = emptyList()

    // Los problemas se guardan por capa. Juntarlos en una sola lista haría que
    // recargar la capa de arriba fuera acumulando copias de los de abajo.
    @Volatile
    private var problemasBase: List<String> = emptyList()

    @Volatile
    private var problemasEncima: List<String> = emptyList()

    /** Registros que no se pudieron leer. Vacío es lo esperable. */
    val problemas: List<String> get() = problemasBase + problemasEncima

    /** Resumen legible del estado, para Logcat o una pantalla de ajustes. */
    @Volatile
    var estado: String = "sin cargar"
        private set

    /* ---------------- carga ---------------- */

    /**
     * Capa empaquetada. Se llama desde `Application.onCreate` y es **síncrona a
     * propósito**: son unos 30 KB, y hacerla en segundo plano abriría una
     * ventana en la que las primeras animaciones saldrían por el camino viejo y
     * las siguientes por el nuevo, que es la clase de diferencia intermitente
     * que después no hay forma de reproducir.
     */
    fun loadBase(context: Context) {
        val json = try {
            context.assets.open(ASSET).bufferedReader().use { it.readText() }
        } catch (e: Exception) {
            estado = "no se pudo abrir $ASSET (${e.message}); sigue el motor de siempre"
            Log.w(TAG, estado)
            return
        }

        val leido = AnimationParser.parse(json)
        base = leido.records
        problemasBase = leido.problemas
        rearmar()
        Log.i(TAG, estado)
        leido.problemas.forEach { Log.w(TAG, it) }
    }

    /**
     * Capa del especialista. Esta sí va en segundo plano: abrir la base de
     * datos en el arranque demoraría el primer dibujo, y mientras tanto la app
     * ya está andando con la capa empaquetada, que es la verificada. Lo único
     * que puede pasar en esa ventana es que una corrección tarde un instante en
     * aplicarse; nunca que una animación falte.
     */
    suspend fun loadOverrides(context: Context) {
        val guardados = try {
            AnimationDatabase.get(context).animaciones().todas()
        } catch (e: Exception) {
            Log.w(TAG, "no se pudo leer la base de animaciones: ${e.message}")
            return
        }
        // Se reemplaza la capa entera, incluso si quedó vacía: borrar el último
        // registro guardado tiene que devolver el catálogo al estado del APK.
        val leido = AnimationParser.parse(comoArreglo(guardados.map { it.json }))
        encima = leido.records
        problemasEncima = leido.problemas.map { "base de datos: $it" }
        rearmar()
        Log.i(TAG, estado)
        problemasEncima.forEach { Log.w(TAG, it) }
    }

    private fun rearmar() {
        val fusionado = fusionar(base, encima)
        catalogo = AnimationCatalog(fusionado)
        estado = "${fusionado.size} animaciones (${base.size} del APK, " +
            "${encima.size} guardadas), ${problemas.size} con problemas"
    }

    private fun comoArreglo(jsons: List<String>): String =
        jsons.joinToString(",", "[", "]")

    /* ---------------- escritura ---------------- */

    /**
     * Guarda o reemplaza una animación. [json] es el objeto de un solo registro,
     * con la misma forma que los de `animaciones.json`.
     *
     * **Se valida antes de guardar.** Un registro que no se puede leer no llega
     * a la base: si se guardara igual, el error reaparecería en cada arranque y
     * no habría forma de saber cuál de todos lo causó. Devuelve el problema para
     * que quien esté editando lo vea en el momento, que es cuando sirve.
     */
    suspend fun guardar(context: Context, json: String): String? {
        val leido = AnimationParser.parse(comoArreglo(listOf(json)))
        leido.problemas.firstOrNull()?.let { return it }
        val registro = leido.records.firstOrNull() ?: return "El JSON no contiene ningún registro."

        return try {
            AnimationDatabase.get(context).animaciones().guardar(
                AnimationEntity(registro.id, json, System.currentTimeMillis())
            )
            loadOverrides(context)
            null
        } catch (e: Exception) {
            "No se pudo guardar: ${e.message}"
        }
    }

    /**
     * Borra la versión guardada de una animación. Si existía en el APK, el
     * registro empaquetado vuelve a mandar: borrar una corrección es deshacerla,
     * no dejar a la app sin esa animación.
     */
    suspend fun borrar(context: Context, id: String): String? = try {
        AnimationDatabase.get(context).animaciones().borrar(id)
        loadOverrides(context)
        null
    } catch (e: Exception) {
        "No se pudo borrar: ${e.message}"
    }

    /* ---------------- consulta ---------------- */

    /** Pose del catálogo si el id existe; si no, la de [avatarPoseFor]. */
    fun pose(r: ResolvedAction, t: Float): AvatarPose =
        catalogo.byId(catalogIdOf(r))?.poseAt(t, r) ?: avatarPoseFor(r, t)

    /** Duración del bucle. El registro puede cambiarla sin tocar el enum. */
    fun loopMillis(r: ResolvedAction): Int =
        catalogo.byId(catalogIdOf(r))?.loopMillis ?: r.action.loopMillis

    /**
     * Interpreta una frase. Primero las reglas del catálogo, por prioridad
     * ascendente; después las de [ActionResolver], intactas.
     *
     * Un registro sin `palabras` ni `raices` no dispara nunca, así que el
     * catálogo recién nacido no cambia ninguna decisión: se mete en el camino
     * solo cuando alguien le escribe disparadores.
     */
    fun resolve(phrase: String): ResolvedAction {
        if (phrase.isBlank()) return ResolvedAction(AvatarAction.IDLE)

        val texto = ActionResolver.normalized(phrase)
        catalogo.match(texto)?.let { animacion ->
            val registro = animacion.record
            return ResolvedAction(
                action = accionDe(registro.id),
                bodyPart = if (registro.usaParteDelCuerpo) ActionResolver.bodyPartOf(texto) else null,
                prop = registro.prop,
                catalogId = registro.id
            )
        }
        return ActionResolver.resolve(phrase)
    }

    private val porNombre: Map<String, AvatarAction> =
        AvatarAction.entries.associateBy { it.name.lowercase() }

    /**
     * Una animación nacida en el catálogo no tiene [AvatarAction] propia. En ese
     * caso el valor es solo una etiqueta: tanto la pose como la duración salen
     * del registro, porque `catalogId` ya quedó fijado.
     */
    private fun accionDe(id: String): AvatarAction = porNombre[id] ?: AvatarAction.TALK
}
