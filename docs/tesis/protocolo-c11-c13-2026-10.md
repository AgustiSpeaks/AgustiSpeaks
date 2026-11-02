# Protocolo de evidencia reproducible — criterios 11 y 13 (segundo intento)

Segundo intento de los criterios 11 (funcionamiento sin red) y 13
(persistencia). El primero sigue en la bitácora tal como se registró; este se
agrega como un intento nuevo, con su fecha, su commit y su evidencia.

Dispositivo: Samsung Galaxy A57, Android 16 (API 36).

## Por qué hay un segundo intento

Comentarios de la profesora guía sobre el primer intento:

- Criterio 11: *"Evitar sostener un cumplimiento del 100 % cuando parte de las
  funciones se respalda únicamente en declaración del autor; incorporar
  evidencia reproducible o ajustar el alcance de la conclusión."*
- Criterio 13: *"Generar evidencia que permita distinguir explícitamente el
  estado antes del cierre y después de reabrir la aplicación."*

En el primer intento del criterio 11 se apoyaban solo en la declaración del
autor la voz (F2 y el "decir" de F8), la foto en un pictogift (F7), el
"agregar" de Favoritos (F6) y el uso de la cámara en F8
(`evidencia-2026-10/c11-sin-red.md`).

**En este intento, ninguna función depende de la palabra del autor.** Cada una
queda respaldada por al menos un archivo generado por el teléfono o por la
computadora: salidas de `adb`, capturas tomadas por `adb` y una grabación
continua de la pantalla con audio.

## Qué versión se mide, y qué cambia por eso

**La versión 1.1, la actual**: el último commit que tocó la app,
`git log --oneline -1 -- app`, en la carpeta del proyecto. **Toda la sesión se
hace en modo edición.** Lo decidió el autor el 2026-11-01.

El primer intento midió la versión 1.0 (`c106b6e`). Entre las dos se agregó el
modo edición, con su contraseña, y se quitó "Agregar foto" de la tarjeta de
«Decir». Por eso, en el criterio 11:

- **F7 se reemplaza por su equivalente en la 1.1.** La función de la 1.0 ya no
  existe. En la 1.1, la foto se le pone a un pictograma en modo edición, y el
  avatar la señala en «Decir» cuando la frase lo nombra.
- **F8 se hace desde el modo edición**: crear con "Agregar" y borrar con el
  lápiz.
- **Se agregan F10, F11 y F12**, las funciones nuevas de la 1.1. Sin ellas, "el
  100 % de las funciones" dejaría afuera parte de la app medida.

El umbral no cambia: el 100 % de las funciones en modo avión.

**Consecuencia para la tesis.** Los criterios 11 y 13 de este intento quedan
medidos sobre la 1.1. Los demás siguen medidos sobre la 1.0 (`c106b6e` y
`6caa5e3`). La tesis tiene que decir qué versión mide cada criterio.

---

## Preparación (una sola vez)

### A. La build de depuración de la 1.1 en el teléfono

La instrumentación de la voz y `run-as` **solo funcionan en la build de
depuración**, la que instala ▶ Run. La APK firmada que se generó para enviar
no sirve: no registra la voz, y `run-as` no puede leerla.

Para saber cuál está instalada:

```powershell
$adb = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"
& $adb shell "dumpsys package com.tunombre.agustispeaks | grep pkgFlags"
```

- Si la línea dice **`DEBUGGABLE`**, está lista.
- Si no, **desinstalá AgustiSpeaks del teléfono** y, en Android Studio, con
  `C:\Users\Felip\AgustiSpeaks` abierto, dale **▶ Run**. Desinstalar borra lo
  guardado en la app; al abrirla va a pedir crear la contraseña.

### B. Una contraseña de prueba

La grabación marca cada toque en la pantalla, también los del teclado: **la
contraseña que se escriba va a quedar en el video.** Antes de la sesión, en
Ajustes → Configuraciones → **Cambiar contraseña**, poné una de prueba (por
ejemplo `prueba2026`). Después de la sesión, cambiala de nuevo por la tuya.

### C. scrcpy, para grabar la pantalla con audio

```powershell
winget install --id Genymobile.scrcpy
```

Graba la pantalla del teléfono **con el sonido de la app**, sin límite de
tiempo. `adb shell screenrecord` no sirve acá: no graba audio y corta a los 3
minutos.

### D. El teléfono, antes de cada sesión

1. **Modo avión** activado, con **Wi-Fi y Bluetooth apagados**.
2. **No molestar** activado: que ninguna notificación aparezca en la
   grabación.
3. **Volumen multimedia** alto: la voz tiene que quedar en el audio.
4. **Neutralizar el selector de fotos.** Abrí AgustiSpeaks y sacá **12 capturas
   de pantalla** de la pantalla de lugares. Después, con la cámara del
   teléfono, sacá **una foto de un objeto**: sin personas, sin papeles, sin
   pantallas. El selector muestra primero lo más nuevo: así, en la grabación,
   la primera pantalla de la galería tiene solo esa foto y capturas de la app.
   Es lo que expuso datos personales en el primer intento.
5. Cerrá AgustiSpeaks.

---

## Criterio 11 — Funcionamiento sin red

**Umbral (sin cambios):** 100 % de las funciones en modo avión.

### Las funciones

| # | Función | Respecto del primer intento |
|---|---|---|
| F1 | Abrir la app y elegir un lugar | igual |
| F2 | Tocar pictogramas: suena la voz y la oración crece en la barra | igual |
| F3 | Navegar entre categorías | igual |
| F4 | «Decir»: la tarjeta con el avatar animado | igual |
| F5 | Recientes: ver una frase dicha y volver a abrirla | igual |
| F6 | Favoritos: agregar una frase; quitarla y recuperarla con «Deshacer» | igual |
| F7 | Foto en un pictograma: ponerle a un pictograma de la app una foto de la galería, y que el avatar la señale en «Decir»; después, volver al original | **reemplaza** a "«Agregar foto» en la tarjeta de «Decir»", que no existe en la 1.1 |
| F8 | Pictograma propio: crearlo con la cámara, decirlo y borrarlo | igual, desde el modo edición |
| F9 | Ajustes: cambiar el avatar y la voz, y ver el cambio | igual |
| F10 | Modo edición: entrar con la contraseña, y salir con la X | **nueva** en la 1.1 |
| F11 | Agregar un lugar y, dentro, una categoría, con fotos; borrar el lugar | **nueva** en la 1.1 |
| F12 | Ocultar un pictograma de la app y volver a mostrarlo | **nueva** en la 1.1 |

### 1. Variables y funciones de ayuda

En una ventana de **PowerShell** (puede ser la Terminal de Android Studio):

```powershell
$adb = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"
$pkg = "com.tunombre.agustispeaks"
$ev  = "C:\Users\Felip\AgustiSpeaks\docs\tesis\evidencia-2026-10"
[Console]::OutputEncoding = [Text.Encoding]::UTF8

# Estado de la red, con la hora del teléfono.
function Red($nombre) {
  & {
    "== commit medido: git log --oneline -1 -- app =="
    git -C C:\Users\Felip\AgustiSpeaks log --oneline -1 -- app
    "== hora del telefono =="
    & $adb shell date
    "== modo avion: settings global airplane_mode_on (1 = activado) =="
    & $adb shell settings get global airplane_mode_on
    "== modo avion: cmd connectivity airplane-mode =="
    & $adb shell "cmd connectivity airplane-mode 2>&1"
    "== Wi-Fi: cmd wifi status =="
    & $adb shell "cmd wifi status 2>&1 | head -3"
    "== Bluetooth: settings global bluetooth_on (0 = apagado) =="
    & $adb shell settings get global bluetooth_on
    "== red activa: dumpsys connectivity (solo red por defecto y redes conectadas) =="
    & $adb shell "dumpsys connectivity | grep -E 'Active default network|NetworkAgentInfo' 2>&1"
    "== prueba de salida: ping a 8.8.8.8 =="
    & $adb shell "ping -c 1 -W 2 8.8.8.8 2>&1"
    "== motor de voz: settings secure tts_default_synth =="
    & $adb shell settings get secure tts_default_synth
    "== build instalada: version y si es de depuracion =="
    & $adb shell "dumpsys package $pkg | grep -E 'versionCode|versionName|pkgFlags' 2>&1"
  } | Out-File -Encoding utf8 "$ev\c11-v2-red-$nombre.txt"
}

# Fotos guardadas por la app, con fecha y tamaño. Solo funciona en depuración.
function Fotos($nombre) {
  & $adb shell "run-as $pkg ls -la files/pictogifts/ 2>&1" |
    Out-File -Encoding utf8 "$ev\c11-v2-fotos-$nombre.txt"
}

# Captura de la pantalla del teléfono, guardada directo en la evidencia.
# Va por adb, así que no queda en la galería del teléfono.
function Cap($nombre) {
  & $adb shell screencap -p /sdcard/cap-tesis.png
  & $adb pull /sdcard/cap-tesis.png "$ev\c11-v2-$nombre.png" | Out-Null
  & $adb shell rm /sdcard/cap-tesis.png
}
```

### 2. Antes de empezar

```powershell
& $adb devices
Red "inicio"
& $adb shell am force-stop $pkg
& $adb logcat -G 16M
& $adb logcat -c
Fotos "0-inicio"
```

`logcat -G 16M` agranda el registro del teléfono. Con el tamaño de fábrica,
en una sesión de varios minutos —con la cámara abierta en el medio— las
primeras filas de la voz se podían pisar antes de copiarlas.

### 3. Empezar la grabación

En **otra** ventana de PowerShell. Queda corriendo hasta el final de la sesión:

```powershell
$env:ADB = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"
scrcpy --record="C:\Users\Felip\AgustiSpeaks\docs\tesis\evidencia-2026-10\c11-v2-sesion.mp4" `
  --max-size=1280 --video-bit-rate=700K --max-fps=15 --audio-bit-rate=64K `
  --audio-dup --show-touches
```

- `$env:ADB` hace que scrcpy use el mismo `adb` que los comandos, sin reiniciar
  la conexión.
- `--show-touches` marca cada toque con un punto: la grabación muestra qué se
  tocó.
- `--audio-dup` deja el sonido también en el teléfono.
- La resolución y la tasa bajas son para que el archivo entre en GitHub, que
  no acepta archivos de más de 100 MB.

### 4. Las funciones, en este orden

Hacé todo **en el teléfono**, despacio. Los comandos van en la primera ventana,
en el momento indicado. La grabación corre todo el tiempo.

| Paso | En el teléfono | En la computadora |
|---|---|---|
| F1 | Abrí AgustiSpeaks desde el ícono | `Cap "F1a-abierta"` |
| F10 | ⚙ → **Configuraciones** → **Entrar al modo edición** → la contraseña de prueba → **Entrar** | `Cap "F10a-modo-edicion-activo"` |
| — | Volvé a la pantalla de lugares: arriba aparece la franja del modo edición | — |
| F1 | Elegí **Colegio** | `Cap "F1b-colegio"` |
| F2 | Panel Palabras: tocá **Yo** y **Quiero**. Categorías → **Bebidas** → **Agua**. Esperá a que termine de hablar entre toque y toque | `Cap "F2-yo-quiero-agua"` |
| F3 | ← del panel derecho → **Juego** → ← → **Bebidas** → ← | `Cap "F3-categorias"` |
| F4 | Tocá **«Decir»** (el parlante de la barra) y esperá a que hable | `Cap "F4-decir"` |
| — | **Listo** | — |
| F5 | Ícono de **Recientes** → tocá **Yo Quiero Agua** | `Cap "F5-recientes"` |
| F6 | En esa tarjeta: **Agregar a favoritos** → **Listo** → pestaña **Favoritos** | `Cap "F6a-favorito-agregado"` |
| F6 | Tocá la **estrella** de la frase para quitarla | `Cap "F6b-quitado"` (antes de 5 s) |
| F6 | Tocá **Deshacer** | `Cap "F6c-recuperado"` |
| — | Volvé al tablero de Colegio. La barra tiene que seguir diciendo "Yo Quiero Agua" | — |
| F7 | Categorías → **Bebidas** → el **lápiz** de **Agua** → **Elegir de la galería** → la foto del objeto → **Guardar** | `Cap "F7a-agua-con-foto"` y después `Fotos "1-tras-F7"` |
| F7 | Tocá **«Decir»**: el avatar tiene que señalar la foto | `Cap "F7b-decir-senala-foto"` |
| F7 | **Listo** → el **lápiz** de **Agua** → **Volver al original** | `Cap "F7c-agua-original"` y después `Fotos "2-tras-volver-F7"` |
| F8 | ← → **Juego** → **Agregar** → nombre del objeto (que no sea una palabra de la app) → **Sacar una foto** → fotografiá el objeto → **Guardar** | `Cap "F8a-creado"` y después `Fotos "3-tras-crear-F8"` |
| F8 | Tocá la **X** de la barra (borrar la frase) y después el pictograma nuevo: tiene que decir su nombre | `Cap "F8b-dicho"` |
| F8 | El **lápiz** del pictograma nuevo → **Borrar** → confirmá | `Cap "F8c-borrado"` y después `Fotos "4-tras-borrar-F8"` |
| F12 | ← → **Bebidas** → el **lápiz** de **Jugo** → **Ocultar**: queda atenuado, con un ojo tachado | `Cap "F12a-jugo-oculto"` |
| F12 | El **ojo tachado** de **Jugo** → **Mostrar** | `Cap "F12b-jugo-visible"` |
| F11 | Flecha de arriba a la izquierda (salir de Colegio) → **Agregar lugar** → nombre "Lugar de prueba" → **Elegir de la galería** → la foto del objeto → **Guardar** | `Cap "F11a-lugar-creado"` y después `Fotos "5-tras-crear-lugar"` |
| F11 | Entrá a **Lugar de prueba** → **Agregar categoría** → nombre "Categoría de prueba" → **Elegir de la galería** → la foto → **Guardar** | `Cap "F11b-categoria-creada"` y después `Fotos "6-tras-crear-categoria"` |
| F11 | Flecha de arriba a la izquierda → el **lápiz** de **Lugar de prueba** → **Borrar** → confirmá | `Cap "F11c-lugar-borrado"` y después `Fotos "7-tras-borrar-lugar"` |
| F9 | ⚙ → Avatar → **Identidad** → **Robot**. Pestaña **Voz** → bajá la **Velocidad** | `Cap "F9a-cambiado"` |
| F9 | Volvé a **Persona** y a la velocidad que estaba | `Cap "F9b-restaurado"` |
| F10 | Volvé a la pantalla de lugares → la **X** de la franja del modo edición | `Cap "F10b-fuera-del-modo-edicion"` |

**Si algo no funciona**, no lo repitas con red ni "para ver si era eso".
Anotá el paso y seguí con el siguiente: el resultado es lo que pasó.

### 5. Terminar

1. Cerrá la ventana de scrcpy (o `Ctrl+C` en su PowerShell): así se termina de
   escribir el video.
2. En la primera ventana:

```powershell
Red "fin"
& $adb logcat -d -v threadtime -s VALIDACION:I |
  Out-File -Encoding utf8 "$ev\c11-v2-voz-logcat.txt"
Get-Item "$ev\c11-v2-*" | Select-Object Name, Length
```

La última línea lista los archivos con su tamaño. **Si el video pasa de
95 MB, no lo agregues a git**: avisame y lo resolvemos.

### 6. Qué respalda cada función

| # | Respaldo (archivo) | Qué tiene que mostrar |
|---|---|---|
| todas | `c11-v2-red-inicio.txt`, `c11-v2-red-fin.txt` | modo avión = 1, Wi-Fi apagado, Bluetooth = 0, sin red por defecto, el `ping` sin salida; build de depuración de la 1.1 y el commit medido |
| todas | `c11-v2-sesion.mp4` | la sesión entera, de corrido, con el avión y la hora en la barra de estado, los toques marcados y el audio de la voz |
| F1 | `c11-v2-F1a-abierta.png`, `c11-v2-F1b-colegio.png` | la app abierta, y el tablero de Colegio |
| F2 | `c11-v2-voz-logcat.txt` | una fila de `VALIDACION` por toque ("Yo", "Yo Quiero", "Yo Quiero Agua"), con la hora del teléfono: es el aviso `onStart` del motor de voz, que empezó a decir esa oración. Más el audio del video |
| F2 | `c11-v2-F2-yo-quiero-agua.png` | la oración "Yo Quiero Agua" en la barra |
| F3 | `c11-v2-F3-categorias.png` + video | la navegación entre categorías |
| F4 | `c11-v2-F4-decir.png` | la tarjeta con el avatar |
| F5 | `c11-v2-F5-recientes.png` | la frase abierta desde Recientes |
| F6 | `c11-v2-F6a…`, `F6b…`, `F6c…` | agregada, quitada con la cuenta regresiva, y de vuelta |
| F7 | `c11-v2-F7a…` + `c11-v2-fotos-1-tras-F7.txt` | Agua con la foto, y un archivo nuevo en `files/pictogifts/` respecto de `fotos-0-inicio` |
| F7 | `c11-v2-F7b-decir-senala-foto.png` | el avatar señalando la foto en «Decir» |
| F7 | `c11-v2-F7c…` + `c11-v2-fotos-2-tras-volver-F7.txt` | Agua con su ícono, y el archivo ya no está |
| F8 | `c11-v2-F8a…` + `c11-v2-fotos-3-tras-crear-F8.txt` | el pictograma creado, y un archivo nuevo con la hora de la foto |
| F8 | `c11-v2-voz-logcat.txt` + `c11-v2-F8b-dicho.png` | una fila de `VALIDACION` con el nombre del pictograma |
| F8 | `c11-v2-F8c…` + `c11-v2-fotos-4-tras-borrar-F8.txt` | el pictograma ya no está, y su archivo tampoco |
| F9 | `c11-v2-F9a…`, `c11-v2-F9b…` | Robot y la velocidad cambiada; después, de vuelta |
| F10 | `c11-v2-F10a…`, `c11-v2-F10b…` | "Modo edición activo" al entrar, y la pantalla de lugares sin la franja al salir |
| F11 | `c11-v2-F11a…` a `F11c…` + `fotos-5`, `fotos-6`, `fotos-7` | el lugar y la categoría creados, un archivo nuevo por cada uno, y los dos archivos borrados al borrar el lugar |
| F12 | `c11-v2-F12a…`, `c11-v2-F12b…` | Jugo atenuado con el ojo tachado, y después normal |

**Por qué la voz se respalda así.** La instrumentación de la tesis
(`Validacion`) registra solo el camino pictograma → voz, que es el de F2 y el
"decir" de F8: las dos funciones cuya definición exige que suene. Cada fila es
el aviso `onStart` del motor de voz para esa oración, emitido con el teléfono
en modo avión. Para el resto, la voz no es parte de la definición, y el audio
queda igual en el video. No hace falta cambiar la instrumentación.

### 7. Resultado

**Cumple si F1 a F12 quedan respaldadas por sus archivos (12 de 12).** Una
función que falla o cuyo respaldo no alcanza se anota tal cual: "no cumple", o
"n de 12 respaldadas".

### 8. Después de la sesión

- Volvé a poner tu contraseña: Ajustes → Configuraciones → Cambiar contraseña.
- Quitá de Favoritos "Yo Quiero Agua", si no la querés guardada.

---

## Criterio 13 — Persistencia

**Umbral (sin cambios):** 100 % de los ajustes y pictogramas propios tras
cerrar desde recientes.

Se hace a continuación del criterio 11, con la misma build y la misma
contraseña de prueba. No necesita modo avión, pero no molesta dejarlo.

### Los 6 elementos (los mismos del protocolo original)

| # | Elemento | Cambio | Clave en el DataStore |
|---|---|---|---|
| A1 | Tipo de avatar | Persona → **Robot** | `av_kind` |
| A2 | Tamaño de pictogramas | Normal → **Grande** | `text_size` |
| A3 | Modo oscuro | apagado → **encendido** | `dark_mode` |
| A4 | Velocidad de la voz | la de siempre → **otra**, bien distinta | `voice_rate` |
| P1 | Pictograma propio en **Bebidas**, con foto **de la galería** | — | `custom_pictograms` y un archivo en `files/pictogifts/` |
| P2 | Pictograma propio en **Juego**, con foto **de la cámara** | — | `custom_pictograms` y un archivo en `files/pictogifts/` |

Los nombres de P1 y P2 son los de los objetos fotografiados, y no pueden ser
palabras de la app.

### Qué demuestra cada parte

El primer intento no permitía distinguir qué capturas eran anteriores al cierre
y cuáles posteriores. Acá, cada elemento queda con tres cosas:

1. **Antes de cerrar:** una captura con la hora visible, y el contenido del
   DataStore leído del teléfono.
2. **Que la app se cerró de verdad:** el número de proceso (PID) antes del
   cierre, **ninguno** después de cerrar desde recientes, y **otro distinto**
   al reabrir. Si el proceso es otro, todo lo que muestra salió del
   almacenamiento, no de la memoria.
3. **Después de reabrir:** la misma captura, con una hora posterior, y el mismo
   DataStore.

Además, la grabación continua muestra el cierre desde recientes y la
reapertura.

### 1. Antes de empezar

- **En recientes, cerrá todas las demás apps** ("Cerrar todo"). Así, cuando se
  cierre AgustiSpeaks, la grabación no muestra las miniaturas de otras apps.
- **Seguí con la contraseña de prueba.** El contenido del DataStore incluye la
  huella de la contraseña, y ese archivo va al repositorio.

Si abriste una ventana de PowerShell nueva, volvé a correr el bloque de
variables del criterio 11 (paso 1). Después, en la misma ventana:

```powershell
# Captura con prefijo c13-v2.
function Cap13($nombre) {
  & $adb shell screencap -p /sdcard/cap-tesis.png
  & $adb pull /sdcard/cap-tesis.png "$ev\c13-v2-$nombre.png" | Out-Null
  & $adb shell rm /sdcard/cap-tesis.png
}

# Proceso, archivos y DataStore en un momento dado. El DataStore se copia tal
# cual (.pb) y además se escribe decodificado en el .txt.
function Estado13($nombre) {
  $pb = "$ev\c13-v2-datastore-$nombre.pb"
  & $adb shell "run-as $pkg cat files/datastore/agusti_settings.preferences_pb > /data/local/tmp/ds-tesis.pb"
  & $adb pull /data/local/tmp/ds-tesis.pb $pb | Out-Null
  & $adb shell rm /data/local/tmp/ds-tesis.pb
  & {
    "== hora del telefono =="
    & $adb shell date
    "== proceso de la app: pidof =="
    & $adb shell "pidof $pkg || echo '(ningun proceso)'"
    "== archivos de la app: DataStore y fotos, con fecha =="
    & $adb shell "run-as $pkg ls -la files/datastore/ files/pictogifts/ 2>&1"
    "== DataStore decodificado (leer-datastore.ps1) =="
    powershell -NoProfile -ExecutionPolicy Bypass `
      -File "C:\Users\Felip\AgustiSpeaks\docs\tesis\herramientas\leer-datastore.ps1" -Archivo $pb
  } | Out-File -Encoding utf8 "$ev\c13-v2-estado-$nombre.txt"
}
```

### 2. Empezar la grabación

En la otra ventana, igual que en el criterio 11 pero con otro archivo:

```powershell
$env:ADB = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"
scrcpy --record="C:\Users\Felip\AgustiSpeaks\docs\tesis\evidencia-2026-10\c13-v2-sesion.mp4" `
  --max-size=1280 --video-bit-rate=700K --max-fps=15 --audio-bit-rate=64K `
  --audio-dup --show-touches
```

### 3. Los pasos

| Paso | En el teléfono | En la computadora |
|---|---|---|
| 1 | Abrí AgustiSpeaks → ⚙ → **Configuraciones** → **Entrar al modo edición** → contraseña de prueba | — |
| 2 | **Avatar** → **Identidad** → **Robot** (A1) | `Cap13 "1-antes-avatar"` |
| 3 | Pestaña **Voz** → cambiá la **Velocidad** a un valor bien distinto del de siempre (A4) | `Cap13 "1-antes-voz"` |
| 4 | **Configuraciones** → **Tamaño de pictogramas: Grande** (A2) y **Modo oscuro** encendido (A3) | `Cap13 "1-antes-config"` |
| 5 | Volvé → **Colegio** → **Bebidas** → **Agregar** → nombre del objeto → **Elegir de la galería** → la foto del objeto → **Guardar** (P1) | `Cap13 "1-antes-bebidas"` |
| 6 | ← → **Juego** → **Agregar** → nombre del otro objeto → **Sacar una foto** → **Guardar** (P2) | `Cap13 "1-antes-juego"` |
| 7 | — (la app abierta, sin tocar nada) | `Estado13 "1-antes"` |
| 8 | Botón de **recientes** → deslizá AgustiSpeaks hacia arriba para cerrarla → queda la pantalla de inicio | `Estado13 "2-cerrada"` |
| 9 | **Esperá un minuto**, para que las capturas de después tengan otra hora | — |
| 10 | Abrí AgustiSpeaks desde el ícono | `Estado13 "3-reabierta"` |
| 11 | Pantalla de lugares: sin la franja del modo edición (no se guarda, a propósito) | `Cap13 "3-despues-lugares"` |
| 12 | ⚙ → **Avatar** → **Identidad** | `Cap13 "3-despues-avatar"` |
| 13 | Pestaña **Voz** | `Cap13 "3-despues-voz"` |
| 14 | **Configuraciones** (el tamaño y el modo oscuro a la vista) | `Cap13 "3-despues-config"` |
| 15 | Volvé → **Colegio** → **Bebidas** | `Cap13 "3-despues-bebidas"` |
| 16 | ← → **Juego** | `Cap13 "3-despues-juego"` |

Después, cerrá la ventana de scrcpy y listá los archivos:

```powershell
Get-Item "$ev\c13-v2-*" | Select-Object Name, Length
```

Igual que antes: **si el video pasa de 95 MB, no lo agregues a git** y avisame.

**Si algún valor no se mantuvo, no lo corrijas ni repitas el paso**: es el
dato.

### 4. Tabla de verificación

Se llena con los archivos de la sesión, en la etapa de registro:

| Elemento | Antes (captura · DataStore) | PID antes | PID después | Después (captura · DataStore) |
|---|---|---|---|---|
| A1 Tipo de avatar | `1-antes-avatar.png` · `av_kind` en `estado-1-antes` | `estado-1-antes` | `estado-3-reabierta` | `3-despues-avatar.png` · `av_kind` en `estado-3-reabierta` |
| A2 Tamaño de pictogramas | `1-antes-config.png` · `text_size` | ″ | ″ | `3-despues-config.png` · `text_size` |
| A3 Modo oscuro | `1-antes-config.png` · `dark_mode` | ″ | ″ | `3-despues-config.png` · `dark_mode` |
| A4 Velocidad de la voz | `1-antes-voz.png` · `voice_rate` | ″ | ″ | `3-despues-voz.png` · `voice_rate` |
| P1 Pictograma en Bebidas | `1-antes-bebidas.png` · `custom_pictograms` + su archivo en `files/pictogifts/` | ″ | ″ | `3-despues-bebidas.png` · lo mismo |
| P2 Pictograma en Juego | `1-antes-juego.png` · `custom_pictograms` + su archivo en `files/pictogifts/` | ″ | ″ | `3-despues-juego.png` · lo mismo |

En `estado-2-cerrada`, el `pidof` tiene que decir "(ningun proceso)". Los
archivos de captura y de estado llevan el prefijo `c13-v2-`.

### 5. Resultado

**Cumple si los 6 elementos tienen el mismo valor antes y después (6 de 6),
con la app cerrada en el medio**: ningún proceso en `estado-2-cerrada` y un PID
distinto al reabrir.

Si al cerrar desde recientes el proceso siguiera vivo, se anota tal cual: en
ese caso, la sesión no demuestra que los valores se leyeron del
almacenamiento.

### 6. Al terminar

Fuera de la evidencia, dejá la app como estaba: Persona, Normal, modo oscuro
apagado y la velocidad de siempre. En modo edición, borrá P1 y P2. Volvé a
poner tu contraseña.
