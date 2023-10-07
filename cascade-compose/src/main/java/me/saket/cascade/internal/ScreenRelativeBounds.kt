package me.saket.cascade.internal

import android.view.View
import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.findRootCoordinates
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.unit.toSize

@Immutable
internal data class ScreenRelativeBounds(
  val boundsInRoot: Rect,
  val root: RootLayoutCoordinatesInfo,
) {
  val boundsInWindow: Rect
    get() = Rect(
      offset = boundsInRoot.topLeft + root.layoutPositionInWindow,
      size = boundsInRoot.size,
    )
}

@Immutable
internal data class ScreenRelativeOffset(
  val positionInWindow: Offset,
  val root: RootLayoutCoordinatesInfo,
) {
  val positionInRoot: Offset
    get() = positionInWindow - root.layoutPositionInWindow
}

@Immutable
internal data class RootLayoutCoordinatesInfo(
  /** The boundaries of this layout relative to the window's origin. */
  val layoutPositionInWindow: Offset,
  val windowBoundsOnScreen: Rect,
)

internal fun ScreenRelativeBounds(coordinates: LayoutCoordinates, owner: View): ScreenRelativeBounds {
  return ScreenRelativeBounds(
    boundsInRoot = Rect(
      offset = coordinates.positionInRoot(),
      size = coordinates.size.toSize()
    ),
    root = RootLayoutCoordinatesInfo(
      // material3 uses View#getWindowVisibleDisplayFrame() for calculating window size,
      // but that produces infinite-like values for windows that have FLAG_LAYOUT_NO_LIMITS
      // set (source: WindowLayout.java). material3 ends up looking okay because WindowManager
      // sanitizes bad values.
      layoutPositionInWindow = coordinates.findRootCoordinates().positionInWindow(),
      windowBoundsOnScreen = intArrayBuffer.let {
        owner.rootView.getLocationOnScreen(it)
        Rect(
          offset = Offset(x = it[0].toFloat(), y = it[1].toFloat()),
          size = Size(owner.rootView.width.toFloat(), owner.rootView.height.toFloat()),
        )
      }
    )
  )
}

// I don't expect this to be shared across threads to need any synchronization.
private val intArrayBuffer = IntArray(size = 2)

/**
 * Calculate a position in another window such that its visual location on screen
 * remains unchanged. That is, its offset from screen's 0,0 remains the same.
 * */
internal fun ScreenRelativeOffset.positionInWindowOf(other: ScreenRelativeBounds): Offset {
  return positionInRoot -
    (other.root.layoutPositionInWindow - root.layoutPositionInWindow) -
    (other.root.windowBoundsOnScreen.topLeft - root.windowBoundsOnScreen.topLeft)
}
