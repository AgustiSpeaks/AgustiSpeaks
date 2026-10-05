package com.tunombre.agustispeaks.avatar

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.tunombre.agustispeaks.avatar.catalog.AvatarCatalog
import com.tunombre.agustispeaks.data.model.AvatarConfig

/** Retrato (busto) estático del avatar. Para el editor y la barra de frase. */
@Composable
fun AvatarView(
    config: AvatarConfig,
    modifier: Modifier = Modifier,
    pose: AvatarPose = AvatarPose()
) {
    Canvas(modifier = modifier) { drawAvatar(config, pose) }
}

/** Avatar de cuerpo completo en una pose fija (miniatura de Recientes). */
@Composable
fun AvatarFigure(
    config: AvatarConfig,
    pose: AvatarPose,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) { drawAvatarFigure(config, pose) }
}

/**
 * Avatar de cuerpo completo animado ejecutando una [ResolvedAction] en bucle.
 * Overlay de "Decir" y lista de Recientes.
 */
@Composable
fun AnimatedAvatar(
    config: AvatarConfig,
    resolved: ResolvedAction,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "avatar-action")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = AvatarCatalog.loopMillis(resolved),
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )
    Canvas(modifier = modifier) { drawAvatarFigure(config, AvatarCatalog.pose(resolved, phase)) }
}
