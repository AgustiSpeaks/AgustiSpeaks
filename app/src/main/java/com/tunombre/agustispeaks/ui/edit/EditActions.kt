package com.tunombre.agustispeaks.ui.edit

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.tunombre.agustispeaks.R
import com.tunombre.agustispeaks.data.model.EditableKind
import com.tunombre.agustispeaks.ui.LocalAppViewModel

/**
 * Las acciones del formulario de algo que ya existe, además de guardar:
 *
 * - **Lo propio se borra**, con confirmación, porque se lleva sus fotos.
 * - **Lo de fábrica se oculta** —o se vuelve a mostrar— y, si se le cambió el
 *   nombre o la foto, puede **volver al original**. Nunca se borra: ocultarlo
 *   se deshace con un toque.
 *
 * Cada acción cierra el formulario con [onClose].
 *
 * @param deleteText qué se pierde al borrar, para la confirmación.
 */
@Composable
fun editActions(
    kind: EditableKind,
    id: String,
    name: String,
    isCustom: Boolean,
    isHidden: Boolean,
    isChanged: Boolean,
    deleteText: String,
    onDelete: () -> Unit,
    onClose: () -> Unit
): List<EditAction> {
    val appVm = LocalAppViewModel.current
    val delete = stringResource(R.string.custom_delete)
    val deleteTitle = stringResource(R.string.custom_delete_title, name)
    val hide = stringResource(R.string.edit_hide)
    val show = stringResource(R.string.edit_show)
    val restore = stringResource(R.string.edit_restore)

    if (isCustom) {
        return listOf(
            EditAction(
                label = delete,
                icon = Icons.Filled.Delete,
                onClick = {
                    onDelete()
                    onClose()
                },
                destructive = true,
                confirmTitle = deleteTitle,
                confirmText = deleteText
            )
        )
    }
    return buildList {
        if (isHidden) {
            add(EditAction(show, Icons.Filled.Visibility, {
                appVm.setHidden(kind, id, false)
                onClose()
            }))
        } else {
            add(EditAction(hide, Icons.Filled.VisibilityOff, {
                appVm.setHidden(kind, id, true)
                onClose()
            }))
        }
        if (isChanged) {
            add(EditAction(restore, Icons.Filled.Restore, {
                appVm.restoreOriginal(kind, id)
                onClose()
            }))
        }
    }
}
