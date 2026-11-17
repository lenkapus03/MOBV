package com.example.mobvzadanie.quiz

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

@Serializable
data class QuizCategoriesResponseDto(
    @SerialName("trivia_categories") val triviaCategories: List<QuizCategoryDto> = emptyList()
)

@Serializable
data class QuizCategoryDto(
    val id: Int,
    val name: String
)

@Serializable
data class TokenResponseDto(
    @SerialName("response_code") val responseCode: Int,
    @SerialName("response_message") val responseMessage: String,
    val token: String? = null
)
interface QuizApiService {
    @GET("api_category.php")
    suspend fun getQuizCategories(): QuizCategoriesResponseDto

    @GET("api_token.php?command=request")
    suspend fun requestToken(): TokenResponseDto

    @GET("api_token.php?command=reset")
    suspend fun resetToken(
        @Query("token") token: String
    ): TokenResponseDto

//    @GET("api.php")
//    suspend fun getQuestions(
//        @Query("amount") amount: Int = 10,
//        @Query("category") category: Int? = null,
//        @Query("difficulty") difficulty: String? = null,
//        @Query("type") type: String? = null,
//        @Query("token") token: String? = null
//    ): QuizQuestionsResponseDto
}

private const val BASE_URL = "https://opentdb.com/"

private val json = Json {
    ignoreUnknownKeys = true
    coerceInputValues = true
}

private val okHttpClient = okhttp3.OkHttpClient.Builder()
    .addInterceptor { chain ->
        val request = chain.request().newBuilder()
            .header("User-Agent", "Mozilla/5.0 (Android; Mobile)")
            .build()
        chain.proceed(request)
    }
    .build()

private val retrofit = Retrofit.Builder()
    .client(okHttpClient)
    .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
    .baseUrl(BASE_URL)
    .build()

object QuizApi {
    val retrofitService: QuizApiService by lazy {
        retrofit.create(QuizApiService::class.java)
    }
}