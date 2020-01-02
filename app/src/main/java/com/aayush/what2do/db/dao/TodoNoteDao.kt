package com.aayush.what2do.db.dao

import android.os.ParcelUuid
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Query
import com.aayush.what2do.model.TodoNote

@Dao
interface TodoNoteDao: BaseDao<TodoNote> {
    @Query("SELECT * FROM TodoNote")
    fun getAllTodoNotes(): LiveData<List<TodoNote>>

    @Query("SELECT * FROM TodoNote WHERE id = :id")
    suspend fun getTodoNoteById(id: ParcelUuid): List<TodoNote>
}