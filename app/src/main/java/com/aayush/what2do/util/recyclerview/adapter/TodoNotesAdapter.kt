package com.aayush.what2do.util.recyclerview.adapter

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.text.format.DateFormat
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.RecyclerView
import com.aayush.what2do.App
import com.aayush.what2do.R
import com.aayush.what2do.model.TodoNote
import com.aayush.what2do.util.*
import com.aayush.what2do.util.service.TodoNotificationService
import com.aayush.what2do.view.activity.AddTodoActivity
import com.amulyakhare.textdrawable.TextDrawable
import com.google.android.material.snackbar.Snackbar

class TodoNotesAdapter(var context: Context) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var todoNotes = emptyList<TodoNote>().toMutableList()
    private lateinit var deletedItem : TodoNote
    private var deletedItemPosition = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_item, parent, false)
        return TodoNotesViewHolder(view)
    }

    override fun getItemCount() = todoNotes.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is TodoNotesViewHolder) {
            val todoNote = todoNotes[position]
            holder.todoTitleTextView.text = todoNote.title
            holder.todoDescriptionTextView.text = todoNote.description

            if (todoNote.hasReminder && todoNote.date != null) {
                holder.todoTitleTextView.maxLines = 1
                holder.todoReminderTextView.visibility = View.VISIBLE
            }
            else {
                holder.todoTitleTextView.maxLines = 2
                holder.todoReminderTextView.visibility = View.GONE
            }

            val textDrawable = TextDrawable.builder()
                .beginConfig()
                .textColor(Color.WHITE)
                .toUpperCase()
                .endConfig()
                .buildRound(todoNote.title[0].toString(), todoNote.color)

            holder.todoImageView.setImageDrawable(textDrawable)

            if (todoNote.date != null) {
                val timeToShow = formatDate(
                    if (DateFormat.is24HourFormat(context)) "MMM d, yyyy  h:mm a"
                    else "MMM d, yyyy  k:mm",
                    todoNote.date)
                holder.todoReminderTextView.text = timeToShow
            }
        }
    }

    inner class TodoNotesViewHolder(val view: View): RecyclerView.ViewHolder(view) {
        var todoTitleTextView : TextView
        var todoDescriptionTextView : TextView
        var todoReminderTextView: TextView
        var todoImageView: ImageView

        init {
            view.setOnClickListener {
                val todoNote = todoNotes[adapterPosition]
                val intent = Intent(context, AddTodoActivity::class.java)
                intent.putExtra(EXTRA_TODO_NOTE, todoNote)
                val fragmentManager = (context as AppCompatActivity).supportFragmentManager
                fragmentManager.findFragmentByTag(TAG_PENDING_FRAGMENT)?.startActivityForResult(intent, REQUEST_ID_TODO)
            }
            todoTitleTextView = view.findViewById(R.id.text_todo_title)
            todoDescriptionTextView = view.findViewById(R.id.text_todo_description)
            todoReminderTextView = view.findViewById(R.id.text_todo_date)
            todoImageView = view.findViewById(R.id.img_todo)

            todoImageView.setOnClickListener {
                todoImageView.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            }
        }
    }

    fun deleteItem(position: Int) {
        deletedItem = todoNotes[position]
        deletedItemPosition = position
        todoNotes.removeAt(position)
        notifyItemRemoved(position)
        deleteTodoNote(App.getAppDatabase(context).todoNoteDao(), deletedItem)
        deleteAlarm(context, Intent(context, TodoNotificationService::class.java), deletedItem.id.hashCode())
        showUndoSnackbar()
    }

    internal fun setTodoNotes(todoNotes: List<TodoNote>?) {
        if (todoNotes != null) {
            this.todoNotes = todoNotes.toMutableList()
            notifyDataSetChanged()
        }
    }

    private fun showUndoSnackbar() {
        val view: CoordinatorLayout = (context as AppCompatActivity).findViewById(R.id.coordinator_layout)
        val snackbar = Snackbar.make(view, "Undo", Snackbar.LENGTH_LONG)
        snackbar.setAction("Undo") {
            undoDelete()
        }
        snackbar.show()
    }

    private fun undoDelete() {
        (todoNotes as ArrayList).add(deletedItemPosition, deletedItem)
        notifyItemInserted(deletedItemPosition)
        insertTodoNote(App.getAppDatabase(context).todoNoteDao(), deletedItem)

        if (deletedItem.hasReminder && deletedItem.date != null) {
            val intent = Intent(context, TodoNotificationService::class.java)
            intent.putExtra(TODO_TITLE, deletedItem.title)
            intent.putExtra(TODO_ID, deletedItem.id)
            createAlarm(context, intent, deletedItem.id.hashCode(), deletedItem.date!!.time)
        }
    }
}