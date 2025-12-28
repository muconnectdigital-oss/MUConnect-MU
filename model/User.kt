package com.rsservice.muconnect.model

data class User(
    var id: String = "",
    val name: String = "",
    val role: String = "",
    val studentClass: String = "",
    val batch: String = "",
    val extra: String = "" // GR / class info
)
