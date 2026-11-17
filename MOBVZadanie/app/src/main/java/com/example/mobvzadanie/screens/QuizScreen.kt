package com.example.mobvzadanie.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.mobvzadanie.quiz.QuizCategoryViewModel

@Composable
fun QuizScreen(
    modifier: Modifier = Modifier,
    onNavigateToHomeScreen: () -> Unit,
    viewModel: QuizCategoryViewModel
) {
    val selectedAnswers by viewModel.selectedAnswers.collectAsState()
    val token by viewModel.tokenState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadToken()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Prehľad kvízu",
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Prehľad vybraných možností z nastavení
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Zvolené nastavenia:",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(12.dp))

                Text("Kategória: ${selectedAnswers[0]?.text ?: "Nezadané"}")
                Spacer(modifier = Modifier.height(4.dp))
                Text("Obtiažnosť: ${selectedAnswers[1]?.text ?: "Nezadané"}")
                Spacer(modifier = Modifier.height(4.dp))
                Text("Typ: ${selectedAnswers[2]?.text ?: "Nezadané"}")
                Spacer(modifier = Modifier.height(4.dp))
                Text("Režim: ${selectedAnswers[3]?.text ?: "Nezadané"}")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Session Token:",
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = token ?: "Načítavam / získavam token...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onNavigateToHomeScreen,
            modifier = Modifier.fillMaxWidth(0.7f)
        ) {
            Text(text = "Návrat domov")
        }
    }
}