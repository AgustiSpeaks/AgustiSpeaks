package com.tunombre.agustispeaks.data.repository

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * La contraseña del modo edición.
 *
 * **No se guarda la contraseña, sino una huella de ella** (PBKDF2 con
 * HMAC-SHA256 y una sal al azar). Para comprobarla se calcula la huella de lo
 * que se escribió y se compara con la guardada. Así, quien llegara a leer los
 * datos de la app no tendría la contraseña, y la sal hace que dos teléfonos
 * con la misma contraseña guarden huellas distintas.
 *
 * Se guarda en el formato `pbkdf2-sha256:<iteraciones>:<sal>:<huella>`, con la
 * sal y la huella en Base64. Las iteraciones van en el texto para poder
 * subirlas más adelante sin invalidar las contraseñas ya creadas.
 *
 * Calcular la huella tarda a propósito (decenas de milisegundos en un
 * teléfono): hay que llamarlo fuera del hilo principal. Son funciones puras,
 * sin Android, para poder probarlas en la JVM.
 */
internal object EditPassword {

    const val MIN_LENGTH = 4

    private const val PREFIX = "pbkdf2-sha256"
    private const val ITERATIONS = 120_000
    private const val SALT_BYTES = 16
    private const val KEY_BITS = 256

    fun isValid(password: String): Boolean = password.length >= MIN_LENGTH

    fun hash(password: String): String {
        val salt = ByteArray(SALT_BYTES).also { SecureRandom().nextBytes(it) }
        val key = derive(password, salt, ITERATIONS)
        val b64 = Base64.getEncoder()
        return listOf(PREFIX, ITERATIONS.toString(), b64.encodeToString(salt), b64.encodeToString(key))
            .joinToString(":")
    }

    /** Si [password] es la que dejó [stored]. Un valor guardado ilegible nunca coincide. */
    fun verify(password: String, stored: String?): Boolean {
        val parts = stored?.split(':') ?: return false
        if (parts.size != 4 || parts[0] != PREFIX) return false
        val iterations = parts[1].toIntOrNull()?.takeIf { it > 0 } ?: return false
        val b64 = Base64.getDecoder()
        val salt = runCatching { b64.decode(parts[2]) }.getOrNull() ?: return false
        val expected = runCatching { b64.decode(parts[3]) }.getOrNull() ?: return false
        // Comparación de tiempo constante: no deja adivinar la huella por
        // cuánto tarda en fallar.
        return MessageDigest.isEqual(derive(password, salt, iterations), expected)
    }

    private fun derive(password: String, salt: ByteArray, iterations: Int): ByteArray {
        val spec = PBEKeySpec(password.toCharArray(), salt, iterations, KEY_BITS)
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
        }
    }
}
