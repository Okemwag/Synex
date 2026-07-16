package com.synex.core.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun PerformanceChart(
    values: List<Double>,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .height(170.dp)
            .background(Color.White.copy(alpha = 0.04f), RoundedCornerShape(18.dp)),
    ) {
        if (values.size < 2) return@Box
        Canvas(Modifier.fillMaxSize()) {
            val min = values.min()
            val range = (values.max() - min).takeIf { it > 0 } ?: 1.0
            val horizontalPadding = 8.dp.toPx()
            val verticalPadding = 18.dp.toPx()
            val width = size.width - horizontalPadding * 2
            val height = size.height - verticalPadding * 2
            val points = values.mapIndexed { index, value ->
                Offset(
                    x = horizontalPadding + width * index / values.lastIndex,
                    y = verticalPadding + height * (1f - ((value - min) / range).toFloat()),
                )
            }
            val line = Path().apply {
                moveTo(points.first().x, points.first().y)
                points.drop(1).forEach { lineTo(it.x, it.y) }
            }
            val fill = Path().apply {
                addPath(line)
                lineTo(points.last().x, size.height)
                lineTo(points.first().x, size.height)
                close()
            }
            drawPath(
                path = fill,
                brush = Brush.verticalGradient(
                    listOf(SynexGreen.copy(alpha = 0.35f), Color.Transparent),
                    startY = 0f,
                    endY = size.height,
                ),
            )
            drawPath(
                path = line,
                color = Color(0xFFAED39F),
                style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round),
            )
            drawCircle(Color.White, radius = 4.dp.toPx(), center = points.last())
            drawCircle(SynexGreen, radius = 2.dp.toPx(), center = points.last())
        }
    }
}
