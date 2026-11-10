package com.example.mobvzadanie.quiz

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.GET

@Serializable
data class QuizCategoriesResponseDto(
    @SerialName("trivia_categories") val triviaCategories: List<QuizCategoryDto> = emptyList()
)

@Serializable
data class QuizCategoryDto(
    val id: Int,
    val name: String
)

interface QuizApiService {
    @GET("api_category.php")
    suspend fun getQuizCategories(): QuizCategoriesResponseDto
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