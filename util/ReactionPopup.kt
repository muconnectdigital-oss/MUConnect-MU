package com.rsservice.muconnect.util

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.TextView
import com.rsservice.muconnect.R

class ReactionPopup(
    private val context: Context,
    private val onReact: (String) -> Unit
) {
    private var popup: PopupWindow? = null

    fun show(anchor: View) {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.popup_reaction, null)

        popup = PopupWindow(
            view,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )

        val ids = listOf(
            R.id.r0, R.id.r1, R.id.r2, R.id.r3, R.id.r4
        )
        val emojis = listOf("👍", "❤️", "😂", "😡", "😢")

        ids.forEachIndexed { index, id ->
            val tv = view.findViewById<TextView>(id)
            tv.text = emojis[index]
            tv.setOnClickListener {
                onReact(emojis[index])
                popup?.dismiss()
            }
        }

        popup?.elevation = 8f
        popup?.showAsDropDown(anchor, 0, -anchor.height - 20)
    }
}
