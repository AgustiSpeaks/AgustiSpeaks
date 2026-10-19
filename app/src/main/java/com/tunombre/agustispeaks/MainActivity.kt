package com.tunombre.agustispeaks

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.tunombre.agustispeaks.navigation.NavGraph
import com.tunombre.agustispeaks.phrase.LocalPhraseController
import com.tunombre.agustispeaks.phrase.PhraseController
import com.tunombre.agustispeaks.tts.LocalTtsManager
import com.tunombre.agustispeaks.tts.TtsManager
import com.tunombre.agustispeaks.ui.AppViewModel
import com.tunombre.agustispeaks.ui.LocalAppViewModel
import com.tunombre.agustispeaks.ui.LocalAvatarConfig
import com.tunombre.agustispeaks.ui.LocalUserSettings
import com.tunombre.agustispeaks.ui.edit.CreatePasswordScreen
import com.tunombre.agustispeaks.ui.theme.AgustiSpeaksTheme

// Los mismos velos que pone enableEdgeToEdge() por defecto detrás de la barra de
// navegación de tres botones.
private val LIGHT_SCRIM = Color.argb(0xe6, 0xFF, 0xFF, 0xFF)
private val DARK_SCRIM = Color.argb(0x80, 0x1b, 0x1b, 0x1b)

class MainActivity : ComponentActivity() {

    private lateinit var ttsManager: TtsManager

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        ttsManager = TtsManager(applicationContext)

        setContent {
            val windowSizeClass = calculateWindowSizeClass(this)
            val navController = rememberNavController()
            val phraseController: PhraseController = viewModel()
            val appViewModel: AppViewModel = viewModel()

            val settings by appViewModel.userSettings.collectAsStateWithLifecycle()
            val avatarConfig by appViewModel.avatarConfig.collectAsStateWithLifecycle()
            val ttsState by ttsManager.state.collectAsStateWithLifecycle()
            val hasPassword by appViewModel.hasPassword.collectAsStateWithLifecycle()

            // Íconos de las barras del sistema según el modo oscuro de la app, no
            // el del teléfono: con el teléfono en oscuro y la app en claro, salían
            // claros sobre fondo claro y no se veían.
            DisposableEffect(settings.darkMode) {
                val dark = settings.darkMode
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { dark },
                    navigationBarStyle = SystemBarStyle.auto(LIGHT_SCRIM, DARK_SCRIM) { dark }
                )
                onDispose {}
            }

            // Aplica la configuración de voz cuando cambia (o cuando el TTS queda listo).
            LaunchedEffect(
                ttsState.isReady,
                settings.voicePitch,
                settings.voiceRate,
                settings.voiceVolume,
                settings.voiceId
            ) {
                if (ttsState.isReady) {
                    ttsManager.applySettings(
                        pitch = settings.voicePitch,
                        rate = settings.voiceRate,
                        volume = settings.voiceVolume,
                        voiceId = settings.voiceId
                    )
                }
            }

            CompositionLocalProvider(
                LocalTtsManager provides ttsManager,
                LocalPhraseController provides phraseController,
                LocalAppViewModel provides appViewModel,
                LocalUserSettings provides settings,
                LocalAvatarConfig provides avatarConfig
            ) {
                AgustiSpeaksTheme(
                    darkTheme = settings.darkMode,
                    highContrast = settings.highContrast
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        // La primera vez, antes que nada, se crea la contraseña
                        // del modo edición. Mientras se lee si ya existe (null),
                        // no se muestra nada: es un instante, y así no aparece el
                        // formulario a quien ya la creó.
                        when (hasPassword) {
                            null -> Unit
                            false -> CreatePasswordScreen(onCreate = appViewModel::createPassword)
                            true -> NavGraph(
                                navController = navController,
                                windowSizeClass = windowSizeClass
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        ttsManager.shutdown()
    }
}
