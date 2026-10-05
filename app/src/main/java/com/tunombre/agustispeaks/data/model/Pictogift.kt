package com.tunombre.agustispeaks.data.model

import java.util.UUID

/**
 * Una frase guardada con su animación, y opcionalmente una foto que muestra
 * exactamente qué se pide: no "un jugo", sino *ese* jugo.
 *
 * La identidad depende de si hay foto:
 *
 * - **Sin foto**, un pictogift es su texto. "Yo quiero" es siempre el mismo,
 *   por eso [id] sale de la frase y guardarlo dos veces no lo duplica.
 * - **Con foto**, cada uno es único. "Yo quiero" con la foto del jugo y "Yo
 *   quiero" con la foto de la pelota son dos pictogifts distintos, y agregar una
 *   foto nueva nunca pisa la de otro: crea uno más.
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

        /** Un pictogift nuevo con foto. Siempre tiene un id distinto. */
        fun withPhoto(phrase: String, photo: String): Pictogift =
            Pictogift(id = "foto:" + UUID.randomUUID(), phrase = phrase.trim(), photo = photo)
    }
}
