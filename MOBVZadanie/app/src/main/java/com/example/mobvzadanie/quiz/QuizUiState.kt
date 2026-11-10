package com.example.mobvzadanie.quiz

sealed interface QuizUiState {
    object Loading : QuizUiState
    data class Error(val message: String) : QuizUiState
    data class Success(
        val categories: List<QuizCategoryEntity>,
        val isRefreshing: Boolean = false,
        val errorMessage: String? = null
    ) : QuizUiState
}