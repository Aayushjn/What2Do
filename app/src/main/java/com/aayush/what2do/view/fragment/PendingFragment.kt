package com.aayush.what2do.view.fragment

import android.app.Activity.RESULT_CANCELED
import android.content.Intent
import android.os.Bundle
import android.os.ParcelUuid
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.aayush.what2do.util.recyclerview.callback.SwipeToDeleteCallback
import com.aayush.what2do.util.service.TodoNotificationService
import com.aayush.what2do.view.activity.AddTodoActivity
import com.amulyakhare.textdrawable.util.ColorGenerator
import jp.wasabeef.recyclerview.animators.FadeInDownAnimator
import kotlinx.android.synthetic.main.fragment_pending.*
import java.util.*


class PendingFragment : Fragment() {
    private lateinit var todoNotes: MutableList<TodoNote>

    private lateinit var recyclerView: EmptySupportRecyclerView
    private lateinit var adapter : TodoNotesAdapter

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
        if (resultCode != RESULT_CANCELED && requestCode == REQUEST_ID_TODO) {
            val todoNote : TodoNote? = data?.getParcelableExtra(EXTRA_TODO_NOTE)
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
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onResume() {
        super.onResume()
        adapter.setTodoNotes(todoNotes.sortedDescending())
        setAlarms()
    }

    private fun setupViews() {
        recyclerView = recycler_todo

        recyclerView.setEmptyView(img_empty_state)
        recyclerView.setHasFixedSize(true)
        recyclerView.itemAnimator = FadeInDownAnimator()
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
        val itemTouchHelper = ItemTouchHelper(SwipeToDeleteCallback(adapter))
        itemTouchHelper.attachToRecyclerView(recyclerView)

        recyclerView.adapter = adapter

        fab.setOnClickListener {
            val todoNote = TodoNote(
                ParcelUuid(UUID.randomUUID()), "", "", Priority.LOW, false, null,
                ColorGenerator.MATERIAL.randomColor)
            val intent = Intent(context, AddTodoActivity::class.java)
            intent.putExtra(EXTRA_TODO_NOTE, todoNote)
            startActivityForResult(intent, REQUEST_ID_TODO)
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
        return Pair(false, -1)
    }

    companion object {
        @JvmStatic
        fun newInstance() = PendingFragment()
    }
}
