package com.example.flashquiz

data class Flashcard(
    val id: String = "",
    val question: String = "",
    val answer: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
