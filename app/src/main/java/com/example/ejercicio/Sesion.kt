package com.example.ejercicio

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat


object Sesion {
    var usuario: String? = null
}

abstract class PantallaProtegida : AppCompatActivity() {

    /** Devuelve false (y redirige al Login) si no hay sesión iniciada. */
    protected fun exigirSesion(): Boolean {
        if (Sesion.usuario != null) return true
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
        return false
    }

    /** Evita que el contenido quede bajo la barra de estado / navegación. */
    protected fun aplicarInsets(raiz: View) {
        val izq = raiz.paddingLeft
        val arr = raiz.paddingTop
        val der = raiz.paddingRight
        val aba = raiz.paddingBottom
        ViewCompat.setOnApplyWindowInsetsListener(raiz) { v, insets ->
            val b = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(izq + b.left, arr + b.top, der + b.right, aba + b.bottom)
            insets
        }
    }
}
