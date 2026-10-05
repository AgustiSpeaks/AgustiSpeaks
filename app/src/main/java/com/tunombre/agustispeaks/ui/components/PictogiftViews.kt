package com.tunombre.agustispeaks.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import com.tunombre.agustispeaks.avatar.ActionResolver
import com.tunombre.agustispeaks.avatar.AvatarAction
import com.tunombre.agustispeaks.avatar.ResolvedAction
import com.tunombre.agustispeaks.avatar.catalog.AvatarCatalog
import com.tunombre.agustispeaks.data.model.Pictogift
import com.tunombre.agustispeaks.data.model.Pictogram
import com.tunombre.agustispeaks.data.repository.UserPhotos
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * La foto que muestra un pictogift: la suya si tiene una, y si no, la del
 * pictograma propio que nombre su frase.
 *
 * Se calcula cada vez a partir del texto, en vez de copiarse al guardar. Así
 * "Quiero Aaw" muestra la foto de Aaw en la tarjeta de "Decir", en Recientes y
 * en Favoritos por igual, y si después se borra el pictograma propio, la frase
 * vuelve sola a su animación de siempre.
 */
fun Pictogift.shownPhoto(customPictograms: List<Pictogram>): String? =
    photo ?: photoNamedIn(phrase, customPictograms)

/**
 * La foto del pictograma propio que nombra [phrase], o null si no nombra
 * ninguno.
 *
 * Se compara con la misma normalización que usa el resolvedor de acciones —sin
 * tildes ni mayúsculas, por palabra completa—, para que "oso" no aparezca
 * dentro de "osito". Si la frase nombra varios, gana **el último**: la frase se
 * arma de izquierda a derecha y lo que se está pidiendo suele ser lo último
 * que se tocó. Si dos terminan en el mismo lugar ("oso" y "mi oso"), gana el
 * más largo, que es el más específico.
 */
fun photoNamedIn(phrase: String, customPictograms: List<Pictogram>): String? {
    val text = ActionResolver.normalized(phrase)
    var best: Pictogram? = null
    var bestEnd = -1
    for (p in customPictograms) {
        if (p.photo == null) continue
        val word = ActionResolver.normalized(p.text)
        val start = text.lastIndexOf(word)
        if (start < 0) continue
        val end = start + word.length
        if (end > bestEnd || (end == bestEnd && p.text.length > (best?.text?.length ?: 0))) {
            best = p
            bestEnd = end
        }
    }
    return best?.photo
}

/**
 * La animación de un pictogift. Sin foto es la de su frase, como siempre. Con
 * foto, el avatar la señala: la foto ya dice qué se pide, y lo que falta es
 * el gesto que la vuelve un pedido.
 *
 * @param shownPhoto la que devuelve [shownPhoto]: puede venir del pictogift o
 *        de un pictograma propio, y en los dos casos el avatar la señala.
 */
fun animationFor(phrase: String, shownPhoto: String?): ResolvedAction =
    if (shownPhoto != null) ResolvedAction(AvatarAction.POINT) else AvatarCatalog.resolve(phrase)

private sealed interface PhotoState {
    data object Loading : PhotoState
    data class Ready(val image: ImageBitmap) : PhotoState
    data object Missing : PhotoState
}

/**
 * Una foto guardada en [UserPhotos] —de un pictogift o de un pictograma propio—,
 * recortada para llenar su espacio. Se lee fuera del hilo principal y achicada a
 * [maxSide], para que una grilla de miniaturas no cargue fotos enteras.
 *
 * Si el archivo no está, se muestra un ícono de imagen rota en vez de un hueco:
 * así se nota que falta algo, y lo demás sigue funcionando.
 */
@Composable
fun SavedPhoto(
    name: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    maxSide: Int = 1024
) {
    val context = LocalContext.current.applicationContext
    val state by produceState<PhotoState>(PhotoState.Loading, name, maxSide) {
        val bitmap = withContext(Dispatchers.IO) { UserPhotos.load(context, name, maxSide) }
        value = if (bitmap != null) PhotoState.Ready(bitmap.asImageBitmap()) else PhotoState.Missing
    }

    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        when (val s = state) {
            is PhotoState.Ready -> Image(
                bitmap = s.image,
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            PhotoState.Missing -> Icon(
                Icons.Filled.BrokenImage,
                contentDescription = "No se encontró la foto",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            PhotoState.Loading -> Unit
        }
    }
}
