package com.tunombre.agustispeaks.ui.settings

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.InvertColors
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tunombre.agustispeaks.R
import com.tunombre.agustispeaks.data.model.TextSizeOption
import com.tunombre.agustispeaks.ui.LocalAppViewModel
import com.tunombre.agustispeaks.ui.edit.ChangePasswordDialog
import com.tunombre.agustispeaks.ui.edit.EnterPasswordDialog

@Composable
fun ConfiguracionesTab(modifier: Modifier = Modifier) {
    val vm = LocalAppViewModel.current
    val settings by vm.userSettings.collectAsStateWithLifecycle()
    val editing by vm.editMode.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var askingPassword by remember { mutableStateOf(false) }
    var changingPassword by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Primero: es lo que busca el adulto cuando entra a Ajustes para
        // personalizar el tablero.
        SectionHeader("Modo edición")
        SettingsCard {
            if (editing) {
                SettingRow(
                    icon = Icons.Filled.Edit,
                    title = "Modo edición activo",
                    subtitle = "En los lugares y en el tablero, cada elemento tiene un lápiz para cambiarlo, " +
                        "y cada lista termina en «Agregar». Se apaga al salir o al cerrar la app.",
                    trailing = {
                        OutlinedButton(onClick = vm::exitEditMode) { Text("Salir") }
                    }
                )
            } else {
                SettingRow(
                    icon = Icons.Filled.Lock,
                    title = "Entrar al modo edición",
                    subtitle = "Para agregar o cambiar lugares, categorías y pictogramas. Pide la contraseña.",
                    onClick = { askingPassword = true }
                )
            }
            SettingRow(
                icon = Icons.Filled.Password,
                title = "Cambiar contraseña",
                subtitle = "Pide la contraseña actual",
                onClick = { changingPassword = true }
            )
        }

        SectionHeader("Primeros pasos")
        SettingsCard {
            SettingRow(
                icon = Icons.AutoMirrored.Filled.MenuBook,
                title = "Cómo usar AgustiSpeaks (Tutorial)",
                subtitle = "Próximamente",
                onClick = { /* TODO: pantalla de tutorial */ }
            )
        }

        SectionHeader("Idiomas")
        SettingsCard {
            SettingRow(
                icon = Icons.Filled.Language,
                title = "Idioma de la interfaz",
                subtitle = "Español",
                trailing = { Text("🇪🇸", style = MaterialTheme.typography.titleLarge) }
            )
        }

        SectionHeader("Acceso táctil")
        SettingsCard {
            SettingSwitchRow(
                icon = Icons.Filled.PanTool,
                title = "Ignorar toques repetidos",
                subtitle = "Evita activaciones dobles muy seguidas",
                checked = settings.ignoreRepeatedTaps,
                onCheckedChange = vm::setIgnoreRepeatedTaps
            )
            SettingSwitchRow(
                icon = Icons.Filled.TouchApp,
                title = "Activar al soltar",
                subtitle = "Si se desactiva, el pictograma responde al tocar",
                checked = settings.activateOnRelease,
                onCheckedChange = vm::setActivateOnRelease
            )
        }

        SectionHeader("Apariencia")
        SettingsCard {
            SettingSwitchRow(
                icon = Icons.Filled.DarkMode,
                title = "Modo oscuro",
                checked = settings.darkMode,
                onCheckedChange = vm::setDarkMode
            )
            SettingSwitchRow(
                icon = Icons.Filled.InvertColors,
                title = "Alto contraste",
                checked = settings.highContrast,
                onCheckedChange = vm::setHighContrast
            )
            SettingRow(
                icon = Icons.Filled.FormatSize,
                title = "Tamaño de pictogramas",
                subtitle = settings.textSize.label
            )
            PictogramSizeSelector(
                selected = settings.textSize,
                onSelect = vm::setTextSize,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }
    }

    if (askingPassword) {
        EnterPasswordDialog(
            onDismiss = { askingPassword = false },
            check = { password, done ->
                vm.enterEditMode(password) { ok ->
                    done(ok)
                    if (ok) askingPassword = false
                }
            }
        )
    }

    if (changingPassword) {
        ChangePasswordDialog(
            onDismiss = { changingPassword = false },
            change = { current, new, done ->
                vm.changePassword(current, new) { ok ->
                    done(ok)
                    if (ok) {
                        changingPassword = false
                        Toast.makeText(context, context.getString(R.string.password_changed), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
    }
}

@Composable
private fun PictogramSizeSelector(
    selected: TextSizeOption,
    onSelect: (TextSizeOption) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TextSizeOption.entries.forEach { option ->
            val isSelected = option == selected
            OutlinedButton(
                onClick = { onSelect(option) },
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    horizontal = 14.dp,
                    vertical = 8.dp
                )
            ) {
                Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                    Text(
                        "Aa",
                        fontWeight = FontWeight.Bold,
                        fontSize = MaterialTheme.typography.bodyLarge.fontSize * option.scale,
                        color = if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        option.label,
                        style = MaterialTheme.typography.labelLarge,
                        color = if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
