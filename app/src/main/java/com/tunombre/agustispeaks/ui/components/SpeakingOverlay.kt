package com.tunombre.agustispeaks.ui.components

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.tunombre.agustispeaks.avatar.AnimatedAvatar
import com.tunombre.agustispeaks.avatar.AvatarGifExporter
import com.tunombre.agustispeaks.avatar.ResolvedAction
import com.tunombre.agustispeaks.ui.LocalAvatarConfig
import kotlinx.coroutines.launch

/**
 * Overlay que aparece al tocar "Decir": muestra el avatar animado haciendo la
 * acción de la frase, y permite compartir un GIF.
 */
@Composable
fun SpeakingOverlay(
    phrase: String,
    resolved: ResolvedAction,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val avatarConfig = LocalAvatarConfig.current
    val scope = rememberCoroutineScope()
    var sharing by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        // Sin el ancho por defecto del sistema el diálogo puede usar todo el
        // espacio que necesita en horizontal.
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card {
            // En horizontal el avatar va a la izquierda, grande, y la frase con
            // los botones a la derecha: así el pictogif se ve lo más nítido
            // posible sin que el diálogo se salga de la pantalla.
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
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

                Column(
                    modifier = Modifier.width(220.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = phrase,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )

                    Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                        Text("Listo")
                    }

                    OutlinedButton(
                        onClick = {
                            if (sharing) return@OutlinedButton
                            sharing = true
                            scope.launch {
                                try {
                                    val uri = AvatarGifExporter.export(context, avatarConfig, resolved)
                                    val send = Intent(Intent.ACTION_SEND).apply {
                                        type = "image/gif"
                                        putExtra(Intent.EXTRA_STREAM, uri)
                                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    }
                                    context.startActivity(Intent.createChooser(send, "Compartir GIF"))
                                } catch (_: Exception) {
                                    // sin acción: el compartir es opcional
                                } finally {
                                    sharing = false
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (sharing) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                            Text("  Compartir GIF")
                        }
                    }
                }
            }
        }
    }
}
