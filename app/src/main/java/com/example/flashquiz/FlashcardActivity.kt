package com.example.flashquiz

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.flashquiz.databinding.ActivityFlashcardBinding

class FlashcardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFlashcardBinding
    private val flashcardList = mutableListOf<Flashcard>()
    private lateinit var adapter: FlashcardAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ViewBinding
        binding = ActivityFlashcardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //  Set toolbar
        setSupportActionBar(binding.flashcardToolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)

// Make back arrow white
        binding.flashcardToolbar.navigationIcon?.setTint(
            resources.getColor(android.R.color.white)
        )
        // Get folder name from intent
        val folderName = intent.getStringExtra("folderName") ?: "Flashcards"

        // Set folder name in custom TextView (white + bold)
        binding.toolbarTitle.text = folderName

        // RecyclerView setup
        adapter = FlashcardAdapter(flashcardList)
        binding.flashcardRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.flashcardRecyclerView.adapter = adapter

        // Floating button click
        binding.addFlashcardFab.setOnClickListener {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, AddFlashcardFragment())
                .addToBackStack(null)
                .commit()
        }



        // Check empty state
        checkEmptyState()
    }


    private fun checkEmptyState() {
        if (flashcardList.isEmpty()) {
            binding.emptyFlashcardText.visibility = android.view.View.VISIBLE
            binding.flashcardRecyclerView.visibility = android.view.View.GONE
        } else {
            binding.emptyFlashcardText.visibility = android.view.View.GONE
            binding.flashcardRecyclerView.visibility = android.view.View.VISIBLE
        }
    }

    private fun showAddFlashcardDialog() {
        // TODO: show dialog to add flashcard
    }
    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

}
