package com.example.flashquiz

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.flashquiz.databinding.FragmentAddFlashcardBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AddFlashcardDialogFragment : DialogFragment() {

    private var _binding: FragmentAddFlashcardBinding? = null
    private val binding get() = _binding!!

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private var folderId: String = ""
    private var flashcardId: String? = null // null for new, non-null for edit
    private var existingQuestion: String? = null
    private var existingAnswer: String? = null

    companion object {
        fun newInstance(folderId: String, question: String = "", answer: String = "", flashcardId: String? = null): AddFlashcardDialogFragment {
            val fragment = AddFlashcardDialogFragment()
            val args = Bundle()
            args.putString("folderId", folderId)
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
        existingQuestion = arguments?.getString("question")
        existingAnswer = arguments?.getString("answer")
        flashcardId = arguments?.getString("flashcardId")


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddFlashcardBinding.inflate(inflater, container, false)

        // Set folder name as toolbar title
        val folderName = arguments?.getString("folderName") ?: "Flashcards"
        binding.dialogToolbar.title = folderName
        binding.dialogToolbar.setTitleTextColor(resources.getColor(android.R.color.white))
        binding.dialogToolbar.setTitleTextAppearance(context, android.R.style.TextAppearance_Material_Widget_ActionBar_Title) // bold text

        // Set white back button
        binding.dialogToolbar.setNavigationIcon(android.R.drawable.ic_menu_close_clear_cancel)
        binding.dialogToolbar.navigationIcon?.setTint(resources.getColor(android.R.color.white))
        binding.dialogToolbar.setNavigationOnClickListener { dismiss() }

        // Pre-fill question and answer if editing
        binding.questionEditText.setText(existingQuestion)
        binding.answerEditText.setText(existingAnswer)

        // Save button
        binding.saveFlashcardBtn.setOnClickListener { saveOrUpdateFlashcard() }

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

        if (flashcardId != null) {
            // Update existing
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
            // Add new
            db.collection("users")
                .document(userId)
                .collection("folders")
                .document(folderId)
                .collection("flashcards")
                .add(flashcardData)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Flashcard added!", Toast.LENGTH_SHORT).show()
                    dismiss()
                }
        }
    }
    override fun onStart() {
        super.onStart()

        // Increase width more (90% of screen), height stays WRAP_CONTENT
        val width = (resources.displayMetrics.widthPixels * 0.9).toInt()
        val height = (resources.displayMetrics.heightPixels * 0.65).toInt() // medium height

        dialog?.window?.setLayout(width, height)
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

