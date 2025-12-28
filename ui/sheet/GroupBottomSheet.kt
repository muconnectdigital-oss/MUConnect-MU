package com.rsservice.muconnect.ui.sheet

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.rsservice.muconnect.R
import androidx.recyclerview.widget.RecyclerView
import com.rsservice.muconnect.model.User
import com.rsservice.muconnect.adapter.MemberAdapter

class GroupBottomSheet(
    private val groupId: String,
    private val isAdmin: Boolean
) : DialogFragment() {

    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var rv: RecyclerView
    private lateinit var tvGroupName: TextView
    private lateinit var btnEdit: ImageView
    private lateinit var btnDelete: ImageView

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext(), R.style.BottomSheetTheme)
        val view = layoutInflater.inflate(R.layout.sheet_group, null)

        rv = view.findViewById(R.id.rvMembers)
        tvGroupName = view.findViewById(R.id.tvGroupName)
        btnEdit = view.findViewById(R.id.btnEdit)
        btnDelete = view.findViewById(R.id.btnDelete)

        rv.layoutManager = LinearLayoutManager(requireContext())

        loadGroup()
        loadMembers()

        btnEdit.visibility = if (isAdmin) View.VISIBLE else View.GONE
        btnDelete.visibility = if (isAdmin) View.VISIBLE else View.GONE

        btnEdit.setOnClickListener { openEditDialog() }
        btnDelete.setOnClickListener { deleteGroup() }

        dialog.setContentView(view)
        return dialog
    }

    private fun loadGroup() {
        firestore.collection("groups").document(groupId)
            .get().addOnSuccessListener {
                tvGroupName.text = it.getString("name")
            }
    }

    private fun loadMembers() {
        firestore.collection("groups").document(groupId)
            .get().addOnSuccessListener { snap ->
                val list = snap.get("members") as List<*>
                firestore.collection("users")
                    .whereIn("id", list)
                    .get().addOnSuccessListener { res ->
                        val users = res.documents.mapNotNull { it.toObject(User::class.java) }
                        rv.adapter = MemberAdapter(users) { u ->
                            MemberDetailsSheet(u).show(parentFragmentManager, "DETAIL")
                        }
                    }
            }
    }

    private fun deleteGroup() {
        firestore.collection("groups").document(groupId)
            .delete().addOnSuccessListener { dismiss() }
    }

    private fun openEditDialog() {
        val et = EditText(requireContext())
        et.setText(tvGroupName.text.toString())

        AlertDialog.Builder(requireContext())
            .setTitle("Rename Group")
            .setView(et)
            .setPositiveButton("Save") { _, _ ->
                firestore.collection("groups").document(groupId)
                    .update("name", et.text.toString())
                tvGroupName.text = et.text.toString()
            }.setNegativeButton("Cancel", null)
            .show()
    }
}
