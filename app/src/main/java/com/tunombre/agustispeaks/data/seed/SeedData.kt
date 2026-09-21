package com.tunombre.agustispeaks.data.seed

import com.tunombre.agustispeaks.data.model.Category
import com.tunombre.agustispeaks.data.model.Pictogram
import com.tunombre.agustispeaks.data.model.Place

/**
 * Datos semilla (por defecto) de la app.
 *
 * - [coreWords]: palabras núcleo/neutrales. **Iguales en todos los lugares**,
 *   siempre disponibles (pestaña "Palabras" del tablero).
 * - [categories] + [pictograms]: vocabulario temático. Cada lugar
 *   ([places]) expone un subconjunto de categorías; dentro de cada categoría
 *   hay pictogramas específicos.
 *
 * `Pictogram.placeIds` se deriva: un pictograma aparece en todo lugar cuya
 * lista de categorías incluya la categoría del pictograma.
 *
 * Contenido genérico y estándar, sin personalización de usuario.
 */
object SeedData {

    const val CORE_CATEGORY_ID = "nucleo"

    /* --------------------------------------------------------------------- */
    /*  Palabras núcleo (siempre visibles, iguales en todos los lugares)      */
    /* --------------------------------------------------------------------- */

    val coreWords: List<Pictogram> = listOf(
        core("yo", "Yo", "person"),
        core("tu", "Tú", "person"),
        core("quiero", "Quiero", "pan_tool"),
        core("no_quiero", "No quiero", "do_not_touch"),
        core("si", "Sí", "thumb_up"),
        core("no", "No", "thumb_down"),
        core("mas", "Más", "add_circle"),
        core("menos", "Menos", "remove_circle"),
        core("ayuda", "Ayuda", "volunteer_activism"),
        core("parar", "Parar", "back_hand"),
        core("ir", "Ir", "directions_walk"),
        core("venir", "Venir", "waving_hand"),
        core("comer", "Comer", "restaurant"),
        core("beber", "Beber", "local_drink"),
        core("jugar", "Jugar", "sports_esports"),
        core("mirar", "Mirar", "visibility"),
        core("dar", "Dar", "volunteer_activism"),
        core("me_gusta", "Me gusta", "favorite"),
        core("no_me_gusta", "No me gusta", "heart_broken"),
        core("me_duele", "Me duele", "healing"),
        core("termine", "Terminé", "check_circle"),
        core("hola", "Hola", "waving_hand"),
        core("gracias", "Gracias", "favorite"),
        core("esto", "Esto", "touch_app"),
        core("aqui", "Aquí", "place")
    )

    /* --------------------------------------------------------------------- */
    /*  Categorías temáticas                                                 */
    /* --------------------------------------------------------------------- */

    val categories: List<Category> = listOf(
        Category("alimentos", "Alimentos", "restaurant", "#FFE0B2"),
        Category("bebidas", "Bebidas", "local_drink", "#B3E5FC"),
        Category("acciones", "Acciones", "directions_run", "#BBDEFB"),
        Category("sentimientos", "Sentimientos", "mood", "#FFF9C4"),
        Category("dolor", "Dolor", "healing", "#FFCDD2"),
        Category("cuerpo", "Partes del cuerpo", "accessibility_new", "#F8BBD0"),
        Category("ropa", "Ropa", "checkroom", "#D1C4E9"),
        Category("higiene", "Higiene", "clean_hands", "#C8E6C9"),
        Category("tiempo", "Tiempo", "wb_sunny", "#FFF59D"),
        Category("familia", "Familia", "family_restroom", "#E1BEE7"),
        Category("juego", "Juego", "toys", "#FFCC80"),
        Category("lugares", "Lugares", "place", "#B2DFDB"),
        Category("escuela", "Escuela", "school", "#B39DDB"),
        Category("compras", "Compras", "shopping_cart", "#FFE082"),
        Category("social", "Social", "waving_hand", "#F48FB1")
    )

    /* --------------------------------------------------------------------- */
    /*  Lugares                                                              */
    /* --------------------------------------------------------------------- */

    /**
     * Un lugar expone un subconjunto de categorías. **"lugares" va en todos**:
     * es el vocabulario de destinos, y querer estar en otra parte es algo que se
     * necesita decir justamente desde donde uno está. Restringir desde dónde se
     * puede pedir "quiero ir a casa" no ahorra nada y cierra una puerta.
     *
     * Hasta ahora esa categoría no figuraba en ningún lugar, así que sus diez
     * pictogramas existían en la semilla y eran inalcanzables en el tablero. Lo
     * encontró la prueba de inventario, contando lugares por categoría.
     */
    val places: List<Place> = listOf(
        Place(
            "colegio", "Colegio", "school", "#A8D8EA",
            listOf("acciones", "sentimientos", "social", "familia", "escuela", "juego", "bebidas", "cuerpo", "lugares")
        ),
        Place(
            "living", "Living", "weekend", "#C7CEEA",
            listOf("acciones", "sentimientos", "social", "familia", "juego", "bebidas", "tiempo", "lugares")
        ),
        Place(
            "cocina", "Cocina", "restaurant", "#FFD9A8",
            listOf("alimentos", "bebidas", "acciones", "sentimientos", "social", "lugares")
        ),
        Place(
            "bano", "Baño", "bathtub", "#B5EAD7",
            listOf("cuerpo", "higiene", "ropa", "acciones", "sentimientos", "lugares")
        ),
        Place(
            "patio", "Patio", "park", "#D0E8C5",
            listOf("acciones", "sentimientos", "social", "familia", "juego", "tiempo", "cuerpo", "lugares")
        ),
        Place(
            "dormitorio", "Dormitorio", "bed", "#E2C6F0",
            listOf("acciones", "sentimientos", "cuerpo", "ropa", "juego", "tiempo", "lugares")
        ),
        Place(
            "hospital", "Hospital", "local_hospital", "#FFB3BA",
            listOf("cuerpo", "dolor", "sentimientos", "acciones", "familia", "higiene", "lugares")
        ),
        Place(
            "tienda", "Tienda", "shopping_cart", "#F0DAB1",
            listOf("compras", "alimentos", "bebidas", "ropa", "acciones", "sentimientos", "social", "lugares")
        )
    )

    /* --------------------------------------------------------------------- */
    /*  Pictogramas temáticos                                                */
    /* --------------------------------------------------------------------- */

    private data class Seed(val id: String, val text: String, val icon: String, val category: String)

    private val seeds: List<Seed> = listOf(
        // Alimentos
        Seed("pan", "Pan", "bakery_dining", "alimentos"),
        Seed("manzana", "Manzana", "nutrition", "alimentos"),
        Seed("banana", "Banana", "nutrition", "alimentos"),
        Seed("fruta", "Fruta", "nutrition", "alimentos"),
        Seed("galletas", "Galletas", "cookie", "alimentos"),
        Seed("fideos", "Fideos", "ramen_dining", "alimentos"),
        Seed("arroz", "Arroz", "rice_bowl", "alimentos"),
        Seed("pollo", "Pollo", "lunch_dining", "alimentos"),
        Seed("carne", "Carne", "kebab_dining", "alimentos"),
        Seed("sopa", "Sopa", "soup_kitchen", "alimentos"),
        Seed("huevo", "Huevo", "egg", "alimentos"),
        Seed("yogur", "Yogur", "icecream", "alimentos"),
        Seed("torta", "Torta", "cake", "alimentos"),
        Seed("helado", "Helado", "icecream", "alimentos"),

        // Bebidas
        Seed("agua", "Agua", "water_drop", "bebidas"),
        Seed("leche", "Leche", "local_drink", "bebidas"),
        Seed("jugo", "Jugo", "local_bar", "bebidas"),
        Seed("te", "Té", "local_cafe", "bebidas"),
        Seed("chocolatada", "Chocolatada", "emoji_food_beverage", "bebidas"),

        // Acciones
        Seed("abrir", "Abrir", "lock_open", "acciones"),
        Seed("cerrar", "Cerrar", "lock", "acciones"),
        Seed("sentarse", "Sentarse", "event_seat", "acciones"),
        Seed("levantarse", "Levantarse", "accessibility_new", "acciones"),
        Seed("esperar", "Esperar", "hourglass_empty", "acciones"),
        Seed("tocar", "Tocar", "touch_app", "acciones"),
        Seed("buscar", "Buscar", "search", "acciones"),
        Seed("caminar", "Caminar", "directions_walk", "acciones"),
        Seed("correr", "Correr", "directions_run", "acciones"),
        Seed("dormir", "Dormir", "bedtime", "acciones"),
        Seed("dibujar", "Dibujar", "draw", "acciones"),
        Seed("escuchar", "Escuchar", "hearing", "acciones"),

        // Sentimientos
        Seed("feliz", "Feliz", "sentiment_very_satisfied", "sentimientos"),
        Seed("triste", "Triste", "sentiment_dissatisfied", "sentimientos"),
        Seed("enojado", "Enojado", "sentiment_very_dissatisfied", "sentimientos"),
        Seed("cansado", "Cansado", "bedtime", "sentimientos"),
        Seed("asustado", "Asustado", "mood_bad", "sentimientos"),
        Seed("aburrido", "Aburrido", "sentiment_neutral", "sentimientos"),
        Seed("nervioso", "Nervioso", "psychology", "sentimientos"),
        Seed("tranquilo", "Tranquilo", "self_improvement", "sentimientos"),
        Seed("contento", "Contento", "sentiment_satisfied", "sentimientos"),

        // Dolor
        Seed("duele_cabeza", "Me duele la cabeza", "healing", "dolor"),
        Seed("duele_panza", "Me duele la panza", "healing", "dolor"),
        Seed("duele_garganta", "Me duele la garganta", "healing", "dolor"),
        Seed("duele_muela", "Me duele la muela", "healing", "dolor"),
        Seed("fiebre", "Tengo fiebre", "thermostat", "dolor"),
        Seed("frio", "Tengo frío", "ac_unit", "dolor"),
        Seed("calor", "Tengo calor", "whatshot", "dolor"),
        Seed("mareado", "Estoy mareado", "monitor_heart", "dolor"),
        Seed("nauseas", "Tengo náuseas", "sick", "dolor"),
        Seed("remedio", "Remedio", "medication", "dolor"),
        Seed("inyeccion", "Inyección", "vaccines", "dolor"),
        Seed("curita", "Curita", "healing", "dolor"),

        // Partes del cuerpo
        Seed("cabeza", "Cabeza", "face", "cuerpo"),
        Seed("cara", "Cara", "face", "cuerpo"),
        Seed("ojos", "Ojos", "visibility", "cuerpo"),
        Seed("boca", "Boca", "mood", "cuerpo"),
        Seed("orejas", "Orejas", "hearing", "cuerpo"),
        Seed("dientes", "Dientes", "sentiment_very_satisfied", "cuerpo"),
        Seed("manos", "Manos", "pan_tool", "cuerpo"),
        Seed("brazos", "Brazos", "accessibility_new", "cuerpo"),
        Seed("piernas", "Piernas", "accessibility_new", "cuerpo"),
        Seed("pies", "Pies", "directions_walk", "cuerpo"),
        Seed("panza", "Panza", "accessibility_new", "cuerpo"),
        Seed("pelo", "Pelo", "face", "cuerpo"),

        // Ropa
        Seed("remera", "Remera", "checkroom", "ropa"),
        Seed("pantalon", "Pantalón", "checkroom", "ropa"),
        Seed("campera", "Campera", "checkroom", "ropa"),
        Seed("zapatos", "Zapatos", "checkroom", "ropa"),
        Seed("medias", "Medias", "checkroom", "ropa"),
        Seed("pijama", "Pijama", "bedtime", "ropa"),
        Seed("gorro", "Gorro", "checkroom", "ropa"),
        Seed("abrigo", "Abrigo", "checkroom", "ropa"),

        // Higiene
        Seed("pipi", "Pipí", "wc", "higiene"),
        Seed("caca", "Caca", "wc", "higiene"),
        Seed("lavar_manos", "Lavarme las manos", "clean_hands", "higiene"),
        Seed("cepillar_dientes", "Cepillarme los dientes", "wash", "higiene"),
        Seed("banarme", "Bañarme", "shower", "higiene"),
        Seed("peinarme", "Peinarme", "face", "higiene"),
        Seed("toalla", "Toalla", "bathroom", "higiene"),
        Seed("jabon", "Jabón", "clean_hands", "higiene"),

        // Tiempo
        Seed("hoy", "Hoy", "today", "tiempo"),
        Seed("manana", "Mañana", "wb_sunny", "tiempo"),
        Seed("ayer", "Ayer", "history", "tiempo"),
        Seed("ahora", "Ahora", "schedule", "tiempo"),
        Seed("despues", "Después", "schedule", "tiempo"),
        Seed("dia", "Día", "wb_sunny", "tiempo"),
        Seed("noche", "Noche", "bedtime", "tiempo"),
        Seed("lluvia", "Lluvia", "water_drop", "tiempo"),
        Seed("nublado", "Nublado", "cloud", "tiempo"),

        // Familia
        Seed("mama", "Mamá", "person", "familia"),
        Seed("papa", "Papá", "person", "familia"),
        Seed("hermano", "Hermano", "people", "familia"),
        Seed("hermana", "Hermana", "people", "familia"),
        Seed("abuela", "Abuela", "elderly", "familia"),
        Seed("abuelo", "Abuelo", "elderly", "familia"),
        Seed("tia", "Tía", "person", "familia"),
        Seed("tio", "Tío", "person", "familia"),
        Seed("bebe", "Bebé", "child_friendly", "familia"),

        // Juego
        Seed("pelota", "Pelota", "sports_soccer", "juego"),
        Seed("dibujar_j", "Dibujar", "draw", "juego"),
        Seed("pintar", "Pintar", "palette", "juego"),
        Seed("musica", "Música", "music_note", "juego"),
        Seed("cuento", "Cuento", "auto_stories", "juego"),
        Seed("juguetes", "Juguetes", "toys", "juego"),
        Seed("bloques", "Bloques", "toys", "juego"),
        Seed("muneca", "Muñeca", "toys", "juego"),
        Seed("tobogan", "Tobogán", "toys", "juego"),
        Seed("hamaca", "Hamaca", "toys", "juego"),

        // Lugares
        Seed("casa", "Casa", "home", "lugares"),
        Seed("bano_lugar", "Baño", "wc", "lugares"),
        Seed("cocina_lugar", "Cocina", "restaurant", "lugares"),
        Seed("escuela_lugar", "Escuela", "school", "lugares"),
        Seed("plaza", "Plaza", "park", "lugares"),
        Seed("hospital_lugar", "Hospital", "local_hospital", "lugares"),
        Seed("tienda_lugar", "Tienda", "shopping_cart", "lugares"),
        Seed("auto", "Auto", "directions_car", "lugares"),
        Seed("afuera", "Afuera", "logout", "lugares"),
        Seed("adentro", "Adentro", "login", "lugares"),

        // Escuela
        Seed("maestra", "Maestra", "school", "escuela"),
        Seed("companero", "Compañero", "people", "escuela"),
        Seed("silla", "Silla", "event_seat", "escuela"),
        Seed("mesa", "Mesa", "table_restaurant", "escuela"),
        Seed("libro", "Libro", "menu_book", "escuela"),
        Seed("cuaderno", "Cuaderno", "menu_book", "escuela"),
        Seed("lapiz", "Lápiz", "edit", "escuela"),
        Seed("mochila", "Mochila", "backpack", "escuela"),
        Seed("recreo", "Recreo", "sports_soccer", "escuela"),
        Seed("escribir", "Escribir", "edit", "escuela"),
        Seed("leer", "Leer", "menu_book", "escuela"),

        // Compras
        Seed("comprar", "Comprar", "shopping_cart", "compras"),
        Seed("pagar", "Pagar", "payments", "compras"),
        Seed("carrito", "Carrito", "shopping_cart", "compras"),
        Seed("bolsa", "Bolsa", "shopping_bag", "compras"),
        Seed("canasta", "Canasta", "shopping_basket", "compras"),
        Seed("plata", "Plata", "paid", "compras"),

        // Social
        Seed("hola_s", "Hola", "waving_hand", "social"),
        Seed("chau", "Chau", "waving_hand", "social"),
        Seed("gracias_s", "Gracias", "favorite", "social"),
        Seed("por_favor", "Por favor", "volunteer_activism", "social"),
        Seed("perdon", "Perdón", "sentiment_dissatisfied", "social"),
        Seed("buenos_dias", "Buenos días", "wb_sunny", "social"),
        Seed("buenas_noches", "Buenas noches", "bedtime", "social"),
        Seed("como_estas", "¿Cómo estás?", "mood", "social"),
        Seed("bien", "Bien", "sentiment_satisfied", "social"),
        Seed("mal", "Mal", "sentiment_dissatisfied", "social")
    )

    val pictograms: List<Pictogram> = seeds.map { s ->
        Pictogram(
            id = s.id,
            text = s.text,
            icon = s.icon,
            categoryId = s.category,
            placeIds = places.filter { it.categoryIds.contains(s.category) }.map { it.id }
        )
    }

    /* --------------------------------------------------------------------- */
    /*  Búsquedas                                                            */
    /* --------------------------------------------------------------------- */

    fun placeById(id: String): Place? = places.firstOrNull { it.id == id }

    fun categoryById(id: String): Category? = categories.firstOrNull { it.id == id }

    /** Categorías disponibles en un lugar, en el orden definido en [categories]. */
    fun categoriesForPlace(placeId: String): List<Category> {
        val ids = placeById(placeId)?.categoryIds.orEmpty()
        return categories.filter { it.id in ids }
    }

    /** Pictogramas de una categoría concreta dentro de un lugar. */
    fun pictogramsFor(placeId: String, categoryId: String): List<Pictogram> =
        pictograms.filter { it.categoryId == categoryId && placeId in it.placeIds }

    private fun core(id: String, text: String, icon: String) = Pictogram(
        id = "core_$id",
        text = text,
        icon = icon,
        categoryId = CORE_CATEGORY_ID,
        placeIds = emptyList()
    )
}
