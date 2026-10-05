# Apéndice C — Resolvedor de acción

Convierte una frase en español en una acción del avatar. Cien por ciento offline,
por palabras clave, sin modelo de lenguaje ni red.

Entrada: la **oración acumulada entera**, no la última palabra. Salida: una
`ResolvedAction` con acción, parte del cuerpo y objeto.

---

## C.1 El algoritmo

```
resolver(frase):
    si frase vacía → IDLE

    texto ← normalizar(frase)
    parte ← parteDelCuerpo(texto)

    # 1. Primero el catálogo, por prioridad ascendente
    para cada regla del catálogo, ordenada por (prioridad, orden en el archivo):
        si regla.coincide(texto) → devolver acción de esa regla

    # 2. Después las reglas en código, en el orden en que están escritas
    para cada sección S de las 19 secciones de ActionResolver:
        para cada regla R de S:
            si R.coincide(texto) → devolver R.acción

    # 3. Respaldo
    devolver TALK
```

### Normalización

```
normalizar(frase):
    minúsculas
    descomponer en NFD                  # separa la letra de su tilde
    borrar todo carácter de la clase \p{Mn}   # borra las tildes sueltas
    envolver entre espacios             # " frase "
```

Los espacios al principio y al final no son cosmética: son lo que permite que la
coincidencia por palabra completa se implemente como una búsqueda de subcadena.

**La normalización es la fuente de casi todas las dificultades del resolvedor.**
Al quitar las tildes, `"cómo"` y `"como"` pasan a ser la misma palabra, y `"ñ"`
pasa a ser `"n"`. Eso convierte el orden de evaluación en una propiedad de
corrección: si la regla de comer se evaluara antes que la de saludar, *"¿Cómo
estás?"* mostraría a alguien masticando.

### Los dos comparadores

```
palabraCompleta(texto, clave) ≡ texto contiene " clave "
subcadena(texto, clave)       ≡ texto contiene "clave"
```

Se eligen caso por caso, y la elección importa:

- `"te"` tiene que ser **palabra completa**, porque vive dentro de `"triste"`.
- `"lavar"` tiene que ser **subcadena**, para cubrir *lavarme*, *lavarse*, *lavo*.

### Orden por prioridad en el catálogo

Cada registro del catálogo lleva una `prioridad` entera. Las reglas se evalúan de
menor a mayor, y a igual prioridad manda el orden en el archivo.

El orden en el archivo **no alcanza como único criterio**. Las cuatro reglas
migradas llevan prioridad explícita 0, 1, 2 y 3 porque si compartieran prioridad
el desempate sería el orden del enum, donde `ASK` va última, y entonces la frase
*"¿Cómo estás? Bien"* caería en `GOOD` en vez de en `ASK`.

## C.2 Fragmento real

De `avatar/ActionResolver.kt`. La sección 0 y los dos comparadores.

```kotlin
fun resolve(phrase: String): ResolvedAction {
    if (phrase.isBlank()) return ResolvedAction(AvatarAction.IDLE)
    val t = normalized(phrase)
    val part = bodyPart(t)

    /* 0) Social y estado. Va primero porque al quitar las tildes "cómo" (la
          pregunta) y "como" (yo como) quedan idénticos, y la regla de comer
          busca subcadenas: sin esto "¿Cómo estás?" salía comiendo. */
    when {
        has(t, "como estas", "como te sientes", "como andas") -> return R(AvatarAction.ASK)
        has(t, "por favor") -> return R(AvatarAction.PLEASE)
        w(t, "bien") || has(t, "estoy bien", "muy bien") -> return R(AvatarAction.GOOD)
        w(t, "mal") || has(t, "estoy mal", "me siento mal") -> return R(AvatarAction.BAD)
    }
    // … 18 secciones más …
    return R(AvatarAction.TALK)
}

/** Contiene la subcadena tal cual (para raíces y frases de varias palabras). */
private fun has(t: String, vararg keys: String) = keys.any { t.contains(it) }

/** Coincidencia de palabra completa (t viene con espacios a ambos lados). */
private fun w(t: String, vararg keys: String) = keys.any { t.contains(" $it ") }

private fun normalize(s: String): String =
    Normalizer.normalize(s.lowercase(), Normalizer.Form.NFD)
        .replace("\\p{Mn}+".toRegex(), "")
```

## C.3 Las diecinueve secciones, en orden de evaluación

| # | Sección | Reglas | Por qué está donde está |
|---|---|---|---|
| 0 | Social y estado | 4 | antes que comer, por `"cómo"` / `"como"` |
| 1 | Bebidas | 6 | antes que todo, porque `"te"` es de dos letras |
| 2 | Higiene | 5 | |
| 3 | Salud | 7 | |
| 3.5 | Alimentos | 1 | antes del verbo comer, o *"quiero comer pan"* pierde el pan |
| 3.6 | Ropa | 1 | |
| 3.7 | Compras y escuela | 12 | sustantivos antes que verbos |
| 3.8 | Juego | 5 | antes de la regla genérica de jugar |
| 3.9 | Tiempo y cielo | 7 | |
| 3.10 | Sueltos | 3 | |
| 3.11 | Lugares | 8 | |
| 3.12 | Familia y escuela | 11 | |
| 4 | Verbos | 27 | |
| 5 | Parte del cuerpo sin verbo | 1 | |
| 6 | Social | 6 | |
| 7 | Respuestas y cantidad | 5 | |
| 8 | Emociones | 10 | |
| 9 | Pronombres | 2 | **última, y nunca antes** |
| — | Respaldo `TALK` | (1) | |

**Total: 121 reglas de disparo** en código, más 4 duplicadas en el catálogo.

### Por qué los pronombres van al final

*"Yo"* y *"tú"* solo significan algo cuando están solos. La app arma la oración
entera, así que *"yo quiero agua"* tiene que resolverse por el verbo. Si estas
reglas estuvieran arriba, cualquier frase que empiece con *"Yo"* mostraría al
avatar señalándose el pecho en vez de haciendo lo que pidió.

## C.4 Tablas auxiliares

Tres funciones no eligen la **acción** sino el **matiz**:

| Tabla | Ramas | Qué elige |
|---|---|---|
| `foodProp` | 15 | qué alimento se dibuja en la mano |
| `garment` | 7 | qué prenda, y a qué parte del cuerpo va |
| `bodyPart` | 17 | qué parte del cuerpo nombra la frase |

Sumadas a las 121 reglas, el resolvedor toma **160 decisiones léxicas distintas**.

## C.5 Limitaciones conocidas

Las encontró el corpus del Apéndice F, no una inspección del código.

**Tres desvíos, los tres por orden de evaluación**, y apuntando en direcciones
distintas:

| Frase | Debería dar | Da | Causa |
|---|---|---|---|
| *"No quiero comer"* | `NO` | `EAT` | el verbo (sec. 4) gana a la negación (sec. 7) |
| *"Buscar mi juguete"* | `SEARCH` | `TOYS` | el sustantivo (sec. 3.8) gana al verbo (sec. 4) |
| *"Quiero llorar"* | `CRY` | `WANT` | el genérico *quiero* (sec. 7) gana a la emoción (sec. 8) |

Que apunten en direcciones opuestas es el hallazgo: **no existe un orden único
que los resuelva a los tres**. Es el costo estructural de un resolvedor por
reglas ordenadas, y es el argumento más fuerte a favor de que ese orden sea
explícito y esté verificado, en vez de ser emergente.
