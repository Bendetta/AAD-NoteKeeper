package com.benliset.notekeeper

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class NoteReminderReceiver : BroadcastReceiver() {

    companion object {
        val EXTRA_NOTE_TITLE = "com.benliset.notekeeper.extra.NOTE_TITLE"
        val EXTRA_NOTE_TEXT = "com.benliset.notekeeper.extra.NOTE_TEXT"
        val EXTRA_NOTE_ID = "com.benliset.notekeeper.extra.NOTE_ID"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val noteTitle = intent.getStringExtra(EXTRA_NOTE_TITLE)
        val noteText = intent.getStringExtra(EXTRA_NOTE_TEXT)
        val noteId = intent.getIntExtra(EXTRA_NOTE_ID, 0)

        NoteReminderNotification.notify(context, noteTitle, noteText, noteId)
    }
}
