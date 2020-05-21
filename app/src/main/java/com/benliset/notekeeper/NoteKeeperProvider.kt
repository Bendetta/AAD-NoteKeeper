package com.benliset.notekeeper

import android.content.*
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
        val NOTES_ROW = 3
        val MIME_VENDOR_TYPE = "vnd.${NoteKeeperProviderContract.AUTHORITY}."
    }

    private lateinit var dbOpenHelper: NoteKeeperOpenHelper

    private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH)

    init {
        uriMatcher.addURI(NoteKeeperProviderContract.AUTHORITY, Courses.PATH, COURSES)
        uriMatcher.addURI(NoteKeeperProviderContract.AUTHORITY, Notes.PATH, NOTES)
        uriMatcher.addURI(NoteKeeperProviderContract.AUTHORITY, Notes.PATH_EXPANDED, NOTES_EXPANDED)
        uriMatcher.addURI(NoteKeeperProviderContract.AUTHORITY, "${Notes.PATH}/#", NOTES_ROW)
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        TODO("Implement this to handle requests to delete one or more rows")
    }

    override fun getType(uri: Uri): String? {
        var mimeType: String? = null
        val uriMatch = uriMatcher.match(uri)
        when(uriMatch) {
            COURSES -> {
                mimeType = "${ContentResolver.CURSOR_DIR_BASE_TYPE}/${MIME_VENDOR_TYPE}${Courses.PATH}"
            }
            NOTES -> {
                mimeType = "${ContentResolver.CURSOR_DIR_BASE_TYPE}/${MIME_VENDOR_TYPE}${Notes.PATH}"
            }
            NOTES_EXPANDED -> {
                mimeType = "${ContentResolver.CURSOR_DIR_BASE_TYPE}/${MIME_VENDOR_TYPE}${Notes.PATH_EXPANDED}"
            }
            NOTES_ROW -> {
                mimeType = "${ContentResolver.CURSOR_ITEM_BASE_TYPE}/${MIME_VENDOR_TYPE}${Notes.PATH}"
            }
        }
        return mimeType
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        val db = dbOpenHelper.writableDatabase
        var rowId: Long = -1
        var rowUri: Uri? = null
        val uriMatch = uriMatcher.match(uri)
        when (uriMatch) {
            NOTES -> {
                rowId = db.insert(NoteInfoEntry.TABLE_NAME, null, values)
                rowUri = ContentUris.withAppendedId(Notes.CONTENT_URI, rowId)
            }
            COURSES -> {
                rowId = db.insert(CourseInfoEntry.TABLE_NAME, null, values)
                rowUri = ContentUris.withAppendedId(Courses.CONTENT_URI, rowId)
            }
            NOTES_EXPANDED -> {

            }
        }

        return rowUri
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
            NOTES_ROW -> {
                val rowId = ContentUris.parseId(uri)
                val rowSelection = "${NoteInfoEntry._ID} = ?"
                val rowSelectionArgs = arrayOf(rowId.toString())
                cursor = db.query(NoteInfoEntry.TABLE_NAME, projection, rowSelection, rowSelectionArgs, null, null, null)
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
