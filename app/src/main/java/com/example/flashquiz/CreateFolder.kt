package com.example.flashquiz

import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import androidx.appcompat.app.AppCompatActivity
import com.example.flashquiz.databinding.ActivityCreateFolderBinding
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
            val folderName = binding.folderNameEditText.text.toString().trim()
            if (folderName.isNotEmpty()) {
                saveFolder(folderName)
            } else {
                Toast.makeText(this, "Enter folder name", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveFolder(name: String) {
        val userId = FirebaseAuth.getInstance().currentUser!!.uid

        val folderRef = db.collection("users")
            .document(userId)
            .collection("folders")
            .document()

        val folder = Folder(folderRef.id, name)

        folderRef.set(folder)
            .addOnSuccessListener {
                Toast.makeText(this, "Folder created", Toast.LENGTH_SHORT).show()
                finish()
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
