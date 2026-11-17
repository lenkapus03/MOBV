package com.example.mobvzadanie.model

data class QuestionAnswerHistory(
    val question: Question,
    val selectedAnswer: String?,
    val isCorrect: Boolean,
    val timeSpentSeconds: Int
)