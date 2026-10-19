package com.tunombre.agustispeaks.data.model

/** Lo que se puede agregar, cambiar u ocultar en modo edición. */
enum class EditableKind { PLACE, CATEGORY, PICTOGRAM }

/**
 * Un cambio sobre algo que trae la app de fábrica: otro nombre, otra foto, u
 * oculto. Lo de fábrica no se toca; el cambio se aplica encima al mostrarlo, y
 * "Volver al original" solo borra el cambio.
 *
 * @param name el nombre nuevo, o null si conserva el de fábrica.
 * @param photo la foto que reemplaza al ícono, o null si conserva el ícono.
 */
data class ItemOverride(
    val kind: EditableKind,
    val id: String,
    val name: String? = null,
    val photo: String? = null,
    val hidden: Boolean = false
) {
    /** Un cambio que no cambia nada: se borra en vez de guardarse. */
    val isEmpty: Boolean get() = name == null && photo == null && !hidden
}

/** Una categoría creada en modo edición. Aparece solo en [placeId], donde se creó. */
data class CustomCategory(
    val category: Category,
    val placeId: String
)

/**
 * Todo lo que se agregó o cambió en modo edición, salvo los pictogramas
 * propios, que tienen su almacén desde antes
 * ([com.tunombre.agustispeaks.data.repository.CustomPictogramJson]).
 *
 * @param places lugares creados en modo edición.
 * @param categories categorías creadas en modo edición.
 * @param overrides cambios sobre lugares, categorías y pictogramas de fábrica.
 */
data class Customization(
    val places: List<Place> = emptyList(),
    val categories: List<CustomCategory> = emptyList(),
    val overrides: List<ItemOverride> = emptyList()
) {
    fun override(kind: EditableKind, id: String): ItemOverride? =
        overrides.firstOrNull { it.kind == kind && it.id == id }

    /**
     * Aplica [change] al cambio guardado de ese elemento, o a uno vacío si no
     * tenía. Si el resultado no cambia nada, se quita: así "Volver al original"
     * y "Mostrar" no dejan registros vacíos acumulándose.
     */
    fun changeOverride(kind: EditableKind, id: String, change: (ItemOverride) -> ItemOverride): Customization {
        val next = change(override(kind, id) ?: ItemOverride(kind, id))
        val rest = overrides.filterNot { it.kind == kind && it.id == id }
        return copy(overrides = if (next.isEmpty) rest else rest + next)
    }

    /** Todas las fotos que usa: de lugares, categorías y cambios. */
    fun photos(): Set<String> =
        (places.mapNotNull { it.photo } +
            categories.mapNotNull { it.category.photo } +
            overrides.mapNotNull { it.photo }).toSet()
}
