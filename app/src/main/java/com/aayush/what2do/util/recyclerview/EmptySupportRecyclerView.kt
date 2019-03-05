package com.aayush.what2do.util.recyclerview

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class EmptySupportRecyclerView: RecyclerView {
    private lateinit var emptyView: View

    private val observer = object: AdapterDataObserver() {
        override fun onChanged() {
            showEmptyView()
        }

        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            super.onItemRangeInserted(positionStart, itemCount)
            showEmptyView()
        }

        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
            super.onItemRangeRemoved(positionStart, itemCount)
            showEmptyView()
        }
    }

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet): super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int): super(context, attrs, defStyle)

    override fun setAdapter(adapter: Adapter<*>?) {
        super.setAdapter(adapter)

        if (adapter != null) {
            adapter.registerAdapterDataObserver(observer)
            observer.onChanged()
        }
    }

    private fun showEmptyView() {
        val adapter = adapter

        if (adapter != null) {
            if (adapter.itemCount == 0) {
                emptyView.visibility = View.VISIBLE
                visibility = View.GONE
            }
            else {
                emptyView.visibility = View.GONE
                visibility = View.VISIBLE
            }
        }
    }

    fun setEmptyView(view: View) {
        emptyView = view
    }
}