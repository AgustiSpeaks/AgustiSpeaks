package com.tunombre.agustispeaks.ui.settings

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
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.InvertColors
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tunombre.agustispeaks.data.model.TextSizeOption
import com.tunombre.agustispeaks.ui.LocalAppViewModel

@Composable
fun ConfiguracionesTab(modifier: Modifier = Modifier) {
    val vm = LocalAppViewModel.current
    val settings by vm.userSettings.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
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
