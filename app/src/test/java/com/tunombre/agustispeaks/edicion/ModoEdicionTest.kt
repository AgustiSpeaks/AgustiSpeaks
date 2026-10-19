package com.tunombre.agustispeaks.edicion

import com.tunombre.agustispeaks.data.catalog.Catalog
import com.tunombre.agustispeaks.data.model.Category
import com.tunombre.agustispeaks.data.model.CustomCategory
import com.tunombre.agustispeaks.data.model.Customization
import com.tunombre.agustispeaks.data.model.EditableKind
import com.tunombre.agustispeaks.data.model.Pictogram
import com.tunombre.agustispeaks.data.model.Place
import com.tunombre.agustispeaks.data.repository.CustomPictogramJson
import com.tunombre.agustispeaks.data.repository.CustomizationJson
import com.tunombre.agustispeaks.data.repository.EditPassword
import com.tunombre.agustispeaks.data.seed.SeedData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * La lógica del modo edición que no depende de Android: la contraseña, el
 * formato en que se guarda lo editado y cómo se combina con lo de fábrica.
 *
 * No es una prueba de la tesis: no escribe en `docs/tesis/salidas-2026-10/`.
 */
class ModoEdicionTest {

    /* -------- Contraseña -------- */

    @Test
    fun `la contraseña correcta se acepta y otra no`() {
        val stored = EditPassword.hash("girasol7")
        assertTrue(EditPassword.verify("girasol7", stored))
        assertFalse(EditPassword.verify("girasol8", stored))
        assertFalse(EditPassword.verify("", stored))
    }

    @Test
    fun `no se guarda la contraseña sino su huella, y con sal distinta cada vez`() {
        val a = EditPassword.hash("girasol7")
        val b = EditPassword.hash("girasol7")
        assertFalse(a.contains("girasol7"))
        assertNotEquals(a, b)
        assertTrue(EditPassword.verify("girasol7", b))
    }

    @Test
    fun `un valor guardado ilegible nunca coincide`() {
        assertFalse(EditPassword.verify("girasol7", null))
        assertFalse(EditPassword.verify("girasol7", ""))
        assertFalse(EditPassword.verify("girasol7", "girasol7"))
        assertFalse(EditPassword.verify("girasol7", "pbkdf2-sha256:no:es:base64"))
    }

    @Test
    fun `largo minimo`() {
        assertFalse(EditPassword.isValid("abc"))
        assertTrue(EditPassword.isValid("abcd"))
    }

    /* -------- Formato guardado -------- */

    private val lugar = Place(
        id = "lugar_1", name = "Casa de la abuela", icon = "", colorHex = "#FFE0B2",
        categoryIds = listOf("social", "lugares"), photo = "casa.jpg", isCustom = true
    )
    private val categoria = CustomCategory(
        Category(id = "categoria_1", name = "Mis juguetes", icon = "", colorHex = "#C8E6C9", photo = "jug.jpg", isCustom = true),
        placeId = "colegio"
    )

    @Test
    fun `lo editado se lee igual a como se guardo`() {
        val original = Customization(
            places = listOf(lugar),
            categories = listOf(categoria),
            overrides = emptyList()
        ).changeOverride(EditableKind.PICTOGRAM, "agua") { it.copy(name = "Agüita", photo = "agua.jpg") }
            .changeOverride(EditableKind.CATEGORY, "juego") { it.copy(hidden = true) }

        assertEquals(original, CustomizationJson.parse(CustomizationJson.write(original)))
    }

    @Test
    fun `un JSON roto o vacio se lee como nada editado`() {
        assertEquals(Customization(), CustomizationJson.parse(null))
        assertEquals(Customization(), CustomizationJson.parse("{esto no es json"))
    }

    @Test
    fun `un cambio que no cambia nada no se guarda`() {
        val oculto = Customization().changeOverride(EditableKind.PLACE, "colegio") { it.copy(hidden = true) }
        val visible = oculto.changeOverride(EditableKind.PLACE, "colegio") { it.copy(hidden = false) }
        assertEquals(1, oculto.overrides.size)
        assertTrue(visible.overrides.isEmpty())
    }

    @Test
    fun `editar un pictograma propio lo deja en su lugar de la lista`() {
        fun propio(id: String, texto: String) = Pictogram(
            id = id, text = texto, icon = "", categoryId = "juego", placeIds = emptyList(),
            isCustom = true, photo = "$id.jpg"
        )
        val lista = CustomPictogramJson.add(CustomPictogramJson.add(null, propio("propio:1", "Oso")), propio("propio:2", "Auto"))
        val editada = CustomPictogramJson.update(lista, propio("propio:1", "Osito"))
        assertEquals(listOf("Osito", "Auto"), CustomPictogramJson.parse(editada).map { it.text })
    }

    /* -------- Lo de fábrica con lo editado encima -------- */

    @Test
    fun `sin nada editado el catalogo es la semilla`() {
        val catalog = Catalog()
        assertEquals(SeedData.places, catalog.places(includeHidden = false))
        assertEquals(SeedData.categoriesForPlace("colegio"), catalog.categoriesFor("colegio", includeHidden = false))
        assertEquals(
            SeedData.pictogramsFor("colegio", "juego"),
            catalog.pictogramsFor("juego", includeHidden = false)
        )
    }

    @Test
    fun `un cambio de nombre y foto se ve, y lo de fabrica no cambia`() {
        val custom = Customization()
            .changeOverride(EditableKind.PICTOGRAM, "core_yo") { it.copy(name = "Agustín", photo = "agustin.jpg") }
        val yo = Catalog(custom).coreWords(includeHidden = false).first { it.id == "core_yo" }
        assertEquals("Agustín", yo.text)
        assertEquals("agustin.jpg", yo.photo)
        assertEquals("Yo", SeedData.coreWords.first { it.id == "core_yo" }.text)
        assertTrue(Catalog(custom).isChanged(EditableKind.PICTOGRAM, "core_yo"))
    }

    @Test
    fun `lo oculto solo se ve en modo edicion`() {
        val custom = Customization().changeOverride(EditableKind.CATEGORY, "juego") { it.copy(hidden = true) }
        val catalog = Catalog(custom)
        assertFalse(catalog.categoriesFor("colegio", includeHidden = false).any { it.id == "juego" })
        assertTrue(catalog.categoriesFor("colegio", includeHidden = true).first { it.id == "juego" }.isHidden)
    }

    @Test
    fun `lo creado aparece donde corresponde`() {
        val propio = Pictogram(
            id = "propio:1", text = "Trompo", icon = "", categoryId = "categoria_1",
            placeIds = emptyList(), isCustom = true, photo = "trompo.jpg"
        )
        val catalog = Catalog(Customization(places = listOf(lugar), categories = listOf(categoria)), listOf(propio))

        assertEquals(lugar, catalog.places(includeHidden = false).last())
        // La categoría creada en Colegio, al final de Colegio y en ningún otro lugar.
        assertEquals("categoria_1", catalog.categoriesFor("colegio", includeHidden = false).last().id)
        assertFalse(catalog.categoriesFor("living", includeHidden = false).any { it.id == "categoria_1" })
        // El lugar creado muestra las categorías de fábrica que se le eligieron.
        assertEquals(listOf("social", "lugares"), catalog.categoriesFor("lugar_1", includeHidden = false).map { it.id })
        assertEquals(listOf(propio), catalog.pictogramsFor("categoria_1", includeHidden = false))
        assertEquals(setOf("casa.jpg", "jug.jpg", "trompo.jpg"), catalog.photos())
    }

    @Test
    fun `el avatar puede señalar la foto de un pictograma de fabrica`() {
        val custom = Customization()
            .changeOverride(EditableKind.PICTOGRAM, "agua") { it.copy(photo = "agua.jpg") }
        val conFoto = Catalog(custom).pictogramsWithPhoto
        assertEquals(listOf("agua"), conFoto.map { it.id })

        val oculto = custom.changeOverride(EditableKind.PICTOGRAM, "agua") { it.copy(hidden = true) }
        assertTrue(Catalog(oculto).pictogramsWithPhoto.isEmpty())
        assertNull(Catalog().originalName(EditableKind.PICTOGRAM, "propio:1"))
    }
}
