package com.example.flashquiz

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.flashquiz.databinding.ActivityCreateFolderBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CreateFolderActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateFolderBinding
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateFolderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // back arrow

        binding.saveFolderButton.setOnClickListener {
            // Get folder name and description
            val folderName = binding.folderNameEditText.text.toString().trim()
            val folderDesc = binding.folderDescEditText.text.toString().trim() // optional

            // Validate folder name
            if (folderName.isEmpty()) {
                Toast.makeText(this, "Please enter folder name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Call save function with both name and description
            saveFolder(folderName, folderDesc)
        }
    }

        // Updated saveFolder function to include description
        private fun saveFolder(name: String, description: String) {
            val userId = FirebaseAuth.getInstance().currentUser!!.uid

            val folderRef = db.collection("users")
                .document(userId)
                .collection("folders")
                .document()

            // Update Folder data class to include description
            val folder = Folder(folderRef.id, name, description)

            folderRef.set(folder)
                .addOnSuccessListener {
                    Toast.makeText(this, "Folder created", Toast.LENGTH_SHORT).show()
                    finish() // go back to MainActivity
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to create folder", Toast.LENGTH_SHORT).show()
                }
        }


        override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
