# Apéndice A — Estructura del proyecto

Un único módulo Gradle (`app`), 60 archivos Kotlin: 55 en `main` y 5 en `test`.
No hay `androidTest`: ninguna prueba necesita el dispositivo.

---

## A.1 Árbol de paquetes

```
com.tunombre.agustispeaks
├── (raíz)              Application y Activity única
├── avatar              motor de animación del avatar
│   └── catalog         el catálogo de animaciones como datos
├── data
│   ├── model           modelos de dominio
│   ├── repository      persistencia de configuración (DataStore)
│   └── seed            vocabulario semilla
├── navigation          destinos y grafo de navegación
├── phrase              controlador de la oración acumulada
├── tts                 síntesis de voz
├── validacion          instrumentación de medición (solo en depuración)
└── ui
    ├── board           pantalla del tablero
    ├── components      componentes reutilizables
    ├── icons           iconos vectoriales propios
    ├── placeselection  pantalla de elección de lugar
    ├── recents         pantalla de frases recientes
    ├── settings        pantalla de configuración
    ├── theme           color, tipografía y tema
    └── util            utilidades de disposición
```

## A.2 Responsabilidad de cada paquete

| Paquete | Descripción |
|---|---|
| (raíz) | `AgustiSpeaksApplication` carga el catálogo base al arrancar; `MainActivity` fija la orientación horizontal y monta el grafo de navegación. |
| `avatar` | El motor: las 88 acciones, el modelo de pose, el resolvedor léxico y los renderizadores de figura y de símbolos. |
| `avatar.catalog` | Modelo del registro, intérprete de fotogramas, parser JSON tolerante, almacén Room, fachada de runtime y generadores procedurales. |
| `data.model` | Lugar, categoría, pictograma, configuración del avatar, preferencias y tamaño de texto. |
| `data.repository` | Lectura y escritura de preferencias sobre DataStore. |
| `data.seed` | Los 170 pictogramas, 15 categorías y 8 lugares por defecto. |
| `navigation` | Destinos y grafo de Navigation-Compose. |
| `phrase` | La oración acumulada: agregar, borrar, limpiar. Es la regla central del producto. |
| `tts` | Envoltorio del motor de voz de Android y su ciclo de vida. |
| `validacion` | Mide la latencia entre pulsación y voz. Inerte fuera de una build de depuración. |
| `ui.*` | Compose y Material 3: pantallas, componentes, iconos dibujados a mano, tema. |

## A.3 Diagrama de capas

```
        ┌──────────────────────────────────────────┐
        │  ui.*        Compose, Material 3          │
        │              pantallas y componentes      │
        └──────────────┬───────────────────────────┘
                       │ observa estado
        ┌──────────────▼───────────────────────────┐
        │  ui.AppViewModel · board.BoardViewModel   │
        │  phrase.PhraseController                  │
        │              la oración acumulada         │
        └───────┬───────────────────────┬───────────┘
                │                       │
    ┌───────────▼──────────┐  ┌─────────▼───────────────────┐
    │  tts.TtsManager      │  │  avatar.catalog             │
    │  voz del sistema     │  │  AvatarCatalog (fachada)    │
    └──────────────────────┘  │   ├─ assets  (capa base)    │
                              │   ├─ Room    (capa encima)  │
                              │   └─ avatarPoseFor (respaldo)│
                              └─────────┬───────────────────┘
                                        │ AvatarPose
                              ┌─────────▼───────────────────┐
                              │  avatar.AvatarFigureRenderer│
                              │  Canvas de Compose          │
                              └─────────────────────────────┘

    data.seed ──► data.model ──► ui.*        (vocabulario)
    data.repository ◄──► DataStore           (preferencias)
```

### La flecha que importa

Las tres ramas del catálogo —base, encima y respaldo— son el núcleo del diseño.
**El respaldo es por registro, no por archivo**: si una sola animación falta o
está rota, esa animación vuelve al motor procedural y el resto del catálogo sigue
en uso. Es lo que hace que la app no pueda quedarse muda por un error de tipeo en
un JSON.

## A.4 Tamaño de los archivos principales

| Archivo | Líneas |
|---|---|
| `avatar/AvatarFigureRenderer.kt` | ~1.480 |
| `avatar/AvatarParts.kt` | 461 |
| `ui/settings/AvatarTab.kt` | 424 |
| `avatar/AvatarAction.kt` | ~430 |
| `test/…/PoseCatalogDumpTest.kt` | 372 |

El renderizador concentra el 30 % del código de la app. Es esperable: dibuja una
figura humana completa, 85 objetos y 16 escenarios con primitivas vectoriales,
sin una sola imagen.
