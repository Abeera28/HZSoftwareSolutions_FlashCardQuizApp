package com.example.flashquiz

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
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
        adapter = FolderAdapter(folderList)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        // Load folders from Firebase in real-time
        db.collection("folders")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Toast.makeText(this, "Error loading folders", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    folderList.clear()
                    for (doc in snapshot.documents) {
                        val folder = doc.toObject(Folder::class.java)
                        if (folder != null) folderList.add(folder)
                    }
                    adapter.notifyDataSetChanged()
                }
            }

        // Button click -> go to CreateFolderActivity
        binding.createFolderButton.setOnClickListener {
            val intent = Intent(this, CreateFolderActivity::class.java)
            startActivity(intent)
        }
    }
}
