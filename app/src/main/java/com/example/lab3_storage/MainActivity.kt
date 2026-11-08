package com.example.lab3_storage

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var etNote: EditText
    private lateinit var tvResult: TextView
    private lateinit var btnOpenRecycler: Button

    private val PREF_KEY = "saved_note"
    private val FILE_NAME = "my_note.txt"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        etNote = findViewById(R.id.etNote)
        tvResult = findViewById(R.id.tvResult)
        btnOpenRecycler = findViewById(R.id.btnOpenRecycler)

        val sp = getPreferences(Context.MODE_PRIVATE)

        // SharedPreferences: Сохранение заметки
        findViewById<Button>(R.id.btnSaveSP).setOnClickListener {
            val text = etNote.text.toString().trim()
            if (text.isEmpty()) {
                Toast.makeText(this, "Заметка пуста, нечего сохранять", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            sp.edit().putString(PREF_KEY, text).apply()
            Toast.makeText(this, "Сохранено в SharedPreferences", Toast.LENGTH_SHORT).show()
            etNote.text.clear()
        }

        // SharedPreferences: Загрузка заметки
        findViewById<Button>(R.id.btnLoadSP).setOnClickListener {
            val savedText = sp.getString(PREF_KEY, null)
            if (savedText.isNullOrEmpty()) {
                tvResult.text = "Данных в SharedPreferences нет"
                Toast.makeText(this, "Нет сохранённых данных", Toast.LENGTH_SHORT).show()
            } else {
                tvResult.text = "Из SharedPreferences:\n$savedText"
            }
        }

        // SharedPreferences: Очистка заметки
        findViewById<Button>(R.id.btnClearSP).setOnClickListener {
            sp.edit().remove(PREF_KEY).apply()
            tvResult.text = "SharedPreferences очищен"
            Toast.makeText(this, "Очищено", Toast.LENGTH_SHORT).show()
        }

        // Файлы: Запись в файл
        findViewById<Button>(R.id.btnSaveFile).setOnClickListener {
            val text = etNote.text.toString().trim()
            if (text.isEmpty()) {
                Toast.makeText(this, "Заметка пуста, нечего сохранять", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            try {
                val file = File(filesDir, FILE_NAME)
                file.writeText(text)
                Toast.makeText(this, "Сохранено в файл", Toast.LENGTH_SHORT).show()
                etNote.text.clear()
            } catch (e: Exception) {
                Toast.makeText(this, "Ошибка записи: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        // Файлы: Чтение из файла
        findViewById<Button>(R.id.btnLoadFile).setOnClickListener {
            val file = File(filesDir, FILE_NAME)
            if (file.exists()) {
                try {
                    val content = file.readText()
                    tvResult.text = "Из файла:\n$content"
                } catch (e: Exception) {
                    Toast.makeText(this, "Ошибка чтения: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } else {
                tvResult.text = "Файл ещё не создан"
                Toast.makeText(this, "Файл не найден", Toast.LENGTH_SHORT).show()
            }
        }

        // Переход к заданию 2 (RecyclerView)
        btnOpenRecycler.setOnClickListener {
            val intent = Intent(this, RecyclerActivity::class.java)
            startActivity(intent)
        }
    }
}
