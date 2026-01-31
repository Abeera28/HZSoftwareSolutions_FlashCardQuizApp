package com.example.flashquiz

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.flashquiz.databinding.ActivityReviewFlashcardBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ReviewFlashcardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReviewFlashcardBinding

    private val flashcards = mutableListOf<Flashcard>()
    private var currentIndex = 0
    private var isFlipped = false

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private lateinit var folderId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReviewFlashcardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        folderId = intent.getStringExtra("folderId") ?: ""

        loadFlashcards()

        // Card flip
        binding.flashCard.setOnClickListener {
            if (flashcards.isNotEmpty()) flipCard()
        }

        // Navigation
        binding.btnNext.setOnClickListener { goNext() }
        binding.btnBack.setOnClickListener { goPrevious() }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun loadFlashcards() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("users")
            .document(userId)
            .collection("folders")
            .document(folderId)
            .collection("flashcards")
            .orderBy("timestamp")
            .get()
            .addOnSuccessListener { snapshot ->

                flashcards.clear()

                for (doc in snapshot) {
                    val card = doc.toObject(Flashcard::class.java)
                    card.id = doc.id
                    flashcards.add(card)
                }

                if (flashcards.isEmpty()) {
                    Toast.makeText(this, "No flashcards found", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    currentIndex = 0
                    showCard()
                }
            }
    }
    private fun showCard() {
        isFlipped = false

        val card = flashcards[currentIndex]
        binding.tvCardText.text = card.question

        binding.flashCard.setCardBackgroundColor(
            ContextCompat.getColor(this, R.color.blue_card)
        )

        binding.tvCount.text = "${currentIndex + 1} / ${flashcards.size}"
    }

    private fun goNext() {
        if (currentIndex < flashcards.size - 1) {
            currentIndex++
            showCard()
        } else {
            Toast.makeText(this, "This is the last card", Toast.LENGTH_SHORT).show()
        }
    }

    private fun goPrevious() {
        if (currentIndex > 0) {
            currentIndex--
            showCard()
        } else {
            Toast.makeText(this, "This is the first card", Toast.LENGTH_SHORT).show()
        }
    }
    private fun flipCard() {

        val scale = resources.displayMetrics.density
        binding.flashCard.cameraDistance = 8000 * scale

        val flipOut = ObjectAnimator.ofFloat(binding.flashCard, "rotationY", 0f, 90f)
        val flipIn = ObjectAnimator.ofFloat(binding.flashCard, "rotationY", -90f, 0f)

        flipOut.duration = 200
        flipIn.duration = 200

        flipOut.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {

                val card = flashcards[currentIndex]

                if (!isFlipped) {
                    binding.tvCardText.text = card.answer
                    binding.flashCard.setCardBackgroundColor(
                        ContextCompat.getColor(this@ReviewFlashcardActivity, R.color.pink_card)
                    )
                } else {
                    binding.tvCardText.text = card.question
                    binding.flashCard.setCardBackgroundColor(
                        ContextCompat.getColor(this@ReviewFlashcardActivity, R.color.blue_card)
                    )
                }

                isFlipped = !isFlipped
                flipIn.start()
            }
        })

        flipOut.start()
    }
}
