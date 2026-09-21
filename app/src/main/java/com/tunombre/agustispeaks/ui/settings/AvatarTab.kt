package com.tunombre.agustispeaks.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tunombre.agustispeaks.avatar.AvatarAction
import com.tunombre.agustispeaks.avatar.AvatarFigure
import com.tunombre.agustispeaks.avatar.AvatarOptions
import com.tunombre.agustispeaks.avatar.AvatarView
import com.tunombre.agustispeaks.avatar.ResolvedAction
import com.tunombre.agustispeaks.avatar.catalog.AvatarCatalog
import com.tunombre.agustispeaks.data.model.AvatarConfig
import com.tunombre.agustispeaks.data.model.AvatarGender
import com.tunombre.agustispeaks.data.model.AvatarKind
import com.tunombre.agustispeaks.tts.LocalTtsManager
import com.tunombre.agustispeaks.ui.LocalAppViewModel
import com.tunombre.agustispeaks.ui.util.hexToColor
import kotlin.math.roundToInt

private val AVATAR_SUBTABS = listOf("Identidad", "Cabeza", "Rostro", "Estilo", "Voz")

/**
 * Editor del avatar, a dos columnas porque la app va en horizontal.
 *
 * Izquierda: la vista previa, **fija**. Nunca sale de pantalla, así se ve el
 * efecto de cada cambio sin tener que desplazarse.
 * Derecha: las sub-pestañas y sus opciones, con su propio scroll.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvatarTab(modifier: Modifier = Modifier) {
    val vm = LocalAppViewModel.current
    val config by vm.avatarDraft.collectAsStateWithLifecycle()
    val isRobot = config.kind == AvatarKind.ROBOT

    var subTab by rememberSaveable { mutableStateOf(0) }

    Row(modifier = modifier.fillMaxSize()) {

        AvatarPreviewPane(
            config = config,
            onRandomize = { vm.randomizeAvatar() },
            modifier = Modifier
                .fillMaxWidth(0.34f)
                .widthIn(min = 180.dp, max = 320.dp)
                .fillMaxHeight()
        )

        VerticalDivider()

        Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
            ScrollableTabRow(selectedTabIndex = subTab, edgePadding = 12.dp) {
                AVATAR_SUBTABS.forEachIndexed { index, title ->
                    Tab(
                        selected = subTab == index,
                        onClick = { subTab = index },
                        text = { Text(title) }
                    )
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                when (subTab) {
                    0 -> IdentidadSection(config, vm::updateAvatar)
                    1 -> if (isRobot) RobotColorSection(config, vm::updateAvatar)
                         else CabezaSection(config, vm::updateAvatar)
                    2 -> if (isRobot) RobotFaceSection(config, vm::updateAvatar)
                         else RostroSection(config, vm::updateAvatar)
                    3 -> EstiloSection(config, vm::updateAvatar, isRobot)
                    else -> VozSection()
                }
            }
        }
    }
}

/**
 * Columna izquierda fija. Las medidas del retrato y de la figura se calculan
 * desde el espacio real disponible: en horizontal la altura es poca y con
 * tamaños fijos el avatar quedaba cortado por las pestañas.
 */
@Composable
private fun AvatarPreviewPane(
    config: AvatarConfig,
    onRandomize: () -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
        // El título y el botón se llevan ~84dp; el resto es para el dibujo.
        // El ancho también limita: retrato (0.82·h) + figura (0.62·h) + separación.
        val byHeight = maxHeight - 84.dp
        val byWidth = (maxWidth - 10.dp) / 1.44f
        val art = minOf(byHeight, byWidth).coerceIn(84.dp, 240.dp)

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "Mi Avatar",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.padding(top = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(art * 0.82f)
                ) {
                    AvatarView(
                        config = config,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .padding(art * 0.06f)
                    )
                }
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.width(art * 0.62f).height(art)
                ) {
                    AvatarFigure(
                        config = config,
                        pose = remember { AvatarCatalog.pose(ResolvedAction(AvatarAction.HELLO), 0.5f) },
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(18.dp))
                            .padding(art * 0.04f)
                    )
                }
            }

            TextButton(onClick = onRandomize, modifier = Modifier.padding(top = 2.dp)) {
                Icon(Icons.Filled.AutoAwesome, contentDescription = null)
                Text("  Aleatorio")
            }
        }
    }
}

/* --------------------------- Persona (avataaars) --------------------------- */

@Composable
private fun IdentidadSection(config: AvatarConfig, update: ((AvatarConfig) -> AvatarConfig) -> Unit) {
    OptionChips(
        title = "Género",
        options = listOf(
            "Neutro" to AvatarGender.NEUTRO.name,
            "Masculino" to AvatarGender.MASCULINO.name,
            "Femenino" to AvatarGender.FEMENINO.name
        ),
        selectedValue = config.gender.name,
        onSelect = { v ->
            val g = AvatarGender.valueOf(v)
            update {
                it.copy(
                    gender = g,
                    // "Robot" solo existe para género neutro.
                    kind = if (g == AvatarGender.NEUTRO) it.kind else AvatarKind.PERSONA
                )
            }
        }
    )

    if (config.gender == AvatarGender.NEUTRO) {
        OptionChips(
            title = "¿Persona o robot?",
            options = listOf("Persona" to AvatarKind.PERSONA.name, "Robot" to AvatarKind.ROBOT.name),
            selectedValue = config.kind.name,
            onSelect = { v -> update { it.copy(kind = AvatarKind.valueOf(v)) } }
        )
    }

    if (config.kind == AvatarKind.PERSONA) {
        ColorSwatchRow(
            title = "Tono de piel",
            colors = AvatarOptions.skinColors.map { it.hex to hexToColor("#${it.hex}") },
            selectedHex = config.skinColor,
            onSelect = { v -> update { it.copy(skinColor = v) } }
        )
    } else {
        ColorSwatchRow(
            title = "Color del robot",
            colors = AvatarOptions.robotColors.map { it.hex to hexToColor("#${it.hex}") },
            selectedHex = config.robotColor,
            onSelect = { v -> update { it.copy(robotColor = v) } }
        )
    }
}

@Composable
private fun CabezaSection(config: AvatarConfig, update: ((AvatarConfig) -> AvatarConfig) -> Unit) {
    OptionChips(
        title = "Peinado",
        options = AvatarOptions.hairStyles.map { it.label to it.value },
        selectedValue = config.hairStyle,
        onSelect = { v -> update { it.copy(hairStyle = v) } }
    )
    ColorSwatchRow(
        title = "Color de cabello",
        colors = AvatarOptions.hairColors.map { it.hex to hexToColor("#${it.hex}") },
        selectedHex = config.hairColor,
        onSelect = { v -> update { it.copy(hairColor = v) } }
    )
    OptionChips(
        title = "Cejas",
        options = AvatarOptions.eyebrows.map { it.label to it.value },
        selectedValue = config.eyebrows,
        onSelect = { v -> update { it.copy(eyebrows = v) } }
    )
}

@Composable
private fun RostroSection(config: AvatarConfig, update: ((AvatarConfig) -> AvatarConfig) -> Unit) {
    OptionChips(
        title = "Ojos",
        options = AvatarOptions.eyes.map { it.label to it.value },
        selectedValue = config.eyes,
        onSelect = { v -> update { it.copy(eyes = v) } }
    )
    OptionChips(
        title = "Boca",
        options = AvatarOptions.mouths.map { it.label to it.value },
        selectedValue = config.mouth,
        onSelect = { v -> update { it.copy(mouth = v) } }
    )
}

/**
 * Ropa, pantalón y zapatos. Aplica también al robot: ahí la "ropa" pinta el
 * chasis, el "pantalón" las placas de las piernas y los "zapatos" las botas.
 */
@Composable
private fun EstiloSection(
    config: AvatarConfig,
    update: ((AvatarConfig) -> AvatarConfig) -> Unit,
    isRobot: Boolean
) {
    if (isRobot) {
        Text(
            "Las extremidades y los dedos del robot son de acero. " +
                "Estos colores pintan el chasis, las placas de las piernas y las botas.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 4.dp)
        )
    } else {
        OptionChips(
            title = "Tipo de ropa",
            options = AvatarOptions.clothing.map { it.label to it.value },
            selectedValue = config.clothing,
            onSelect = { v -> update { it.copy(clothing = v) } }
        )
    }
    ColorSwatchRow(
        title = if (isRobot) "Color del chasis" else "Color de ropa",
        colors = AvatarOptions.clothingColors.map { it.hex to hexToColor("#${it.hex}") },
        selectedHex = config.clothingColor,
        onSelect = { v -> update { it.copy(clothingColor = v) } }
    )
    ColorSwatchRow(
        title = if (isRobot) "Color de las piernas" else "Color del pantalón",
        colors = AvatarOptions.pantsColors.map { it.hex to hexToColor("#${it.hex}") },
        selectedHex = config.pantsColor,
        onSelect = { v -> update { it.copy(pantsColor = v) } }
    )
    ColorSwatchRow(
        title = if (isRobot) "Color de las botas" else "Color de los zapatos",
        colors = AvatarOptions.shoeColors.map { it.hex to hexToColor("#${it.hex}") },
        selectedHex = config.shoeColor,
        onSelect = { v -> update { it.copy(shoeColor = v) } }
    )
}

/* ------------------------------ Robot (bottts) ---------------------------- */

/** Cabeza del robot: color, forma del rostro y accesorio. */
@Composable
private fun RobotColorSection(config: AvatarConfig, update: ((AvatarConfig) -> AvatarConfig) -> Unit) {
    ColorSwatchRow(
        title = "Color del robot",
        colors = AvatarOptions.robotColors.map { it.hex to hexToColor("#${it.hex}") },
        selectedHex = config.robotColor,
        onSelect = { v -> update { it.copy(robotColor = v) } }
    )
    OptionChips(
        title = "Forma del rostro",
        options = AvatarOptions.robotFaces.map { it.label to it.value },
        selectedValue = config.robotFace,
        onSelect = { v -> update { it.copy(robotFace = v) } }
    )
    OptionChips(
        title = "Accesorio",
        options = AvatarOptions.robotAccessories.map { it.label to it.value },
        selectedValue = config.robotAccessory,
        onSelect = { v -> update { it.copy(robotAccessory = v) } }
    )
}

@Composable
private fun RobotFaceSection(config: AvatarConfig, update: ((AvatarConfig) -> AvatarConfig) -> Unit) {
    OptionChips(
        title = "Ojos",
        options = AvatarOptions.robotEyes.map { it.label to it.value },
        selectedValue = config.robotEyes,
        onSelect = { v -> update { it.copy(robotEyes = v) } }
    )
    OptionChips(
        title = "Boca",
        options = AvatarOptions.robotMouths.map { it.label to it.value },
        selectedValue = config.robotMouth,
        onSelect = { v -> update { it.copy(robotMouth = v) } }
    )
    if (config.robotFace == "visor") {
        Text(
            "Con el rostro \"Visor\" los ojos se ven como una barra de luz.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/* --------------------------------- Voz ----------------------------------- */

@Composable
private fun VozSection() {
    val vm = LocalAppViewModel.current
    val tts = LocalTtsManager.current
    val settings by vm.userSettings.collectAsStateWithLifecycle()
    val ttsState by tts.state.collectAsStateWithLifecycle()

    val voices = remember(ttsState.isReady) { tts.spanishVoices() }

    var volume by remember { mutableFloatStateOf(settings.voiceVolume) }
    var rate by remember { mutableFloatStateOf(rateToFraction(settings.voiceRate)) }
    var pitch by remember { mutableFloatStateOf(rateToFraction(settings.voicePitch)) }

    Column(modifier = Modifier.fillMaxWidth()) {
        if (voices.isEmpty()) {
            Text(
                "VOZ DEL SISTEMA",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            Text("Voz por defecto del sistema", style = MaterialTheme.typography.bodyLarge)
        } else {
            DropdownField(
                label = "Voz del sistema",
                options = voices.map { it.label to it.id },
                selectedValue = settings.voiceId ?: voices.first().id,
                onSelect = { vm.setVoiceId(it) }
            )
        }

        LabeledSlider(
            label = "Volumen",
            value = volume,
            onValueChange = { volume = it },
            onValueChangeFinished = { vm.setVoiceVolume(volume) },
            valueLabel = "${(volume * 100).roundToInt()}%"
        )
        LabeledSlider(
            label = "Velocidad",
            value = rate,
            onValueChange = { rate = it },
            onValueChangeFinished = { vm.setVoiceRate(fractionToRate(rate)) },
            valueLabel = "${(rate * 100).roundToInt()}%"
        )
        LabeledSlider(
            label = "Tono",
            value = pitch,
            onValueChange = { pitch = it },
            onValueChangeFinished = { vm.setVoicePitch(fractionToRate(pitch)) },
            valueLabel = "${(pitch * 100).roundToInt()}%"
        )

        Button(
            onClick = {
                tts.applySettings(
                    pitch = fractionToRate(pitch),
                    rate = fractionToRate(rate),
                    volume = volume,
                    voiceId = settings.voiceId
                )
                tts.speak("Hola, así sueno con AgustiSpeaks")
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
        ) {
            Icon(Icons.Filled.PlayArrow, contentDescription = null)
            Text("  Probar voz")
        }
    }
}

/* ------------------------------------------------------------------------- */

/** Mapea el valor TTS (0.5..2.0) a la fracción del slider (0..1); 1.0 -> ~0.33. */
private fun rateToFraction(ttsValue: Float): Float =
    ((ttsValue - 0.5f) / 1.5f).coerceIn(0f, 1f)

private fun fractionToRate(fraction: Float): Float =
    (0.5f + fraction * 1.5f).coerceIn(0.5f, 2.0f)
