package com.example.ui.dialogs

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.entity.AssetEntity
import com.example.data.entity.MonthlyPriceEntity
import com.example.domain.model.HoldingsItem
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.LossRed
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun UpdateMonthlyPriceDialog(
    assets: List<AssetEntity>,
    holdings: List<HoldingsItem>,
    existingMonthlyPrices: List<MonthlyPriceEntity>,
    onDismiss: () -> Unit,
    onSavePrices: (yearMonth: String, prices: Map<Long, Double>) -> Unit,
    onOpenAddAsset: () -> Unit = {},
    onSeedSampleData: () -> Unit = {}
) {
    val defaultMonth = remember {
        SimpleDateFormat("yyyy-MM", Locale.US).format(Date())
    }
    var yearMonthInput by remember { mutableStateOf(defaultMonth) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Map of assetId to entered price text
    val pricesMap = remember {
        mutableStateMapOf<Long, String>().apply {
            assets.forEach { asset ->
                // Check if price already exists for default month
                val existing = existingMonthlyPrices.find {
                    it.assetId == asset.id && it.yearMonth == defaultMonth
                }?.closingPrice

                // Or fallback to current price
                val fallback = holdings.find { it.asset.id == asset.id }?.currentPrice
                val initialVal = existing ?: fallback ?: 0.0
                if (initialVal > 0.0) {
                    this[asset.id] = String.format(Locale.US, "%.2f", initialVal)
                } else {
                    this[asset.id] = ""
                }
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = DarkSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Monthly Price Valuation",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Enter month-end closing prices to update PnL",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Year-Month Selector Input
                OutlinedTextField(
                    value = yearMonthInput,
                    onValueChange = { newMonth ->
                        yearMonthInput = newMonth
                        // When user changes month, check if existing prices exist for that month
                        if (newMonth.length == 7) {
                            assets.forEach { asset ->
                                val priceForNewMonth = existingMonthlyPrices.find {
                                    it.assetId == asset.id && it.yearMonth == newMonth
                                }?.closingPrice
                                if (priceForNewMonth != null) {
                                    pricesMap[asset.id] = String.format(Locale.US, "%.2f", priceForNewMonth)
                                }
                            }
                        }
                    },
                    label = { Text("Valuation Month (YYYY-MM)") },
                    placeholder = { Text("e.g. 2026-03") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("valuation_month_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = DarkBorder
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Asset Closing Prices ($)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (assets.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No assets in portfolio yet.",
                            color = TextMuted,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = onOpenAddAsset,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("+ Add Asset First")
                            }
                            OutlinedButton(
                                onClick = onSeedSampleData,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Load Sample Portfolio")
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                    ) {
                        items(assets, key = { it.id }) { asset ->
                            val holding = holdings.find { it.asset.id == asset.id }
                            val color = try {
                                Color(android.graphics.Color.parseColor(asset.colorHex))
                            } catch (_: Exception) {
                                MaterialTheme.colorScheme.primary
                            }

                            Card(
                                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated.copy(alpha = 0.6f)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .background(color, CircleShape)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = asset.symbol,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = TextPrimary
                                            )
                                            val unitsText = holding?.let {
                                                "${String.format(Locale.US, if (it.unitsHeld < 1) "%.4f" else "%.2f", it.unitsHeld)} held"
                                            } ?: "0 held"
                                            Text(
                                                text = unitsText,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = TextMuted
                                            )
                                        }
                                    }

                                    OutlinedTextField(
                                        value = pricesMap[asset.id] ?: "",
                                        onValueChange = { pricesMap[asset.id] = it },
                                        placeholder = { Text("0.00") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        singleLine = true,
                                        modifier = Modifier
                                            .width(130.dp)
                                            .testTag("price_input_${asset.symbol}"),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                                            unfocusedBorderColor = DarkBorder
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = LossRed
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = {
                            val ym = yearMonthInput.trim()
                            if (ym.length != 7 || ym[4] != '-') {
                                errorMessage = "Please enter month in YYYY-MM format (e.g. 2026-03)"
                                return@Button
                            }

                            val validPrices = mutableMapOf<Long, Double>()
                            assets.forEach { asset ->
                                val priceVal = pricesMap[asset.id]?.toDoubleOrNull()
                                if (priceVal != null && priceVal > 0.0) {
                                    validPrices[asset.id] = priceVal
                                }
                            }

                            if (validPrices.isEmpty()) {
                                errorMessage = "Please enter at least one valid price"
                                return@Button
                            }

                            onSavePrices(ym, validPrices)
                        },
                        modifier = Modifier.testTag("save_monthly_prices_button")
                    ) {
                        Text("Save Prices & Recalculate")
                    }
                }
            }
        }
    }
}
