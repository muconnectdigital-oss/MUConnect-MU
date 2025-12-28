package com.rsservice.muconnect.ui.common

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.rsservice.muconnect.R
import com.rsservice.muconnect.adapter.GroupsAdapter
import com.rsservice.muconnect.model.Group

class GroupsActivity : AppCompatActivity() {

    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var adapter: GroupsAdapter
    private val allGroups = mutableListOf<Group>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_groups)

        // ✅ FIND VIEWS PROPERLY
        val rvGroups = findViewById<RecyclerView>(R.id.rvGroups)
        val etSearch = findViewById<EditText>(R.id.etSearch)

        rvGroups.layoutManager = LinearLayoutManager(this)

        adapter = GroupsAdapter(mutableListOf()) { group ->
            openChat(group.id)
        }
        rvGroups.adapter = adapter

        loadGroups()

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterGroups(s.toString())
            }
        })
    }

    private fun loadGroups() {
        firestore.collection("groups")
            .orderBy("lastMessageTime")
            .addSnapshotListener { snapshot, _ ->
                val list = snapshot?.documents?.mapNotNull {
                    it.toObject(Group::class.java)?.apply { id = it.id }
                } ?: emptyList()

                allGroups.clear()
                allGroups.addAll(list)
                adapter.update(list)
            }
    }

    private fun filterGroups(query: String) {
        val filtered = allGroups.filter {
            it.name.contains(query, ignoreCase = true)
        }
        adapter.update(filtered)
    }

    private fun openChat(groupId: String) {
        val intent = Intent(this, GroupChatActivity::class.java)
        intent.putExtra("groupId", groupId)
        startActivity(intent)
    }
}
