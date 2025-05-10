package com.example.skystWaffleunivServer.dto

data class SocketNotification(
    val userId: Long,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
)
