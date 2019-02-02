package com.aayush.what2do.model

import android.os.Parcel
import android.os.ParcelUuid
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.aayush.what2do.util.*
import java.util.*

@Entity
data class TodoNote(@PrimaryKey(autoGenerate = false) val id: ParcelUuid,
                    var title: String,
                    var description : String,
                    var priority: Priority,
                    var hasReminder: Boolean,
                    var date: Date?,
                    var color: Int): Parcelable, Comparable<TodoNote> {

    constructor(parcel: Parcel) : this(
        parcel.readParcelable(ParcelUuid::class.java.classLoader)!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readEnum<Priority>()!!,
        parcel.readBoolean(),
        parcel.readDate(),
        parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) = with(parcel) {
        writeParcelable(id, flags)
        writeString(title)
        writeString(description)
        writeEnum(priority)
        writeBoolean(hasReminder)
        writeDate(date)
        writeInt(color)
    }

    override fun describeContents() = 0

    override fun compareTo(other: TodoNote) = compareValuesBy(
        this,
        other,
        { it.priority },
        { if (it.hasReminder && it.date != null) it.date else it.title }
    )

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<TodoNote> {
            override fun createFromParcel(parcel: Parcel) = TodoNote(parcel)

            override fun newArray(size: Int) = arrayOfNulls<TodoNote>(size)
        }
    }
}