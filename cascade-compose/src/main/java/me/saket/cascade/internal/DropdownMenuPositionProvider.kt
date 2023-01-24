package me.saket.cascade.internal

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupPositionProvider
import kotlin.math.max
import kotlin.math.min

/**
 * Copied from [material3](https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:compose/material3/material3/src/commonMain/kotlin/androidx/compose/material3/Menu.kt?q=file:androidx%2Fcompose%2Fmaterial3%2FMenu.kt%20class:androidx.compose.material3.DropdownMenuPositionProvider).
 */
@Immutable
internal data class DropdownMenuPositionProvider(
  val contentOffset: DpOffset,
  val density: Density,
  val onPositionCalculated: (anchorBounds: IntRect, menuBounds: IntRect) -> Unit = { _, _ -> }
) : PopupPositionProvider {

  override fun calculatePosition(
    anchorBounds: IntRect,
    windowSize: IntSize,
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
    val toDisplayRight = windowSize.width - popupContentSize.width
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
        if (anchorBounds.right <= windowSize.width) toDisplayLeft else toDisplayRight
      )
    }.firstOrNull {
      it >= 0 && it + popupContentSize.width <= windowSize.width
    } ?: toLeft

    // Compute vertical position.
    val toBottom = maxOf(anchorBounds.bottom + contentOffsetY, verticalMargin)
    val toTop = anchorBounds.top - contentOffsetY - popupContentSize.height
    val toCenter = anchorBounds.top - popupContentSize.height / 2
    val toDisplayBottom = windowSize.height - popupContentSize.height - verticalMargin
    val y = sequenceOf(toBottom, toTop, toCenter, toDisplayBottom).firstOrNull {
      it >= verticalMargin &&
        it + popupContentSize.height <= windowSize.height - verticalMargin
    } ?: toTop

    onPositionCalculated(
      anchorBounds,
      IntRect(x, y, x + popupContentSize.width, y + popupContentSize.height)
    )
    return IntOffset(x, y)
  }
}

internal val MenuVerticalMargin = 48.dp

internal fun calculateTransformOrigin(
  parentBounds: IntRect,
  menuBounds: IntRect
): TransformOrigin {
  val pivotX = when {
    menuBounds.left >= parentBounds.right -> 0f
    menuBounds.right <= parentBounds.left -> 1f
    menuBounds.width == 0 -> 0f
    else -> {
      val intersectionCenter =
        (
          max(parentBounds.left, menuBounds.left) +
            min(parentBounds.right, menuBounds.right)
          ) / 2
      (intersectionCenter - menuBounds.left).toFloat() / menuBounds.width
    }
  }
  val pivotY = when {
    menuBounds.top >= parentBounds.bottom -> 0f
    menuBounds.bottom <= parentBounds.top -> 1f
    menuBounds.height == 0 -> 0f
    else -> {
      val intersectionCenter =
        (
          max(parentBounds.top, menuBounds.top) +
            min(parentBounds.bottom, menuBounds.bottom)
          ) / 2
      (intersectionCenter - menuBounds.top).toFloat() / menuBounds.height
    }
  }
  return TransformOrigin(pivotX, pivotY)
}
