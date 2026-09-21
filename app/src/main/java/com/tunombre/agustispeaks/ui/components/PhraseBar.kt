package com.tunombre.agustispeaks.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.tunombre.agustispeaks.R
import com.tunombre.agustispeaks.avatar.AvatarView
import com.tunombre.agustispeaks.ui.LocalAvatarConfig

/**
 * Barra tipo chat donde se construye la frase.
 *
 * Estructura visual fija de "quién habla": avatar/etiqueta "Tú" (genérico, no
 * editable en esta etapa) + texto de la frase + botón repetir (TTS) + botón
 * borrar.
 */
@Composable
fun PhraseBar(
    phrase: String,
    onSay: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 3.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SpeakerAvatar()

            Box(modifier = Modifier.weight(1f)) {
                if (phrase.isBlank()) {
                    Text(
                        text = stringResource(R.string.phrase_hint),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = phrase,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        modifier = Modifier.horizontalScroll(rememberScrollState())
                    )
                }
            }

            BarAction(
                icon = Icons.Filled.Backspace,
                contentDescription = stringResource(R.string.cd_clear_phrase),
                enabled = phrase.isNotBlank(),
                onClick = onClear
            )
            FilledIconButton(
                onClick = onSay,
                enabled = phrase.isNotBlank(),
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = stringResource(R.string.cd_say_phrase),
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
private fun SpeakerAvatar() {
    val avatarConfig = LocalAvatarConfig.current
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.size(52.dp)
    ) {
        AvatarView(config = avatarConfig, modifier = Modifier.size(52.dp))
    }
}

@Composable
private fun BarAction(
    icon: ImageVector,
    contentDescription: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.size(52.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(30.dp)
        )
    }
}
