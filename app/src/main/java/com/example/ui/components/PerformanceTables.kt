package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.entity.AssetEntity
import com.example.data.entity.TransactionEntity
import com.example.domain.model.HoldingsItem
import com.example.domain.model.MonthlyPerformance
import com.example.domain.model.YearlyPerformance
import com.example.ui.theme.DarkBorder
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

/**
 * Monthly Performance Table
 */
@Composable
fun MonthlyPerformanceTable(
    monthlyData: List<MonthlyPerformance>,
    modifier: Modifier = Modifier
) {
    if (monthlyData.isEmpty()) {
        EmptyTablePlaceholder("No monthly data recorded yet.")
        return
    }

    val horizontalScrollState = rememberScrollState()

    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder.copy(alpha = 0.5f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "Monthly Performance Ledger",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Scroll horizontally to view full return breakdown",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted
            )

            Spacer(modifier = Modifier.height(8.dp))

            Column(modifier = Modifier.horizontalScroll(horizontalScrollState)) {
                // Header Row
                val currSym = CurrencyManager.currentCurrency.value.symbol.trim()
                Row(
                    modifier = Modifier
                        .background(DarkSurfaceElevated, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TableHeaderCell("Month", width = 85.dp)
                    TableHeaderCell("Value", width = 95.dp, alignRight = true)
                    TableHeaderCell("Capital", width = 95.dp, alignRight = true)
                    TableHeaderCell("MoM PnL ($currSym)", width = 110.dp, alignRight = true)
                    TableHeaderCell("MoM Return", width = 95.dp, alignRight = true)
                    TableHeaderCell("Total PnL ($currSym)", width = 110.dp, alignRight = true)
                    TableHeaderCell("Total Return", width = 95.dp, alignRight = true)
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Reverse chronological display so newest is on top
                monthlyData.asReversed().forEachIndexed { index, item ->
                    val isEven = index % 2 == 0
                    val isProfit = item.monthlyPnL >= 0
                    val isTotalProfit = item.totalPnL >= 0

                    Row(
                        modifier = Modifier
                            .background(if (isEven) Color.Transparent else DarkSurfaceElevated.copy(alpha = 0.25f))
                            .padding(horizontal = 8.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TableCell(item.displayMonth, width = 85.dp, isBold = true)
                        TableCell(formatCurrency(item.endingPortfolioValue), width = 95.dp, alignRight = true)
                        TableCell(formatCurrency(item.investedCapital), width = 95.dp, alignRight = true, color = TextSecondary)
                        TableCell(
                            text = "${if (isProfit) "+" else ""}${formatCurrencyPrecise(item.monthlyPnL)}",
                            width = 105.dp,
                            alignRight = true,
                            color = if (isProfit) ProfitGreen else LossRed,
                            isBold = true
                        )
                        TableCell(
                            text = "${if (isProfit) "+" else ""}${String.format(Locale.US, "%.2f", item.monthlyPnLPercent)}%",
                            width = 95.dp,
                            alignRight = true,
                            color = if (isProfit) ProfitGreen else LossRed,
                            isBold = true
                        )
                        TableCell(
                            text = "${if (isTotalProfit) "+" else ""}${formatCurrencyPrecise(item.totalPnL)}",
                            width = 105.dp,
                            alignRight = true,
                            color = if (isTotalProfit) ProfitGreen else LossRed
                        )
                        TableCell(
                            text = "${if (isTotalProfit) "+" else ""}${String.format(Locale.US, "%.1f", item.totalPnLPercent)}%",
                            width = 95.dp,
                            alignRight = true,
                            color = if (isTotalProfit) ProfitGreen else LossRed
                        )
                    }
                    HorizontalDivider(color = DarkBorder.copy(alpha = 0.3f), thickness = 0.5.dp)
                }
            }
        }
    }
}

/**
 * Yearly Performance Table
 */
@Composable
fun YearlyPerformanceTable(
    yearlyData: List<YearlyPerformance>,
    modifier: Modifier = Modifier
) {
    if (yearlyData.isEmpty()) {
        EmptyTablePlaceholder("No annual data calculated yet.")
        return
    }

    val horizontalScrollState = rememberScrollState()

    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder.copy(alpha = 0.5f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "Annual Performance Summary",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Year-over-Year returns and capital accumulation",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted
            )

            Spacer(modifier = Modifier.height(8.dp))

            Column(modifier = Modifier.horizontalScroll(horizontalScrollState)) {
                val currSym = CurrencyManager.currentCurrency.value.symbol.trim()
                Row(
                    modifier = Modifier
                        .background(DarkSurfaceElevated, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TableHeaderCell("Year", width = 75.dp)
                    TableHeaderCell("Year End Value", width = 110.dp, alignRight = true)
                    TableHeaderCell("Invested Capital", width = 110.dp, alignRight = true)
                    TableHeaderCell("Annual PnL ($currSym)", width = 115.dp, alignRight = true)
                    TableHeaderCell("Annual Return", width = 100.dp, alignRight = true)
                    TableHeaderCell("Cumulative Return", width = 115.dp, alignRight = true)
                }

                Spacer(modifier = Modifier.height(4.dp))

                yearlyData.asReversed().forEachIndexed { index, item ->
                    val isEven = index % 2 == 0
                    val isProfit = item.yearlyPnL >= 0
                    val isTotalProfit = item.totalPnL >= 0

                    Row(
                        modifier = Modifier
                            .background(if (isEven) Color.Transparent else DarkSurfaceElevated.copy(alpha = 0.25f))
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TableCell(item.year, width = 75.dp, isBold = true)
                        TableCell(formatCurrency(item.yearEndValue), width = 110.dp, alignRight = true)
                        TableCell(formatCurrency(item.investedCapital), width = 110.dp, alignRight = true, color = TextSecondary)
                        TableCell(
                            text = "${if (isProfit) "+" else ""}${formatCurrencyPrecise(item.yearlyPnL)}",
                            width = 110.dp,
                            alignRight = true,
                            color = if (isProfit) ProfitGreen else LossRed,
                            isBold = true
                        )
                        TableCell(
                            text = "${if (isProfit) "+" else ""}${String.format(Locale.US, "%.2f", item.yearlyPnLPercent)}%",
                            width = 100.dp,
                            alignRight = true,
                            color = if (isProfit) ProfitGreen else LossRed,
                            isBold = true
                        )
                        TableCell(
                            text = "${if (isTotalProfit) "+" else ""}${String.format(Locale.US, "%.1f", item.totalPnLPercent)}%",
                            width = 115.dp,
                            alignRight = true,
                            color = if (isTotalProfit) ProfitGreen else LossRed
                        )
                    }
                    HorizontalDivider(color = DarkBorder.copy(alpha = 0.3f), thickness = 0.5.dp)
                }
            }
        }
    }
}

/**
 * Holdings Table
 */
@Composable
fun HoldingsTable(
    holdings: List<HoldingsItem>,
    modifier: Modifier = Modifier
) {
    if (holdings.isEmpty()) {
        EmptyTablePlaceholder("No assets in portfolio.")
        return
    }

    val horizontalScrollState = rememberScrollState()

    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder.copy(alpha = 0.5f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "Asset Holdings & Cost Basis Table",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column(modifier = Modifier.horizontalScroll(horizontalScrollState)) {
                Row(
                    modifier = Modifier
                        .background(DarkSurfaceElevated, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TableHeaderCell("Asset", width = 110.dp)
                    TableHeaderCell("Holdings", width = 85.dp, alignRight = true)
                    TableHeaderCell("Avg Buy", width = 90.dp, alignRight = true)
                    TableHeaderCell("Current Price", width = 100.dp, alignRight = true)
                    TableHeaderCell("Invested", width = 95.dp, alignRight = true)
                    TableHeaderCell("Current Value", width = 100.dp, alignRight = true)
                    TableHeaderCell("PnL (${CurrencyManager.currentCurrency.value.symbol.trim()})", width = 105.dp, alignRight = true)
                    TableHeaderCell("PnL (%)", width = 85.dp, alignRight = true)
                }

                Spacer(modifier = Modifier.height(4.dp))

                holdings.forEachIndexed { index, holding ->
                    val isEven = index % 2 == 0
                    val isProfit = holding.unrealizedPnL >= 0

                    Row(
                        modifier = Modifier
                            .background(if (isEven) Color.Transparent else DarkSurfaceElevated.copy(alpha = 0.25f))
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Asset with color tag
                        Row(
                            modifier = Modifier.width(110.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val color = try {
                                Color(android.graphics.Color.parseColor(holding.asset.colorHex))
                            } catch (_: Exception) {
                                TextPrimary
                            }
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .background(color, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = holding.asset.symbol,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    maxLines = 1
                                )
                                Text(
                                    text = holding.asset.category,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted,
                                    maxLines = 1
                                )
                            }
                        }

                        TableCell(
                            text = String.format(Locale.US, if (holding.unitsHeld < 1) "%.4f" else "%.2f", holding.unitsHeld),
                            width = 85.dp,
                            alignRight = true
                        )
                        TableCell(formatCurrencyPrecise(holding.avgBuyPrice), width = 90.dp, alignRight = true)
                        TableCell(formatCurrencyPrecise(holding.currentPrice), width = 100.dp, alignRight = true)
                        TableCell(formatCurrency(holding.costBasis), width = 95.dp, alignRight = true, color = TextSecondary)
                        TableCell(formatCurrency(holding.currentValue), width = 100.dp, alignRight = true, isBold = true)
                        TableCell(
                            text = "${if (isProfit) "+" else ""}${formatCurrencyPrecise(holding.unrealizedPnL)}",
                            width = 100.dp,
                            alignRight = true,
                            color = if (isProfit) ProfitGreen else LossRed,
                            isBold = true
                        )
                        TableCell(
                            text = "${if (isProfit) "+" else ""}${String.format(Locale.US, "%.1f", holding.unrealizedPnLPercent)}%",
                            width = 85.dp,
                            alignRight = true,
                            color = if (isProfit) ProfitGreen else LossRed,
                            isBold = true
                        )
                    }
                    HorizontalDivider(color = DarkBorder.copy(alpha = 0.3f), thickness = 0.5.dp)
                }
            }
        }
    }
}

/**
 * Transaction History Table with Delete / Details
 */
@Composable
fun TransactionHistoryTable(
    transactions: List<TransactionEntity>,
    assetsMap: Map<Long, AssetEntity>,
    onDeleteTransaction: (TransactionEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    if (transactions.isEmpty()) {
        EmptyTablePlaceholder("No transactions recorded. Tap '+ Transaction' to record your first trade.")
        return
    }

    val horizontalScrollState = rememberScrollState()

    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder.copy(alpha = 0.5f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "Transaction Ledger",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))

            Column(modifier = Modifier.horizontalScroll(horizontalScrollState)) {
                Row(
                    modifier = Modifier
                        .background(DarkSurfaceElevated, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TableHeaderCell("Date", width = 85.dp)
                    TableHeaderCell("Type", width = 65.dp)
                    TableHeaderCell("Asset", width = 80.dp)
                    TableHeaderCell("Quantity", width = 85.dp, alignRight = true)
                    TableHeaderCell("Unit Price", width = 90.dp, alignRight = true)
                    TableHeaderCell("Fees", width = 60.dp, alignRight = true)
                    TableHeaderCell("Total", width = 95.dp, alignRight = true)
                    TableHeaderCell("Notes", width = 120.dp)
                    TableHeaderCell("", width = 45.dp)
                }

                Spacer(modifier = Modifier.height(4.dp))

                transactions.forEachIndexed { index, tx ->
                    val isEven = index % 2 == 0
                    val isBuy = tx.type.equals("BUY", ignoreCase = true)
                    val asset = assetsMap[tx.assetId]

                    Row(
                        modifier = Modifier
                            .background(if (isEven) Color.Transparent else DarkSurfaceElevated.copy(alpha = 0.25f))
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TableCell(tx.date, width = 85.dp)
                        Box(modifier = Modifier.width(65.dp)) {
                            Surface(
                                color = if (isBuy) ProfitGreen.copy(alpha = 0.15f) else LossRed.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = tx.type.uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isBuy) ProfitGreen else LossRed,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        TableCell(asset?.symbol ?: "#${tx.assetId}", width = 80.dp, isBold = true)
                        TableCell(
                            text = String.format(Locale.US, if (tx.quantity < 1) "%.4f" else "%.2f", tx.quantity),
                            width = 85.dp,
                            alignRight = true
                        )
                        TableCell(formatCurrencyPrecise(tx.pricePerUnit), width = 90.dp, alignRight = true)
                        TableCell(formatCurrencyPrecise(tx.fees), width = 60.dp, alignRight = true, color = TextMuted)
                        TableCell(formatCurrencyPrecise(tx.totalAmount), width = 95.dp, alignRight = true, isBold = true)
                        TableCell(tx.notes.ifEmpty { "-" }, width = 120.dp, color = TextSecondary)

                        IconButton(
                            onClick = { onDeleteTransaction(tx) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete transaction",
                                tint = TextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    HorizontalDivider(color = DarkBorder.copy(alpha = 0.3f), thickness = 0.5.dp)
                }
            }
        }
    }
}

@Composable
private fun TableHeaderCell(
    text: String,
    width: androidx.compose.ui.unit.Dp,
    alignRight: Boolean = false
) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = TextSecondary,
        textAlign = if (alignRight) TextAlign.End else TextAlign.Start,
        modifier = Modifier.width(width)
    )
}

@Composable
private fun TableCell(
    text: String,
    width: androidx.compose.ui.unit.Dp,
    alignRight: Boolean = false,
    color: Color = TextPrimary,
    isBold: Boolean = false
) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        fontWeight = if (isBold) FontWeight.SemiBold else FontWeight.Normal,
        color = color,
        textAlign = if (alignRight) TextAlign.End else TextAlign.Start,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.width(width)
    )
}

@Composable
private fun EmptyTablePlaceholder(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurfaceElevated.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = TextMuted,
            textAlign = TextAlign.Center
        )
    }
}
