package me.saket.cascade.internal

import android.view.View
import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.findRootCoordinates
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.toSize

@Immutable
internal data class ScreenRelativeBounds(
  val boundsInRoot: Rect,
  val root: RootLayoutCoordinatesInfo,
)

@Immutable
internal data class ScreenRelativeOffset(
  val positionInRoot: Offset,
  val root: RootLayoutCoordinatesInfo,
)

@Immutable
internal data class RootLayoutCoordinatesInfo(
  val layoutBoundsInWindow: Rect,
  val windowPositionOnScreen: Offset,
) {
  val layoutPositionInWindow: Offset get() = layoutBoundsInWindow.topLeft
}

internal fun ScreenRelativeBounds(coordinates: LayoutCoordinates, owner: View): ScreenRelativeBounds {
  return ScreenRelativeBounds(
    boundsInRoot = Rect(
      offset = coordinates.positionInRoot(),
      size = coordinates.size.toSize()
    ),
    root = RootLayoutCoordinatesInfo(
      layoutBoundsInWindow = coordinates.findRootCoordinates().boundsInWindow(),
      windowPositionOnScreen = run {
        owner.rootView.getLocationOnScreen(intArrayBuffer)
        Offset(x = intArrayBuffer[0].toFloat(), y = intArrayBuffer[1].toFloat())
      }
    )
  )
}

// I do not expect this to be shared across threads to need any synchronization.
private val intArrayBuffer = IntArray(size = 2)

/**
 * Calculate a position in another window such that its visual location on screen
 * remains unchanged. That is, its offset from screen's 0,0 remains the same.
 * */
internal fun ScreenRelativeOffset.positionInWindowOf(other: ScreenRelativeBounds): Offset {
  return positionInRoot -
    (other.root.layoutPositionInWindow - root.layoutPositionInWindow) -
    (other.root.windowPositionOnScreen - root.windowPositionOnScreen)
}
