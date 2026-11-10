package com.example.mobvzadanie.quiz

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface QuizRepository {
    fun getQuizCategoriesStream(): Flow<List<QuizCategoryEntity>>
    suspend fun refreshCategories()
}


class OfflineFirstQuizRepository(
    private val quizDao: QuizDao,
    private val quizApiService: QuizApiService
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
}