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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.PortfolioUiState
import com.example.ui.components.AssetAllocationDonutChart
import com.example.ui.components.MonthlyPnLBarChart
import com.example.ui.components.PortfolioHeroCard
import com.example.ui.components.PortfolioValueAreaChart
import com.example.ui.components.StatMetricCard
import com.example.ui.theme.BrandPrimary
import com.example.ui.theme.CapitalAmber
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.LossRed
import com.example.ui.theme.ProfitGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.NumberFormat
import java.util.Locale

private val currencyFormatter: NumberFormat = NumberFormat.getCurrencyInstance(Locale.US).apply {
    maximumFractionDigits = 2
}

@Composable
fun DashboardScreen(
    state: PortfolioUiState,
    onOpenAddTransaction: () -> Unit,
    onOpenUpdatePrice: () -> Unit,
    onOpenAddAsset: () -> Unit,
    onSelectTimeframe: (String) -> Unit,
    onSelectChartType: (String) -> Unit,
    onSeedSampleData: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            // Portfolio Hero Summary Card
            PortfolioHeroCard(
                summary = state.summary,
                modifier = Modifier.testTag("portfolio_hero_card")
            )
        }

        // Quick Action Buttons
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onOpenAddTransaction,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("quick_add_tx_button")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Trade", fontWeight = FontWeight.Bold, color = Color.Black)
                }

                Button(
                    onClick = onOpenAddAsset,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                    modifier = Modifier
                        .weight(0.9f)
                        .testTag("quick_add_asset_button")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = BrandPrimary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Asset", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = onOpenUpdatePrice,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                    modifier = Modifier
                        .weight(1.1f)
                        .testTag("quick_update_price_button")
                ) {
                    Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = null, tint = CapitalAmber, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Prices", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // Empty state seed banner
        if (state.transactions.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BrandPrimary.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.AutoGraph, contentDescription = null, tint = BrandPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Welcome to Portfolia",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Track your transactions, capital invested, monthly price updates, and month/year PnL analytics. Start by loading realistic sample data or record your first trade.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = onSeedSampleData,
                                colors = ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Load Sample Portfolio")
                            }
                            OutlinedButton(
                                onClick = onOpenAddAsset,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("+ Add Asset")
                            }
                        }
                    }
                }
            }
        }

        // KPI Metric Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val momText = "${if (state.summary.latestMonthlyPnL >= 0) "+" else ""}${String.format(Locale.US, "%.1f", state.summary.latestMonthlyPnLPercent)}%"
                StatMetricCard(
                    title = "Latest Month",
                    value = momText,
                    subtitle = currencyFormatter.format(state.summary.latestMonthlyPnL),
                    icon = if (state.summary.latestMonthlyPnL >= 0) Icons.AutoMirrored.Filled.TrendingUp else Icons.Default.ShowChart,
                    iconColor = if (state.summary.latestMonthlyPnL >= 0) ProfitGreen else LossRed,
                    modifier = Modifier.weight(1f)
                )

                val yoyText = "${if (state.summary.currentYearPnL >= 0) "+" else ""}${String.format(Locale.US, "%.1f", state.summary.currentYearPnLPercent)}%"
                StatMetricCard(
                    title = "Year to Date",
                    value = yoyText,
                    subtitle = currencyFormatter.format(state.summary.currentYearPnL),
                    icon = Icons.Default.AutoGraph,
                    iconColor = if (state.summary.currentYearPnL >= 0) ProfitGreen else LossRed,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val best = state.summary.bestAsset
                StatMetricCard(
                    title = "Top Asset",
                    value = best?.asset?.symbol ?: "-",
                    subtitle = best?.let { "${if (it.unrealizedPnL >= 0) "+" else ""}${String.format(Locale.US, "%.1f", it.unrealizedPnLPercent)}%" } ?: "No trades",
                    icon = Icons.Default.Star,
                    iconColor = CapitalAmber,
                    modifier = Modifier.weight(1f)
                )

                StatMetricCard(
                    title = "Realized PnL",
                    value = currencyFormatter.format(state.summary.totalRealizedPnL),
                    subtitle = "${state.summary.transactionCount} Trades recorded",
                    icon = Icons.Default.CurrencyExchange,
                    iconColor = if (state.summary.totalRealizedPnL >= 0) ProfitGreen else LossRed,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Main Performance Chart Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Chart selector segment
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        SegmentedButton(
                            selected = state.selectedChartType == "VALUE",
                            onClick = { onSelectChartType("VALUE") },
                            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3),
                            icon = { Icon(Icons.Default.ShowChart, contentDescription = null, modifier = Modifier.size(14.dp)) }
                        ) {
                            Text("Value", style = MaterialTheme.typography.labelSmall)
                        }
                        SegmentedButton(
                            selected = state.selectedChartType == "MOM_PNL",
                            onClick = { onSelectChartType("MOM_PNL") },
                            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3),
                            icon = { Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = null, modifier = Modifier.size(14.dp)) }
                        ) {
                            Text("MoM PnL", style = MaterialTheme.typography.labelSmall)
                        }
                        SegmentedButton(
                            selected = state.selectedChartType == "ALLOCATION",
                            onClick = { onSelectChartType("ALLOCATION") },
                            shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3),
                            icon = { Icon(Icons.Default.PieChart, contentDescription = null, modifier = Modifier.size(14.dp)) }
                        ) {
                            Text("Allocation", style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Timeframe filter chips
                    if (state.selectedChartType != "ALLOCATION") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("All", "1Y", "6M", "YTD").forEach { tf ->
                                FilterChip(
                                    selected = state.selectedTimeframe == tf,
                                    onClick = { onSelectTimeframe(tf) },
                                    label = { Text(tf, style = MaterialTheme.typography.labelSmall) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = BrandPrimary.copy(alpha = 0.2f),
                                        selectedLabelColor = BrandPrimary
                                    )
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    when (state.selectedChartType) {
                        "VALUE" -> PortfolioValueAreaChart(
                            monthlyData = state.monthlyPerformances,
                            modifier = Modifier.fillMaxWidth()
                        )
                        "MOM_PNL" -> MonthlyPnLBarChart(
                            monthlyData = state.monthlyPerformances,
                            showPercentage = false,
                            modifier = Modifier.fillMaxWidth()
                        )
                        "ALLOCATION" -> AssetAllocationDonutChart(
                            holdings = state.holdings,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // Active Holdings Snapshot
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
                        Text(
                            text = "Portfolio Holdings",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "${state.summary.assetCount} Active",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val active = state.holdings.filter { it.unitsHeld > 0 }
                    if (active.isEmpty()) {
                        Text(
                            text = "No active positions. Record BUY transactions to populate your portfolio.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    } else {
                        active.forEach { holding ->
                            val isProfit = holding.unrealizedPnL >= 0
                            val color = try {
                                Color(android.graphics.Color.parseColor(holding.asset.colorHex))
                            } catch (_: Exception) {
                                BrandPrimary
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .background(color, CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = holding.asset.symbol,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = "${String.format(Locale.US, if (holding.unitsHeld < 1) "%.4f" else "%.2f", holding.unitsHeld)} @ ${currencyFormatter.format(holding.currentPrice)}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextMuted
                                        )
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = currencyFormatter.format(holding.currentValue),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "${if (isProfit) "+" else ""}${String.format(Locale.US, "%.1f", holding.unrealizedPnLPercent)}% (${if (isProfit) "+" else ""}${currencyFormatter.format(holding.unrealizedPnL)})",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isProfit) ProfitGreen else LossRed
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
