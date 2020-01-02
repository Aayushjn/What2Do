package com.aayush.what2do.view.fragment


import android.animation.Animator
import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.content.Context.INPUT_METHOD_SERVICE
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NavUtils
import com.aayush.what2do.R
import com.aayush.what2do.model.Priority
import com.aayush.what2do.model.TodoNote
import com.aayush.what2do.util.android.asString
import com.aayush.what2do.util.android.hideKeyboard
import com.aayush.what2do.util.android.is24HourFormat
import com.aayush.what2do.util.common.*
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog
import kotlinx.android.synthetic.main.fragment_add_todo.*
import java.util.*


class AddTodoFragment: BaseFragment(), DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {
    private val timeFormatString: String by lazy {
        if (is24HourFormat(context!!)) "k:mm" else "h:mm a"
    }

    private lateinit var todoNote: TodoNote
    private lateinit var todoTitle: String
    private lateinit var todoDescription: String
    private lateinit var todoPriority: String
    private var todoHasReminder: Boolean = false
    private var todoDate: Date? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_add_todo, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        extractTodoNote()
        setupViews()
        setupListeners()
        setDateAndTimeTextView()

        if (todoHasReminder && (todoDate != null)) {
            setReminderTextView()
            setDateLayoutVisibleWithAnimations(true)
        }
        if (todoDate == null) {
            switch_todo_reminder.isChecked = false
            text_todo_reminder.visibility = View.GONE
        }

        edit_todo_title.requestFocus()
        edit_todo_title.setText(todoTitle)
        edit_todo_desc.setText(todoDescription)
        spinner_priority.setSelection(todoNote.priority.priorityNumber)

        val imm: InputMethodManager = (parentContext as AppCompatActivity).getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)
        edit_todo_title.setSelection(edit_todo_title.length())
        edit_todo_desc.setSelection(edit_todo_desc.length())

        setDateLayoutVisible(switch_todo_reminder.isChecked)

        switch_todo_reminder.isChecked = todoHasReminder && (todoDate != null)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        if (item.itemId == android.R.id.home) {
            if (NavUtils.getParentActivityName((parentContext as AppCompatActivity)) != null) {
                makeTodoNote(RESULT_CANCELED)
                hideKeyboard(edit_todo_title)
                hideKeyboard(edit_todo_desc)
                NavUtils.navigateUpFromSameTask((parentContext as AppCompatActivity))
            }
            true
        } else {
            super.onOptionsItemSelected(item)
        }

    // <-- DatePickerDialog.OnDateSetListener over-ridden method -->

    override fun onDateSet(view: DatePickerDialog?, year: Int, monthOfYear: Int, dayOfMonth: Int) {
        val calendar = Calendar.getInstance()
        val hour: Int
        val minute: Int

        val reminderCalendar = Calendar.getInstance()
        reminderCalendar.set(year, monthOfYear, dayOfMonth)

        if (reminderCalendar.before(calendar)) {
            return
        }

        todoDate?.let {
            calendar.time = it
        }

        hour = if (is24HourFormat(context!!)) {
            calendar.get(Calendar.HOUR_OF_DAY)
        } else {
            calendar.get(Calendar.HOUR)
        }
        minute = calendar.get(Calendar.MINUTE)

        calendar.set(year, monthOfYear, dayOfMonth, hour, minute)
        todoDate = calendar.time
        setReminderTextView()
        setDateTextView()
    }

    // <-- TimePickerDialog.OnTimeSetListener over-ridden method -->

    override fun onTimeSet(view: TimePickerDialog?, hourOfDay: Int, minute: Int, second: Int) {
        val calendar = Calendar.getInstance()
        todoDate?.let {
            calendar.time = it
        }

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        calendar.set(year, month, day, hourOfDay, minute, second)
        todoDate = calendar.time

        setReminderTextView()
        setTimeTextView()
    }

    private fun extractTodoNote() {
        todoNote = (parentContext as AppCompatActivity).intent?.getParcelableExtra(EXTRA_TODO_NOTE)!!

        todoTitle = todoNote.title
        todoDescription = todoNote.description
        todoPriority = todoNote.priority.toString()
        todoHasReminder = todoNote.hasReminder
        todoDate = todoNote.date
    }

    private fun setupViews() {
        ArrayAdapter.createFromResource(context!!, R.array.priority_options, android.R.layout.simple_spinner_item)
            .apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinner_priority.adapter = this
            }
    }

    private fun setupListeners() {
        text_todo_date.setOnClickListener {
            val date: Date = todoDate ?: Date()
            hideKeyboard(edit_todo_title)
            hideKeyboard(edit_todo_desc)

            val calendar = Calendar.getInstance()
            calendar.time = date

            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            DatePickerDialog.newInstance(this, year, month, day).apply {
                isThemeDark = true
                show((parentContext as AppCompatActivity).supportFragmentManager, TAG_DATE_FRAGMENT)
            }
        }

        text_todo_time.setOnClickListener {
            val date: Date = todoDate ?: Date()
            hideKeyboard(edit_todo_title)
            hideKeyboard(edit_todo_desc)

            val calendar = Calendar.getInstance()
            calendar.time = date

            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            TimePickerDialog.newInstance(this, hour, minute, is24HourFormat(context!!)).apply {
                isThemeDark = true
                show((parentContext as AppCompatActivity).supportFragmentManager, TAG_TIME_FRAGMENT)
            }
        }

        edit_todo_title.addTextChangedListener(
            object: TextWatcher {
                override fun afterTextChanged(s: Editable?) {}
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    todoTitle = s.toString()
                }
            }
        )

        edit_todo_desc.addTextChangedListener(
            object: TextWatcher {
                override fun afterTextChanged(s: Editable?) {}
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    todoDescription = s.toString()
                }
            }
        )

        switch_todo_reminder.setOnCheckedChangeListener { _, isChecked ->
            if (!isChecked) {
                todoDate = null
            }
            todoHasReminder = isChecked
            setDateAndTimeTextView()
            setDateLayoutVisibleWithAnimations(isChecked)
            hideKeyboard(edit_todo_title)
            hideKeyboard(edit_todo_desc)
        }

        fab_make_todo.setOnClickListener {
            if (edit_todo_title.length() <= 0) {
                edit_todo_title.error = getString(R.string.todo_error)
            } else if (todoDate != null && todoDate!!.before(Date())) {
                makeTodoNote(RESULT_CANCELED)
            } else {
                makeTodoNote(RESULT_OK)
                (parentContext as AppCompatActivity).finish()
            }
            hideKeyboard(edit_todo_title)
            hideKeyboard(edit_todo_desc)
        }

        linear_layout_todo_reminder.setOnClickListener {
            hideKeyboard(edit_todo_title)
            hideKeyboard(edit_todo_desc)
        }
    }

    private fun setDateAndTimeTextView() {
        if (todoNote.hasReminder && todoDate != null) {
            val date = todoDate.formattedDate()
            val time = todoDate.formattedTime(timeFormatString)
            text_todo_date.text = date
            text_todo_time.text = time
        }
        else {
            text_todo_date.text = getString(R.string.date_default)
            val calendar : Calendar = Calendar.getInstance()

            if (is24HourFormat(context!!)) {
                calendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY) + 1)
            }
            else {
                calendar.set(Calendar.HOUR, calendar.get(Calendar.HOUR) + 1)
            }
            calendar.set(Calendar.MINUTE, 0)
            todoDate = calendar.time

            text_todo_time.text = todoDate.formattedTime(timeFormatString)
        }
    }

    private fun setReminderTextView() {
        if (todoDate != null) {
            text_todo_reminder.visibility = View.VISIBLE
            if (todoDate!!.before(Date())) {
                text_todo_reminder.error = getString(R.string.date_error_check_again)
                return
            }
            val dateString = todoDate.formattedDate()
            val timeString: String = todoDate.formattedTime(timeFormatString)
            text_todo_reminder.text =
                String.format(getString(R.string.remind_date_and_time), dateString, timeString)
        } else {
            text_todo_reminder.visibility = View.GONE
        }
    }

    private fun setDateTextView() {
        text_todo_date.text = todoDate.formattedDate()
    }

    private fun setTimeTextView() {
        text_todo_time.text = todoDate.formattedTime(timeFormatString)
    }

    private fun setDateLayoutVisible(checked : Boolean) = if (checked) {
        linear_layout_todo_time.visibility = View.VISIBLE
    } else {
        linear_layout_todo_time.visibility = View.GONE
    }

    private fun setDateLayoutVisibleWithAnimations(checked: Boolean) {
        if (checked) {
            setReminderTextView()
            linear_layout_todo_time.animate().alpha(1.0f).setDuration(500).setListener(
                object : Animator.AnimatorListener {
                    override fun onAnimationStart(animation: Animator) {
                        linear_layout_todo_time.visibility = View.VISIBLE
                    }

                    override fun onAnimationEnd(animation: Animator) {}
                    override fun onAnimationCancel(animation: Animator) {}
                    override fun onAnimationRepeat(animation: Animator) {}
                }
            )
        } else {
            linear_layout_todo_time.animate().alpha(0.0f).setDuration(500).setListener(
                object : Animator.AnimatorListener {
                    override fun onAnimationStart(animation: Animator) {}

                    override fun onAnimationEnd(animation: Animator) {
                        linear_layout_todo_time.visibility = View.GONE
                    }

                    override fun onAnimationCancel(animation: Animator) {}
                    override fun onAnimationRepeat(animation: Animator) {}
                }
            )
        }
    }

    private fun makeTodoNote(result : Int) {
        val intent = Intent()

        todoTitle = edit_todo_title.asString().capitalize()
        todoDescription = edit_todo_desc.asString().capitalize()
        todoPriority = spinner_priority.selectedItem.toString()

        todoDate?.let {
            val calendar = Calendar.getInstance()
            calendar.time = it
            calendar.set(Calendar.SECOND, 0)
            todoDate = calendar.time
        }

        todoNote.title = todoTitle
        todoNote.description = todoDescription
        todoNote.priority = when (todoPriority) {
            "High" -> Priority.HIGH
            "Medium" -> Priority.MEDIUM
            else -> Priority.LOW
        }
        todoNote.hasReminder = todoHasReminder
        todoNote.date = todoDate

        intent.putExtra(EXTRA_TODO_NOTE, todoNote)
        (parentContext as AppCompatActivity).setResult(result, intent)
    }

    companion object {
        @JvmStatic fun newInstance() = AddTodoFragment()
    }
}
