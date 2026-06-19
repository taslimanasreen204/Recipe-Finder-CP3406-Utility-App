package au.edu.jcu.cp3406_cp5307_utilityappstartertemplate.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Centralised Retrofit factory for TheMealDB.
 *
 * Provides a singleton [MealApiService] backed by a shared OkHttp client
 * with HTTP-level request/response logging for debugging.
 *
 * This object acts as the dependency-injection root for the networking layer.
 * In a production app the services would be provided via a Hilt module.
 */
object RetrofitClient {

    private const val BASE_URL = "https://www.themealdb.com/"

    private val okHttpClient: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
    }

    val mealApiService: MealApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MealApiService::class.java)
    }
}
