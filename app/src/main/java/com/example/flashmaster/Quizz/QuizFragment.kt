package com.example.flashmaster.Quizz

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.*
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import com.example.flashmaster.FoldersPart.New.Flashcard
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.material3.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import com.example.flashmaster.ui.theme.FlashMasterTheme

class QuizFragment : Fragment() {
    private lateinit var folderId: String
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        folderId = arguments?.getString("folderId") ?: ""
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                FlashMasterTheme {
                    var flashcards by remember { mutableStateOf<List<Flashcard>>(emptyList()) }
                    var currentIndex by remember { mutableStateOf(0) }
                    var revealed by remember { mutableStateOf(false) }
                    var correctCount by remember { mutableStateOf(0) }
                    var incorrectCount by remember { mutableStateOf(0) }
                    var skippedCount by remember { mutableStateOf(0) }
                    var quizDone by remember { mutableStateOf(false) }

                    // Load flashcards only once
                    LaunchedEffect(folderId) {
                        db.collection("flashcards")
                            .whereEqualTo("folderId", folderId)
                            .get()
                            .addOnSuccessListener { snapshot ->
                                flashcards = snapshot.documents.mapNotNull {
                                    Flashcard.fromMap(it.id, it.data ?: return@mapNotNull null)
                                }
                                currentIndex = 0
                                revealed = false
                                correctCount = 0
                                incorrectCount = 0
                                skippedCount = 0
                                quizDone = false
                            }
                            .addOnFailureListener {
                                flashcards = emptyList()
                                currentIndex = 0
                                revealed = false
                                correctCount = 0
                                incorrectCount = 0
                                skippedCount = 0
                                quizDone = false
                            }
                    }

                    if (quizDone) {
                        QuizResultScreen(
                            correct = correctCount,
                            incorrect = incorrectCount,
                            skipped = skippedCount,
                            onDone = { requireActivity().onBackPressedDispatcher.onBackPressed() }
                        )
                    } else {
                        val card = flashcards.getOrNull(currentIndex)
                        val frontText = card?.frontText ?: "No Cards"
                        val backText = card?.backText ?: "No Cards"

                        FlashcardScreen(
                            cardFrontText = frontText,
                            cardBackText = backText,
                            cardIndex = currentIndex,
                            cardCount = flashcards.size,
                            revealed = revealed,
                            onBack = { requireActivity().onBackPressedDispatcher.onBackPressed() },
                            onPrev = {
                                if (flashcards.isNotEmpty() && currentIndex > 0) {
                                    skippedCount++
                                    currentIndex = (currentIndex - 1 + flashcards.size) % flashcards.size
                                    revealed = false
                                }
                            },
                            onNext = {
                                if (flashcards.isNotEmpty() && currentIndex < flashcards.size - 1) {
                                    skippedCount++
                                    currentIndex = (currentIndex + 1) % flashcards.size
                                    revealed = false
                                } else if (flashcards.isNotEmpty() && currentIndex == flashcards.size - 1) {
                                    skippedCount++
                                    quizDone = true
                                }
                            },
                            onEdit = { /* TODO: Edit action */ },
                            onReveal = {
                                revealed = true
                            },
                            onCorrect = {
                                correctCount++
                                if (currentIndex < flashcards.size - 1) {
                                    currentIndex++
                                    revealed = false
                                } else {
                                    quizDone = true
                                }
                            },
                            onIncorrect = {
                                incorrectCount++
                                if (currentIndex < flashcards.size - 1) {
                                    currentIndex++
                                    revealed = false
                                } else {
                                    quizDone = true
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    companion object {
        fun newInstance(folderId: String): QuizFragment {
            return QuizFragment().apply {
                arguments = Bundle().apply {
                    putString("folderId", folderId)
                }
            }
        }
    }
}

@Composable
fun QuizResultScreen(correct: Int, incorrect: Int, skipped: Int, onDone: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text("Done", fontSize = 36.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
            Spacer(modifier = Modifier.height(32.dp))
            Text("Correct:  $correct", fontSize = 24.sp, color = MaterialTheme.colorScheme.onBackground)
            Text("Incorrect:  $incorrect", fontSize = 24.sp, color = MaterialTheme.colorScheme.onBackground)
            Text("Skipped:  $skipped", fontSize = 24.sp, color = MaterialTheme.colorScheme.onBackground)
            Spacer(modifier = Modifier.height(48.dp))
            Button(
                onClick = onDone,
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(56.dp),
                shape =         RoundedCornerShape(28.dp)
            ) {
                Text("Done", fontSize = 20.sp)
            }
        }
    }
}