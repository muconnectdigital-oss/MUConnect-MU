package com.rsservice.muconnect.model

import com.google.firebase.Timestamp

data class Group(
    var id: String = "",
    val name: String = "",
    val description: String = "",
    val members: List<String> = emptyList(),
    val admins: List<String> = emptyList(),
    val lastMessage: String = "",
    val lastMessageTime: Timestamp? = null,
    val mutedBy: List<String> = emptyList(),
    val pinnedMessageId: String = ""
)
