package com.tunombre.agustispeaks.data.model

/**
 * Una frase guardada con su animación, y opcionalmente una foto que muestra
 * exactamente qué se pide: no "un jugo", sino *ese* jugo.
 *
 * La identidad depende de si hay foto:
 *
 * - **Sin foto**, un pictogift es su texto. "Yo quiero" es siempre el mismo,
 *   por eso [id] sale de la frase y guardarlo dos veces no lo duplica.
 * - **Con foto**, cada uno es único (`foto:<UUID>`). Se creaban con el botón
 *   "Agregar foto" de la tarjeta de «Decir», que se quitó con el modo edición:
 *   ahora la foto la lleva el pictograma. Los que ya estaban en favoritos se
 *   leen y se muestran igual.
 *
 * @param photo nombre del archivo dentro de la carpeta de fotos de la app (ver
 *   [com.tunombre.agustispeaks.data.repository.UserPhotos]), o null.
 */
data class Pictogift(
    val id: String,
    val phrase: String,
    val photo: String? = null
) {
    companion object {
        /** El pictogift de una frase sin foto. Misma frase, mismo id. */
        fun fromText(phrase: String): Pictogift {
            val clean = phrase.trim()
            return Pictogift(id = "texto:" + clean.lowercase(), phrase = clean)
        }
    }
}
