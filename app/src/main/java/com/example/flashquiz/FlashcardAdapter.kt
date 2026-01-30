package com.example.flashquiz

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.flashquiz.databinding.ItemFlashcardBinding

class FlashcardAdapter(private val flashcardList: MutableList<Flashcard>) :
    RecyclerView.Adapter<FlashcardAdapter.FlashcardViewHolder>() {

    inner class FlashcardViewHolder(val binding: ItemFlashcardBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FlashcardViewHolder {
        val binding = ItemFlashcardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FlashcardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FlashcardViewHolder, position: Int) {
        val flashcard = flashcardList[position]

        holder.binding.questionTextView.text = flashcard.question
        holder.binding.answerTextView.text = flashcard.answer
    }

    override fun getItemCount(): Int = flashcardList.size
}
