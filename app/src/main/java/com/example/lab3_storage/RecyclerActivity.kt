package com.example.lab3_storage

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class RecyclerActivity : AppCompatActivity() {

    private lateinit var etNewNote: EditText
    private lateinit var btnAdd: Button
    private lateinit var btnBack: Button
    private lateinit var rvNotes: RecyclerView
    private lateinit var adapter: NotesAdapter

    private val notesList = mutableListOf(
        "Первая тестовая заметка",
        "Сдать лабораторную работу №3",
        "Повторить жизненный цикл Activity"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recycler)

        etNewNote = findViewById(R.id.etNewNote)
        btnAdd = findViewById(R.id.btnAdd)
        btnBack = findViewById(R.id.btnBack)
        rvNotes = findViewById(R.id.rvNotes)

        adapter = NotesAdapter(notesList) { position ->
            adapter.removeNote(position)
        }

        rvNotes.layoutManager = LinearLayoutManager(this)
        rvNotes.adapter = adapter

        // Добавление новой заметки в список
        btnAdd.setOnClickListener {
            val text = etNewNote.text.toString().trim()
            if (text.isEmpty()) {
                Toast.makeText(this, "Введите текст заметки", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            notesList.add(text)
            val lastPosition = notesList.size - 1
            adapter.notifyItemInserted(lastPosition)
            etNewNote.text.clear()
        }

        btnBack.setOnClickListener {
            finish()
        }
    }
}
