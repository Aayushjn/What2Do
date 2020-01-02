package com.aayush.what2do.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.aayush.what2do.App
import com.aayush.what2do.model.TodoNote

class TodoNoteViewModel(application: Application): AndroidViewModel(application) {
    var todoNoteLiveData: LiveData<List<TodoNote>> =
        App.getAppDatabase(application).todoNoteDao().getAllTodoNotes()
}