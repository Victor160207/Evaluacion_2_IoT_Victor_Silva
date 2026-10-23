package com.example.ejercicio

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class DbHelper(context: Context) :
    SQLiteOpenHelper(context.applicationContext, "incendios.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE usuarios (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "usuario TEXT NOT NULL UNIQUE, " +
                "sal TEXT NOT NULL, " +
                "hash TEXT NOT NULL)"
        )
        db.execSQL(
            "CREATE TABLE lecturas (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "fecha TEXT NOT NULL, " +
                "temperatura REAL NOT NULL, " +
                "humedad REAL NOT NULL, " +
                "led INTEGER NOT NULL, " +
                "alarma INTEGER NOT NULL)"
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
    }


    fun registrarUsuario(usuario: String, password: String): Boolean {
        val sal = Seguridad.nuevaSal()
        val valores = ContentValues().apply {
            put("usuario", usuario.lowercase(Locale.ROOT))
            put("sal", sal)
            put("hash", Seguridad.hash(password, sal))
        }
        return writableDatabase.insert("usuarios", null, valores) != -1L
    }

    /** Verifica usuario y contraseña comparando hashes. */
    fun validarUsuario(usuario: String, password: String): Boolean {
        return readableDatabase.rawQuery(
            "SELECT sal, hash FROM usuarios WHERE usuario = ?",
            arrayOf(usuario.lowercase(Locale.ROOT))
        ).use { c ->
            c.moveToFirst() && Seguridad.verificar(password, c.getString(0), c.getString(1))
        }
    }


    fun guardarLectura(l: Lectura) {
        val fecha = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())
        val valores = ContentValues().apply {
            put("fecha", fecha)
            put("temperatura", l.temperatura.toDouble())
            put("humedad", l.humedad.toDouble())
            put("led", if (l.led) 1 else 0)
            put("alarma", if (l.alarma) 1 else 0)
        }
        val db = writableDatabase
        db.insert("lecturas", null, valores)
        // Se conservan solo las últimas 500 lecturas
        db.execSQL(
            "DELETE FROM lecturas WHERE id NOT IN " +
                "(SELECT id FROM lecturas ORDER BY id DESC LIMIT 500)"
        )
    }


    fun ultimasLecturas(limite: Int): List<String> {
        val lista = mutableListOf<String>()
        readableDatabase.rawQuery(
            "SELECT fecha, temperatura, humedad, led, alarma " +
                "FROM lecturas ORDER BY id DESC LIMIT ?",
            arrayOf(limite.toString())
        ).use { c ->
            while (c.moveToNext()) {
                val alarma = if (c.getInt(4) == 1) "  ⚠ ALARMA" else ""
                val led = if (c.getInt(3) == 1) "ON" else "OFF"
                lista.add(
                    "${c.getString(0)}\n" +
                        "Temp: ${"%.1f".format(Locale.US, c.getDouble(1))} °C   " +
                        "Hum: ${"%.1f".format(Locale.US, c.getDouble(2))} %   " +
                        "LED: $led$alarma"
                )
            }
        }
        return lista
    }

    fun borrarLecturas() {
        writableDatabase.delete("lecturas", null, null)
    }
}
