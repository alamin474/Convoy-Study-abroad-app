package com.example.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.Country
import com.example.data.model.FundingType
import com.example.ui.components.AdMobBanner
import com.example.ui.components.AdMobNativeAdCard
import com.example.ui.components.CountryCard
import com.example.ui.components.DestinationCard
import com.example.ui.components.ScholarshipCard
import com.example.ui.components.ScholarshipCardSkeleton
import com.example.ui.components.UniversityCard
import com.example.ui.components.UniversityCardSkeleton
import com.example.ui.viewmodel.HomeViewModel

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToUniversities: () -> Unit,
    onNavigateToScholarships: () -> Unit,
    onNavigateToUniversityDetail: (String) -> Unit,
    onNavigateToScholarshipDetail: (String) -> Unit,
    onNavigateToSupport: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedDestinationForExplorer by remember { mutableStateOf<Country?>(null) }

    // Prioritize Cyprus, Malaysia, Denmark, Greece at the front for Convoy major destinations
    val priorityCountryIds = listOf("c_cyprus", "c_malaysia", "c_denmark", "c_greece")
    val prioritizedCountries = remember(uiState.countries) {
        uiState.countries.sortedWith(
            compareByDescending<Country> { it.countryId in priorityCountryIds }
                .thenBy { priorityCountryIds.indexOf(it.countryId).takeIf { idx -> idx != -1 } ?: Int.MAX_VALUE }
        )
    }

    val priorityCountryNames = listOf("Cyprus", "Malaysia", "Denmark", "Greece")
    val affordableUniversities = remember(uiState.lowTuitionUniversities) {
        uiState.lowTuitionUniversities.sortedByDescending { it.country in priorityCountryNames }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen_lazy_column"),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // -----------------------------------------------------------------
        // 1. HERO SECTION
        // -----------------------------------------------------------------
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer,
                                MaterialTheme.colorScheme.surface
                            )
                        )
                    )
                    .padding(18.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = MaterialTheme.colorScheme.primary
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Public,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "CONVOY GLOBAL PLATFORM",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Convoy: Study Abroad",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                lineHeight = 30.sp
                            )
                            Text(
                                text = "Your Journey to Global Education Starts Here",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.primary,
                                lineHeight = 18.sp
                            )
                        }

                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surface,
                            shadowElevation = 4.dp,
                            modifier = Modifier
                                .size(52.dp)
                                .padding(start = 8.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_convoy_symbol),
                                    contentDescription = "Convoy Logo",
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Founder Image Hero Visual Card
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("convoy_official_hero_banner")
                    ) {
                        Column {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(16f / 9f)
                                    .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp))
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.img_convoy_hero),
                                    contentDescription = "Convoy Founder holding tablet displaying GLOBAL OPPORTUNITIES",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .testTag("main_hero_founder_visual")
                                )

                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.verticalGradient(
                                                colors = listOf(
                                                    Color.Transparent,
                                                    Color.Black.copy(alpha = 0.8f)
                                                ),
                                                startY = 180f
                                            )
                                        )
                                )

                                Surface(
                                    shape = RoundedCornerShape(20.dp),
                                    color = Color(0xFF00E5FF),
                                    shadowElevation = 6.dp,
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(12.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = Color(0xFF0F172A),
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "GLOBAL OPPORTUNITIES",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFF0F172A),
                                            letterSpacing = 0.5.sp
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xCC0F172A),
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(12.dp)
                                ) {
                                    Text(
                                        text = "👨‍💼 Convoy Global Higher Education Network",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            // CTA Action Buttons under Hero Image
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "‘স্বপ্ন পূরণ করুন, বিশ্বমানের শিক্ষায়’",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Button(
                                        onClick = onNavigateToUniversities,
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("hero_explore_universities_cta")
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.School,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Explore Universities",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }

                                    OutlinedButton(
                                        onClick = onNavigateToScholarships,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("hero_find_scholarships_cta")
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.WorkspacePremium,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Find Scholarships",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Global Search Bar Trigger
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = {
                            viewModel.onSearchQueryChange(it)
                            if (it.isNotEmpty()) onNavigateToUniversities()
                        },
                        placeholder = {
                            Text(
                                "Search universities, scholarships, or countries...",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("home_search_input")
                    )
                }
            }
        }

        // -----------------------------------------------------------------
        // 2. EXPLORE BY COUNTRY
        // -----------------------------------------------------------------
        item {
            Column(modifier = Modifier.padding(top = 22.dp)) {
                SectionHeader(
                    title = "Explore by Country 🌍",
                    subtitle = "Cyprus, Malaysia, Denmark, Greece & top study destinations",
                    onSeeAllClick = onNavigateToUniversities
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(prioritizedCountries, key = { it.countryId }) { country ->
                        CountryCard(
                            country = country,
                            onClick = {
                                selectedDestinationForExplorer = country
                                onNavigateToUniversities()
                            },
                            modifier = Modifier.testTag("explore_country_${country.countryId}")
                        )
                    }
                }
            }
        }

        // -----------------------------------------------------------------
        // 3. FEATURED UNIVERSITIES
        // -----------------------------------------------------------------
        item {
            Column(modifier = Modifier.padding(top = 24.dp)) {
                SectionHeader(
                    title = "Featured Universities 🎓",
                    subtitle = "Top-ranked global institutions accepting international applications",
                    onSeeAllClick = onNavigateToUniversities
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(uiState.featuredUniversities, key = { it.id }) { university ->
                        UniversityCard(
                            university = university,
                            onClick = { onNavigateToUniversityDetail(university.id) },
                            onBookmarkToggle = { viewModel.toggleUniversityBookmark(university.id) },
                            modifier = Modifier.width(280.dp)
                        )
                    }
                }
            }
        }

        // -----------------------------------------------------------------
        // 4. FULLY FUNDED SCHOLARSHIPS
        // -----------------------------------------------------------------
        item {
            Column(modifier = Modifier.padding(top = 24.dp)) {
                SectionHeader(
                    title = "Fully Funded Scholarships 🎁",
                    subtitle = "Grants, tuition waivers & government scholarships",
                    onSeeAllClick = onNavigateToScholarships
                )

                Spacer(modifier = Modifier.height(12.dp))

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (uiState.isLoading) {
                        items(3) {
                            ScholarshipCardSkeleton(modifier = Modifier.width(300.dp))
                        }
                    } else {
                        items(uiState.featuredScholarships, key = { it.id }) { scholarship ->
                            ScholarshipCard(
                                scholarship = scholarship,
                                onClick = { onNavigateToScholarshipDetail(scholarship.id) },
                                onSaveToggle = { viewModel.toggleScholarshipSave(scholarship.id) },
                                modifier = Modifier.width(300.dp)
                            )
                        }
                    }
                }
            }
        }

        // -----------------------------------------------------------------
        // 5. AFFORDABLE UNIVERSITIES
        // -----------------------------------------------------------------
        if (uiState.isLoading || affordableUniversities.isNotEmpty()) {
            item {
                Column(modifier = Modifier.padding(top = 24.dp)) {
                    SectionHeader(
                        title = "Affordable Universities 💶",
                        subtitle = "Low tuition in Cyprus, Malaysia, Denmark & Greece from €0 - €3,000 / year",
                        onSeeAllClick = onNavigateToUniversities
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (uiState.isLoading) {
                            items(3) {
                                UniversityCardSkeleton(modifier = Modifier.width(280.dp))
                            }
                        } else {
                            items(affordableUniversities, key = { it.id }) { uni ->
                                UniversityCard(
                                    university = uni,
                                    onClick = { onNavigateToUniversityDetail(uni.id) },
                                    onBookmarkToggle = { viewModel.toggleUniversityBookmark(uni.id) },
                                    modifier = Modifier.width(280.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // -----------------------------------------------------------------
        // 6. DESTINATION EXPLORER
        // -----------------------------------------------------------------
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Destination Explorer 🗺️",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Explore universities, tuition, scholarships, requirements & student life",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Interactive Destination Grid / Selector
                val topDestinations = prioritizedCountries.take(4)
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    topDestinations.forEach { country ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedDestinationForExplorer = if (selectedDestinationForExplorer?.countryId == country.countryId) null else country },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = country.flagEmoji, fontSize = 24.sp)
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = country.name,
                                                    fontSize = 16.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                if (country.isLowTuitionDestination) {
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Surface(
                                                        shape = RoundedCornerShape(6.dp),
                                                        color = MaterialTheme.colorScheme.primaryContainer
                                                    ) {
                                                        Text(
                                                            text = "Low Tuition",
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = MaterialTheme.colorScheme.primary,
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                }
                                            }
                                            Text(
                                                text = "${country.universityCount}+ Universities • Avg. ${country.avgTuitionPerYear}",
                                                fontSize = 12.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }

                                    Icon(
                                        imageVector = if (selectedDestinationForExplorer?.countryId == country.countryId) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = "Expand",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }

                                // Expanded Details View
                                AnimatedVisibility(visible = selectedDestinationForExplorer?.countryId == country.countryId) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 12.dp)
                                    ) {
                                        HorizontalDivider(modifier = Modifier.padding(bottom = 12.dp))

                                        // 6 Explorer Pillars Grid
                                        FlowRow(
                                            maxItemsInEachRow = 2,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            ExplorerPillarItem(
                                                icon = Icons.Default.School,
                                                title = "Universities",
                                                value = "${country.universityCount}+ Accredited",
                                                modifier = Modifier.weight(1f)
                                            )
                                            ExplorerPillarItem(
                                                icon = Icons.Default.Euro,
                                                title = "Tuition Fees",
                                                value = country.tuitionRange,
                                                modifier = Modifier.weight(1f)
                                            )
                                            ExplorerPillarItem(
                                                icon = Icons.Default.WorkspacePremium,
                                                title = "Scholarships",
                                                value = country.scholarshipAvailability,
                                                modifier = Modifier.weight(1f)
                                            )
                                            ExplorerPillarItem(
                                                icon = Icons.Default.Assignment,
                                                title = "Visa & Requirements",
                                                value = country.studentVisaOverview,
                                                modifier = Modifier.weight(1f)
                                            )
                                            ExplorerPillarItem(
                                                icon = Icons.Default.Work,
                                                title = "Work Rights",
                                                value = country.partTimeWorkInfo,
                                                modifier = Modifier.weight(1f)
                                            )
                                            ExplorerPillarItem(
                                                icon = Icons.Default.Info,
                                                title = "Study Info",
                                                value = country.livingCostOverview,
                                                modifier = Modifier.weight(1f)
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(12.dp))

                                        Button(
                                            onClick = onNavigateToUniversities,
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("View All ${country.name} Universities & Programs")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // -----------------------------------------------------------------
        // 7. UPCOMING DEADLINES
        // -----------------------------------------------------------------
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Upcoming Deadlines ⏰",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Don't miss key scholarship and admission windows",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                uiState.upcomingDeadlines.forEach { deadline ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(text = deadline.flagEmoji, fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = deadline.title,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${deadline.institution} • ${deadline.category}",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "${deadline.daysRemaining} days left",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // -----------------------------------------------------------------
        // 8. WHY CONVOY
        // -----------------------------------------------------------------
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp)) {
                Text(
                    text = "Why Choose Convoy 🛡️",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Comprehensive end-to-end support for your global education journey",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        WhyConvoyCard(
                            icon = Icons.Default.Search,
                            title = "University Discovery",
                            description = "Search & compare 1,000+ accredited global programs",
                            modifier = Modifier.weight(1f)
                        )
                        WhyConvoyCard(
                            icon = Icons.Default.WorkspacePremium,
                            title = "Scholarship Discovery",
                            description = "Access fully funded grants, merit awards & waivers",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        WhyConvoyCard(
                            icon = Icons.Default.SupportAgent,
                            title = "Application Assistance",
                            description = "Step-by-step guidance from experienced counselors",
                            modifier = Modifier.weight(1f)
                        )
                        WhyConvoyCard(
                            icon = Icons.Default.UploadFile,
                            title = "Document Submission",
                            description = "Direct, secure document portal verification",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        WhyConvoyCard(
                            icon = Icons.Default.TrackChanges,
                            title = "Application Tracking",
                            description = "Real-time updates on your admission status",
                            modifier = Modifier.weight(1f)
                        )
                        WhyConvoyCard(
                            icon = Icons.Default.HeadsetMic,
                            title = "Expert Counselling",
                            description = "Personalized 1-on-1 guidance for visa & enrollment",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Non-intrusive Discovery Native Ad Card
        item {
            AdMobNativeAdCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        // -----------------------------------------------------------------
        // 9. START YOUR APPLICATION
        // -----------------------------------------------------------------
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.RocketLaunch,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Ready to Start Your Journey?",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Apply to top global universities with full Convoy support and expert guidance.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onNavigateToUniversities,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("start_application_cta_button")
                    ) {
                        Text(
                            text = "Start Application",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ExplorerPillarItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = value,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun WhyConvoyCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 14.sp
            )
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    subtitle: String,
    onSeeAllClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { onSeeAllClick() }
        ) {
            Text(
                text = "See All",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "See All",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

