package com.example.ui.screens.scholarships

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.example.data.model.FundingType
import com.example.ui.components.AdMobNativeAdCard
import com.example.ui.components.ScholarshipCard
import com.example.ui.components.ScholarshipCardSkeleton
import com.example.ui.viewmodel.ScholarshipsViewModel

@Composable
fun ScholarshipsScreen(
    viewModel: ScholarshipsViewModel,
    onNavigateToDetail: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("scholarships_screen_root")
    ) {
        // Header & Search Box
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Global Scholarships",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Discover fully-funded grants, government fellowships, and tuition waivers",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.onSearchQueryChange(it) },
                    placeholder = { Text("Search scholarships by title, country, or provider...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotEmpty()) {
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
                        .testTag("scholarships_search_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Category Chips (Fully Funded, Merit, Need-Based, Government, University)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(end = 16.dp)
                ) {
                    items(FundingType.entries) { type ->
                        val isSelected = uiState.selectedFundingType == type
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.onFundingTypeSelect(type) },
                            label = { Text(type.label, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier.testTag("funding_type_${type.name}")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Country & Degree Filters Row
                val countriesList = listOf("All", "United Kingdom", "Germany", "USA", "Canada", "Australia")
                val degreeList = listOf("All", "Master's Degree", "PhD", "Bachelor's Degree")
                val deadlineList = listOf("All", "2026 Deadlines", "2027 Deadlines")

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(end = 16.dp)
                ) {
                    item {
                        Text(
                            text = "Country:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 8.dp, end = 2.dp)
                        )
                    }
                    items(countriesList) { country ->
                        val isSelected = uiState.selectedCountry == country
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.onCountrySelect(country) },
                            label = { Text(country, fontSize = 11.sp) }
                        )
                    }
                    item {
                        Text(
                            text = "Degree:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 8.dp, start = 8.dp, end = 2.dp)
                        )
                    }
                    items(degreeList) { degree ->
                        val isSelected = uiState.selectedDegree == degree
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.onDegreeSelect(degree) },
                            label = { Text(degree, fontSize = 11.sp) }
                        )
                    }
                    item {
                        Text(
                            text = "Deadline:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 8.dp, start = 8.dp, end = 2.dp)
                        )
                    }
                    items(deadlineList) { deadlineFilter ->
                        val isSelected = uiState.selectedDeadlineFilter == deadlineFilter
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.onDeadlineFilterSelect(deadlineFilter) },
                            label = { Text(deadlineFilter, fontSize = 11.sp) }
                        )
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
                        .testTag("scholarships_skeleton_list"),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(4) {
                        ScholarshipCardSkeleton()
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
                            text = "Unable to Load Scholarships",
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
            uiState.scholarships.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.WorkspacePremium,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No Scholarships Found",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Try searching with a different term or select 'All Types'.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("scholarships_list"),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    itemsIndexed(uiState.scholarships, key = { index, scholarship -> "${scholarship.id}_$index" }) { index, scholarship ->
                        val label = uiState.activeSponsoredMap[scholarship.scholarshipId] ?: uiState.activeSponsoredMap[scholarship.title]
                        ScholarshipCard(
                            scholarship = scholarship,
                            sponsoredLabel = label,
                            onClick = { onNavigateToDetail(scholarship.id) },
                            onSaveToggle = { viewModel.toggleSave(scholarship.id) }
                        )

                        if ((index + 1) % 6 == 0 && index < uiState.scholarships.size - 1) {
                            Spacer(modifier = Modifier.height(16.dp))
                            AdMobNativeAdCard()
                        }
                    }
                }
            }
        }
    }
}
