package au.edu.jcu.cp3406_cp5307_utilityappstartertemplate.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import au.edu.jcu.cp3406_cp5307_utilityappstartertemplate.data.model.MealCategory
import au.edu.jcu.cp3406_cp5307_utilityappstartertemplate.data.model.MealDetail
import au.edu.jcu.cp3406_cp5307_utilityappstartertemplate.data.model.MealSummary
import au.edu.jcu.cp3406_cp5307_utilityappstartertemplate.data.model.RecipeUiState
import au.edu.jcu.cp3406_cp5307_utilityappstartertemplate.data.remote.RetrofitClient
import au.edu.jcu.cp3406_cp5307_utilityappstartertemplate.data.repository.MealRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the Recipe Finder app.
 *
 * Owns all UI-visible state and survives configuration changes (screen rotation).
 * Exposes immutable [StateFlow]s collected by the Compose UI; all mutations happen
 * inside [viewModelScope] coroutines so the main thread is never blocked.
 *
 * Settings (selected category / area) are held in-memory only — as per the
 * assignment brief ("settings do not need to be persistent").
 *
 * @param repository Injected [MealRepository] — the single source of truth for data.
 */
class RecipeViewModel(private val repository: MealRepository) : ViewModel() {

    // ── Search / browse results ───────────────────────────────────────────────

    private val _uiState = MutableStateFlow<RecipeUiState>(RecipeUiState.Idle)
    val uiState: StateFlow<RecipeUiState> = _uiState.asStateFlow()

    // ── Full recipe shown when user taps a meal card ──────────────────────────

    private val _selectedMeal = MutableStateFlow<MealDetail?>(null)
    val selectedMeal: StateFlow<MealDetail?> = _selectedMeal.asStateFlow()

    private val _isLoadingDetail = MutableStateFlow(false)
    val isLoadingDetail: StateFlow<Boolean> = _isLoadingDetail.asStateFlow()

    // ── Filter metadata loaded on startup ─────────────────────────────────────

    private val _categories = MutableStateFlow<List<MealCategory>>(emptyList())
    val categories: StateFlow<List<MealCategory>> = _categories.asStateFlow()

    private val _areas = MutableStateFlow<List<String>>(emptyList())
    val areas: StateFlow<List<String>> = _areas.asStateFlow()

    // ── Settings state (Settings screen drives these) ─────────────────────────

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _selectedArea = MutableStateFlow("All")
    val selectedArea: StateFlow<String> = _selectedArea.asStateFlow()

    // ── Init ──────────────────────────────────────────────────────────────────

    init {
        loadFilterOptions()
    }

    /** Load category list and area list for the Settings screen chips. */
    private fun loadFilterOptions() {
        viewModelScope.launch {
            repository.getCategories().onSuccess { _categories.value = it }
            repository.getAreas().onSuccess { _areas.value = it.sorted() }
        }
    }

    // ── Public actions called from UI ─────────────────────────────────────────

    /**
     * Search meals that include [ingredient].
     * The query is also attempted as a name search so results are maximised.
     */
    fun searchByIngredient(ingredient: String) {
        if (ingredient.isBlank()) return
        viewModelScope.launch {
            _uiState.value = RecipeUiState.Loading
            _selectedMeal.value = null

            // Try ingredient filter first; fall back to name search
            val result = repository.filterByIngredient(ingredient.trim())
                .getOrDefault(emptyList())
                .ifEmpty {
                    repository.searchByName(ingredient.trim()).getOrDefault(emptyList())
                }

            _uiState.value = if (result.isEmpty()) {
                RecipeUiState.Empty(ingredient)
            } else {
                RecipeUiState.Results(result)
            }
        }
    }

    /** Browse all meals in [category] (called from Settings screen). */
    fun browseByCategory(category: String) {
        viewModelScope.launch {
            _uiState.value = RecipeUiState.Loading
            _selectedMeal.value = null
            val result = repository.filterByCategory(category).getOrDefault(emptyList())
            _uiState.value = if (result.isEmpty()) RecipeUiState.Empty(category)
                             else RecipeUiState.Results(result)
        }
    }

    /** Browse all meals from cuisine [area] (called from Settings screen). */
    fun browseByArea(area: String) {
        viewModelScope.launch {
            _uiState.value = RecipeUiState.Loading
            _selectedMeal.value = null
            val result = repository.filterByArea(area).getOrDefault(emptyList())
            _uiState.value = if (result.isEmpty()) RecipeUiState.Empty(area)
                             else RecipeUiState.Results(result)
        }
    }

    /** Fetch full recipe detail when the user taps a [MealSummary] card. */
    fun openMeal(meal: MealSummary) {
        viewModelScope.launch {
            _isLoadingDetail.value = true
            repository.getMealDetail(meal.idMeal)
                .onSuccess { _selectedMeal.value = it }
                .onFailure { _selectedMeal.value = null }
            _isLoadingDetail.value = false
        }
    }

    /** Close the detail view and return to the results list. */
    fun closeDetail() {
        _selectedMeal.value = null
    }

    /** Fetch a random recipe — the "Surprise Me" feature. */
    fun surpriseMe() {
        viewModelScope.launch {
            _isLoadingDetail.value = true
            _selectedMeal.value = null
            repository.getRandomMeal()
                .onSuccess { _selectedMeal.value = it }
            _isLoadingDetail.value = false
        }
    }

    /** Update the category selection from the Settings screen. */
    fun setCategory(category: String) { _selectedCategory.value = category }

    /** Update the area selection from the Settings screen. */
    fun setArea(area: String) { _selectedArea.value = area }

    /** Apply the current Settings filters and navigate results to main screen. */
    fun applyFilters() {
        val cat = _selectedCategory.value
        val area = _selectedArea.value
        when {
            cat != "All" -> browseByCategory(cat)
            area != "All" -> browseByArea(area)
            else -> { _uiState.value = RecipeUiState.Idle }
        }
    }

    // ── Factory (manual DI) ───────────────────────────────────────────────────

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return RecipeViewModel(
                    MealRepository(RetrofitClient.mealApiService)
                ) as T
            }
        }
    }
}
