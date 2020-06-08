package com.benliset.notekeeper

import android.app.IntentService
import android.content.Intent

class NoteBackupService : IntentService("NoteBackupService") {
    companion object {
        const val EXTRA_COURSE_ID = "com.benliset.notekeeper.extra.COURSE_ID"
    }

    override fun onHandleIntent(intent: Intent?) {
        intent?.let {
            val backupCourseId = it.getStringExtra(EXTRA_COURSE_ID)
            NoteBackup.doBackup(this, backupCourseId)
        }
    }
}
