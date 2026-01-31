package com.example.flashquiz

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.flashquiz.databinding.FragmentAddFlashcardBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.lang.Exception

class AddFlashcardDialogFragment : DialogFragment() {

    private var _binding: FragmentAddFlashcardBinding? = null
    private val binding get() = _binding!!

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private var folderId: String = ""
    private var folderName: String = ""
    private var flashcardId: String? = null
    private var existingQuestion: String? = null
    private var existingAnswer: String? = null

    companion object {
        fun newInstance(
            folderId: String,
            folderName: String,
            question: String = "",
            answer: String = "",
            flashcardId: String? = null
        ): AddFlashcardDialogFragment {

            val fragment = AddFlashcardDialogFragment()
            val args = Bundle()
            args.putString("folderId", folderId)
            args.putString("folderName", folderName)
            args.putString("question", question)
            args.putString("answer", answer)
            args.putString("flashcardId", flashcardId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        folderId = arguments?.getString("folderId") ?: ""
        folderName = arguments?.getString("folderName") ?: "Flashcards"
        existingQuestion = arguments?.getString("question")
        existingAnswer = arguments?.getString("answer")
        flashcardId = arguments?.getString("flashcardId")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddFlashcardBinding.inflate(inflater, container, false)

        //  Toolbar Title = Folder Name
        binding.dialogToolbar.title = folderName
        binding.dialogToolbar.setTitleTextColor(Color.WHITE)

        //  Close button
        binding.dialogToolbar.setNavigationIcon(android.R.drawable.ic_menu_close_clear_cancel)
        binding.dialogToolbar.navigationIcon?.setTint(Color.WHITE)
        binding.dialogToolbar.setNavigationOnClickListener { dismiss() }

        // Prefill when editing
        binding.questionEditText.setText(existingQuestion)
        binding.answerEditText.setText(existingAnswer)

        // Save button
        binding.saveFlashcardBtn.setOnClickListener {
            saveOrUpdateFlashcard()
        }

        return binding.root
    }

    private fun saveOrUpdateFlashcard() {
        val question = binding.questionEditText.text.toString().trim()
        val answer = binding.answerEditText.text.toString().trim()

        if (question.isEmpty() || answer.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter both question and answer", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = auth.currentUser?.uid ?: return

        val flashcardData = hashMapOf(
            "question" to question,
            "answer" to answer,
            "timestamp" to System.currentTimeMillis()
        )

        try {
            if (flashcardId != null) {
                //  Update flashcard
                db.collection("users")
                    .document(userId)
                    .collection("folders")
                    .document(folderId)
                    .collection("flashcards")
                    .document(flashcardId!!)
                    .set(flashcardData)
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Flashcard updated!", Toast.LENGTH_SHORT).show()
                        dismiss()
                    }
            } else {
                //  Add new flashcard
                db.collection("users")
                    .document(userId)
                    .collection("folders")
                    .document(folderId)
                    .collection("flashcards")
                    .add(flashcardData)
                    .addOnSuccessListener {

                        val folderRef = db.collection("users")
                            .document(userId)
                            .collection("folders")
                            .document(folderId)

                        db.runTransaction { transaction ->
                            val snapshot = transaction.get(folderRef)
                            val currentCount = snapshot.getLong("flashcardCount") ?: 0
                            transaction.update(folderRef, "flashcardCount", currentCount + 1)
                        }

                        Toast.makeText(requireContext(), "Flashcard added!", Toast.LENGTH_SHORT).show()
                        dismiss()
                    }

            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    //  Dialog Size (Wider & Less Height)
    override fun onStart() {
        super.onStart()

        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.95).toInt(), // width 95%
            WindowManager.LayoutParams.WRAP_CONTENT // auto height
        )

        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
