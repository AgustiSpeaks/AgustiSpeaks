package com.tunombre.agustispeaks.avatar

/**
 * Parte del cuerpo hacia la que apunta / se lleva la mano del avatar.
 * Las coordenadas son fracciones del lienzo (mismas que usa AvatarRenderer).
 */
enum class BodyPart(val fx: Float, val fy: Float) {
    HEAD(0.5f, 0.20f),
    FOREHEAD(0.5f, 0.30f),
    HAIR(0.5f, 0.13f),
    EYE(0.42f, 0.42f),
    NOSE(0.5f, 0.46f),
    CHEEK(0.64f, 0.46f),
    EAR(0.74f, 0.44f),
    MOUTH(0.5f, 0.55f),
    TOOTH(0.44f, 0.55f),
    THROAT(0.5f, 0.63f),
    CHEST(0.5f, 0.75f),
    BELLY(0.5f, 0.93f),
    SHOULDER(0.30f, 0.66f),
    ARM(0.22f, 0.80f),
    HAND(0.30f, 0.92f),
    LEG(0.42f, 1.00f),
    FOOT(0.44f, 1.04f),
    BACK(0.72f, 0.80f)
}
