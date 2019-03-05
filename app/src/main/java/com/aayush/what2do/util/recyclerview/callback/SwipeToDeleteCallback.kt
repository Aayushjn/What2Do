package com.aayush.what2do.util.recyclerview.callback

import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.aayush.what2do.R
import com.aayush.what2do.util.recyclerview.adapter.TodoNotesAdapter

class SwipeToDeleteCallback(private val adapter: TodoNotesAdapter) :
    ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
    private val clearPaint: Paint = Paint()

    private var icon: Drawable? = ContextCompat.getDrawable(adapter.context, R.drawable.ic_delete)
    private var background: ColorDrawable = ColorDrawable(Color.RED)

    init {
        clearPaint.xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }

    override fun onChildDraw(
        c: Canvas, recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder, dX: Float, dY: Float, actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        val itemView = viewHolder.itemView
        val backgroundCornerOffset = 20

        val iconMargin = (itemView.height - icon!!.intrinsicHeight) / 2
        val iconTop = itemView.top + (itemView.height - icon!!.intrinsicHeight) / 2
        val iconBottom = iconTop + icon!!.intrinsicHeight

        val isCancelled = dX == 0F && !isCurrentlyActive

        if (isCancelled) {
            clearCanvas(c, itemView.right + dX, itemView.top.toFloat(), itemView.right.toFloat(),
                itemView.bottom.toFloat())
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            return
        }

        when {
            dX > 0 -> { // Swiping to the right
                background = ColorDrawable(Color.BLUE)
                icon = ContextCompat.getDrawable(adapter.context, R.drawable.ic_voice)
                val iconLeft = itemView.left + iconMargin
                val iconRight = iconLeft + icon!!.intrinsicWidth
                icon!!.setBounds(iconLeft, iconTop, iconRight, iconBottom)

                background.setBounds(
                    itemView.left,
                    itemView.top,
                    itemView.left + dX.toInt() + backgroundCornerOffset,
                    itemView.bottom
                )
            }
            dX < 0 -> { // Swiping to the left
                background = ColorDrawable(Color.RED)
                icon = ContextCompat.getDrawable(adapter.context, R.drawable.ic_delete)
                val iconRight = itemView.right - iconMargin
                val iconLeft = iconRight - icon!!.intrinsicWidth
                icon!!.setBounds(iconLeft, iconTop, iconRight, iconBottom)

                background.setBounds(
                    itemView.right + dX.toInt() - backgroundCornerOffset,
                    itemView.top,
                    itemView.right,
                    itemView.bottom
                )
            }
            else -> background.setBounds(0, 0, 0, 0)
        }

        background.draw(c)
        icon!!.draw(c)
    }

    override fun onMove(
        recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val position = viewHolder.adapterPosition
        if (direction == ItemTouchHelper.LEFT) {
            adapter.deleteItem(position)
        }
        else {
            adapter.readItem(position)
            adapter.notifyItemChanged(position)
        }
    }

    private fun clearCanvas(c: Canvas, left: Float, top: Float, right: Float, bottom: Float) {
        c.drawRect(left, top, right, bottom, clearPaint)
    }
}
