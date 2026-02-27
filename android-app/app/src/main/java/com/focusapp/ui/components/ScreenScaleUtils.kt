package com.focusapp.ui.components

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Reference screen width in dp (based on typical Android emulator / Pixel 6).
 * All dp and sp values in the app were originally designed for this width.
 */
private const val REFERENCE_WIDTH_DP = 411f

/**
 * Scale factor limits:
 * - MAX_SCALE = 1.0: Elements never grow beyond the emulator reference size
 * - MIN_SCALE = 0.7: Prevents elements from becoming too small on very small screens
 */
private const val MIN_SCALE = 0.7f
private const val MAX_SCALE = 1.0f

/**
 * CompositionLocal that provides the current screen scale factor.
 * Default is 1.0f (no scaling).
 */
val LocalScreenScale = compositionLocalOf { 1f }

/**
 * Wraps content with a screen-scale-aware provider.
 * Calculates a scale factor based on the current screen width compared to the reference width.
 *
 * Usage:
 * ```
 * ScreenScaleProvider {
 *     val scale = LocalScreenScale.current
 *     Text(fontSize = 18.sp * scale)
 *     Box(modifier = Modifier.padding((32.dp * scale).coerceAtLeast(8.dp)))
 * }
 * ```
 */
@Composable
fun ScreenScaleProvider(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    BoxWithConstraints(modifier = modifier) {
        val widthDp = maxWidth
        val scale = remember(widthDp) {
            (widthDp.value / REFERENCE_WIDTH_DP).coerceIn(MIN_SCALE, MAX_SCALE)
        }
        CompositionLocalProvider(LocalScreenScale provides scale) {
            content()
        }
    }
}

/**
 * Extension: scales a Dp value by the given factor, with an optional minimum.
 */
fun Dp.scaled(factor: Float, min: Dp = 0.dp): Dp {
    return (this * factor).coerceAtLeast(min)
}

/**
 * Extension: scales a TextUnit (sp) value by the given factor, with an optional minimum.
 */
fun TextUnit.scaled(factor: Float, min: TextUnit = 0.sp): TextUnit {
    val result = this * factor
    return if (min.value > 0f && result < min) min else result
}
