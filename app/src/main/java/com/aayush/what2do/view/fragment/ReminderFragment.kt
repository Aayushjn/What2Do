package com.aayush.what2do.view.fragment

import android.os.Bundle
import android.os.ParcelUuid
import android.view.*
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatSpinner
import androidx.fragment.app.Fragment
import com.aayush.what2do.App
import com.aayush.what2do.R
import com.aayush.what2do.model.TodoNote
import com.aayush.what2do.util.TODO_ID
import com.aayush.what2do.util.deleteTodoNote
import com.aayush.what2do.util.getAllTodoNotes
import com.google.android.material.button.MaterialButton
import kotlinx.android.synthetic.main.fragment_reminder.*
import timber.log.Timber
import java.util.*

class ReminderFragment: Fragment() {
    private lateinit var todoTitleTextView: TextView
    private lateinit var removeButton: MaterialButton
    private lateinit var spinner: AppCompatSpinner
    private lateinit var snoozeOptions: Array<String>
    private lateinit var todoNotes: List<TodoNote>
    private var todoNote: TodoNote? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_reminder, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews()

        todoNotes = getAllTodoNotes(App.getAppDatabase(context!!).todoNoteDao())

        val intent = activity?.intent!!
        val id: ParcelUuid = intent.getParcelableExtra(TODO_ID)

        for (note in todoNotes) {
            if (note.id == id) {
                todoNote = note
                break
            }
        }

        snoozeOptions = resources.getStringArray(R.array.snooze_options)
        todoTitleTextView.text = todoNote?.title

        if (todoNote != null) {
            removeButton.setOnClickListener {
                (todoNotes as ArrayList).remove(todoNote!!)
                deleteTodoNote(App.getAppDatabase(context!!).todoNoteDao(), todoNote!!)
                Toast.makeText(context, "Todo note completed!", Toast.LENGTH_SHORT).show()

                closeApp()
            }
        }
        else {
            Timber.d("Todo note is null")
        }

        val adapter = ArrayAdapter<String>(context!!, R.layout.text_view_spinner, snoozeOptions)
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        spinner.adapter = adapter
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        activity?.menuInflater?.inflate(R.menu.menu_reminder, menu)
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
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    private fun addTimeToDate(minutes: Int): Date {
        val date = Date()
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.add(Calendar.MINUTE, minutes)
        Toast.makeText(context, "Snoozed for $minutes minutes", Toast.LENGTH_SHORT).show()
        return calendar.time
    }

    private fun valueFromSpinner(): Int {
        return when (spinner.selectedItemPosition) {
            0 -> 10
            1 -> 30
            2 -> 60
            else -> 0
        }
    }

    private fun closeApp() {
        activity?.finish()
    }

    private fun setupViews() {
        todoTitleTextView = text_todo_title
        removeButton = btn_todo_remove
        spinner = spinner_snooze
    }

    companion object {
        @JvmStatic
        fun newInstance() = ReminderFragment()
    }
}
