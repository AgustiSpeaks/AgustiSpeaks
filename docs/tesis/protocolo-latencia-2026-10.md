# Protocolo de latencia — criterio 10

**Umbral (fijado el 5-10-2026):** mediana ≤ 300 ms y ningún caso > 1.000 ms,
sobre 50 pulsaciones con frases nuevas de 1 a 6 pictogramas. El arranque en frío
se informa aparte.

Dispositivo: Samsung Galaxy A57, Android 16 (API 36).

---

## Qué se mide

Desde que la app procesa el toque de un pictograma hasta que el motor de voz
avisa que empezó a decir la oración (`onStart`). **No** es el sonido saliendo
del parlante, que necesitaría un micrófono externo: es el último instante que la
app puede observar. Así hay que declararlo en la tesis.

Cada toque entrega al motor de voz **la oración acumulada entera**, así que una
oración de 6 pictogramas es más texto para sintetizar que una de 1. Por eso la
secuencia reparte las 50 mediciones entre 1 y 6 pictogramas.

## La sesión en números

| | Pulsaciones |
|---|---|
| Arranque en frío (n = 1), se informa aparte | 1 |
| Mediciones del criterio (n = 2 a 51) | 50 |
| **Total de toques** | **51** |

Distribución de las 50 según cuántos pictogramas tiene la oración:

| Pictogramas | 1 | 2 | 3 | 4 | 5 | 6 |
|---|---|---|---|---|---|---|
| Mediciones | 9 | 9 | 8 | 8 | 8 | 8 |

**Ninguna oración se repite.** Cada bloque empieza con una palabra distinta de
todas las demás, así que cada oración de la sesión es única. Una oración
repetida podría salir de la caché del motor de voz y medir otra cosa.

---

## Preparación

1. **El código tiene que estar commiteado.** Anotá el hash del último commit
   que tocó la app, con `git log --oneline -1 -- app`. Va a la columna
   "Commit" de la bitácora. Sin el `-- app` saldría el último commit de
   cualquier tipo, que puede ser uno que solo agregó documentos.
2. **Instalá la build de depuración** en el A57 desde Android Studio (▶ Run).
   La medición solo existe en esa build: en release no registra nada.
3. En el teléfono, **Ajustes → Idiomas → Salida de texto a voz**: el motor tiene
   que ser *Speech Services by Google* con la voz en español **instalada**. Si
   no está instalada, la app no habla sin red.
4. **Reiniciá el teléfono.** Así arrancan en frío tanto la app como el motor de
   voz, y no queda ninguna frase en caché de una sesión anterior. Desbloquealo y
   esperá un minuto.
5. Activá el **modo avión**: la medición es sobre el camino sin red, que es el
   que afirma la tesis. El cable USB sigue funcionando en modo avión.
6. Conectá el cable USB y, en la Terminal de Android Studio (abajo, ya está
   parada en la carpeta del proyecto), **vaciá el registro**:

   ```powershell
   $adb = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"
   & $adb logcat -c
   ```

7. Abrí AgustiSpeaks **desde el ícono del teléfono**, no desde Android Studio.
8. Elegí el lugar **Colegio**.
9. **Esperá 5 segundos** antes del primer toque. El motor de voz termina de
   prepararse en ese tiempo; si se toca antes, puede no sonar y la medición se
   pierde.

## Reglas durante la sesión

- Tocá **exactamente** la secuencia de abajo, en orden.
- **Después de cada toque, esperá a que termine de hablar y contá un segundo**
  antes del siguiente. Un toque mientras todavía habla corta la locución
  anterior, y la medición deja de ser comparable.
- Entre bloques, tocá la **X de la barra de la frase** (Borrar la frase). Borrar
  no habla y no se mide.
- **No toques "Decir"**, ni abras Recientes, Favoritos o Ajustes, hasta terminar.
  "Decir" habla la oración por otro camino y la deja en la caché del motor.
- Para cambiar de categoría: la flecha ← **del panel derecho**, al lado del
  nombre de la categoría, vuelve a la lista de categorías. Navegar no habla ni
  se mide. **No uses la flecha de arriba a la izquierda**, al lado de "Colegio":
  esa sale del lugar, y al volver a entrar la app empieza una frase nueva.
- **Mirá la barra de la frase después de cada toque: la oración tiene que ir
  creciendo.** Después del segundo toque de un bloque tiene que mostrar dos
  palabras, después del tercero tres, y así. Si alguna vez muestra solo la
  última palabra, la frase se borró por el camino: la sesión ya no sirve,
  conviene cortarla y empezar de nuevo desde el reinicio.
- La sesión **empieza por el bloque 0** ("Hola"). Ese primer toque es el
  arranque en frío, y no se reemplaza por otro.
- **Si te equivocás de pictograma, cortá la sesión ahí** y empezá de nuevo desde
  el reinicio del teléfono. Un error invalida la sesión entera, así que seguir
  solo gasta tiempo. Tampoco sirve corregir sobre la marcha: volver a tocar el
  pictograma correcto repite una oración que el motor ya tiene en caché.
- **Empezar de nuevo es reiniciar el teléfono, siempre.** No alcanza con borrar
  la frase y volver a "Hola", ni con vaciar el registro. La app y el motor de
  voz siguen con todo en memoria: las oraciones ya dichas salen de la caché en
  pocos milisegundos (`repetida`), y la cuenta de pulsaciones no vuelve a 1.
  Así salió el intento 3.

### Comprobación en el primer toque

Después de tocar "Hola" (n = 1), mirá la pestaña **Logcat** de Android Studio
filtrada por `tag:VALIDACION`. Tiene que mostrar exactamente dos líneas:

```
n,frase,pictogramas,latencia_ms,regimen
1,"Hola",1,…,arranque_frio
```

Si la fila empieza con otro número, o dice `repetida`, la app no arrancó en
frío: **reiniciá el teléfono antes de seguir**. Son diez segundos que ahorran
una sesión entera.
- En el panel "Palabras", **"Quiero" y "No quiero" están uno al lado del otro**.
  Es donde se equivocó el intento 2.

"Palabras" es el panel izquierdo, que siempre está a la vista. El resto son
categorías del panel derecho de Colegio.

---

## Secuencia

### Bloque 0 — arranque en frío (se informa aparte)

| n | Tocar | Dónde | Oración resultante | Pict. |
|---|---|---|---|---|
| 1 | Hola | Social | Hola | 1 |

**Borrar la frase.**

### Bloque 1

| n | Tocar | Dónde | Oración resultante | Pict. |
|---|---|---|---|---|
| 2 | Yo | Palabras | Yo | 1 |
| 3 | Quiero | Palabras | Yo Quiero | 2 |
| 4 | Beber | Acciones | Yo Quiero Beber | 3 |
| 5 | Agua | Bebidas | Yo Quiero Beber Agua | 4 |
| 6 | Por favor | Social | Yo Quiero Beber Agua Por favor | 5 |
| 7 | Gracias | Social | Yo Quiero Beber Agua Por favor Gracias | 6 |

**Borrar la frase.**

### Bloque 2

| n | Tocar | Dónde | Oración resultante | Pict. |
|---|---|---|---|---|
| 8 | Mamá | Familia | Mamá | 1 |
| 9 | Quiero | Palabras | Mamá Quiero | 2 |
| 10 | Jugar | Acciones | Mamá Quiero Jugar | 3 |
| 11 | Pelota | Juego | Mamá Quiero Jugar Pelota | 4 |
| 12 | Afuera | Lugares | Mamá Quiero Jugar Pelota Afuera | 5 |
| 13 | Por favor | Social | Mamá Quiero Jugar Pelota Afuera Por favor | 6 |

**Borrar la frase.**

### Bloque 3

| n | Tocar | Dónde | Oración resultante | Pict. |
|---|---|---|---|---|
| 14 | Maestra | Escuela | Maestra | 1 |
| 15 | Quiero | Palabras | Maestra Quiero | 2 |
| 16 | Leer | Escuela | Maestra Quiero Leer | 3 |
| 17 | Libro | Escuela | Maestra Quiero Leer Libro | 4 |
| 18 | Más | Social | Maestra Quiero Leer Libro Más | 5 |
| 19 | Gracias | Social | Maestra Quiero Leer Libro Más Gracias | 6 |

**Borrar la frase.**

### Bloque 4

| n | Tocar | Dónde | Oración resultante | Pict. |
|---|---|---|---|---|
| 20 | Me duele | Palabras | Me duele | 1 |
| 21 | Cabeza | Partes del cuerpo | Me duele Cabeza | 2 |
| 22 | Quiero | Palabras | Me duele Cabeza Quiero | 3 |
| 23 | Ir | Acciones | Me duele Cabeza Quiero Ir | 4 |
| 24 | Casa | Lugares | Me duele Cabeza Quiero Ir Casa | 5 |
| 25 | Ayuda | Palabras | Me duele Cabeza Quiero Ir Casa Ayuda | 6 |

**Borrar la frase.**

### Bloque 5

| n | Tocar | Dónde | Oración resultante | Pict. |
|---|---|---|---|---|
| 26 | Papá | Familia | Papá | 1 |
| 27 | Venir | Acciones | Papá Venir | 2 |
| 28 | Aquí | Lugares | Papá Venir Aquí | 3 |
| 29 | Ayuda | Palabras | Papá Venir Aquí Ayuda | 4 |
| 30 | Por favor | Social | Papá Venir Aquí Ayuda Por favor | 5 |
| 31 | Gracias | Social | Papá Venir Aquí Ayuda Por favor Gracias | 6 |

**Borrar la frase.**

### Bloque 6

| n | Tocar | Dónde | Oración resultante | Pict. |
|---|---|---|---|---|
| 32 | Tú | Palabras | Tú | 1 |
| 33 | Quiero | Palabras | Tú Quiero | 2 |
| 34 | Escuchar | Acciones | Tú Quiero Escuchar | 3 |
| 35 | Música | Juego | Tú Quiero Escuchar Música | 4 |
| 36 | Me gusta | Palabras | Tú Quiero Escuchar Música Me gusta | 5 |
| 37 | Más | Social | Tú Quiero Escuchar Música Me gusta Más | 6 |

**Borrar la frase.**

### Bloque 7

| n | Tocar | Dónde | Oración resultante | Pict. |
|---|---|---|---|---|
| 38 | No quiero | Palabras | No quiero | 1 |
| 39 | Dormir | Acciones | No quiero Dormir | 2 |
| 40 | Quiero | Palabras | No quiero Dormir Quiero | 3 |
| 41 | Jugar | Acciones | No quiero Dormir Quiero Jugar | 4 |
| 42 | Bloques | Juego | No quiero Dormir Quiero Jugar Bloques | 5 |
| 43 | Por favor | Social | No quiero Dormir Quiero Jugar Bloques Por favor | 6 |

**Borrar la frase.**

### Bloque 8

| n | Tocar | Dónde | Oración resultante | Pict. |
|---|---|---|---|---|
| 44 | Abuela | Familia | Abuela | 1 |
| 45 | Hola | Social | Abuela Hola | 2 |
| 46 | ¿Cómo estás? | Social | Abuela Hola ¿Cómo estás? | 3 |
| 47 | Yo | Palabras | Abuela Hola ¿Cómo estás? Yo | 4 |
| 48 | Bien | Social | Abuela Hola ¿Cómo estás? Yo Bien | 5 |
| 49 | Gracias | Social | Abuela Hola ¿Cómo estás? Yo Bien Gracias | 6 |

**Borrar la frase.**

### Bloque 9

| n | Tocar | Dónde | Oración resultante | Pict. |
|---|---|---|---|---|
| 50 | Terminé | Palabras | Terminé | 1 |
| 51 | Chau | Social | Terminé Chau | 2 |

Fin de la sesión. **No cierres la app todavía**: primero copiá el registro.

---

## Copiar el resultado a `docs/tesis/datos/latencias-2026-10.csv`

En la misma Terminal de Android Studio, con el teléfono todavía conectado:

```powershell
$adb = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"
[Console]::OutputEncoding = [Text.Encoding]::UTF8
New-Item -ItemType Directory -Force docs\tesis\datos | Out-Null
& $adb logcat -d -v raw -s VALIDACION:I |
    Where-Object { $_ -notmatch '^-{5,}' } |
    Out-File -Encoding utf8 docs\tesis\datos\latencias-2026-10.csv
```

Qué hace cada línea:

| Línea | Para qué |
|---|---|
| `[Console]::OutputEncoding = …UTF8` | sin esto, PowerShell lee mal las tildes que manda `adb` y "Mamá" queda como "MamÃ¡" |
| `logcat -d -v raw -s VALIDACION:I` | vuelca solo los mensajes con el tag `VALIDACION`, sin hora ni prefijos: cada mensaje ya es una fila CSV |
| `Where-Object { … }` | quita las líneas separadoras `--------- beginning of main` que agrega `logcat` |
| `Out-File -Encoding utf8` | guarda en UTF-8, que Excel abre con las tildes bien |

Si preferís no usar la terminal: en la pestaña **Logcat** de Android Studio,
filtrá por `tag:VALIDACION`, seleccioná las 52 líneas y pegámelas en el chat.
Armo el CSV yo.

## Cómo saber si la sesión es válida

Antes de analizar nada, el archivo tiene que cumplir esto:

| Comprobación | Esperado |
|---|---|
| Encabezado | una sola línea `n,frase,pictogramas,latencia_ms,regimen`, la primera |
| Filas | 51, con `n` de 1 a 51 **sin huecos** |
| `regimen` | 1 `arranque_frio` (n = 1) y 50 `nueva` |
| `repetida` | **ninguna** |
| Oraciones | iguales a las de la tabla de arriba |

Si falta un `n`, ese toque no llegó a sonar. Si hay más de un encabezado, el
registro no se vació antes de empezar. Si aparece `repetida` o una oración
distinta a la tabla, hubo un error de protocolo. **En cualquiera de esos casos se
repite la sesión entera, desde el reinicio del teléfono.** No se borran filas a
mano: elegir qué mediciones cuentan después de verlas es justo lo que el umbral
fijado de antemano busca evitar.

## Intentos anteriores

| Intento | Fecha | Archivo | Por qué no vale |
|---|---|---|---|
| 1 | 2026-10-06 14:15 | `datos/latencias-2026-10-intento-1-invalido.csv` | faltó el bloque 0; en los bloques 1 a 3 la frase se vació entre toques, así que hubo oraciones de 1 pictograma donde tenía que haber de 2 a 6, y 4 oraciones repetidas. Solo 50 pulsaciones en lugar de 51 |
| 2 | 2026-10-06 15:03 | `datos/latencias-2026-10-intento-2-invalido.csv` | dos pictogramas equivocados en el bloque 7 (n = 38 y 40). Al corregir, "No quiero" se dijo dos veces y la segunda salió de la caché (n = 41, `repetida`). 54 pulsaciones. Sacar las filas erróneas no alcanza: la medición planificada de "No quiero" quedó contaminada y quedarían 49 |

| 3 | 2026-10-06 15:16 | `datos/latencias-2026-10-intento-3-invalido.csv` | se repitió la secuencia desde "Hola" sin reiniciar el teléfono, después de cortar un intento en el toque 17. Sin encabezado, `n` empieza en 18, y las primeras 15 filas son `repetida` con 5 a 11 ms: salieron de la caché del motor de voz |

Se conservan sin editar, como registro. **No se citan** para el criterio 10.

De paso, los tres intentos dejan a la vista algo que la tesis puede usar: las
oraciones repetidas tardan **de 5 a 11 ms**, porque el motor las saca de su
caché. Eso confirma que las 50 oraciones del protocolo tienen que ser nuevas
para medir la síntesis y no la caché.

## Análisis

Sobre las 50 filas `nueva`:

- **Mediana** de `latencia_ms`, contra el umbral de 300 ms.
- **Máximo**, contra el umbral de 1.000 ms.
- Mediana por cantidad de pictogramas, de 1 a 6: muestra si decir la oración
  entera cuesta más a medida que crece.

El arranque en frío (n = 1) se informa aparte, con su valor.

El cálculo lo hago yo con el CSV, sin estimar nada, y el resultado va a la fila
10 de la bitácora, con el commit, el dispositivo y la ruta del CSV como
evidencia.
