package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.PortfolioUiState
import com.example.ui.components.AssetAllocationDonutChart
import com.example.ui.components.HoldingsTable
import com.example.ui.components.MonthlyPerformanceTable
import com.example.ui.components.MonthlyPnLBarChart
import com.example.ui.components.StatMetricCard
import com.example.ui.components.YearlyPerformanceTable
import com.example.ui.components.YearlyPnLBarChart
import com.example.ui.theme.BrandPrimary
import com.example.ui.theme.CapitalAmber
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.LossRed
import com.example.ui.theme.ProfitGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.CurrencyManager

private fun formatCurrency(amount: Double): String = CurrencyManager.format(amount)
private fun formatCurrencyPrecise(amount: Double): String = CurrencyManager.formatPrecise(amount)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    state: PortfolioUiState,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var showMonthlyPercentage by remember { mutableStateOf(false) }

    val tabs = listOf("Monthly PnL", "Yearly PnL", "Holdings")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkSurface)
    ) {
        // Tab Row
        SecondaryTabRow(
            selectedTabIndex = selectedTab,
            containerColor = DarkSurfaceElevated,
            contentColor = BrandPrimary,
            modifier = Modifier.fillMaxWidth()
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    modifier = Modifier.testTag("analytics_tab_$index")
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            when (selectedTab) {
                0 -> {
                    // MONTHLY ANALYSIS
                    val monthlyList = state.monthlyPerformances
                    val winCount = monthlyList.count { it.monthlyPnL >= 0 }
                    val totalMonths = monthlyList.size
                    val winRate = if (totalMonths > 0) (winCount.toDouble() / totalMonths) * 100.0 else 0.0

                    val bestMonth = monthlyList.maxByOrNull { it.monthlyPnL }
                    val worstMonth = monthlyList.minByOrNull { it.monthlyPnL }

                    // Monthly Stats KPI
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            StatMetricCard(
                                title = "Profitable Months",
                                value = "$winCount / $totalMonths",
                                subtitle = "${String.format(Locale.US, "%.0f", winRate)}% Win Rate",
                                icon = Icons.Default.CheckCircle,
                                iconColor = ProfitGreen,
                                modifier = Modifier.weight(1f)
                            )
                            StatMetricCard(
                                title = "Best Month",
                                value = bestMonth?.let { "${if (it.monthlyPnL >= 0) "+" else ""}${formatCurrency(it.monthlyPnL)}" } ?: "-",
                                subtitle = bestMonth?.displayMonth ?: "No data",
                                icon = Icons.AutoMirrored.Filled.TrendingUp,
                                iconColor = ProfitGreen,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Monthly PnL Bar Chart Card
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "Month-over-Month PnL",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = "Tap any bar to inspect monthly return",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextMuted
                                        )
                                    }

                                    // Toggle between $ and %
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        FilterChip(
                                            selected = !showMonthlyPercentage,
                                            onClick = { showMonthlyPercentage = false },
                                            label = { Text(CurrencyManager.currentCurrency.value.symbol.trim(), fontWeight = FontWeight.Bold) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = BrandPrimary.copy(alpha = 0.2f),
                                                selectedLabelColor = BrandPrimary
                                            )
                                        )
                                        FilterChip(
                                            selected = showMonthlyPercentage,
                                            onClick = { showMonthlyPercentage = true },
                                            label = { Text("%", fontWeight = FontWeight.Bold) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = BrandPrimary.copy(alpha = 0.2f),
                                                selectedLabelColor = BrandPrimary
                                            )
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                MonthlyPnLBarChart(
                                    monthlyData = state.monthlyPerformances,
                                    showPercentage = showMonthlyPercentage,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    // Monthly Performance Data Table
                    item {
                        MonthlyPerformanceTable(
                            monthlyData = state.monthlyPerformances,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                1 -> {
                    // YEARLY ANALYSIS
                    val yearlyList = state.yearlyPerformances
                    val bestYear = yearlyList.maxByOrNull { it.yearlyPnL }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            StatMetricCard(
                                title = "Years Tracked",
                                value = "${yearlyList.size} Years",
                                subtitle = if (yearlyList.isNotEmpty()) "${yearlyList.first().year} - ${yearlyList.last().year}" else "No data",
                                icon = Icons.Default.CalendarToday,
                                iconColor = BrandPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            StatMetricCard(
                                title = "Top Annual Gain",
                                value = bestYear?.let { "${if (it.yearlyPnL >= 0) "+" else ""}${formatCurrency(it.yearlyPnL)}" } ?: "-",
                                subtitle = bestYear?.let { "${it.year} (${String.format(Locale.US, "%.1f", it.yearlyPnLPercent)}%)" } ?: "No data",
                                icon = Icons.AutoMirrored.Filled.TrendingUp,
                                iconColor = ProfitGreen,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Yearly Bar Chart
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Annual Growth & Returns",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                YearlyPnLBarChart(
                                    yearlyData = state.yearlyPerformances,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    // Yearly Performance Data Table
                    item {
                        YearlyPerformanceTable(
                            yearlyData = state.yearlyPerformances,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                2 -> {
                    // HOLDINGS & ALLOCATION
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Asset Allocation Distribution",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                AssetAllocationDonutChart(
                                    holdings = state.holdings,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    item {
                        HoldingsTable(
                            holdings = state.holdings,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}
