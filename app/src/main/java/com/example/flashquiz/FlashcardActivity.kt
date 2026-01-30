package com.example.flashquiz

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
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
        binding.toolbarTitle.text = folderName

        // RecyclerView setup
        adapter = FlashcardAdapter(flashcardList)
        binding.flashcardRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.flashcardRecyclerView.adapter = adapter

        // Load flashcards
        loadFlashcards()

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


    }

    private fun loadFlashcards() {
        if (folderId.isEmpty()) return
        val userId = auth.currentUser?.uid ?: return

        db.collection("users")
            .document(userId)
            .collection("folders")
            .document(folderId)
            .collection("flashcards")
            .orderBy("timestamp")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener

                flashcardList.clear()
                snapshot?.documents?.forEach { doc ->
                    val flashcard = doc.toObject(Flashcard::class.java)
                    if (flashcard != null) flashcardList.add(flashcard)
                }
                adapter.notifyDataSetChanged()
                checkEmptyState()
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
