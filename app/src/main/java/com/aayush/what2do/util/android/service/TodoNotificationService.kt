package com.aayush.what2do.util.android.service

import android.app.IntentService
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.os.ParcelUuid
import androidx.core.app.NotificationCompat
import com.aayush.what2do.R
import com.aayush.what2do.util.android.getNotificationManager
import com.aayush.what2do.util.common.CHANNEL_REMINDER
import com.aayush.what2do.util.common.CHANNEL_REMINDER_ID
import com.aayush.what2do.util.common.TODO_ID
import com.aayush.what2do.util.common.TODO_TITLE
import com.aayush.what2do.view.activity.ReminderActivity

class TodoNotificationService: IntentService("TodoNotificationService") {
    private lateinit var text: String
    private lateinit var id: ParcelUuid

    override fun onHandleIntent(intent: Intent?) {
        text = intent?.getStringExtra(TODO_TITLE)!!
        id = intent.getParcelableExtra(TODO_ID)!!

        val notificationManager: NotificationManager = getNotificationManager()
        val notificationIntent = Intent(this, ReminderActivity::class.java).apply {
            putExtra(TODO_ID, id)
        }
        val deleteIntent = Intent(this, DeleteNotificationService::class.java).apply {
            putExtra(TODO_ID, id)
        }

        val notification: Notification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationCompat.Builder(this, CHANNEL_REMINDER_ID)
                .setContentTitle(text)
                .setSmallIcon(R.drawable.ic_done)
                .setAutoCancel(true)
                .setDeleteIntent(PendingIntent.getService(
                    this,
                    id.hashCode(),
                    deleteIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                ))
                .setContentIntent(PendingIntent.getActivity(
                    this,
                    id.hashCode(),
                    notificationIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                ))
                .build()
        } else {
            NotificationCompat.Builder(this)
                .setContentTitle(text)
                .setSmallIcon(R.drawable.ic_done)
                .setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setDeleteIntent(PendingIntent.getService(
                    this,
                    id.hashCode(),
                    deleteIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                ))
                .setContentIntent(PendingIntent.getActivity(
                    this,
                    id.hashCode(),
                    notificationIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                ))
                .build()
        }
        notificationManager.notify(100, notification)
    }
}