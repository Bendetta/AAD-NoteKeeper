package com.benliset.notekeeper

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import android.provider.BaseColumns
import com.benliset.notekeeper.NoteKeeperDatabaseContract.CourseInfoEntry
import com.benliset.notekeeper.NoteKeeperDatabaseContract.NoteInfoEntry
import com.benliset.notekeeper.NoteKeeperProviderContract.Courses
import com.benliset.notekeeper.NoteKeeperProviderContract.Notes

class NoteKeeperProvider : ContentProvider() {

    companion object {
        val COURSES = 0
        val NOTES = 1
        val NOTES_EXPANDED = 2
    }

    private lateinit var dbOpenHelper: NoteKeeperOpenHelper

    private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH)

    init {
        uriMatcher.addURI(NoteKeeperProviderContract.AUTHORITY, Courses.PATH, COURSES)
        uriMatcher.addURI(NoteKeeperProviderContract.AUTHORITY, Notes.PATH, NOTES)
        uriMatcher.addURI(NoteKeeperProviderContract.AUTHORITY, Notes.PATH_EXPANDED, NOTES_EXPANDED)
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        TODO("Implement this to handle requests to delete one or more rows")
    }

    override fun getType(uri: Uri): String? {
        TODO(
            "Implement this to handle requests for the MIME type of the data" +
                    "at the given URI"
        )
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        TODO("Implement this to handle requests to insert a new row.")
    }

    override fun onCreate(): Boolean {
        dbOpenHelper = NoteKeeperOpenHelper(context)
        return true
    }

    override fun query(
        uri: Uri, projection: Array<String>?, selection: String?,
        selectionArgs: Array<String>?, sortOrder: String?
    ): Cursor? {
        var cursor: Cursor? = null
        val db = dbOpenHelper.readableDatabase

        val uriMatch = uriMatcher.match(uri)
        when (uriMatch) {
            COURSES -> {
                cursor = db.query(CourseInfoEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder)
            }
            NOTES -> {
                cursor = db.query(NoteInfoEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder)
            }
            NOTES_EXPANDED -> {
                cursor = notesExpandedQuery(db, projection, selection, selectionArgs, sortOrder)
            }
        }
        return cursor
    }

    private fun notesExpandedQuery(
        db: SQLiteDatabase,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?
    ): Cursor? {
        val columns = projection?.map {
            if (it == BaseColumns._ID || it == Courses.COLUMN_COURSE_ID) NoteInfoEntry.getQName(it) else it
        }?.toTypedArray()

        val tablesWithJoin = "${NoteInfoEntry.TABLE_NAME} JOIN ${CourseInfoEntry.TABLE_NAME} ON " +
            "${NoteInfoEntry.getQName(NoteInfoEntry.COLUMN_COURSE_ID)} = ${CourseInfoEntry.getQName(CourseInfoEntry.COLUMN_COURSE_ID)}"

        return db.query(tablesWithJoin, columns, selection, selectionArgs, null, null, sortOrder)
    }

    override fun update(
        uri: Uri, values: ContentValues?, selection: String?,
        selectionArgs: Array<String>?
    ): Int {
        TODO("Implement this to handle requests to update one or more rows.")
    }
}
