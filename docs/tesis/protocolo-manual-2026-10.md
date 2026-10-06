# Protocolo de los criterios manuales — 11 a 16

Umbrales fijados por el autor el 5-10-2026. Dispositivo: Samsung Galaxy A57,
Android 16 (API 36). Código medido: el último commit que tocó la app
(`git log --oneline -1 -- app`), que es el que tiene que estar instalado.

Cada criterio dice **qué hacer**, **qué tiene que pasar** y **qué guardar**. Las
casillas ☐ se marcan sí o no a medida que se avanza; con eso se completa la
bitácora.

---

## Antes de empezar

1. **La build de depuración del commit medido, instalada en el A57** (▶ Run
   desde Android Studio). Anotá el hash.
2. Creá la carpeta de evidencia: `docs/tesis/evidencia-2026-10/`. Cada captura se
   guarda ahí con el nombre que indica cada paso (`c11-…`, `c12-…`).
3. **Cómo sacar una captura del teléfono y traerla a la PC**, desde la Terminal
   de Android Studio:

   ```powershell
   $adb = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"
   & $adb shell screencap -p /sdcard/captura.png
   & $adb pull /sdcard/captura.png docs\tesis\evidencia-2026-10\NOMBRE.png
   ```

   Cambiá `NOMBRE` por el que pide cada paso. También vale sacarla con el
   teléfono (botón de encendido + bajar volumen) y pasarla por cable.
4. **Usá solo fotos de objetos, nunca de personas**: un juguete, una taza, una
   mochila. Esto vale para todas las fotos de este protocolo.

## Orden recomendado

| Orden | Criterio | Dónde | Por qué en este lugar |
|---|---|---|---|
| 1 | 12 Permisos | PC | no necesita el teléfono |
| 2 | 16 Resguardo (parte estática y prueba de respaldo) | PC + cable | no necesita tocar la app |
| 3 | 14 Accesibilidad | teléfono, **con internet** | hay que instalar Accessibility Scanner desde Play Store |
| 4 | 15 Pictograma propio con foto | teléfono + Device Explorer | la foto de cámara sirve también para el criterio 16 |
| 5 | 13 Persistencia | teléfono | |
| 6 | 11 Sin red | teléfono, **en modo avión** | al final, porque deja el teléfono sin red |

---

## Criterio 12 — Permisos

**Umbral:** 0 permisos de red o cámara en el manifiesto combinado.

### Pasos

1. En Android Studio, abrí `app/src/main/AndroidManifest.xml`.
2. Abajo del editor, tocá la pestaña **Merged Manifest**.
3. En el panel del manifiesto combinado, buscá todos los `<uses-permission>`
   (Ctrl+F, `uses-permission`).
4. ☐ No aparece `android.permission.INTERNET`.
5. ☐ No aparece `android.permission.ACCESS_NETWORK_STATE` ni
   `ACCESS_WIFI_STATE`.
6. ☐ No aparece `android.permission.CAMERA`.
7. Anotá todos los `<uses-permission>` que sí aparezcan. Lo esperado es uno solo:
   `com.tunombre.agustispeaks.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION`. Es
   **propio de la app**, lo agrega la biblioteca `androidx.core` con nivel
   `signature`, y no es de red ni de cámara.
8. En el teléfono: **Ajustes → Aplicaciones → AgustiSpeaks → Permisos**.
   ☐ La pantalla dice que la app no pide permisos.

**Comprobación cruzada (opcional):** Build → **Analyze APK…** →
`app/build/outputs/apk/debug/app-debug.apk` → `AndroidManifest.xml`. Es el
manifiesto que realmente se instala.

### Evidencia

| Archivo | Qué muestra |
|---|---|
| `c12-merged-manifest.png` | la pestaña Merged Manifest con los `<uses-permission>` a la vista |
| `c12-permisos-telefono.png` | la pantalla de permisos de AgustiSpeaks en el A57 |

**Resultado:** cumple si las casillas 4, 5 y 6 son sí.

---

## Criterio 16 — Resguardo de las fotografías

**Umbral:** 0 fotos fuera del almacenamiento privado o incluidas en el respaldo
automático.

Tiene tres partes. La C se hace durante el criterio 15, porque necesita sacar una
foto con la cámara desde la app.

### A. Configuración de respaldo (PC)

1. En la pestaña **Merged Manifest**, buscá `allowBackup`.
   ☐ Dice `android:allowBackup="false"`.
2. Abrí `app/src/main/res/xml/data_extraction_rules.xml`.
   ☐ `<cloud-backup>` y `<device-transfer>` excluyen `root`, `file`,
   `database`, `sharedpref` y `external`.

### B. Prueba de respaldo en el teléfono (cable USB)

```powershell
$adb = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"
& $adb shell bmgr enabled
& $adb shell bmgr backupnow com.tunombre.agustispeaks
```

- Si el primer comando dice que el servicio de respaldo está **deshabilitado**,
  la prueba B no se puede hacer sin activarlo, y activarlo cambia la
  configuración de respaldo de tu teléfono. **No lo actives**: anotá
  "B no realizable, respaldo deshabilitado en el dispositivo" y el criterio se
  apoya en A y C.
- Si está habilitado, el segundo comando tiene que informar que el respaldo de
  `com.tunombre.agustispeaks` **no está permitido**. ☐ Lo informa.
  **[NO VERIFICADO]** el texto exacto que imprime Android 16: copiá la salida
  completa tal cual.

### C. Dónde quedan las fotos (durante el criterio 15)

Antes de sacar la foto con la cámara en el paso 15.3:

1. Abrí la **Galería** del teléfono y mirá cuál es la foto más reciente.
   Captura: `c16-galeria-antes.png`.

Después de guardar el pictograma en el paso 15.3:

2. ☐ En **Device Explorer** (ver criterio 15), la foto nueva está en
   `/data/data/com.tunombre.agustispeaks/files/pictogifts/`, que es
   almacenamiento privado de la app.
3. ☐ En la **Galería**, **no** apareció ninguna foto nueva.
   Captura: `c16-galeria-despues.png`.
4. ☐ En Device Explorer, `/sdcard/DCIM/Camera/` no tiene un archivo con la
   hora de la foto.

Si en el paso 3 o 4 aparece una foto nueva, la app de cámara del Samsung guardó
**su propia copia** además de la que le prestó la app. Es un hallazgo y se
anota tal cual: el criterio no se cumple, y la causa está fuera de la app.

### Evidencia

| Archivo | Qué muestra |
|---|---|
| `c16-allowbackup.png` | `allowBackup="false"` en el manifiesto combinado |
| `c16-bmgr.txt` | la salida completa de los dos comandos `bmgr` |
| `c16-galeria-antes.png`, `c16-galeria-despues.png` | la galería antes y después de sacar la foto |
| `c16-device-explorer.png` | la carpeta `files/pictogifts` con la foto |

**Resultado:** cumple si A tiene las dos casillas en sí, si B es sí (o no
realizable) y si C 2, 3 y 4 son sí.

---

## Criterio 14 — Accesibilidad

**Umbral:** 0 objetivos táctiles menores a 48 dp en los pictogramas y en
«Decir»; contraste de texto de al menos 4,5:1; todo lo demás se reporta.

### Preparación (con internet)

1. Instalá **Accessibility Scanner**, de Google, desde Play Store.
2. **Ajustes → Accesibilidad → Aplicaciones instaladas → Accessibility
   Scanner** → activalo. Aparece un botón flotante azul con una tilde.
3. En AgustiSpeaks, dejá el **tamaño de texto en el valor por defecto**.

### Pantallas a revisar

En cada una: abrí la pantalla, tocá el botón flotante → **Instantánea**, y
esperá el informe.

| # | Pantalla | Cómo llegar |
|---|---|---|
| 1 | Tablero con una categoría abierta | Colegio → Acciones. Se ven el panel Palabras, los pictogramas y la barra con el botón de hablar |
| 2 | Tablero con la lista de categorías | Colegio, sin abrir ninguna |
| 3 | Tarjeta de «Decir» | tocá "Yo", "Quiero" y después el botón de hablar |
| 4 | Recientes / Favoritos | la estrella de la barra superior |

### Qué anotar en cada pantalla

Para cada sugerencia del informe: tipo, elemento y valor.

| Tipo de sugerencia | Qué decide |
|---|---|
| **Objetivo táctil** sobre un pictograma o sobre el botón de hablar | ☐ ninguna → cumple esa parte |
| **Contraste de texto**, con la proporción que informa | ☐ ninguna por debajo de 4,5:1 → cumple esa parte |
| Cualquier otra (etiquetas, contraste de imagen, objetivos táctiles en otros botones) | se reporta, no decide |

**Un límite de la herramienta:** Accessibility Scanner exige 4,5:1 al texto
normal, pero solo 3:1 al texto grande. Un texto grande con un contraste entre
3:1 y 4,5:1 **no aparece en el informe**, aunque no llegue al umbral de la
tesis. Si eso importa, los contrastes exactos se pueden calcular a partir de los
colores del código, con una prueba automatizada aparte.

### Evidencia

| Archivo | Qué muestra |
|---|---|
| `c14-pantalla-1.png` … `c14-pantalla-4.png` | el informe del Scanner sobre cada pantalla, con el número de sugerencias |
| `c14-sugerencias.md` | la lista de sugerencias, una por renglón: pantalla, tipo, elemento, valor |

**Se espera encontrar sugerencias.** Una app que dibuja toda su interfaz a mano
difícilmente salga con cero, y un cero limpio sería raro. Lo que decide el
criterio son solo las dos primeras filas de la tabla.

---

## Criterio 15 — Pictograma propio con foto

**Umbral:** 100 % de los pasos (crear, ver, decir, reabrir, eliminar), y el
archivo de la foto tiene que desaparecer al eliminar.

Se hace con **dos** pictogramas, uno por cada origen de la foto, así quedan
cubiertos los dos caminos. Los nombres no pueden coincidir con palabras de la
app: si se llamara "Auto", toda frase con "auto" mostraría su foto.

| | Nombre | Categoría | Foto |
|---|---|---|---|
| P1 | **Camioncito** | Juego | con la **cámara**, de un juguete u objeto |
| P2 | **Mi taza** | Bebidas | de la **galería**, de una taza u objeto |

### Device Explorer

En Android Studio: **View → Tool Windows → Device Explorer**, o Ctrl+Shift+A y
escribí `Device Explorer`. Elegí el A57 y navegá a
`/data/data/com.tunombre.agustispeaks/files/pictogifts/`. Para actualizar la
lista: clic derecho → **Synchronize**.

### Pasos

**15.1 — Antes de crear.** Device Explorer en la carpeta `pictogifts`.
Captura: `c15-carpeta-antes.png`. Anotá cuántos archivos hay.

**15.2 — Galería antes.** Paso C.1 del criterio 16.

**15.3 — Crear P1, con la cámara.**
1. Colegio → Juego → tarjeta **Agregar**.
2. Nombre: `Camioncito`.
3. **Sacar una foto** → fotografiá el objeto → aceptá.
4. ☐ La vista previa muestra el pictograma con la foto y el color de Juego.
   Captura: `c15-formulario.png`.
5. **Guardar**.

Ahora hacé los pasos C.2 a C.4 del criterio 16.

**15.4 — Crear P2, desde la galería.** Colegio → Bebidas → Agregar → nombre
`Mi taza` → **Elegir de la galería** → elegí la foto → Guardar.

**15.5 — Ver.**
1. ☐ P1 está al final de Juego, con su foto en lugar del ícono.
   Captura: `c15-tablero.png`.
2. ☐ P2 está al final de Bebidas, con su foto.
3. ☐ P1 aparece también en **Living**, otro lugar que tiene Juego.
4. ☐ En Device Explorer (Synchronize) hay **dos archivos nuevos** respecto de
   15.1. Anotá sus nombres. Captura: `c15-carpeta-con-fotos.png`.

**15.6 — Decir.**
1. En Colegio, borrá la frase, tocá **Quiero** y después **Camioncito**.
2. ☐ La voz dice "Quiero Camioncito".
3. Tocá el botón de hablar.
4. ☐ La tarjeta muestra **al avatar señalando la foto** de Camioncito.
   Captura: `c15-decir.png`.

**15.7 — Reabrir.**
1. Cerrá la app **desde recientes** (botón ||| del teléfono → deslizá la tarjeta
   de AgustiSpeaks hacia arriba).
2. Abrila de nuevo desde el ícono.
3. ☐ P1 y P2 siguen ahí, cada uno con su foto.

**15.8 — Eliminar P1.**
1. Mantené apretado **Camioncito** → aparece "¿Borrar «Camioncito»?" →
   **Borrar**.
2. ☐ Desaparece del tablero.
3. En Device Explorer → Synchronize.
4. ☐ **El archivo de P1 ya no está**, sin cerrar ni reabrir la app.
   El de P2 sigue. Captura: `c15-carpeta-despues-de-borrar.png`.

**15.9 — Eliminar P2** de la misma forma.
☐ Su archivo desaparece al sincronizar.

### Evidencia

Las capturas nombradas en cada paso, y la lista de casillas.

**Resultado:** cumple si todas las casillas del 15.3 al 15.9 son sí.

---

## Criterio 13 — Persistencia

**Umbral:** 100 % de los ajustes y pictogramas propios tras cerrar desde
recientes.

### Qué se cambia

Cuatro ajustes de distinta naturaleza, y dos pictogramas propios:

| # | Qué | Dónde | Tipo de dato | De | A |
|---|---|---|---|---|---|
| A1 | Tipo de avatar | Ajustes → Avatar | opción | el actual | el otro (persona ↔ robot) |
| A2 | Tamaño del texto | Ajustes → Configuraciones | escala | el actual | otro tamaño |
| A3 | Modo oscuro | Ajustes → Configuraciones | sí / no | el actual | el contrario |
| A4 | Velocidad de la voz | Ajustes → Avatar (voz) | número | el actual | bien distinta, que se note al oído |
| P3 | Pictograma propio `Mi mochila` | Escuela | — | — | foto de una mochila |
| P4 | Pictograma propio `Pelota roja` | Juego | — | — | foto de una pelota u objeto |

### Pasos

1. Anotá el valor actual de A1 a A4. Captura de cada pantalla de Ajustes:
   `c13-antes-1.png`, `c13-antes-2.png`.
2. Cambiá A1 a A4 y creá P3 y P4.
3. ☐ Los seis cambios se ven en la app: avatar, tamaño de texto, colores, voz
   más rápida o más lenta, y los dos pictogramas en su categoría.
4. **Cerrá desde recientes**: botón ||| del teléfono → deslizá la tarjeta hacia
   arriba. **No con el botón Atrás**: Atrás puede dejar la app viva en memoria,
   y entonces los valores vendrían de la memoria y no del disco.
5. **Comprobá que la app se cerró de verdad** (opcional, pero lo deja
   demostrado): abrí la pestaña **Logcat** filtrada por `tag:VALIDACION`. Al
   reabrir y tocar el primer pictograma, tiene que aparecer una fila que empieza
   con `1,` y termina en `arranque_frio`. La cuenta volvió a 1, así que el
   proceso es nuevo.
6. Abrí la app desde el ícono.
7. Marcá cada uno:
   - ☐ A1 tipo de avatar
   - ☐ A2 tamaño del texto
   - ☐ A3 modo oscuro
   - ☐ A4 velocidad de la voz (tocá un pictograma y escuchá)
   - ☐ P3 Mi mochila, con su foto
   - ☐ P4 Pelota roja, con su foto

   Capturas: `c13-despues-1.png`, `c13-despues-2.png`, `c13-pictogramas.png`.
8. Al terminar, devolvé A1 a A4 a como estaban y borrá P3 y P4.

**Resultado:** cumple si las seis casillas del paso 7 son sí (6 de 6).

---

## Criterio 11 — Funcionamiento sin red

**Umbral:** 100 % de las funciones en modo avión.

### Las funciones que se prueban

La lista está fijada acá, antes de probar. Son todas las funciones de la app que
usa una persona:

| # | Función |
|---|---|
| F1 | Abrir la app y elegir un lugar |
| F2 | Tocar pictogramas: suena la voz y la oración crece en la barra |
| F3 | Navegar entre categorías |
| F4 | «Decir»: la tarjeta con el avatar animado |
| F5 | Recientes: ver una frase dicha y volver a abrirla |
| F6 | Favoritos: agregar una frase; quitarla y recuperarla con «Deshacer» |
| F7 | Pictogift con foto: «Agregar foto» desde la galería en la tarjeta de «Decir» |
| F8 | Pictograma propio: crearlo con la cámara, decirlo y borrarlo |
| F9 | Ajustes: cambiar el avatar y la voz, y ver el cambio |

### Preparación

1. Cerrá la app desde recientes.
2. Activá el **modo avión**.
3. **Fijate que Wi-Fi y Bluetooth también estén apagados.** En Samsung, el modo
   avión puede dejarlos encendidos si alguna vez se activaron con el modo avión
   puesto. ☐ En la barra de estado se ve el avión, y no el ícono de Wi-Fi.
4. Abrí la app desde el ícono.

### Pasos

Probá F1 a F9 en orden. Para cada una:

- ☐ funciona igual que con red: sin errores, sin esperas raras y sin avisos de
  conexión.

Capturas, cada una con **el avión visible en la barra de estado**:

| Archivo | Qué muestra |
|---|---|
| `c11-decir.png` | la tarjeta de «Decir» con el avatar animado (F4) |
| `c11-pictograma-propio.png` | el pictograma propio creado sin red (F8) |
| `c11-favoritos.png` | Favoritos con la frase agregada (F6) |

Al terminar, borrá lo que creaste: el pictograma propio, el favorito y el
pictogift.

**Si algo no funciona**, anotá exactamente qué y en qué paso. **No se repite
con red** para "ver si era eso": el resultado es "no cumple" con la función que
falló.

**Resultado:** cumple si F1 a F9 son sí (9 de 9).

---

## Después de terminar

Pasame, para cada criterio, las casillas marcadas (alcanza con "todo sí" o con
el detalle de lo que dio no), y decime si las capturas quedaron en
`docs/tesis/evidencia-2026-10/`. Con eso completo las filas 11 a 16 de la
bitácora y las commiteo junto con la evidencia.
