package com.tunombre.agustispeaks.phrase

/**
 * Lo que hace la app cuando se toca un pictograma, en este orden:
 *
 * 1. agrega su texto a la oración;
 * 2. avisa la pulsación, que es donde arranca la medición de latencia;
 * 3. entrega al motor de voz **la oración entera**, no la palabra suelta;
 * 4. la registra en Recientes.
 *
 * El punto 3 es la regla central del producto, y es lo que verifica el
 * criterio 9 de la tesis. Por eso esto vive fuera de la pantalla: el tablero
 * lo llama tal cual, y la prueba lo ejercita en la JVM con un motor de voz de
 * mentira en lugar del de Android. Si la regla viviera dentro de la pantalla,
 * la prueba solo podría verificar una copia.
 *
 * @return la oración que se entregó al motor de voz.
 */
fun tapPictogram(
    text: String,
    phrase: PhraseController,
    speak: (String) -> Unit,
    record: (String) -> Unit,
    onPress: (String) -> Unit = {}
): String {
    val sentence = phrase.addWord(text)
    onPress(sentence)
    speak(sentence)
    record(sentence)
    return sentence
}
