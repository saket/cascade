package me.saket.cascade.internal

import android.view.View
import androidx.compose.foundation.border
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.findRootCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.minus
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import kotlin.math.roundToInt

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
  positionProvider: DropdownMenuPositionProvider,
  anchorPosition: ImmutableLayoutCoordinates?,
  anchorView: View,
  content: @Composable () -> Unit
) {
  val layoutDirection = LocalLayoutDirection.current

  var anchorBounds by remember { mutableStateOf(IntRect.Zero) }
  var anchorRootPosition by remember { mutableStateOf(IntOffset.Zero) }
  var popupPosition by remember { mutableStateOf(IntOffset.Zero) }
  val windowSizeRectBuffer = remember { android.graphics.Rect(0, 0, 0, 0) }

  anchorPosition?.let { anchorPosition ->
    LaunchedEffect(anchorPosition) {
      val layoutPosition = anchorPosition.coordinates.positionInWindow().let {
        IntOffset(x = it.x.roundToInt(), y = it.y.roundToInt())
      }
      val layoutSize = anchorPosition.coordinates.size
      anchorBounds = IntRect(layoutPosition, layoutSize)
      anchorRootPosition = anchorPosition.coordinates.findRootCoordinates().positionInWindow().let {
        IntOffset(x = it.x.roundToInt(), y = it.y.roundToInt())
      }
    }
  }

  Box(modifier) {
    Box(
      Modifier
        .onGloballyPositioned { coordinates ->
          val rootCoordinates = coordinates.findRootCoordinates()
          println("root coords = ${rootCoordinates.positionInWindow()}")
        }
        .onSizeChanged { contentSize ->
          val windowBounds = windowSizeRectBuffer.let { rect ->
            anchorView.getWindowVisibleDisplayFrame(rect)
            IntRect(left = rect.left, top = rect.top, right = rect.right, bottom = rect.bottom)
          }

          popupPosition = positionProvider.calculatePosition(
            anchorBounds = anchorBounds,
            windowSize = windowBounds.size,
            layoutDirection = layoutDirection,
            popupContentSize = contentSize,
          ).let { position ->
            println("+++ anchor topleft = ${anchorRootPosition}, window topleft = ${windowBounds.topLeft}")
            position.minus(windowBounds.topLeft)
          }

          // popupPosition was calculated relative to anchor's window.
          // it must be offset to account for popup's window.

          println("Popup position = $popupPosition. content = $contentSize, anchor = $anchorBounds window = $windowBounds")
          //println("window inside popup = $windowBounds")
          println("------------------------------")
        }
        .absoluteOffset { popupPosition.copy(y = popupPosition.y) }
        // Hide the popup while it can't be positioned correctly.
        // todo: improve this. comparison with IntRect.Zero feels weird.
        .alpha(if (anchorBounds !== IntRect.Zero) 1f else 0f)
    ) {
      content()
    }
  }
}
