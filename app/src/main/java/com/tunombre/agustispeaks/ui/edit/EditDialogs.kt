package com.tunombre.agustispeaks.ui.edit

import android.content.ActivityNotFoundException
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.tunombre.agustispeaks.R
import com.tunombre.agustispeaks.data.model.Category
import com.tunombre.agustispeaks.data.model.Pictogram
import com.tunombre.agustispeaks.data.repository.UserPhotos
import com.tunombre.agustispeaks.ui.LocalAppViewModel
import com.tunombre.agustispeaks.ui.components.PictogramButton
import com.tunombre.agustispeaks.ui.settings.ColorSwatchRow
import com.tunombre.agustispeaks.ui.util.hexToColor

/** Largo máximo de un nombre: tiene que entrar en dos renglones de una tarjeta. */
private const val MAX_NAME = 30

private val PREVIEW_SIDE = 160.dp

/**
 * Colores para lo que se crea en modo edición: los mismos tonos pastel de las
 * categorías de fábrica, para que lo nuevo no desentone.
 */
val NEW_ITEM_COLORS = listOf(
    "#FFE0B2", "#B3E5FC", "#C8E6C9", "#F8BBD0", "#D1C4E9",
    "#FFF59D", "#B2DFDB", "#FFCDD2", "#BBDEFB", "#FFCC80"
)

/**
 * Una acción del formulario además de guardar: ocultar, mostrar, volver al
 * original o borrar.
 *
 * @param confirmText si no es null, se pide confirmar antes, con este texto.
 */
class EditAction(
    val label: String,
    val icon: ImageVector,
    val onClick: () -> Unit,
    val destructive: Boolean = false,
    val confirmTitle: String? = null,
    val confirmText: String? = null
)

/**
 * Diálogo de formulario ancho y desplazable.
 *
 * **No es un `AlertDialog`.** Ese tiene ancho fijo y no se desplaza: en un
 * teléfono en horizontal aplastaba los campos y dejaba botones recortados
 * fuera de la vista. Este usa casi todo el ancho y, si igual no entra, se
 * desplaza en vez de recortar.
 */
@Composable
fun FormDialog(
    title: String,
    onDismiss: () -> Unit,
    confirmLabel: String,
    confirmEnabled: Boolean,
    onConfirm: () -> Unit,
    maxWidth: Dp = 860.dp,
    content: @Composable ColumnScope.() -> Unit
) {
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
                .widthIn(max = maxWidth)
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                content()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End)
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.cancel), style = MaterialTheme.typography.titleMedium)
                    }
                    Button(onClick = onConfirm, enabled = confirmEnabled) {
                        Text(confirmLabel, style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    }
}

/**
 * Formulario para crear o cambiar un lugar, una categoría o un pictograma:
 * nombre y foto, y según el caso color, otras opciones ([extra]) y acciones
 * como ocultar o borrar ([actions]).
 *
 * La vista previa es la tarjeta tal como va a quedar, con su color; tocarla
 * abre la galería. Sin foto se ve el ícono de fábrica ([icon]); lo creado en
 * modo edición no tiene ícono, por eso ahí la foto es obligatoria
 * ([photoRequired]): sin ella no muestra nada que lo distinga.
 *
 * @param colors si no es null, se puede elegir el color entre estos.
 * @param onSave recibe el nombre, la foto (null si se queda con el ícono) y el
 *        color.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditItemDialog(
    title: String,
    initialName: String,
    initialPhoto: String?,
    icon: String,
    colorHex: String,
    photoRequired: Boolean,
    onDismiss: () -> Unit,
    onSave: (name: String, photo: String?, colorHex: String) -> Unit,
    nameHint: String? = null,
    colors: List<String>? = null,
    actions: List<EditAction> = emptyList(),
    note: String? = null,
    extra: (@Composable () -> Unit)? = null
) {
    val context = LocalContext.current
    val appVm = LocalAppViewModel.current

    var name by rememberSaveable { mutableStateOf(initialName) }
    var photo by rememberSaveable { mutableStateOf(initialPhoto) }
    var color by rememberSaveable { mutableStateOf(colorHex) }
    var saving by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<Int?>(null) }
    var confirming by remember { mutableStateOf<EditAction?>(null) }

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
    val canSave = name.isNotBlank() && (chosenPhoto != null || !photoRequired) && !saving

    FormDialog(
        title = title,
        onDismiss = onDismiss,
        confirmLabel = stringResource(R.string.save),
        confirmEnabled = canSave,
        onConfirm = { onSave(name.trim(), chosenPhoto, color) }
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.Top
        ) {
            if (chosenPhoto != null || icon.isNotBlank()) {
                // Tocar la vista previa cambia la foto.
                PictogramButton(
                    pictogram = Pictogram(
                        id = "vista_previa",
                        text = name.ifBlank { stringResource(R.string.custom_name) },
                        icon = icon,
                        categoryId = "",
                        placeIds = emptyList(),
                        photo = chosenPhoto
                    ),
                    containerColor = hexToColor(color),
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
                    supportingText = if (nameHint != null) {
                        { Text(nameHint) }
                    } else {
                        null
                    },
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

        if (colors != null) {
            ColorSwatchRow(
                title = stringResource(R.string.edit_color),
                colors = colors.map { it to hexToColor(it) },
                selectedHex = color,
                onSelect = { color = it }
            )
        }

        extra?.invoke()

        if (actions.isNotEmpty()) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                actions.forEach { action ->
                    OutlinedButton(
                        onClick = { if (action.confirmText != null) confirming = action else action.onClick() },
                        colors = if (action.destructive) {
                            ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        } else {
                            ButtonDefaults.outlinedButtonColors()
                        },
                        modifier = Modifier.heightIn(min = 48.dp)
                    ) {
                        Icon(action.icon, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                        Spacer(Modifier.width(ButtonDefaults.IconSpacing))
                        Text(action.label, style = MaterialTheme.typography.titleSmall)
                    }
                }
            }
        }

        note?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    confirming?.let { action ->
        AlertDialog(
            onDismissRequest = { confirming = null },
            title = { action.confirmTitle?.let { Text(it) } },
            text = { Text(action.confirmText.orEmpty(), style = MaterialTheme.typography.bodyLarge) },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirming = null
                        action.onClick()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = if (action.destructive) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(action.label, style = MaterialTheme.typography.titleMedium)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirming = null }) {
                    Text(stringResource(R.string.cancel), style = MaterialTheme.typography.titleMedium)
                }
            }
        )
    }
}

/**
 * Para elegir qué categorías de fábrica muestra un lugar creado. Cada una es
 * un chip que se marca o se desmarca.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoryChips(
    categories: List<Category>,
    selected: Set<String>,
    onToggle: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.place_categories).uppercase(),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            categories.forEach { category ->
                FilterChip(
                    selected = category.id in selected,
                    onClick = { onToggle(category.id) },
                    label = { Text(category.name, style = MaterialTheme.typography.titleSmall) },
                    modifier = Modifier.heightIn(min = 48.dp)
                )
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
