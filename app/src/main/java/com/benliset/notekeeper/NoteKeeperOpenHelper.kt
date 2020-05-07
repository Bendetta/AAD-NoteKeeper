package com.benliset.notekeeper

import android.content.Context
import android.database.DatabaseErrorHandler
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.benliset.notekeeper.NoteKeeperDatabaseContract.CourseInfoEntry
import com.benliset.notekeeper.NoteKeeperDatabaseContract.NoteInfoEntry

class NoteKeeperOpenHelper(
    context: Context?
) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        val DATABASE_NAME = "NoteKeeper.db"
        val DATABASE_VERSION = 2
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db?.let {
            it.execSQL(CourseInfoEntry.SQL_CREATE_TABLE)
            it.execSQL(NoteInfoEntry.SQL_CREATE_TABLE)
            it.execSQL(CourseInfoEntry.SQL_CREATE_INDEX1)
            it.execSQL(NoteInfoEntry.SQL_CREATE_INDEX1)

            val worker = DatabaseDataWorker(it)
            worker.insertCourses()
            worker.insertSampleNotes()
        }
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db?.let {
                it.execSQL(CourseInfoEntry.SQL_CREATE_INDEX1)
                it.execSQL(NoteInfoEntry.SQL_CREATE_INDEX1)
            }
        }
    }

}