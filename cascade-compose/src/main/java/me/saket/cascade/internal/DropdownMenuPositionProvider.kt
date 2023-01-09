package me.saket.cascade.internal

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

@Immutable
internal data class DropdownMenuPositionProvider(
  private val contentOffset: DpOffset,
  private val density: Density,
) {
  fun calculatePosition(
    anchorBounds: IntRect,
    windowBounds: IntRect,
    layoutDirection: LayoutDirection,
    popupContentSize: IntSize
  ): IntOffset {
    // The min margin above and below the menu, relative to the screen.
    val verticalMargin = with(density) { MenuVerticalMargin.roundToPx() }
    // The content offset specified using the dropdown offset parameter.
    val contentOffsetX = with(density) { contentOffset.x.roundToPx() }
    val contentOffsetY = with(density) { contentOffset.y.roundToPx() }

    // Compute horizontal position.
    val toRight = anchorBounds.left + contentOffsetX
    val toLeft = anchorBounds.right - contentOffsetX - popupContentSize.width
    val toDisplayRight = windowBounds.width - popupContentSize.width
    val toDisplayLeft = 0
    val x = if (layoutDirection == LayoutDirection.Ltr) {
      sequenceOf(
        toRight,
        toLeft,
        // If the anchor gets outside of the window on the left, we want to position
        // toDisplayLeft for proximity to the anchor. Otherwise, toDisplayRight.
        if (anchorBounds.left >= 0) toDisplayRight else toDisplayLeft
      )
    } else {
      sequenceOf(
        toLeft,
        toRight,
        // If the anchor gets outside of the window on the right, we want to position
        // toDisplayRight for proximity to the anchor. Otherwise, toDisplayLeft.
        if (anchorBounds.right <= windowBounds.width) toDisplayLeft else toDisplayRight
      )
    }.firstOrNull {
      it >= 0 && it + popupContentSize.width <= windowBounds.width
    } ?: toLeft

    // Compute vertical position.
    val toBottom = maxOf(anchorBounds.top + contentOffsetY, verticalMargin) - windowBounds.top
    val toTop = anchorBounds.top - contentOffsetY - popupContentSize.height
    val toCenter = anchorBounds.top - popupContentSize.height / 2
    val toDisplayBottom = windowBounds.height - popupContentSize.height - verticalMargin
    val y = sequenceOf(toBottom, toTop, toCenter, toDisplayBottom).firstOrNull {
      it >= verticalMargin &&
        it + popupContentSize.height <= windowBounds.height - verticalMargin
    } ?: toTop

    return IntOffset(x, y)
  }
}

internal val MenuVerticalMargin = 0.dp
