package com.example.mobvzadanie.model

enum class QuizMode(val title: String) {
    CLASSIC("Klasický kvíz"),
    MARATHON("Maratón");

    companion object {
        fun fromTitle(title: String?): QuizMode {
            return entries.find { it.title.equals(title, ignoreCase = true) } ?: CLASSIC
        }
    }
}