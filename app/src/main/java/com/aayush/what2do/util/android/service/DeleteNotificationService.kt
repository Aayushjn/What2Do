package com.aayush.what2do.util.android.service

import android.app.IntentService
import android.content.Intent
import android.os.ParcelUuid
import com.aayush.what2do.App
import com.aayush.what2do.model.TodoNote
import com.aayush.what2do.util.common.TODO_ID
import com.aayush.what2do.util.db.getAllTodoNotes

class DeleteNotificationService: IntentService("DeleteNotificationService") {
    private lateinit var todoNotes: MutableList<TodoNote>
    private var todoNote: TodoNote? = null

    override fun onHandleIntent(intent: Intent?) {
        val id: ParcelUuid = intent?.getParcelableExtra(TODO_ID)!!

        todoNotes = App.getAppDatabase(this).getAllTodoNotes().value!!.toMutableList()
        todoNote = todoNotes.find { it.id == id }
        todoNote?.let { todoNotes.remove(it) }
    }
}