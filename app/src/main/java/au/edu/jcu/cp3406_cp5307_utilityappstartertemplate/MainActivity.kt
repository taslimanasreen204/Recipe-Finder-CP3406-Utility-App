package au.edu.jcu.cp3406_cp5307_utilityappstartertemplate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import au.edu.jcu.cp3406_cp5307_utilityappstartertemplate.data.model.MealDetail
import au.edu.jcu.cp3406_cp5307_utilityappstartertemplate.data.model.MealSummary
import au.edu.jcu.cp3406_cp5307_utilityappstartertemplate.data.model.RecipeUiState
import au.edu.jcu.cp3406_cp5307_utilityappstartertemplate.ui.theme.CP3406_CP5603UtilityAppStarterTemplateTheme
import au.edu.jcu.cp3406_cp5307_utilityappstartertemplate.ui.viewmodel.RecipeViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest

// ─── Activity ─────────────────────────────────────────────────────────────────

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CP3406_CP5603UtilityAppStarterTemplateTheme {
                UtilityApp()
            }
        }
    }
}

// ─── Root scaffold ─────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UtilityApp(vm: RecipeViewModel = viewModel(factory = RecipeViewModel.Factory)) {
    var selectedTab by rememberSaveable { mutableStateOf("Utility") }
    val selectedMeal by vm.selectedMeal.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (selectedMeal != null) selectedMeal!!.strMeal else "Recipe Finder",
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    if (selectedMeal != null) {
                        IconButton(onClick = { vm.closeDetail() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, "Recipes") },
                    label = { Text("Recipes") },
                    selected = selectedTab == "Utility",
                    onClick = { selectedTab = "Utility" }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Settings, "Settings") },
                    label = { Text("Filters") },
                    selected = selectedTab == "Settings",
                    onClick = { selectedTab = "Settings" }
                )
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding)) {
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(200)) },
                label = "tab_anim"
            ) { tab ->
                when (tab) {
                    "Utility"  -> UtilityScreen(vm)
                    "Settings" -> SettingsScreen(vm, onApply = { selectedTab = "Utility" })
                }
            }
        }
    }
}

// ─── Main Screen (Recipe search + results + detail) ───────────────────────────

@Composable
fun UtilityScreen(vm: RecipeViewModel = viewModel(factory = RecipeViewModel.Factory)) {
    val uiState        by vm.uiState.collectAsState()
    val selectedMeal   by vm.selectedMeal.collectAsState()
    val isLoadingDetail by vm.isLoadingDetail.collectAsState()

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        // If a meal is selected, show its full recipe
        if (selectedMeal != null) {
            MealDetailView(meal = selectedMeal!!, onBack = { vm.closeDetail() })
            return@Surface
        }
        // Loading detail spinner overlay
        if (isLoadingDetail) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Surface
        }

        Column(Modifier.fillMaxSize()) {
            SearchBar(vm)
            AnimatedContent(
                targetState = uiState,
                transitionSpec = { fadeIn(tween(250)) togetherWith fadeOut(tween(250)) },
                label = "state_anim",
                modifier = Modifier.weight(1f)
            ) { state ->
                when (state) {
                    is RecipeUiState.Idle    -> WelcomeScreen(vm)
                    is RecipeUiState.Loading -> LoadingView()
                    is RecipeUiState.Results -> MealGrid(state.meals) { vm.openMeal(it) }
                    is RecipeUiState.Empty   -> EmptyView(state.query)
                    is RecipeUiState.Error   -> ErrorView(state.message) { vm.surpriseMe() }
                }
            }
        }
    }
}

// ─── Search bar ───────────────────────────────────────────────────────────────

@Composable
private fun SearchBar(vm: RecipeViewModel) {
    var query by rememberSaveable { mutableStateOf("") }
    val focus = LocalFocusManager.current

    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            label = { Text("Search by ingredient or meal name") },
            trailingIcon = {
                IconButton(onClick = {
                    vm.searchByIngredient(query)
                    focus.clearFocus()
                }) {
                    Icon(Icons.Default.Search, "Search")
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = {
                vm.searchByIngredient(query)
                focus.clearFocus()
            }),
            modifier = Modifier.fillMaxWidth()
        )
        FilledTonalButton(
            onClick = { vm.surpriseMe() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("🎲  Surprise Me!")
        }
    }
}

// ─── Welcome / home screen ────────────────────────────────────────────────────

@Composable
private fun WelcomeScreen(vm: RecipeViewModel) {
    val categories by vm.categories.collectAsState()

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                "What would you like to cook today?",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
        item {
            Text(
                "Browse by category",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        // Category grid — 3 columns
        items(categories.chunked(3)) { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { cat ->
                    CategoryChipCard(
                        label = cat.strCategory,
                        thumbUrl = cat.strCategoryThumb,
                        modifier = Modifier.weight(1f),
                        onClick = { vm.browseByCategory(cat.strCategory) }
                    )
                }
                // Pad incomplete rows
                repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun CategoryChipCard(
    label: String,
    thumbUrl: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(8.dp)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(thumbUrl).crossfade(true).build(),
                contentDescription = label,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(8.dp))
            )
            Spacer(Modifier.height(4.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ─── Results grid ─────────────────────────────────────────────────────────────

@Composable
private fun MealGrid(meals: List<MealSummary>, onTap: (MealSummary) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(meals) { meal -> MealCard(meal = meal, onClick = { onTap(meal) }) }
    }
}

@Composable
private fun MealCard(meal: MealSummary, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Column {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(meal.strMealThumb).crossfade(true).build(),
                contentDescription = meal.strMeal,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
            )
            Text(
                text = meal.strMeal,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

// ─── Full recipe detail view ──────────────────────────────────────────────────

@Composable
private fun MealDetailView(meal: MealDetail, onBack: () -> Unit) {
    LazyColumn(Modifier.fillMaxSize()) {
        // Hero image
        item {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(meal.strMealThumb).crossfade(true).build(),
                contentDescription = meal.strMeal,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
            )
        }

        // Metadata row
        item {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                meal.strCategory?.let { MetaChip("🍽 $it") }
                meal.strArea?.let { MetaChip("🌍 $it") }
            }
        }

        // Ingredients
        item {
            SectionTitle("Ingredients")
        }
        items(meal.ingredients()) { (measure, ingredient) ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("•", color = MaterialTheme.colorScheme.primary, fontSize = 18.sp)
                Spacer(Modifier.width(8.dp))
                Text(
                    buildString {
                        if (measure.isNotBlank()) append("$measure ")
                        append(ingredient)
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // Instructions
        item {
            SectionTitle("Instructions")
            Text(
                text = meal.strInstructions ?: "No instructions available.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun MetaChip(label: String) {
    Surface(
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.secondaryContainer,
        tonalElevation = 1.dp
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 4.dp)
    )
    HorizontalDivider(Modifier.padding(horizontal = 16.dp))
}

// ─── Utility composables ───────────────────────────────────────────────────────

@Composable
private fun LoadingView() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(Modifier.size(48.dp))
    }
}

@Composable
private fun EmptyView(query: String) {
    Column(
        Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("🔍", fontSize = 48.sp)
        Spacer(Modifier.height(12.dp))
        Text(
            "No recipes found for \"$query\"",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Try a different ingredient or browse by category in the Filters tab.",
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun ErrorView(message: String, onRetry: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("⚠️", fontSize = 48.sp)
        Spacer(Modifier.height(12.dp))
        Text(message, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.error)
        Spacer(Modifier.height(20.dp))
        Button(onClick = onRetry) { Text("Try a Random Recipe") }
    }
}

// ─── Settings Screen (category + area filters) ────────────────────────────────

@Composable
fun SettingsScreen(
    vm: RecipeViewModel = viewModel(factory = RecipeViewModel.Factory),
    onApply: () -> Unit = {}
) {
    val categories      by vm.categories.collectAsState()
    val areas           by vm.areas.collectAsState()
    val selectedCat     by vm.selectedCategory.collectAsState()
    val selectedArea    by vm.selectedArea.collectAsState()

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Filters", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(
            "Choose a category or cuisine, then tap Browse to load matching meals.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )

        HorizontalDivider()

        // ── Category chips ─────────────────────────────────────────────────
        Text("Meal Category", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = selectedCat == "All",
                onClick = { vm.setCategory("All") },
                label = { Text("All") }
            )
            categories.forEach { cat ->
                FilterChip(
                    selected = selectedCat == cat.strCategory,
                    onClick = { vm.setCategory(cat.strCategory) },
                    label = { Text(cat.strCategory) }
                )
            }
        }

        HorizontalDivider()

        // ── Cuisine / area chips ───────────────────────────────────────────
        Text("Cuisine", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = selectedArea == "All",
                onClick = { vm.setArea("All") },
                label = { Text("All") }
            )
            areas.forEach { area ->
                FilterChip(
                    selected = selectedArea == area,
                    onClick = { vm.setArea(area) },
                    label = { Text(area) }
                )
            }
        }

        HorizontalDivider()

        // ── Apply button ───────────────────────────────────────────────────
        Button(
            onClick = { vm.applyFilters(); onApply() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Browse Recipes →")
        }
    }
}

// ─── Preview ──────────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
fun MealCardPreview() {
    CP3406_CP5603UtilityAppStarterTemplateTheme {
        MealCard(
            meal = MealSummary("1", "Spaghetti Carbonara", ""),
            onClick = {}
        )
    }
}
