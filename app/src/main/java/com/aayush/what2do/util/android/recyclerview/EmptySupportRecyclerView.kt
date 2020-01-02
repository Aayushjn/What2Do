package com.aayush.what2do.util.android.recyclerview

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class EmptySupportRecyclerView: RecyclerView {
    private lateinit var emptyView: View

    private val observer: AdapterDataObserver = object: AdapterDataObserver() {
        override fun onChanged() = showEmptyView()
        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) = showEmptyView()
        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) = showEmptyView()
    }

    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet): super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyle: Int): super(context, attrs, defStyle)

    override fun setAdapter(adapter: Adapter<*>?) {
        val oldAdapter: Adapter<ViewHolder>? = this.adapter
        super.setAdapter(adapter)

        oldAdapter?.unregisterAdapterDataObserver(observer)

        adapter?.let {
            it.registerAdapterDataObserver(observer)
            observer.onChanged()
        }
    }

    fun setEmptyView(view: View) {
        emptyView = view
    }

    private fun showEmptyView() {
        adapter?.let {
            if (it.itemCount == 0) {
                emptyView.visibility = View.VISIBLE
                visibility = View.GONE
            } else {
                emptyView.visibility = View.GONE
                visibility = View.VISIBLE
            }
        }
    }
}