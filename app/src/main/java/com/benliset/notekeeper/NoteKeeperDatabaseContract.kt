package com.benliset.notekeeper

import android.provider.BaseColumns
import android.provider.BaseColumns._ID

object NoteKeeperDatabaseContract {

    object CourseInfoEntry: BaseColumns {
        val TABLE_NAME = "course_info"
        val COLUMN_COURSE_ID = "course_id"
        val COLUMN_COURSE_TITLE = "course_title"
        val _ID = BaseColumns._ID

        val SQL_CREATE_TABLE = "CREATE TABLE $TABLE_NAME (${_ID} INTEGER PRIMARY KEY, $COLUMN_COURSE_ID TEXT UNIQUE NOT NULL, $COLUMN_COURSE_TITLE TEXT NOT NULL)"
    }

    object NoteInfoEntry: BaseColumns {
        val TABLE_NAME = "note_info"
        val COLUMN_NOTE_TITLE = "note_title"
        val COLUMN_NOTE_TEXT = "note_text"
        val COLUMN_COURSE_ID = "course_id"
        val _ID = BaseColumns._ID

        val SQL_CREATE_TABLE = "CREATE TABLE $TABLE_NAME (${_ID} INTEGER PRIMARY KEY, $COLUMN_NOTE_TITLE TEXT NOT NULL, $COLUMN_NOTE_TEXT TEXT, $COLUMN_COURSE_ID TEXT NOT NULL)"
    }
}