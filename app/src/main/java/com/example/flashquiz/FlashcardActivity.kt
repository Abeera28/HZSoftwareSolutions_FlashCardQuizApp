package com.example.flashquiz

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.Query
import kotlin.jvm.java
import android.content.Intent
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.flashquiz.databinding.ActivityFlashcardBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FlashcardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFlashcardBinding
    private val flashcardList = mutableListOf<Flashcard>()
    private lateinit var adapter: FlashcardAdapter

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private var folderId: String = ""
    private var folderName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFlashcardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Toolbar setup
        setSupportActionBar(binding.flashcardToolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.flashcardToolbar.navigationIcon?.setTint(resources.getColor(android.R.color.white))

        // Get folder info
        folderId = intent.getStringExtra("folderId") ?: ""
        folderName = intent.getStringExtra("folderName") ?: "Flashcards"
        binding.flashcardToolbar.title = folderName

        // RecyclerView setup
        adapter = FlashcardAdapter(flashcardList, folderId, db)
        binding.flashcardRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.flashcardRecyclerView.adapter = adapter
        // Load flashcards
        loadFlashcards()

        // Sync flashcard counts for old folders
        syncFlashcardCounts()

        // FAB click
        binding.addFlashcardFab.setOnClickListener {
            val dialog = AddFlashcardDialogFragment.newInstance(folderId, folderName)
            dialog.show(supportFragmentManager, "AddFlashcardDialog")
        }
        // Hide FAB when fragment is open
        supportFragmentManager.addOnBackStackChangedListener {
            val fragmentVisible = supportFragmentManager.findFragmentById(R.id.fragmentContainer) != null

            binding.flashcardRecyclerView.visibility = if (fragmentVisible) View.GONE else View.VISIBLE
            binding.emptyFlashcardText.visibility = if (fragmentVisible) View.GONE else {
                if (flashcardList.isEmpty()) View.VISIBLE else View.GONE
            }
            binding.addFlashcardFab.visibility = if (fragmentVisible) View.GONE else View.VISIBLE
        }
        binding.reviewFlashcardBtn.setOnClickListener {
            if (flashcardList.isEmpty()) {
                Toast.makeText(this, "Add flashcards to start review", Toast.LENGTH_SHORT).show()
            } else {
                val intent = Intent(this, ReviewFlashcardActivity::class.java)
                intent.putExtra("folderId", folderId)
                startActivity(intent)
            }
        }



    }

    private fun loadFlashcards() {
        if (folderId.isEmpty()) return
        val userId = auth.currentUser?.uid ?: return

        db.collection("users")
            .document(userId)
            .collection("folders")
            .document(folderId)
            .collection("flashcards")
            .orderBy("timestamp", Query.Direction.DESCENDING) // newest on top
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener

                flashcardList.clear()
                snapshot?.documents?.forEach { doc ->
                    val flashcard = doc.toObject(Flashcard::class.java)
                    flashcard?.id = doc.id // save Firestore ID
                    if (flashcard != null) flashcardList.add(flashcard)
                }
                adapter.notifyDataSetChanged()
                checkEmptyState()
            }

    }

    private fun syncFlashcardCounts() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("users")
            .document(userId)
            .collection("folders")
            .get()
            .addOnSuccessListener { result ->
                for (doc in result) {
                    val folderId = doc.id

                    // Count flashcards in this folder
                    db.collection("users")
                        .document(userId)
                        .collection("folders")
                        .document(folderId)
                        .collection("flashcards")
                        .get()
                        .addOnSuccessListener { flashcardsSnapshot ->
                            val count = flashcardsSnapshot.size() // actual number of cards
                            // Update Firestore folder document
                            db.collection("users")
                                .document(userId)
                                .collection("folders")
                                .document(folderId)
                                .update("flashcardCount", count)
                        }
                }
            }
    }

    private fun checkEmptyState() {
        if (flashcardList.isEmpty()) {
            binding.emptyFlashcardText.visibility = View.VISIBLE
            binding.flashcardRecyclerView.visibility = View.GONE
        } else {
            binding.emptyFlashcardText.visibility = View.GONE
            binding.flashcardRecyclerView.visibility = View.VISIBLE
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
