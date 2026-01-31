package com.example.flashquiz

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.flashquiz.databinding.ActivityReviewFlashcardBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ReviewFlashcardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReviewFlashcardBinding
    private val flashcards = mutableListOf<Flashcard>()
    private var currentIndex = 0
    private var showAnswer = false

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReviewFlashcardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val folderId = intent.getStringExtra("folderId") ?: return
        loadFlashcards(folderId)

        binding.cardView.setOnClickListener {
            flipCard()
        }

        binding.nextBtn.setOnClickListener {
            nextCard()
        }
    }

    private fun loadFlashcards(folderId: String) {
        val userId = auth.currentUser?.uid ?: return

        db.collection("users")
            .document(userId)
            .collection("folders")
            .document(folderId)
            .collection("flashcards")
            .get()
            .addOnSuccessListener { snapshot ->
                flashcards.clear()
                snapshot.documents.forEach { doc ->
                    val card = doc.toObject(Flashcard::class.java)
                    if (card != null) flashcards.add(card)
                }
                showCard()
            }
    }

    private fun showCard() {
        if (flashcards.isEmpty()) return
        showAnswer = false
        binding.cardText.text = flashcards[currentIndex].question
    }

    private fun flipCard() {
        if (flashcards.isEmpty()) return
        showAnswer = !showAnswer
        binding.cardText.text =
            if (showAnswer) flashcards[currentIndex].answer
            else flashcards[currentIndex].question
    }

    private fun nextCard() {
        if (flashcards.isEmpty()) return
        currentIndex = (currentIndex + 1) % flashcards.size
        showCard()
    }
}
