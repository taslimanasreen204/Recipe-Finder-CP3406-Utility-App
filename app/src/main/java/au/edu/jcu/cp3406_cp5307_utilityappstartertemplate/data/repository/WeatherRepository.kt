package au.edu.jcu.cp3406_cp5307_utilityappstartertemplate.data.repository

import au.edu.jcu.cp3406_cp5307_utilityappstartertemplate.data.model.MealCategory
import au.edu.jcu.cp3406_cp5307_utilityappstartertemplate.data.model.MealDetail
import au.edu.jcu.cp3406_cp5307_utilityappstartertemplate.data.model.MealSummary
import au.edu.jcu.cp3406_cp5307_utilityappstartertemplate.data.remote.MealApiService

/**
 * Repository that abstracts all TheMealDB data access.
 *
 * Follows the Repository pattern: the [RecipeViewModel] calls this class and
 * has no direct knowledge of Retrofit or HTTP details. The [MealApiService]
 * is injected via the constructor, making the repository easy to test with fakes.
 *
 * Each public function returns a [Result], letting callers distinguish between
 * a successful (possibly empty) response and a network/parsing failure.
 */
class MealRepository(private val api: MealApiService) {

    /** Fetch all meal categories (Beef, Chicken, Seafood, Vegetarian, …). */
    suspend fun getCategories(): Result<List<MealCategory>> = runCatching {
        api.getCategories().categories ?: emptyList()
    }

    /** Fetch all cuisine areas (Italian, Indian, Mexican, British, …). */
    suspend fun getAreas(): Result<List<String>> = runCatching {
        api.getAreas().meals?.map { it.strArea } ?: emptyList()
    }

    /**
     * Search for meals whose name contains [query].
     * TheMealDB returns null meals list when there are no results.
     */
    suspend fun searchByName(query: String): Result<List<MealSummary>> = runCatching {
        api.searchByName(query).meals ?: emptyList()
    }

    /**
     * Filter meals that feature [ingredient] as a main ingredient.
     * e.g. "chicken_breast", "garlic", "rice"
     */
    suspend fun filterByIngredient(ingredient: String): Result<List<MealSummary>> = runCatching {
        api.filterByIngredient(ingredient).meals ?: emptyList()
    }

    /** Filter meals belonging to [category] (e.g. "Seafood", "Vegetarian"). */
    suspend fun filterByCategory(category: String): Result<List<MealSummary>> = runCatching {
        api.filterByCategory(category).meals ?: emptyList()
    }

    /** Filter meals from cuisine [area] (e.g. "Italian", "Indian"). */
    suspend fun filterByArea(area: String): Result<List<MealSummary>> = runCatching {
        api.filterByArea(area).meals ?: emptyList()
    }

    /** Fetch the full recipe details for a specific meal by its [id]. */
    suspend fun getMealDetail(id: String): Result<MealDetail?> = runCatching {
        api.getMealById(id).meals?.firstOrNull()
    }

    /** Fetch a random full recipe — used for the "Surprise Me" feature. */
    suspend fun getRandomMeal(): Result<MealDetail?> = runCatching {
        api.getRandomMeal().meals?.firstOrNull()
    }
}
