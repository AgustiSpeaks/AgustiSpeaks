package com.tunombre.agustispeaks.avatar.catalog

import com.tunombre.agustispeaks.avatar.AvatarAction
import com.tunombre.agustispeaks.avatar.AvatarPose
import com.tunombre.agustispeaks.avatar.ResolvedAction
import com.tunombre.agustispeaks.avatar.avatarPoseFor

/**
 * La escapatoria procedural: animaciones que son matemática y no fotogramas.
 *
 * Once acciones tiemblan, frotan o mastican a ocho, nueve o dieciséis ciclos
 * por bucle. Ninguna cantidad razonable de fotogramas las representa, y forzar
 * el intento terminaría inventando un lenguaje de programación adentro del JSON.
 * El registro solo lleva el nombre; el movimiento vive en Kotlin.
 *
 * Y vive donde ya vivía: estos generadores **delegan en `avatarPoseFor`**. No
 * duplican una sola línea de la matemática original, así que son idénticos por
 * construcción, no por parecido. La ganancia del catálogo acá no es sacar el
 * código de Kotlin —eso no se puede— sino darle un nombre al que un registro
 * pueda apuntar.
 */
object ProceduralPoses {

    /**
     * Por qué cada una es procedural:
     *
     * - `fast` = sin(t·8·2π), ocho ciclos: dolor y descompuesto, donde el
     *   temblor rápido ES el significado. Lavarse, cepillarse y bañarse usaban
     *   la misma señal por error —ahí el temblor se leía como una falla, no
     *   como frotarse— y pasaron a un gesto lento de tres pasadas, que además
     *   las devolvió al catálogo declarativo.
     * - `shiver` = sin(t·16·2π), dieciséis ciclos: frío, nervioso, miedo.
     * - `chew`, siete ciclos rectificados: comer.
     * - sin(18πt), nueve ciclos: dibujar.
     * - `propPhase = t`, un diente de sierra que salta en el borde del bucle:
     *   escuchar, después y antes. En las ondas de la bocina y en las agujas
     *   del reloj el salto del final es invisible —una vuelta entera termina
     *   donde empezó— pero interpolarlo las haría girar al revés justo ahí, que
     *   en "después" y "antes" es exactamente el significado opuesto.
     * - una mandíbula que salta entre dos valores fijos: remedio. Interpolar
     *   entre dos valores es lo contrario de saltar entre dos valores, así que
     *   ninguna cantidad de fotogramas lo describe. Es el mismo `if` que ya
     *   hacía procedural a comer; que estuviera de este lado fue un descuido
     *   que encontró el test de paridad.
     */
    val byAction: Map<AvatarAction, String> = mapOf(
        AvatarAction.HURT to "dolor",
        AvatarAction.SICK to "descompuesto",
        AvatarAction.COLD to "frio",
        AvatarAction.NERVOUS to "nervioso",
        AvatarAction.SCARED to "miedo",
        AvatarAction.EAT to "comer",
        AvatarAction.SCRIBBLE to "dibujar",
        AvatarAction.LISTEN to "escuchar",
        AvatarAction.MEDICINE to "remedio",
        AvatarAction.LATER to "despues",
        AvatarAction.BEFORE to "antes"
    )

    private val byName: Map<String, AvatarAction> =
        byAction.entries.associate { (accion, nombre) -> nombre to accion }

    fun exists(nombre: String): Boolean = byName.containsKey(nombre)

    fun names(): Set<String> = byName.keys

    /** null si el nombre no existe, para que el intérprete pueda caer al plan B. */
    fun generate(nombre: String, t: Float, r: ResolvedAction): AvatarPose? {
        val accion = byName[nombre] ?: return null
        return avatarPoseFor(ResolvedAction(accion, r.bodyPart, r.prop), t)
    }
}
