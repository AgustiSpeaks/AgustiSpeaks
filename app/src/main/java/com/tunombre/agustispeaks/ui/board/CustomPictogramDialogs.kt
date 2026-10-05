package com.tunombre.agustispeaks.ui.board

import android.content.ActivityNotFoundException
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.tunombre.agustispeaks.R
import com.tunombre.agustispeaks.data.model.Category
import com.tunombre.agustispeaks.data.model.Pictogram
import com.tunombre.agustispeaks.data.repository.UserPhotos
import com.tunombre.agustispeaks.ui.LocalAppViewModel
import com.tunombre.agustispeaks.ui.components.PictogramButton
import com.tunombre.agustispeaks.ui.util.hexToColor

/** Largo máximo del nombre: tiene que entrar en dos renglones de una tarjeta. */
private const val MAX_NAME = 30

private val PREVIEW_SIDE = 160.dp

/**
 * Formulario para crear un pictograma propio en [category]: un nombre y una
 * foto, sin animación. La vista previa es el pictograma tal como va a quedar en
 * el tablero, con el color de su categoría; tocarla abre la galería.
 *
 * Los dos datos son obligatorios: sin nombre no hay qué decir en voz alta, y
 * sin foto el pictograma no muestra nada que lo distinga de los demás.
 *
 * **No es un `AlertDialog`.** Ese tiene ancho fijo y no se desplaza: en un
 * teléfono en horizontal aplastaba el campo del nombre y dejaba los botones de
 * foto recortados fuera de la vista. Este usa casi todo el ancho, pone los dos
 * botones de foto lado a lado para ahorrar alto, y si igual no entra, se
 * desplaza en vez de recortar.
 */
@Composable
fun AddPictogramDialog(
    category: Category,
    onDismiss: () -> Unit,
    onSave: (text: String, photo: String) -> Unit
) {
    val context = LocalContext.current
    val appVm = LocalAppViewModel.current

    var name by rememberSaveable { mutableStateOf("") }
    var photo by rememberSaveable { mutableStateOf<String?>(null) }
    var saving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<Int?>(null) }

    fun receive(uri: Uri) {
        saving = true
        error = null
        appVm.savePhoto(uri) { saved ->
            saving = false
            if (saved != null) photo = saved else error = R.string.photo_failed
        }
    }

    val pickPhoto = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) receive(uri)
    }
    val takePhoto = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        if (ok) receive(UserPhotos.cameraUri(context))
    }

    fun openGallery() {
        try {
            pickPhoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        } catch (e: ActivityNotFoundException) {
            error = R.string.photo_failed
        }
    }

    fun openCamera() {
        try {
            takePhoto.launch(UserPhotos.cameraUri(context))
        } catch (e: ActivityNotFoundException) {
            error = R.string.photo_no_camera
        }
    }

    val chosenPhoto = photo
    val canSave = name.isNotBlank() && chosenPhoto != null && !saving

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .widthIn(max = 860.dp)
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text(
                    text = stringResource(R.string.custom_title, category.name),
                    style = MaterialTheme.typography.headlineSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    if (chosenPhoto != null) {
                        // Tocar la vista previa cambia la foto.
                        PictogramButton(
                            pictogram = Pictogram(
                                id = "vista_previa",
                                text = name.ifBlank { stringResource(R.string.custom_name) },
                                icon = "",
                                categoryId = category.id,
                                placeIds = emptyList(),
                                photo = chosenPhoto
                            ),
                            containerColor = hexToColor(category.colorHex),
                            onClick = { if (!saving) openGallery() },
                            modifier = Modifier.size(PREVIEW_SIDE)
                        )
                    } else {
                        Surface(
                            onClick = { openGallery() },
                            enabled = !saving,
                            shape = MaterialTheme.shapes.large,
                            color = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline),
                            modifier = Modifier.size(PREVIEW_SIDE)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize().padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically)
                            ) {
                                if (saving) {
                                    CircularProgressIndicator()
                                } else {
                                    Icon(
                                        Icons.Filled.AddPhotoAlternate,
                                        contentDescription = null,
                                        modifier = Modifier.size(56.dp)
                                    )
                                    Text(
                                        text = stringResource(R.string.custom_no_photo),
                                        style = MaterialTheme.typography.titleMedium,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { if (it.length <= MAX_NAME) name = it },
                            label = { Text(stringResource(R.string.custom_name)) },
                            supportingText = { Text(stringResource(R.string.custom_name_hint)) },
                            textStyle = MaterialTheme.typography.titleLarge,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                capitalization = KeyboardCapitalization.Sentences,
                                imeAction = ImeAction.Done
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            PhotoButton(
                                icon = Icons.Filled.PhotoCamera,
                                label = stringResource(R.string.photo_camera),
                                enabled = !saving,
                                onClick = { openCamera() }
                            )
                            PhotoButton(
                                icon = Icons.Filled.PhotoLibrary,
                                label = stringResource(R.string.photo_gallery),
                                enabled = !saving,
                                onClick = { openGallery() }
                            )
                        }
                        error?.let {
                            Text(
                                text = stringResource(it),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End)
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.cancel), style = MaterialTheme.typography.titleMedium)
                    }
                    Button(
                        onClick = { if (chosenPhoto != null) onSave(name.trim(), chosenPhoto) },
                        enabled = canSave
                    ) {
                        Text(stringResource(R.string.save), style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    }
}

/** Botón grande de foto: ocupa la mitad del ancho, con ícono y texto legibles. */
@Composable
private fun RowScope.PhotoButton(
    icon: ImageVector,
    label: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    FilledTonalButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .weight(1f)
            .heightIn(min = 56.dp)
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(ButtonDefaults.IconSpacing))
        Text(label, style = MaterialTheme.typography.titleSmall, textAlign = TextAlign.Center)
    }
}

/**
 * Confirmación antes de borrar un pictograma propio. Se pide porque borrar se
 * hace con un toque largo, que un chico puede hacer sin querer, y porque la foto
 * se pierde con él.
 */
@Composable
fun DeletePictogramDialog(
    pictogram: Pictogram,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.custom_delete_title, pictogram.text)) },
        text = { Text(stringResource(R.string.custom_delete_text), style = MaterialTheme.typography.bodyLarge) },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Text(stringResource(R.string.custom_delete), style = MaterialTheme.typography.titleMedium)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel), style = MaterialTheme.typography.titleMedium)
            }
        }
    )
}
