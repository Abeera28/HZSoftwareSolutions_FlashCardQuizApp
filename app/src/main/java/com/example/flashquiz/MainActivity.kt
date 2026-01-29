package com.example.flashquiz

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import kotlin.jvm.java
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.flashquiz.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val db = FirebaseFirestore.getInstance()
    private lateinit var auth: FirebaseAuth
    private val folderList = mutableListOf<Folder>()
    private lateinit var adapter: FolderAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        setSupportActionBar(binding.toolbar)

        // RecyclerView setup
        adapter = FolderAdapter(folderList) { folder ->
            // Open FlashcardActivity when a folder is clicked
            val intent = Intent(this, FlashcardActivity::class.java)
            intent.putExtra("folderId", folder.id)
            intent.putExtra("folderName", folder.name)
            startActivity(intent)
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        // Load folders from Firebase in real-time
        val userId = auth.currentUser!!.uid

        db.collection("users")
            .document(userId)
            .collection("folders")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Toast.makeText(this, "Error loading folders", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                folderList.clear()
                snapshot?.documents?.forEach { doc ->
                    val folder = doc.toObject(Folder::class.java)
                    if (folder != null) folderList.add(folder)
                }

                adapter.notifyDataSetChanged()

                if (folderList.isEmpty()) {
                    binding.emptyPlaceholder.visibility = View.VISIBLE
                    binding.recyclerView.visibility = View.GONE
                } else {
                    binding.emptyPlaceholder.visibility = View.GONE
                    binding.recyclerView.visibility = View.VISIBLE
                }
            }

        // Button click -> go to CreateFolderActivity
        binding.createFolderButton.setOnClickListener {
            val intent = Intent(this, CreateFolderActivity::class.java)
            startActivity(intent)
        }
    }
}
