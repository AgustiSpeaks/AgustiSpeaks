# Cifras estáticas — lo que se responde leyendo el código

Fase 1 del plan de evidencia. Todo lo de este archivo sale de leer archivos de
configuración y de código fuente. **No se ejecutó nada**: ni build, ni pruebas.

Fecha de lectura: 2026-09-20.

---

## 0. Tres hallazgos previos que condicionan todo lo demás

Los pongo primero porque cambian cómo hay que leer el resto del plan.

### 0.1 El catálogo empaquetado no existe en este momento

`app/src/main/assets/` existe pero **está vacío**. El archivo `animaciones.json`
no está.

Lo borré yo a propósito en la sesión anterior, para que la prueba de volcado lo
regenerara con las 88 acciones. Esa prueba **todavía no se corrió**. Consecuencias:

- La app que corriste en el teléfono está funcionando **al 100% por el camino de
  respaldo procedural** (`avatarPoseFor`), no por el catálogo. Funcionó bien, lo
  cual es en sí mismo evidencia de que el respaldo por registro hace lo que dice
  que hace — pero no es el modo que la tesis describe como normal.
- Toda cifra que dependa del catálogo (fotogramas, tamaño del JSON, reglas de
  disparo en el catálogo) **no se puede medir hasta regenerarlo**.
- `PoseCatalogDumpTest` tiene que ser la primera prueba que corras, antes que
  cualquier otra del plan.

### 0.2 El historial de git arranca hoy, no antes

Cuando escribí este documento no existía `.git` en el proyecto. **Se inicializó el
2026-09-20**, después de esa lectura, con git 2.55.0:

| | |
|---|---|
| Commit inicial | `a8f137e00bddaec073e5d5a102545592fd963acb` |
| Rama | `main` |
| Autor | FelipeD05M \<felipeandresdume@gmail.com\> |
| Archivos versionados | 88 |

Lo que esto **sí** resuelve: de acá en adelante cada medición puede citarse contra
un commit, y la columna "commit" de la bitácora del Apéndice G tiene contenido real.

Lo que **no** resuelve: el historial no se inventa hacia atrás. Todo el desarrollo
anterior a hoy —incluido el refactor del catálogo— queda comprimido en ese único
commit inicial. La pregunta de 279 contra 291 (sección 4) sigue sin poder
responderse por historial, y la respuesta de esa sección no cambia.

### 0.3 No existe `ValidacionTest.kt`, ni `docs/`, ni `androidTest/`

Busqué en todo el proyecto. El `ValidacionTest.kt` que mencionás no está en ningún
lado: no hay nada que adaptar, hay que escribirlo de cero. Tampoco existe el
directorio `app/src/androidTest/`; si hacemos la prueba de Room habrá que crearlo
junto con su dependencia de test, que hoy no está declarada en `build.gradle.kts`.

---

## 1. Versiones

Fuentes: `gradle/libs.versions.toml` y `app/build.gradle.kts`.

| Componente | Versión | Dónde está declarada |
|---|---|---|
| Android Gradle Plugin | 8.7.3 | `libs.versions.toml` → `agp` |
| Kotlin | 2.1.0 | `libs.versions.toml` → `kotlin` |
| KSP | 2.1.0-1.0.29 | `libs.versions.toml` → `ksp` |
| Compose BOM | 2024.12.01 | `libs.versions.toml` → `composeBom` |
| Material 3 | **la fija el BOM** | `androidx-material3`, sin `version.ref` |
| Navigation Compose | 2.8.5 | `libs.versions.toml` → `navigationCompose` |
| Room (runtime, ktx, compiler) | 2.6.1 | `libs.versions.toml` → `room` |
| DataStore Preferences | 1.1.1 | `libs.versions.toml` → `datastore` |
| Lifecycle (runtime, compose, viewmodel) | 2.8.7 | `libs.versions.toml` → `lifecycle` |
| Activity Compose | 1.9.3 | `libs.versions.toml` → `activityCompose` |
| Core KTX | 1.15.0 | `libs.versions.toml` → `coreKtx` |
| Coroutines (Android) | 1.9.0 | `libs.versions.toml` → `kotlinxCoroutines` |
| JUnit | 4.13.2 | `libs.versions.toml` → `junit` |
| org.json (solo pruebas JVM) | 20240303 | `libs.versions.toml` → `json` |
| compileSdk | 35 | `app/build.gradle.kts` |
| minSdk | 26 | `app/build.gradle.kts` |
| targetSdk | 35 | `app/build.gradle.kts` |
| Java source/target | 17 | `app/build.gradle.kts` |
| JVM target de Kotlin | 17 | `app/build.gradle.kts` |

**Sobre Material 3 y el resto de Compose:** el proyecto usa un BOM (*Bill of
Materials*). Las bibliotecas de Compose se declaran **sin número de versión**: el
BOM 2024.12.01 decide cuál corresponde a cada una. El número concreto de Material 3
no está escrito en ningún archivo del repositorio y solo aparece en el árbol de
dependencias ya resuelto.

Si la tesis necesita ese número exacto, hay que obtenerlo de Gradle, y eso es
ejecutar algo. Te dejo el comando para Android Studio (Terminal integrada, que sí
tiene el entorno):

```
gradlew app:dependencies --configuration debugRuntimeClasspath
```

Buscá la línea `androidx.compose.material3:material3`. **No lo corrí.** Hasta
entonces, la forma honesta de ponerlo en la tesis es "Material 3, versión
gestionada por Compose BOM 2024.12.01".

**Dato relevante para el capítulo de arquitectura:** `org.json` está declarada como
`testImplementation`, no como `implementation`. Es decir, **no entra en el APK**.
En Android `org.json` viene en el sistema operativo; en las pruebas locales de la
JVM es un stub que lanza "not mocked", y por eso hace falta la implementación real
solo para poder probar el parser sin dispositivo. La app no suma ninguna
dependencia de terceros para leer el catálogo.

---

## 2. Modelo de pose

### 2.1 La discrepancia: el número equivocado es el 18

`AvatarPose` (en `app/src/main/java/.../avatar/AvatarPose.kt`) tiene **21 campos**.

| Cifra de tu documentación | Real | Veredicto |
|---|---|---|
| 18 campos | **21** | **incorrecta** |
| 12 continuos | 12 | correcta |
| 9 discretos | 9 | correcta |

12 + 9 = 21, y 21 es exactamente el número de campos. **Ningún campo queda fuera de
los dos grupos**: la partición es completa y sin solapamiento. El intérprete no
ignora nada de la pose.

Esto último no es casualidad y vale la pena decirlo en la tesis: que la unión de
`FloatField` y `StepField` cubra `AvatarPose` entero es lo que garantiza que una
animación del catálogo pueda expresar cualquier pose que el motor procedural sea
capaz de producir. Si un campo quedara afuera, habría poses irrepresentables como
datos, y la migración no podría ser completa ni siquiera en principio.

### 2.2 Los 12 campos continuos (se interpolan)

Definidos en el enum `FloatField`, en `avatar/catalog/AnimationRecord.kt`.

| # | Campo | Clave JSON | Valor por defecto | Qué controla |
|---|---|---|---|---|
| 1 | `bob` | `bob` | 0 | rebote vertical del cuerpo |
| 2 | `lean` | `lean` | 0 | inclinación del cuerpo |
| 3 | `headTilt` | `headTilt` | 0 | giro de la cabeza, en grados |
| 4 | `jaw` | `jaw` | 0 | apertura de la boca |
| 5 | `tears` | `tears` | 0 | lágrimas |
| 6 | `eyeClose` | `eyeClose` | 0 | cierre de los párpados |
| 7 | `crouch` | `crouch` | 0 | de pie (0) a agachado o sentado (1) |
| 8 | `legSwing` | `legSwing` | 0 | paso al caminar o correr |
| 9 | `armSwing` | `armSwing` | 0 | balanceo natural de los brazos |
| 10 | `reach` | `reach` | 0 | extensión del brazo activo |
| 11 | `propPhase` | `propPhase` | 0 | fase del objeto, independiente del brazo |
| 12 | `fxPulse` | `fxPulse` | **1** | latido del símbolo de apoyo |

`fxPulse` es el único continuo cuyo valor por defecto no es 0. Tiene sentido: un
símbolo sin animación declarada se muestra entero, no invisible.

### 2.3 Los 9 campos discretos (se sostienen)

Definidos en el enum `StepField`, mismo archivo. Toman el valor del último
fotograma que los fijó y lo conservan hasta que otro lo cambie.

| # | Campo | Clave JSON | Tipo | Valor por defecto |
|---|---|---|---|---|
| 1 | `stance` | `stance` | `Stance` | `STAND` |
| 2 | `armL` | `armL` | `ArmPose` | `DOWN` |
| 3 | `armR` | `armR` | `ArmPose` | `DOWN` |
| 4 | `expression` | `expression` | `Expression?` | `null` |
| 5 | `prop` | `prop` | `Prop?` | `null` |
| 6 | `fx` | `fx` | `Fx` | `NONE` |
| 7 | `handTarget` | `handTarget` | `BodyPart?` | `null` |
| 8 | `highlight` | `highlight` | `BodyPart?` | `null` |
| 9 | `sleeping` | `sleeping` | `Boolean` | `false` |

### 2.4 Un detalle del formato que conviene citar en la tesis

En el mapa `steps` de un fotograma, **que una clave esté presente con valor `null`
no es lo mismo que que la clave falte**:

- presente con `null` → apaga el campo (por ejemplo, borra la expresión de la cara);
- ausente → "no opino, dejá lo que había".

Sin esa distinción no habría forma de apagar nada a mitad de un ciclo. Es el tipo de
decisión de diseño de formato que un tribunal puede preguntar y que no se deduce
mirando el JSON.

---

## 3. Resolvedor: cuántas reglas hay y dónde

Tu documento dice "aproximadamente sesenta". El número real es bastante mayor.

| Ubicación | Reglas de disparo | Observación |
|---|---|---|
| Catálogo (`animaciones.json`) | **4** | ASK, PLEASE, GOOD, BAD |
| Código (`ActionResolver.resolve`) | **118** | incluye las 4 anteriores, duplicadas a propósito |
| **Distintas en total** | **118** | el catálogo no agrega ninguna regla nueva |

Método de conteo: una "regla de disparo" es **una rama que devuelve una acción**,
es decir un `return R(...)` dentro de `resolve()`. Hay 119 en el archivo; la número
119 es `return R(AvatarAction.TALK)`, el caso de respaldo cuando nada coincidió, que
no cuento como regla. Aparte hay un guardia previo, `return ResolvedAction(IDLE)`
para la frase vacía.

### 3.1 Desglose por sección

Las secciones son las del propio archivo, y su orden **es** el orden de evaluación.

| Sección | Contenido | Reglas |
|---|---|---|
| 0 | Social y estado (va primera por el choque "cómo"/"como") | 4 |
| 1 | Bebidas específicas | 6 |
| 2 | Higiene y cuidado | 5 |
| 3 | Salud | 7 |
| 3.5 | Alimentos (delega en `foodProp`) | 1 |
| 3.6 | Ropa (delega en `garment`) | 1 |
| 3.7 | Compras y escuela | 9 |
| 3.8 | Juego | 5 |
| 3.9 | Tiempo y cielo | 7 |
| 3.10 | Sueltos (jabón, toalla, curita) | 3 |
| 3.11 | Lugares | 8 |
| 3.12 | Familia y gente de la escuela | 11 |
| 4 | Acciones con verbo | 27 |
| 5 | Nombrar una parte del cuerpo, sin verbo | 1 |
| 6 | Social | 6 |
| 7 | Respuestas y cantidad | 5 |
| 8 | Emociones | 10 |
| 9 | Pronombres (última, y nunca antes) | 2 |
| — | Respaldo: `TALK` | (1) |
| | **Total de reglas** | **118** |

### 3.2 Tablas auxiliares

Tres funciones privadas no eligen la *acción* sino el *matiz* (qué objeto, qué parte
del cuerpo). Las cuento aparte porque son otra cosa, pero suman al tamaño real del
vocabulario reconocido:

| Tabla | Ramas | Qué elige |
|---|---|---|
| `foodProp` | 15 | qué alimento se dibuja en la mano |
| `garment` | 7 | qué prenda y a qué parte del cuerpo va |
| `bodyPart` | 17 | qué parte del cuerpo nombra la frase |

Sumadas a las 118 reglas, el resolvedor toma **157 decisiones léxicas distintas**.

### 3.3 Por qué solo 4 reglas están en el catálogo

Las 4 migradas son exactamente las de la sección 0, y se eligieron por una razón
estructural, no por conveniencia: **ya se evaluaban antes que todas las demás**.
El catálogo también se consulta antes que el resolvedor. Mover al catálogo reglas
que ya eran las primeras no puede alterar el orden de evaluación de ninguna otra.

Llevan prioridad explícita 0, 1, 2 y 3 para reproducir el orden en que están
escritas en el código. Dejarlas todas en la misma prioridad no alcanzaría: el
desempate sería el orden del enum, donde `ASK` va última, y entonces la frase
"¿cómo estás? bien" caería en `GOOD` en vez de en `ASK`.

Las 4 siguen existiendo también en `ActionResolver`. Si el catálogo no carga —que
es exactamente la situación descrita en la sección 0.1 de este documento— esas
frases se resuelven igual que siempre.

**Para la tesis:** la afirmación defendible es que la migración de disparadores
está *demostrada pero no completada*: 4 de 118, el 3,4 %. El mecanismo funciona; el
trabajo de mover las otras 114 no se hizo.

---

## 4. Historia de fotogramas: 279 contra 291

### 4.1 Lo que no se puede determinar

**El historial de git empieza hoy** (ver sección 0.2). No puedo reconstruir qué
versión tenía 279 y cuál 291, porque no existe historial anterior al commit
inicial: todo el desarrollo previo entró comprimido en un solo commit. Lo dejo
dicho tal cual, que es lo que pediste.

### 4.2 Lo que sí encontré, y qué vale como evidencia

Sobrevive un artefacto de compilación: `app/build/animaciones.json`, con fecha de
modificación **2026-09-19 22:42** y **28.285 bytes**. Lo leí y conté:

| Medición sobre `app/build/animaciones.json` | Valor |
|---|---|
| Registros (`"id"`) | 66 |
| Fotogramas (`{ "t": ... }`) | **291** |
| Registros con generador procedural | 12 |
| Tamaño | 28.285 bytes |

Los 66 registros lo fechan: es de **antes** de las 22 acciones nuevas de esta
etapa. Es decir, es el catálogo tal como quedó **al cierre del refactor**.

Conclusión que sostengo: **291 es la cifra de cierre del refactor del catálogo**, y
279 corresponde a una iteración anterior del proceso de reducción. El artefacto en
disco coincide exactamente con 291 sobre exactamente 66 acciones.

Conclusión que **no** sostengo: no puedo probar *en qué momento* se midió 279, ni
descartar que 279 haya sido una medición correcta de otra variante del generador.
Sin historial, esto es un indicio fuerte, no una prueba.

### 4.3 Un detalle que corrige tu figura 3

La serie que tenías anotada para la figura 3 es 1.088 → 1.010 → 736 → valor final.
Falta un punto: hubo una medición de **279** entre 736 y 291. Y la subida de 279 a
291 no es un empeoramiento por descuido: **agregar fotogramas fue el precio de que
algunas curvas quedaran exactas**. La serie real tiene una cola que sube, y esa cola
es parte del hallazgo, no ruido.

Con los datos disponibles hoy, la serie honesta es:

```
1.088  →  1.010  →  736  →  279  →  291
```

con la nota de que el orden entre 279 y 291 es inferido del artefacto en disco y no
verificable por historial. El punto final de las 88 acciones todavía no existe: sale
de correr `PoseCatalogDumpTest`.

### 4.4 Advertencia sobre los 12 procedurales

El artefacto viejo tiene **12** registros procedurales. `ProceduralPoses.byAction`
hoy tiene **11**. La diferencia es esperable —en esta etapa lavarse las manos,
cepillarse los dientes y bañarse salieron de la lista procedural, y antes había
otras— pero es otra cifra que **no debe copiarse del artefacto viejo a la tesis**.
La cifra válida sale de la prueba de inventario de la Fase 2.

---

## 5. Verificación cruzada del vocabulario

Esto lo vas a medir con la prueba de la Fase 2. Lo cuento acá **a mano, leyendo
`SeedData.kt`**, a propósito: si la prueba da lo mismo, tenés dos mediciones
independientes de la misma cifra, que es justo lo que conviene poder afirmar en una
tesis. Si da distinto, hay un error en la prueba y lo encontramos antes de publicar.

| Grupo | Cantidad |
|---|---|
| Lugares | 8 |
| Categorías temáticas | 15 |
| Palabras núcleo (categoría `nucleo`) | 25 |
| Pictogramas temáticos | 145 |
| **Total de pictogramas** | **170** |

Desglose de los 145 temáticos:

| Categoría | Pictogramas |
|---|---|
| alimentos | 14 |
| bebidas | 5 |
| acciones | 12 |
| sentimientos | 9 |
| dolor | 12 |
| cuerpo | 12 |
| ropa | 8 |
| higiene | 8 |
| tiempo | 9 |
| familia | 9 |
| juego | 10 |
| lugares | 10 |
| escuela | 11 |
| compras | 6 |
| social | 10 |
| **Total** | **145** |

Nota metodológica: las palabras núcleo tienen `placeIds` vacío porque no pertenecen
a ningún lugar — están siempre visibles en todos. Un pictograma temático aparece en
todo lugar cuya lista de categorías incluya su categoría, así que **el mismo
pictograma se muestra en varios lugares**. Si una prueba recorre lugares y luego
categorías, va a contar repetidos. La cifra 170 es de pictogramas *distintos*.

---

## 6. Apéndice A — Estructura del proyecto

61 archivos Kotlin: 57 en `main`, 4 en `test`. Ninguno en `androidTest`.

### 6.1 Árbol de paquetes

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

### 6.2 Qué hace cada paquete

| Paquete | Archivos | Líneas | Descripción |
|---|---|---|---|
| (raíz) | 2 | 111 | `AgustiSpeaksApplication` carga el catálogo base al arrancar; `MainActivity` fija la orientación horizontal y monta el grafo. |
| `avatar` | 12 | 3.328 | El motor: acciones, pose, resolvedor léxico, renderizadores de figura y de símbolos, exportador y codificador GIF. |
| `avatar.catalog` | 6 | 901 | Modelo del registro, intérprete de fotogramas, parser JSON tolerante, almacén Room, fachada de runtime y generadores procedurales. |
| `data.model` | 6 | 135 | Lugar, categoría, pictograma, configuración del avatar, preferencias y tamaño de texto. |
| `data.repository` | 1 | 146 | Lectura y escritura de preferencias sobre DataStore. |
| `data.seed` | 1 | 303 | Los 170 pictogramas, 15 categorías y 8 lugares por defecto. |
| `navigation` | 2 | 80 | Destinos y grafo de Navigation-Compose. |
| `phrase` | 2 | 48 | La oración acumulada: agregar, borrar, limpiar. Es la regla central del producto. |
| `tts` | 2 | 164 | Envoltorio del motor de voz de Android y su ciclo de vida. |
| `ui` | 2 | 92 | ViewModel raíz y *CompositionLocals* de la app. |
| `ui.board` | 2 | 228 | Pantalla del tablero y su ViewModel. |
| `ui.components` | 5 | 413 | Tarjeta de categoría, barra de frase, panel, botón de pictograma, grilla y superposición de habla. |
| `ui.icons` | 1 | 240 | Iconos dibujados a mano, sin recursos de imagen. |
| `ui.placeselection` | 2 | 153 | Elección de lugar al entrar. |
| `ui.recents` | 1 | 181 | Frases recientes. |
| `ui.settings` | 4 | 867 | Configuración, con la pestaña de personalización del avatar. |
| `ui.theme` | 3 | 247 | Color, tema y tipografía. |
| `ui.util` | 1 | 22 | Utilidades de disposición. |
| `test` | 4 | 735 | Volcado del catálogo, paridad, fusión de capas y cobertura de pictogramas. |

### 6.3 Los cinco archivos más grandes

Útiles para justificar por qué los fragmentos de los apéndices van recortados.

| Archivo | Líneas |
|---|---|
| `avatar/AvatarFigureRenderer.kt` | 1.458 |
| `avatar/AvatarParts.kt` | 461 |
| `ui/settings/AvatarTab.kt` | 424 |
| `avatar/AvatarAction.kt` | 422 |
| `test/.../PoseCatalogDumpTest.kt` | 372 |

### 6.4 Diagrama de capas

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
    ┌───────────▼──────────┐  ┌─────────▼──────────────────┐
    │  tts.TtsManager      │  │  avatar.catalog             │
    │  voz del sistema     │  │  AvatarCatalog (fachada)    │
    └──────────────────────┘  │   ├─ assets  (capa base)    │
                              │   ├─ Room    (capa encima)  │
                              │   └─ avatarPoseFor (respaldo)│
                              └─────────┬──────────────────┘
                                        │ AvatarPose
                              ┌─────────▼──────────────────┐
                              │  avatar.AvatarFigureRenderer│
                              │  Canvas de Compose          │
                              └────────────────────────────┘

    data.seed ──► data.model ──► ui.*        (vocabulario)
    data.repository ◄──► DataStore           (preferencias)
```

La flecha que importa para la tesis es la de tres ramas del catálogo: base, encima
y respaldo. **El respaldo es por registro, no por archivo**: si una sola animación
falta o está rota, esa animación vuelve al motor procedural y el resto del catálogo
sigue en uso. Es lo que hace que la app no pueda quedarse muda por un error de
tipeo en un JSON.

---

## 7. Qué queda pendiente de esta fase

| Cifra | Cómo se obtiene |
|---|---|
| Versión exacta de Material 3 | `gradlew app:dependencies` en la Terminal de Android Studio |
| Fotogramas del catálogo de 88 acciones | `PoseCatalogDumpTest` (Fase 2) |
| Tamaño en bytes del catálogo actual | idem |
| Cuál versión tenía 279 fotogramas | **no determinable**: el historial arranca hoy |
