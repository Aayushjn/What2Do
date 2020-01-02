package com.aayush.what2do.view.fragment

import android.app.Activity.RESULT_CANCELED
import android.content.Intent
import android.os.Bundle
import android.os.ParcelUuid
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.OvershootInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.observe
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aayush.what2do.App
import com.aayush.what2do.R
import com.aayush.what2do.model.Priority
import com.aayush.what2do.model.TodoNote
import com.aayush.what2do.model.has
import com.aayush.what2do.util.android.createAlarm
import com.aayush.what2do.util.android.recyclerview.adapter.TodoNotesAdapter
import com.aayush.what2do.util.android.recyclerview.callback.SwipeCallback
import com.aayush.what2do.util.android.service.TodoNotificationService
import com.aayush.what2do.util.android.speak
import com.aayush.what2do.util.android.toast
import com.aayush.what2do.util.common.*
import com.aayush.what2do.util.db.insertTodoNote
import com.aayush.what2do.util.db.updateTodoNote
import com.aayush.what2do.view.activity.AddTodoActivity
import com.aayush.what2do.viewmodel.TodoNoteViewModel
import com.amulyakhare.textdrawable.util.ColorGenerator
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog
import jp.wasabeef.recyclerview.animators.SlideInRightAnimator
import kotlinx.android.synthetic.main.fragment_pending.*
import timber.log.Timber
import java.util.*


class PendingFragment: BaseFragment(), DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {
    private val viewModel: TodoNoteViewModel by activityViewModels()
    private var todoNotes: MutableList<TodoNote> = mutableListOf()

    private val adapter: TodoNotesAdapter by lazy { TodoNotesAdapter((parentContext as AppCompatActivity)) }

    private lateinit var speechRecognizer: SpeechRecognizer
    private var tts: TextToSpeech? = null

    private lateinit var title: String
    private var description = ""
    private lateinit var priority: Priority
    private var reminder: Date? = null
    private val color = ColorGenerator.MATERIAL.randomColor

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_pending, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews()
        retrieveTodoNotes()
        setAlarms()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != RESULT_CANCELED && requestCode == REQ_ID_TODO) {
            val todoNote: TodoNote? = data?.getParcelableExtra(EXTRA_TODO_NOTE)
            if (todoNote?.title?.length!! <= 0) {
                return
            }

            if (todoNote.hasReminder && todoNote.date != null) {
                if (todoNote.date!!.compareTo(Date()) != 0) {
                    val intent = Intent(context!!, TodoNotificationService::class.java).apply {
                        putExtra(TODO_ID, todoNote.id)
                        putExtra(TODO_TITLE, todoNote.title)
                    }
                    createAlarm(context!!, intent, todoNote.id.hashCode(), todoNote.date!!.time)
                }
            }

            Timber.d(todoNote.toString())
            todoNotes.forEach { Timber.d(it.id.toString()) }
            val res = todoNotes.has(todoNote)
            var msg = "Todo note added!"
            if (res.first) {
                App.getAppDatabase(context!!).updateTodoNote(todoNote)
                msg = "Todo note updated!"
            } else {
                App.getAppDatabase(context!!).insertTodoNote(todoNote)
            }
            context?.toast(msg)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onResume() {
        super.onResume()
        adapter.todoNotes = todoNotes.sortedDescending().toMutableList()
        setAlarms()
    }

    override fun onPause() {
        super.onPause()
        adapter.tts?.let { if (it.isSpeaking) it.stop() }
        tts?.let { if (it.isSpeaking) it.stop() }
    }

    override fun onDestroy() {
        super.onDestroy()
        adapter.tts?.shutdown()
        tts?.shutdown()
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

        reminder?.let { calendar.time = it }

        hour = if (DateFormat.is24HourFormat(context)) {
            calendar.get(Calendar.HOUR_OF_DAY)
        } else {
            calendar.get(Calendar.HOUR)
        }
        minute = calendar.get(Calendar.MINUTE)

        calendar.set(year, monthOfYear, dayOfMonth, hour, minute)
        reminder = calendar.time

        TimePickerDialog.newInstance(this, hour, minute, DateFormat.is24HourFormat(context)).apply {
            isThemeDark = true
            show((parentContext as AppCompatActivity).supportFragmentManager, TAG_TIME_FRAGMENT)
        }
    }

    // <-- TimePickerDialog.OnTimeSetListener over-ridden method -->

    override fun onTimeSet(view: TimePickerDialog?, hourOfDay: Int, minute: Int, second: Int) {
        val calendar = Calendar.getInstance()
        reminder?.let { calendar.time = it }

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        calendar.set(year, month, day, hourOfDay, minute, second)
        reminder = calendar.time

        val todoNote = TodoNote(ParcelUuid(UUID.randomUUID()), title, description, priority,
            reminder != null, reminder, color)
        App.getAppDatabase(context!!).insertTodoNote(todoNote)

        initTTS("Todo note added", UTTERANCE_ID_ADD_TODO)
    }

    private fun setupViews() {
        with(recycler_todo) {
            setEmptyView(img_empty_state)
            setHasFixedSize(true)
            itemAnimator = SlideInRightAnimator(OvershootInterpolator(1.0f))
            layoutManager = LinearLayoutManager(context)
            addOnScrollListener(
                object: RecyclerView.OnScrollListener() {
                    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                        if (dy > 0) {
                            fab.hide()
                        } else if (dy < 0) {
                            fab.show()
                        }
                    }
                }
            )
        }
        ItemTouchHelper(SwipeCallback(adapter)).attachToRecyclerView(recycler_todo)
        recycler_todo.adapter = adapter

        fab.setOnClickListener {
            val todoNote = TodoNote(
                ParcelUuid(UUID.randomUUID()), "", "", Priority.LOW, false, null,
                ColorGenerator.MATERIAL.randomColor)
            val intent = Intent(context, AddTodoActivity::class.java).apply {
                putExtra(EXTRA_TODO_NOTE, todoNote)
            }
            startActivityForResult(intent, REQ_ID_TODO)
        }

        fab.setOnLongClickListener {
            initSpeechRecognition()
            true
        }
    }

    private fun retrieveTodoNotes() {
        viewModel.todoNoteLiveData.observe(this) { adapter.todoNotes = it.toMutableList() }
    }

    private fun setAlarms() {
        for (todoNote in todoNotes) {
            if (todoNote.hasReminder && todoNote.date != null) {
                if (todoNote.date!!.before(Date())) {
                    todoNote.date = null
                    continue
                }
                val intent = Intent(context!!, TodoNotificationService::class.java).apply {
                    putExtra(TODO_TITLE, todoNote.title)
                    putExtra(TODO_ID, todoNote.id)
                }
                createAlarm(context!!, intent, todoNote.id.hashCode(), todoNote.date!!.time)
            }
        }
    }

    private fun initSpeechRecognition() {
        Timber.d("Starting audio recording!")

        val speechIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }

        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object: RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {}
                    override fun onRmsChanged(rmsdB: Float) {}
                    override fun onBufferReceived(buffer: ByteArray?) {}
                    override fun onPartialResults(partialResults: Bundle?) { }
                    override fun onEvent(eventType: Int, params: Bundle?) {}
                    override fun onBeginningOfSpeech() {}
                    override fun onEndOfSpeech() {}
                    override fun onError(error: Int) {}

                    override fun onResults(results: Bundle?) {
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        processResult(matches?.get(0))
                    }
                })

                startListening(speechIntent)
            }
        }
    }

    private fun processResult(match: String?) {
        Timber.d(match)
        if (match != null) {
            if (!match.startsWith("title", true) || !match.contains("priority", true) ||
                match.length < match.indexOf("priority", ignoreCase = true) + 12) {
                initTTS("Say something like \"Title is 'HCI Assignment', description is " +
                        "'Add voice commands', priority is 'medium'\"",
                    UTTERANCE_ID_SPEECH_ERROR
                )
                return
            }

            if (match.contains("description", true)) {
                title = match.substring(9, match.indexOf("description", ignoreCase = true))
                description = match.substring(match.indexOf("description", ignoreCase = true) + 15,
                    match.indexOf("priority", ignoreCase = true))
            } else {
                title = match.substring(9, match.indexOf("priority", ignoreCase = true))
            }
            priority = Priority.valueOf(
                match.substring(
                    match.indexOf("priority", ignoreCase = true) + 12,
                    match.length
                ).toUpperCase(DEFAULT_LOCALE)
            )

            val calendar = Calendar.getInstance()
            calendar.time = Date()

            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            DatePickerDialog.newInstance(this, year, month, day).apply {
                isThemeDark = true
                show((parentContext as AppCompatActivity).supportFragmentManager, TAG_DATE_FRAGMENT)
            }
        }
    }

    private fun initTTS(msg: String, utteranceId: String) {
        if (tts == null) {
            tts = TextToSpeech(context, TextToSpeech.OnInitListener {
                if (it == TextToSpeech.SUCCESS) {
                    val result = tts?.setLanguage(Locale.US)
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        context?.toast("This Language is not supported")
                    } else {
                        tts.speak(msg, utteranceId)
                    }
                } else {
                    Timber.e("Initialization Failed!")
                }
            })
        } else {
            tts.speak(msg, utteranceId)
        }
    }

    companion object {
        @JvmStatic fun newInstance() = PendingFragment()
    }
}
