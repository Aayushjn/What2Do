package com.aayush.what2do.util.db

import android.os.ParcelUuid
import androidx.lifecycle.LiveData
import com.aayush.what2do.db.AppDatabase
import com.aayush.what2do.model.TodoNote
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

fun AppDatabase.getAllTodoNotes(): LiveData<List<TodoNote>> = runBlocking(Dispatchers.IO) {
    this@getAllTodoNotes.todoNoteDao().getAllTodoNotes()
}

fun AppDatabase.getTodoNoteById(id: ParcelUuid): List<TodoNote> = runBlocking(Dispatchers.IO) {
    this@getTodoNoteById.todoNoteDao().getTodoNoteById(id)
}

fun AppDatabase.updateTodoNote(todoNote: TodoNote) = runBlocking(Dispatchers.IO) {
    this@updateTodoNote.todoNoteDao().update(todoNote)
}

fun AppDatabase.insertTodoNote(todoNote: TodoNote) = runBlocking(Dispatchers.IO) {
    this@insertTodoNote.todoNoteDao().insert(todoNote)
}

fun AppDatabase.deleteTodoNote(todoNote: TodoNote) = runBlocking(Dispatchers.IO) {
    this@deleteTodoNote.todoNoteDao().delete(todoNote)
}
