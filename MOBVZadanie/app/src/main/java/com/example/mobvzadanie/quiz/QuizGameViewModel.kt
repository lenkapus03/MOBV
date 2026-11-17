package com.example.mobvzadanie.quiz

import android.content.Context
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.mobvzadanie.UserAccount
import com.example.mobvzadanie.model.Question
import com.example.mobvzadanie.model.QuestionAnswerHistory
import com.example.mobvzadanie.model.QuizGameState
import com.example.mobvzadanie.model.QuizMode
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class QuizGameViewModel(
    private val repository: QuizRepository,
    private val categoryId: Int?,
    private val difficulty: String?,
    private val type: String?,
    private val mode: QuizMode
) : ViewModel() {

    private val questions = mutableStateListOf<Question>()
    private val history = mutableListOf<QuestionAnswerHistory>()

    private var currentQuestionIndex = 0
    private var correctAnswersCount = 0
    private var incorrectAnswersCount = 0
    private var totalTimeSeconds = 0
    private var isFetchingMore = false

    private val _gameState = MutableStateFlow<QuizGameState>(QuizGameState.Loading)
    val gameState: StateFlow<QuizGameState> = _gameState.asStateFlow()

    private var timerJob: Job? = null
    private var timeRemainingSeconds = 15

    init {
        startGame()
    }

    fun startGame() {
        viewModelScope.launch {
            _gameState.value = QuizGameState.Loading
            try {
                val amount = if (mode == QuizMode.MARATHON) 50 else 10
                val fetched = repository.getQuestions(
                    amount = amount,
                    category = categoryId,
                    difficulty = difficulty,
                    type = type
                )
                questions.clear()
                questions.addAll(fetched)
                currentQuestionIndex = 0
                showNextQuestion()
            } catch (e: Exception) {
                _gameState.value = QuizGameState.Error(e.message ?: "Chyba načítania otázok")
            }
        }
    }

    private fun showNextQuestion() {
        if (currentQuestionIndex >= questions.size) {
            finishGame()
            return
        }

        if (mode == QuizMode.MARATHON && !isFetchingMore && currentQuestionIndex >= questions.size - 15) {
            loadMoreMarathonQuestions()
        }

        start15sTimer()
    }

    private fun start15sTimer() {
        timerJob?.cancel()
        timeRemainingSeconds = 15
        updateActiveQuestionState()

        timerJob = viewModelScope.launch {
            while (timeRemainingSeconds > 0) {
                delay(1000.milliseconds)
                timeRemainingSeconds--
                totalTimeSeconds++
                updateActiveQuestionState()
            }
            onAnswerSelected(selectedAnswer = null)
        }
    }

    fun onAnswerSelected(selectedAnswer: String?) {
        timerJob?.cancel()

        val question = questions[currentQuestionIndex]
        val isCorrect = selectedAnswer != null && selectedAnswer == question.correctAnswer
        val timeSpent = 15 - timeRemainingSeconds

        history.add(
            QuestionAnswerHistory(
                question = question,
                selectedAnswer = selectedAnswer,
                isCorrect = isCorrect,
                timeSpentSeconds = timeSpent
            )
        )

        if (isCorrect) {
            correctAnswersCount++
        } else {
            incorrectAnswersCount++
            if (mode == QuizMode.MARATHON) {
                finishGame()
                return
            }
        }

        currentQuestionIndex++

        if (mode == QuizMode.CLASSIC && currentQuestionIndex >= 10) {
            finishGame()
        } else {
            showNextQuestion()
        }
    }

    private fun loadMoreMarathonQuestions() {
        isFetchingMore = true
        viewModelScope.launch {
            try {
                val newBatch = repository.getQuestions(
                    amount = 50,
                    category = categoryId,
                    difficulty = difficulty,
                    type = type
                )
                questions.addAll(newBatch)
            } catch (e: Exception) {
            } finally {
                isFetchingMore = false
            }
        }
    }

    private fun updateActiveQuestionState() {
        val question = questions[currentQuestionIndex]
        _gameState.value = QuizGameState.ActiveQuestion(
            question = question,
            questionIndex = currentQuestionIndex + 1,
            totalQuestions = if (mode == QuizMode.CLASSIC) 10 else questions.size,
            timeRemainingSeconds = timeRemainingSeconds,
            mode = mode
        )
    }

    private fun finishGame() {
        timerJob?.cancel()

        viewModelScope.launch {
            try {
                val firstQuestion = history.firstOrNull()?.question
                val categoryName = firstQuestion?.category ?: "Všeobecné"
                val difficultyName = firstQuestion?.difficulty ?: "Rôzna"

                val resultEntity = QuizResultEntity(
                    categoryName = categoryName,
                    difficulty = difficultyName,
                    mode = mode.title,
                    durationSeconds = totalTimeSeconds,
                    totalQuestions = history.size,
                    correctAnswers = correctAnswersCount,
                    incorrectAnswers = incorrectAnswersCount
                )

                val historyEntities = history.map { h ->
                    QuizQuestionHistoryEntity(
                        questionText = h.question.questionText,
                        optionsJson = h.question.options.joinToString("|"),
                        selectedAnswer = h.selectedAnswer,
                        correctAnswer = h.question.correctAnswer,
                        timeSpentSeconds = h.timeSpentSeconds,
                        isCorrect = h.isCorrect
                    )
                }

                repository.saveQuizResult(resultEntity, historyEntities)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        _gameState.value = QuizGameState.Finished(
            totalQuestions = history.size,
            correctCount = correctAnswersCount,
            incorrectCount = incorrectAnswersCount,
            totalTimeSeconds = totalTimeSeconds,
            mode = mode,
            history = history.toList()
        )
    }

    companion object {
        fun provideFactory(
            context: Context,
            categoryId: Int?,
            difficulty: String?,
            type: String?,
            modeTitle: String?
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val database = QuizDatabase.getDatabase(context)
                val userAccount = UserAccount(context)
                val repository = OfflineFirstQuizRepository(
                    database.quizDao,
                    QuizApi.retrofitService,
                    userAccount
                )
                return QuizGameViewModel(
                    repository = repository,
                    categoryId = categoryId,
                    difficulty = difficulty,
                    type = type,
                    mode = QuizMode.fromTitle(modeTitle)
                ) as T
            }
        }
    }
}