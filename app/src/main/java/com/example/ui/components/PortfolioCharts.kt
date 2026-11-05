package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.HoldingsItem
import com.example.domain.model.MonthlyPerformance
import com.example.domain.model.YearlyPerformance
import com.example.ui.theme.BrandPrimary
import com.example.ui.theme.CapitalAmber
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.LossRed
import com.example.ui.theme.ProfitGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

private val currencyFormatter: NumberFormat = NumberFormat.getCurrencyInstance(Locale.US).apply {
    maximumFractionDigits = 0
}

private val currencyPreciseFormatter: NumberFormat = NumberFormat.getCurrencyInstance(Locale.US).apply {
    maximumFractionDigits = 2
}

/**
 * Interactive Dual Area/Line Chart: Portfolio Value vs Invested Capital
 */
@Composable
fun PortfolioValueAreaChart(
    monthlyData: List<MonthlyPerformance>,
    modifier: Modifier = Modifier
) {
    if (monthlyData.isEmpty()) {
        EmptyChartPlaceholder("Add monthly valuations or transactions to view performance curve")
        return
    }

    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    val progress = remember { Animatable(0f) }

    LaunchedEffect(monthlyData.size) {
        progress.snapTo(0f)
        progress.animateTo(1f, animationSpec = tween(durationMillis = 750))
    }

    val selectedItem = selectedIndex?.let { monthlyData.getOrNull(it) } ?: monthlyData.lastOrNull()

    Column(modifier = modifier) {
        // Legend and Selected Point Tooltip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = selectedItem?.displayMonth ?: "Latest",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = currencyFormatter.format(selectedItem?.endingPortfolioValue ?: 0.0),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    val pnl = selectedItem?.totalPnL ?: 0.0
                    val pnlPct = selectedItem?.totalPnLPercent ?: 0.0
                    val isProfit = pnl >= 0.0
                    Surface(
                        color = if (isProfit) ProfitGreen.copy(alpha = 0.15f) else LossRed.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "${if (isProfit) "+" else ""}${String.format(Locale.US, "%.1f", pnlPct)}%",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isProfit) ProfitGreen else LossRed,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Legend indicators
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(BrandPrimary, CircleShape)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Value", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                Spacer(modifier = Modifier.width(12.dp))
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(CapitalAmber, CircleShape)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Capital", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            }
        }

        // Canvas Area Chart
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .pointerInput(monthlyData) {
                    detectTapGestures(
                        onPress = { offset ->
                            val count = monthlyData.size
                            if (count > 0) {
                                val itemWidth = size.width / count
                                val index = (offset.x / itemWidth).toInt().coerceIn(0, count - 1)
                                selectedIndex = index
                            }
                        }
                    )
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                val count = monthlyData.size
                if (count <= 0) return@Canvas

                val maxValue = monthlyData.maxOf { max(it.endingPortfolioValue, it.investedCapital) }
                    .coerceAtLeast(100.0) * 1.08
                val minValue = monthlyData.minOf { min(it.endingPortfolioValue, it.investedCapital) }
                    .coerceAtLeast(0.0) * 0.9

                val range = (maxValue - minValue).coerceAtLeast(1.0)

                fun getY(value: Double): Float {
                    val normalized = ((value - minValue) / range).toFloat().coerceIn(0f, 1f)
                    return height - (normalized * height * progress.value)
                }

                fun getX(index: Int): Float {
                    return if (count > 1) {
                        (index.toFloat() / (count - 1)) * width
                    } else {
                        width / 2f
                    }
                }

                // Horizontal grid lines
                val gridSteps = 3
                for (i in 0..gridSteps) {
                    val y = height * (i.toFloat() / gridSteps)
                    drawLine(
                        color = DarkBorder.copy(alpha = 0.5f),
                        start = Offset(0f, y),
                        end = Offset(width, y),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
                    )
                }

                // Invested Capital Path (Amber dashed line)
                val capitalPath = Path()
                monthlyData.forEachIndexed { index, item ->
                    val x = getX(index)
                    val y = getY(item.investedCapital)
                    if (index == 0) capitalPath.moveTo(x, y) else capitalPath.lineTo(x, y)
                }
                drawPath(
                    path = capitalPath,
                    color = CapitalAmber.copy(alpha = 0.85f),
                    style = Stroke(
                        width = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f)),
                        cap = StrokeCap.Round
                    )
                )

                // Portfolio Value Filled Area & Line
                val valueAreaPath = Path()
                val valueLinePath = Path()

                monthlyData.forEachIndexed { index, item ->
                    val x = getX(index)
                    val y = getY(item.endingPortfolioValue)
                    if (index == 0) {
                        valueAreaPath.moveTo(x, height)
                        valueAreaPath.lineTo(x, y)
                        valueLinePath.moveTo(x, y)
                    } else {
                        valueAreaPath.lineTo(x, y)
                        valueLinePath.lineTo(x, y)
                    }
                }
                valueAreaPath.lineTo(getX(count - 1), height)
                valueAreaPath.close()

                // Draw gradient area
                drawPath(
                    path = valueAreaPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            BrandPrimary.copy(alpha = 0.35f),
                            BrandPrimary.copy(alpha = 0.02f)
                        ),
                        startY = 0f,
                        endY = height
                    )
                )

                // Draw solid line
                drawPath(
                    path = valueLinePath,
                    color = BrandPrimary,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )

                // Draw points and highlight selected point
                val activeIdx = selectedIndex ?: (count - 1)
                monthlyData.forEachIndexed { index, item ->
                    val x = getX(index)
                    val y = getY(item.endingPortfolioValue)

                    if (index == activeIdx) {
                        // Vertical guideline
                        drawLine(
                            color = BrandPrimary.copy(alpha = 0.5f),
                            start = Offset(x, 0f),
                            end = Offset(x, height),
                            strokeWidth = 1.5.dp.toPx()
                        )

                        // Outer ring
                        drawCircle(
                            color = BrandPrimary.copy(alpha = 0.3f),
                            radius = 9.dp.toPx(),
                            center = Offset(x, y)
                        )
                        // Inner circle
                        drawCircle(
                            color = TextPrimary,
                            radius = 4.dp.toPx(),
                            center = Offset(x, y)
                        )
                    } else if (count <= 12 || index % 2 == 0) {
                        drawCircle(
                            color = BrandPrimary,
                            radius = 2.5.dp.toPx(),
                            center = Offset(x, y)
                        )
                    }
                }
            }
        }

        // Bottom Month Labels
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val first = monthlyData.firstOrNull()?.displayMonth ?: ""
            val mid = if (monthlyData.size > 2) monthlyData[monthlyData.size / 2].displayMonth else ""
            val last = monthlyData.lastOrNull()?.displayMonth ?: ""

            Text(first, style = MaterialTheme.typography.labelSmall, color = TextMuted)
            if (mid.isNotEmpty()) {
                Text(mid, style = MaterialTheme.typography.labelSmall, color = TextMuted)
            }
            Text(last, style = MaterialTheme.typography.labelSmall, color = TextMuted)
        }
    }
}

/**
 * Month-over-Month PnL Bar Chart ($ or %)
 */
@Composable
fun MonthlyPnLBarChart(
    monthlyData: List<MonthlyPerformance>,
    showPercentage: Boolean = false,
    modifier: Modifier = Modifier
) {
    if (monthlyData.isEmpty()) {
        EmptyChartPlaceholder("No monthly return history recorded yet")
        return
    }

    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    val progress = remember { Animatable(0f) }

    LaunchedEffect(monthlyData.size, showPercentage) {
        progress.snapTo(0f)
        progress.animateTo(1f, animationSpec = tween(durationMillis = 650))
    }

    val selected = selectedIndex?.let { monthlyData.getOrNull(it) } ?: monthlyData.lastOrNull()

    Column(modifier = modifier) {
        // Header info
        if (selected != null) {
            val valText = if (showPercentage) {
                "${if (selected.monthlyPnLPercent >= 0) "+" else ""}${String.format(Locale.US, "%.2f", selected.monthlyPnLPercent)}%"
            } else {
                "${if (selected.monthlyPnL >= 0) "+" else ""}${currencyPreciseFormatter.format(selected.monthlyPnL)}"
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${selected.displayMonth} Return",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
                Text(
                    text = valText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (selected.monthlyPnL >= 0) ProfitGreen else LossRed
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .pointerInput(monthlyData) {
                    detectTapGestures(
                        onPress = { offset ->
                            val count = monthlyData.size
                            if (count > 0) {
                                val itemWidth = size.width / count
                                val index = (offset.x / itemWidth).toInt().coerceIn(0, count - 1)
                                selectedIndex = index
                            }
                        }
                    )
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                val count = monthlyData.size
                if (count == 0) return@Canvas

                val values = monthlyData.map {
                    if (showPercentage) it.monthlyPnLPercent else it.monthlyPnL
                }

                val maxPositive = values.filter { it > 0 }.maxOrNull() ?: 10.0
                val minNegative = values.filter { it < 0 }.minOrNull() ?: -10.0
                val absMax = max(maxPositive, -minNegative).coerceAtLeast(1.0)

                // Baseline is centered or proportional
                val zeroY = height * 0.5f

                // Draw baseline
                drawLine(
                    color = DarkBorder,
                    start = Offset(0f, zeroY),
                    end = Offset(width, zeroY),
                    strokeWidth = 1.5.dp.toPx()
                )

                val barSlotWidth = width / count
                val barWidth = (barSlotWidth * 0.65f).coerceIn(6f, 32.dp.toPx())
                val corner = CornerRadius(4.dp.toPx(), 4.dp.toPx())

                monthlyData.forEachIndexed { index, item ->
                    val value = if (showPercentage) item.monthlyPnLPercent else item.monthlyPnL
                    val isPositive = value >= 0
                    val normalizedHeight = ((kotlin.math.abs(value) / absMax) * (height * 0.44f) * progress.value).toFloat()
                    val barX = (index * barSlotWidth) + ((barSlotWidth - barWidth) / 2f)

                    val top = if (isPositive) zeroY - normalizedHeight else zeroY
                    val barHeight = normalizedHeight.coerceAtLeast(2.dp.toPx())

                    val isSelected = index == (selectedIndex ?: (count - 1))
                    val barColor = if (isPositive) {
                        if (isSelected) ProfitGreen else ProfitGreen.copy(alpha = 0.75f)
                    } else {
                        if (isSelected) LossRed else LossRed.copy(alpha = 0.75f)
                    }

                    drawRoundRect(
                        color = barColor,
                        topLeft = Offset(barX, top),
                        size = Size(barWidth, barHeight),
                        cornerRadius = corner
                    )

                    // Draw selection indicator highlight
                    if (isSelected) {
                        drawRoundRect(
                            color = TextPrimary.copy(alpha = 0.6f),
                            topLeft = Offset(barX - 1.5.dp.toPx(), top - 1.5.dp.toPx()),
                            size = Size(barWidth + 3.dp.toPx(), barHeight + 3.dp.toPx()),
                            cornerRadius = corner,
                            style = Stroke(width = 1.5.dp.toPx())
                        )
                    }
                }
            }
        }
    }
}

/**
 * Yearly Performance Bar Chart
 */
@Composable
fun YearlyPnLBarChart(
    yearlyData: List<YearlyPerformance>,
    modifier: Modifier = Modifier
) {
    if (yearlyData.isEmpty()) {
        EmptyChartPlaceholder("No yearly data recorded yet")
        return
    }

    Column(modifier = modifier) {
        val maxVal = yearlyData.maxOfOrNull { kotlin.math.abs(it.yearlyPnL) }?.coerceAtLeast(100.0) ?: 100.0

        yearlyData.forEach { yearItem ->
            val isPositive = yearItem.yearlyPnL >= 0
            val fraction = (kotlin.math.abs(yearItem.yearlyPnL) / maxVal).toFloat().coerceIn(0.05f, 1f)

            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = yearItem.year,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${if (isPositive) "+" else ""}${currencyPreciseFormatter.format(yearItem.yearlyPnL)}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isPositive) ProfitGreen else LossRed
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = if (isPositive) ProfitGreen.copy(alpha = 0.15f) else LossRed.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "${if (isPositive) "+" else ""}${String.format(Locale.US, "%.1f", yearItem.yearlyPnLPercent)}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isPositive) ProfitGreen else LossRed,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Progress bar representing relative scale
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .background(DarkBorder.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fraction)
                                .height(8.dp)
                                .background(
                                    if (isPositive) ProfitGreen else LossRed,
                                    RoundedCornerShape(4.dp)
                                )
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Year End Value: ${currencyFormatter.format(yearItem.yearEndValue)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                        Text(
                            text = "Capital: ${currencyFormatter.format(yearItem.investedCapital)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }
                }
            }
        }
    }
}

/**
 * Asset Allocation Donut Chart
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AssetAllocationDonutChart(
    holdings: List<HoldingsItem>,
    modifier: Modifier = Modifier
) {
    val activeHoldings = holdings.filter { it.unitsHeld > 0 && it.currentValue > 0 }
    if (activeHoldings.isEmpty()) {
        EmptyChartPlaceholder("No active holdings to display allocation")
        return
    }

    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    val totalValue = activeHoldings.sumOf { it.currentValue }

    val colors = activeHoldings.map { holding ->
        try {
            Color(android.graphics.Color.parseColor(holding.asset.colorHex))
        } catch (_: Exception) {
            BrandPrimary
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(200.dp)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(activeHoldings) {
                        detectTapGestures { offset ->
                            val center = Offset(size.width / 2f, size.height / 2f)
                            val touchVec = offset - center
                            var angle = Math.toDegrees(kotlin.math.atan2(touchVec.y.toDouble(), touchVec.x.toDouble())).toFloat()
                            if (angle < 0) angle += 360f

                            var currentAngle = 0f
                            activeHoldings.forEachIndexed { idx, item ->
                                val sweep = ((item.currentValue / totalValue) * 360f).toFloat()
                                if (angle >= currentAngle && angle < currentAngle + sweep) {
                                    selectedIndex = if (selectedIndex == idx) null else idx
                                    return@detectTapGestures
                                }
                                currentAngle += sweep
                            }
                        }
                    }
            ) {
                val strokeWidth = 28.dp.toPx()
                val radius = (size.minDimension - strokeWidth) / 2f
                val center = Offset(size.width / 2f, size.height / 2f)

                var startAngle = -90f
                activeHoldings.forEachIndexed { index, holding ->
                    val sweep = ((holding.currentValue / totalValue) * 360f).toFloat()
                    val isSelected = selectedIndex == index
                    val sliceColor = colors[index]

                    drawArc(
                        color = if (isSelected) sliceColor else sliceColor.copy(alpha = 0.9f),
                        startAngle = startAngle,
                        sweepAngle = sweep - 2f, // subtle gap
                        useCenter = false,
                        topLeft = Offset(center.x - radius, center.y - radius),
                        size = Size(radius * 2, radius * 2),
                        style = Stroke(
                            width = if (isSelected) strokeWidth * 1.25f else strokeWidth,
                            cap = StrokeCap.Round
                        )
                    )
                    startAngle += sweep
                }
            }

            // Center Content
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val selectedHolding = selectedIndex?.let { activeHoldings.getOrNull(it) }
                if (selectedHolding != null) {
                    Text(
                        text = selectedHolding.asset.symbol,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = String.format(Locale.US, "%.1f%%", selectedHolding.allocationPercent),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                } else {
                    Text(
                        text = "Portfolio",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                    Text(
                        text = currencyFormatter.format(totalValue),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Allocation Legend Chips
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            activeHoldings.forEachIndexed { index, holding ->
                val isSelected = selectedIndex == index
                Surface(
                    onClick = { selectedIndex = if (isSelected) null else index },
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSelected) colors[index].copy(alpha = 0.25f) else DarkSurfaceElevated,
                    border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, colors[index]) else null,
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(colors[index], CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${holding.asset.symbol} ${String.format(Locale.US, "%.1f", holding.allocationPercent)}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) TextPrimary else TextSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyChartPlaceholder(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .background(DarkSurfaceElevated.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted,
            modifier = Modifier.padding(16.dp)
        )
    }
}
