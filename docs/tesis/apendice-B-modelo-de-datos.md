# Apéndice B — Modelo de datos

Seis entidades. Ninguna se guarda en una base relacional salvo el registro del
catálogo: el vocabulario viaja compilado en la semilla y la configuración en
DataStore.

---

## B.1 Vocabulario

### Lugar (`data.model.Place`)

Dónde está el chico. Determina qué categorías se muestran.

| Campo | Tipo | Descripción |
|---|---|---|
| `id` | `String` | identificador estable |
| `name` | `String` | nombre visible |
| `icon` | `String` | nombre lógico del icono, se resuelve en la capa de UI |
| `colorHex` | `String` | color asociado, `"#RRGGBB"` |
| `categoryIds` | `List<String>` | categorías disponibles en este lugar |

### Categoría (`data.model.Category`)

| Campo | Tipo | Descripción |
|---|---|---|
| `id` | `String` | identificador estable |
| `name` | `String` | nombre visible |
| `icon` | `String` | nombre lógico del icono |
| `colorHex` | `String` | color pastel propio de la categoría |

### Pictograma (`data.model.Pictogram`)

| Campo | Tipo | Por defecto | Descripción |
|---|---|---|---|
| `id` | `String` | — | identificador estable |
| `text` | `String` | — | palabra que se lee por voz y se muestra |
| `icon` | `String` | — | nombre lógico del icono |
| `categoryId` | `String` | — | categoría a la que pertenece |
| `placeIds` | `List<String>` | — | lugares donde aparece |
| `isFavorite` | `Boolean` | `false` | marcado por el usuario |
| `isCustom` | `Boolean` | `false` | creado desde Ajustes, no viene de la semilla |
| `gifUrl` | `String?` | `null` | campo inactivo |

**Dos campos están declarados y no se usan.** `isFavorite` y `isCustom` existen
en el modelo pero ninguna pantalla los escribe, y `gifUrl` quedó sin objeto al
quitarse la exportación de GIF. Conviene declararlos como tales en la tesis en
lugar de presentarlos como funcionalidad.

`placeIds` **se deriva**, no se escribe a mano: un pictograma aparece en todo
lugar cuya lista de categorías incluya la suya. Esa derivación es la que dejó
diez pictogramas inalcanzables hasta que la prueba de inventario los encontró.

## B.2 Configuración

### Avatar (`data.model.AvatarConfig`)

Dieciséis campos. Los de estilo son claves internas que interpreta el
renderizador; los colores van en hexadecimal **sin** `#`.

| Grupo | Campos |
|---|---|
| Tipo | `kind` (`PERSONA` \| `ROBOT`), `gender` (`NEUTRO` \| `MASCULINO` \| `FEMENINO`) |
| Persona | `skinColor`, `hairStyle`, `hairColor`, `eyebrows`, `eyes`, `mouth` |
| Ropa | `clothing`, `clothingColor`, `pantsColor`, `shoeColor` |
| Robot | `robotColor`, `robotEyes`, `robotMouth`, `robotFace`, `robotAccessory` |

La ropa aplica a los dos tipos: en el robot pinta el chasis, las placas de las
piernas y las botas. El robot solo está disponible con género neutro.

### Preferencias (`data.model.UserSettings`)

| Campo | Tipo | Por defecto | Descripción |
|---|---|---|---|
| `interfaceLanguage` | `String` | `"es"` | idioma de la interfaz |
| `ignoreRepeatedTaps` | `Boolean` | `false` | ignora toques repetidos muy seguidos |
| `activateOnRelease` | `Boolean` | `true` | activa al soltar, no al tocar |
| `darkMode` | `Boolean` | `false` | fuerza el tema oscuro |
| `highContrast` | `Boolean` | `false` | esquema de alto contraste |
| `textSize` | `TextSizeOption` | `NORMAL` | tamaño de los pictogramas |
| `voicePitch` | `Float` | `1.0` | tono de voz |
| `voiceRate` | `Float` | `1.0` | velocidad del habla |
| `voiceVolume` | `Float` | `1.0` | volumen |
| `voiceId` | `String?` | `null` | voz del sistema elegida |
| `gridColumns` | `Int?` | `null` | columnas forzadas; `null` = según pantalla |

Tres de estos campos son de accesibilidad y conviene señalarlos: `ignoreRepeatedTaps`
y `activateOnRelease` atienden dificultades motoras, y `textSize` con
`highContrast` atienden dificultades visuales.

`TextSizeOption` es un enum de cinco pasos con un multiplicador: `NORMAL` 1,0 ·
`GRANDE` 1,15 · `MAS_GRANDE` 1,3 · `EXTRA` 1,5 · `MAXIMO` 1,75.

Se persisten con **DataStore Preferences**, no con Room: son pares clave-valor
sin relaciones, y Room aquí sería una base de datos para guardar once escalares.

## B.3 Catálogo de animaciones

### Registro guardado (`avatar.catalog.AnimationEntity`)

Es la única tabla Room de la app.

| Campo | Tipo | Descripción |
|---|---|---|
| `id` | `String` (clave primaria) | id de la animación, p. ej. `"comer"` |
| `json` | `String` | el registro entero, como su propio JSON |
| `actualizado` | `Long` | marca de tiempo de la última escritura |

**El registro se guarda como documento, no desarmado en tablas.** Es una decisión
de diseño que conviene defender explícitamente:

Una animación se edita entera y la única búsqueda que hace la app es por id. No
hay nada que ganar normalizándola, y sí bastante que perder. Desarmada, el
formato quedaría definido en dos lugares —el parser y el esquema de la base— y
agregar un campo obligaría a migrar la base además de tocar el parser. Así, **el
parser es la única definición del formato** y la base no se entera cuando el
formato crece.

### Registro en memoria (`avatar.catalog.AnimationRecord`)

Lo que el parser produce a partir de ese JSON.

| Campo | Tipo | Por defecto | Descripción |
|---|---|---|---|
| `id` | `String` | — | identificador |
| `loopMillis` | `Int` | — | duración de una vuelta del ciclo |
| `prioridad` | `Int` | `100` | orden de evaluación de sus disparadores |
| `palabras` | `List<String>` | vacía | dispara por palabra completa |
| `raices` | `List<String>` | vacía | dispara por subcadena |
| `ademas` | `List<String>` | vacía | condición adicional, palabra completa |
| `ademasRaices` | `List<String>` | vacía | condición adicional, subcadena |
| `excluye` | `List<String>` | vacía | veta por palabra completa |
| `excluyeRaices` | `List<String>` | vacía | veta por subcadena |
| `usaParteDelCuerpo` | `Boolean` | `false` | la parte nombrada viaja a la pose |
| `prop` | `Prop?` | `null` | objeto por defecto |
| `procedural` | `String?` | `null` | nombre del generador en Kotlin |
| `cicloCerrado` | `Boolean` | `true` | si el último fotograma vuelve al primero |
| `fotogramas` | `List<Keyframe>` | vacía | los instantes declarados |
| `orden` | `Int` | `0` | posición en el archivo; desempata prioridades |

Las seis listas de disparo siguen **una sola regla**: sin sufijo es palabra
completa, con sufijo `Raices` es subcadena.

### Fotograma (`avatar.catalog.Keyframe`)

| Campo | Tipo | Descripción |
|---|---|---|
| `t` | `Float` | instante dentro del ciclo, 0 a 1 |
| `ease` | `Ease?` | curva de salida hacia el fotograma siguiente |
| `floats` | `Map<FloatField, Float>` | campos continuos declarados |
| `steps` | `Map<StepField, Any?>` | campos discretos declarados |

Un fotograma **solo dice lo que cambia**: lo que no aparece se hereda.

**Un detalle del formato que conviene citar.** En `steps`, que una clave esté
presente con valor `null` no es lo mismo que que la clave falte:

- presente con `null` → **apaga** el campo (borra la expresión de la cara);
- ausente → "no opino, dejá lo que había".

Sin esa distinción no habría forma de apagar nada a mitad de un ciclo.

## B.4 Dónde vive cada cosa

| Dato | Almacenamiento | Motivo |
|---|---|---|
| Vocabulario (170 pictogramas) | código, `SeedData` | no cambia en ejecución |
| Catálogo base (88 animaciones) | `assets/animaciones.json` | viaja en el APK |
| Correcciones del especialista | Room, una tabla | se editan en el dispositivo |
| Preferencias y avatar | DataStore Preferences | pares clave-valor |
| Frases recientes | DataStore Preferences | lista corta |

Nada se guarda fuera del dispositivo. La app no declara permiso de internet.
