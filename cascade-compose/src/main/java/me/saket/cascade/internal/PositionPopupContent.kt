package me.saket.cascade.internal

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.round
import androidx.compose.ui.unit.toOffset
import androidx.compose.ui.window.PopupPositionProvider
import kotlin.math.roundToInt

@Composable
internal fun PositionPopupContent(
  modifier: Modifier = Modifier,
  positionProvider: PopupPositionProvider,
  anchorBounds: ScreenRelativeBounds?,
  content: @Composable () -> Unit
) {
  val popupView = LocalView.current
  val layoutDirection = LocalLayoutDirection.current
  var contentPosition: IntOffset? by remember { mutableStateOf(null) }

  Box(modifier) {
    Box(
      Modifier
        .onGloballyPositioned { coordinates ->
          val popupContentBounds = ScreenRelativeBounds(coordinates, owner = popupView)

          if (anchorBounds != null) {
            contentPosition = positionProvider
              .calculatePosition(
                anchorBounds = anchorBounds.boundsInRoot.roundToIntRect(),
                // material3 uses View#getWindowVisibleDisplayFrame() for calculating window size,
                // but that produces infinite-like values for windows that have FLAG_LAYOUT_NO_LIMITS set.
                // material3 ends up looking okay because WindowManager sanitizes bad values.
                windowSize = anchorBounds.root.layoutBoundsInWindow.intSize(),
                layoutDirection = layoutDirection,
                popupContentSize = coordinates.size,
              )
              .let { position ->
                // Material3's DropdownMenuPositionProvider was written to calculate
                // a position in the anchor's window. Cascade will have to adjust the
                // position to use it inside the popup's window.
                val positionInAnchorWindow = ScreenRelativeOffset(
                  positionInRoot = position.toOffset(),
                  root = anchorBounds.root
                )
                positionInAnchorWindow
                  .positionInWindowOf(popupContentBounds)
                  .round()
              }
          }
        }
        // Hide the popup until it can be positioned.
        .alpha(if (contentPosition != null) 1f else 0f)
        .absoluteOffset { contentPosition ?: IntOffset.Zero }
    ) {
      content()
    }
  }
}

private fun Rect.roundToIntRect(): IntRect {
  return IntRect(topLeft = topLeft.round(), bottomRight = bottomRight.round())
}

private fun Rect.intSize(): IntSize {
  return IntSize(width = width.roundToInt(), height = height.roundToInt())
}
