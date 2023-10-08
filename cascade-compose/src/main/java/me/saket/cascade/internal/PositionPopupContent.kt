package me.saket.cascade.internal

import android.os.Build
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.round
import androidx.compose.ui.unit.toOffset
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import kotlin.math.roundToInt
import android.graphics.Rect as AndroidRect

@Composable
@Suppress("NAME_SHADOWING")
internal fun PositionPopupContent(
  modifier: Modifier = Modifier,
  positionProvider: PopupPositionProvider,
  anchorBounds: ScreenRelativeBounds?,
  properties: PopupProperties,
  content: @Composable () -> Unit
) {
  val popupView = LocalView.current
  val layoutDirection = LocalLayoutDirection.current
  var contentPosition: IntOffset? by remember { mutableStateOf(null) }

  if (Build.VERSION.SDK_INT >= 29 && properties.excludeFromSystemGesture) {
    contentPosition?.let { contentPosition ->
      LaunchedEffect(contentPosition) {
        popupView.systemGestureExclusionRects = mutableListOf(
          AndroidRect(0, 0, contentPosition.x, contentPosition.y)
        )
      }
    }
  }

  Box(modifier) {
    Box(
      Modifier
        .onGloballyPositioned { coordinates ->
          if (anchorBounds != null) {
            val popupContentBounds = ScreenRelativeBounds(coordinates, owner = popupView)
            // todo: run these calculations only if something changes?
            val positionInAnchorWindow = positionProvider.calculatePosition(
              anchorBounds = anchorBounds.boundsInWindow.round(),
              windowSize = anchorBounds.root.windowBoundsMinusIme.size.round(),
              layoutDirection = layoutDirection,
              popupContentSize = coordinates.size,
            )
            // Material3's DropdownMenuPositionProvider was written to calculate
            // a position in the anchor's window. Cascade will have to adjust the
            // position to use it inside the popup's window.
            val relativePositionInAnchorWindow = ScreenRelativeOffset(
              positionInWindow = positionInAnchorWindow.toOffset(),
              root = anchorBounds.root
            )
            contentPosition = relativePositionInAnchorWindow
              .positionInWindowOf(popupContentBounds)
              .round()
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

private fun Rect.round(): IntRect {
  return IntRect(topLeft = topLeft.round(), bottomRight = bottomRight.round())
}

private fun Size.round(): IntSize {
  return IntSize(width = width.roundToInt(), height = height.roundToInt())
}
