package com.rsservice.muconnect.ui.admin

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.rsservice.muconnect.R
import com.rsservice.muconnect.adapter.StudentsAdapter
import com.rsservice.muconnect.model.User
import com.rsservice.muconnect.ui.common.GroupChatActivity

class CreateGroupActivity : AppCompatActivity() {

    private val firestore = FirebaseFirestore.getInstance()
    private val selectedMembers = mutableSetOf<String>()
    private lateinit var adapter: StudentsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_group)

        val etGroupName = findViewById<EditText>(R.id.etGroupName)
        val rvUsers = findViewById<RecyclerView>(R.id.rvUsers)
        val btnCreate = findViewById<Button>(R.id.btnCreateGroup)

        rvUsers.layoutManager = LinearLayoutManager(this)

        adapter = StudentsAdapter(mutableListOf()) { user, isChecked ->
            if (isChecked) {
                selectedMembers.add(user.id)
            } else {
                selectedMembers.remove(user.id)
            }
        }

        rvUsers.adapter = adapter

        checkPermission()
        loadUsers()

        btnCreate.setOnClickListener {
            val groupName = etGroupName.text.toString().trim()

            if (groupName.isEmpty()) {
                etGroupName.error = "Group name required"
                return@setOnClickListener
            }

            if (selectedMembers.isEmpty()) {
                Toast.makeText(this, "Select at least one member", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            createGroup(groupName)
        }
    }

    // ---------------- ROLE CHECK ----------------
    private fun checkPermission() {
        val currentUserRole = intent.getStringExtra("role") ?: ""

        if (currentUserRole != "teacher" && currentUserRole != "admin") {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    // ---------------- LOAD USERS ----------------
    private fun loadUsers() {
        firestore.collection("users")
            .get()
            .addOnSuccessListener { result ->
                val users = result.documents.mapNotNull { doc ->
                    doc.toObject(User::class.java)?.apply {
                        id = doc.id
                    }
                }
                adapter.update(users)
            }
    }

    // ---------------- CREATE GROUP ----------------
    private fun createGroup(groupName: String) {

        val currentUserId = intent.getStringExtra("userId") ?: return

        selectedMembers.add(currentUserId)

        val groupData = hashMapOf(
            "name" to groupName,
            "createdBy" to currentUserId,
            "members" to selectedMembers.toList(),
            "createdAt" to FieldValue.serverTimestamp(),
            "lastMessage" to "",
            "lastMessageTime" to null,
            "mutedBy" to emptyList<String>()
        )

        firestore.collection("groups")
            .add(groupData)
            .addOnSuccessListener { doc ->
                openGroupChat(doc.id)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to create group", Toast.LENGTH_SHORT).show()
            }
    }

    // ---------------- OPEN CHAT ----------------
    private fun openGroupChat(groupId: String) {
        val intent = Intent(this, GroupChatActivity::class.java)
        intent.putExtra("groupId", groupId)
        startActivity(intent)
        finish()
    }
}
