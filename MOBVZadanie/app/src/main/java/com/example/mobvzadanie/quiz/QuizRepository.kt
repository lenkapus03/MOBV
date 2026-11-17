package com.example.mobvzadanie.quiz

import com.example.mobvzadanie.UserAccount
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

interface QuizRepository {
    fun getQuizCategoriesStream(): Flow<List<QuizCategoryEntity>>
    suspend fun refreshCategories()
    suspend fun getOrFetchToken(): String
    suspend fun resetOrRefreshToken(): String
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

    private suspend fun requestNewToken(): String {
        val response = quizApiService.requestToken()
        val newToken = response.token ?: throw Exception("Získanie nového tokenu zlyhalo")
        userAccount.setSessionKey(newToken)
        return newToken
    }
}