package me.saket.cascade.internal

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ScreenRelativeOffsetTest {
  @Test fun canary() {
    val first = ScreenRelativeOffset(
      positionInRoot = Offset(544f, 200f),
      root = RootLayoutCoordinatesInfo(
        layoutBoundsInWindow = Rect.Zero,
        windowPositionOnScreen = Offset.Zero
      )
    )
    val second = ScreenRelativeBounds(
      boundsInRoot = Rect(0f, 0f, 515f, 630f),
      root = RootLayoutCoordinatesInfo(
        layoutBoundsInWindow = Rect.Zero,
        windowPositionOnScreen = Offset(1f, 63f)
      )
    )
    assertThat(first.positionInWindowOf(second)).isEqualTo(Offset(543f, 137f))
  }
}
