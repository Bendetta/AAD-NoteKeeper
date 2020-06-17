package com.benliset.notekeeper

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.SimpleCursorAdapter
import android.widget.Spinner
import androidx.lifecycle.ViewModelProvider
import androidx.loader.app.LoaderManager
import androidx.loader.content.Loader
import androidx.loader.content.CursorLoader
import com.benliset.notekeeper.NoteKeeperDatabaseContract.CourseInfoEntry
import com.benliset.notekeeper.NoteKeeperDatabaseContract.NoteInfoEntry
import com.benliset.notekeeper.NoteKeeperProviderContract.Courses
import com.benliset.notekeeper.NoteKeeperProviderContract.Notes
import com.google.android.material.snackbar.Snackbar

import kotlinx.android.synthetic.main.activity_note.*
import kotlinx.coroutines.coroutineScope
import kotlin.properties.Delegates

class NoteActivity : AppCompatActivity(), LoaderManager.LoaderCallbacks<Cursor> {

    companion object {
        val NOTE_ID = "com.benliset.notekeeper.NOTE_POSITION"
        val LOADER_NOTES = 0
        val LOADER_COURSES = 1
    }

    private var notesQueryFinished: Boolean = false
    private var coursesQueryFinished: Boolean = false
    private val TAG = javaClass.simpleName
    private val ID_NOT_SET = -1

    private var note: NoteInfo? = null
    private var isNewNote = true
    private var noteId = ID_NOT_SET
    private var isCancelling = false
    private var noteUri: Uri? = null

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

    private lateinit var adapterCourses: SimpleCursorAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note)
        setSupportActionBar(toolbar)

        val viewModelProvider = ViewModelProvider(viewModelStore, ViewModelProvider.AndroidViewModelFactory.getInstance(application))
        viewModel = viewModelProvider.get(NoteActivityViewModel::class.java)

        if (viewModel.isNewlyCreated && savedInstanceState != null) {
            viewModel.restoreState(savedInstanceState!!)
            noteUri = Uri.parse(viewModel.noteUri)
        }
        viewModel.isNewlyCreated = false

        adapterCourses =
            SimpleCursorAdapter(
                this, android.R.layout.simple_spinner_item, null,
                arrayOf(CourseInfoEntry.COLUMN_COURSE_TITLE),
                intArrayOf(android.R.id.text1), 0
            )
        adapterCourses.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCourses.adapter = adapterCourses

        supportLoaderManager.initLoader(LOADER_COURSES, null, this)

        readDisplayStateValues()
        saveOriginalNoteValues()

        if (!isNewNote) {
            supportLoaderManager.initLoader(LOADER_NOTES, null, this)
        }
        //loadNoteData()

        Log.d(TAG, "onCreate")
    }

    private fun loadCourseData() {
        val db = dbOpenHelper.readableDatabase
        val courseColumns = arrayOf(CourseInfoEntry.COLUMN_COURSE_TITLE,
            CourseInfoEntry.COLUMN_COURSE_ID,
            CourseInfoEntry._ID)

        val cursor = db.query(CourseInfoEntry.TABLE_NAME, courseColumns,
            null, null, null, null, CourseInfoEntry.COLUMN_COURSE_TITLE)
        adapterCourses.changeCursor(cursor)
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
        val values = ContentValues()
        values.put(Notes.COLUMN_COURSE_ID, "")
        values.put(Notes.COLUMN_NOTE_TITLE, "")
        values.put(Notes.COLUMN_NOTE_TEXT, "")

        Log.d(TAG, "Call to execute - thread: ${Thread.currentThread().id}")
        CreateNewNoteAsyncTask().execute(values)
    }

    private inner class CreateNewNoteAsyncTask: AsyncTask<ContentValues, Int, Uri?>() {
        private lateinit var progressBar: ProgressBar

        override fun onPreExecute() {
            progressBar = findViewById(R.id.progress_bar)
            progressBar.visibility = View.VISIBLE
            progressBar.progress = 1
        }
        override fun doInBackground(vararg params: ContentValues?): Uri? {
            Log.d(TAG, "doInBackground - thread: ${Thread.currentThread().id}")
            val insertValues = params[0]
            val rowUri = contentResolver.insert(Notes.CONTENT_URI, insertValues)
            simulateLongRunningWork()
            publishProgress(2)

            simulateLongRunningWork()
            publishProgress(3)
            return rowUri
        }

        override fun onProgressUpdate(vararg values: Int?) {
            val progressValue = values[0]!!
            progressBar.progress = progressValue
        }

        override fun onPostExecute(result: Uri?) {
            Log.d(TAG, "onPostExecute - thread: ${Thread.currentThread().id}")
            noteUri = result
            displaySnackbar(noteUri.toString())
            progressBar.visibility = View.GONE
        }
    }

    override fun onPause() {
        super.onPause()
        if (isCancelling) {
            Log.i(TAG, "Cancelling note at position $noteId")
            if (isNewNote) {
                deleteNoteFromDatabase()
            } else {
                storePreviousNoteValues()
            }
        } else {
            saveNote()
        }

        Log.d(TAG, "onPause")
    }

    private fun deleteNoteFromDatabase() {
        val selection = "${NoteInfoEntry._ID} = ?"
        val selectionArgs = arrayOf(noteId.toString())
        // TODO: Should move this to AsyncTask
        val db = dbOpenHelper.writableDatabase
        db.delete(NoteInfoEntry.TABLE_NAME, selection, selectionArgs)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        viewModel.saveState(outState)
    }

    private fun storePreviousNoteValues() {
        viewModel.originalCourseNoteId?.let {
            val course = DataManager.instance.getCourse(it)
            note?.course = course
        }
        note?.title = viewModel.originalNoteTitle
        note?.text = viewModel.originalNoteText
    }

    private fun saveNote() {
        val courseId = selectedCourseId()
        val noteTitle = textNoteTitle.text.toString()
        val noteText = textNoteText.text.toString()
        saveNoteToDatabase(courseId, noteTitle, noteText)
    }

    private fun selectedCourseId(): String {
        val selectedPosition = spinnerCourses.selectedItemPosition
        val cursor = adapterCourses.cursor
        cursor.moveToPosition(selectedPosition)
        val courseIdPos = cursor.getColumnIndex(CourseInfoEntry.COLUMN_COURSE_ID)
        val courseId = cursor.getString(courseIdPos)
        return courseId
    }

    private fun saveNoteToDatabase(courseId: String, noteTitle: String, noteText: String) {
        val selection = "${NoteInfoEntry._ID} = ?"
        val selectionArgs = arrayOf(noteId.toString())

        val values = ContentValues()
        values.put(NoteInfoEntry.COLUMN_COURSE_ID, courseId)
        values.put(NoteInfoEntry.COLUMN_NOTE_TITLE, noteTitle)
        values.put(NoteInfoEntry.COLUMN_NOTE_TEXT, noteText)

        val db = dbOpenHelper.writableDatabase
        db.update(NoteInfoEntry.TABLE_NAME, values, selection, selectionArgs)
    }

    private fun displayNote() {
        val courseId = noteCursor.getString(courseIdPos)
        val noteTitle = noteCursor.getString(noteTitlePos)
        val noteText = noteCursor.getString(noteTextPos)

        val courseIndex = getIndexOfCourseId(courseId)
        spinnerCourses?.setSelection(courseIndex)
        textNoteTitle?.setText(noteTitle)
        textNoteText?.setText(noteText)

        CourseEventBroadcastHelper.sendEventBroadcast(this, courseId, "Editing Note")
    }

    private fun getIndexOfCourseId(courseId: String?): Int {
        val cursor = adapterCourses.cursor
        val courseIdPos = cursor.getColumnIndex(CourseInfoEntry.COLUMN_COURSE_ID)
        var courseRowIndex = 0

        var more = cursor.moveToFirst()
        while (more) {
            val cursorCourseId = cursor.getString(courseIdPos)
            if (courseId.equals(cursorCourseId)) {
                break
            }

            courseRowIndex++
            more = cursor.moveToNext()
        }
        return courseRowIndex
    }

    private fun readDisplayStateValues() {
        noteId = intent.getIntExtra(NOTE_ID, ID_NOT_SET)
        isNewNote = noteId == ID_NOT_SET
        if (isNewNote) {
            createNewNote()
        }

        Log.i(TAG, "noteId: $noteId")
    }

    private fun displaySnackbar(message: String) {
        val view = findViewById<View>(R.id.spinner_courses)
        Snackbar.make(view, message, Snackbar.LENGTH_LONG).show()
    }

    private fun simulateLongRunningWork() {
        try {
            Thread.sleep(2000);
        } catch(ex: Exception) {}
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
            R.id.action_set_reminder -> {
                showReminderNotification()
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun showReminderNotification() {
        val noteTitle = textNoteTitle.text.toString()
        val noteText = textNoteText.text.toString()
        val noteId = noteId

        val intent = Intent(this, NoteReminderReceiver::class.java)
        intent.putExtra(NoteReminderReceiver.EXTRA_NOTE_TITLE, noteTitle)
        intent.putExtra(NoteReminderReceiver.EXTRA_NOTE_TEXT, noteText)
        intent.putExtra(NoteReminderReceiver.EXTRA_NOTE_ID, noteId)

        val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val currentTimeInMilliseconds = SystemClock.elapsedRealtime()
        val ONE_HOUR = 60 * 60 * 1000
        val TEN_SECONDS = 10 * 1000

        val alarmTime = currentTimeInMilliseconds + TEN_SECONDS
        alarmManager.set(AlarmManager.ELAPSED_REALTIME, alarmTime, pendingIntent)
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

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        if (id == LOADER_NOTES) {
            return createLoaderNotes()
        } else { // if (id == LOADER_COURSES)
            return createLoaderCourses()
        }
    }

    private fun createLoaderCourses(): Loader<Cursor> {
        coursesQueryFinished = false
        val uri = Courses.CONTENT_URI
        val courseColumns = arrayOf(Courses.COLUMN_COURSE_TITLE,
            Courses.COLUMN_COURSE_ID,
            Courses._ID)
        return CursorLoader(this, uri, courseColumns, null, null, Courses.COLUMN_COURSE_TITLE)
    }

    private fun createLoaderNotes(): Loader<Cursor> {
        notesQueryFinished = false
        val noteColumns = arrayOf(Notes.COLUMN_COURSE_ID, Notes.COLUMN_NOTE_TITLE, Notes.COLUMN_NOTE_TEXT)
        noteUri = ContentUris.withAppendedId(Notes.CONTENT_URI, noteId.toLong()).let {
            return CursorLoader(this, it, noteColumns, null, null, null)
        }
    }

    override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor?) {
        if (loader.id == LOADER_NOTES) {
            loadFinishedNotes(data)
        } else if (loader.id == LOADER_COURSES) {
            adapterCourses.changeCursor(data)
            coursesQueryFinished = true
            displayNoteWhenQueriesFinished()
        }
    }

    private fun loadFinishedNotes(data: Cursor?) {
        data?.let {
            noteCursor = it
            courseIdPos = noteCursor.getColumnIndex(NoteInfoEntry.COLUMN_COURSE_ID)
            noteTitlePos = noteCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TITLE)
            noteTextPos = noteCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TEXT)
            noteCursor.moveToNext()
            notesQueryFinished = true
            displayNoteWhenQueriesFinished()
        }
    }

    private fun displayNoteWhenQueriesFinished() {
        if (notesQueryFinished && coursesQueryFinished) {
            displayNote()
        }
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
        if (loader.id == LOADER_NOTES) {
            noteCursor.close()
        } else if (loader.id == LOADER_COURSES) {
            adapterCourses.changeCursor(null)
        }
    }
}
