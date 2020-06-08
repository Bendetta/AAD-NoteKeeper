package com.benliset.notekeeper

import android.content.Context
import android.net.Uri
import android.util.Log
import com.benliset.notekeeper.NoteKeeperProviderContract.Notes


class NoteUploader(private val mContext: Context) {
    private val TAG = javaClass.simpleName

    private var mCanceled = false

    fun isCanceled(): Boolean {
        return mCanceled
    }

    fun cancel() {
        mCanceled = true
    }

    fun doUpload(dataUri: Uri) {
        val columns = arrayOf(
            Notes.COLUMN_COURSE_ID,
            Notes.COLUMN_NOTE_TITLE,
            Notes.COLUMN_NOTE_TEXT
        )
        val cursor =
            mContext!!.contentResolver.query(dataUri, columns, null, null, null)
        val courseIdPos = cursor!!.getColumnIndex(Notes.COLUMN_COURSE_ID)
        val noteTitlePos = cursor.getColumnIndex(Notes.COLUMN_NOTE_TITLE)
        val noteTextPos = cursor.getColumnIndex(Notes.COLUMN_NOTE_TEXT)
        Log.i(TAG, ">>>*** UPLOAD START - $dataUri ***<<<")
        mCanceled = false
        while (!mCanceled && cursor.moveToNext()) {
            val courseId = cursor.getString(courseIdPos)
            val noteTitle = cursor.getString(noteTitlePos)
            val noteText = cursor.getString(noteTextPos)
            if (noteTitle != "") {
                Log.i(
                    TAG,
                    ">>>Uploading Note<<< $courseId|$noteTitle|$noteText"
                )
                simulateLongRunningWork()
            }
        }
        if (mCanceled) Log.i(
            TAG,
            ">>>*** UPLOAD !!CANCELED!! - $dataUri ***<<<"
        ) else Log.i(TAG, ">>>*** UPLOAD COMPLETE - $dataUri ***<<<")
        cursor.close()
    }

    private fun simulateLongRunningWork() {
        try {
            Thread.sleep(2000)
        } catch (ex: Exception) {
        }
    }
}