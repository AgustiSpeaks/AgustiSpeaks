package com.tunombre.agustispeaks.tesis

import com.tunombre.agustispeaks.avatar.ActionResolver
import com.tunombre.agustispeaks.avatar.AvatarAction
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

/**
 * Criterios 5 y 6 de la bitácora — Corpus del resolvedor.
 *
 * Oraciones con la acción que **deberían** producir, y dos criterios de
 * validación: resolución mínima del 90 % (criterio 5) y cero colisiones
 * léxicas (criterio 6).
 *
 * Los grupos rotulan dónde vive hoy cada palabra en el tablero. Cuando una
 * palabra cambia de categoría se cambia el rótulo, nunca la acción esperada:
 * el significado de la oración no depende de dónde esté el botón.
 *
 * ### La regla que hace que esto sea una prueba y no un espejo
 *
 * La acción esperada de cada oración está decidida **por el significado de la
 * oración**, no copiando lo que el resolvedor devuelve hoy. Si estuviera copiada,
 * el corpus pasaría siempre y no mediría nada: estaría comparando el resolvedor
 * consigo mismo, que es la misma trampa de muestreo que ya nos costó cara en el
 * volcado del catálogo.
 *
 * De eso se sigue que **un fallo acá no es necesariamente un error de la prueba**.
 * Puede ser el resolvedor el que está equivocado. Los casos donde la respuesta
 * correcta es discutible llevan una nota y se listan aparte al final, para que la
 * decisión quede a la vista en lugar de escondida en una tabla.
 */
class CorpusResolvedorTest {

    /** Cada prueba escribe su propio archivo, porque son dos apéndices. */
    private lateinit var ap: Apendice

    private val minimoDeResolucion = 90.0

    private data class Caso(
        val grupo: String,
        val frase: String,
        val esperada: AvatarAction,
        /** Por qué esta acción y no otra, cuando la elección es opinable. */
        val nota: String = ""
    )

    /**
     * Pares de frases que la normalización pone peligrosamente cerca. Cada uno
     * tiene que resolverse a **dos acciones distintas**; que colapsen en una es
     * lo que el criterio llama colisión léxica.
     *
     * Existen porque quitar las tildes vuelve iguales a "cómo" y "como", y porque
     * varias reglas buscan subcadenas: "té" vive dentro de "triste" y "bebé"
     * dentro de "beber". El orden de evaluación del resolvedor es lo único que
     * los separa, así que es una propiedad de corrección, no un detalle.
     */
    private data class Trampa(
        val a: String, val esperadaA: AvatarAction,
        val b: String, val esperadaB: AvatarAction,
        val porque: String
    )

    private val trampas = listOf(
        Trampa("Estoy triste", AvatarAction.SAD, "Quiero té", AvatarAction.DRINK,
            "\"te\" es subcadena de \"triste\""),
        Trampa("¿Cómo estás?", AvatarAction.ASK, "Quiero comer", AvatarAction.EAT,
            "sin tildes, \"cómo\" y \"como\" son la misma palabra"),
        Trampa("Ya está", AvatarAction.DONE, "Ahora", AvatarAction.NOW,
            "\"ya\" como disparador de ahora robaría \"ya está\""),
        Trampa("Buenas tardes", AvatarAction.HELLO, "Más tarde", AvatarAction.LATER,
            "\"tarde\" es saludo y es momento del día"),
        Trampa("Buenos días", AvatarAction.HELLO, "Es de día", AvatarAction.DAY,
            "el saludo lleva la s; el concepto va en singular"),
        Trampa("El bebé", AvatarAction.CRADLE, "Quiero beber", AvatarAction.DRINK,
            "\"bebe\" vive dentro de \"beber\""),
        Trampa("Pararme", AvatarAction.STAND, "Parar", AvatarAction.STOP,
            "levantarse y detenerse comparten la raíz"),
        Trampa("No toques", AvatarAction.NO, "Quiero tocar", AvatarAction.TAP,
            "la negación tiene que ganarle al verbo que niega"),
        Trampa("Yo", AvatarAction.TOUCH_PART, "Yo quiero agua", AvatarAction.DRINK,
            "el pronombre solo significa algo cuando está solo"),
        Trampa("Mi cabeza", AvatarAction.TOUCH_PART, "Mi mochila", AvatarAction.DRESS,
            "\"mi\" no puede decidir por sí mismo")
    )

    private val corpus = listOf(
        /* ---- núcleo y pronombres ---- */
        Caso("núcleo", "Yo", AvatarAction.TOUCH_PART),
        Caso("núcleo", "Tú", AvatarAction.POINT),
        Caso("núcleo", "Yo quiero agua", AvatarAction.DRINK,
            "la app arma la oración entera: manda el verbo con su objeto, no el pronombre"),
        Caso("núcleo", "No quiero", AvatarAction.NO),
        Caso("núcleo", "Sí", AvatarAction.YES),
        Caso("núcleo", "Ayuda", AvatarAction.HELP),
        Caso("núcleo", "Parar", AvatarAction.STOP),
        Caso("núcleo", "Terminé", AvatarAction.DONE),

        /* ---- alimentos ---- */
        Caso("alimentos", "Quiero pan", AvatarAction.EAT),
        Caso("alimentos", "Tengo hambre", AvatarAction.EAT),
        Caso("alimentos", "Quiero sopa", AvatarAction.EAT),
        Caso("alimentos", "Manzana", AvatarAction.EAT),
        Caso("alimentos", "Quiero galletas", AvatarAction.EAT),
        Caso("alimentos", "No quiero comer", AvatarAction.NO,
            "el mensaje es el rechazo, no la comida: mostrar a alguien comiendo dice lo contrario"),

        /* ---- bebidas ---- */
        Caso("bebidas", "Quiero agua", AvatarAction.DRINK),
        Caso("bebidas", "Tengo sed", AvatarAction.DRINK),
        Caso("bebidas", "Leche", AvatarAction.DRINK),
        Caso("bebidas", "Quiero té", AvatarAction.DRINK),

        /* ---- acciones ---- */
        Caso("acciones", "Quiero jugar", AvatarAction.PLAY),
        Caso("acciones", "Vamos a correr", AvatarAction.RUN),
        Caso("acciones", "Quiero dormir", AvatarAction.SLEEP),
        Caso("acciones", "Abrir la puerta", AvatarAction.OPEN),
        Caso("acciones", "Cerrar la puerta", AvatarAction.CLOSE),
        Caso("acciones", "Quiero dibujar", AvatarAction.SCRIBBLE),
        Caso("acciones", "Escribir", AvatarAction.WRITE),
        Caso("acciones", "Esperar un momento", AvatarAction.WAIT),
        Caso("acciones", "Caminar", AvatarAction.GO),
        Caso("acciones", "Sentarme", AvatarAction.SIT),
        Caso("acciones", "Quiero escuchar", AvatarAction.LISTEN),
        Caso("acciones", "Buscar mi juguete", AvatarAction.SEARCH,
            "el verbo es el mensaje; el juguete es el objeto de la búsqueda, no la acción"),

        /* ---- sentimientos ---- */
        Caso("sentimientos", "Estoy feliz", AvatarAction.HAPPY),
        Caso("sentimientos", "Estoy triste", AvatarAction.SAD),
        Caso("sentimientos", "Estoy enojado", AvatarAction.ANGRY),
        Caso("sentimientos", "Tengo miedo", AvatarAction.SCARED),
        Caso("sentimientos", "Estoy cansado", AvatarAction.TIRED),
        Caso("sentimientos", "Estoy aburrido", AvatarAction.BORED),
        Caso("sentimientos", "Estoy nervioso", AvatarAction.NERVOUS),
        Caso("sentimientos", "Estoy tranquilo", AvatarAction.CALM),
        Caso("sentimientos", "Quiero llorar", AvatarAction.CRY),

        /* ---- dolor ---- */
        Caso("dolor", "Me duele la cabeza", AvatarAction.HURT),
        Caso("dolor", "Me duele la panza", AvatarAction.HURT),
        Caso("dolor", "Me duele la garganta", AvatarAction.HURT),
        Caso("dolor", "Tengo fiebre", AvatarAction.FEVER),
        Caso("dolor", "Tengo frío", AvatarAction.COLD),
        Caso("dolor", "Tengo calor", AvatarAction.HOT),
        Caso("dolor", "Tengo náuseas", AvatarAction.SICK),
        Caso("dolor", "Quiero el remedio", AvatarAction.MEDICINE),
        Caso("dolor", "La inyección", AvatarAction.INJECTION),
        Caso("dolor", "Una curita", AvatarAction.TOUCH_PART,
            "nombrar la curita es señalar dónde duele, no una acción propia"),

        /* ---- cuerpo ---- */
        Caso("cuerpo", "Mis manos", AvatarAction.TOUCH_PART),
        Caso("cuerpo", "Mi cabeza", AvatarAction.TOUCH_PART),
        Caso("cuerpo", "Los ojos", AvatarAction.TOUCH_PART),
        Caso("cuerpo", "Mis pies", AvatarAction.TOUCH_PART),

        /* ---- ropa ---- */
        Caso("ropa", "Ponerme los zapatos", AvatarAction.DRESS),
        Caso("ropa", "Quiero la campera", AvatarAction.DRESS),
        Caso("ropa", "Mi gorro", AvatarAction.DRESS),
        Caso("ropa", "El pijama", AvatarAction.DRESS),

        /* ---- higiene ---- */
        Caso("higiene", "Lavarme las manos", AvatarAction.WASH_HANDS),
        Caso("higiene", "Cepillarme los dientes", AvatarAction.BRUSH_TEETH),
        Caso("higiene", "Quiero bañarme", AvatarAction.BATHE),
        Caso("higiene", "Peinarme", AvatarAction.COMB),
        Caso("higiene", "Jabón", AvatarAction.SHOW,
            "nombrar un objeto es mostrarlo; usarlo es lavarse las manos, que es otra acción"),
        Caso("higiene", "Quiero hacer pis", AvatarAction.TOILET),
        Caso("higiene", "La toalla", AvatarAction.SHOW),

        /* ---- tiempo ---- */
        Caso("tiempo", "Ahora", AvatarAction.NOW),
        Caso("tiempo", "Mañana", AvatarAction.LATER),
        Caso("tiempo", "Ayer", AvatarAction.BEFORE),
        Caso("tiempo", "Es de día", AvatarAction.DAY),
        Caso("tiempo", "Es de noche", AvatarAction.NIGHT),
        Caso("tiempo", "Está lloviendo", AvatarAction.RAIN),
        Caso("tiempo", "Está nublado", AvatarAction.CLOUDY),

        /* ---- familia ---- */
        Caso("familia", "Quiero a mamá", AvatarAction.WITH),
        Caso("familia", "Mi hermano", AvatarAction.WITH),
        Caso("familia", "La abuela", AvatarAction.WITH),
        Caso("familia", "Mi tía", AvatarAction.WITH),
        Caso("familia", "El bebé", AvatarAction.CRADLE),

        /* ---- juego ---- */
        Caso("juego", "Quiero los juguetes", AvatarAction.TOYS),
        Caso("juego", "La hamaca", AvatarAction.SWING),
        Caso("juego", "El tobogán", AvatarAction.SLIDE),
        Caso("juego", "Quiero la pelota", AvatarAction.PLAY),
        Caso("juego", "Los bloques", AvatarAction.BLOCKS),
        Caso("juego", "La muñeca", AvatarAction.CRADLE),

        /* ---- lugares ---- */
        Caso("lugares", "Quiero ir a casa", AvatarAction.GO_TO),
        Caso("lugares", "Vamos a la plaza", AvatarAction.GO_TO),
        Caso("lugares", "Vamos a la escuela", AvatarAction.GO_TO),
        Caso("lugares", "Quiero ir al baño", AvatarAction.TOILET,
            "el mensaje es la necesidad, no el traslado"),

        /* ---- escuela ---- */
        Caso("escuela", "La maestra", AvatarAction.WITH),
        Caso("escuela", "Mi mochila", AvatarAction.DRESS),
        Caso("escuela", "El cuaderno", AvatarAction.SHOW),
        Caso("escuela", "El lápiz", AvatarAction.SHOW),
        Caso("escuela", "El libro", AvatarAction.SHOW,
            "mismo criterio que lápiz y cuaderno: el sustantivo nombra, y leer es otra cosa"),
        Caso("escuela", "Quiero leer", AvatarAction.READ),
        Caso("escuela", "Recreo", AvatarAction.PLAY),

        /* ---- compras ---- */
        Caso("compras", "Quiero comprar", AvatarAction.SHOP),
        Caso("compras", "Pagar", AvatarAction.PAY),
        Caso("compras", "La bolsa", AvatarAction.CARRY),
        Caso("compras", "La plata", AvatarAction.SHOW,
            "corregido: la esperaba en PAGAR, lo cual contradecía el criterio que " +
                "yo mismo apliqué a lápiz y cuaderno. El sustantivo nombra"),

        /* ---- social ---- */
        // "Más" y "Gracias" eran palabras núcleo hasta octubre de 2026; hoy
        // están en Social. Cambió el rótulo, no la acción esperada.
        Caso("social", "Quiero más", AvatarAction.MORE),
        Caso("social", "Gracias", AvatarAction.THANKS),
        Caso("social", "Hola", AvatarAction.HELLO),
        Caso("social", "Chau", AvatarAction.BYE),
        Caso("social", "Por favor", AvatarAction.PLEASE),
        Caso("social", "Perdón", AvatarAction.SORRY),
        Caso("social", "Buenos días", AvatarAction.HELLO),
        Caso("social", "Buenas tardes", AvatarAction.HELLO),
        Caso("social", "¿Cómo estás?", AvatarAction.ASK),
        Caso("social", "¿Cómo estás? Bien", AvatarAction.ASK,
            "la pregunta manda sobre la respuesta: es la frase que abre el intercambio"),
        Caso("social", "Estoy bien", AvatarAction.GOOD),
        Caso("social", "Me siento mal", AvatarAction.BAD),
        Caso("social", "Ya está", AvatarAction.DONE)
    )

    /**
     * Criterio 5: resolución ≥ 90 %. Es una prueba aparte de la de colisiones
     * para que cada criterio tenga su propio veredicto: si las dos vivieran en
     * un mismo método, la primera en fallar taparía a la otra y la bitácora de
     * verificación quedaría con una fila sin resultado.
     */
    @Test
    fun resolucionDelCorpus() {
        ap = Apendice("criterio-05-corpus")
        ap.encabezado("CRITERIO 5 — CORPUS DEL RESOLVEDOR", "CorpusResolvedorTest")
        linea("Umbral: resolución ≥ ${fmt(minimoDeResolucion)} %")
        linea("")
        linea("La acción esperada se define por el significado de la oración, no por")
        linea("lo que el resolvedor devuelve. Un FALLA puede ser del resolvedor.")
        linea("")

        val fallas = tabla()
        linea("")
        decisiones()

        val resueltas = corpus.size - fallas
        val porcentaje = 100.0 * resueltas / corpus.size
        ap.veredicto(
            5, porcentaje >= minimoDeResolucion,
            "${fmt(porcentaje)} % ($resueltas de ${corpus.size} oraciones)"
        )
        ap.guardar()

        assertTrue(
            "Resolución ${fmt(porcentaje)} %, por debajo del mínimo de " +
                "${fmt(minimoDeResolucion)} %. El umbral no se toca: los casos " +
                "marcados FALLA son el dato.",
            porcentaje >= minimoDeResolucion
        )
    }

    /** Criterio 6: cero colisiones léxicas. */
    @Test
    fun colisionesLexicas() {
        ap = Apendice("criterio-06-trampas")
        ap.encabezado("CRITERIO 6 — PARES TRAMPA", "CorpusResolvedorTest")
        linea("Umbral: colisión léxica 0 casos")
        linea("")

        val colisiones = paresTrampa()
        ap.veredicto(6, colisiones == 0, "$colisiones colisiones en ${trampas.size} pares")
        ap.guardar()
        assertTrue(
            "$colisiones colisiones léxicas. El criterio exige 0: dos significados " +
                "que producen la misma animación son indistinguibles para el chico.",
            colisiones == 0
        )
    }

    /* ------------------------------------------------------------------ */

    private fun tabla(): Int {
        linea(
            "GRUPO".padEnd(13) + "ORACIÓN".padEnd(26) +
                "ESPERADA".padEnd(13) + "OBTENIDA".padEnd(13) + "ESTADO"
        )
        linea("-".repeat(72))

        var fallas = 0
        var grupoPrevio = ""
        for (c in corpus) {
            val obtenida = ActionResolver.resolve(c.frase).action
            val ok = obtenida == c.esperada
            if (!ok) fallas++

            val grupo = if (c.grupo == grupoPrevio) "" else c.grupo
            grupoPrevio = c.grupo

            linea(
                grupo.padEnd(13) + c.frase.padEnd(26) +
                    c.esperada.name.padEnd(13) + obtenida.name.padEnd(13) +
                    if (ok) "OK" else "FALLA"
            )
        }
        linea("-".repeat(72))

        val resueltas = corpus.size - fallas
        linea("")
        dato("Oraciones en el corpus", corpus.size)
        dato("Resueltas segun lo esperado", resueltas)
        dato("Desvíos", fallas)
        dato("Resolución", fmt(100.0 * resueltas / corpus.size) + " %")
        dato("Mínimo exigido", fmt(minimoDeResolucion) + " %")
        return fallas
    }

    /** Devuelve cuántos pares colapsaron. El veredicto lo da quien llama. */
    private fun paresTrampa(): Int {
        linea("Cada par tiene que resolverse a dos acciones DISTINTAS. Que colapsen")
        linea("en una sola es una colisión: dos significados con la misma animación.")
        linea("")

        var colisiones = 0
        var desvios = 0
        for (t in trampas) {
            val ra = ActionResolver.resolve(t.a).action
            val rb = ActionResolver.resolve(t.b).action
            val separa = ra != rb
            val correcto = ra == t.esperadaA && rb == t.esperadaB
            if (!separa) colisiones++
            if (!correcto) desvios++

            val marca = when {
                !separa -> "COLISIÓN"
                !correcto -> "SEPARA PERO DESVÍA"
                else -> "OK"
            }
            linea("  ${t.a} → ${ra.name}   |   ${t.b} → ${rb.name}   [$marca]")
            linea("      ${t.porque}")
        }
        linea("")
        dato("Pares evaluados", trampas.size)
        dato("Colisiones léxicas", colisiones)
        dato("Separan pero no dan lo esperado", desvios - colisiones)
        return colisiones
    }

    /**
     * Las oraciones cuya acción correcta es una decisión de producto y no una
     * lectura del diccionario. Se imprimen aparte para que queden a la vista de
     * quien revise el corpus, en vez de escondidas entre las filas de la tabla.
     */
    private fun decisiones() {
        val conNota = corpus.filter { it.nota.isNotEmpty() }
        linea("=".repeat(72))
        linea("DECISIONES DE PRODUCTO TOMADAS AL ARMAR EL CORPUS (${conNota.size})")
        linea("=".repeat(72))
        for (c in conNota) {
            linea("  \"${c.frase}\" → ${c.esperada.name}")
            linea("      ${c.nota}")
        }
    }

    /* ------------------------------------------------------------------ */

    private fun linea(s: String) = ap.linea(s)

    private fun dato(etiqueta: String, valor: Any) =
        linea("  " + etiqueta.padEnd(40, '.') + " " + valor.toString().padStart(10))

    private fun fmt(v: Double): String = String.format(Locale.ROOT, "%.1f", v)
}
