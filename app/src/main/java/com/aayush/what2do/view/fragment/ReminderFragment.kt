package com.aayush.what2do.view.fragment

import android.os.Bundle
import android.os.ParcelUuid
import android.view.*
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.aayush.what2do.App
import com.aayush.what2do.R
import com.aayush.what2do.model.TodoNote
import com.aayush.what2do.util.android.toast
import com.aayush.what2do.util.common.TODO_ID
import com.aayush.what2do.util.db.deleteTodoNote
import com.aayush.what2do.util.db.getTodoNoteById
import kotlinx.android.synthetic.main.fragment_reminder.*
import timber.log.Timber
import java.util.*

class ReminderFragment: BaseFragment() {
    private lateinit var snoozeOptions: Array<String>
    private var todoNotes: MutableList<TodoNote> = mutableListOf()
    private var todoNote: TodoNote? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_reminder, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val id: ParcelUuid = (parentContext as AppCompatActivity).intent!!.getParcelableExtra(TODO_ID)!!
        todoNotes = App.getAppDatabase(parentContext!!).getTodoNoteById(id).toMutableList()
        todoNote = if (todoNotes.isEmpty()) null else todoNotes[0]

        snoozeOptions = resources.getStringArray(R.array.snooze_options)
        text_todo_title.text = todoNote?.title

        if (todoNote != null) {
            btn_todo_remove.setOnClickListener {
                todoNotes.remove(todoNote!!)
                App.getAppDatabase(context!!).deleteTodoNote(todoNote!!)

                context?.toast("Todo note completed!")

                closeApp()
            }
        } else {
            Timber.d("Todo note is null")
        }

        spinner_snooze.adapter = ArrayAdapter<String>(
            context!!,
            R.layout.text_view_spinner,
            snoozeOptions
        ).apply { setDropDownViewResource(R.layout.spinner_dropdown_item) }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        (parentContext as AppCompatActivity).menuInflater.inflate(R.menu.menu_reminder, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.item_done -> {
                val date = addTimeToDate(valueFromSpinner())
                todoNote?.date = date
                todoNote?.hasReminder = true
                closeApp()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun addTimeToDate(minutes: Int): Date {
        val calendar = Calendar.getInstance().apply {
            time = Date()
            add(Calendar.MINUTE, minutes)
        }
        context?.toast("Snoozed for $minutes minutes")
        return calendar.time
    }

    private fun valueFromSpinner(): Int = when (spinner_snooze.selectedItemPosition) {
        0 -> 10
        1 -> 30
        2 -> 60
        else -> 0
    }

    private fun closeApp() {
        (parentContext as AppCompatActivity).finish()
    }

    companion object {
        @JvmStatic fun newInstance() = ReminderFragment()
    }
}
