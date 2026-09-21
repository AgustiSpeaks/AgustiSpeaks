package com.tunombre.agustispeaks.avatar

import java.text.Normalizer

/**
 * Interpreta una frase y devuelve una [ResolvedAction] (acción + parte del
 * cuerpo + objeto). 100% offline por palabras clave.
 */
object ActionResolver {

    /**
     * Frase en minúsculas, sin tildes y con un espacio a cada lado, que es la
     * forma que esperan [has] y [w]. El catálogo usa esta misma función a
     * propósito: dos normalizaciones parecidas pero distintas serían otra
     * versión del problema de "cómo" contra "como", y esa ya nos costó bastante.
     */
    fun normalized(phrase: String): String = " " + normalize(phrase) + " "

    /** Parte del cuerpo nombrada en un texto ya normalizado por [normalized]. */
    fun bodyPartOf(normalized: String): BodyPart? = bodyPart(normalized)

    fun resolve(phrase: String): ResolvedAction {
        if (phrase.isBlank()) return ResolvedAction(AvatarAction.IDLE)
        val t = normalized(phrase)
        val part = bodyPart(t)

        /* 0) Social y estado. Va primero porque al quitar las tildes "cómo" (la
              pregunta) y "como" (yo como) quedan idénticos, y la regla de comer
              busca subcadenas: sin esto "¿Cómo estás?" salía comiendo. */
        when {
            has(t, "como estas", "como te sientes", "como andas", "como te va", "que tal") ->
                return R(AvatarAction.ASK)
            has(t, "por favor") -> return R(AvatarAction.PLEASE)
            w(t, "bien") || has(t, "estoy bien", "muy bien", "todo bien") -> return R(AvatarAction.GOOD)
            w(t, "mal") || has(t, "estoy mal", "me siento mal") -> return R(AvatarAction.BAD)
        }

        /* 1) Bebidas específicas (antes que cualquier otra cosa: "te" es corto). */
        if (w(t, "agua")) return R(AvatarAction.DRINK, prop = Prop.WATER)
        if (w(t, "leche")) return R(AvatarAction.DRINK, prop = Prop.MILK)
        if (w(t, "jugo", "jugos")) return R(AvatarAction.DRINK, prop = Prop.JUICE)
        if (w(t, "te", "mate") || has(t, "tomar te", "tacita de te")) return R(AvatarAction.DRINK, prop = Prop.TEA)
        if (w(t, "chocolatada", "cocoa") || has(t, "chocolate caliente", "leche chocolatada")) return R(AvatarAction.DRINK, prop = Prop.COCOA)
        if (w(t, "bebida", "beber", "sed", "gaseosa", "refresco") || has(t, "quiero beber", "tengo sed")) return R(AvatarAction.DRINK, prop = Prop.CUP)

        /* 2) Higiene / cuidado. */
        when {
            has(t, "lavar", "lavarme", "lavo") && w(t, "mano", "manos") -> return R(AvatarAction.WASH_HANDS)
            has(t, "cepillar", "cepillarme", "lavarme los dientes") && w(t, "diente", "dientes", "muela") -> return R(AvatarAction.BRUSH_TEETH)
            has(t, "banar", "banarme", "ducha", "duchar", "me bano") -> return R(AvatarAction.BATHE)
            has(t, "peinar", "peinarme") || has(t, "cepillarme el pelo") -> return R(AvatarAction.COMB)
            w(t, "pipi", "caca", "bano", "inodoro") || has(t, "hacer pis", "ir al bano") -> return R(AvatarAction.TOILET)
        }

        /* 3) Salud. */
        when {
            has(t, "me duele", "duele", "dolor", "lastim") -> return R(AvatarAction.HURT, part)
            w(t, "fiebre") || has(t, "temperatura") -> return R(AvatarAction.FEVER)
            w(t, "frio") -> return R(AvatarAction.COLD)
            w(t, "calor") -> return R(AvatarAction.HOT)
            w(t, "mareo", "mareado", "nausea", "nauseas", "asco", "vomito") -> return R(AvatarAction.SICK)
            w(t, "inyeccion", "vacuna", "pinchazo") -> return R(AvatarAction.INJECTION)
            w(t, "remedio", "pastilla", "jarabe", "medicina", "medicamento") -> return R(AvatarAction.MEDICINE)
        }

        /* 3.5) Alimentos: cada uno con su propio objeto en la mano. */
        foodProp(t)?.let { return R(AvatarAction.EAT, prop = it) }

        /* 3.6) Ropa. La prenda decide a qué parte del cuerpo van las manos, que
                es lo que diferencia ponerse un gorro de ponerse los zapatos. */
        garment(t)?.let { (prenda, donde) -> return R(AvatarAction.DRESS, donde, prenda) }

        /* 3.7) Compras y escuela: objetos que se llevan o se usan. Van antes de
                los verbos porque son sustantivos y nadie se los disputa. */
        when {
            has(t, "pagar", "pago") || w(t, "plata", "dinero", "moneda", "monedas", "billete") ->
                return R(AvatarAction.PAY, prop = Prop.MONEY)
            has(t, "comprar", "compro", "compra") ||
                w(t, "carrito", "changuito", "supermercado") ->
                return R(AvatarAction.SHOP, prop = Prop.CART)
            w(t, "bolsa", "bolsas") -> return R(AvatarAction.CARRY, prop = Prop.BAG)
            w(t, "canasta", "canasto") -> return R(AvatarAction.CARRY, prop = Prop.BASKET)
            // Ponerse la mochila es vestirse, con la espalda como destino.
            w(t, "mochila") -> return R(AvatarAction.DRESS, BodyPart.BACK, Prop.BACKPACK)
            // Nombrar un objeto y usarlo son cosas distintas. "Lápiz" y
            // "cuaderno" nombran: el avatar los muestra. "Escribir" y "dibujar"
            // son acciones, y ni siquiera la misma: una es sentado a la mesa y
            // la otra de pie.
            w(t, "cuaderno", "cuadernos") -> return R(AvatarAction.SHOW, prop = Prop.NOTEBOOK)
            w(t, "lapiz", "lapices", "lapicera", "birome") ->
                return R(AvatarAction.SHOW, prop = Prop.PENCIL_HELD)
            has(t, "escribir", "escribo", "escribe") ->
                return R(AvatarAction.WRITE, prop = Prop.DESK)
            w(t, "mesa", "escritorio", "pupitre") -> return R(AvatarAction.SIT, prop = Prop.TABLE)
        }

        /* 3.8) Juego. Va antes que la regla genérica de jugar: con todo cayendo
                en PLAY, el tobogán, la hamaca y los juguetes se veían iguales. */
        when {
            w(t, "juguete", "juguetes") -> return R(AvatarAction.TOYS, prop = Prop.TOYS)
            w(t, "bloques", "bloque", "cubos", "encastre") ->
                return R(AvatarAction.BLOCKS, prop = Prop.BLOCKS)
            w(t, "muneca", "munequito", "peluche", "osito", "bebote") ->
                return R(AvatarAction.CRADLE, prop = Prop.DOLL)
            w(t, "hamaca", "columpio") || has(t, "hamacar") ->
                return R(AvatarAction.SWING, prop = Prop.SWING)
            w(t, "tobogan", "resbaladilla") -> return R(AvatarAction.SLIDE, prop = Prop.SLIDE)
        }

        /* 3.9) Tiempo y cielo. Son los conceptos más abstractos del tablero: el
                reloj y el fondo cargan casi todo el significado, porque ninguna
                postura por sí sola distingue "ayer" de "mañana". */
        when {
            // Sin "ya": "ya está" es terminar, y esta sección corre antes.
            w(t, "ahora", "hoy") -> return R(AvatarAction.NOW)
            w(t, "manana", "despues", "luego", "rato") || has(t, "mas tarde") ->
                return R(AvatarAction.LATER)
            w(t, "ayer", "antes", "recien") -> return R(AvatarAction.BEFORE)
            // "dia" en singular: "buenos dias" lleva la s y no cae acá.
            w(t, "dia", "mediodia") -> return R(AvatarAction.DAY)
            w(t, "noche", "anochecer") -> return R(AvatarAction.NIGHT)
            w(t, "lluvia", "llueve", "lloviendo", "paraguas") -> return R(AvatarAction.RAIN)
            w(t, "nublado", "nubes", "nube") -> return R(AvatarAction.CLOUDY)
        }

        /* 3.10) Los que faltaban sueltos, reusando lo que ya existe. */
        when {
            // El jabón se muestra; lavarse las manos es otra cosa. Que las dos
            // palabras cayeran en la misma animación las volvía indistinguibles.
            w(t, "jabon", "jaboncito") -> return R(AvatarAction.SHOW, prop = Prop.SOAP)
            w(t, "toalla", "toallon") -> return R(AvatarAction.SHOW, prop = Prop.TOWEL)
            w(t, "curita", "venda", "vendita") ->
                return R(AvatarAction.TOUCH_PART, BodyPart.ARM, Prop.BANDAGE)
        }

        /* 3.11) Lugares: camina y el sitio aparece detrás. */
        when {
            w(t, "casa", "hogar") -> return R(AvatarAction.GO_TO, prop = Prop.HOUSE)
            w(t, "escuela", "colegio", "jardin") -> return R(AvatarAction.GO_TO, prop = Prop.SCHOOL)
            w(t, "plaza", "parque") -> return R(AvatarAction.GO_TO, prop = Prop.PARK)
            w(t, "hospital", "doctor", "medico") -> return R(AvatarAction.GO_TO, prop = Prop.HOSPITAL)
            w(t, "tienda", "negocio", "kiosco", "almacen") ->
                return R(AvatarAction.GO_TO, prop = Prop.SHOP)
            w(t, "cocina") -> return R(AvatarAction.GO_TO, prop = Prop.KITCHEN)
            w(t, "auto", "coche", "carro", "colectivo") -> return R(AvatarAction.GO_TO, prop = Prop.CAR)
            w(t, "adentro", "entrar") -> return R(AvatarAction.GO_TO, prop = Prop.DOOR)
        }

        /* 3.12) Familia y gente de la escuela: la otra persona aparece al lado.
                 "bebe" no choca con "beber" porque la coincidencia es por
                 palabra completa, y además las bebidas ya se resolvieron arriba. */
        when {
            w(t, "mama", "mami", "madre") -> return R(AvatarAction.WITH, prop = Prop.MOM)
            w(t, "papa", "papi", "padre") -> return R(AvatarAction.WITH, prop = Prop.DAD)
            w(t, "hermana") -> return R(AvatarAction.WITH, prop = Prop.SISTER)
            w(t, "hermano") -> return R(AvatarAction.WITH, prop = Prop.BROTHER)
            w(t, "abuela", "abu") -> return R(AvatarAction.WITH, prop = Prop.GRANDMA)
            w(t, "abuelo") -> return R(AvatarAction.WITH, prop = Prop.GRANDPA)
            w(t, "tia") -> return R(AvatarAction.WITH, prop = Prop.AUNT)
            w(t, "tio") -> return R(AvatarAction.WITH, prop = Prop.UNCLE)
            w(t, "maestra", "maestro", "seno", "profe") ->
                return R(AvatarAction.WITH, prop = Prop.TEACHER)
            w(t, "companero", "companera", "amigo", "amiga") ->
                return R(AvatarAction.WITH, prop = Prop.FRIEND)
            w(t, "bebe", "beba") -> return R(AvatarAction.CRADLE, prop = Prop.BABY)
        }

        /* 4) Acciones con verbo. */
        when {
            has(t, "correr", "corro", "corre") -> return R(AvatarAction.RUN)
            has(t, "saltar", "salto", "brincar") -> return R(AvatarAction.JUMP)
            has(t, "bailar", "baile", "bailo") || w(t, "musica") -> return R(AvatarAction.DANCE)
            has(t, "cantar", "canto") || w(t, "cancion") -> return R(AvatarAction.SING)
            has(t, "sentar", "sentarme", "sentado") || w(t, "silla") -> return R(AvatarAction.SIT)
            // "veni" cubre también "vení": la normalización ya sacó la tilde.
            has(t, "venir", "veni") || has(t, "ven aca", "ven aqui") -> return R(AvatarAction.COME)
            has(t, "termine", "terminar", "termino") || w(t, "listo", "lista") ||
                has(t, "ya esta", "no mas") -> return R(AvatarAction.DONE)
            has(t, "levantar", "levantarme", "pararme") || has(t, "de pie") -> return R(AvatarAction.STAND)
            has(t, "dormir", "duermo", "acostar", "descansar") || w(t, "sueno", "siesta") -> return R(AvatarAction.SLEEP)
            w(t, "cansado", "cansada", "agotado") -> return R(AvatarAction.TIRED)
            has(t, "escuchar", "escucho") || w(t, "oir") -> return R(AvatarAction.LISTEN)
            has(t, "leer", "leo") || w(t, "libro", "cuento") -> return R(AvatarAction.READ)
            // Escribir y lápiz salieron de acá: ahora tienen sus propias
            // acciones en la sección 3.7. Queda dibujar y pintar, de pie.
            has(t, "dibujar", "dibujo", "pintar", "pinto") -> return R(AvatarAction.SCRIBBLE)
            // Juguetes, tobogán y hamaca ya se fueron a sus propias acciones en
            // la sección 3.8. Acá queda el juego genérico con la pelota.
            has(t, "jugar", "juego") || w(t, "pelota", "recreo") -> return R(AvatarAction.PLAY)
            has(t, "buscar", "busco", "busca", "encontrar") || has(t, "donde esta") -> return R(AvatarAction.SEARCH)
            has(t, "tocar", "toco", "toca") && !has(t, "no toques", "no tocar") -> return R(AvatarAction.TAP)
            has(t, "abrir", "abro", "abre") -> return R(AvatarAction.OPEN)
            has(t, "cerrar", "cierro", "cierra") -> return R(AvatarAction.CLOSE)
            has(t, "abrazo", "abrazar", "abrazame") -> return R(AvatarAction.HUG)
            w(t, "beso") || has(t, "besar", "besito") -> return R(AvatarAction.KISS)
            has(t, "esperar", "esperame") || w(t, "espera") -> return R(AvatarAction.WAIT)
            has(t, "mirar", "miro", "mira") || w(t, "ver") -> return R(AvatarAction.LOOK)
            has(t, "senalar") || w(t, "esto", "eso", "aca", "alli", "alla", "aqui") -> return R(AvatarAction.POINT)
            has(t, "dame eso", "dame") || w(t, "dar") -> return R(AvatarAction.GIVE)
            has(t, "agarrar", "traer") || w(t, "tomar") && !w(t, "agua", "leche", "te", "jugo") -> return R(AvatarAction.TAKE)
            has(t, "comer", "como", "comida", "hambre") -> return R(AvatarAction.EAT, prop = Prop.FOOD)
            has(t, "irme", "caminar", "camino", "salir", "paseo") || w(t, "ir", "vamos", "afuera") -> return R(AvatarAction.GO)
        }

        /* 5) Nombrar / señalar una parte del cuerpo (sin verbo). */
        if (part != null) return R(AvatarAction.TOUCH_PART, part)

        /* 6) Social. */
        when {
            w(t, "hola") || has(t, "buenos dias", "buenas tardes", "buenas noches") -> return R(AvatarAction.HELLO)
            w(t, "chau", "adios") || has(t, "hasta luego", "nos vemos") -> return R(AvatarAction.BYE)
            w(t, "gracias") -> return R(AvatarAction.THANKS)
            w(t, "perdon", "disculpa") || has(t, "lo siento") -> return R(AvatarAction.SORRY)
            w(t, "ayuda", "ayudame", "auxilio", "socorro") -> return R(AvatarAction.HELP)
            // "parar" va como palabra completa y después de la sección 4, donde
            // "pararme" ya se resolvió como levantarse. Las dos cosas hacen
            // falta: sin la palabra completa, "pararme" caería acá.
            w(t, "basta", "quieto", "detente", "alto", "parar", "pare") ||
                has(t, "no sigas") -> return R(AvatarAction.STOP)
        }

        /* 7) Respuestas y cantidad. */
        when {
            has(t, "no quiero", "no me gusta") || w(t, "no", "nada") -> return R(AvatarAction.NO)
            w(t, "si", "claro", "dale") || has(t, "esta bien") -> return R(AvatarAction.YES)
            w(t, "mas") || has(t, "otra vez", "de nuevo") -> return R(AvatarAction.MORE)
            w(t, "menos", "poco", "poquito") -> return R(AvatarAction.LESS)
            w(t, "quiero", "necesito", "quisiera") -> return R(AvatarAction.WANT)
        }

        /* 8) Emociones. */
        when {
            has(t, "llorar", "lloro", "llorando") -> return R(AvatarAction.CRY)
            has(t, "reir", "reirme") || w(t, "risa", "jaja") -> return R(AvatarAction.LAUGH)
            w(t, "nervioso", "nerviosa", "ansioso", "ansiosa", "nervios") -> return R(AvatarAction.NERVOUS)
            w(t, "triste", "solo", "sola") || has(t, "me siento mal") -> return R(AvatarAction.SAD)
            w(t, "enojado", "enojada", "furioso", "rabia", "bronca") -> return R(AvatarAction.ANGRY)
            w(t, "miedo", "asustado", "asustada", "susto") -> return R(AvatarAction.SCARED)
            w(t, "aburrido", "aburrida") || has(t, "me aburro") -> return R(AvatarAction.BORED)
            w(t, "tranquilo", "tranquila", "calma", "relajado") -> return R(AvatarAction.CALM)
            w(t, "sorpresa", "sorprendido", "sorprendida", "wow", "guau") -> return R(AvatarAction.SURPRISED)
            w(t, "feliz", "contento", "contenta", "genial") || has(t, "me gusta") -> return R(AvatarAction.HAPPY)
        }

        /* 9) Pronombres, al final y nunca antes.
              "Yo" y "tú" solo significan algo cuando están solos: la app arma la
              oración entera, así que "yo quiero agua" tiene que resolverse por
              el verbo. Si estas reglas estuvieran arriba, cualquier frase que
              empiece con "Yo" mostraría al avatar señalándose el pecho en vez
              de haciendo lo que pidió. */
        when {
            w(t, "yo", "mi", "mio") -> return R(AvatarAction.TOUCH_PART, BodyPart.CHEST)
            w(t, "tu", "vos", "usted") -> return R(AvatarAction.POINT)
        }

        return R(AvatarAction.TALK)
    }

    private fun R(action: AvatarAction, part: BodyPart? = null, prop: Prop? = null) =
        ResolvedAction(action, part, prop)

    /** Contiene la subcadena tal cual (para stems y frases de varias palabras). */
    private fun has(t: String, vararg keys: String) = keys.any { t.contains(it) }

    /** Coincidencia de palabra completa (t viene con espacios al inicio y al final). */
    private fun w(t: String, vararg keys: String) = keys.any { t.contains(" $it ") }

    /**
     * Alimento concreto nombrado en la frase. Cada uno lleva su propio dibujo,
     * para que pedir sopa no se vea igual que pedir una galleta.
     */
    private fun foodProp(t: String): Prop? = when {
        w(t, "sopa", "caldo", "puree", "pure") -> Prop.SOUP
        w(t, "fideo", "fideos", "pasta", "tallarines", "spaghetti", "ravioles") -> Prop.NOODLES
        w(t, "arroz") -> Prop.RICE
        w(t, "pollo", "milanesa") -> Prop.CHICKEN
        w(t, "carne", "bife", "hamburguesa", "asado") -> Prop.MEAT
        w(t, "huevo", "huevos", "tortilla") -> Prop.EGG
        w(t, "pan", "tostada", "tostadas", "sandwich", "medialuna") -> Prop.BREAD
        w(t, "manzana", "fruta", "frutas", "pera", "naranja", "durazno") -> Prop.APPLE
        w(t, "banana", "platano") -> Prop.BANANA
        w(t, "galleta", "galletas", "galletita", "galletitas") -> Prop.COOKIE
        w(t, "yogur", "yogurt") -> Prop.YOGURT
        w(t, "torta", "pastel", "budin", "bizcocho") -> Prop.CAKE
        w(t, "helado") -> Prop.ICE_CREAM
        w(t, "queso") -> Prop.CHEESE
        w(t, "pizza", "empanada", "empanadas") -> Prop.PIZZA
        else -> null
    }

    /**
     * Prenda nombrada en la frase, y la parte del cuerpo donde va puesta. El
     * destino de las manos es lo que hace que ponerse un gorro no se vea igual
     * que ponerse los zapatos.
     */
    private fun garment(t: String): Pair<Prop, BodyPart>? = when {
        w(t, "remera", "camiseta", "polera", "camisa") -> Prop.SHIRT to BodyPart.CHEST
        w(t, "campera", "abrigo", "buzo", "saco", "chaqueta") -> Prop.JACKET to BodyPart.CHEST
        w(t, "pijama", "pijamas") -> Prop.PAJAMAS to BodyPart.CHEST
        w(t, "pantalon", "pantalones", "short", "jean") -> Prop.PANTS to BodyPart.LEG
        w(t, "zapatos", "zapato", "zapatillas", "botas") -> Prop.SHOES to BodyPart.FOOT
        w(t, "medias", "media", "calcetines") -> Prop.SOCKS to BodyPart.FOOT
        w(t, "gorro", "gorra", "sombrero") -> Prop.HAT to BodyPart.HEAD
        else -> null
    }

    private fun bodyPart(t: String): BodyPart? = when {
        w(t, "cabeza", "craneo") -> BodyPart.HEAD
        w(t, "frente") -> BodyPart.FOREHEAD
        w(t, "pelo", "cabello") -> BodyPart.HAIR
        w(t, "ojo", "ojos") -> BodyPart.EYE
        w(t, "nariz") -> BodyPart.NOSE
        w(t, "cachete", "mejilla", "cara") -> BodyPart.CHEEK
        w(t, "oreja", "orejas", "oido") -> BodyPart.EAR
        w(t, "diente", "dientes", "muela", "boca") -> BodyPart.TOOTH
        w(t, "garganta", "cuello") -> BodyPart.THROAT
        w(t, "pecho", "corazon") -> BodyPart.CHEST
        w(t, "panza", "estomago", "barriga", "guata", "ombligo") -> BodyPart.BELLY
        w(t, "hombro") -> BodyPart.SHOULDER
        w(t, "brazo", "brazos", "codo") -> BodyPart.ARM
        w(t, "mano", "manos", "dedo") -> BodyPart.HAND
        w(t, "pierna", "piernas", "rodilla") -> BodyPart.LEG
        w(t, "pie", "pies", "tobillo") -> BodyPart.FOOT
        w(t, "espalda") -> BodyPart.BACK
        else -> null
    }

    private fun normalize(s: String): String =
        Normalizer.normalize(s.lowercase(), Normalizer.Form.NFD)
            .replace("\\p{Mn}+".toRegex(), "")
}
