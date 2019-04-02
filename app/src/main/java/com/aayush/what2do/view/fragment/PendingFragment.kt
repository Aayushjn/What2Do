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
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aayush.what2do.App
import com.aayush.what2do.R
import com.aayush.what2do.model.Priority
import com.aayush.what2do.model.TodoNote
import com.aayush.what2do.util.*
import com.aayush.what2do.util.recyclerview.EmptySupportRecyclerView
import com.aayush.what2do.util.recyclerview.adapter.TodoNotesAdapter
import com.aayush.what2do.util.recyclerview.callback.SwipeCallback
import com.aayush.what2do.util.service.TodoNotificationService
import com.aayush.what2do.view.activity.AddTodoActivity
import com.amulyakhare.textdrawable.util.ColorGenerator
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog
import jp.wasabeef.recyclerview.animators.SlideInRightAnimator
import kotlinx.android.synthetic.main.fragment_pending.*
import timber.log.Timber
import java.util.*


class PendingFragment: Fragment(), DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {
    private lateinit var todoNotes: MutableList<TodoNote>

    private lateinit var recyclerView: EmptySupportRecyclerView
    private lateinit var adapter: TodoNotesAdapter

    private lateinit var speechRecognizer: SpeechRecognizer
    private var tts: TextToSpeech? = null

    private lateinit var title: String
    private var description = ""
    private lateinit var priority: Priority
    private var reminder: Date? = null
    private val color = ColorGenerator.MATERIAL.randomColor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        todoNotes = getAllTodoNotes(App.getAppDatabase(context!!).todoNoteDao()).sortedDescending().toMutableList()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_pending, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = TodoNotesAdapter(activity!!)
        adapter.setTodoNotes(todoNotes)
        setupViews()
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
                    val intent = Intent(context!!, TodoNotificationService::class.java)
                    intent.putExtra(TODO_ID, todoNote.id)
                    intent.putExtra(TODO_TITLE, todoNote.title)
                    createAlarm(context!!, intent, todoNote.id.hashCode(), todoNote.date!!.time)
                }
            }

            val res = contains(todoNote)
            if (res.first) {
                todoNotes.removeAt(res.second)
                todoNotes.add(res.second, todoNote)
            }
            else {
                todoNotes.add(todoNote)
            }
            adapter.notifyItemInserted(todoNotes.lastIndex)
            insertTodoNote(App.getAppDatabase(context!!).todoNoteDao(), todoNote)
            Toast.makeText(context, "Todo note added!", Toast.LENGTH_SHORT).show()
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onResume() {
        super.onResume()
        adapter.setTodoNotes(todoNotes.sortedDescending())
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

        val timePickerDialog = TimePickerDialog.newInstance(
            this, hour, minute, DateFormat.is24HourFormat(context)
        )
        timePickerDialog.isThemeDark = true
        timePickerDialog.show(activity!!.supportFragmentManager, TAG_TIME_FRAGMENT)
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
        insertTodoNote(App.getAppDatabase(context!!).todoNoteDao(), todoNote)

        initTTS("Todo note added", UTTERANCE_ID_ADD_TODO)
    }

    private fun setupViews() {
        recyclerView = recycler_todo

        recyclerView.setEmptyView(img_empty_state)
        recyclerView.setHasFixedSize(true)
        recyclerView.itemAnimator = SlideInRightAnimator(OvershootInterpolator(1.0f))
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.addOnScrollListener(
            object: RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    if (dy > 0) {
                        fab.hide()
                    }
                    else if (dy < 0) {
                        fab.show()
                    }
                }
            }
        )
        val itemTouchHelper = ItemTouchHelper(SwipeCallback(adapter))
        itemTouchHelper.attachToRecyclerView(recyclerView)

        recyclerView.adapter = adapter

        fab.setOnClickListener {
            val todoNote = TodoNote(
                ParcelUuid(UUID.randomUUID()), "", "", Priority.LOW, false, null,
                ColorGenerator.MATERIAL.randomColor)
            val intent = Intent(context, AddTodoActivity::class.java)
            intent.putExtra(EXTRA_TODO_NOTE, todoNote)
            startActivityForResult(intent, REQ_ID_TODO)
        }

        fab.setOnLongClickListener {
            initSpeechRecognition()
            true
        }
    }

    private fun setAlarms() {
        for (todoNote in todoNotes) {
            if (todoNote.hasReminder && todoNote.date != null) {
                if (todoNote.date!!.before(Date())) {
                    todoNote.date = null
                    continue
                }
                val intent = Intent(context!!, TodoNotificationService::class.java)
                intent.putExtra(TODO_TITLE, todoNote.title)
                intent.putExtra(TODO_ID, todoNote.id)
                createAlarm(context!!, intent, todoNote.id.hashCode(), todoNote.date!!.time)
            }
        }
    }

    private fun contains(todoNote: TodoNote): Pair<Boolean, Int> {
        for (note in todoNotes) {
            if (note.id == todoNote.id) {
                return Pair(true, todoNotes.indexOf(note))
            }
        }
        return false to -1
    }

    private fun initSpeechRecognition() {
        Timber.d("Starting audio recording!")

        val speechIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        speechIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)

        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
            speechRecognizer.setRecognitionListener(object: RecognitionListener {
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

            speechRecognizer.startListening(speechIntent)
        }
    }

    private fun processResult(match: String?) {
        Timber.d(match)
        if (match != null) {
            if (!match.startsWith("title", true) || !match.contains("priority", true) ||
                    match.length < match.indexOf("priority", ignoreCase = true) + 12) {
                initTTS("Say something like \"Title is 'HCI Assignment', description is " +
                        "'Add voice commands', priority is 'medium'\"", UTTERANCE_ID_SPEECH_ERROR)
                return
            }

            if (match.contains("description", true)) {
                title = match.substring(9, match.indexOf("description", ignoreCase = true))
                description = match.substring(match.indexOf("description", ignoreCase = true) + 15,
                    match.indexOf("priority", ignoreCase = true))
            }
            else {
                title = match.substring(9, match.indexOf("priority", ignoreCase = true))
            }
            priority = Priority.valueOf(
                match.substring(
                    match.indexOf("priority", ignoreCase = true) + 12,
                    match.length
                ).toUpperCase()
            )

            val calendar = Calendar.getInstance()
            calendar.time = Date()

            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog.newInstance(this, year, month, day)
            datePickerDialog.isThemeDark = true
            datePickerDialog.show(activity!!.supportFragmentManager, TAG_DATE_FRAGMENT)
        }
    }

    private fun initTTS(msg: String, utteranceId: String) {
        if (tts == null) {
            tts = TextToSpeech(context, TextToSpeech.OnInitListener {
                if (it == TextToSpeech.SUCCESS) {
                    val result = tts?.setLanguage(Locale.US)
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Toast.makeText(context, "This Language is not supported", Toast.LENGTH_SHORT).show()
                    }
                    else {
                        speak(tts, msg, utteranceId)
                    }
                }
                else {
                    Timber.e("Initialization Failed!")
                }
            })
        }
        else {
            speak(tts, msg, utteranceId)
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = PendingFragment()
    }
}
