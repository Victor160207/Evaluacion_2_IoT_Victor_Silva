package com.example.ejercicio

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import java.util.Locale

class ActivadoresActivity : PantallaProtegida() {

    private lateinit var txtEstadoActivador: TextView
    private lateinit var txtModoLed: TextView
    private lateinit var txtUmbralActual: TextView
    private lateinit var edtUmbral: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!exigirSesion()) return

        setContentView(R.layout.activity_activadores)
        aplicarInsets(findViewById(R.id.raiz))

        txtEstadoActivador = findViewById(R.id.txtEstadoActivador)
        txtModoLed = findViewById(R.id.txtModoLed)
        txtUmbralActual = findViewById(R.id.txtUmbralActual)
        edtUmbral = findViewById(R.id.edtUmbral)

        findViewById<Button>(R.id.btnLedOn).setOnClickListener {
            enviarComando("/led/on")
        }

        findViewById<Button>(R.id.btnLedOff).setOnClickListener {
            enviarComando("/led/off")
        }

        findViewById<Button>(R.id.btnLedAuto).setOnClickListener {
            enviarComando("/led/auto")
        }

        findViewById<Button>(R.id.btnFijarUmbral).setOnClickListener {
            fijarUmbral()
        }

        findViewById<Button>(R.id.btnVolverActivadores).setOnClickListener {
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        if (Sesion.usuario == null) return
        refrescarEstado()
    }

    /** Lee /estado para mostrar el modo del LED y el umbral que tiene el ESP32. */
    private fun refrescarEstado() {
        EspClient.get(
            "/estado",
            onOk = { respuesta ->
                Lectura.desde(respuesta)?.let {
                    txtModoLed.text = "Modo del LED: ${it.modo}"
                    txtUmbralActual.text =
                        "Umbral de alarma: ${"%.1f".format(Locale.US, it.umbral)} °C"
                }
            },
            onError = { /* el mensaje principal lo muestran los botones */ }
        )
    }

    private fun enviarComando(ruta: String) {
        txtEstadoActivador.text = "Enviando comando..."

        EspClient.get(
            ruta,
            onOk = { respuesta ->
                txtEstadoActivador.text = "Estado: $respuesta"
                refrescarEstado()
            },
            onError = { mensaje ->
                txtEstadoActivador.text = "Estado: $mensaje"
            }
        )
    }

    private fun fijarUmbral() {
        val texto = edtUmbral.text.toString().trim().replace(',', '.')
        val valor = texto.toFloatOrNull()

        if (valor == null || valor < 1f || valor > 100f) {
            txtEstadoActivador.text = "Estado: Ingrese un umbral entre 1 y 100 °C"
            return
        }

        enviarComando(EspClient.formatoRuta("/umbral", valor))
        edtUmbral.text.clear()
    }
}
