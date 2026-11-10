package com.example.mobvzadanie.quiz

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class QuizCategoryViewModel(private val repository: QuizRepository) : ViewModel() {

    private val _isRefreshing = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)

    val uiState: StateFlow<QuizUiState> = combine(
        repository.getQuizCategoriesStream(),
        _isRefreshing,
        _error
    ) { categories, isRefreshing, error ->
        when {
            categories.isEmpty() && isRefreshing -> QuizUiState.Loading
            categories.isEmpty() && error != null -> QuizUiState.Error(error)
            else -> QuizUiState.Success(
                categories = categories,
                isRefreshing = isRefreshing,
                errorMessage = error
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = QuizUiState.Loading
    )

    init {
        refreshData()
    }

    fun refreshData() {
        viewModelScope.launch {
            _isRefreshing.value = true
            _error.value = null
            try {
                repository.refreshCategories()
            } catch (e: Exception) {
                _error.value = e.message ?: "Neznáma chyba siete"
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    companion object {
        fun provideFactory(context: Context): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val database = QuizDatabase.getDatabase(context)
                    val repository = OfflineFirstQuizRepository(database.quizDao, QuizApi.retrofitService)
                    return QuizCategoryViewModel(repository) as T
                }
            }
    }
}