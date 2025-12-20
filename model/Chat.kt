package com.rsservice.muconnect.model

data class Chat(
    val chatId: String = "",
    val users: List<String> = emptyList(),
    val lastMessage: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)
