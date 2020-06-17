package com.benliset.notekeeper

import android.os.Bundle
import androidx.lifecycle.ViewModel

class NoteActivityViewModel: ViewModel() {
    companion object {
        val ORIGINAL_NOTE_COURSE_ID = "com.benliset.notekeeper.ORIGINAL_NOTE_COURSE_ID"
        val ORIGINAL_NOTE_TITLE = "com.benliset.notekeeper.ORIGINAL_NOTE_TITLE"
        val ORIGINAL_NOTE_TEXT = "com.benliset.notekeeper.ORIGINAL_NOTE_TEXT"
        val NOTE_URI = "com.benliset.notekeeper.NOTE_URI"
    }

    var originalNoteText: String? = null
    var originalNoteTitle: String? = null
    var originalCourseNoteId: String? = null
    var noteUri: String? = null
    var isNewlyCreated = true

    fun saveState(outState: Bundle) {
        outState.putString(ORIGINAL_NOTE_COURSE_ID, originalCourseNoteId)
        outState.putString(ORIGINAL_NOTE_TITLE, originalNoteTitle)
        outState.putString(ORIGINAL_NOTE_TEXT, originalNoteText)
        outState.putString(NOTE_URI, noteUri)
    }

    fun restoreState(inState: Bundle) {
        originalCourseNoteId = inState.getString(ORIGINAL_NOTE_COURSE_ID)
        originalNoteTitle = inState.getString(ORIGINAL_NOTE_TITLE)
        originalNoteText = inState.getString(ORIGINAL_NOTE_TEXT)
        noteUri = inState.getString(NOTE_URI)
    }
}