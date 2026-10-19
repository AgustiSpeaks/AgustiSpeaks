package com.tunombre.agustispeaks.ui.edit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.tunombre.agustispeaks.R
import com.tunombre.agustispeaks.data.repository.EditPassword

/**
 * Lo primero que se ve al abrir la app por primera vez: crear la contraseña
 * del modo edición. No se puede saltear; mientras no exista, no hay modo
 * edición y nadie puede cambiar el vocabulario.
 *
 * Es para el adulto, por eso explica para qué sirve y avisa que todavía no se
 * puede recuperar.
 */
@Composable
fun CreatePasswordScreen(onCreate: (String) -> Unit) {
    var password by rememberSaveable { mutableStateOf("") }
    var repeat by rememberSaveable { mutableStateOf("") }
    // Los errores se muestran recién después del primer intento: avisar
    // "muy corta" mientras alguien todavía está escribiendo solo molesta.
    var tried by rememberSaveable { mutableStateOf(false) }
    var saving by remember { mutableStateOf(false) }

    val tooShort = !EditPassword.isValid(password)
    val mismatch = password != repeat

    fun submit() {
        tried = true
        if (!tooShort && !mismatch && !saving) {
            saving = true
            onCreate(password)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(modifier = Modifier.widthIn(max = 860.dp).fillMaxWidth()) {
            Row(
                modifier = Modifier.padding(28.dp),
                horizontalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Filled.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        stringResource(R.string.password_create_title),
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Text(
                        stringResource(R.string.password_create_text),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        stringResource(R.string.password_create_warning),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PasswordField(
                        value = password,
                        onValueChange = { password = it },
                        label = stringResource(R.string.password_label),
                        supportingText = if (tried && tooShort) {
                            stringResource(R.string.password_too_short, EditPassword.MIN_LENGTH)
                        } else {
                            stringResource(R.string.password_min, EditPassword.MIN_LENGTH)
                        },
                        isError = tried && tooShort
                    )
                    PasswordField(
                        value = repeat,
                        onValueChange = { repeat = it },
                        label = stringResource(R.string.password_repeat),
                        supportingText = if (tried && mismatch) stringResource(R.string.password_mismatch) else null,
                        isError = tried && mismatch,
                        imeAction = ImeAction.Done,
                        onDone = { submit() }
                    )
                    Button(
                        onClick = { submit() },
                        enabled = !saving,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
                    ) {
                        if (saving) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        } else {
                            Text(stringResource(R.string.password_save), style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Pide la contraseña para entrar al modo edición.
 *
 * @param check recibe lo escrito y una función a la que avisarle si era la
 *        correcta. Si lo era, quien abrió el diálogo lo cierra.
 */
@Composable
fun EnterPasswordDialog(
    onDismiss: () -> Unit,
    check: (password: String, done: (Boolean) -> Unit) -> Unit
) {
    var password by remember { mutableStateOf("") }
    var checking by remember { mutableStateOf(false) }
    var wrong by remember { mutableStateOf(false) }

    fun submit() {
        if (password.isEmpty() || checking) return
        checking = true
        check(password) { ok ->
            checking = false
            if (!ok) {
                wrong = true
                password = ""
            }
        }
    }

    FormDialog(
        title = stringResource(R.string.edit_mode_enter),
        onDismiss = onDismiss,
        confirmLabel = stringResource(R.string.edit_mode_enter_button),
        confirmEnabled = password.isNotEmpty() && !checking,
        onConfirm = { submit() },
        maxWidth = 640.dp
    ) {
        Text(stringResource(R.string.edit_mode_enter_hint), style = MaterialTheme.typography.bodyLarge)
        PasswordField(
            value = password,
            onValueChange = {
                password = it
                wrong = false
            },
            label = stringResource(R.string.password_label),
            supportingText = if (wrong) stringResource(R.string.password_wrong) else null,
            isError = wrong,
            imeAction = ImeAction.Done,
            onDone = { submit() }
        )
    }
}

/**
 * Cambia la contraseña: pide la actual, y la nueva dos veces.
 *
 * @param change recibe la actual y la nueva, y una función a la que avisarle si
 *        se cambió. false es que la actual no era esa.
 */
@Composable
fun ChangePasswordDialog(
    onDismiss: () -> Unit,
    change: (current: String, new: String, done: (Boolean) -> Unit) -> Unit
) {
    var current by remember { mutableStateOf("") }
    var new by remember { mutableStateOf("") }
    var repeat by remember { mutableStateOf("") }
    var tried by remember { mutableStateOf(false) }
    var working by remember { mutableStateOf(false) }
    var wrongCurrent by remember { mutableStateOf(false) }

    val tooShort = !EditPassword.isValid(new)
    val mismatch = new != repeat

    fun submit() {
        tried = true
        if (current.isEmpty() || tooShort || mismatch || working) return
        working = true
        change(current, new) { ok ->
            working = false
            if (!ok) {
                wrongCurrent = true
                current = ""
            }
        }
    }

    FormDialog(
        title = stringResource(R.string.password_change),
        onDismiss = onDismiss,
        confirmLabel = stringResource(R.string.save),
        confirmEnabled = current.isNotEmpty() && !working,
        onConfirm = { submit() },
        maxWidth = 640.dp
    ) {
        PasswordField(
            value = current,
            onValueChange = {
                current = it
                wrongCurrent = false
            },
            label = stringResource(R.string.password_current),
            supportingText = if (wrongCurrent) stringResource(R.string.password_wrong) else null,
            isError = wrongCurrent
        )
        PasswordField(
            value = new,
            onValueChange = { new = it },
            label = stringResource(R.string.password_new),
            supportingText = if (tried && tooShort) {
                stringResource(R.string.password_too_short, EditPassword.MIN_LENGTH)
            } else {
                stringResource(R.string.password_min, EditPassword.MIN_LENGTH)
            },
            isError = tried && tooShort
        )
        PasswordField(
            value = repeat,
            onValueChange = { repeat = it },
            label = stringResource(R.string.password_repeat),
            supportingText = if (tried && mismatch) stringResource(R.string.password_mismatch) else null,
            isError = tried && mismatch,
            imeAction = ImeAction.Done,
            onDone = { submit() }
        )
    }
}

/**
 * Campo de contraseña, con un ojo para verla mientras se escribe: en una
 * pantalla táctil es fácil equivocarse de tecla sin darse cuenta.
 */
@Composable
fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    isError: Boolean = false,
    imeAction: ImeAction = ImeAction.Next,
    onDone: () -> Unit = {}
) {
    var visible by remember { mutableStateOf(false) }
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        textStyle = MaterialTheme.typography.titleLarge,
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = imeAction),
        keyboardActions = KeyboardActions(onDone = { onDone() }),
        trailingIcon = {
            IconButton(onClick = { visible = !visible }) {
                Icon(
                    imageVector = if (visible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                    contentDescription = stringResource(
                        if (visible) R.string.password_hide else R.string.password_show
                    )
                )
            }
        },
        supportingText = if (supportingText != null) {
            { Text(supportingText) }
        } else {
            null
        },
        isError = isError,
        modifier = modifier.fillMaxWidth()
    )
}
