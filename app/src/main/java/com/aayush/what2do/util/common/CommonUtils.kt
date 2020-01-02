package com.aayush.what2do.util.common

import java.text.SimpleDateFormat
import java.util.*

val DEFAULT_LOCALE: Locale = Locale.US

fun Date?.formattedString(): String = this?.let { SimpleDateFormat.getDateTimeInstance().format(it) } ?: ""

fun Date?.formattedDate(): String = this?.let { SimpleDateFormat.getDateInstance().format(it) } ?: ""

fun Date?.formattedTime(pattern: String): String = this?.let { SimpleDateFormat(pattern, Locale.getDefault()).format(it) } ?: ""

fun String.capitalize(): String = if (!isEmpty() && this[0].isLetter()) {
    this[0].toUpperCase() + substring(1)
} else {
    this
}