package com.example.ejercicio

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView

class MainActivity : PantallaProtegida() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!exigirSesion()) return

        setContentView(R.layout.activity_main)
        aplicarInsets(findViewById(R.id.raiz))

        findViewById<TextView>(R.id.txtUsuario).text = "Usuario: ${Sesion.usuario}"

        val btnMonitoreo = findViewById<Button>(R.id.btnMonitoreo)
        val btnActivadores = findViewById<Button>(R.id.btnActivadores)
        val btnHistorial = findViewById<Button>(R.id.btnHistorial)
        val btnConexion = findViewById<Button>(R.id.btnConexion)
        val btnCerrarSesion = findViewById<Button>(R.id.btnCerrarSesion)

        btnMonitoreo.setOnClickListener {
            startActivity(Intent(this, MonitoreoActivity::class.java))
        }

        btnActivadores.setOnClickListener {
            startActivity(Intent(this, ActivadoresActivity::class.java))
        }

        btnHistorial.setOnClickListener {
            startActivity(Intent(this, HistorialActivity::class.java))
        }

        btnConexion.setOnClickListener {
            val intent = Intent(this, ConexionActivity::class.java)
            intent.putExtra("ORIGEN", "Anti Incendios")
            startActivity(intent)
        }

        btnCerrarSesion.setOnClickListener {
            Sesion.usuario = null
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
}
