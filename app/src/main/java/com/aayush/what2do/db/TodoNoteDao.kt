package com.aayush.what2do.db

import androidx.room.*
import com.aayush.what2do.model.TodoNote

@Dao
interface TodoNoteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTodoNote(todoNote: TodoNote)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateTodoNote(todoNote: TodoNote)

    @Delete
    fun deleteTodoNote(todoNote: TodoNote)

    @Query("SELECT * FROM TodoNote")
    fun getAllTodoNotes(): List<TodoNote>
}