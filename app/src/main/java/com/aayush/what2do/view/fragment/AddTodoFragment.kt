package com.aayush.what2do.view.fragment


import android.animation.Animator
import android.app.Activity.RESULT_CANCELED
import android.app.Activity.RESULT_OK
import android.content.Context.INPUT_METHOD_SERVICE
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatSpinner
import androidx.core.app.NavUtils
import androidx.fragment.app.Fragment
import com.aayush.what2do.R
import com.aayush.what2do.model.Priority
import com.aayush.what2do.model.TodoNote
import com.aayush.what2do.util.EXTRA_TODO_NOTE
import com.aayush.what2do.util.TAG_DATE_FRAGMENT
import com.aayush.what2do.util.TAG_TIME_FRAGMENT
import com.aayush.what2do.util.formatDate
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog
import kotlinx.android.synthetic.main.fragment_add_todo.*
import java.util.*


class AddTodoFragment : Fragment(), DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {
    private lateinit var formatString : String

    private lateinit var todoNote : TodoNote
    private lateinit var todoTitle : String
    private lateinit var todoDescription : String
    private lateinit var todoPriority: String
    private var todoHasReminder : Boolean = false
    private var todoDate : Date? = null

    private lateinit var todoReminderSwitch : SwitchMaterial
    private lateinit var todoReminderTextView : TextView
    private lateinit var todoDateTextView : TextView
    private lateinit var todoTimeTextView : TextView
    private lateinit var todoTitleEditText : TextInputEditText
    private lateinit var todoDescriptionEditText : TextInputEditText
    private lateinit var todoPrioritySpinner: AppCompatSpinner
    private lateinit var todoMakeFab : FloatingActionButton
    private lateinit var dateLinearLayout: LinearLayout
    private lateinit var reminderLinearLayout : LinearLayout

    // <-- AppCompatActivity over-ridden methods -->

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        formatString = if (DateFormat.is24HourFormat(context)) {
            "k:mm"
        } else {
            "h:mm a"
        }

        return inflater.inflate(R.layout.fragment_add_todo, container, false)
    }

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
            todoReminderSwitch.isChecked = false
            todoReminderTextView.visibility = View.GONE
        }

        todoTitleEditText.requestFocus()
        todoTitleEditText.setText(todoTitle)
        todoDescriptionEditText.setText(todoDescription)

        val imm : InputMethodManager = activity?.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)
        todoTitleEditText.setSelection(todoTitleEditText.length())
        todoDescriptionEditText.setSelection(todoDescriptionEditText.length())

        setDateLayoutVisible(todoReminderSwitch.isChecked)

        todoReminderSwitch.isChecked = todoHasReminder && (todoDate != null)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            if (NavUtils.getParentActivityName(activity as AppCompatActivity) != null) {
                makeTodoNote(RESULT_CANCELED)
                hideKeyboard(todoTitleEditText)
                hideKeyboard(todoDescriptionEditText)
                NavUtils.navigateUpFromSameTask(activity as AppCompatActivity)
            }
            true
        } else {
            super.onOptionsItemSelected(item)
        }
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

        if (todoDate != null) {
            calendar.time = todoDate
        }

        hour = if (DateFormat.is24HourFormat(context)) {
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
        if (todoDate != null) {
            calendar.time = todoDate
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
        todoNote = activity?.intent?.getParcelableExtra(EXTRA_TODO_NOTE)!!

        todoTitle = todoNote.title
        todoDescription = todoNote.description
        todoPriority = todoNote.priority.toString()
        todoHasReminder = todoNote.hasReminder
        todoDate = todoNote.date
    }

    private fun setupViews() {
        todoReminderSwitch = switch_todo_reminder
        todoReminderTextView = text_todo_reminder
        todoDateTextView = text_todo_date
        todoTimeTextView = text_todo_time
        todoDescriptionEditText = edit_todo_desc
        todoTitleEditText = edit_todo_title
        todoPrioritySpinner = spinner_priority
        todoMakeFab = fab_make_todo
        dateLinearLayout = linear_layout_todo_time
        reminderLinearLayout = linear_layout_todo_reminder

        ArrayAdapter.createFromResource(context!!, R.array.priority_options, android.R.layout.simple_spinner_item)
            .also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                todoPrioritySpinner.adapter = adapter
            }
    }

    private fun setupListeners() {
        todoDateTextView.setOnClickListener {
            val date : Date? = if (todoNote.date != null) {
                todoDate
            } else {
                Date()
            }
            hideKeyboard(todoTitleEditText)
            hideKeyboard(todoDescriptionEditText)

            val calendar = Calendar.getInstance()
            calendar.time = date

            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog.newInstance(this, year, month, day)
            datePickerDialog.isThemeDark = true
            datePickerDialog.show(activity!!.supportFragmentManager, TAG_DATE_FRAGMENT)
        }

        todoTimeTextView.setOnClickListener {
            val date : Date? = if (todoNote.date != null) {
                todoDate
            } else {
                Date()
            }
            hideKeyboard(todoTitleEditText)
            hideKeyboard(todoDescriptionEditText)

            val calendar = Calendar.getInstance()
            calendar.time = date

            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            val timePickerDialog = TimePickerDialog.newInstance(
                this, hour, minute, DateFormat.is24HourFormat(context)
            )
            timePickerDialog.isThemeDark = true
            timePickerDialog.show(activity!!.supportFragmentManager, TAG_TIME_FRAGMENT)
        }

        todoTitleEditText.addTextChangedListener(
            object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {}

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    todoTitle = s.toString()
                }
            }
        )

        todoDescriptionEditText.addTextChangedListener(
            object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {}

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    todoDescription = s.toString()
                }
            }
        )

        todoReminderSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (!isChecked) {
                todoDate = null
            }
            todoHasReminder = isChecked
            setDateAndTimeTextView()
            setDateLayoutVisibleWithAnimations(isChecked)
            hideKeyboard(todoTitleEditText)
            hideKeyboard(todoDescriptionEditText)
        }

        todoMakeFab.setOnClickListener {
            if (todoTitleEditText.length() <= 0) {
                todoTitleEditText.error = getString(R.string.todo_error)
            }
            else if (todoDate != null && todoDate!!.before(Date())) {
                makeTodoNote(RESULT_CANCELED)
            }
            else {
                makeTodoNote(RESULT_OK)
                activity?.finish()
            }
            hideKeyboard(todoTitleEditText)
            hideKeyboard(todoDescriptionEditText)
        }

        reminderLinearLayout.setOnClickListener {
            hideKeyboard(todoTitleEditText)
            hideKeyboard(todoDescriptionEditText)
        }
    }

    private fun setDateAndTimeTextView() {
        if (todoNote.hasReminder && todoDate != null) {
            val date = formatDate("d MMM, yyyy", todoDate)
            val time = formatDate(formatString, todoDate)
            todoDateTextView.text = date
            todoTimeTextView.text = time
        }
        else {
            todoDateTextView.text = getString(R.string.date_default)
            val calendar : Calendar = Calendar.getInstance()

            if (DateFormat.is24HourFormat(context)) {
                calendar.set(Calendar.HOUR_OF_DAY, calendar.get(Calendar.HOUR_OF_DAY) + 1)
            }
            else {
                calendar.set(Calendar.HOUR, calendar.get(Calendar.HOUR) + 1)
            }
            calendar.set(Calendar.MINUTE, 0)
            todoDate = calendar.time

            todoTimeTextView.text = formatDate(formatString, todoDate)
        }
    }

    private fun setReminderTextView() {
        if (todoDate != null) {
            todoReminderTextView.visibility = View.VISIBLE
            if (todoDate!!.before(Date())) {
                todoReminderTextView.error = getString(R.string.date_error_check_again)
                return
            }
            val date = todoDate
            val dateString = formatDate("d MMM, yyyy", date)
            val timeString: String
            var amPmString = ""

            if (DateFormat.is24HourFormat(context)) {
                timeString = formatDate("k:mm", date)
            }
            else {
                timeString = formatDate("h:mm", date)
                amPmString = formatDate("a", date)
            }
            todoReminderTextView.text =
                String.format(getString(R.string.remind_date_and_time), dateString, timeString, amPmString)
        }
        else {
            todoReminderTextView.visibility = View.GONE
        }
    }

    private fun setDateTextView() {
        val dateFormat = "d MMM, yyyy"
        todoDateTextView.text = formatDate(dateFormat, todoDate)
    }

    private fun setTimeTextView() {
        todoTimeTextView.text = formatDate(formatString, todoDate)
    }

    private fun setDateLayoutVisible(checked : Boolean) {
        if (checked) {
            dateLinearLayout.visibility = View.VISIBLE
        }
        else {
            dateLinearLayout.visibility = View.GONE
        }
    }

    private fun setDateLayoutVisibleWithAnimations(checked: Boolean) {
        if (checked) {
            setReminderTextView()
            dateLinearLayout.animate().alpha(1.0f).setDuration(500).setListener(
                object : Animator.AnimatorListener {
                    override fun onAnimationStart(animation: Animator) {
                        dateLinearLayout.visibility = View.VISIBLE
                    }

                    override fun onAnimationEnd(animation: Animator) {}

                    override fun onAnimationCancel(animation: Animator) {}

                    override fun onAnimationRepeat(animation: Animator) {}
                }
            )
        }
        else {
            dateLinearLayout.animate().alpha(0.0f).setDuration(500).setListener(
                object : Animator.AnimatorListener {
                    override fun onAnimationStart(animation: Animator) {}

                    override fun onAnimationEnd(animation: Animator) {
                        dateLinearLayout.visibility = View.GONE
                    }

                    override fun onAnimationCancel(animation: Animator) {}

                    override fun onAnimationRepeat(animation: Animator) {}
                }
            )
        }
    }

    private fun makeTodoNote(result : Int) {
        val intent = Intent()

        todoTitle = todoTitle.capitalize()
        todoDescription = todoDescription.capitalize()
        todoPriority = todoPrioritySpinner.selectedItem.toString()

        if (todoDate != null) {
            val calendar = Calendar.getInstance()
            calendar.time = todoDate
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
        activity?.setResult(result, intent)
    }

    private fun hideKeyboard(editText: TextInputEditText) {
        val imm : InputMethodManager = activity?.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(editText.windowToken, 0)
    }

    companion object {
        @JvmStatic
        fun newInstance() = AddTodoFragment()
    }
}
