# Recipe Finder — CP3406 Utility App

An at-a-glance Android recipe utility that helps you discover meals based on ingredients you already have, browse by cuisine or dietary category, and get spontaneous inspiration with a single tap.

---

## Features

| Feature | Details |
|---|---|
| **Ingredient search** | Type any ingredient (e.g. "chicken", "garlic") to find matching recipes |
| **Meal name search** | Falls back to name search if no ingredient match is found |
| **Category browse** | Home screen shows all TheMealDB categories (Beef, Seafood, Vegetarian, Vegan, Dessert, …) as a visual grid |
| **Cuisine filter** | Filter tab lets you narrow by cuisine area (Italian, Indian, Mexican, British, …) |
| **Full recipe detail** | Tap any meal card to see the complete ingredient list + step-by-step instructions |
| **Surprise Me!** | Fetches a random recipe in one tap — great for meal planning inspiration |

---

## Architecture

The app follows the **MVVM + Repository** pattern recommended by Google for modern Android:

```
UI Layer
  MainActivity.kt        → Scaffold, bottom nav, animated transitions
  UtilityScreen          → Search bar, category home grid, results grid, meal detail
  SettingsScreen         → Category & cuisine FilterChip rows + Apply button

ViewModel Layer
  RecipeViewModel        → All UI state, settings, and coroutine-scoped data fetches

Data Layer
  MealRepository         → Single source of truth; wraps all API calls in Result<T>
  MealApiService         → Retrofit interface for TheMealDB (8 endpoints)
  RetrofitClient         → OkHttp + Retrofit singleton (manual DI entry point)

Model
  WeatherModels.kt       → MealCategory, MealSummary, MealDetail, RecipeUiState
```

Dependency injection is handled manually via `RecipeViewModel.Factory` and constructor injection into `MealRepository` — demonstrating the DI pattern without requiring annotation processors.

---

## Technology Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose + Material Design 3
- **Image loading:** Coil (`coil-compose`)
- **Networking:** Retrofit 2 + Gson + OkHttp
- **Async:** Kotlin Coroutines + `viewModelScope`
- **State:** `StateFlow` + `collectAsState()`
- **Architecture:** MVVM + Repository
- **API:** [TheMealDB](https://www.themealdb.com/) — free, no key required
- **Min SDK:** 24 (Android 7.0)

---

## API Endpoints Used

Base URL: `https://www.themealdb.com/`

| Purpose | Endpoint |
|---|---|
| Category list | `GET /api/json/v1/1/categories.php` |
| Area/cuisine list | `GET /api/json/v1/1/list.php?a=list` |
| Search by name | `GET /api/json/v1/1/search.php?s={query}` |
| Filter by ingredient | `GET /api/json/v1/1/filter.php?i={ingredient}` |
| Filter by category | `GET /api/json/v1/1/filter.php?c={category}` |
| Filter by area | `GET /api/json/v1/1/filter.php?a={area}` |
| Full meal detail | `GET /api/json/v1/1/lookup.php?i={id}` |
| Random meal | `GET /api/json/v1/1/random.php` |

---

## Project Structure

```
app/src/main/java/.../
├── data/
│   ├── model/WeatherModels.kt         # MealCategory, MealSummary, MealDetail, RecipeUiState
│   ├── remote/WeatherApiService.kt    # Retrofit interface — 8 TheMealDB endpoints
│   ├── remote/RetrofitClient.kt       # OkHttp + Retrofit singleton (manual DI)
│   ├── remote/GeocodingApiService.kt  # (stub — not used in this version)
│   └── repository/WeatherRepository.kt # MealRepository — all data access
├── ui/
│   ├── viewmodel/WeatherViewModel.kt  # RecipeViewModel with StateFlow state
│   └── theme/
└── MainActivity.kt                    # UtilityApp, UtilityScreen, SettingsScreen
```

---

## Key Concepts Covered (Weeks 1–5)

| Week | Concept | Implementation |
|------|---------|----------------|
| 1 | Kotlin + Android Studio | All `.kt` files |
| 2 | Jetpack Compose layouts | `LazyVerticalGrid`, `LazyColumn`, `Row`, `Card`, `AnimatedContent` |
| 3 | Material Design 3 | `FilterChip`, `TopAppBar`, `NavigationBar`, `Surface`, `MaterialTheme` |
| 4 | ViewModel + Repository + DI | `RecipeViewModel`, `MealRepository`, `RetrofitClient`, `Factory` pattern |
| 5 | Retrofit + Coroutines | `MealApiService`, `viewModelScope`, `StateFlow`, `Result<T>` |

---

## Setup

1. Clone the repository
2. Open the `App/` folder in Android Studio (Electric Eel or newer)
3. Sync Gradle — all dependencies download automatically
4. Run on an emulator or physical device with Android 7.0+ and internet access

No API keys or configuration required.
