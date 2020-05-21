package com.benliset.notekeeper

import android.net.Uri
import android.provider.BaseColumns

object NoteKeeperProviderContract {
    val AUTHORITY = "com.benliset.notekeeper.provider"
    val AUTHORITY_URI = Uri.parse("content://${AUTHORITY}")

    private interface CoursesIdColumns {
        val COLUMN_COURSE_ID: String
            get() = "course_id"
    }

    private interface CoursesColumns {
        val COLUMN_COURSE_TITLE: String
            get() = "course_title"
    }

    private interface NotesColumns {
        val COLUMN_NOTE_TITLE: String
            get() = "note_title"
        val COLUMN_NOTE_TEXT: String
            get() = "note_text"
    }

    object Courses: KBaseColumns, CoursesColumns, CoursesIdColumns {
        val PATH = "courses"
        val CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, PATH)
    }

    object Notes: KBaseColumns, NotesColumns, CoursesIdColumns, CoursesColumns {
        val PATH = "notes"
        val CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, PATH)
        val PATH_EXPANDED = "notes_expanded"
        val CONTENT_EXPANDED_URI = Uri.withAppendedPath(AUTHORITY_URI, PATH_EXPANDED)
    }
}

interface KBaseColumns: BaseColumns {
    val _ID: String
        get() = BaseColumns._ID
}