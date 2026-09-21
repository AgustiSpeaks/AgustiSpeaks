package com.tunombre.agustispeaks.ui.recents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tunombre.agustispeaks.R
import com.tunombre.agustispeaks.avatar.AvatarFigure
import com.tunombre.agustispeaks.avatar.ResolvedAction
import com.tunombre.agustispeaks.avatar.catalog.AvatarCatalog
import com.tunombre.agustispeaks.data.model.AvatarConfig
import com.tunombre.agustispeaks.phrase.LocalPhraseController
import com.tunombre.agustispeaks.tts.LocalTtsManager
import com.tunombre.agustispeaks.ui.LocalAppViewModel
import com.tunombre.agustispeaks.ui.LocalAvatarConfig
import com.tunombre.agustispeaks.ui.components.SpeakingOverlay

/**
 * Historial de frases dichas recientemente (últimas 30, persistidas). Cada
 * fila muestra el pictogift (avatar en la pose de esa acción) + la frase.
 * Al tocar la fila se abre la animación completa; con la flecha se carga como
 * frase actual.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecentsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val appVm = LocalAppViewModel.current
    val recents by appVm.recentPhrases.collectAsStateWithLifecycle()
    val avatarConfig = LocalAvatarConfig.current
    val phraseController = LocalPhraseController.current
    val tts = LocalTtsManager.current

    var overlay by remember { mutableStateOf<Pair<String, ResolvedAction>?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.recents_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cd_back)
                        )
                    }
                },
                actions = {
                    if (recents.isNotEmpty()) {
                        IconButton(onClick = { appVm.clearRecents() }) {
                            Icon(
                                Icons.Filled.DeleteSweep,
                                contentDescription = stringResource(R.string.cd_clear_recents)
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        if (recents.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    stringResource(R.string.recents_empty),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(recents, key = { it }) { phrase ->
                    val resolved = remember(phrase) { AvatarCatalog.resolve(phrase) }
                    RecentRow(
                        phrase = phrase,
                        resolved = resolved,
                        avatarConfig = avatarConfig,
                        onOpen = {
                            tts.speak(phrase)
                            overlay = phrase to resolved
                        },
                        onUse = {
                            phraseController.setPhrase(phrase)
                            tts.speak(phrase)
                            onBack()
                        }
                    )
                }
            }
        }
    }

    overlay?.let { (phrase, resolved) ->
        SpeakingOverlay(
            phrase = phrase,
            resolved = resolved,
            onDismiss = { overlay = null }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecentRow(
    phrase: String,
    resolved: ResolvedAction,
    avatarConfig: AvatarConfig,
    onOpen: () -> Unit,
    onUse: () -> Unit
) {
    Card(onClick = onOpen, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(start = 10.dp, top = 8.dp, bottom = 8.dp, end = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.width(70.dp).height(84.dp)
            ) {
                AvatarFigure(
                    config = avatarConfig,
                    pose = AvatarCatalog.pose(resolved, 0.62f),
                    modifier = Modifier.width(70.dp).height(84.dp).clip(RoundedCornerShape(14.dp)).padding(4.dp)
                )
            }
            Text(
                text = phrase,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            )
            IconButton(onClick = onUse) {
                Icon(
                    Icons.Filled.NorthEast,
                    contentDescription = "Usar esta frase",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
