package com.example.flashquiz

data class Flashcard(
    var id: String = "",          // make it var, not val
    val question: String = "",
    val answer: String = "",
    val timestamp: Long = 0L
)

