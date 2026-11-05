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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.entity.TransactionEntity
import com.example.ui.PortfolioUiState
import com.example.ui.components.TransactionHistoryTable
import com.example.ui.theme.BrandPrimary
import com.example.ui.theme.CapitalAmber
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.LossRed
import com.example.ui.theme.ProfitGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.CurrencyManager

private fun formatCurrency(amount: Double): String = CurrencyManager.format(amount)

@Composable
fun TransactionsScreen(
    state: PortfolioUiState,
    onOpenAddTransaction: () -> Unit,
    onDeleteTransaction: (TransactionEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var typeFilter by remember { mutableStateOf("ALL") }
    var selectedAssetFilter by remember { mutableStateOf<Long?>(null) }

    val assetsMap = remember(state.assets) {
        state.assets.associateBy { it.id }
    }

    val filteredTransactions = state.transactions.filter { tx ->
        val matchesType = when (typeFilter) {
            "BUY" -> tx.type.equals("BUY", ignoreCase = true)
            "SELL" -> tx.type.equals("SELL", ignoreCase = true)
            else -> true
        }
        val matchesAsset = selectedAssetFilter == null || tx.assetId == selectedAssetFilter
        matchesType && matchesAsset
    }

    val totalBuyAmount = state.transactions
        .filter { it.type.equals("BUY", ignoreCase = true) }
        .sumOf { it.totalAmount }

    val totalSellAmount = state.transactions
        .filter { it.type.equals("SELL", ignoreCase = true) }
        .sumOf { it.totalAmount }

    val netCashDeployed = totalBuyAmount - totalSellAmount

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onOpenAddTransaction,
                containerColor = BrandPrimary,
                contentColor = Color.Black,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("fab_add_transaction")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Transaction")
            }
        },
        containerColor = DarkSurface,
        modifier = modifier
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            // Capital Summary Cards
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
                                text = "Capital Activity Summary",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            androidx.compose.material3.Button(
                                onClick = onOpenAddTransaction,
                                shape = RoundedCornerShape(8.dp),
                                colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = BrandPrimary),
                                modifier = Modifier.testTag("tx_screen_record_trade_btn")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Record Trade", color = Color.Black, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Total Purchases", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                Text(
                                    text = formatCurrency(totalBuyAmount),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = ProfitGreen
                                )
                            }
                            Column {
                                Text("Total Sales", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                Text(
                                    text = formatCurrency(totalSellAmount),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = LossRed
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Net Cash Deployed", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                                Text(
                                    text = formatCurrency(netCashDeployed),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = CapitalAmber
                                )
                            }
                        }
                    }
                }
            }

            // Filters
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        listOf("ALL", "BUY", "SELL").forEach { type ->
                            FilterChip(
                                selected = typeFilter == type,
                                onClick = { typeFilter = type },
                                label = { Text(type, style = MaterialTheme.typography.labelSmall) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = BrandPrimary.copy(alpha = 0.2f),
                                    selectedLabelColor = BrandPrimary
                                )
                            )
                        }
                    }

                    // Asset filter chips if assets exist
                    if (state.assets.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            FilterChip(
                                selected = selectedAssetFilter == null,
                                onClick = { selectedAssetFilter = null },
                                label = { Text("All Assets", style = MaterialTheme.typography.labelSmall) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = BrandPrimary.copy(alpha = 0.2f),
                                    selectedLabelColor = BrandPrimary
                                )
                            )
                            state.assets.take(4).forEach { asset ->
                                FilterChip(
                                    selected = selectedAssetFilter == asset.id,
                                    onClick = {
                                        selectedAssetFilter = if (selectedAssetFilter == asset.id) null else asset.id
                                    },
                                    label = { Text(asset.symbol, style = MaterialTheme.typography.labelSmall) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = BrandPrimary.copy(alpha = 0.2f),
                                        selectedLabelColor = BrandPrimary
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Full Transactions Table
            item {
                TransactionHistoryTable(
                    transactions = filteredTransactions,
                    assetsMap = assetsMap,
                    onDeleteTransaction = onDeleteTransaction,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}
