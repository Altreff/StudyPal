package com.example.flashmaster.Quizz

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.NavigateBefore
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashcardScreen(
    cardFrontText: String,
    cardBackText: String,
    cardIndex: Int,
    cardCount: Int,
    revealed: Boolean,
    onBack: () -> Unit = {},
    onPrev: () -> Unit = {},
    onNext: () -> Unit = {},
    onEdit: () -> Unit = {},
    onReveal: () -> Unit = {},
    onCorrect: () -> Unit = {},
    onIncorrect: () -> Unit = {}
) {
    val colorScheme = MaterialTheme.colorScheme
    val cardText = if (revealed) cardBackText else cardFrontText

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.TopCenter),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = colorScheme.primary)
            }
            Spacer(modifier = Modifier.width(4.dp))
            Spacer(modifier = Modifier.weight(1f))
            Text("aA", color = colorScheme.primary)
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = onPrev) {
                Icon(Icons.Default.NavigateBefore, contentDescription = "Previous", tint = colorScheme.primary)
            }
            IconButton(onClick = onNext) {
                Icon(Icons.Default.NavigateNext, contentDescription = "Next", tint = colorScheme.primary)
            }
            Spacer(modifier = Modifier.width(8.dp))
            TextButton(onClick = onEdit) {
                Text("Edit", color = colorScheme.primary)
            }
        }

        // Card counter
        Text(
            text = "${cardIndex + 1}/$cardCount",
            color = colorScheme.onBackground,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 32.dp, top = 64.dp)
        )

        // Flashcard
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(320.dp)
                .background(colorScheme.surface, RoundedCornerShape(32.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = cardText,
                color = colorScheme.onSurface,
                fontSize = 28.sp,
                fontWeight = FontWeight.Medium
            )
        }

        // Bottom buttons
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (revealed) {
                Button(
                    onClick = onIncorrect,
                    colors = ButtonDefaults.buttonColors(containerColor = colorScheme.error),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Incorrect", color = colorScheme.onError)
                }
                Button(
                    onClick = onCorrect,
                    colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Correct", color = colorScheme.onPrimary)
                }
            } else {
                Button(
                    onClick = onReveal,
                    colors = ButtonDefaults.buttonColors(containerColor = colorScheme.primary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("Reveal", color = colorScheme.onPrimary)
                }
            }
        }
    }
} 