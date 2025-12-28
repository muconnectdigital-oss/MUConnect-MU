package com.rsservice.muconnect.ui.admin

import android.content.Intent
import android.os.Bundle
import android.view.View
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

    private lateinit var adapter: StudentsAdapter
    private val selectedMembers = mutableSetOf<String>()

    // UI
    private lateinit var tvSelectedCount: TextView
    private lateinit var rbStudents: RadioButton
    private lateinit var rbTeachers: RadioButton
    private lateinit var spinnerClass: Spinner
    private lateinit var rvUsers: RecyclerView

    private val classList = mutableListOf("All Classes")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_group)

        // ---------- UI refs ----------
        val etGroupName = findViewById<EditText>(R.id.etGroupName)
        val etDescription = findViewById<EditText>(R.id.etDescription)
        val btnCreate = findViewById<Button>(R.id.btnCreateGroup)

        tvSelectedCount = findViewById(R.id.tvSelectedCount)
        rbStudents = findViewById(R.id.rbStudents)
        rbTeachers = findViewById(R.id.rbTeachers)
        spinnerClass = findViewById(R.id.spinnerClass)
        rvUsers = findViewById(R.id.rvUsers)

        // ---------- RecyclerView ----------
        rvUsers.layoutManager = LinearLayoutManager(this)
        adapter = StudentsAdapter(mutableListOf()) { user, isChecked ->
            if (isChecked) selectedMembers.add(user.id)
            else selectedMembers.remove(user.id)

            updateSelectedCount()
        }
        rvUsers.adapter = adapter

        // ---------- Load class list ----------
        loadClasses()

        // ---------- Spinner ----------
        spinnerClass.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    if (rbStudents.isChecked) {
                        loadStudents(classList[position])
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }

        // ---------- Tabs ----------
        rbStudents.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                spinnerClass.visibility = View.VISIBLE
                loadStudents(spinnerClass.selectedItem.toString())
            }
        }

        rbTeachers.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                spinnerClass.visibility = View.GONE
                loadTeachers()
            }
        }

        rbStudents.isChecked = true

        // ---------- Create Group ----------
        btnCreate.setOnClickListener {
            val name = etGroupName.text.toString().trim()
            val desc = etDescription.text.toString().trim()

            if (name.isEmpty()) {
                etGroupName.error = "Group name required"
                return@setOnClickListener
            }

            if (selectedMembers.isEmpty()) {
                Toast.makeText(this, "Select at least one member", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            createGroup(name, desc)
        }
    }

    // ---------------- LOAD CLASSES ----------------
    private fun loadClasses() {
        firestore.collection("classes")
            .get()
            .addOnSuccessListener { result ->
                result.documents.forEach { classList.add(it.id) }

                spinnerClass.adapter = ArrayAdapter(
                    this,
                    android.R.layout.simple_spinner_dropdown_item,
                    classList
                )
            }
    }

    // ---------------- LOAD STUDENTS ----------------
    private fun loadStudents(className: String) {
        firestore.collection("students")
            .get()
            .addOnSuccessListener { result ->
                val users = result.documents.mapNotNull { doc ->
                    val fullName = doc.getString("fullName") ?: return@mapNotNull null
                    val grNo = doc.getString("grNo") ?: ""
                    val cls = doc.getString("class") ?: ""
                    val batch = doc.getString("batch") ?: ""

                    if (className != "All Classes" && cls != className) return@mapNotNull null

                    User(
                        id = doc.id,
                        name = fullName,
                        role = "student",
                        studentClass = cls,
                        batch = batch,
                        extra = "GR: $grNo • Class: $cls • Batch: $batch"
                    )
                }
                adapter.update(users)
            }
    }

    // ---------------- LOAD TEACHERS ----------------
    private fun loadTeachers() {
        firestore.collection("users")
            .whereEqualTo("role", "teacher")
            .get()
            .addOnSuccessListener { result ->
                val users = result.documents.mapNotNull { doc ->
                    User(
                        id = doc.id,
                        name = doc.getString("name") ?: "",
                        role = "teacher"
                    )
                }
                adapter.update(users)
            }
    }

    // ---------------- CREATE GROUP ----------------
    private fun createGroup(name: String, description: String) {
        val data = hashMapOf(
            "name" to name,
            "description" to description,
            "members" to selectedMembers.toList(),
            "createdAt" to FieldValue.serverTimestamp(),
            "lastMessage" to "",
            "lastMessageTime" to FieldValue.serverTimestamp(),
            "mutedBy" to emptyList<String>()
        )

        firestore.collection("groups")
            .add(data)
            .addOnSuccessListener { openGroupChat(it.id) }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to create group", Toast.LENGTH_SHORT).show()
            }
    }

    // ---------------- HELPERS ----------------
    private fun updateSelectedCount() {
        tvSelectedCount.text =
            "Select Members (${selectedMembers.size} selected)"
    }

    private fun openGroupChat(groupId: String) {
        val intent = Intent(this, GroupChatActivity::class.java)
        intent.putExtra("groupId", groupId)
        startActivity(intent)
        finish()
    }
}
