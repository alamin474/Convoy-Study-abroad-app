package com.example.ui.screens.universities

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import com.example.data.model.TuitionCategory
import com.example.ui.components.AdMobNativeAdCard
import com.example.ui.components.UniversityCard
import com.example.ui.components.UniversityCardSkeleton
import com.example.ui.viewmodel.UniversitiesViewModel

@Composable
fun UniversitiesScreen(
    viewModel: UniversitiesViewModel,
    onNavigateToDetail: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val filter = uiState.filterState

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("universities_screen_root")
    ) {
        // Header & Search Box
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Discover Universities",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Find world-class higher education institutions across the globe",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = filter.searchQuery,
                    onValueChange = { viewModel.onSearchQueryChange(it) },
                    placeholder = { Text("Search by name, city, or program...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingIcon = {
                        if (filter.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                                Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("universities_search_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Country Filters Row
                Text(
                    text = "Country Filter",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(end = 16.dp)
                ) {
                    items(uiState.availableCountries) { country ->
                        val isSelected = filter.selectedCountry == country
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.onCountrySelect(country) },
                            label = { Text(country, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier.testTag("country_filter_$country")
                        )
                    }
                }

                // Advanced Filter Chips (Tuition, Ranking, Scholarships)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(top = 4.dp, end = 16.dp)
                ) {
                    // Tuition Filter
                    item {
                        FilterChip(
                            selected = filter.selectedTuition != null,
                            onClick = {
                                val next = when (filter.selectedTuition) {
                                    null -> TuitionCategory.UNDER_10K
                                    TuitionCategory.UNDER_10K -> TuitionCategory.FROM_10K_TO_25K
                                    TuitionCategory.FROM_10K_TO_25K -> TuitionCategory.OVER_25K
                                    TuitionCategory.OVER_25K -> null
                                }
                                viewModel.onTuitionSelect(next)
                            },
                            label = {
                                Text(
                                    text = filter.selectedTuition?.label ?: "Tuition: Any",
                                    fontSize = 11.sp
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }

                    // Ranking Filter
                    item {
                        FilterChip(
                            selected = filter.selectedMaxRanking != null,
                            onClick = {
                                val next = when (filter.selectedMaxRanking) {
                                    null -> 20
                                    20 -> 50
                                    50 -> 100
                                    else -> null
                                }
                                viewModel.onRankingSelect(next)
                            },
                            label = {
                                Text(
                                    text = if (filter.selectedMaxRanking == null) "Ranking: Any" else "Top ${filter.selectedMaxRanking} World",
                                    fontSize = 11.sp
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }

                    // Scholarship Only Filter
                    item {
                        FilterChip(
                            selected = filter.scholarshipOnly,
                            onClick = { viewModel.onScholarshipOnlyToggle(!filter.scholarshipOnly) },
                            label = { Text("Scholarships Available", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f),
                                selectedLabelColor = MaterialTheme.colorScheme.tertiary
                            )
                        )
                    }

                    // Clear All Filters
                    if (filter.selectedCountry != "All" || filter.selectedTuition != null || filter.selectedMaxRanking != null || filter.scholarshipOnly || filter.searchQuery.isNotEmpty()) {
                        item {
                            TextButton(onClick = { viewModel.clearFilters() }) {
                                Text("Reset Filters", fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }

        // List Content, Loading Shimmer, Error or Empty State
        when {
            uiState.isLoading -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("universities_skeleton_list"),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(4) {
                        UniversityCardSkeleton()
                    }
                }
            }
            uiState.errorMessage != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Unable to Load Universities",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = uiState.errorMessage ?: "Something went wrong while connecting.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.retry() }) {
                            Text("Retry")
                        }
                    }
                }
            }
            uiState.universities.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.School,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No Universities Found",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Try adjusting your search terms or clearing active filters.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        TextButton(onClick = { viewModel.clearFilters() }) {
                            Text("Clear All Filters")
                        }
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("universities_list"),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    itemsIndexed(uiState.universities, key = { index, uni -> "${uni.id}_$index" }) { index, uni ->
                        val label = uiState.activeSponsoredMap[uni.universityId] ?: uiState.activeSponsoredMap[uni.name]
                        UniversityCard(
                            university = uni,
                            sponsoredLabel = label,
                            onClick = { onNavigateToDetail(uni.id) },
                            onBookmarkToggle = { viewModel.toggleBookmark(uni.id) }
                        )

                        if ((index + 1) % 6 == 0 && index < uiState.universities.size - 1) {
                            Spacer(modifier = Modifier.height(16.dp))
                            AdMobNativeAdCard()
                        }
                    }
                }
            }
        }
    }
}
