package com.aayush.what2do.util.android

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.speech.tts.TextToSpeech
import android.text.format.DateFormat
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText

fun Context.toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()

fun Context.toastLong(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_LONG).show()

fun Context.getAlarmManager(): AlarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

fun Context.getNotificationManager(): NotificationManager =
    getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

inline fun View.snackbar(text: String, func: Snackbar.() -> Snackbar) =
    Snackbar.make(this, text, Snackbar.LENGTH_LONG).func().show()

fun TextToSpeech?.speak(msg: String, utteranceId: String) {
    this?.speak(msg, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
}

fun Fragment.hideKeyboard(editText: TextInputEditText) {
    val imm: InputMethodManager = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(editText.windowToken, 0)
}

fun EditText.asString(): String = text.toString()

fun is24HourFormat(context: Context): Boolean = DateFormat.is24HourFormat(context)

fun createAlarm(context: Context, intent: Intent, requestCode: Int, time: Long) {
    val alarmManager = context.getAlarmManager()
    val pendingIntent = PendingIntent.getService(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    alarmManager.set(AlarmManager.RTC_WAKEUP, time, pendingIntent)
}

fun deleteAlarm(context: Context, intent: Intent, requestCode: Int) {
    if (doesPendingIntentExist(context, intent, requestCode)) {
        val pendingIntent = PendingIntent.getService(context, requestCode, intent, PendingIntent.FLAG_NO_CREATE)
        pendingIntent.cancel()
        context.getAlarmManager().cancel(pendingIntent)
    }
}

fun doesPendingIntentExist(context: Context, intent: Intent, requestCode: Int): Boolean =
    PendingIntent.getService(context, requestCode, intent, PendingIntent.FLAG_NO_CREATE) != null