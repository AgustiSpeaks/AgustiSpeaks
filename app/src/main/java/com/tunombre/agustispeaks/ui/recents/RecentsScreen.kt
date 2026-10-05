package com.tunombre.agustispeaks.ui.recents

import android.os.SystemClock
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.NorthEast
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDefaults
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tunombre.agustispeaks.R
import com.tunombre.agustispeaks.avatar.AvatarFigure
import com.tunombre.agustispeaks.avatar.catalog.AvatarCatalog
import com.tunombre.agustispeaks.data.model.AvatarConfig
import com.tunombre.agustispeaks.data.model.Pictogift
import com.tunombre.agustispeaks.phrase.LocalPhraseController
import com.tunombre.agustispeaks.tts.LocalTtsManager
import com.tunombre.agustispeaks.ui.LocalAppViewModel
import com.tunombre.agustispeaks.ui.LocalAvatarConfig
import com.tunombre.agustispeaks.ui.components.SavedPhoto
import com.tunombre.agustispeaks.ui.components.SpeakingOverlay
import com.tunombre.agustispeaks.ui.components.animationFor
import com.tunombre.agustispeaks.ui.components.shownPhoto
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val TAB_RECIENTES = 0
private const val TAB_FAVORITOS = 1

/**
 * Frases ya dichas, en dos pestañas:
 *
 * - **Recientes**: las últimas 30, en orden de uso. Se llenan solas.
 * - **Favoritos**: los pictogifts que alguien eligió guardar, con o sin foto.
 *   No se borran con el historial ni tienen tope. La misma frase puede estar
 *   varias veces, una por cada foto.
 *
 * Cada fila muestra el pictogift (avatar en la pose de esa acción, y la foto si
 * tiene) + la frase. Al tocar la fila se abre la animación completa; con la
 * flecha se carga como frase actual; con la estrella entra o sale de favoritos.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecentsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    /** Abrir directo en Favoritos: es lo que hace la estrella de la barra superior. */
    startOnFavorites: Boolean = false
) {
    val appVm = LocalAppViewModel.current
    val recents by appVm.recentPhrases.collectAsStateWithLifecycle()
    val favorites by appVm.favorites.collectAsStateWithLifecycle()
    val avatarConfig = LocalAvatarConfig.current
    val phraseController = LocalPhraseController.current
    val tts = LocalTtsManager.current

    var tab by rememberSaveable { mutableIntStateOf(if (startOnFavorites) TAB_FAVORITOS else TAB_RECIENTES) }
    var overlay by remember { mutableStateOf<Pictogift?>(null) }

    // `distinctBy` porque el id es la clave de la lista, y una clave repetida
    // hace caer a LazyColumn. No debería pasar nunca; si pasa, que no se note.
    val recentGifts = remember(recents) { recents.map { Pictogift.fromText(it) }.distinctBy { it.id } }
    val favoriteGifts = remember(favorites) { favorites.distinctBy { it.id } }
    val favoriteIds = remember(favorites) { favorites.map { it.id }.toSet() }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val resources = LocalContext.current.resources
    val undoLabel = stringResource(R.string.undo)
    val undo = remember { UndoBatch() }

    /**
     * Muestra el aviso de la tanda abierta, con el contador de nuevo en 5. Si
     * había uno, lo reemplaza: el nuevo ya cuenta a todos los quitados.
     */
    fun showUndo() {
        val round = ++undo.round
        val count = undo.removed.size
        val message = resources.getQuantityString(R.plurals.favorites_removed, count, count)
        scope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            // Indefinido porque lo cierra [UndoSnackbar] a los 5 segundos: el
            // mismo contador que se ve en pantalla es el que lo cierra.
            val result = snackbarHostState.showSnackbar(
                message = message,
                actionLabel = undoLabel,
                duration = SnackbarDuration.Indefinite
            )
            // Lo cerró un aviso más nuevo: la tanda sigue abierta y decide ese.
            if (round != undo.round) return@launch
            if (result == SnackbarResult.ActionPerformed) appVm.restoreFavorites(undo.removed.toList())
            undo.removed.clear()
        }
    }

    /**
     * La estrella de una fila. Al quitar, ofrece "Deshacer": en la pestaña
     * Favoritos la fila desaparece en el acto, así que sin esto un toque
     * accidental no tendría vuelta atrás, y con una foto de por medio sería
     * perderla. La foto sigue en disco hasta el próximo arranque, así que
     * deshacer la recupera entera.
     *
     * Los quitados seguidos se juntan en una sola tanda: dos toques rápidos son
     * justo el accidente típico al desplazar la lista, y "Deshacer" tiene que
     * devolver los dos, no solo el último.
     */
    fun toggleFromRow(gift: Pictogift) {
        // La posición se mide sobre la lista sin los que ya están en la tanda.
        // Tocando rápido, la lista en pantalla todavía puede tener al anterior,
        // porque guardar tarda unos milisegundos más que un toque.
        val pendingIds = undo.removed.map { it.first.id }.toSet()
        val position = favorites.filterNot { it.id in pendingIds }.indexOfFirst { it.id == gift.id }
        appVm.toggleFavorite(gift)

        if (position >= 0) {
            undo.removed.add(gift to position)
            showUndo()
        } else if (undo.removed.removeAll { it.first.id == gift.id }) {
            // Se volvió a agregar uno de la tanda con su propia estrella: ya no
            // hay que deshacerlo, y el aviso tiene que dejar de contarlo.
            if (undo.removed.isEmpty()) snackbarHostState.currentSnackbarData?.dismiss() else showUndo()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) { data -> UndoSnackbar(data) } },
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
                    // Solo el historial se vacía de un toque. Los favoritos se
                    // eligieron de a uno y se quitan de a uno, con su estrella.
                    if (tab == TAB_RECIENTES && recents.isNotEmpty()) {
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
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            TabRow(selectedTabIndex = tab) {
                PhrasesTab(
                    selected = tab == TAB_RECIENTES,
                    onClick = { tab = TAB_RECIENTES },
                    icon = Icons.Filled.History,
                    title = stringResource(R.string.recents_title)
                )
                PhrasesTab(
                    selected = tab == TAB_FAVORITOS,
                    onClick = { tab = TAB_FAVORITOS },
                    icon = Icons.Filled.Star,
                    title = stringResource(R.string.favorites_title)
                )
            }

            val gifts = if (tab == TAB_RECIENTES) recentGifts else favoriteGifts
            if (gifts.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        stringResource(
                            if (tab == TAB_RECIENTES) R.string.recents_empty else R.string.favorites_empty
                        ),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(gifts, key = { it.id }) { gift ->
                        PhraseRow(
                            gift = gift,
                            avatarConfig = avatarConfig,
                            isFavorite = gift.id in favoriteIds,
                            onOpen = {
                                tts.speak(gift.phrase)
                                overlay = gift
                            },
                            onUse = {
                                phraseController.setPhrase(gift.phrase)
                                tts.speak(gift.phrase)
                                onBack()
                            },
                            onToggleFavorite = { toggleFromRow(gift) }
                        )
                    }
                }
            }
        }
    }

    overlay?.let { gift ->
        SpeakingOverlay(
            gift = gift,
            onDismiss = { overlay = null }
        )
    }
}

/** Cuánto tiempo hay para deshacer, en milisegundos. */
private const val UNDO_MILLIS = 5_000L

/**
 * Los favoritos quitados desde que apareció el aviso, en el orden en que se
 * quitaron, cada uno con la posición que tenía en ese momento.
 *
 * [round] numera los avisos: cuando uno nuevo reemplaza al anterior, el
 * anterior se cierra, y así sabe que no le toca ni deshacer ni cerrar la tanda.
 */
private class UndoBatch {
    val removed = mutableListOf<Pair<Pictogift, Int>>()
    var round = 0
}

/**
 * Aviso de "Se quitó de favoritos" con una cuenta regresiva: un anillo que se
 * vacía y los segundos que quedan, para que se sepa cuánto tiempo hay para
 * deshacer. Al llegar a cero se cierra solo.
 *
 * La cuenta es de reloj y no una animación. Las animaciones respetan la escala
 * de animación del sistema, y con "Quitar animaciones" activado en
 * Accesibilidad duran cero: el aviso se cerraría en el acto, justo para quien
 * más tiempo necesita. Por lo mismo, el anillo avanza a saltos cortos en lugar
 * de deslizarse.
 */
@Composable
private fun UndoSnackbar(data: SnackbarData) {
    var remaining by remember(data) { mutableLongStateOf(UNDO_MILLIS) }
    LaunchedEffect(data) {
        val start = SystemClock.elapsedRealtime()
        while (remaining > 0L) {
            delay(33)
            remaining = (UNDO_MILLIS - (SystemClock.elapsedRealtime() - start)).coerceAtLeast(0L)
        }
        data.dismiss()
    }

    val seconds = ((remaining + 999L) / 1000L).toInt()
    Snackbar(
        modifier = Modifier.padding(12.dp),
        action = {
            TextButton(
                onClick = { data.performAction() },
                colors = ButtonDefaults.textButtonColors(contentColor = SnackbarDefaults.actionColor)
            ) {
                Text(data.visuals.actionLabel.orEmpty())
            }
        }
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(34.dp)) {
                CircularProgressIndicator(
                    progress = { remaining.toFloat() / UNDO_MILLIS },
                    modifier = Modifier.fillMaxSize(),
                    color = SnackbarDefaults.actionColor,
                    trackColor = LocalContentColor.current.copy(alpha = 0.24f),
                    strokeWidth = 3.dp
                )
                Text(
                    text = seconds.toString(),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    // El número cambia cada segundo; leerlo en voz alta cada vez
                    // sería ruido. El lector de pantalla ya anuncia el aviso.
                    modifier = Modifier.clearAndSetSemantics { }
                )
            }
            Text(
                text = data.visuals.message,
                modifier = Modifier.padding(start = 12.dp)
            )
        }
    }
}

/** Pestaña con el ícono al lado del texto, para no robarle alto a la lista en horizontal. */
@Composable
private fun PhrasesTab(
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    title: String
) {
    Tab(
        selected = selected,
        onClick = onClick,
        text = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
                Text(title, modifier = Modifier.padding(start = 8.dp))
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PhraseRow(
    gift: Pictogift,
    avatarConfig: AvatarConfig,
    isFavorite: Boolean,
    onOpen: () -> Unit,
    onUse: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    val customPictograms by LocalAppViewModel.current.customPictograms.collectAsStateWithLifecycle()
    // La suya, o la de un pictograma propio que nombre la frase.
    val shownPhoto = gift.shownPhoto(customPictograms)
    val resolved = remember(gift.phrase, shownPhoto) { animationFor(gift.phrase, shownPhoto) }

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
            shownPhoto?.let { photo ->
                SavedPhoto(
                    name = photo,
                    contentDescription = stringResource(R.string.photo_description, gift.phrase),
                    maxSide = 256,
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .size(84.dp)
                        .clip(RoundedCornerShape(14.dp))
                )
            }
            Text(
                text = gift.phrase,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            )
            // El estado va en la forma (llena o hueca) y no solo en el color,
            // para que se lea igual en alto contraste y en modo oscuro.
            IconButton(onClick = onToggleFavorite) {
                Icon(
                    if (isFavorite) Icons.Filled.Star else Icons.Filled.StarBorder,
                    contentDescription = stringResource(
                        if (isFavorite) R.string.favorite_remove else R.string.favorite_add
                    ),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
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
