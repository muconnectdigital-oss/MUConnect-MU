package com.rsservice.muconnect.model

import com.google.firebase.Timestamp

data class Message(
    var id: String = "",
    var senderId: String = "",
    var senderName: String = "",
    var type: String = "text",                 // text, image, video, file
    var text: String = "",
    var fileUrl: String = "",
    var fileName: String = "",
    var createdAt: Timestamp? = null,
    var seenBy: MutableList<String> = mutableListOf(),

    // --- reply fields ---
    var replyTo: String? = null,
    var replyToText: String? = null,

    // --- reaction emoji ---
    var reaction: String? = null
)
