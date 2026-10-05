package com.tunombre.agustispeaks.ui.components

import android.content.ActivityNotFoundException
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tunombre.agustispeaks.R
import com.tunombre.agustispeaks.avatar.AnimatedAvatar
import com.tunombre.agustispeaks.avatar.ResolvedAction
import com.tunombre.agustispeaks.data.model.AvatarConfig
import com.tunombre.agustispeaks.data.model.Pictogift
import com.tunombre.agustispeaks.data.repository.UserPhotos
import com.tunombre.agustispeaks.ui.LocalAppViewModel
import com.tunombre.agustispeaks.ui.LocalAvatarConfig

/* Medidas de la tarjeta con foto, a escala 1. */
private val AVATAR_WIDTH = 220.dp
private val AVATAR_HEIGHT = 232.dp
private val PHOTO_SIDE = 200.dp

/**
 * Dónde empieza la foto, como fracción del ancho del avatar. El dedo que señala
 * llega a un 0,70 y el símbolo de "señalar" junto a la cabeza a un 0,73: a 0,78
 * la foto queda justo delante del dedo sin que el símbolo le tape la esquina.
 */
private const val PHOTO_START = 0.78f

private val TEXT_COLUMN_WIDTH = 240.dp

/**
 * Overlay que aparece al tocar "Decir": muestra el avatar animado haciendo la
 * acción de la frase, con los botones para guardarla en favoritos y para
 * agregarle una foto.
 *
 * **Con foto**, el avatar se corre a la izquierda y la señala. **Agregar una
 * foto siempre crea un pictogift nuevo**: si la frase ya tenía otro con foto,
 * ese queda como estaba, y la tarjeta pasa a mostrar el nuevo.
 *
 * Tenía además un botón para exportar la animación como GIF y compartirla. Se
 * quitó: fallaba al abrir el selector de aplicaciones, y compartir un GIF no es
 * algo que la app necesite para comunicar. Con él se fueron el codificador
 * GIF89a, el exportador y el `FileProvider` de entonces; viven en el historial
 * de git si alguna vez hacen falta.
 */
@Composable
fun SpeakingOverlay(
    gift: Pictogift,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val avatarConfig = LocalAvatarConfig.current
    val appVm = LocalAppViewModel.current
    val favorites by appVm.favorites.collectAsStateWithLifecycle()
    val customPictograms by appVm.customPictograms.collectAsStateWithLifecycle()

    // El que se muestra: empieza siendo [gift] y pasa a ser el nuevo cuando se
    // le agrega una foto.
    var current by remember(gift.id) { mutableStateOf(gift) }
    // La suya, o la de un pictograma propio que nombre la frase.
    val shownPhoto = current.shownPhoto(customPictograms)
    val resolved = remember(current.phrase, shownPhoto) { animationFor(current.phrase, shownPhoto) }
    val isFavorite = favorites.any { it.id == current.id }

    var menuOpen by remember { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<Int?>(null) }

    fun receive(uri: Uri) {
        saving = true
        message = null
        appVm.addPhoto(current.phrase, uri) { created ->
            saving = false
            if (created != null) {
                current = created
                message = R.string.photo_saved
            } else {
                message = R.string.photo_failed
            }
        }
    }

    val pickPhoto = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) receive(uri)
    }
    val takePhoto = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        if (ok) receive(UserPhotos.cameraUri(context))
    }

    Dialog(
        onDismissRequest = onDismiss,
        // Sin el ancho por defecto del sistema el diálogo puede usar todo el
        // espacio que necesita en horizontal.
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card {
            BoxWithConstraints {
                val photo = shownPhoto
                // La versión con foto es más ancha. En una pantalla angosta se
                // achica el avatar con su foto, y no la columna de botones.
                val photoBoxWidth = AVATAR_WIDTH * PHOTO_START + PHOTO_SIDE + 16.dp
                val scale = ((maxWidth - TEXT_COLUMN_WIDTH - 84.dp) / photoBoxWidth).coerceIn(0.6f, 1f)

                // En horizontal el avatar va a la izquierda, grande, y la frase con
                // los botones a la derecha: así el pictograma animado se ve lo más
                // nítido posible sin que el diálogo se salga de la pantalla.
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    if (photo == null) {
                        Surface(
                            shape = RoundedCornerShape(24.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.width(250.dp).height(262.dp)
                        ) {
                            AnimatedAvatar(
                                config = avatarConfig,
                                resolved = resolved,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(24.dp))
                                    .padding(8.dp)
                            )
                        }
                    } else {
                        AvatarPointingAtPhoto(
                            config = avatarConfig,
                            resolved = resolved,
                            photo = photo,
                            phrase = current.phrase,
                            scale = scale
                        )
                    }

                    Column(
                        // 240 y no 220: con 220 el botón "Quitar de favoritos", con
                        // su estrella, quedaba justo al borde de partirse en dos líneas.
                        modifier = Modifier.width(TEXT_COLUMN_WIDTH),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = current.phrase,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        // Guardar desde acá es el momento natural: la frase acaba de
                        // funcionar y el pictogift está a la vista.
                        OutlinedButton(
                            onClick = { appVm.toggleFavorite(current) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                if (isFavorite) Icons.Filled.Star else Icons.Filled.StarBorder,
                                contentDescription = null,
                                modifier = Modifier.size(ButtonDefaults.IconSize)
                            )
                            Spacer(Modifier.width(ButtonDefaults.IconSpacing))
                            Text(
                                stringResource(
                                    if (isFavorite) R.string.favorite_remove else R.string.favorite_add
                                )
                            )
                        }

                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = { menuOpen = true },
                                enabled = !saving,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                if (saving) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(ButtonDefaults.IconSize),
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Icon(
                                        Icons.Filled.AddAPhoto,
                                        contentDescription = null,
                                        modifier = Modifier.size(ButtonDefaults.IconSize)
                                    )
                                }
                                Spacer(Modifier.width(ButtonDefaults.IconSpacing))
                                Text(
                                    stringResource(
                                        when {
                                            saving -> R.string.photo_saving
                                            photo == null -> R.string.photo_add
                                            else -> R.string.photo_add_another
                                        }
                                    )
                                )
                            }
                            DropdownMenu(
                                expanded = menuOpen,
                                onDismissRequest = { menuOpen = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.photo_camera)) },
                                    leadingIcon = { Icon(Icons.Filled.PhotoCamera, contentDescription = null) },
                                    onClick = {
                                        menuOpen = false
                                        try {
                                            takePhoto.launch(UserPhotos.cameraUri(context))
                                        } catch (e: ActivityNotFoundException) {
                                            message = R.string.photo_no_camera
                                        }
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.photo_gallery)) },
                                    leadingIcon = { Icon(Icons.Filled.PhotoLibrary, contentDescription = null) },
                                    onClick = {
                                        menuOpen = false
                                        try {
                                            pickPhoto.launch(
                                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                            )
                                        } catch (e: ActivityNotFoundException) {
                                            message = R.string.photo_failed
                                        }
                                    }
                                )
                            }
                        }

                        message?.let {
                            Text(
                                text = stringResource(it),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }

                        Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                            Text("Listo")
                        }
                    }
                }
            }
        }
    }
}

/**
 * El avatar a la izquierda señalando la foto, en un mismo fondo para que se lean
 * como una sola imagen. El avatar se dibuja encima: si el dedo llega a la foto,
 * queda delante y no detrás.
 */
@Composable
private fun AvatarPointingAtPhoto(
    config: AvatarConfig,
    resolved: ResolvedAction,
    photo: String,
    phrase: String,
    scale: Float
) {
    val avatarWidth = AVATAR_WIDTH * scale
    val avatarHeight = AVATAR_HEIGHT * scale
    val photoSide = PHOTO_SIDE * scale
    val photoStart = avatarWidth * PHOTO_START

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier
            .width(photoStart + photoSide + 16.dp * scale)
            .height(avatarHeight)
    ) {
        Box {
            SavedPhoto(
                name = photo,
                contentDescription = stringResource(R.string.photo_description, phrase),
                modifier = Modifier
                    .offset(x = photoStart, y = (avatarHeight - photoSide) / 2)
                    .size(photoSide)
                    .clip(RoundedCornerShape(16.dp))
            )
            AnimatedAvatar(
                config = config,
                resolved = resolved,
                modifier = Modifier
                    .width(avatarWidth)
                    .height(avatarHeight)
                    .padding(8.dp * scale)
            )
        }
    }
}
