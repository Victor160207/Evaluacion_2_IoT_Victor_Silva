package com.example.ejercicio

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView


class HistorialActivity : PantallaProtegida() {

    private lateinit var lstHistorial: ListView
    private lateinit var txtResumen: TextView
    private lateinit var db: DbHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!exigirSesion()) return

        setContentView(R.layout.activity_historial)
        aplicarInsets(findViewById(R.id.raiz))

        db = DbHelper(this)
        lstHistorial = findViewById(R.id.lstHistorial)
        txtResumen = findViewById(R.id.txtResumenHistorial)

        findViewById<Button>(R.id.btnBorrarHistorial).setOnClickListener {
            db.borrarLecturas()
            cargar()
        }

        findViewById<Button>(R.id.btnVolverHistorial).setOnClickListener {
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        if (Sesion.usuario == null) return
        cargar()
    }

    private fun cargar() {
        val lecturas = db.ultimasLecturas(50)
        lstHistorial.adapter =
            ArrayAdapter(this, android.R.layout.simple_list_item_1, lecturas)

        txtResumen.text =
            if (lecturas.isEmpty()) "Sin lecturas guardadas. Abra Monitoreo para registrar datos."
            else "${lecturas.size} lecturas (más recientes primero)"
    }
}
