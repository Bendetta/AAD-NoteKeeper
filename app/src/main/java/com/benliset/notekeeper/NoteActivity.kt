package com.benliset.notekeeper

import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import androidx.lifecycle.ViewModelProvider
import com.benliset.notekeeper.NoteKeeperDatabaseContract.NoteInfoEntry

import kotlinx.android.synthetic.main.activity_note.*
import kotlin.properties.Delegates

class NoteActivity : AppCompatActivity() {

    companion object {
        val NOTE_ID = "com.benliset.notekeeper.NOTE_POSITION"
    }

    private val TAG = javaClass.simpleName
    private val ID_NOT_SET = -1

    private var note: NoteInfo? = null
    private var isNewNote = true
    private var noteId = ID_NOT_SET
    private var isCancelling = false

    private var viewModel = NoteActivityViewModel()

    private val dbOpenHelper = NoteKeeperOpenHelper(this)
    private lateinit var noteCursor: Cursor
    private var courseIdPos by Delegates.notNull<Int>()
    private var noteTitlePos by Delegates.notNull<Int>()
    private var noteTextPos by Delegates.notNull<Int>()

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
        setContentView(R.layout.activity_note)
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

        loadNoteData()

        Log.d(TAG, "onCreate")
    }

    override fun onDestroy() {
        super.onDestroy()

        dbOpenHelper.close()
    }

    private fun saveOriginalNoteValues() {
        if (isNewNote) {
            return
        }

        viewModel.originalCourseNoteId = note?.course?.courseId
        viewModel.originalNoteTitle = note?.title
        viewModel.originalNoteText = note?.text
    }

    private fun loadNoteData() {
        val db = dbOpenHelper.readableDatabase

        val selection = "${NoteInfoEntry._ID} = ?"
        val selectionArgs = arrayOf(noteId.toString())

        val noteColumns = arrayOf(NoteInfoEntry.COLUMN_COURSE_ID, NoteInfoEntry.COLUMN_NOTE_TITLE, NoteInfoEntry.COLUMN_NOTE_TEXT)
        noteCursor = db.query(NoteInfoEntry.TABLE_NAME, noteColumns, selection, selectionArgs, null, null, null)
        courseIdPos = noteCursor.getColumnIndex(NoteInfoEntry.COLUMN_COURSE_ID)
        noteTitlePos = noteCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TITLE)
        noteTextPos = noteCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TEXT)
        noteCursor.moveToNext()
        displayNote()
    }

    private fun createNewNote() {
        noteId = DataManager.instance.createNewNote()
    }

    override fun onPause() {
        super.onPause()
        if (isCancelling) {
            Log.i(TAG, "Cancelling note at position $noteId")
            if (isNewNote) {
                DataManager.instance.removeNote(noteId)
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

    private fun displayNote() {
        val courseId = noteCursor.getString(courseIdPos)
        val noteTitle = noteCursor.getString(noteTitlePos)
        val noteText = noteCursor.getString(noteTextPos)
        val courses = DataManager.instance.courses
        val course = DataManager.instance.getCourse(courseId)
        val courseIndex = courses.indexOf(course)
        spinnerCourses?.setSelection(courseIndex)
        textNoteTitle?.setText(noteTitle)
        textNoteText?.setText(noteText)
    }

    private fun readDisplayStateValues() {
        noteId = intent.getIntExtra(NOTE_ID, ID_NOT_SET)
        isNewNote = noteId == ID_NOT_SET
        if (isNewNote) {
            createNewNote()
        }

        Log.i(TAG, "noteId: $noteId")
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
        when (item.itemId) {
            R.id.action_send_mail -> {
                sendEmail()
            }
            R.id.action_cancel -> {
                isCancelling = true
                finish()
            }
            R.id.action_next -> {
                moveNext()
            }
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        val item = menu?.findItem(R.id.action_next)
        val lastNoteIndex = DataManager.instance.notes.size - 1
        item?.isEnabled = noteId < lastNoteIndex
        return super.onPrepareOptionsMenu(menu)
    }

    private fun moveNext() {
        saveNote()

        noteId++
        note = DataManager.instance.notes[noteId]

        saveOriginalNoteValues()
        displayNote()
        invalidateOptionsMenu()
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
