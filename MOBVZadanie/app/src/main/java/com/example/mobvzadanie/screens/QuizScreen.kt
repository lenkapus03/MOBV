package com.example.mobvzadanie.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mobvzadanie.model.QuizGameState
import com.example.mobvzadanie.model.QuizMode
import com.example.mobvzadanie.quiz.QuizCategoryViewModel
import com.example.mobvzadanie.quiz.QuizGameViewModel

@Composable
fun QuizScreen(
    modifier: Modifier = Modifier,
    onNavigateToHomeScreen: () -> Unit,
    viewModel: QuizCategoryViewModel
) {
    val context = LocalContext.current
    val selectedAnswers by viewModel.selectedAnswers.collectAsState()

    val categoryId = selectedAnswers[0]?.urlValue?.toIntOrNull()
    val difficulty = selectedAnswers[1]?.urlValue
    val type = selectedAnswers[2]?.urlValue
    val modeTitle = selectedAnswers[3]?.text

    val gameViewModel: QuizGameViewModel = viewModel(
        factory = QuizGameViewModel.provideFactory(
            context = context,
            categoryId = categoryId,
            difficulty = difficulty,
            type = type,
            modeTitle = modeTitle
        )
    )

    val gameState by gameViewModel.gameState.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        when (val state = gameState) {
            is QuizGameState.Loading -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "Načítavam kvízové otázky...", style = MaterialTheme.typography.bodyLarge)
                }
            }

            is QuizGameState.Error -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Chyba: ${state.message}",
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { gameViewModel.startGame() }) {
                        Text(text = "Skúsiť znova")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(onClick = onNavigateToHomeScreen) {
                        Text(text = "Návrat domov")
                    }
                }
            }

            is QuizGameState.ActiveQuestion -> {
                ActiveQuestionContent(
                    state = state,
                    onOptionSelected = { option -> gameViewModel.onAnswerSelected(option) }
                )
            }

            is QuizGameState.Finished -> {
                FinishedQuizContent(
                    state = state,
                    onNavigateToHomeScreen = onNavigateToHomeScreen
                )
            }
        }
    }
}

@Composable
fun ActiveQuestionContent(
    state: QuizGameState.ActiveQuestion,
    onOptionSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (state.mode == QuizMode.CLASSIC)
                        "Otázka ${state.questionIndex} / ${state.totalQuestions}"
                    else
                        "Otázka ${state.questionIndex} (Maratón)",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "${state.timeRemainingSeconds}s",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (state.timeRemainingSeconds <= 3)
                        MaterialTheme.colorScheme.error
                    else
                        MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 15-sekundový progress bar
            LinearProgressIndicator(
                progress = { state.timeRemainingSeconds / 15f },
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Text(
                text = state.question.questionText,
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(24.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            state.question.options.forEach { option ->
                OutlinedButton(
                    onClick = { onOptionSelected(option) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text(
                        text = option,
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun FinishedQuizContent(
    state: QuizGameState.Finished,
    onNavigateToHomeScreen: () -> Unit
) {
    val accuracyPercentage = if (state.totalQuestions > 0) {
        (state.correctCount * 100) / state.totalQuestions
    } else 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Kvíz dokončený!",
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Herný mód: ${state.mode.title}",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Správne odpovede: ${state.correctCount} / ${state.totalQuestions}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Nesprávne odpovede: ${state.incorrectCount}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Úspešnosť: $accuracyPercentage %",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Celkový čas trvania: ${state.totalTimeSeconds} s",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onNavigateToHomeScreen,
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(48.dp)
        ) {
            Text(text = "Návrat domov", style = MaterialTheme.typography.titleMedium)
        }
    }
}