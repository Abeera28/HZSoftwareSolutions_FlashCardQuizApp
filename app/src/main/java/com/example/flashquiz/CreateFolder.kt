package com.example.flashquiz

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.flashquiz.databinding.ActivityCreateFolderBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CreateFolderActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateFolderBinding
    private val db = FirebaseFirestore.getInstance()

    private var folderId: String? = null
    private var isEditMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateFolderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        //  Check if this activity is opened for EDIT
        folderId = intent.getStringExtra("folderId")

        if (folderId != null) {
            isEditMode = true

            val name = intent.getStringExtra("folderName")
            val desc = intent.getStringExtra("folderDesc")

            binding.folderNameEditText.setText(name)
            binding.folderDescEditText.setText(desc)

            binding.saveFolderButton.text = "Update Folder"
            supportActionBar?.title = "Edit Folder"
        }

        binding.saveFolderButton.setOnClickListener {
            val folderName = binding.folderNameEditText.text.toString().trim()
            val folderDesc = binding.folderDescEditText.text.toString().trim()

            if (folderName.isEmpty()) {
                Toast.makeText(this, "Please enter folder name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (isEditMode) {
                updateFolder(folderName, folderDesc)
            } else {
                saveFolder(folderName, folderDesc)
            }
        }
    }

    // CREATE NEW FOLDER
    private fun saveFolder(name: String, description: String) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = user.uid

        val folderRef = db.collection("users")
            .document(userId)
            .collection("folders")
            .document()

        // Add timestamp here
        val folder = Folder(
            id = folderRef.id,
            name = name,
            description = description,
            timestamp = System.currentTimeMillis() // current time in milliseconds
        )

        folderRef.set(folder)
            .addOnSuccessListener {
                Toast.makeText(this, "Folder created", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to create folder", Toast.LENGTH_SHORT).show()
            }
    }


    // UPDATE EXISTING FOLDER
    private fun updateFolder(name: String, description: String) {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val userId = user.uid

        val folderRef = db.collection("users")
            .document(userId)
            .collection("folders")
            .document(folderId!!) // existing document ID

        val updates = mapOf(
            "name" to name,
            "description" to description,
            "timestamp" to System.currentTimeMillis() // update timestamp

        )

        folderRef.update(updates)
            .addOnSuccessListener {
                Toast.makeText(this, "Folder updated", Toast.LENGTH_SHORT).show()
                finish() // go back to MainActivity
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to update folder", Toast.LENGTH_SHORT).show()
            }
    }


    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
