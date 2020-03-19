package com.benliset.notekeeper

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import androidx.lifecycle.ViewModelProvider

import kotlinx.android.synthetic.main.activity_main.*

class NoteActivity : AppCompatActivity() {

    companion object {
        val NOTE_POSITION = "com.benliset.notekeeper.NOTE_POSITION"
    }

    private val TAG = javaClass.simpleName
    private val POSITION_NOT_SET = -1

    private var note: NoteInfo? = null
    private var isNewNote = true
    private var notePosition = POSITION_NOT_SET
    private var isCancelling = false

    private var viewModel = NoteActivityViewModel()

    private val spinnerCourses: Spinner by lazy {
        val spinnerCourses = findViewById<Spinner>(R.id.spinner_courses)
        spinnerCourses
    }

    private val textNoteTitle: EditText by lazy {
        val textNoteTitle = findViewById<EditText>(R.id.text_note_title)
        textNoteTitle
    }


    private val textNoteText: EditText by lazy {
        val textNoteText = findViewById<EditText>(R.id.text_note_text)
        textNoteText
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val viewModelProvider = ViewModelProvider(viewModelStore, ViewModelProvider.AndroidViewModelFactory.getInstance(application))
        viewModel = viewModelProvider.get(NoteActivityViewModel::class.java)

        if (viewModel.isNewlyCreated && savedInstanceState != null) {
            viewModel.restoreState(savedInstanceState!!)
        }
        viewModel.isNewlyCreated = false

        val courses = DataManager.instance.courses
        val adapterCourses = ArrayAdapter<CourseInfo>(this, android.R.layout.simple_spinner_item, courses)
        adapterCourses.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCourses.adapter = adapterCourses

        readDisplayStateValues()
        saveOriginalNoteValues()

        displayNote(spinnerCourses, textNoteTitle, textNoteText)

        Log.d(TAG, "onCreate")
    }

    private fun saveOriginalNoteValues() {
        if (isNewNote) {
            return
        }

        viewModel.originalCourseNoteId = note?.course?.courseId
        viewModel.originalNoteTitle = note?.title
        viewModel.originalNoteText = note?.text
    }

    private fun createNewNote() {
        notePosition = DataManager.instance.createNewNote()
    }

    override fun onPause() {
        super.onPause()
        if (isCancelling) {
            Log.i(TAG, "Cancelling note at position $notePosition")
            if (isNewNote) {
                DataManager.instance.removeNote(notePosition)
            } else {
                storePreviousNoteValues()
            }
        } else {
            saveNote()
        }

        Log.d(TAG, "onPause")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        viewModel.saveState(outState)
    }

    private fun storePreviousNoteValues() {
        val course = DataManager.instance.getCourse(viewModel.originalCourseNoteId!!)
        note?.course = course
        note?.title = viewModel.originalNoteTitle
        note?.text = viewModel.originalNoteText
    }

    private fun saveNote() {
        note?.course = spinnerCourses.selectedItem as CourseInfo
        note?.title = textNoteTitle.text.toString()
        note?.text = textNoteText.text.toString()
    }

    private fun displayNote(spinnerCourses: Spinner?, textNoteTitle: EditText?, textNoteText: EditText?) {
        val courses = DataManager.instance.courses
        val courseIndex = courses.indexOf(note?.course)
        spinnerCourses?.setSelection(courseIndex)
        textNoteTitle?.setText(note?.title)
        textNoteText?.setText(note?.text)
    }

    private fun readDisplayStateValues() {
        notePosition = intent.getIntExtra(NOTE_POSITION, POSITION_NOT_SET)
        isNewNote = notePosition == POSITION_NOT_SET
        if (isNewNote) {
            createNewNote()
        }

        Log.i(TAG, "notePosition: $notePosition")
        note = DataManager.instance.notes[notePosition]
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
            R.id.action_send_mail -> {
                sendEmail()
                true
            }
            R.id.action_cancel -> {
                isCancelling = true
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun sendEmail() {
        val course = spinnerCourses.selectedItem as CourseInfo
        val subject = textNoteTitle.text.toString()
        val text = "Checkout what I learned in the Pluralsight course ${course.title}\n${textNoteText.text.toString()}"

        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "message/rfc2822"
        intent.putExtra(Intent.EXTRA_SUBJECT, subject)
        intent.putExtra(Intent.EXTRA_TEXT, text)
        startActivity(intent)
    }
}
