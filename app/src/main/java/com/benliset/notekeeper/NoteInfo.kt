package com.benliset.notekeeper

import android.os.Parcel
import android.os.Parcelable

class NoteInfo(var course: CourseInfo?, var title: String?, var text: String?) : Parcelable {

    private val compareKey: String
        get() = course?.courseId + "|" + title + "|" + text


    private constructor(parcel: Parcel) : this(
        parcel.readParcelable(CourseInfo::class.java.classLoader),
        parcel.readString(),
        parcel.readString()
    )

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

    override fun describeContents(): Int {
       return 0
    }

    override fun writeToParcel(dest: Parcel?, flags: Int) {
        dest?.writeParcelable(course, 0)
        dest?.writeString(title)
        dest?.writeString(text)
    }

    companion object CREATOR : Parcelable.Creator<NoteInfo> {
        override fun createFromParcel(parcel: Parcel): NoteInfo {
            return NoteInfo(parcel)
        }

        override fun newArray(size: Int): Array<NoteInfo?> {
            return arrayOfNulls(size)
        }
    }
}