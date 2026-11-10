package com.example.mobvzadanie.screens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.mobvzadanie.quiz.QuizCategoryViewModel
import com.example.mobvzadanie.quiz.QuizUiState
import kotlinx.coroutines.launch

data class QuizSettingStep(
    val question: String,
    val options: List<String>,
    val answer: String? = null
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun QuizSettingsScreen(
    modifier: Modifier,
    onNavigateToHomeScreen: () -> Unit,
    viewModel: QuizCategoryViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    val categories = when (val state = uiState) {
        is QuizUiState.Success -> if (state.categories.isNotEmpty()) {
            state.categories.map { it.name }
        } else {
            listOf("Žiadne kategórie nie sú k dispozícii")
        }
        is QuizUiState.Loading -> listOf("Načítavam kategórie...")
        is QuizUiState.Error -> listOf("Chyba načítania (${state.message})")
    }

    val steps = listOf(
        QuizSettingStep("Vyberte si kategóriu otázok", categories),
        QuizSettingStep("Vyberte si obtiažnosť", listOf("Všetky", "Ľahká", "Stredná", "Ťažká")),
        QuizSettingStep("Vyberte si typ otázok", listOf("Viacero možností", "Pravda / Nepravda")),
        QuizSettingStep("Vyberte si režim kvízu", listOf("Klasický kvíz", "Maratón"))
    )

    val pagerState = rememberPagerState(pageCount = { steps.size })
    val coroutineScope = rememberCoroutineScope()

    HorizontalPager(
        state = pagerState,
        modifier = modifier.fillMaxSize(),
        userScrollEnabled = false
    ) { pageIndex ->
        val currentStep = steps[pageIndex]
        val question = currentStep.question
        val options = currentStep.options
        var selectedAnswer by remember(pageIndex) { mutableStateOf(currentStep.answer) }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 64.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                stickyHeader {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        Text(
                            text = question,
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp, horizontal = 8.dp)
                        )
                    }
                }

                items(options) { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedAnswer = option }
                            .padding(vertical = 4.dp, horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        RadioButton(
                            selected = selectedAnswer == option,
                            onClick = { selectedAnswer = option }
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(text = option)
                    }
                }
            }

            Button(
                onClick = {
                    if (pageIndex == steps.size - 1) {
                        onNavigateToHomeScreen()
                    } else {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(
                                page = pageIndex + 1,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioNoBouncy,
                                    stiffness = Spring.StiffnessMedium
                                ),
                            )
                        }
                    }
                },
                modifier = Modifier.align(Alignment.BottomCenter),
                enabled = selectedAnswer != null
            ) {
                Text(text = if (pageIndex == steps.size - 1) "Začať kvíz" else "Pokračovať")
            }
        }
    }
}