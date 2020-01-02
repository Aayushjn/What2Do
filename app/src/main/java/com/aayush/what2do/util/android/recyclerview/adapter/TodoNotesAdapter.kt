package com.aayush.what2do.util.android.recyclerview.adapter

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.speech.tts.TextToSpeech
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.RecyclerView
import com.aayush.what2do.App
import com.aayush.what2do.R
import com.aayush.what2do.model.TodoNote
import com.aayush.what2do.util.android.*
import com.aayush.what2do.util.android.service.TodoNotificationService
import com.aayush.what2do.util.common.*
import com.aayush.what2do.util.db.deleteTodoNote
import com.aayush.what2do.util.db.insertTodoNote
import com.aayush.what2do.view.activity.AddTodoActivity
import com.amulyakhare.textdrawable.TextDrawable
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.recycler_view_item.view.*
import timber.log.Timber
import java.util.*

class TodoNotesAdapter(var context: Context): RecyclerView.Adapter<TodoNotesAdapter.TodoNotesViewHolder>() {
    private lateinit var deletedItem: TodoNote
    private var deletedItemPosition = 0

    var todoNotes: MutableList<TodoNote> = mutableListOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    var tts: TextToSpeech? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoNotesViewHolder =
        TodoNotesViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.recycler_view_item, parent, false))

    override fun onBindViewHolder(holder: TodoNotesViewHolder, position: Int) = holder.bind(todoNotes[position])

    override fun getItemCount(): Int = todoNotes.size

    fun deleteItem(position: Int) {
        deletedItem = todoNotes[position]
        deletedItemPosition = position
        todoNotes.removeAt(position)
        notifyItemRemoved(position)
        App.getAppDatabase(context).deleteTodoNote(deletedItem)

        context.toast("Todo note deleted!")
        deleteAlarm(
            context,
            Intent(context, TodoNotificationService::class.java),
            deletedItem.id.hashCode()
        )
        showUndoSnackbar()
    }

    fun readItem(position: Int) {
        tts = TextToSpeech(context, TextToSpeech.OnInitListener {
            if (it == TextToSpeech.SUCCESS) {
                val result = tts?.setLanguage(Locale.getDefault())
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    context.toast("This Language is not supported")
                } else {
                    val selectedItem = todoNotes[position]
                    val text = if (selectedItem.hasReminder && selectedItem.date != null) {
                        selectedItem.title + "\n" + selectedItem.description + "\n" +
                                selectedItem.priority + " priority\nTo be completed on " +
                                selectedItem.date.formattedString()
                    } else {
                        selectedItem.title + "\n" + selectedItem.description + "\n" +
                                selectedItem.priority + " priority"
                    }
                    tts.speak(text,
                        UTTERANCE_ID_TODO_NOTE
                    )
                }
            } else {
                Timber.e("Initialization Failed!")
            }
        })
        notifyItemRemoved(position)
        notifyItemInserted(position)
    }

    private fun showUndoSnackbar() {
        val view: CoordinatorLayout = (context as AppCompatActivity).findViewById(R.id.coordinator_layout)
        view.snackbar("Undo") { setAction("Undo") { undoDelete() } }
    }

    private fun undoDelete() {
        todoNotes.add(deletedItemPosition, deletedItem)
        notifyItemInserted(deletedItemPosition)
        App.getAppDatabase(context).insertTodoNote(deletedItem)
        context.toast("Todo note added back!")

        if (deletedItem.hasReminder && deletedItem.date != null) {
            val intent = Intent(context, TodoNotificationService::class.java)
            intent.putExtra(TODO_TITLE, deletedItem.title)
            intent.putExtra(TODO_ID, deletedItem.id)
            createAlarm(
                context,
                intent,
                deletedItem.id.hashCode(),
                deletedItem.date!!.time
            )
        }
    }

    inner class TodoNotesViewHolder(override val containerView: View): RecyclerView.ViewHolder(containerView), LayoutContainer {
        init {
            containerView.setOnClickListener {
                val todoNote = todoNotes[adapterPosition]
                val intent = Intent(context, AddTodoActivity::class.java)
                intent.putExtra(EXTRA_TODO_NOTE, todoNote)
                val fragmentManager = (context as AppCompatActivity).supportFragmentManager
                fragmentManager.findFragmentByTag(TAG_PENDING_FRAGMENT)?.startActivityForResult(
                    intent,
                    REQ_ID_TODO
                )
            }

            containerView.img_todo.setOnClickListener {
                containerView.img_todo.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            }
        }

        fun bind(todoNote: TodoNote) {
            containerView.text_todo_title.text = todoNote.title
            containerView.text_todo_description.text = todoNote.description

            if (todoNote.hasReminder && todoNote.date != null) {
                containerView.text_todo_title.maxLines = 1
                containerView.text_todo_date.visibility = View.VISIBLE
            } else {
                containerView.text_todo_title.maxLines = 2
                containerView.text_todo_date.visibility = View.GONE
            }

            val textDrawable = TextDrawable.builder()
                .beginConfig()
                .textColor(Color.WHITE)
                .toUpperCase()
                .endConfig()
                .buildRound(todoNote.title[0].toString(), todoNote.color)

            containerView.img_todo.setImageDrawable(textDrawable)

            todoNote.date?.let { containerView.text_todo_date.text = it.formattedString() }
        }
    }
}