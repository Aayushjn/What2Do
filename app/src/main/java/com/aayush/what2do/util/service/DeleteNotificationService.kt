package com.aayush.what2do.util.service

import android.app.IntentService
import android.content.Intent
import android.os.ParcelUuid
import com.aayush.what2do.App
import com.aayush.what2do.model.TodoNote
import com.aayush.what2do.util.TODO_ID
import com.aayush.what2do.util.getAllTodoNotes

class DeleteNotificationService: IntentService("DeleteNotificationService") {
    private lateinit var todoNotes: List<TodoNote>
    private var todoNote: TodoNote? = null

    override fun onHandleIntent(intent: Intent?) {
        val id = intent?.getParcelableExtra<ParcelUuid>(TODO_ID)!!

        getAllTodoNotes(App.getAppDatabase(this).todoNoteDao())
        for (todo in todoNotes) {
            if (todo.id == id) {
                todoNote = todo
                break
            }
        }

        if (todoNote != null) {
            (todoNotes as ArrayList).remove(todoNote!!)
        }
    }
}