package com.example.ui.screens.admin

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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AnalyticsDateFilter
import com.example.data.model.BusinessAnalyticsMetrics
import com.example.data.model.CountryPerformanceMetrics
import com.example.ui.viewmodel.AdminTab
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConvoyAnalyticsDashboardContent(
    metrics: BusinessAnalyticsMetrics,
    selectedFilter: AnalyticsDateFilter,
    onFilterSelected: (AnalyticsDateFilter, Long?, Long?) -> Unit,
    onTabNavigate: (AdminTab) -> Unit
) {
    var showCustomDateModal by remember { mutableStateOf(false) }
    var startInputText by remember { mutableStateOf("2026-01-01") }
    var endInputText by remember { mutableStateOf("2026-12-31") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("convoy_analytics_dashboard_view"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        // Header & Date Filter Bar
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.Insights,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Convoy Business Analytics",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                            Text(
                                text = "Real database events & transaction analytics",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Shield,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "PII SAFE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Date Filters Row
                    Text(
                        text = "Time Period Filter:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(AnalyticsDateFilter.entries.toTypedArray()) { filter ->
                            FilterChip(
                                selected = selectedFilter == filter,
                                onClick = {
                                    if (filter == AnalyticsDateFilter.CUSTOM) {
                                        showCustomDateModal = true
                                    } else {
                                        onFilterSelected(filter, null, null)
                                    }
                                },
                                label = { Text(filter.displayName) },
                                leadingIcon = if (selectedFilter == filter) {
                                    { Icon(imageVector = Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                                } else null
                            )
                        }
                    }
                }
            }
        }

        // Summary KPI Banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Tracked Business Yield",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "$${"%.2f".format(metrics.totalEstimatedRevenue)} USD",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Column {
                        Text(
                            text = "Submitted Applications",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = metrics.applicationsSubmitted.toString(),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Column {
                        Text(
                            text = "Completion Rate",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "${"%.1f".format(metrics.applicationCompletionRate)}%",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
            }
        }

        // 1. USER METRICS
        item {
            AnalyticsSectionCard(
                title = "USER METRICS",
                icon = Icons.Filled.Group,
                tagText = "STUDENT ACCOUNTS"
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AnalyticsKpiCard(
                        label = "Total Users",
                        value = metrics.totalUsers.toString(),
                        subtext = "Registered in DB",
                        icon = Icons.Filled.People,
                        modifier = Modifier.weight(1f),
                        onClick = { onTabNavigate(AdminTab.STUDENTS) }
                    )
                    AnalyticsKpiCard(
                        label = "New Users",
                        value = metrics.newUsers.toString(),
                        subtext = "Period Growth",
                        icon = Icons.Filled.PersonAdd,
                        modifier = Modifier.weight(1f),
                        onClick = { onTabNavigate(AdminTab.STUDENTS) }
                    )
                    AnalyticsKpiCard(
                        label = "Active Users",
                        value = metrics.activeUsers.toString(),
                        subtext = "Active in Apps/Docs",
                        icon = Icons.Filled.HowToReg,
                        modifier = Modifier.weight(1f),
                        onClick = { onTabNavigate(AdminTab.STUDENTS) }
                    )
                }
            }
        }

        // 2. DISCOVERY
        item {
            AnalyticsSectionCard(
                title = "DISCOVERY & ENGAGEMENT",
                icon = Icons.Filled.Explore,
                tagText = "PLATFORM ACTIVITY"
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AnalyticsKpiCard(
                        label = "University Views",
                        value = metrics.universityViews.toString(),
                        subtext = "Institution Detail",
                        icon = Icons.Filled.AccountBalance,
                        modifier = Modifier.weight(1f),
                        onClick = { onTabNavigate(AdminTab.UNIVERSITIES) }
                    )
                    AnalyticsKpiCard(
                        label = "Scholarship Views",
                        value = metrics.scholarshipViews.toString(),
                        subtext = "Funding Grants",
                        icon = Icons.Filled.School,
                        modifier = Modifier.weight(1f),
                        onClick = { onTabNavigate(AdminTab.SCHOLARSHIPS) }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AnalyticsKpiCard(
                        label = "Country Views",
                        value = metrics.countryViews.toString(),
                        subtext = "Destination Pages",
                        icon = Icons.Filled.Public,
                        modifier = Modifier.weight(1f),
                        onClick = { onTabNavigate(AdminTab.COUNTRIES) }
                    )
                    AnalyticsKpiCard(
                        label = "Search Activity",
                        value = metrics.searchActivityCount.toString(),
                        subtext = "Total Queries",
                        icon = Icons.Filled.Search,
                        modifier = Modifier.weight(1f)
                    )
                    AnalyticsKpiCard(
                        label = "Saved Unis",
                        value = metrics.savedUniversitiesCount.toString(),
                        subtext = "Bookmarked",
                        icon = Icons.Filled.Bookmark,
                        modifier = Modifier.weight(1f)
                    )
                }

                if (metrics.topSearches.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Frequent Student Searches:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    metrics.topSearches.forEach { (query, count) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "• \"$query\"",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "$count searches",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        // 3. LEAD METRICS
        item {
            AnalyticsSectionCard(
                title = "LEAD GENERATION",
                icon = Icons.Filled.FilterList,
                tagText = "STUDENT INTENT"
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AnalyticsKpiCard(
                        label = "Profile Creations",
                        value = metrics.profileCreations.toString(),
                        subtext = "Completed Onboarding",
                        icon = Icons.Filled.Badge,
                        modifier = Modifier.weight(1f),
                        onClick = { onTabNavigate(AdminTab.STUDENTS) }
                    )
                    AnalyticsKpiCard(
                        label = "Information Requests",
                        value = metrics.informationRequests.toString(),
                        subtext = "Inquiries & Leads",
                        icon = Icons.Filled.ContactMail,
                        modifier = Modifier.weight(1f),
                        onClick = { onTabNavigate(AdminTab.LEADS) }
                    )
                    AnalyticsKpiCard(
                        label = "Application Starts",
                        value = metrics.applicationStarts.toString(),
                        subtext = "Draft In Progress",
                        icon = Icons.Filled.Edit,
                        modifier = Modifier.weight(1f),
                        onClick = { onTabNavigate(AdminTab.APPLICATIONS) }
                    )
                }
            }
        }

        // 4. CONVERSION ANALYTICS
        item {
            AnalyticsSectionCard(
                title = "CONVERSION & APPLICATIONS",
                icon = Icons.Filled.AssignmentTurnedIn,
                tagText = "FUNNEL PERFORMANCE"
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AnalyticsKpiCard(
                        label = "Applications Submitted",
                        value = metrics.applicationsSubmitted.toString(),
                        subtext = "Completed & Verification",
                        icon = Icons.Filled.Send,
                        modifier = Modifier.weight(1f),
                        onClick = { onTabNavigate(AdminTab.APPLICATIONS) }
                    )
                    AnalyticsKpiCard(
                        label = "Completion Rate",
                        value = "${"%.1f".format(metrics.applicationCompletionRate)}%",
                        subtext = "Starts -> Submitted",
                        icon = Icons.Filled.CheckCircle,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Breakdown lists
                if (metrics.applicationsByCountry.isNotEmpty()) {
                    Text(
                        text = "Applications by Country:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    metrics.applicationsByCountry.forEach { (country, count) ->
                        ProgressBarRow(
                            label = country,
                            count = count,
                            total = metrics.applicationsSubmitted.coerceAtLeast(1)
                        )
                    }
                }

                if (metrics.applicationsByUniversity.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Applications by University:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    metrics.applicationsByUniversity.entries.take(5).forEach { (uni, count) ->
                        ProgressBarRow(
                            label = uni,
                            count = count,
                            total = metrics.applicationsSubmitted.coerceAtLeast(1)
                        )
                    }
                }

                if (metrics.applicationsByProgram.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Applications by Program:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    metrics.applicationsByProgram.entries.take(5).forEach { (prog, count) ->
                        ProgressBarRow(
                            label = prog,
                            count = count,
                            total = metrics.applicationsSubmitted.coerceAtLeast(1)
                        )
                    }
                }
            }
        }

        // 5. BUSINESS & REVENUE
        item {
            AnalyticsSectionCard(
                title = "BUSINESS & MONETIZATION",
                icon = Icons.Filled.MonetizationOn,
                tagText = "VERIFIED REVENUE"
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AnalyticsKpiCard(
                        label = "Service Requests",
                        value = metrics.serviceRequests.toString(),
                        subtext = "Counseling / Visa",
                        icon = Icons.Filled.SupportAgent,
                        modifier = Modifier.weight(1f),
                        onClick = { onTabNavigate(AdminTab.ASSISTANCE) }
                    )
                    AnalyticsKpiCard(
                        label = "Referral Conversions",
                        value = metrics.referralConversions.toString(),
                        subtext = "Approved Referrals",
                        icon = Icons.Filled.CardGiftcard,
                        modifier = Modifier.weight(1f),
                        onClick = { onTabNavigate(AdminTab.REFERRALS) }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AnalyticsKpiCard(
                        label = "Qualified Referrals",
                        value = metrics.qualifiedReferrals.toString(),
                        subtext = "Qualified Rewards",
                        icon = Icons.Filled.Verified,
                        modifier = Modifier.weight(1f),
                        onClick = { onTabNavigate(AdminTab.REFERRALS) }
                    )
                    AnalyticsKpiCard(
                        label = "Sponsored Listings",
                        value = metrics.sponsoredListingsCount.toString(),
                        subtext = "Active Packages",
                        icon = Icons.Filled.Campaign,
                        modifier = Modifier.weight(1f),
                        onClick = { onTabNavigate(AdminTab.SPONSORED_LISTINGS) }
                    )
                    AnalyticsKpiCard(
                        label = "Partner Apps",
                        value = metrics.verifiedPartnerApplications.toString(),
                        subtext = "Commission Eligible",
                        icon = Icons.Filled.Handshake,
                        modifier = Modifier.weight(1f),
                        onClick = { onTabNavigate(AdminTab.PARTNERS) }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Tracked Partner Commissions",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "$${"%.2f".format(metrics.totalCommissionsRevenue)} USD",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Disbursed Referral Rewards",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "$${"%.2f".format(metrics.totalReferralDisbursements)} USD",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }

        // 6. COUNTRY PERFORMANCE
        item {
            AnalyticsSectionCard(
                title = "COUNTRY PERFORMANCE",
                icon = Icons.Filled.Flag,
                tagText = "REGIONAL METRICS"
            ) {
                Text(
                    text = "Separate analytics for core destinations:",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(10.dp))

                metrics.countryPerformance.forEach { country ->
                    CountryPerformanceCard(country = country)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        // Footer & Privacy Banner
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Data Security Note: Aggregate metrics only. Individual student personal information (PII) is not exposed in analytics view.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    // Custom Date Range Modal
    if (showCustomDateModal) {
        AlertDialog(
            onDismissRequest = { showCustomDateModal = false },
            title = { Text("Custom Date Range Filter") },
            text = {
                Column {
                    Text(
                        text = "Enter dates in YYYY-MM-DD format:",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = startInputText,
                        onValueChange = { startInputText = it },
                        label = { Text("Start Date") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = endInputText,
                        onValueChange = { endInputText = it },
                        label = { Text("End Date") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                        val startMillis = try { sdf.parse(startInputText)?.time } catch (e: Exception) { null }
                        val endMillis = try { sdf.parse(endInputText)?.time } catch (e: Exception) { null }
                        onFilterSelected(AnalyticsDateFilter.CUSTOM, startMillis, endMillis)
                        showCustomDateModal = false
                    }
                ) {
                    Text("Apply Filter")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomDateModal = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun AnalyticsSectionCard(
    title: String,
    icon: ImageVector,
    tagText: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = tagText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            content()
        }
    }
}

@Composable
private fun AnalyticsKpiCard(
    label: String,
    value: String,
    subtext: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Surface(
        onClick = { onClick?.invoke() },
        enabled = onClick != null,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtext,
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun ProgressBarRow(
    label: String,
    count: Int,
    total: Int
) {
    val fraction = (count.toFloat() / total.toFloat().coerceAtLeast(1f)).coerceIn(0f, 1f)

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "$count (${"%.1f".format(fraction * 100)}%)",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.height(3.dp))
        LinearProgressIndicator(
            progress = { fraction },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
private fun CountryPerformanceCard(country: CountryPerformanceMetrics) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = country.flagEmoji, fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = country.countryName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "Conv: ${"%.1f".format(country.conversionRate)}%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetricSmallItem("Universities", country.universityCount.toString())
                MetricSmallItem("Applications", country.applicationsCount.toString())
                MetricSmallItem("Leads / Inquiries", country.leadsCount.toString())
                MetricSmallItem("Views & Activity", country.viewsCount.toString())
            }
        }
    }
}

@Composable
private fun MetricSmallItem(label: String, value: String) {
    Column {
        Text(
            text = label,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
