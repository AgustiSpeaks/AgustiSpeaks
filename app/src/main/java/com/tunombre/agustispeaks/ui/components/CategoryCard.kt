package com.tunombre.agustispeaks.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.tunombre.agustispeaks.data.model.Category
import com.tunombre.agustispeaks.ui.icons.AppIcon
import com.tunombre.agustispeaks.ui.util.hexToColor

/** Proporción de la tarjeta de categoría. La usa también la tarjeta "Agregar categoría". */
const val CATEGORY_CARD_RATIO = 1.15f

/**
 * Tarjeta de categoría: icono grande + nombre, sobre el color de la categoría.
 * Al tocarla se abre el detalle de esa categoría. Si tiene foto, la foto ocupa
 * el lugar del ícono.
 */
@Composable
fun CategoryCard(
    category: Category,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val container = hexToColor(category.colorHex)
    val content = if (container.luminance() > 0.5f) Color(0xFF1B1B1B) else Color(0xFFFFFFFF)

    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        color = container,
        contentColor = content,
        tonalElevation = 2.dp,
        shadowElevation = 3.dp,
        // Algo más alta que ancha respecto de antes: los nombres de categoría
        // son largos ("Partes del cuerpo") y necesitan dos renglones.
        //
        // El nombre va también como descripción, sobre el elemento que recibe
        // el toque: una tarjeta cortada por el borde de la grilla quedaba sin
        // nombre para el lector de pantalla (lo marcó Accessibility Scanner).
        modifier = modifier
            .aspectRatio(CATEGORY_CARD_RATIO)
            .semantics { contentDescription = category.name }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically)
        ) {
            // Mismo criterio que en el pictograma: el nombre se mide primero y
            // el icono ocupa el espacio que queda, nunca al revés.
            BoxWithConstraints(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                val side = minOf(maxWidth, maxHeight)
                if (side > 14.dp) {
                    val photo = category.photo
                    if (photo != null) {
                        SavedPhoto(
                            name = photo,
                            contentDescription = null,
                            maxSide = 384,
                            modifier = Modifier.size(side).clip(RoundedCornerShape(10.dp))
                        )
                    } else {
                        AppIcon(name = category.icon, modifier = Modifier.size(side))
                    }
                }
            }
            Text(
                text = category.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
