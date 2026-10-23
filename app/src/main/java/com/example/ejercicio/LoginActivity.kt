package com.example.ejercicio

import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var edtUsuario: EditText
    private lateinit var edtPassword: EditText
    private lateinit var btnIngresar: Button
    private lateinit var btnRegistrar: Button
    private lateinit var txtMensajeLogin: TextView
    private lateinit var db: DbHelper

    private var intentosFallidos = 0
    private var bloqueadoHasta = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        db = DbHelper(this)

        edtUsuario = findViewById(R.id.edtUsuario)
        edtPassword = findViewById(R.id.edtPassword)
        btnIngresar = findViewById(R.id.btnIngresar)
        btnRegistrar = findViewById(R.id.btnRegistrar)
        txtMensajeLogin = findViewById(R.id.txtMensajeLogin)

        btnIngresar.setOnClickListener { validarIngreso() }
        btnRegistrar.setOnClickListener { registrarCuenta() }
    }

    private fun errorDeCampos(usuario: String, password: String): String? {
        if (usuario.isEmpty() || password.isEmpty()) return "Complete usuario y contraseña"
        return null
    }

    private fun validarIngreso() {
        val ahora = SystemClock.elapsedRealtime()
        if (ahora < bloqueadoHasta) {
            val seg = (bloqueadoHasta - ahora) / 1000 + 1
            txtMensajeLogin.text = "Demasiados intentos. Espere $seg s"
            return
        }

        val usuario = edtUsuario.text.toString().trim()
        val password = edtPassword.text.toString()

        errorDeCampos(usuario, password)?.let {
            txtMensajeLogin.text = it
            return
        }

        setBotones(false)
        txtMensajeLogin.text = "Verificando..."

        Thread {
            val valido = db.validarUsuario(usuario, password)
            runOnUiThread {
                setBotones(true)
                if (valido) {
                    intentosFallidos = 0
                    Sesion.usuario = usuario.lowercase()
                    edtPassword.text.clear()
                    txtMensajeLogin.text = ""
                    irAlPanel()
                } else {
                    intentosFallidos++
                    if (intentosFallidos >= 5) {
                        intentosFallidos = 0
                        bloqueadoHasta = SystemClock.elapsedRealtime() + 30_000
                        txtMensajeLogin.text = "Demasiados intentos. Espere 30 s"
                    } else {
                        txtMensajeLogin.text = "Credenciales incorrectas"
                    }
                }
            }
        }.start()
    }

    private fun registrarCuenta() {
        val usuario = edtUsuario.text.toString().trim()
        val password = edtPassword.text.toString()

        errorDeCampos(usuario, password)?.let {
            txtMensajeLogin.text = it
            return
        }
        if (usuario.length < 3 || !usuario.all { it.isLetterOrDigit() }) {
            txtMensajeLogin.text = "Usuario: mínimo 3 caracteres, solo letras y números"
            return
        }
        if (password.length < 6) {
            txtMensajeLogin.text = "La contraseña debe tener al menos 6 caracteres"
            return
        }

        setBotones(false)
        txtMensajeLogin.text = "Creando cuenta..."

        Thread {
            val creado = db.registrarUsuario(usuario, password)
            runOnUiThread {
                setBotones(true)
                txtMensajeLogin.text =
                    if (creado) "Cuenta creada. Ahora puede ingresar"
                    else "Ese usuario ya existe"
            }
        }.start()
    }

    private fun setBotones(habilitados: Boolean) {
        btnIngresar.isEnabled = habilitados
        btnRegistrar.isEnabled = habilitados
    }

    private fun irAlPanel() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
