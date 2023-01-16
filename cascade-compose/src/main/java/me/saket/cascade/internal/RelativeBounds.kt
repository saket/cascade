package me.saket.cascade.internal

import android.view.View
import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.findRootCoordinates
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.unit.toOffset

@Immutable
internal data class RelativeBounds(
  val boundsInRoot: Rect,
  val rootPositionInWindow: Offset,
  val windowPositionOnScreen: Offset
) {
  companion object {
    // todo: does this get called on every frame of the animation?
    operator fun invoke(coordinates: LayoutCoordinates, owner: View): RelativeBounds {
      return coordinates.findRootCoordinates().let { rootCoordinates ->
        RelativeBounds(
          boundsInRoot = rootCoordinates.localBoundingBoxOf(coordinates),
          rootPositionInWindow = rootCoordinates.positionInWindow(),
          windowPositionOnScreen = IntArray(size = 2).let {
            owner.rootView.getLocationOnScreen(it)
            Offset(x = it[0].toFloat(), y = it[1].toFloat())
          }
        )
      }
    }
  }
}

@Immutable
internal data class RelativePosition(
  val positionInRoot: Offset,
  val rootPositionInWindow: Offset,
  val windowPositionOnScreen: Offset,
) {
  fun alignedWithRootOf(other: RelativeBounds): RelativePosition {
    return RelativePosition(
      positionInRoot = positionInRoot.minus(
        other.rootPositionInWindow - rootPositionInWindow
      ).minus(
        other.windowPositionOnScreen - windowPositionOnScreen
      ),
      rootPositionInWindow = other.rootPositionInWindow,
      windowPositionOnScreen = other.windowPositionOnScreen,
    )
  }

  companion object {
    operator fun invoke(coordinates: LayoutCoordinates, owner: View): RelativePosition {
      return RelativePosition(
        positionInRoot = coordinates.positionInRoot(),
        rootPositionInWindow = coordinates.findRootCoordinates().positionInWindow(),
        windowPositionOnScreen = IntArray(size = 2).let {
          owner.rootView.getLocationOnScreen(it)
          Offset(x = it[0].toFloat(), y = it[1].toFloat())
        }
      )
    }
  }
}

internal fun Offset.relativeTo(other: RelativeBounds): RelativePosition {
  return RelativePosition(
    positionInRoot = this,
    rootPositionInWindow = other.rootPositionInWindow,
    windowPositionOnScreen = other.windowPositionOnScreen,
  )
}
