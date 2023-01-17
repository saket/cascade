package me.saket.cascade.internal

import android.view.View
import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.findRootCoordinates
import androidx.compose.ui.layout.positionInWindow

@Immutable
internal data class ScreenRelativeBounds(
  val boundsInRoot: Rect,
  val rootOffsetFromScreen: RootOffsetFromScreen,
)

@Immutable
internal data class ScreenRelativeOffset(
  val positionInRoot: Offset,
  val rootOffsetFromScreen: RootOffsetFromScreen,
)

@Immutable
internal data class RootOffsetFromScreen(
  val rootLayoutPositionInWindow: Offset,
  val windowPositionOnScreen: Offset,
)

internal fun ScreenRelativeBounds(coordinates: LayoutCoordinates, owner: View): ScreenRelativeBounds {
  return coordinates.findRootCoordinates().let { rootCoordinates ->
    ScreenRelativeBounds(
      boundsInRoot = rootCoordinates.localBoundingBoxOf(coordinates),
      rootOffsetFromScreen = RootOffsetFromScreen(
        rootLayoutPositionInWindow = rootCoordinates.positionInWindow(),
        windowPositionOnScreen = run {
          owner.rootView.getLocationOnScreen(intArrayBuffer)
          Offset(x = intArrayBuffer[0].toFloat(), y = intArrayBuffer[1].toFloat())
        }
      )
    )
  }
}

// I do not expect this to be shared across threads to need any synchronization.
private val intArrayBuffer = IntArray(size = 2)

/**
 * Calculate a position in another window such that its visual location on screen
 * remains unchanged. That is, its offset from screen's 0,0 remains the same.
 * */
internal fun ScreenRelativeOffset.positionInWindowOf(other: ScreenRelativeBounds): Offset {
  return positionInRoot -
    (other.rootOffsetFromScreen.rootLayoutPositionInWindow - rootOffsetFromScreen.rootLayoutPositionInWindow) -
    (other.rootOffsetFromScreen.windowPositionOnScreen - rootOffsetFromScreen.windowPositionOnScreen)
}
