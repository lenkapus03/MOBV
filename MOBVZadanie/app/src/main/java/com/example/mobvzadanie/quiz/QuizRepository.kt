package com.example.mobvzadanie.quiz

import com.example.mobvzadanie.UserAccount
import com.example.mobvzadanie.model.Question
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import com.example.mobvzadanie.Utils
import com.example.mobvzadanie.decodeHtml

interface QuizRepository {
    fun getQuizCategoriesStream(): Flow<List<QuizCategoryEntity>>
    suspend fun refreshCategories()
    suspend fun getOrFetchToken(): String
    suspend fun resetOrRefreshToken(): String
    suspend fun getQuestions(
        amount: Int,
        category: Int? = null,
        difficulty: String? = null,
        type: String? = null
    ): List<Question>
    suspend fun saveQuizResult(
        result: QuizResultEntity,
        historyItems: List<QuizQuestionHistoryEntity>
    )
    fun getAllQuizResultsStream(): Flow<List<QuizResultEntity>>
    fun getQuizResultWithHistory(resultId: Long): Flow<QuizResultWithHistory>
}

class OfflineFirstQuizRepository(
    private val quizDao: QuizDao,
    private val quizApiService: QuizApiService,
    private val userAccount: UserAccount
) : QuizRepository {

    override fun getQuizCategoriesStream(): Flow<List<QuizCategoryEntity>> {
        return quizDao.getCategories()
    }

    override suspend fun refreshCategories() {
        val response = quizApiService.getQuizCategories()
        val entities = response.triviaCategories.map { dto ->
            QuizCategoryEntity(id = dto.id, name = dto.name)
        }
        quizDao.insertCategories(entities)
    }

    override suspend fun getOrFetchToken(): String {
        val existingToken = userAccount.getSessionKey().firstOrNull()
        if (!existingToken.isNullOrBlank()) {
            return existingToken
        }
        return requestNewToken()
    }

    override suspend fun resetOrRefreshToken(): String {
        val existingToken = userAccount.getSessionKey().firstOrNull()

        if (!existingToken.isNullOrBlank()) {
            try {
                val resetResponse = quizApiService.resetToken(existingToken)
                if (resetResponse.responseCode == 0 && !resetResponse.token.isNullOrBlank()) {
                    userAccount.setSessionKey(resetResponse.token)
                    return resetResponse.token
                }
            } catch (e: Exception) {
            }
        }

        return requestNewToken()
    }

    override suspend fun getQuestions(
        amount: Int,
        category: Int?,
        difficulty: String?,
        type: String?
    ): List<Question> {
        var token = getOrFetchToken()
        var response = quizApiService.getQuestions(
            amount = amount,
            category = category,
            difficulty = difficulty,
            type = type,
            token = token
        )

        // Token Not Found Session Token does not exist.
        if (response.responseCode == 3) {
            token = requestNewToken()
            response = quizApiService.getQuestions(amount, category, difficulty, type, token)

        // Token Empty Session Token has returned all possible questions for the specified query.
        // Resetting the Token is necessary.
        } else if (response.responseCode == 4) {
            token = resetOrRefreshToken()
            response = quizApiService.getQuestions(amount, category, difficulty, type, token)
        }

        if (response.responseCode != 0 || response.results.isEmpty()) {
            throw Exception("Nenašli sa žiadne otázky pre zvolené kritériá.")
        }

        return response.results.map { dto ->
            val decodedQuestion = dto.question.decodeHtml()
            val decodedCorrect = dto.correctAnswer.decodeHtml()
            val decodedIncorrect = dto.incorrectAnswers.map { it.decodeHtml() }

            val allOptions = (listOf(decodedCorrect) + decodedIncorrect).shuffled()

            Question(
                questionText = decodedQuestion,
                options = allOptions,
                correctAnswer = decodedCorrect,
                category = dto.category.decodeHtml(),
                difficulty = dto.difficulty,
                type = dto.type
            )
        }
    }

    override suspend fun saveQuizResult(
        result: QuizResultEntity,
        historyItems: List<QuizQuestionHistoryEntity>
    ) {
        quizDao.saveQuizGameResult(result, historyItems)
    }

    override fun getAllQuizResultsStream(): Flow<List<QuizResultEntity>> {
        return quizDao.getAllQuizResultsStream()
    }

    override fun getQuizResultWithHistory(resultId: Long): Flow<QuizResultWithHistory> {
        return quizDao.getQuizResultWithHistory(resultId)
    }

    private suspend fun requestNewToken(): String {
        val response = quizApiService.requestToken()
        val newToken = response.token ?: throw Exception("Získanie nového tokenu zlyhalo")
        userAccount.setSessionKey(newToken)
        return newToken
    }
}