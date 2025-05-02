package com.example.flashmaster.Quizz

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.draw.alpha
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.fragment.app.Fragment
import com.example.flashmaster.FoldersPart.New.Flashcard
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.math.roundToInt

class QuizFragment : Fragment() {

    private lateinit var folderId: String
    private val db = FirebaseFirestore.getInstance()
    private var flashcards: List<Flashcard> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        folderId = arguments?.getString("folderId") ?: ""
        Log.d("QuizFragment", "onCreate: folderId = $folderId")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d("QuizFragment", "onCreateView: folderId = $folderId")
        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    var currentIndex by remember { mutableStateOf(0) }
                    var isFlipped by remember { mutableStateOf(false) }
                    var offsetX by remember { mutableStateOf(0f) }
                    var offsetY by remember { mutableStateOf(0f) }
                    var isLoading by remember { mutableStateOf(true) }
                    
                    val density = LocalDensity.current
                    
                    LaunchedEffect(Unit) {
                        Log.d("QuizFragment", "Loading flashcards for folderId: $folderId")
                        loadFlashcards { loadedFlashcards ->
                            Log.d("QuizFragment", "Loaded ${loadedFlashcards.size} flashcards")
                            flashcards = loadedFlashcards
                            isLoading = false
                        }
                    }
                    
                    if (isLoading) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                        return@MaterialTheme
                    }
                    
                    if (flashcards.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No flashcards available in this folder")
                        }
                        return@MaterialTheme
                    }
                    
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Progress indicator
                        Text(
                            text = "${currentIndex + 1}/${flashcards.size}",
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        
                        // Flashcard
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(16.dp)
                                .pointerInput(Unit) {
                                    detectDragGestures(
                                        onDrag = { change: PointerInputChange, dragAmount: Offset ->
                                            change.consume()
                                            offsetX += dragAmount.x
                                            offsetY += dragAmount.y
                                        },
                                        onDragEnd = {
                                            if (offsetX > 100f) {
                                                // Swipe right - previous card
                                                if (currentIndex > 0) {
                                                    currentIndex--
                                                    isFlipped = false
                                                }
                                            } else if (offsetX < -100f) {
                                                // Swipe left - next card
                                                if (currentIndex < flashcards.size - 1) {
                                                    currentIndex++
                                                    isFlipped = false
                                                }
                                            }
                                            offsetX = 0f
                                            offsetY = 0f
                                        }
                                    )
                                }
                                .offset {
                                    IntOffset(offsetX.roundToInt(), offsetY.roundToInt())
                                }
                        ) {
                            val rotation = animateFloatAsState(
                                targetValue = if (isFlipped) 180f else 0f,
                                animationSpec = tween(500)
                            )
                            
                            val animateFront by animateFloatAsState(
                                targetValue = if (!isFlipped) 1f else 0f,
                                animationSpec = tween(500)
                            )
                            
                            val animateBack by animateFloatAsState(
                                targetValue = if (isFlipped) 1f else 0f,
                                animationSpec = tween(500)
                            )
                            
                            // Front of card
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(300.dp)
                                    .graphicsLayer {
                                        rotationY = rotation.value
                                        cameraDistance = 8 * density.density
                                    }
                                    .alpha(animateFront)
                                    .pointerInput(Unit) {
                                        detectTapGestures(
                                            onTap = {
                                                isFlipped = !isFlipped
                                            }
                                        )
                                    },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = flashcards[currentIndex].frontText,
                                        fontSize = 24.sp,
                                        textAlign = TextAlign.Center,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                            
                            // Back of card
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(300.dp)
                                    .graphicsLayer {
                                        rotationY = rotation.value + 180f
                                        cameraDistance = 8 * density.density
                                    }
                                    .alpha(animateBack)
                                    .pointerInput(Unit) {
                                        detectTapGestures(
                                            onTap = {
                                                isFlipped = !isFlipped
                                            }
                                        )
                                    },
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                                )
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = flashcards[currentIndex].backText,
                                        fontSize = 24.sp,
                                        textAlign = TextAlign.Center,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                        }
                        
                        // Navigation buttons
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Button(
                                onClick = {
                                    if (currentIndex > 0) {
                                        currentIndex--
                                        isFlipped = false
                                    }
                                },
                                enabled = currentIndex > 0
                            ) {
                                Text("Previous")
                            }
                            
                            Button(
                                onClick = { isFlipped = !isFlipped }
                            ) {
                                Text(if (isFlipped) "Show Question" else "Show Answer")
                            }
                            
                            Button(
                                onClick = {
                                    if (currentIndex < flashcards.size - 1) {
                                        currentIndex++
                                        isFlipped = false
                                    }
                                },
                                enabled = currentIndex < flashcards.size - 1
                            ) {
                                Text("Next")
                            }
                        }
                    }
                }
            }
        }
    }

    private fun loadFlashcards(onLoaded: (List<Flashcard>) -> Unit) {
        Log.d("QuizFragment", "loadFlashcards: Querying for folderId = $folderId")
        db.collection("flashcards")
            .whereEqualTo("folderId", folderId)
            .get()
            .addOnSuccessListener { snapshot ->
                Log.d("QuizFragment", "loadFlashcards: Got ${snapshot.documents.size} documents")
                val loadedFlashcards = snapshot.documents.mapNotNull {
                    try {
                        Flashcard.fromMap(it.id, it.data ?: return@mapNotNull null)
                    } catch (e: Exception) {
                        Log.e("QuizFragment", "Error parsing flashcard: ${e.message}")
                        null
                    }
                }
                Log.d("QuizFragment", "loadFlashcards: Successfully parsed ${loadedFlashcards.size} flashcards")
                onLoaded(loadedFlashcards)
            }
            .addOnFailureListener { e ->
                Log.e("QuizFragment", "Error loading flashcards: ${e.message}")
                onLoaded(emptyList())
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
