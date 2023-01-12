package me.saket.cascade.internal

import androidx.compose.runtime.State
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.clipPath

fun Modifier.clipReveal(
  shape: Shape,
  transitionProgress: State<Float>
): Modifier {
  if (transitionProgress.value == 1f) {
    return this
  }

  val path = Path()
  return drawWithCache {
    path.asAndroidPath().rewind() // rewind() is faster than reset().

    val outline = shape.createOutline(
      size = size,
      layoutDirection = layoutDirection,
      density = this
    )
    val rect = when (outline) {
      is Outline.Generic,
      is Outline.Rectangle -> {
        RoundRect(
          rect = Rect(Offset.Zero, size = size.copy(height = size.height * transitionProgress.value)),
          cornerRadius = CornerRadius.Zero
        )
      }

      is Outline.Rounded -> {
        RoundRect(
          rect = Rect(Offset.Zero, size = size.copy(height = size.height * transitionProgress.value)),
          topLeft = outline.roundRect.topLeftCornerRadius,
          topRight = outline.roundRect.topRightCornerRadius,
          bottomRight = outline.roundRect.bottomRightCornerRadius,
          bottomLeft = outline.roundRect.bottomLeftCornerRadius,
        )
      }
    }
    path.addRoundRect(rect)

    onDrawWithContent {
      clipPath(path) {
        this@onDrawWithContent.drawContent()
      }
    }
  }
}
