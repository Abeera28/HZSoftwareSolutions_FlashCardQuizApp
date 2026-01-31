package com.example.flashquiz

import android.content.Intent
import android.view.LayoutInflater
import java.util.Date
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import java.util.Locale
import java.text.SimpleDateFormat
import androidx.recyclerview.widget.RecyclerView
import com.example.flashquiz.databinding.ItemFolderBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FolderAdapter(private val folderList: MutableList<Folder>,
    private val onFolderClick: (Folder) -> Unit):
    RecyclerView.Adapter<FolderAdapter.FolderViewHolder>() {

    private val db = FirebaseFirestore.getInstance()

    inner class FolderViewHolder(val binding: ItemFolderBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FolderViewHolder {
        val binding = ItemFolderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FolderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FolderViewHolder, position: Int) {
        val folder = folderList[position]

        holder.binding.folderNameTextView.text = folder.name
        holder.binding.folderDescTextView.text = folder.description

        // Format timestamp to hh:mm a (e.g., 11:45 PM)
        val date = Date(folder.timestamp)
        val formatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
        holder.binding.folderTimeTextView.text = formatter.format(date)

        holder.binding.flashcardCountTextView.text = "${folder.flashcardCount} cards"

        // 3-dot menu click
        holder.binding.menuButton.setOnClickListener {
            val popup = PopupMenu(holder.itemView.context, holder.binding.menuButton)
            popup.menuInflater.inflate(R.menu.folder_menu, popup.menu)

            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {

                    //  EDIT
                    R.id.editFolder -> {
                        val context = holder.itemView.context
                        val intent = Intent(context, CreateFolderActivity::class.java)
                        intent.putExtra("folderId", folder.id)
                        intent.putExtra("folderName", folder.name)
                        intent.putExtra("folderDesc", folder.description)
                        context.startActivity(intent)
                        true
                    }

                    // DELETE
                    R.id.deleteFolder -> {
                        deleteFolder(folder, holder) // Only folder and holder
                        true
                    }

                    else -> false
                }
            }

            popup.show()
        }
        // ADD THIS: Click listener for the whole folder card
        holder.binding.root.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, FlashcardActivity::class.java)
            intent.putExtra("folderId", folder.id)
            intent.putExtra("folderName", folder.name)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = folderList.size

    //  DELETE FUNCTION
    // Inside FolderAdapter
    private fun deleteFolder(folder: Folder, holder: FolderViewHolder) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        db.collection("users")
            .document(userId)
            .collection("folders")
            .document(folder.id)
            .delete()
            .addOnSuccessListener {
                // No need to manually remove from folderList
                // Firestore snapshot listener in MainActivity will auto-update the RecyclerView
                Toast.makeText(holder.itemView.context, "Folder deleted", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(holder.itemView.context, "Failed to delete", Toast.LENGTH_SHORT).show()
            }
    }


}
