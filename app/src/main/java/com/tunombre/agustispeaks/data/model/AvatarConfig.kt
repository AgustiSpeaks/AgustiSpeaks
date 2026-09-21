package com.tunombre.agustispeaks.data.model

/**
 * Configuración del avatar del niño. Los valores de estilo son claves internas
 * que interpreta `avatar/AvatarView.kt` (dibujo nativo con Compose). Los
 * colores son hex SIN '#'.
 *
 * - [kind] == PERSONA: cara + pelo + ojos + boca + ropa.
 * - [kind] == ROBOT: solo disponible con género NEUTRO. Usa cabeza, carátula y
 *   accesorio propios; sus extremidades y dedos se dibujan en acero.
 *
 * La ropa ([clothingColor], [pantsColor], [shoeColor]) aplica a TODOS los
 * avatares: en el robot pinta el chasis, las placas de las piernas y las botas.
 */
data class AvatarConfig(
    val kind: AvatarKind = AvatarKind.PERSONA,
    val gender: AvatarGender = AvatarGender.NEUTRO,
    // Persona
    val skinColor: String = "f1c27d",
    val hairStyle: String = "classic",
    val hairColor: String = "2c1b18",
    val eyebrows: String = "normal",
    val eyes: String = "normal",
    val mouth: String = "smile",
    // Ropa (persona y robot)
    val clothing: String = "sweater",
    val clothingColor: String = "74c0fc",
    val pantsColor: String = "3f5d75",
    val shoeColor: String = "3a3a44",
    // Robot
    val robotColor: String = "6c5ce7",
    val robotEyes: String = "round",
    val robotMouth: String = "grille",
    val robotFace: String = "plate",
    val robotAccessory: String = "antenna"
)

enum class AvatarKind { PERSONA, ROBOT }

enum class AvatarGender { NEUTRO, MASCULINO, FEMENINO }
