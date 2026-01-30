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

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private var folderId: String = ""

    companion object {
        fun newInstance(folderId: String, folderName: String): AddFlashcardDialogFragment {
            val fragment = AddFlashcardDialogFragment()
            val args = Bundle()
            args.putString("folderId", folderId)
            args.putString("folderName", folderName)
            fragment.arguments = args
            return fragment
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, 0) // optional, remove default title
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddFlashcardBinding.inflate(inflater, container, false)
        val folderName = arguments?.getString("folderName") ?: "Flashcards"
        binding.dialogToolbar.title = folderName
        binding.dialogToolbar.navigationIcon?.setTint(resources.getColor(android.R.color.white))
        binding.dialogToolbar.setTitleTextColor(resources.getColor(android.R.color.white))



        // Toolbar back button
        binding.dialogToolbar.setNavigationIcon(android.R.drawable.ic_menu_close_clear_cancel)
        binding.dialogToolbar.setNavigationOnClickListener { dismiss() }

        binding.saveFlashcardBtn.setOnClickListener { saveFlashcard() }

        return binding.root
    }

    private fun saveFlashcard() {
        val question = binding.questionEditText.text.toString().trim()
        val answer = binding.answerEditText.text.toString().trim()

        if (question.isEmpty() || answer.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter both question and answer", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = auth.currentUser?.uid ?: return
        val flashcard = hashMapOf(
            "question" to question,
            "answer" to answer,
            "timestamp" to System.currentTimeMillis()
        )

        // Disable button to prevent double click
        binding.saveFlashcardBtn.isEnabled = false

        // Save to Firestore first
        db.collection("users")
            .document(userId)
            .collection("folders")
            .document(folderId)
            .collection("flashcards")
            .add(flashcard)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Flashcard added!", Toast.LENGTH_SHORT).show()

                // Dismiss after Firebase operation to avoid crash
                if (isAdded) dismiss()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to add flashcard", Toast.LENGTH_SHORT).show()
                binding.saveFlashcardBtn.isEnabled = true
            }
    }


    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.9).toInt(), // 90% of screen width
            ViewGroup.LayoutParams.WRAP_CONTENT // height wraps content
        )
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
