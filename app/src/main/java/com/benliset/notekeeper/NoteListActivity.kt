package com.benliset.notekeeper

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import kotlinx.android.synthetic.main.activity_note_list.*

class NoteListActivity : AppCompatActivity() {

    lateinit var noteRecyclerAdapter: NoteRecyclerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note_list)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            startActivity(Intent(this, NoteActivity::class.java))
        }

        initializeDisplayContent()
    }

    override fun onResume() {
        super.onResume()
        noteRecyclerAdapter.notifyDataSetChanged()
    }

    private fun initializeDisplayContent() {
        val recyclerNotes = findViewById<RecyclerView>(R.id.list_notes)
        val notesLayoutManager = LinearLayoutManager(this)
        recyclerNotes.layoutManager = notesLayoutManager

        val notes = DataManager.instance.notes
        noteRecyclerAdapter = NoteRecyclerAdapter(this, notes)
        recyclerNotes.adapter = noteRecyclerAdapter
    }

}
