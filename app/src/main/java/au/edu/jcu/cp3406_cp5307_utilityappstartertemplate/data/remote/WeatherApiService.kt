package au.edu.jcu.cp3406_cp5307_utilityappstartertemplate.data.remote

import au.edu.jcu.cp3406_cp5307_utilityappstartertemplate.data.model.AreaListResponse
import au.edu.jcu.cp3406_cp5307_utilityappstartertemplate.data.model.CategoriesResponse
import au.edu.jcu.cp3406_cp5307_utilityappstartertemplate.data.model.MealDetailResponse
import au.edu.jcu.cp3406_cp5307_utilityappstartertemplate.data.model.MealsListResponse
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit service interface for the TheMealDB public API.
 * Base URL: https://www.themealdb.com/
 * All endpoints are free and require no API key.
 */
interface MealApiService {

    /** Retrieve all meal categories (Beef, Chicken, Seafood, Vegetarian, etc.) */
    @GET("api/json/v1/1/categories.php")
    suspend fun getCategories(): CategoriesResponse

    /** List all cuisine areas (Italian, Indian, Mexican, British, …) */
    @GET("api/json/v1/1/list.php")
    suspend fun getAreas(@Query("a") list: String = "list"): AreaListResponse

    /** Search meals whose name contains [name]. */
    @GET("api/json/v1/1/search.php")
    suspend fun searchByName(@Query("s") name: String): MealsListResponse

    /** Filter meals that include [ingredient] as a main ingredient. */
    @GET("api/json/v1/1/filter.php")
    suspend fun filterByIngredient(@Query("i") ingredient: String): MealsListResponse

    /** Filter meals belonging to [category] (e.g. "Chicken"). */
    @GET("api/json/v1/1/filter.php")
    suspend fun filterByCategory(@Query("c") category: String): MealsListResponse

    /** Filter meals from a specific cuisine [area] (e.g. "Italian"). */
    @GET("api/json/v1/1/filter.php")
    suspend fun filterByArea(@Query("a") area: String): MealsListResponse

    /** Look up the full recipe details for a meal by its ID. */
    @GET("api/json/v1/1/lookup.php")
    suspend fun getMealById(@Query("i") id: String): MealDetailResponse

    /** Return a single random meal with full recipe details. */
    @GET("api/json/v1/1/random.php")
    suspend fun getRandomMeal(): MealDetailResponse
}
