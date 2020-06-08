package com.benliset.notekeeper

import android.app.Service
import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.os.IBinder

class NoteUploaderJobService : JobService() {
    companion object {
        val EXTRA_DATA_URI = "com.benliset.notekeeper.extras.DATA_URI"
    }

    private var noteUploader: NoteUploader? = null

    override fun onStartJob(params: JobParameters?): Boolean {
        noteUploader = NoteUploader(this)
        StartJobAsyncTask().execute(params)

        return true
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        noteUploader?.cancel()
        return true
    }

    private inner class StartJobAsyncTask: AsyncTask<JobParameters, Void, Void>() {
        override fun doInBackground(vararg params: JobParameters?): Void? {
            val jobParams = params[0]!!

            val stringDataUri = jobParams.extras.getString(EXTRA_DATA_URI)
            val dataUri = Uri.parse(stringDataUri)
            noteUploader?.doUpload(dataUri)

            if (noteUploader?.isCanceled() != true) {
                jobFinished(jobParams, false)
            }

            return null
        }
    }
}
