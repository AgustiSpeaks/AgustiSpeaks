package com.tunombre.agustispeaks.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * Las fotos que agrega quien usa la app, guardadas dentro de ella: las de los
 * pictogifts y las de los pictogramas propios.
 *
 * **Se copian, no se enlazan.** La galería y la cámara entregan un permiso de
 * lectura que dura lo que dura la pantalla que lo pidió; si se guardara el
 * enlace, la foto desaparecería al día siguiente, o cuando alguien la borre de
 * la galería. Una copia propia no depende de nada de eso.
 *
 * **Se achican al copiarlas.** Una foto de teléfono pesa varios megas y en la
 * tarjeta se ve a unos cientos de píxeles: guardarla entera solo llenaría el
 * almacenamiento.
 *
 * Todo lo de acá lee o escribe archivos: hay que llamarlo fuera del hilo
 * principal.
 */
object UserPhotos {

    // La carpeta conserva el nombre de cuando solo había fotos de pictogifts:
    // cambiarlo dejaría huérfanas las fotos que ya se guardaron.
    private const val FOLDER = "pictogifts"
    private const val CAMERA_FOLDER = "camara"
    private const val CAMERA_FILE = "captura.jpg"

    /** Lado mayor de la foto guardada, en píxeles. */
    private const val MAX_SIDE = 1280

    fun file(context: Context, name: String): File =
        File(File(context.filesDir, FOLDER), name)

    /**
     * Dónde escribe la cámara. Es un archivo temporal y siempre el mismo: la
     * foto se procesa con [save] apenas vuelve, y la cámara la pisa la próxima vez.
     */
    fun cameraUri(context: Context): Uri {
        val dir = File(context.cacheDir, CAMERA_FOLDER).apply { mkdirs() }
        return FileProvider.getUriForFile(
            context,
            context.packageName + ".fotos",
            File(dir, CAMERA_FILE)
        )
    }

    /**
     * Copia la imagen de [uri] a la carpeta de la app, derecha y achicada.
     * Devuelve el nombre del archivo, o null si no se pudo leer como imagen.
     */
    fun save(context: Context, uri: Uri): String? = try {
        val resolver = context.contentResolver

        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
            null
        } else {
            val options = BitmapFactory.Options().apply {
                inSampleSize = sampleSize(bounds.outWidth, bounds.outHeight, MAX_SIDE)
            }
            val decoded = resolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, options)
            }
            if (decoded == null) {
                null
            } else {
                // Las cámaras guardan la foto acostada y anotan el giro aparte.
                // Sin esto, media galería se vería de costado.
                val degrees = resolver.openInputStream(uri)?.use { exifRotation(it) } ?: 0
                val result = scaleDown(rotate(decoded, degrees), MAX_SIDE)

                val name = UUID.randomUUID().toString() + ".jpg"
                val dest = file(context, name)
                dest.parentFile?.mkdirs()
                FileOutputStream(dest).use { result.compress(Bitmap.CompressFormat.JPEG, 88, it) }
                name
            }
        }
    } catch (e: Exception) {
        null
    }

    /** Lee una foto guardada, achicada a [maxSide] para no gastar memoria en una miniatura. */
    fun load(context: Context, name: String, maxSide: Int): Bitmap? = try {
        val f = file(context, name)
        if (!f.exists()) {
            null
        } else {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(f.path, bounds)
            val options = BitmapFactory.Options().apply {
                inSampleSize = sampleSize(bounds.outWidth, bounds.outHeight, maxSide)
            }
            BitmapFactory.decodeFile(f.path, options)
        }
    } catch (e: Exception) {
        null
    }

    /**
     * Borra las fotos que ya no usa nadie, y la última captura de la cámara.
     * [inUse] tiene que traer **todas** las fotos vigentes —de pictogifts y de
     * pictogramas propios—: la que falte se borra.
     *
     * No se borran en el momento en que se quita un favorito o un pictograma,
     * sino acá, al arrancar: así "Deshacer" puede devolver un favorito quitado
     * por error con su foto entera, y una foto elegida en un formulario que
     * después se canceló se limpia sola. Y solo se tocan archivos anteriores a
     * [startedAt], para no borrar una foto que se está guardando justo mientras
     * esto corre.
     */
    fun cleanUp(context: Context, inUse: Set<String>, startedAt: Long) {
        File(context.filesDir, FOLDER).listFiles()?.forEach { f ->
            if (f.name !in inUse && f.lastModified() < startedAt) f.delete()
        }
        File(File(context.cacheDir, CAMERA_FOLDER), CAMERA_FILE).delete()
    }

    private fun sampleSize(width: Int, height: Int, maxSide: Int): Int {
        var sample = 1
        while (max(width, height) / (sample * 2) >= maxSide) sample *= 2
        return sample
    }

    private fun exifRotation(stream: java.io.InputStream): Int =
        when (ExifInterface(stream).getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90
            ExifInterface.ORIENTATION_ROTATE_180 -> 180
            ExifInterface.ORIENTATION_ROTATE_270 -> 270
            else -> 0
        }

    private fun rotate(bitmap: Bitmap, degrees: Int): Bitmap {
        if (degrees == 0) return bitmap
        val matrix = Matrix().apply { postRotate(degrees.toFloat()) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun scaleDown(bitmap: Bitmap, maxSide: Int): Bitmap {
        val side = max(bitmap.width, bitmap.height)
        if (side <= maxSide) return bitmap
        val factor = maxSide.toFloat() / side
        return Bitmap.createScaledBitmap(
            bitmap,
            (bitmap.width * factor).roundToInt(),
            (bitmap.height * factor).roundToInt(),
            true
        )
    }
}
