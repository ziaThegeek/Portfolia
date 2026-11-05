package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.entity.AssetEntity
import com.example.data.entity.TransactionEntity
import com.example.ui.theme.BrandPrimary
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.LossRed
import com.example.ui.theme.ProfitGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionDialog(
    assets: List<AssetEntity>,
    preselectedAssetId: Long? = null,
    onDismiss: () -> Unit,
    onSaveTransaction: (TransactionEntity) -> Unit,
    onSaveTransactionWithNewAsset: (AssetEntity, (Long) -> TransactionEntity) -> Unit,
    onOpenAddAsset: () -> Unit
) {
    var selectedType by remember { mutableStateOf("BUY") }
    var isCreatingInlineAsset by remember { mutableStateOf(assets.isEmpty()) }
    var selectedAsset by remember {
        mutableStateOf(assets.find { it.id == preselectedAssetId } ?: assets.firstOrNull())
    }
    var assetDropdownExpanded by remember { mutableStateOf(false) }

    // Inline new asset fields
    var newSymbol by remember { mutableStateOf("") }
    var newName by remember { mutableStateOf("") }
    var newCategory by remember { mutableStateOf("Stock") }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }

    val currentDateStr = remember {
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    }
    var dateText by remember { mutableStateOf(currentDateStr) }
    var quantityText by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }
    var feesText by remember { mutableStateOf("0") }
    var notesText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val qty = quantityText.toDoubleOrNull() ?: 0.0
    val price = priceText.toDoubleOrNull() ?: 0.0
    val fees = feesText.toDoubleOrNull() ?: 0.0
    val calculatedTotal = if (selectedType == "BUY") (qty * price) + fees else (qty * price) - fees

    val categories = listOf("Stock", "Crypto", "ETF", "Commodity", "Bond", "Other")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = DarkSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Record Transaction",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Add trade to update invested capital and units",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Buy / Sell Segmented Button
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = selectedType == "BUY",
                        onClick = { selectedType = "BUY" },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                        colors = SegmentedButtonDefaults.colors(
                            activeContainerColor = ProfitGreen.copy(alpha = 0.2f),
                            activeContentColor = ProfitGreen
                        )
                    ) {
                        Text("BUY", fontWeight = FontWeight.Bold)
                    }
                    SegmentedButton(
                        selected = selectedType == "SELL",
                        onClick = { selectedType = "SELL" },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                        colors = SegmentedButtonDefaults.colors(
                            activeContainerColor = LossRed.copy(alpha = 0.2f),
                            activeContentColor = LossRed
                        )
                    ) {
                        Text("SELL", fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Asset Selector or Inline Creation
                if (isCreatingInlineAsset) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DarkSurfaceElevated, RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "New Asset Info",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = BrandPrimary
                            )
                            if (assets.isNotEmpty()) {
                                TextButton(onClick = { isCreatingInlineAsset = false }) {
                                    Text("Pick Existing", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = newSymbol,
                                onValueChange = { newSymbol = it.uppercase() },
                                label = { Text("Ticker") },
                                placeholder = { Text("e.g. NVDA") },
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("new_asset_ticker_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = DarkBorder
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedTextField(
                                value = newName,
                                onValueChange = { newName = it },
                                label = { Text("Asset Name") },
                                placeholder = { Text("e.g. Nvidia") },
                                singleLine = true,
                                modifier = Modifier.weight(1.5f),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = DarkBorder
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        ExposedDropdownMenuBox(
                            expanded = categoryDropdownExpanded,
                            onExpandedChange = { categoryDropdownExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = newCategory,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Category") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = DarkBorder
                                )
                            )
                            ExposedDropdownMenu(
                                expanded = categoryDropdownExpanded,
                                onDismissRequest = { categoryDropdownExpanded = false }
                            ) {
                                categories.forEach { cat ->
                                    DropdownMenuItem(
                                        text = { Text(cat) },
                                        onClick = {
                                            newCategory = cat
                                            categoryDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ExposedDropdownMenuBox(
                            expanded = assetDropdownExpanded,
                            onExpandedChange = { assetDropdownExpanded = it },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = selectedAsset?.let { "${it.symbol} • ${it.name}" } ?: "Select Asset",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Asset") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = assetDropdownExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = DarkBorder
                                )
                            )
                            ExposedDropdownMenu(
                                expanded = assetDropdownExpanded,
                                onDismissRequest = { assetDropdownExpanded = false }
                            ) {
                                assets.forEach { asset ->
                                    DropdownMenuItem(
                                        text = { Text("${asset.symbol} • ${asset.name}") },
                                        onClick = {
                                            selectedAsset = asset
                                            assetDropdownExpanded = false
                                        }
                                    )
                                }
                                DropdownMenuItem(
                                    text = { Text("+ New Asset...", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) },
                                    onClick = {
                                        assetDropdownExpanded = false
                                        isCreatingInlineAsset = true
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Date
                OutlinedTextField(
                    value = dateText,
                    onValueChange = { dateText = it },
                    label = { Text("Date (YYYY-MM-DD)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = DarkBorder
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Quantity & Price
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = quantityText,
                        onValueChange = { quantityText = it },
                        label = { Text("Quantity") },
                        placeholder = { Text("e.g. 10") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("tx_quantity_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = DarkBorder
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = priceText,
                        onValueChange = { priceText = it },
                        label = { Text("Price/Unit ($)") },
                        placeholder = { Text("e.g. 150.00") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("tx_price_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = DarkBorder
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Fees
                OutlinedTextField(
                    value = feesText,
                    onValueChange = { feesText = it },
                    label = { Text("Trading Fees ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = DarkBorder
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Notes
                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = { Text("Notes (Optional)") },
                    placeholder = { Text("e.g. Monthly DCA, dip purchase") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = DarkBorder
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Calculated total banner
                Surface(
                    color = DarkSurfaceElevated,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (selectedType == "BUY") "Total Cash Invested:" else "Total Cash Proceeds:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                        Text(
                            text = String.format(Locale.US, "$%.2f", calculatedTotal),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
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

                Spacer(modifier = Modifier.height(20.dp))

                // Actions
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
                            if (qty <= 0.0) {
                                errorMessage = "Quantity must be greater than 0"
                                return@Button
                            }
                            if (price <= 0.0) {
                                errorMessage = "Price must be greater than 0"
                                return@Button
                            }
                            if (dateText.isBlank()) {
                                errorMessage = "Please enter a valid date (YYYY-MM-DD)"
                                return@Button
                            }

                            if (isCreatingInlineAsset) {
                                if (newSymbol.isBlank()) {
                                    errorMessage = "Please enter asset ticker (e.g. AAPL)"
                                    return@Button
                                }
                                val assetName = newName.ifBlank { newSymbol.trim() }
                                val newAsset = AssetEntity(
                                    symbol = newSymbol.trim().uppercase(),
                                    name = assetName.trim(),
                                    category = newCategory
                                )
                                onSaveTransactionWithNewAsset(newAsset) { assetId ->
                                    TransactionEntity(
                                        assetId = assetId,
                                        type = selectedType,
                                        date = dateText.trim(),
                                        quantity = qty,
                                        pricePerUnit = price,
                                        fees = fees,
                                        notes = notesText.trim()
                                    )
                                }
                            } else {
                                val asset = selectedAsset
                                if (asset == null) {
                                    errorMessage = "Please select or create an asset first"
                                    return@Button
                                }
                                onSaveTransaction(
                                    TransactionEntity(
                                        assetId = asset.id,
                                        type = selectedType,
                                        date = dateText.trim(),
                                        quantity = qty,
                                        pricePerUnit = price,
                                        fees = fees,
                                        notes = notesText.trim()
                                    )
                                )
                            }
                        },
                        modifier = Modifier.testTag("tx_submit_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedType == "BUY") ProfitGreen else LossRed
                        )
                    ) {
                        Text("Record ${selectedType.lowercase().replaceFirstChar { it.uppercase() }}")
                    }
                }
            }
        }
    }
}
