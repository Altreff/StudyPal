package com.example.flashmaster.Quizz

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.Fragment
import com.example.flashmaster.FoldersPart.New.Flashcard
import com.example.flashmaster.ui.theme.FlashMasterTheme
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

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
                    val coroutineScope = rememberCoroutineScope()

                    LaunchedEffect(folderId) {
                        db.collection("flashcards")
                            .whereEqualTo("folderId", folderId)
                            .get()
                            .addOnSuccessListener { snapshot ->
                                flashcards = snapshot.documents.mapNotNull {
                                    Flashcard.fromMap(it.id, it.data ?: return@mapNotNull null)
                                }
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

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(Unit) {
                                    detectHorizontalDragGestures { _, dragAmount ->
                                        when {
                                            dragAmount > 50 -> { // Swipe right
                                                if (currentIndex > 0) {
                                                    currentIndex--
                                                    revealed = false
                                                }
                                            }
                                            dragAmount < -50 -> { // Swipe left
                                                if (currentIndex < flashcards.size - 1) {
                                                    currentIndex++
                                                    revealed = false
                                                } else {
                                                    quizDone = true
                                                }
                                            }
                                        }
                                    }
                                }
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onDoubleTap = { revealed = !revealed }
                                    )
                                }
                        ) {
                            AnimatedVisibility(
                                visible = !revealed,
                                enter = fadeIn() + slideInHorizontally(),
                                exit = fadeOut() + slideOutHorizontally()
                            ) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                        .height(200.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color(0xFF2196F3) // Material Blue
                                    )
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = card?.frontText ?: "No Cards",
                                            fontSize = 24.sp,
                                            color = Color.White
                                        )
                                    }
                                }
                            }

                            AnimatedVisibility(
                                visible = revealed,
                                enter = fadeIn() + slideInHorizontally { it },
                                exit = fadeOut() + slideOutHorizontally { -it }
                            ) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                        .height(200.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color(0xFF1976D2) // Darker Blue
                                    )
                                ) {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = card?.backText ?: "No Cards",
                                            fontSize = 24.sp,
                                            color = Color.White
                                        )
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                                    .align(Alignment.BottomCenter),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Button(
                                    onClick = {
                                        incorrectCount++
                                        if (currentIndex < flashcards.size - 1) {
                                            currentIndex++
                                            revealed = false
                                        } else {
                                            quizDone = true
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFE57373) // Light Red
                                    )
                                ) {
                                    Text("Incorrect")
                                }

                                Button(
                                    onClick = { revealed = !revealed },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF2196F3) // Blue
                                    )
                                ) {
                                    Text(if (revealed) "Hide" else "Reveal")
                                }

                                Button(
                                    onClick = {
                                        correctCount++
                                        if (currentIndex < flashcards.size - 1) {
                                            currentIndex++
                                            revealed = false
                                        } else {
                                            quizDone = true
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF81C784) // Light Green
                                    )
                                ) {
                                    Text("Correct")
                                }
                            }
                        }
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
                shape = RoundedCornerShape(28.dp)
            ) {
                Text("Done", fontSize = 20.sp)
            }
        }
    }
}