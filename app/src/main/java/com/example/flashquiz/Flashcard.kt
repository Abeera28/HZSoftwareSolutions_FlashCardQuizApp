package com.example.flashquiz

data class Flashcard(
    var id: String = "",
    var question: String = "",
    var answer: String = "",
    var timestamp: Long = 0L
)
