package com.tunombre.agustispeaks.avatar

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.core.content.FileProvider
import com.tunombre.agustispeaks.avatar.catalog.AvatarCatalog
import com.tunombre.agustispeaks.data.model.AvatarConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream

/**
 * Genera un GIF del avatar (cuerpo completo) ejecutando una acción y devuelve
 * un [Uri] compartible (vía FileProvider). Todo offline.
 */
object AvatarGifExporter {

    // Lienzo amplio: los símbolos de la acción (flechas, "?", ✓…) y los objetos
    // necesitan aire para leerse bien en el GIF compartido.
    private const val W = 280
    private const val H = 340
    private const val FRAMES = 24
    private val BG = Color(0xFFF3EEF9)

    suspend fun export(
        context: Context,
        config: AvatarConfig,
        resolved: ResolvedAction
    ): Uri = withContext(Dispatchers.Default) {
        val pixels = (0 until FRAMES).map { i ->
            val phase = i.toFloat() / FRAMES
            val bmp = renderFrame(config, AvatarCatalog.pose(resolved, phase))
            IntArray(W * H).also { bmp.getPixels(it, 0, W, 0, 0, W, H) }
        }
        val delayCs = (AvatarCatalog.loopMillis(resolved) / FRAMES / 10).coerceIn(2, 25)

        val dir = File(context.cacheDir, "shared").apply { mkdirs() }
        val file = File(dir, "agustispeaks_accion.gif")
        BufferedOutputStream(FileOutputStream(file)).use { out ->
            GifEncoder.encode(out, W, H, pixels, delayCs)
        }
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    private fun renderFrame(config: AvatarConfig, pose: AvatarPose): Bitmap {
        val image = ImageBitmap(W, H)
        val canvas = Canvas(image)
        CanvasDrawScope().draw(
            density = Density(1f),
            layoutDirection = LayoutDirection.Ltr,
            canvas = canvas,
            size = Size(W.toFloat(), H.toFloat())
        ) {
            drawRect(BG)
            drawAvatarFigure(config, pose)
        }
        return image.asAndroidBitmap()
    }
}
