package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.InsertChart
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PriceCheck
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.InsertChart
import androidx.compose.material.icons.outlined.PriceCheck
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.ui.dialogs.AddAssetDialog
import com.example.ui.dialogs.AddTransactionDialog
import com.example.ui.dialogs.SelectCurrencyDialog
import com.example.ui.dialogs.UpdateMonthlyPriceDialog
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.HoldingsPricesScreen
import com.example.ui.screens.TransactionsScreen
import com.example.ui.theme.BrandPrimary
import com.example.ui.theme.CapitalAmber
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

data class NavTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PortfolioApp(
    viewModel: PortfolioViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val activeDialog by viewModel.activeDialog.collectAsStateWithLifecycle()
    val currency by viewModel.currency.collectAsStateWithLifecycle()
    var selectedScreenIndex by remember { mutableIntStateOf(0) }
    var menuExpanded by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.userMessage) {
        val msg = state.userMessage
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearUserMessage()
        }
    }

    // Handle back button on sub-screens
    if (selectedScreenIndex != 0) {
        BackHandler {
            selectedScreenIndex = 0
        }
    }

    val navItems = listOf(
        NavTab("Overview", Icons.Filled.Dashboard, Icons.Outlined.Dashboard, "nav_overview"),
        NavTab("Analytics", Icons.Filled.InsertChart, Icons.Outlined.InsertChart, "nav_analytics"),
        NavTab("Valuations", Icons.Filled.PriceCheck, Icons.Outlined.PriceCheck, "nav_valuations"),
        NavTab("Ledger", Icons.Filled.ReceiptLong, Icons.Outlined.ReceiptLong, "nav_ledger")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.portfolia_icon_1793916517647),
                            contentDescription = "Portfolia Logo",
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Portfolia",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .background(BrandPrimary.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "PRO",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandPrimary
                                )
                            }
                        }
                    }
                },
                actions = {
                    // Currency Selector Badge/Button
                    Surface(
                        onClick = { viewModel.openSelectCurrencyDialog() },
                        shape = RoundedCornerShape(8.dp),
                        color = BrandPrimary.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BrandPrimary.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .testTag("top_currency_button")
                            .padding(end = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = currency.symbol.trim(),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = BrandPrimary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = currency.code,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                    }
                    IconButton(
                        onClick = { viewModel.openAddTransactionDialog() },
                        modifier = Modifier.testTag("top_add_trade_button")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add Trade", tint = BrandPrimary)
                    }
                    IconButton(
                        onClick = { viewModel.openUpdatePriceDialog() },
                        modifier = Modifier.testTag("top_update_price_button")
                    ) {
                        Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = "Monthly Prices", tint = CapitalAmber)
                    }
                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier.testTag("app_menu_button")
                    ) {
                        Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Menu", tint = TextSecondary)
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                        containerColor = DarkSurfaceElevated
                    ) {
                        DropdownMenuItem(
                            text = { Text("Currency (${currency.code})") },
                            leadingIcon = {
                                Icon(Icons.Default.CurrencyExchange, contentDescription = null, tint = BrandPrimary)
                            },
                            onClick = {
                                menuExpanded = false
                                viewModel.openSelectCurrencyDialog()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("+ Record Trade") },
                            onClick = {
                                menuExpanded = false
                                viewModel.openAddTransactionDialog()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("+ Add New Asset") },
                            onClick = {
                                menuExpanded = false
                                viewModel.openAddAssetDialog()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Update Monthly Prices") },
                            onClick = {
                                menuExpanded = false
                                viewModel.openUpdatePriceDialog()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Load Sample Portfolio (2025-2026)") },
                            onClick = {
                                menuExpanded = false
                                viewModel.seedSampleData()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Clear Portfolio Data", color = MaterialTheme.colorScheme.error) },
                            onClick = {
                                menuExpanded = false
                                viewModel.clearAllData()
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkSurface,
                    scrolledContainerColor = DarkSurface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = DarkSurfaceElevated,
                contentColor = TextPrimary,
                tonalElevation = 8.dp
            ) {
                navItems.forEachIndexed { index, item ->
                    val isSelected = selectedScreenIndex == index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedScreenIndex = index },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.title
                            )
                        },
                        label = {
                            Text(
                                text = item.title,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = BrandPrimary,
                            selectedTextColor = BrandPrimary,
                            indicatorColor = BrandPrimary.copy(alpha = 0.15f),
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextSecondary
                        ),
                        modifier = Modifier.testTag(item.testTag)
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = DarkSurface,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedScreenIndex) {
                0 -> DashboardScreen(
                    state = state,
                    onOpenAddTransaction = { viewModel.openAddTransactionDialog() },
                    onOpenUpdatePrice = { viewModel.openUpdatePriceDialog() },
                    onOpenAddAsset = { viewModel.openAddAssetDialog() },
                    onSelectTimeframe = { viewModel.setTimeframe(it) },
                    onSelectChartType = { viewModel.setChartType(it) },
                    onSeedSampleData = { viewModel.seedSampleData() }
                )
                1 -> AnalyticsScreen(
                    state = state
                )
                2 -> HoldingsPricesScreen(
                    state = state,
                    onOpenUpdatePrices = { viewModel.openUpdatePriceDialog() },
                    onOpenAddAsset = { viewModel.openAddAssetDialog() },
                    onOpenAddTransactionForAsset = { assetId ->
                        viewModel.openAddTransactionDialog(assetId)
                    }
                )
                3 -> TransactionsScreen(
                    state = state,
                    onOpenAddTransaction = { viewModel.openAddTransactionDialog() },
                    onDeleteTransaction = { tx -> viewModel.deleteTransaction(tx) }
                )
            }
        }

        // Active Dialogs driven reactively by activeDialog
        when (val dialog = activeDialog) {
            is ActiveDialog.AddTransaction -> {
                AddTransactionDialog(
                    assets = state.assets,
                    preselectedAssetId = dialog.preselectedAssetId,
                    onDismiss = { viewModel.closeDialog() },
                    onSaveTransaction = { tx -> viewModel.addTransaction(tx) },
                    onSaveTransactionWithNewAsset = { asset, builder ->
                        viewModel.addTransactionWithNewAsset(asset, builder)
                    },
                    onOpenAddAsset = { viewModel.openAddAssetDialog() }
                )
            }
            is ActiveDialog.AddAsset -> {
                AddAssetDialog(
                    onDismiss = { viewModel.closeDialog() },
                    onSaveAsset = { asset -> viewModel.addAsset(asset) }
                )
            }
            is ActiveDialog.UpdateMonthlyPrice -> {
                UpdateMonthlyPriceDialog(
                    assets = state.assets,
                    holdings = state.holdings,
                    existingMonthlyPrices = state.monthlyPrices,
                    onDismiss = { viewModel.closeDialog() },
                    onSavePrices = { ym, prices -> viewModel.saveMonthlyPrices(ym, prices) },
                    onOpenAddAsset = { viewModel.openAddAssetDialog() },
                    onSeedSampleData = { viewModel.seedSampleData() }
                )
            }
            is ActiveDialog.SelectCurrency -> {
                SelectCurrencyDialog(
                    currentCurrency = currency,
                    onDismiss = { viewModel.closeDialog() },
                    onCurrencySelected = { selectedCurrency ->
                        viewModel.setCurrency(selectedCurrency)
                    }
                )
            }
            ActiveDialog.None -> {}
        }
    }
}
