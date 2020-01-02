package com.aayush.what2do.util.android

import android.os.Parcel
import android.os.Parcelable
import java.util.*

interface KParcelable: Parcelable {
    override fun describeContents(): Int = 0
    override fun writeToParcel(dest: Parcel, flags: Int)
}

fun Parcel.readBool(): Boolean = readInt() != 0

fun Parcel.writeBool(value: Boolean) = writeInt(if (value) 1 else 0)

inline fun <T> Parcel.readNullable(reader: () -> T): T? = if (readInt() != 0) reader() else null

inline fun <T> Parcel.writeNullable(value: T?, writer: (T) -> Unit) = if (value != null) {
    writeInt(1)
    writer(value)
} else {
    writeInt(0)
}

inline fun <reified T: Enum<T>> Parcel.readEnum(): T? = readInt().let { if (it >= 0) enumValues<T>()[it] else null }

fun <T: Enum<T>> Parcel.writeEnum(value: T?) = writeInt(value?.ordinal ?: -1)

fun Parcel.readDate(): Date? = readNullable { Date(readLong()) }

fun Parcel.writeDate(value: Date?) = writeNullable(value) { writeLong(it.time) }