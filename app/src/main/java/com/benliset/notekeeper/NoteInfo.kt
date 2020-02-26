package com.benliset.notekeeper

class NoteInfo(var course: CourseInfo?, var title: String?, var text: String?) {

    private val compareKey: String = course?.courseId + "|" + title + "|" + text

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val that = o as NoteInfo
        return compareKey == that.compareKey
    }

    override fun hashCode(): Int {
        return compareKey.hashCode()
    }

    override fun toString(): String {
        return compareKey
    }

}