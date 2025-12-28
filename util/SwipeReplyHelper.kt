package com.rsservice.muconnect.util

import android.graphics.Canvas
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

class SwipeReplyHelper(
    private val onReply: (Int) -> Unit
) : ItemTouchHelper.SimpleCallback(
    0,
    ItemTouchHelper.RIGHT or ItemTouchHelper.LEFT
) {
    override fun onMove(rv: RecyclerView, vh: RecyclerView.ViewHolder, t: RecyclerView.ViewHolder)
            = false

    override fun onSwiped(vh: RecyclerView.ViewHolder, dir: Int) {
        onReply(vh.adapterPosition)
    }

    override fun onChildDraw(
        c: Canvas, rv: RecyclerView, vh: RecyclerView.ViewHolder,
        dX: Float, dY: Float, actionState: Int, active: Boolean
    ) {
        super.onChildDraw(c, rv, vh, dX / 4, dY, actionState, active)
    }
}
