package com.example.mobvzadanie.model

data class Question(
    val questionText: String,
    val options: List<String>,
    val correctAnswer: String,
    val category: String,
    val difficulty: String,
    val type: String
)