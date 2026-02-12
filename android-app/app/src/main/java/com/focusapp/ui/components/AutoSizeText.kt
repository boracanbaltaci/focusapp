package com.focusapp.ui.components

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

/**
 * Measures the full display text at reference font size and calculates a scale factor
 * so that the text fits within the available width. Returns scale factor (0.0 - 1.0).
 * 
 * Usage: Measure the combined text at the largest font size, get scale factor,
 * then apply that same factor to all text elements proportionally.
 */
@Composable
fun ProportionalScaleBox(
    referenceText: String,
    referenceStyle: TextStyle,
    referenceFontSize: TextUnit = 240.sp,
    minScaleFactor: Float = 0.25f,
    modifier: Modifier = Modifier,
    content: @Composable (scaleFactor: Float) -> Unit
) {
    val textMeasurer = rememberTextMeasurer()

    BoxWithConstraints(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        val maxWidthPx = constraints.maxWidth

        val scaleFactor = remember(referenceText, referenceStyle.fontFamily, maxWidthPx, referenceFontSize) {
            val testStyle = referenceStyle.copy(
                fontSize = referenceFontSize,
                lineHeight = referenceFontSize
            )
            val measurement = textMeasurer.measure(
                text = referenceText,
                style = testStyle,
                maxLines = 1
            )
            if (measurement.size.width <= maxWidthPx) {
                1f
            } else {
                (maxWidthPx.toFloat() / measurement.size.width).coerceAtLeast(minScaleFactor)
            }
        }

        content(scaleFactor)
    }
}
