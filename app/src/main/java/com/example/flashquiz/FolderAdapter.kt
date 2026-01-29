package com.example.flashquiz

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView
import com.example.flashquiz.databinding.ItemFolderBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FolderAdapter(private val folderList: MutableList<Folder>) :
    RecyclerView.Adapter<FolderAdapter.FolderViewHolder>() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    inner class FolderViewHolder(val binding: ItemFolderBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FolderViewHolder {
        val binding = ItemFolderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FolderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FolderViewHolder, position: Int) {
        val folder = folderList[position]

        holder.binding.folderNameTextView.text = folder.name
        holder.binding.folderDescTextView.text = folder.description ?: ""

        //  3-dot menu click
        holder.binding.menuButton.setOnClickListener {
            showPopupMenu(it, folder, position)
        }
    }

    override fun getItemCount(): Int = folderList.size

    private fun showPopupMenu(view: View, folder: Folder, position: Int) {
        val popup = PopupMenu(view.context, view)
        popup.inflate(R.menu.folder_menu)

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {

                R.id.action_edit -> {
                    val context = view.context
                    val intent = Intent(context, CreateFolderActivity::class.java)
                    intent.putExtra("folderId", folder.id)
                    intent.putExtra("folderName", folder.name)
                    intent.putExtra("folderDesc", folder.description)
                    context.startActivity(intent)
                    true
                }

                R.id.action_delete -> {
                    deleteFolder(folder, position)
                    true
                }

                else -> false
            }
        }
        popup.show()
    }

    private fun deleteFolder(folder: Folder, position: Int) {
        val userId = auth.currentUser!!.uid

        db.collection("users")
            .document(userId)
            .collection("folders")
            .document(folder.id)
            .delete()
            .addOnSuccessListener {
                folderList.removeAt(position)
                notifyItemRemoved(position)
            }
    }
}
