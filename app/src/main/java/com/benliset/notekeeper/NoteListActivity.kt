package com.benliset.notekeeper

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity

import kotlinx.android.synthetic.main.activity_note_list.*

class NoteListActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note_list)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            startActivity(Intent(this, NoteActivity::class.java))
        }

        initializeDisplayContent()
    }

    private fun initializeDisplayContent() {
        val listNotes = findViewById<ListView>(R.id.list_notes)

        val notes = DataManager.instance.notes
        val adapterNotes = ArrayAdapter<NoteInfo>(this, android.R.layout.simple_list_item_1, notes)

        listNotes.adapter = adapterNotes
        
        listNotes.setOnItemClickListener { parent, view, position, id ->
            val intent = Intent(this, NoteActivity::class.java)
            intent.putExtra(NoteActivity.NOTE_POSITION, position)
            startActivity(intent)
        }
    }

}
