package com.tunombre.agustispeaks.avatar

/** Una opción seleccionable: etiqueta en español + clave interna. */
data class AvatarOption(val label: String, val value: String)

/** Un color: etiqueta + hex SIN '#'. */
data class AvatarColor(val label: String, val hex: String)

/**
 * Catálogos de opciones del editor de avatar. Los `value` los interpreta
 * [AvatarView].
 */
object AvatarOptions {

    val skinColors = listOf(
        AvatarColor("Claro", "ffe0bd"),
        AvatarColor("Medio claro", "f1c27d"),
        AvatarColor("Medio", "e0ac69"),
        AvatarColor("Moreno", "c68642"),
        AvatarColor("Oscuro", "8d5524")
    )

    val hairStyles = listOf(
        AvatarOption("Clásico", "classic"),
        AvatarOption("Mohicano", "mohawk"),
        AvatarOption("Lado", "side"),
        AvatarOption("Rapado", "buzz"),
        AvatarOption("Puntas", "spiky"),
        AvatarOption("Largo", "long"),
        AvatarOption("Rulos", "curly"),
        AvatarOption("Colita", "bun")
    )

    val hairColors = listOf(
        AvatarColor("Negro", "2c1b18"),
        AvatarColor("Castaño oscuro", "4a312c"),
        AvatarColor("Castaño", "7a4a32"),
        AvatarColor("Rubio", "d6b370"),
        AvatarColor("Rojo", "b55239"),
        AvatarColor("Rosa", "f59bc4"),
        AvatarColor("Violeta", "9b7ede"),
        AvatarColor("Celeste", "63c7e8"),
        AvatarColor("Blanco", "e8e1d9")
    )

    val eyebrows = listOf(
        AvatarOption("Normal", "normal"),
        AvatarOption("Levantadas", "raised"),
        AvatarOption("Enojado", "angry"),
        AvatarOption("Preocupado", "worried")
    )

    val eyes = listOf(
        AvatarOption("Normal", "normal"),
        AvatarOption("Grandes", "wide"),
        AvatarOption("Felices", "happy"),
        AvatarOption("Dormido", "sleepy"),
        AvatarOption("Sorpresa", "surprised")
    )

    val mouths = listOf(
        AvatarOption("Sonrisa", "smile"),
        AvatarOption("Risa", "grin"),
        AvatarOption("Neutral", "neutral"),
        AvatarOption("Sorpresa", "open"),
        AvatarOption("Triste", "sad"),
        AvatarOption("Pícaro", "smirk")
    )

    val clothing = listOf(
        AvatarOption("Suéter", "sweater"),
        AvatarOption("Camiseta", "tshirt"),
        AvatarOption("Camisa", "shirt")
    )

    val clothingColors = listOf(
        AvatarColor("Violeta", "6c5ce7"),
        AvatarColor("Celeste", "74c0fc"),
        AvatarColor("Rosa", "ff8fa3"),
        AvatarColor("Amarillo", "ffd43b"),
        AvatarColor("Lavanda", "d0bfff"),
        AvatarColor("Menta", "63d9a5"),
        AvatarColor("Rojo", "e35d6a"),
        AvatarColor("Negro", "3a3a44"),
        AvatarColor("Blanco", "eef1f4")
    )

    /** Color del pantalón. En el robot pinta las placas de las piernas. */
    val pantsColors = listOf(
        AvatarColor("Jean oscuro", "3f5d75"),
        AvatarColor("Jean claro", "6f93b0"),
        AvatarColor("Gris", "6b7280"),
        AvatarColor("Negro", "2f2f38"),
        AvatarColor("Beige", "c9b18b"),
        AvatarColor("Verde", "4b7a5a"),
        AvatarColor("Rojo", "b6474f"),
        AvatarColor("Violeta", "7a6bc4")
    )

    /** Color de los zapatos. En el robot pinta las botas. */
    val shoeColors = listOf(
        AvatarColor("Negro", "3a3a44"),
        AvatarColor("Blanco", "eef1f4"),
        AvatarColor("Rojo", "d94f4f"),
        AvatarColor("Azul", "3f6fbf"),
        AvatarColor("Verde", "46a06b"),
        AvatarColor("Amarillo", "e8b53c"),
        AvatarColor("Marrón", "7a5230"),
        AvatarColor("Rosa", "e78ab0")
    )

    /* ------------------------- Robot ------------------------- */

    val robotColors = listOf(
        AvatarColor("Violeta", "6c5ce7"),
        AvatarColor("Celeste", "29b6f6"),
        AvatarColor("Azul", "1e88e5"),
        AvatarColor("Verde", "43a047"),
        AvatarColor("Naranja", "fb8c00"),
        AvatarColor("Rojo", "e53935"),
        AvatarColor("Gris", "607d8b")
    )

    val robotEyes = listOf(
        AvatarOption("Redondos", "round"),
        AvatarOption("Cuadrados", "square"),
        AvatarOption("Línea", "line"),
        AvatarOption("Contentos", "happy"),
        AvatarOption("Visor", "visor")
    )

    val robotMouths = listOf(
        AvatarOption("Rejilla", "grille"),
        AvatarOption("Sonrisa", "smile"),
        AvatarOption("Línea", "line"),
        AvatarOption("Dientes", "teeth")
    )

    /** Forma del rostro (carátula) del robot: cambia toda la cara, no solo los rasgos. */
    val robotFaces = listOf(
        AvatarOption("Pantalla", "plate"),
        AvatarOption("Visor", "visor"),
        AvatarOption("Ojo de buey", "porthole"),
        AvatarOption("Retro", "retro"),
        AvatarOption("Casco", "helmet")
    )

    val robotAccessories = listOf(
        AvatarOption("Antena", "antenna"),
        AvatarOption("Dos antenas", "antennas"),
        AvatarOption("Orejeras", "headphones"),
        AvatarOption("Gafas", "goggles"),
        AvatarOption("Radar", "radar"),
        AvatarOption("Aro", "halo"),
        AvatarOption("Sin accesorio", "none")
    )
}
