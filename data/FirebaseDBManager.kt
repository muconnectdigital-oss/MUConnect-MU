package com.rsservice.muconnect.data

import com.google.firebase.firestore.FirebaseFirestore
import com.rsservice.muconnect.model.Message

class FirebaseDBManager {

    private val db = FirebaseFirestore.getInstance()

    fun sendMessage(chatId: String, message: Message) {
        db.collection("chats")
            .document(chatId)
            .collection("messages")
            .add(message)
    }

    fun listenMessages(
        chatId: String,
        onUpdate: (List<Message>) -> Unit
    ) {
        db.collection("chats")
            .document(chatId)
            .collection("messages")
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, _ ->
                val messages =
                    snapshot?.toObjects(Message::class.java) ?: emptyList()
                onUpdate(messages)
            }
    }
}
