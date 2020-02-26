package com.benliset.notekeeper

import android.os.Parcel
import android.os.Parcelable

class ModuleInfo(val moduleId: String?, val title: String, var isComplete: Boolean = false) : Parcelable {

    private constructor(source: Parcel)
            : this(source.readString(), source.readString()!!, source.readByte().toInt() == 1)

    override fun toString(): String {
        return title
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val that = o as ModuleInfo
        return moduleId == that.moduleId
    }

    override fun hashCode(): Int {
        return moduleId.hashCode()
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(moduleId)
        dest.writeString(title)
        dest.writeByte((if (isComplete) 1 else 0).toByte())
    }

    companion object CREATOR : Parcelable.Creator<ModuleInfo> {
        override fun createFromParcel(parcel: Parcel): ModuleInfo {
            return ModuleInfo(parcel)
        }

        override fun newArray(size: Int): Array<ModuleInfo?> {
            return arrayOfNulls(size)
        }
    }
}