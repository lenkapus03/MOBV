package com.example.mobvzadanie.model

sealed interface QuizGameState {
    object Loading : QuizGameState
    data class Error(val message: String) : QuizGameState

    data class ActiveQuestion(
        val question: Question,
        val questionIndex: Int,
        val totalQuestions: Int,
        val timeRemainingSeconds: Int,
        val mode: QuizMode
    ) : QuizGameState

    data class Finished(
        val totalQuestions: Int,
        val correctCount: Int,
        val incorrectCount: Int,
        val totalTimeSeconds: Int,
        val mode: QuizMode,
        val history: List<QuestionAnswerHistory>
    ) : QuizGameState
}