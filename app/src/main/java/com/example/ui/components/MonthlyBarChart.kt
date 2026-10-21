package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MonthSpend
import com.example.util.CurrencyUtils

@Composable
fun MonthlyBarChart(
    months: List<MonthSpend>,
    modifier: Modifier = Modifier
) {
    val progress = remember { Animatable(0f) }

    LaunchedEffect(months) {
        progress.snapTo(0f)
        progress.animateTo(1f, animationSpec = tween(durationMillis = 600))
    }

    val maxAmount = months.maxOfOrNull { it.totalAmount }?.takeIf { it > 0 } ?: 1.0
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryBarColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
    val backgroundTrackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        // Bar Chart Canvas
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val availableWidth = size.width
                val availableHeight = size.height
                val barCount = months.size
                if (barCount == 0) return@Canvas

                val slotWidth = availableWidth / barCount
                val barWidth = (slotWidth * 0.48f).coerceIn(12f, 36.dp.toPx())
                val cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)

                months.forEachIndexed { index, monthSpend ->
                    val centerX = slotWidth * index + slotWidth / 2f
                    val left = centerX - barWidth / 2f

                    // Draw subtle background track
                    drawRoundRect(
                        color = backgroundTrackColor,
                        topLeft = Offset(left, 0f),
                        size = Size(barWidth, availableHeight),
                        cornerRadius = cornerRadius
                    )

                    // Draw actual spend bar
                    val heightRatio = ((monthSpend.totalAmount / maxAmount).toFloat()).coerceIn(0f, 1f)
                    val barHeight = availableHeight * heightRatio * progress.value
                    val top = availableHeight - barHeight

                    if (barHeight > 0) {
                        val barColor = if (monthSpend.isCurrentMonth) primaryColor else secondaryBarColor
                        drawRoundRect(
                            color = barColor,
                            topLeft = Offset(left, top),
                            size = Size(barWidth, barHeight),
                            cornerRadius = cornerRadius
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Month Labels & Amount Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            months.forEach { month ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = month.monthLabel,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (month.isCurrentMonth) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 11.sp
                        ),
                        color = if (month.isCurrentMonth) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (month.totalAmount > 0) {
                        Text(
                            text = CurrencyUtils.formatInr(month.totalAmount),
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1
                        )
                    } else {
                        Text(
                            text = "-",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                }
            }
        }
    }
}
