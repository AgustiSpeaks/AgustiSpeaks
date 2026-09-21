# AgustiSpeaks

App Android de Comunicación Aumentativa y Alternativa (CAA/AAC): tablero de
pictogramas por lugar para armar frases que se leen en voz alta (texto‑a‑voz).

- **UI**: Kotlin + Jetpack Compose + Material 3
- **Arquitectura**: MVVM, Navigation‑Compose, ViewModel + StateFlow
- **Persistencia**: Room + DataStore (Preferences)
- **Voz**: `android.speech.tts.TextToSpeech` mediante `TtsManager` (instancia
  única, reproduce con `QUEUE_FLUSH`)
- **Adaptativo**: `WindowSizeClass` de Material 3
- **minSdk 26 · targetSdk 35 · idioma de la UI: español**

## Abrir el proyecto

Abrir la carpeta en **Android Studio** (Ladybug o superior). Android Studio
descarga el Gradle Wrapper, el SDK y las dependencias automáticamente.

> El binario `gradle/wrapper/gradle-wrapper.jar` no está incluido. Android
> Studio lo genera al abrir el proyecto; desde consola se puede crear con
> `gradle wrapper --gradle-version 8.11.1` si se tiene Gradle instalado.

## Estado

Andamiaje inicial: build scripts, manifest, tema Material 3, `TtsManager` y
`NavGraph` con rutas (`place_selection`, `board/{placeId}`, `favorites`,
`settings`). Las pantallas son stubs. Siguiente paso: `PlaceSelectionScreen`
y datos semilla.

## Estructura

```
app/src/main/java/com/tunombre/agustispeaks/
 ├─ data/
 │   ├─ model/        Place, Category, Pictogram, UserSettings, TextSizeOption
 │   ├─ local/        Room + DataStore (pendiente)
 │   └─ repository/   Repositorios (pendiente)
 ├─ tts/              TtsManager, LocalTtsManager
 ├─ ui/
 │   ├─ theme/        Color.kt, Type.kt, Theme.kt
 │   ├─ placeselection/
 │   ├─ board/
 │   ├─ favorites/
 │   └─ settings/
 └─ navigation/       Destinations.kt, NavGraph.kt
```
