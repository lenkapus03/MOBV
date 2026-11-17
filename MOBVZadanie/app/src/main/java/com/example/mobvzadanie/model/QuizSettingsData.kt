package com.example.mobvzadanie.model

data class QuizOption(
    val text: String,
    val urlValue: String? = null
)

data class QuizSettingStep(
    val question: String,
    val urlString: String? = null,
    val options: List<QuizOption>
)

object QuizSettingsData {
    val difficultyOptions = listOf(
        QuizOption("Všetky", null),
        QuizOption("Ľahká", "easy"),
        QuizOption("Stredná", "medium"),
        QuizOption("Ťažká", "hard")
    )

    val typeOptions = listOf(
        QuizOption("Viacero možností", "multiple"),
        QuizOption("Pravda / Nepravda", "boolean")
    )

    val modeOptions = listOf(
        QuizOption("Klasický kvíz"),
        QuizOption("Maratón")
    )

    fun getQuizSteps(categoryOptions: List<QuizOption>): List<QuizSettingStep> {
        return listOf(
            QuizSettingStep("Vyberte si kategóriu otázok", "category", categoryOptions),
            QuizSettingStep("Vyberte si obtiažnosť", "difficulty", difficultyOptions),
            QuizSettingStep("Vyberte si typ otázok", "type", typeOptions),
            QuizSettingStep("Vyberte si režim kvízu", null, modeOptions)
        )
    }
}