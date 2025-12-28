package com.rsservice.muconnect.dialogs

import android.app.AlertDialog
import android.content.Context
import android.widget.EditText

class EditMessageDialog(
    val context: Context,
    val old: String,
    val onDone: (String) -> Unit
) {
    fun show() {
        val et = EditText(context); et.setText(old)

        AlertDialog.Builder(context)
            .setTitle("Edit Message")
            .setView(et)
            .setPositiveButton("Save") { _, _ -> onDone(et.text.toString()) }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
