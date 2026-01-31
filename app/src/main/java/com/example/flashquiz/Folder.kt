package com.example.flashquiz

data class Folder(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val flashcardCount: Long = 0
)