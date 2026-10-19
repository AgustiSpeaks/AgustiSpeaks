package com.tunombre.agustispeaks.ui.edit

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.tunombre.agustispeaks.R

/** Opacidad de lo oculto en modo edición: se ve que está, y que no se muestra. */
private const val HIDDEN_ALPHA = 0.45f

/**
 * La franja que avisa que el modo edición está activo. Va arriba del contenido
 * en cada pantalla que se puede editar: así el adulto nunca deja el modo puesto
 * sin darse cuenta.
 *
 * Empieza con una X, en la esquina superior izquierda, que sale del modo en un
 * toque: no hace falta volver a Ajustes para apagarlo.
 */
@Composable
fun EditModeBanner(onExit: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        color = MaterialTheme.colorScheme.tertiaryContainer,
        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(start = 4.dp, end = 16.dp, top = 2.dp, bottom = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onExit) {
                Icon(
                    Icons.Filled.Close,
                    contentDescription = stringResource(R.string.edit_mode_exit),
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.edit_mode_banner),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Envuelve un lugar, una categoría o un pictograma para el modo edición: le
 * pone un lápiz en la esquina para abrir su formulario, y lo atenúa si está
 * oculto (el lápiz pasa a ser un ojo tachado).
 *
 * Tocar el elemento sigue haciendo lo de siempre —decir la palabra, abrir la
 * categoría o el lugar—: el modo edición agrega opciones, no cambia cómo se usa.
 * Fuera del modo edición no agrega nada.
 */
@Composable
fun EditableTile(
    editing: Boolean,
    hidden: Boolean,
    name: String,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    if (!editing) {
        Box(modifier) { content() }
        return
    }
    Box(modifier) {
        Box(if (hidden) Modifier.alpha(HIDDEN_ALPHA) else Modifier) { content() }
        // 40 dp a la vista; el botón asegura igual 48 dp de zona táctil.
        FilledTonalIconButton(
            onClick = onEdit,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(2.dp)
                .size(40.dp)
        ) {
            Icon(
                imageVector = if (hidden) Icons.Filled.VisibilityOff else Icons.Filled.Edit,
                contentDescription = stringResource(R.string.edit_item, name),
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

/**
 * La tarjeta "Agregar" al final de una grilla. Del mismo tamaño que lo que la
 * rodea pero sin relleno, solo con el borde: se distingue a simple vista, y
 * nadie la toca creyendo que va a decir algo.
 */
@Composable
fun AddTile(
    label: String,
    icon: ImageVector,
    aspectRatio: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.primary,
        border = BorderStroke(2.dp, MaterialTheme.colorScheme.outline),
        modifier = modifier.aspectRatio(aspectRatio)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 6.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically)
        ) {
            BoxWithConstraints(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                // Más chico que un ícono de pictograma, para que no compita con ellos.
                val side = minOf(maxWidth, maxHeight) * 0.6f
                if (side > 14.dp) {
                    Icon(icon, contentDescription = null, modifier = Modifier.size(side))
                }
            }
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 2,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
