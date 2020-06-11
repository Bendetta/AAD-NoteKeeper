package com.benliset.notekeeper

import android.content.Context
import android.content.Intent

object CourseEventBroadcastHelper {
    const val ACTION_COURSE_EVENT = "com.benliset.notekeeper.action.COURSE_EVENT"

    const val EXTRA_COURSE_ID = "com.benliset.notekeeper.extra.COURSE_ID"
    const val EXTRA_COURSE_MESSAGE = "com.benliset.notekeeper.extra.COURSE_MESSAGE"

    fun sendEventBroadcast(context: Context, courseId: String, message: String) {
        val intent = Intent(ACTION_COURSE_EVENT)
        intent.putExtra(EXTRA_COURSE_ID, courseId)
        intent.putExtra(EXTRA_COURSE_MESSAGE, message)

        context.sendBroadcast(intent)
    }
}