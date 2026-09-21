package com.tunombre.agustispeaks

import android.app.Application
import com.tunombre.agustispeaks.avatar.catalog.AvatarCatalog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Clase [Application] de la app. Punto de arranque del catálogo de animaciones.
 */
class AgustiSpeaksApplication : Application() {

    /** Vive lo que vive la app; no hay nada que cancelar antes. */
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()

        // La capa empaquetada va sí o sí antes del primer dibujo: es la que
        // está verificada contra el motor.
        AvatarCatalog.loadBase(this)

        // La del especialista puede llegar un instante después. Lo peor que
        // pasa mientras tanto es que una corrección tarde en aplicarse.
        scope.launch { AvatarCatalog.loadOverrides(this@AgustiSpeaksApplication) }
    }
}
