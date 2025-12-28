package com.rsservice.muconnect.ui.sheet

import android.app.Dialog
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.rsservice.muconnect.R
import com.rsservice.muconnect.model.User

class MemberDetailsSheet(
    private val user: User
) : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext(), R.style.BottomSheetTheme)
        val v = layoutInflater.inflate(R.layout.sheet_member_details, null)

        v.findViewById<TextView>(R.id.tvName).text = user.name
        v.findViewById<TextView>(R.id.tvRole).text = user.role
        v.findViewById<TextView>(R.id.tvExtra).text = user.extra ?: ""

        dialog.setContentView(v)
        return dialog
    }
}
