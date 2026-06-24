package com.example.example.ui.navigation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip


import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.min

enum class AnchorSide {
    LEFT, RIGHT
}

data class BeadNavItem(
    val icon: ImageVector,
    val label: String,
)

@Composable
fun BeadNavBar(
    items: List<BeadNavItem>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    anchorSide: AnchorSide = AnchorSide.LEFT,
    beadSize: Dp = 48.dp,
    beadSpacing: Dp = 56.dp,
    barHeight: Dp = beadSize + 8.dp,
    maxBarWidthFraction: Float = 0.85f,
) {
    val density = LocalDensity.current
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val configuration = LocalConfiguration.current
    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
    val maxBarWidthPx = screenWidthPx * maxBarWidthFraction

    val beadSizePx = with(density) { beadSize.toPx() }
    val beadSpacingPx = with(density) { beadSpacing.toPx() }
    val barHeightPx = with(density) { barHeight.toPx() }
    val capsuleRadiusPx = barHeightPx / 2f
    val wallPx = with(density) { 4.dp.toPx() }
    val minBarWidthPx = barHeightPx + 2f * wallPx
    val maxExtensionPx = maxBarWidthPx - minBarWidthPx

    val isLeft = anchorSide == AnchorSide.LEFT
    val dir = if (isLeft) 1f else -1f
    val beadInsetPx = wallPx

    fun beadVisualCenter(index: Int, offset: Float): Float {
        return if (isLeft) capsuleRadiusPx + beadInsetPx + offset - index * beadSpacingPx
        else capsuleRadiusPx + beadInsetPx + index * beadSpacingPx
    }

    fun closestBeadIndex(offset: Float): Int {
        var closest = 0
        var minDist = Float.MAX_VALUE
        for (i in items.indices) {
            val dist = abs(offset - i * beadSpacingPx)
            if (dist < minDist) {
                minDist = dist
                closest = i
            }
        }
        return closest
    }

    fun getOffsetForIndex(index: Int): Float {
        return index * beadSpacingPx
    }

    fun calculateBarWidth(offset: Float): Float {
        return minBarWidthPx + min(offset, maxExtensionPx)
    }

    var chainOffset by remember {
        mutableFloatStateOf(getOffsetForIndex(selectedIndex))
    }
    var barWidthPx by remember {
        mutableFloatStateOf(calculateBarWidth(getOffsetForIndex(selectedIndex)))
    }
    val snapAnim = remember { Animatable(chainOffset) }

    fun updateBarWidth(offset: Float) {
        barWidthPx = calculateBarWidth(offset)
    }

    LaunchedEffect(selectedIndex, anchorSide) {
        chainOffset = getOffsetForIndex(selectedIndex)
        barWidthPx = calculateBarWidth(chainOffset)
    }

    var pendingIndex by remember { mutableIntStateOf(selectedIndex) }
    var hapticFiredIndex by remember { mutableIntStateOf(selectedIndex) }

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = if (isLeft) Alignment.TopStart else Alignment.TopEnd
    ) {
    Surface(
        modifier = Modifier
            .padding(bottom = 24.dp, start = 4.dp, end = 4.dp)
            .width(with(density) { barWidthPx.toDp() })
            .height(barHeight),
        shape = RoundedCornerShape(barHeight / 2),
        color = MaterialTheme.colorScheme.primaryContainer,
        tonalElevation = 4.dp,
        shadowElevation = 12.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(barHeight)
                .clip(RoundedCornerShape(barHeight / 2))
                .pointerInput(items.size, anchorSide) {
                    detectDragGestures(
                        onDragStart = {
                            scope.launch { snapAnim.stop() }
                            hapticFiredIndex = closestBeadIndex(chainOffset)
                        },
                        onDragEnd = {
                            val targetIdx = pendingIndex.coerceIn(0, items.size - 1)
                            val targetOffset = getOffsetForIndex(targetIdx)
                            scope.launch {
                                snapAnim.snapTo(chainOffset)
                                snapAnim.animateTo(
                                    targetOffset,
                                    spring(
                                        dampingRatio = 0.25f,
                                        stiffness = 150f,
                                    )
                                ) {
                                    chainOffset = value
                                    updateBarWidth(value)
                                }
                                onItemSelected(targetIdx)
                            }
                        },
                        onDragCancel = {
                            scope.launch {
                                snapAnim.animateTo(chainOffset, spring()) {
                                    chainOffset = value
                                    updateBarWidth(value)
                                }
                            }
                        },
                    ) { change, dragAmount ->
                        change.consume()
                        var newOffset = chainOffset + dragAmount.x * dir
                        newOffset = newOffset.coerceIn(0f, maxExtensionPx)
                        chainOffset = newOffset
                        updateBarWidth(newOffset)

                        val closest = closestBeadIndex(chainOffset)
                        pendingIndex = closest
                        if (closest != hapticFiredIndex) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            hapticFiredIndex = closest
                        }
                    }
                },
            contentAlignment = Alignment.TopStart,
        ) {
            for (index in items.indices) {
                val vc = beadVisualCenter(index, chainOffset)
                val beadDist = abs(chainOffset - index * beadSpacingPx)
                if (vc + beadSizePx / 2 < 0 || vc - beadSizePx / 2 > barWidthPx) {
                    continue
                }
                val normDist = min(beadDist / beadSpacingPx, 1f)
                val beadAlpha = lerp(1f, 0.4f, normDist)
                val isSelected = beadDist < beadSpacingPx * 0.3f

                Box(
                    modifier = Modifier
                        .graphicsLayer {
                            translationX = vc - beadSizePx / 2
                            translationY = capsuleRadiusPx - beadSizePx / 2
                            alpha = beadAlpha
                        }
                        .size(beadSize)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.secondaryContainer
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = items[index].icon,
                        contentDescription = items[index].label,
                        modifier = Modifier.size(24.dp),
                        tint = if (isSelected) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                }
            }
        }
    }
    }
}
