package com.example.ejercicio

import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object Seguridad {

    private const val ITERACIONES = 120_000
    private const val LARGO_HASH_BITS = 256
    private const val LARGO_SAL_BYTES = 16

    fun nuevaSal(): String {
        val sal = ByteArray(LARGO_SAL_BYTES)
        SecureRandom().nextBytes(sal)
        return Base64.encodeToString(sal, Base64.NO_WRAP)
    }

    fun hash(password: String, salBase64: String): String {
        val sal = Base64.decode(salBase64, Base64.NO_WRAP)
        val spec = PBEKeySpec(password.toCharArray(), sal, ITERACIONES, LARGO_HASH_BITS)
        val fabrica = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val bytes = fabrica.generateSecret(spec).encoded
        spec.clearPassword()
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    /** Comparación en tiempo constante para no filtrar información por tiempos. */
    fun verificar(password: String, salBase64: String, hashGuardado: String): Boolean {
        val calculado = hash(password, salBase64).toByteArray()
        return MessageDigest.isEqual(calculado, hashGuardado.toByteArray())
    }
}
