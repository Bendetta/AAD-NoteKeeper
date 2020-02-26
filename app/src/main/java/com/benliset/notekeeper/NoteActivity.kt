package com.benliset.notekeeper

import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner

import kotlinx.android.synthetic.main.activity_main.*

class NoteActivity : AppCompatActivity() {

    companion object {
        val NOTE_INFO = "com.benliset.notekeeper.NOTE_INFO"
    }

    private var note: NoteInfo? = null
    private var isNewNote = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val spinnerCourses = findViewById<Spinner>(R.id.spinner_courses)

        val courses = DataManager.instance.courses
        val adapterCourses = ArrayAdapter<CourseInfo>(this, android.R.layout.simple_spinner_item, courses)
        adapterCourses.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCourses.adapter = adapterCourses

        readDisplayStateValues()

        val textNoteTitle = findViewById<EditText>(R.id.text_note_title)
        val textNoteText = findViewById<EditText>(R.id.text_note_text)

        if (!isNewNote) {
            displayNote(spinnerCourses, textNoteTitle, textNoteText)
        }
    }

    private fun displayNote(spinnerCourses: Spinner?, textNoteTitle: EditText?, textNoteText: EditText?) {
        val courses = DataManager.instance.courses
        val courseIndex = courses.indexOf(note?.course)
        spinnerCourses?.setSelection(courseIndex)
        textNoteTitle?.setText(note?.title)
        textNoteText?.setText(note?.text)
    }

    private fun readDisplayStateValues() {
        note = intent.getParcelableExtra(NOTE_INFO)
        isNewNote = note == null
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_note, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}
