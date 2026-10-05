# Apéndice G — Bitácora de verificación

Registro de cada criterio de validación: cuándo se verificó, contra qué commit,
en qué dispositivo, con qué resultado y dónde está la evidencia.

> **Nota:** la lista de trece criterios la compuse a partir de lo que el proyecto
> afirma y de lo que se puede verificar. **Conviene que la revises antes de darla
> por buena**, porque define qué se considera "validado" en el capítulo 8. El
> criterio de exportación del GIF, que figuraba en el plan original, se eliminó
> junto con la función.

---

## G.1 Tabla de criterios

| # | Criterio | Fecha | Commit | Dispositivo | Android | Resultado | Evidencia |
|---|---|---|---|---|---|---|---|
| 1 | Correspondencia motor ↔ catálogo | 2026-09-24 | `94cf5c6` | — (JVM) | — | **Cumple**: 88 = 88, sin sobrantes | `salidas/E-inventario.txt` |
| 2 | Vocabulario alcanzable | 2026-09-24 | `94cf5c6` | — (JVM) | — | **Cumple**: 0 categorías huérfanas | `salidas/E-inventario.txt` |
| 3 | Equivalencia catálogo ↔ motor | 2026-09-24 | `94cf5c6` | — (JVM) | — | **Cumple**: 0 desvíos en 97 casos | `salidas/G1-equivalencia.txt` |
| 4 | Cobertura de representación | 2026-09-24 | `94cf5c6` | — (JVM) | — | **No cumple**: 328 valores fuera de rango, todos de `HOT` | `salidas/G2-representacion.txt` |
| 5 | Resolución del corpus ≥ 90 % | 2026-09-24 | `94cf5c6` | — (JVM) | — | **Cumple**: 97,3 % | `salidas/F1-corpus.txt` |
| 6 | Colisión léxica = 0 | 2026-09-24 | `94cf5c6` | — (JVM) | — | **Cumple**: 0 de 10 pares | `salidas/F1bis-trampas.txt` |
| 7 | Cobertura semántica | 2026-09-24 | `94cf5c6` | — (JVM) | — | Medido: 100 % (sin umbral) | `salidas/F2-cobertura.txt` |
| 8 | Distinguibilidad | 2026-09-24 | `94cf5c6` | — (JVM) | — | Medido: 18 pares idénticos (sin umbral) | `salidas/F2-cobertura.txt` |
| 9 | Latencia pictograma → voz | 2026-09-26 | *[pendiente]* | *[CONFIRMAR modelo]* | *[CONFIRMAR]* | Medido: 31,6 ms de media en síntesis nueva | `datos/latencias.csv` |
| 10 | Funcionamiento sin red | *[pendiente]* | | | | | |
| 11 | Ausencia de permiso de internet | *[pendiente]* | | | | | |
| 12 | Persistencia de la configuración | *[pendiente]* | | | | | |
| 13 | Accesibilidad | *[pendiente]* | | | | | |

### Sobre el criterio 4

Es el único automatizado que no cumple, y se dejó así deliberadamente. `HOT` usa
`reach = 0,35 + 0,65·w`, con `w` entre −1 y 1, lo que da un tercio del ciclo con
extensión de brazo negativa. No se arregló porque la lectura correcta no está
decidida: puede ser un `up` copiado como `w` desde `DRINK`, que usa las mismas
dos constantes, o puede ser deliberado, porque abanicarse **es** un vaivén.

Declarar un criterio en rojo y explicar por qué es más defendible que ajustarlo
para que pase.

### Sobre los criterios 7 y 8

No llevan umbral a propósito. Fijar un umbral después de ver la salida sería
elegir el número que la prueba ya aprueba. Se declaran como **medidos**, no como
cumplidos.

---

## G.2 Protocolo de verificación manual

Los cuatro criterios que no se pueden automatizar. Cada uno indica qué hacer, qué
debería pasar y qué guardar como evidencia.

### Criterio 10 — Funcionamiento sin red

Verifica la afirmación central del proyecto: que la app funciona sin conexión.

1. Instalar la app y abrirla una vez con red, para que el motor de voz de Android
   termine de inicializarse.
2. Cerrarla por completo desde la vista de recientes.
3. **Activar el modo avión.** Confirmar que el icono aparece en la barra de estado.
4. Abrir la app.
5. Elegir un lugar, entrar a una categoría y tocar cuatro pictogramas seguidos.
6. Comprobar que **se oye la voz** y que **el avatar se anima** en los cuatro.
7. Tocar "Decir" para que relea la oración acumulada.
8. Entrar a Ajustes y cambiar el color de ropa del avatar.

**Debe pasar:** todo funciona igual que con red. Sin errores, sin esperas
inusuales, sin avisos de conexión.

**Riesgo conocido:** algunos motores de voz de Android usan voces que requieren
red. Si no se oye nada, hay que verificar en Ajustes que la voz elegida sea una
instalada localmente. **Eso no es un fallo de la app**, pero sí una limitación
del entorno que conviene documentar en la tesis.

**Evidencia:** captura de pantalla con el icono de modo avión visible y el avatar
animado en la misma imagen.

### Criterio 11 — Ausencia de permiso de internet

Verifica que la afirmación anterior es estructural y no casual.

1. En Android Studio, abrir `app/src/main/AndroidManifest.xml`.
2. Ir a la pestaña **Merged Manifest**, abajo del editor.
3. Buscar en el panel izquierdo cualquier elemento `<uses-permission>`.

**Debe pasar:** no aparece `android.permission.INTERNET` ni
`ACCESS_NETWORK_STATE`. El manifiesto combinado es lo que hay que mirar y no el
propio: una biblioteca podría inyectar un permiso que el manifiesto del proyecto
no declara.

**Evidencia:** captura de la pestaña Merged Manifest mostrando la lista completa
de permisos.

**Nota:** la eliminación del `FileProvider` junto con la exportación de GIF
simplificó este manifiesto. Conviene rehacer la captura después de ese cambio.

### Criterio 12 — Persistencia de la configuración

1. Abrir Ajustes y cambiar **cuatro** cosas de distinta naturaleza:
   - el tipo de avatar (persona ↔ robot),
   - un color de ropa,
   - el tamaño de los pictogramas,
   - la velocidad de la voz.
2. Volver al tablero y comprobar que los cambios se ven.
3. **Cerrar la app desde la vista de recientes**, no con el botón atrás. Deslizar
   la tarjeta para descartarla.
4. Volver a abrir la app.

**Debe pasar:** las cuatro configuraciones siguen aplicadas, y la app abre en
horizontal.

**Por qué desde recientes y no con atrás:** el botón atrás puede dejar el proceso
vivo, y entonces los valores vendrían de memoria y no de disco. Descartar la
tarjeta mata el proceso, que es lo que obliga a DataStore a releer.

**Evidencia:** dos capturas, antes de cerrar y después de reabrir.

### Criterio 13 — Accesibilidad

1. Instalar **Accessibility Scanner** desde Google Play en el dispositivo.
2. Activarlo en Ajustes → Accesibilidad.
3. Abrir AgustiSpeaks y tocar el botón flotante del escáner en:
   - la pantalla de elección de lugar,
   - el tablero con una categoría abierta,
   - la tarjeta de "Decir",
   - Ajustes.
4. Revisar las sugerencias de cada pantalla.

**Qué mirar, en orden de importancia para esta app:**

| Hallazgo | Por qué importa acá |
|---|---|
| Área táctil menor a 48 dp | un chico con dificultad motora falla el toque |
| Contraste de texto insuficiente | los pictogramas se leen de reojo |
| Etiqueta de contenido ausente | un lector de pantalla no puede nombrar el pictograma |
| Elementos superpuestos | toques que activan lo que no era |

**Se espera encontrar hallazgos.** Lo honesto en la tesis es reportar cuántos y
de qué tipo, no afirmar que no hay ninguno. Un cero limpio en esta herramienta
sería sospechoso en una app que dibuja toda su interfaz a mano.

**Evidencia:** captura del informe del escáner por pantalla, con el número de
sugerencias visible.

---

## G.3 Cómo citar un resultado

Cada fila de la tabla tiene que poder reproducirse. El procedimiento:

1. `git checkout <commit>` deja el árbol en el estado exacto que se midió.
2. Borrar `app/src/main/assets/animaciones.json` y correr `PoseCatalogDumpTest`,
   que lo regenera.
3. Correr la prueba del criterio.
4. Comparar con el archivo de `docs/tesis/salidas/`.

Las salidas se guardan en `docs/` y no en `build/` justamente por esto: `build/`
está ignorado por git, y un resultado que no se puede atar a un commit no es
evidencia.

**Límite que hay que declarar:** el historial de git empieza el 2026-09-20. Todo
el desarrollo anterior está comprimido en el commit inicial, de modo que los
criterios no se pueden reproducir contra estados previos a esa fecha.
