package com.example.ejercicio

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MonitoreoActivity : PantallaProtegida() {

    private lateinit var txtAlerta: TextView
    private lateinit var txtTemperatura: TextView
    private lateinit var txtHumedad: TextView
    private lateinit var txtLed: TextView
    private lateinit var txtUmbral: TextView
    private lateinit var txtEstadoSensor: TextView
    private lateinit var db: DbHelper

    private val handler = Handler(Looper.getMainLooper())
    private var activa = false
    private var ultimoGuardado = 0L

    private val ciclo = Runnable { consultar() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!exigirSesion()) return

        setContentView(R.layout.activity_monitoreo)
        aplicarInsets(findViewById(R.id.raiz))

        db = DbHelper(this)

        txtAlerta = findViewById(R.id.txtAlerta)
        txtTemperatura = findViewById(R.id.txtTemperatura)
        txtHumedad = findViewById(R.id.txtHumedad)
        txtLed = findViewById(R.id.txtLed)
        txtUmbral = findViewById(R.id.txtUmbral)
        txtEstadoSensor = findViewById(R.id.txtEstadoSensor)

        findViewById<Button>(R.id.btnActualizar).setOnClickListener { consultar() }
        findViewById<Button>(R.id.btnVolverMonitoreo).setOnClickListener { finish() }
    }

    override fun onResume() {
        super.onResume()
        if (Sesion.usuario == null) return
        activa = true
        consultar()
    }

    override fun onPause() {
        super.onPause()
        activa = false
        handler.removeCallbacks(ciclo)
    }

    private fun consultar() {
        handler.removeCallbacks(ciclo)
        txtEstadoSensor.text = "Estado: Consultando..."

        EspClient.get(
            "/estado",
            onOk = { respuesta ->
                val lectura = Lectura.desde(respuesta)
                if (lectura != null) mostrar(lectura) else mostrarError("Respuesta inválida")
                programarSiguiente()
            },
            onError = { mensaje ->
                mostrarError(mensaje)
                programarSiguiente()
            }
        )
    }

    private fun programarSiguiente() {
        handler.removeCallbacks(ciclo)   // evita ciclos duplicados
        if (activa) handler.postDelayed(ciclo, 3000)
    }

    private fun mostrar(l: Lectura) {
        txtTemperatura.text = "Temperatura: ${"%.1f".format(Locale.US, l.temperatura)} °C"
        txtHumedad.text = "Humedad: ${"%.1f".format(Locale.US, l.humedad)} %"
        txtUmbral.text = "Umbral de alarma: ${"%.1f".format(Locale.US, l.umbral)} °C"

        val estadoLed = if (l.led) "ENCENDIDO" else "APAGADO"
        txtLed.text = "LED: $estadoLed (modo ${l.modo})"

        if (l.alarma) {
            txtAlerta.text = "⚠ ALERTA: RIESGO DE INCENDIO"
            txtAlerta.setTextColor(ContextCompat.getColor(this, R.color.alerta))
        } else {
            txtAlerta.text = "✔ Temperatura normal"
            txtAlerta.setTextColor(ContextCompat.getColor(this, R.color.normal))
        }

        val hora = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        txtEstadoSensor.text = "Estado: Datos recibidos ($hora)"

        // Se guarda como máximo una lectura cada 10 s para no llenar la base de datos
        val ahora = SystemClock.elapsedRealtime()
        if (ahora - ultimoGuardado >= 10_000) {
            ultimoGuardado = ahora
            db.guardarLectura(l)
        }
    }

    private fun mostrarError(mensaje: String) {
        txtEstadoSensor.text = "Estado: $mensaje"
        txtAlerta.text = "Sin datos del sensor"
        txtAlerta.setTextColor(ContextCompat.getColor(this, R.color.neutro))
    }
}
