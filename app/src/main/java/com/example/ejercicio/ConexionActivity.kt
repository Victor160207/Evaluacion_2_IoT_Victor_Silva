package com.example.ejercicio

import android.os.Bundle
import android.widget.Button
import android.widget.TextView

class ConexionActivity : PantallaProtegida() {

    private lateinit var txtEstadoConexion: TextView
    private lateinit var txtDispositivo: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!exigirSesion()) return

        setContentView(R.layout.activity_conexion)
        aplicarInsets(findViewById(R.id.raiz))

        val txtTituloConexion = findViewById<TextView>(R.id.txtTituloConexion)
        txtEstadoConexion = findViewById(R.id.txtEstadoConexion)
        txtDispositivo = findViewById(R.id.txtDispositivo)
        val txtInstrucciones = findViewById<TextView>(R.id.txtInstrucciones)
        val btnProbar = findViewById<Button>(R.id.btnProbarConexion)
        val btnVolver = findViewById<Button>(R.id.btnVolver)

        val origen = intent.getStringExtra("ORIGEN") ?: "Anti Incendios"
        txtTituloConexion.text = "CONEXIÓN - $origen"

        txtDispositivo.text = "ESP32: ${EspClient.IP_ESP32}  (red ${EspClient.SSID})"
        txtInstrucciones.text =
            "1. Conecte el teléfono a la red ${EspClient.SSID}.\n" +
                "2. Mantenga la conexión aunque indique que no hay Internet.\n" +
                "3. Presione PROBAR CONEXIÓN."

        btnProbar.setOnClickListener { probarConexion() }
        btnVolver.setOnClickListener { finish() }
    }

    private fun probarConexion() {
        txtEstadoConexion.text = "Estado: Probando..."

        EspClient.get(
            "/estado",
            onOk = { respuesta ->
                txtEstadoConexion.text =
                    if (Lectura.desde(respuesta) != null) "Estado: ESP32 conectado por Wi-Fi"
                    else "Estado: Respuesta inválida del dispositivo"
            },
            onError = { mensaje ->
                txtEstadoConexion.text = "Estado: $mensaje"
            }
        )
    }
}
