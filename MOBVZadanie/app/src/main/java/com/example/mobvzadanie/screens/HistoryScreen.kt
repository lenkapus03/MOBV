package com.example.mobvzadanie.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.mobvzadanie.quiz.QuizCategoryViewModel
import com.example.mobvzadanie.quiz.QuizResultEntity
import com.example.mobvzadanie.quiz.QuizResultWithHistory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class SortOption(val title: String) {
    NEWEST("Najnovšie"),
    BEST_SCORE("Najlepšie skóre"),
    FASTEST_TIME("Najrýchlejší čas")
}

@Composable
fun HistoryScreen(
    modifier: Modifier = Modifier,
    viewModel: QuizCategoryViewModel
) {
    val results by viewModel.allQuizResults.collectAsState()
    var selectedSort by remember { mutableStateOf(SortOption.NEWEST) }
    var selectedResultIdForDetail by remember { mutableStateOf<Long?>(null) }

    val sortedResults = remember(results, selectedSort) {
        when (selectedSort) {
            SortOption.NEWEST -> results.sortedByDescending { it.timestamp }
            SortOption.BEST_SCORE -> results.sortedByDescending { it.correctAnswers }
            SortOption.FASTEST_TIME -> results.sortedBy { it.durationSeconds }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "História kvízov",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SortOption.entries.forEach { option ->
                FilterChip(
                    selected = selectedSort == option,
                    onClick = { selectedSort = option },
                    label = { Text(option.title) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (sortedResults.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Zatiaľ neboli odohrané žiadne kvízy.",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(sortedResults, key = { it.id }) { result ->
                    QuizResultItem(
                        result = result,
                        onClick = { selectedResultIdForDetail = result.id }
                    )
                }
            }
        }
    }

    selectedResultIdForDetail?.let { resultId ->
        QuizDetailDialog(
            resultId = resultId,
            viewModel = viewModel,
            onDismiss = { selectedResultIdForDetail = null }
        )
    }
}

@Composable
fun QuizResultItem(
    result: QuizResultEntity,
    onClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
    val formattedDate = dateFormat.format(Date(result.timestamp))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = result.categoryName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = result.mode,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Skóre: ${result.correctAnswers} / ${result.totalQuestions}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Čas: ${result.durationSeconds}s",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = formattedDate,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun QuizDetailDialog(
    resultId: Long,
    viewModel: QuizCategoryViewModel,
    onDismiss: () -> Unit
) {
    val detailWithHistory by viewModel.getQuizResultWithHistory(resultId).collectAsState(initial = null)

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Zatvoriť")
            }
        },
        title = { Text("Detailný rozbor kvízu") },
        text = {
            detailWithHistory?.let { detail ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(detail.questionHistory) { item ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = if (item.isCorrect)
                                        Color(0xFFE8F5E9) // Svetlozelená
                                    else
                                        Color(0xFFFFEBEE), // Svetločervená
                                    shape = MaterialTheme.shapes.small
                                )
                                .padding(12.dp)
                        ) {
                            Text(
                                text = item.questionText,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Vaša odpoveď: ${item.selectedAnswer ?: "Vypršal čas"}",
                                color = if (item.isCorrect) Color(0xFF2E7D32) else Color(0xFFC62828),
                                style = MaterialTheme.typography.bodySmall
                            )
                            if (!item.isCorrect) {
                                Text(
                                    text = "Správna odpoveď: ${item.correctAnswer}",
                                    color = Color(0xFF2E7D32),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            } ?: Text("Načítavam detail...")
        }
    )
}