package com.rsservice.muconnect.ui.common

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.rsservice.muconnect.R
import com.rsservice.muconnect.adapter.ChatAdapter
import com.rsservice.muconnect.model.Message

class GroupChatActivity : AppCompatActivity() {

    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var groupId: String

    private lateinit var rvMessages: androidx.recyclerview.widget.RecyclerView
    private lateinit var etMessage: EditText
    private lateinit var btnSend: ImageButton
    private lateinit var btnAttach: ImageButton
    private lateinit var progressUpload: ProgressBar

    private val messages = mutableListOf<Message>()
    private lateinit var adapter: ChatAdapter

    private val myUserId = "R"
    private val PICK_FILE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_chat)

        groupId = intent.getStringExtra("groupId") ?: return

        rvMessages = findViewById(R.id.rvMessages)
        etMessage = findViewById(R.id.etMessage)
        btnSend = findViewById(R.id.btnSend)
        btnAttach = findViewById(R.id.btnAttach)
        progressUpload = findViewById(R.id.progressUpload)

        rvMessages.layoutManager = LinearLayoutManager(this)
        adapter = ChatAdapter(messages, myUserId)
        rvMessages.adapter = adapter

        listenMessages()
        sendText()

        btnAttach.setOnClickListener { openPicker() }
    }

    private fun sendText() {
        btnSend.setOnClickListener {
            val text = etMessage.text.toString().trim()
            if (text.isEmpty()) return@setOnClickListener

            sendMessage(
                mapOf(
                    "senderId" to myUserId,
                    "senderName" to myUserId,
                    "type" to "text",
                    "text" to text,
                    "seenBy" to listOf(myUserId),
                    "createdAt" to FieldValue.serverTimestamp()
                ),
                text
            )

            etMessage.setText("")
        }
    }

    private fun openPicker() {
        val i = Intent(Intent.ACTION_GET_CONTENT)
        i.type = "*/*"
        startActivityForResult(i, PICK_FILE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_FILE && resultCode == RESULT_OK) {
            data?.data?.let { uploadFile(it) }
        }
    }

    private fun uploadFile(uri: Uri) {
        progressUpload.visibility = ProgressBar.VISIBLE

        val type = contentResolver.getType(uri)?.let {
            when {
                it.startsWith("image") -> "image"
                it.startsWith("video") -> "video"
                else -> "file"
            }
        } ?: "file"

        val ref = FirebaseStorage.getInstance()
            .reference.child("group_media/$groupId/${System.currentTimeMillis()}")

        ref.putFile(uri)
            .addOnProgressListener {
                val percent = (100 * it.bytesTransferred / it.totalByteCount).toInt()
                progressUpload.progress = percent
            }
            .addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { url ->
                    sendMessage(
                        mapOf(
                            "senderId" to myUserId,
                            "senderName" to myUserId,
                            "type" to type,
                            "fileUrl" to url.toString(),
                            "seenBy" to listOf(myUserId),
                            "createdAt" to FieldValue.serverTimestamp()
                        ),
                        if (type == "image") "📷 Image"
                        else if (type == "video") "🎬 Video"
                        else "📎 File"
                    )
                }
            }
            .addOnCompleteListener {
                progressUpload.visibility = ProgressBar.GONE
            }
    }

    private fun sendMessage(data: Map<String, Any>, lastMessage: String) {
        firestore.collection("groups")
            .document(groupId)
            .collection("messages")
            .add(data)

        firestore.collection("groups")
            .document(groupId)
            .update(
                "lastMessage", lastMessage,
                "lastMessageTime", FieldValue.serverTimestamp()
            )
    }

    private fun listenMessages() {
        firestore.collection("groups")
            .document(groupId)
            .collection("messages")
            .orderBy("createdAt")
            .addSnapshotListener { snap, _ ->
                val list = snap?.documents?.mapNotNull {
                    it.toObject(Message::class.java)?.apply { id = it.id }
                } ?: emptyList()

                adapter.update(list)

                list.forEach {
                    if (!it.seenBy.contains(myUserId)) {
                        firestore.collection("groups")
                            .document(groupId)
                            .collection("messages")
                            .document(it.id)
                            .update("seenBy", FieldValue.arrayUnion(myUserId))
                    }
                }
            }
    }
}
