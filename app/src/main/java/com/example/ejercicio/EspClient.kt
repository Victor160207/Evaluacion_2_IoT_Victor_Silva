package com.example.ejercicio

import android.os.Handler
import android.os.Looper
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

data class Lectura(
    val temperatura: Float,
    val humedad: Float,
    val led: Boolean,
    val alarma: Boolean,
    val umbral: Float,
    val modo: String
) {
    companion object {
        /** Devuelve null si la respuesta no tiene el formato esperado. */
        fun desde(respuesta: String): Lectura? {
            val d = respuesta.trim().split(";")
            if (d.size < 6) return null
            val temp = d[0].toFloatOrNull() ?: return null
            val hum = d[1].toFloatOrNull() ?: return null
            val umbral = d[4].toFloatOrNull() ?: return null
            return Lectura(temp, hum, d[2] == "1", d[3] == "1", umbral, d[5])
        }
    }
}


object EspClient {

    const val IP_ESP32 = "192.168.4.1"
    const val SSID = "ESP32_ControlIoT"

    private const val API_KEY = "incendio2026"

    private val principal = Handler(Looper.getMainLooper())

    fun get(
        ruta: String,
        onOk: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        Thread {
            var conexion: HttpURLConnection? = null
            try {
                val separador = if (ruta.contains("?")) "&" else "?"
                val url = URL("http://$IP_ESP32$ruta${separador}key=$API_KEY")

                conexion = url.openConnection() as HttpURLConnection
                conexion.requestMethod = "GET"
                conexion.connectTimeout = 3000
                conexion.readTimeout = 3000

                val codigo = conexion.responseCode
                if (codigo == 200) {
                    val cuerpo = conexion.inputStream.bufferedReader().use { it.readText() }
                    principal.post { onOk(cuerpo) }
                } else {
                    val mensaje = when (codigo) {
                        401 -> "Clave de acceso rechazada por el ESP32"
                        500 -> "El ESP32 no logra leer el DHT22"
                        400 -> "Valor rechazado por el ESP32"
                        else -> "Error HTTP: $codigo"
                    }
                    principal.post { onError(mensaje) }
                }
            } catch (e: Exception) {
                principal.post { onError("Sin conexión con el ESP32") }
            } finally {
                conexion?.disconnect()
            }
        }.start()
    }

    fun formatoRuta(ruta: String, valor: Float): String =
        String.format(Locale.US, "%s?valor=%.1f", ruta, valor)
}
