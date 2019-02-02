package com.aayush.what2do.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.os.Build
import android.text.format.DateFormat
import com.aayush.what2do.db.TodoNoteDao
import com.aayush.what2do.model.TodoNote
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import java.util.*

fun getAllTodoNotes(dao: TodoNoteDao): List<TodoNote> = runBlocking(Dispatchers.IO) {
    dao.getAllTodoNotes()
}

fun insertTodoNote(dao: TodoNoteDao, todoNote: TodoNote) = runBlocking(Dispatchers.IO) {
    dao.insertTodoNote(todoNote)
}

fun deleteTodoNote(dao: TodoNoteDao, todoNote: TodoNote) = runBlocking(Dispatchers.IO) {
    dao.deleteTodoNote(todoNote)
}

fun formatDate(formatString : String, date : Date?) : String {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        SimpleDateFormat(formatString).format(date)
    } else {
        DateFormat.format(formatString, date).toString()
    }
}

fun getAlarmManager(context: Context): AlarmManager {
    return context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
}

fun createAlarm(context: Context, intent: Intent, requestCode: Int, time: Long) {
    val alarmManager = getAlarmManager(context)
    val pendingIntent = PendingIntent.getService(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    alarmManager.set(AlarmManager.RTC_WAKEUP, time, pendingIntent)
}

private fun doesPendingIntentExist(context: Context, intent: Intent, requestCode: Int): Boolean {
    val pendingIntent = PendingIntent.getService(context, requestCode, intent, PendingIntent.FLAG_NO_CREATE)
    return pendingIntent != null
}

fun deleteAlarm(context: Context, intent: Intent, requestCode: Int) {
    if (doesPendingIntentExist(context, intent, requestCode)) {
        val pendingIntent = PendingIntent.getService(context, requestCode, intent, PendingIntent.FLAG_NO_CREATE)
        pendingIntent.cancel()
        getAlarmManager(context).cancel(pendingIntent)
    }
}