package me.saket.cascade.internal

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ScreenRelativeOffsetTest {
  @Test fun canary() {
    val first = ScreenRelativeOffset(
      positionInWindow = Offset(544f, 200f),
      root = RootLayoutCoordinatesInfo(
        layoutPositionInWindow = Offset.Zero,
        windowBoundsMinusIme = Rect(offset = Offset.Zero, size = Size.Zero)
      )
    )
    val second = ScreenRelativeBounds(
      boundsInRoot = Rect(0f, 0f, 515f, 630f),
      root = RootLayoutCoordinatesInfo(
        layoutPositionInWindow = Offset.Zero,
        windowBoundsMinusIme = Rect(offset = Offset(1f, 63f), size = Size.Zero)
      )
    )
    assertThat(first.positionInWindowOf(second)).isEqualTo(Offset(543f, 137f))
  }
}
