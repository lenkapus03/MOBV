package com.example.mobvzadanie.quiz

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Embedded
import androidx.room.Relation
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "quiz_categories")
data class QuizCategoryEntity(
    @PrimaryKey val id: Int,
    val name: String
)

@Entity(tableName = "quiz_results")
data class QuizResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val categoryName: String,
    val difficulty: String,
    val mode: String,
    val durationSeconds: Int,
    val totalQuestions: Int,
    val correctAnswers: Int,
    val incorrectAnswers: Int
)

@Entity(
    tableName = "quiz_question_history",
    foreignKeys = [
        ForeignKey(
            entity = QuizResultEntity::class,
            parentColumns = ["id"],
            childColumns = ["quizResultId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class QuizQuestionHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val quizResultId: Long = 0,
    val questionText: String,
    val optionsJson: String,
    val selectedAnswer: String?,
    val correctAnswer: String,
    val timeSpentSeconds: Int,
    val isCorrect: Boolean
)

data class QuizResultWithHistory(
    @Embedded val result: QuizResultEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "quizResultId"
    )
    val questionHistory: List<QuizQuestionHistoryEntity>
)

@Dao
interface QuizDao {
    @Query("SELECT * FROM quiz_categories")
    fun getCategories(): Flow<List<QuizCategoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<QuizCategoryEntity>)

    // DAO metódy pre históriu
    @Insert
    suspend fun insertQuizResult(result: QuizResultEntity): Long

    @Insert
    suspend fun insertQuestionHistoryItems(items: List<QuizQuestionHistoryEntity>)

    // Atomická transakcia pre uloženie celého kvízu naraz
    @Transaction
    suspend fun saveQuizGameResult(
        result: QuizResultEntity,
        historyItems: List<QuizQuestionHistoryEntity>
    ) {
        val resultId = insertQuizResult(result)
        val updatedItems = historyItems.map { it.copy(quizResultId = resultId) }
        insertQuestionHistoryItems(updatedItems)
    }

    @Query("SELECT * FROM quiz_results ORDER BY timestamp DESC")
    fun getAllQuizResultsStream(): Flow<List<QuizResultEntity>>

    @Transaction
    @Query("SELECT * FROM quiz_results WHERE id = :resultId")
    fun getQuizResultWithHistory(resultId: Long): Flow<QuizResultWithHistory>
}

@Database(
    entities = [
        QuizCategoryEntity::class,
        QuizResultEntity::class,
        QuizQuestionHistoryEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class QuizDatabase : RoomDatabase() {
    abstract val quizDao: QuizDao

    companion object {
        @Volatile
        private var INSTANCE: QuizDatabase? = null

        fun getDatabase(context: Context): QuizDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    QuizDatabase::class.java,
                    "quiz_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}