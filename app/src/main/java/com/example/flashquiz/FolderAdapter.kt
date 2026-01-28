package com.example.flashquiz

import android.view.LayoutInflater
import android.view.ViewGroup
import com.example.flashquiz.databinding.ItemFolderBinding
import androidx.recyclerview.widget.RecyclerView

class FolderAdapter(private val folderList: List<Folder>) :
    RecyclerView.Adapter<FolderAdapter.FolderViewHolder>() {

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
    }

    override fun getItemCount(): Int = folderList.size
}

