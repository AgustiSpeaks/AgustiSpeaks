package com.tunombre.agustispeaks.data.catalog

import com.tunombre.agustispeaks.data.model.Category
import com.tunombre.agustispeaks.data.model.Customization
import com.tunombre.agustispeaks.data.model.EditableKind
import com.tunombre.agustispeaks.data.model.Pictogram
import com.tunombre.agustispeaks.data.model.Place
import com.tunombre.agustispeaks.data.seed.SeedData

/**
 * El vocabulario que ve quien usa la app: lo de fábrica ([SeedData]) con los
 * cambios del modo edición encima, más lo creado en ese modo.
 *
 * [SeedData] no se modifica nunca. Las pruebas de la tesis lo leen tal cual, y
 * "Volver al original" funciona porque lo original sigue ahí.
 *
 * Cada consulta recibe `includeHidden`: en modo edición se muestran también
 * los elementos ocultos (atenuados, para poder volver a mostrarlos), y fuera de
 * él no aparecen.
 *
 * Es una clase pura, sin Android: se puede probar en la JVM.
 */
class Catalog(
    private val custom: Customization = Customization(),
    private val customPictograms: List<Pictogram> = emptyList()
) {

    /** Los lugares de fábrica, con sus cambios, y después los creados. */
    fun places(includeHidden: Boolean): List<Place> =
        (SeedData.places.map { applied(it) } + custom.places).visible(includeHidden) { it.isHidden }

    /** El lugar de [id], esté oculto o no. */
    fun place(id: String): Place? =
        SeedData.placeById(id)?.let { applied(it) } ?: custom.places.firstOrNull { it.id == id }

    /**
     * Las categorías de un lugar: las de fábrica que tiene asignadas, en el
     * orden de [SeedData.categories], y al final las creadas en ese lugar.
     */
    fun categoriesFor(placeId: String, includeHidden: Boolean): List<Category> {
        val place = place(placeId) ?: return emptyList()
        val base = SeedData.categories.filter { it.id in place.categoryIds }.map { applied(it) }
        val own = custom.categories.filter { it.placeId == placeId }.map { it.category }
        return (base + own).visible(includeHidden) { it.isHidden }
    }

    /** La categoría de [id], esté oculta o no. */
    fun category(id: String): Category? =
        SeedData.categoryById(id)?.let { applied(it) }
            ?: custom.categories.firstOrNull { it.category.id == id }?.category

    /**
     * Los pictogramas de una categoría: los de fábrica y después los propios,
     * en el orden en que se crearon.
     *
     * No hace falta el lugar. En [SeedData] un pictograma aparece en todo lugar
     * que muestre su categoría, así que filtrar por categoría da lo mismo que
     * `SeedData.pictogramsFor(lugar, categoría)` en cada lugar de fábrica, y
     * además sirve para los lugares creados.
     */
    fun pictogramsFor(categoryId: String, includeHidden: Boolean): List<Pictogram> {
        val seed = SeedData.pictograms.filter { it.categoryId == categoryId }.map { applied(it) }
        val own = customPictograms.filter { it.categoryId == categoryId }
        return (seed + own).visible(includeHidden) { it.isHidden }
    }

    /** Las palabras del panel izquierdo, con sus cambios. */
    fun coreWords(includeHidden: Boolean): List<Pictogram> =
        SeedData.coreWords.map { applied(it) }.visible(includeHidden) { it.isHidden }

    /**
     * Los pictogramas a la vista que tienen foto: los propios y los de fábrica
     * a los que se les puso una. Son los que el avatar puede señalar en la
     * tarjeta de «Decir» cuando la frase los nombra.
     */
    val pictogramsWithPhoto: List<Pictogram> by lazy {
        val changed = (SeedData.coreWords + SeedData.pictograms)
            .filter { custom.override(EditableKind.PICTOGRAM, it.id)?.photo != null }
            .map { applied(it) }
            .filterNot { it.isHidden }
        changed + customPictograms.filter { it.photo != null }
    }

    /** Todas las fotos en uso, de lo creado y de lo cambiado. */
    fun photos(): Set<String> = custom.photos() + customPictograms.mapNotNull { it.photo }

    /** El nombre de fábrica de un elemento, o null si no es de fábrica. */
    fun originalName(kind: EditableKind, id: String): String? = when (kind) {
        EditableKind.PLACE -> SeedData.placeById(id)?.name
        EditableKind.CATEGORY -> SeedData.categoryById(id)?.name
        EditableKind.PICTOGRAM -> (SeedData.coreWords + SeedData.pictograms).firstOrNull { it.id == id }?.text
    }

    /** Si el elemento de fábrica tiene algún cambio de nombre o de foto. */
    fun isChanged(kind: EditableKind, id: String): Boolean =
        custom.override(kind, id)?.let { it.name != null || it.photo != null } ?: false

    private fun applied(p: Place): Place {
        val o = custom.override(EditableKind.PLACE, p.id) ?: return p
        return p.copy(name = o.name ?: p.name, photo = o.photo ?: p.photo, isHidden = o.hidden)
    }

    private fun applied(c: Category): Category {
        val o = custom.override(EditableKind.CATEGORY, c.id) ?: return c
        return c.copy(name = o.name ?: c.name, photo = o.photo ?: c.photo, isHidden = o.hidden)
    }

    private fun applied(p: Pictogram): Pictogram {
        val o = custom.override(EditableKind.PICTOGRAM, p.id) ?: return p
        return p.copy(text = o.name ?: p.text, photo = o.photo ?: p.photo, isHidden = o.hidden)
    }

    private inline fun <T> List<T>.visible(includeHidden: Boolean, hidden: (T) -> Boolean): List<T> =
        if (includeHidden) this else filterNot(hidden)
}
