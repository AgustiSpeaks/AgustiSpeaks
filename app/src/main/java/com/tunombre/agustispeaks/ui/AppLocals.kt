package com.tunombre.agustispeaks.ui

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import com.tunombre.agustispeaks.data.model.AvatarConfig
import com.tunombre.agustispeaks.data.model.UserSettings

/** ViewModel de app (ajustes + avatar + recientes). Provisto en MainActivity. */
val LocalAppViewModel = staticCompositionLocalOf<AppViewModel> {
    error("LocalAppViewModel no provisto")
}

/** Configuración actual del usuario (se recompone al cambiar). */
val LocalUserSettings = compositionLocalOf { UserSettings() }

/** Avatar persistido actual. */
val LocalAvatarConfig = compositionLocalOf { AvatarConfig() }
