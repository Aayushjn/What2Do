package com.aayush.what2do.model

import android.os.Build
import android.os.Parcel
import android.os.ParcelUuid
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.aayush.what2do.util.android.*
import java.util.Date

@Entity
data class TodoNote(@PrimaryKey(autoGenerate = false) val id: ParcelUuid,
                    var title: String,
                    var description: String,
                    var priority: Priority,
                    var hasReminder: Boolean,
                    var date: Date?,
                    var color: Int): KParcelable, Comparable<TodoNote> {

    private constructor(parcel: Parcel): this(
        parcel.readParcelable(ParcelUuid::class.java.classLoader)!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readEnum<Priority>()!!,
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            parcel.readBoolean()
        } else {
            parcel.readBool()
        },
        parcel.readDate(),
        parcel.readInt()
    )

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeParcelable(id, flags)
        writeString(title)
        writeString(description)
        writeEnum(priority)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            writeBoolean(hasReminder)
        } else {
            writeBool(hasReminder)
        }
        writeDate(date)
        writeInt(color)
    }

    // Sort by priority, then by date if any, otherwise te title
    override fun compareTo(other: TodoNote): Int = compareValuesBy(
        this,
        other,
        { it.priority },
        { if (it.hasReminder && it.date != null) it.date else it.title }
    )

    companion object CREATOR: Parcelable.Creator<TodoNote> {
        override fun createFromParcel(parcel: Parcel): TodoNote = TodoNote(parcel)
        override fun newArray(size: Int): Array<TodoNote?> = arrayOfNulls(size)
    }
}

fun List<TodoNote>.has(element: TodoNote): Pair<Boolean, Int> {
    forEachIndexed { index, todoNote ->
        if (element.id == todoNote.id) {
            return true to index
        }
    }
    return false to -1
}