package me.saket.cascade.internal

import android.view.View
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
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.round
import androidx.compose.ui.unit.toOffset
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties

@Composable
internal fun FullSizedPopup(
  onDismissRequest: () -> Unit,
  properties: PopupProperties,
  content: @Composable () -> Unit
) {
  Box(
    Modifier.onGloballyPositioned { coordinates ->
      val parentCoordinates = coordinates.parentLayoutCoordinates!!
      println("onGloballyPositioned() -> coordinates = ${parentCoordinates.positionInWindow()}")
    }
  )

  Popup(
    onDismissRequest = onDismissRequest,
    properties = properties,
    content = content
  )
}

@Composable
internal fun PositionPopupContent(
  modifier: Modifier = Modifier,
  positionProvider: PopupPositionProvider,
  anchorBounds: RelativeBounds?,
  anchorView: View,
  content: @Composable () -> Unit
) {
  val layoutDirection = LocalLayoutDirection.current

  var popupPosition: RelativePosition? by remember { mutableStateOf(null) }
  val windowSizeRectBuffer = remember { android.graphics.Rect(0, 0, 0, 0) }

//  anchorBounds?.let { anchorPosition ->
//    LaunchedEffect(anchorPosition) {
//      val layoutPosition = anchorPosition.coordinates.positionInWindow().let {
//        IntOffset(x = it.x.roundToInt(), y = it.y.roundToInt())
//      }
//      val layoutSize = anchorPosition.coordinates.size
//      anchorBounds = IntRect(layoutPosition, layoutSize)
//      anchorRootPosition = anchorPosition.coordinates.findRootCoordinates().positionInWindow().let {
//        IntOffset(x = it.x.roundToInt(), y = it.y.roundToInt())
//      }
//    }
//  }

  val popupView = LocalView.current

  Box(modifier) {
    Box(
      Modifier
        .onGloballyPositioned { coordinates ->
          // todo: can this be calculated from RelativeBounds?
          val anchorWindowBounds = windowSizeRectBuffer.let { rect ->
            anchorView.getWindowVisibleDisplayFrame(rect)
            IntRect(left = rect.left, top = rect.top, right = rect.right, bottom = rect.bottom)
          }
          val popupContentBounds = RelativeBounds(coordinates, owner = popupView)
          val contentSize = coordinates.size

          if (anchorBounds != null) {
            popupPosition = positionProvider.calculatePosition(
              anchorBounds = anchorBounds.boundsInRoot.round(),
              windowSize = anchorWindowBounds.size,
              layoutDirection = layoutDirection,
              popupContentSize = contentSize,
            ).let { position ->
              // todo: explain better.
              // popupPosition was calculated relative to anchor's window.
              // it must be offset to account for popup's window.
              val positionInAnchorWindow = position.toOffset().relativeTo(anchorBounds)
              positionInAnchorWindow.alignedWithRootOf(popupContentBounds)
            }
          }
        }
        .absoluteOffset {
          popupPosition?.positionInRoot?.round() ?: IntOffset.Zero
        }
        // Hide the popup while it can't be positioned correctly.
        .alpha(if (popupPosition != null) 1f else 0f)
    ) {
      content()
    }
  }
}

private fun Rect.round(): IntRect {
  return IntRect(topLeft = topLeft.round(), bottomRight = bottomRight.round())
}
