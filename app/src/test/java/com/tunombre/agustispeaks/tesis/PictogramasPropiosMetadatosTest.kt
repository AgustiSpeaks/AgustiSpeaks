package com.tunombre.agustispeaks.tesis

import com.tunombre.agustispeaks.data.model.Pictogram
import com.tunombre.agustispeaks.data.repository.CustomPictogramJson
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pictogramas propios: guardar, leer y borrar sus **metadatos**.
 *
 * Complementa los criterios 13 y 15, que son manuales y se verifican en el
 * teléfono. Esta prueba no los reemplaza: comprueba en la JVM la parte que se
 * puede comprobar sin dispositivo, que es el formato con el que se guardan.
 * Ejercita [CustomPictogramJson], las mismas funciones que usa
 * `SettingsRepository` para escribir en DataStore.
 *
 * ### Qué no cubre, y qué haría falta para cubrirlo
 *
 * - **Que DataStore persista en disco** y sobreviva al cierre del proceso.
 * - **Que el archivo de la foto se cree y se borre** (`UserPhotos`).
 *
 * Las dos cosas necesitan un `Context` de Android real. Se podrían probar con
 * una prueba instrumentada en `app/src/androidTest/`, que hoy no existe. Haría
 * falta agregar las dependencias de prueba instrumentada al proyecto
 * (`androidx.test:runner` y `androidx.test.ext:junit`) y correrla con el
 * teléfono conectado. Mientras tanto, las cubre el protocolo manual de los
 * criterios 13 y 15.
 *
 * No tiene umbral propio en la bitácora: cada comprobación pasa o falla, y la
 * prueba falla si falla alguna.
 */
class PictogramasPropiosMetadatosTest {

    private val ap = Apendice("pictogramas-propios-metadatos")

    private val resultados = mutableListOf<Pair<String, Boolean>>()

    private fun pictograma(id: String, categoria: String, texto: String, foto: String?) = Pictogram(
        id = id,
        text = texto,
        icon = "",
        categoryId = categoria,
        placeIds = emptyList(),
        isCustom = true,
        photo = foto
    )

    private val oso = pictograma("propio:1", "juego", "Oso", "a1.jpg")
    private val botella = pictograma("propio:2", "bebidas", "Mi botella", "b2.jpg")
    private val sinFoto = pictograma("propio:3", "escuela", "Pizarrón", null)

    @Test
    fun guardarLeerYBorrar() = try {
        medir()
    } finally {
        ap.guardar()
    }

    private fun medir() {
        ap.encabezado("PICTOGRAMAS PROPIOS — METADATOS (complementa los criterios 13 y 15)", "PictogramasPropiosMetadatosTest")
        linea("Formato: arreglo JSON, un objeto {id, categoria, texto, foto} por pictograma.")
        linea("")

        // Guardar y leer
        val uno = CustomPictogramJson.add(null, oso)
        comprobar("Guardar el primero sobre un almacén vacío", CustomPictogramJson.parse(uno) == listOf(oso))

        val dos = CustomPictogramJson.add(uno, botella)
        comprobar(
            "Guardar un segundo conserva el orden de creación",
            CustomPictogramJson.parse(dos) == listOf(oso, botella)
        )

        val tres = CustomPictogramJson.add(dos, sinFoto)
        val leidos = CustomPictogramJson.parse(tres)
        comprobar("Leer devuelve los tres, iguales a los guardados", leidos == listOf(oso, botella, sinFoto))
        comprobar("Todos se leen marcados como propios", leidos.all { it.isCustom })
        comprobar("Sin foto se lee como sin foto, no como \"null\"", leidos.last().photo == null)

        val acentos = pictograma("propio:4", "juego", "Ñandú pequeño", "c3.jpg")
        comprobar(
            "Tildes y eñes sobreviven a guardar y leer",
            CustomPictogramJson.parse(CustomPictogramJson.add(null, acentos)) == listOf(acentos)
        )

        comprobar(
            "Uno con nombre en blanco no se guarda",
            CustomPictogramJson.parse(CustomPictogramJson.add(uno, pictograma("propio:5", "juego", "  ", "d4.jpg"))) ==
                listOf(oso)
        )

        // Borrar
        val sinBotella = CustomPictogramJson.remove(tres, botella.id)
        comprobar("Borrar quita solo ese", CustomPictogramJson.parse(sinBotella) == listOf(oso, sinFoto))
        comprobar(
            "Borrar uno que no existe no cambia nada",
            CustomPictogramJson.parse(CustomPictogramJson.remove(tres, "propio:no-existe")) == leidos
        )
        val vacio = CustomPictogramJson.remove(CustomPictogramJson.remove(sinBotella, oso.id), sinFoto.id)
        comprobar("Borrar todos deja el almacén vacío", CustomPictogramJson.parse(vacio).isEmpty())

        // Tolerancia: un dato roto no puede tirar abajo la app
        comprobar("Un almacén nulo se lee vacío", CustomPictogramJson.parse(null).isEmpty())
        comprobar("Un JSON roto se lee vacío, sin excepción", CustomPictogramJson.parse("{esto no es json").isEmpty())
        comprobar(
            "Un elemento incompleto se saltea sin llevarse a los demás",
            CustomPictogramJson.parse(
                """[{"id":"propio:1","categoria":"juego","texto":"Oso","foto":"a1.jpg"},""" +
                    """{"id":"propio:x","texto":"Sin categoría"},"no soy un objeto"]"""
            ) == listOf(oso)
        )

        val fallas = resultados.count { !it.second }
        linea("")
        dato("Comprobaciones", resultados.size)
        dato("Fallas", fallas)
        assertTrue("$fallas comprobaciones fallaron. Ver la salida.", fallas == 0)
    }

    private fun comprobar(que: String, ok: Boolean) {
        resultados += que to ok
        linea("  [" + (if (ok) "OK   " else "FALLA") + "] " + que)
    }

    private fun linea(s: String) = ap.linea(s)

    private fun dato(etiqueta: String, valor: Any) =
        linea("  " + etiqueta.padEnd(40, '.') + " " + valor.toString().padStart(10))
}
