package com.example.flashquiz

import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.View
import android.widget.Toast
import android.widget.TextView
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import android.widget.PopupMenu
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.recyclerview.widget.RecyclerView
import com.example.flashquiz.databinding.ItemFlashcardBinding

class FlashcardAdapter(
    private val flashcards: MutableList<Flashcard>,
    private val folderId: String,
    private val db: FirebaseFirestore
) : RecyclerView.Adapter<FlashcardAdapter.FlashcardViewHolder>() {

    inner class FlashcardViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val flashcardIcon: ImageView = itemView.findViewById(R.id.flashcardIcon)
        val questionTextView: TextView = itemView.findViewById(R.id.questionTextView)
        val answerTextView: TextView = itemView.findViewById(R.id.answerTextView)
        val menuButton: ImageView = itemView.findViewById(R.id.menuButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FlashcardViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_flashcard, parent, false)
        return FlashcardViewHolder(view)
    }

    override fun onBindViewHolder(holder: FlashcardViewHolder, position: Int) {
        val flashcard = flashcards[position]

        holder.questionTextView.text = flashcard.question
        holder.answerTextView.text = flashcard.answer

        // Click on flashcard to edit
        holder.itemView.setOnClickListener {
            val dialog = AddFlashcardDialogFragment.newInstance(
                folderId,
                flashcard.question,
                flashcard.answer,
                flashcard.id
            )
            dialog.show((holder.itemView.context as AppCompatActivity).supportFragmentManager, "EditFlashcard")
        }

        // Popup menu
        holder.menuButton.setOnClickListener { view ->
            val popup = PopupMenu(view.context, view)
            popup.menuInflater.inflate(R.menu.folder_menu, popup.menu)
            popup.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.editFolder -> {
                        // Edit flashcard
                        holder.itemView.performClick()
                        true
                    }
                    R.id.deleteFolder -> {
                        deleteFlashcard(flashcards[holder.adapterPosition], holder)
                        true
                    }
                    else -> false
                }
            }
            popup.show()
        }

    }

    override fun getItemCount(): Int = flashcards.size

    private fun deleteFlashcard(flashcard: Flashcard, holder: FlashcardViewHolder) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        db.collection("users")
            .document(userId)
            .collection("folders")
            .document(folderId)
            .collection("flashcards")
            .document(flashcard.id)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(holder.itemView.context, "Flashcard deleted", Toast.LENGTH_SHORT).show()
                val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return@addOnSuccessListener
                val folderRef = db.collection("users")
                    .document(userId)
                    .collection("folders")
                    .document(folderId)

                db.runTransaction { transaction ->
                    val snapshot = transaction.get(folderRef)
                    val currentCount = snapshot.getLong("flashcardCount") ?: 0
                    if (currentCount > 0) {
                        transaction.update(folderRef, "flashcardCount", currentCount - 1)
                    }
                }

                // DO NOT remove from list manually
            }
            .addOnFailureListener {
                Toast.makeText(holder.itemView.context, "Failed to delete flashcard", Toast.LENGTH_SHORT).show()
            }
    }


}
