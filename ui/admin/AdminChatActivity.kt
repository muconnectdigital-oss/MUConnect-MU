package com.rsservice.muconnect.ui.admin

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.rsservice.muconnect.adapter.ChatAdapter
import com.rsservice.muconnect.data.FirebaseDBManager
import com.rsservice.muconnect.databinding.ActivityAdminChatBinding
import com.rsservice.muconnect.model.Message

class AdminChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminChatBinding
    private val adapter = ChatAdapter()
    private val db = FirebaseDBManager()

    private val chatId by lazy {
        intent.getStringExtra("chatId")!!
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.rvChat.layoutManager = LinearLayoutManager(this)
        binding.rvChat.adapter = adapter

        db.listenMessages(chatId) {
            adapter.submitList(it)
        }

        binding.btnSend.setOnClickListener {
            sendMessage()
        }
    }

    private fun sendMessage() {
        val text = binding.etMessage.text.toString().trim()
        if (text.isEmpty()) return

        val msg = Message(
            senderId = FirebaseAuth.getInstance().uid!!,
            text = text
        )

        db.sendMessage(chatId, msg)
        binding.etMessage.setText("")
    }
}
