package com.example.flashquiz

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.core.view.WindowInsetsCompat

class FlashcardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_flashcard)

        val folderName = intent.getStringExtra("folderName")
        title = folderName

        val fab: FloatingActionButton = findViewById(R.id.addFlashcardFab)
        fab.setOnClickListener {
            // Show dialog to add flashcard
            showAddFlashcardDialog()
        }
    }

    private fun showAddFlashcardDialog() {
        // Your dialog code to add flashcards
    }
}
