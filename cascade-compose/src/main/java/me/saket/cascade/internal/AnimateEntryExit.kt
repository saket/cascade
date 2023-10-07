package me.saket.cascade.internal

import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.addOutline
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection

private const val InTransitionDuration = 300
private const val OutTransitionDuration = 300

@Composable
internal fun AnimateEntryExit(
  modifier: Modifier = Modifier,
  expandedStates: MutableTransitionState<Boolean>,
  transformOriginState: State<TransformOrigin>,
  shadowElevation: Dp,
  shape: Shape,
  content: @Composable () -> Unit
) {
  val isExpandedTransition = updateTransition(expandedStates, label = "CascadeDropDownMenu")
  val scale by isExpandedTransition.animateFloat(
    transitionSpec = {
      tween(if (false isTransitioningTo true) InTransitionDuration else OutTransitionDuration)
    },
    label = "scale",
    targetValueByState = { if (it) 1f else 0f }
  )
  val alpha by isExpandedTransition.animateFloat(
    transitionSpec = {
      tween(if (false isTransitioningTo true) InTransitionDuration else OutTransitionDuration)
    },
    label = "alpha",
    targetValueByState = { if (it) 1f else 0f }
  )
  val reveal by isExpandedTransition.animateFloat(
    transitionSpec = {
      tween((if (false isTransitioningTo true) InTransitionDuration * 1.2 else OutTransitionDuration * 0.8).toInt())
    },
    label = "clip",
    targetValueByState = { if (it) 1f else 0.25f }
  )

  val clippingShape = remember(reveal) {
    object : Shape {
      override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
      ): Outline {
        val outline = shape.createOutline(
          size = size,
          layoutDirection = layoutDirection,
          density = density
        )
        return when (outline) {
          is Outline.Generic,
          is Outline.Rectangle -> {
            Outline.Rectangle(
              Rect(Offset.Zero, size = size.copy(height = size.height * reveal))
            )
          }

          is Outline.Rounded -> {
            Outline.Rounded(
              RoundRect(
                rect = Rect(Offset.Zero, size = size.copy(height = size.height * reveal)),
                topLeft = outline.roundRect.topLeftCornerRadius,
                topRight = outline.roundRect.topRightCornerRadius,
                bottomRight = outline.roundRect.bottomRightCornerRadius,
                bottomLeft = outline.roundRect.bottomLeftCornerRadius,
              )
            )
          }
        }
      }
    }
  }

  Box(
    modifier.scale(scale, transformOrigin = transformOriginState.value)
  ) {
    // Drop shadows and content are drawn in separate sibling layouts because:
    //
    // - shadow().alpha() will not apply alpha to shadows.
    //
    // - alpha().shadow() will cause shadows to get clipped of content bounds
    //   because its usage of graphicsLayer().
    //
    // - shadow() applied on the parent will cause shadows to get clipped outside
    //   of Popup's bounds, e.g., behind the status bar.
    //
    // FWIW material3.DropdownMenu() also suffers from these same problems. Its
    // shadows get clipped during entry/exit transitions, but the 8dp shadows are
    // small enough for the clipping to go unnoticed.
    Box(
      Modifier
        .matchParentSize()
        // Because the drop shadows are drawn separately from the popup's content,
        // this layout's inner shadows must be clipped out to prevent it from
        // showing up behind the translucent content.
        .then(alpha < 1f) { clipDifference(clippingShape) }
        .shadow(
          elevation = shadowElevation,
          shape = clippingShape,
          clip = false,
          ambientColor = Color.Black.copy(alpha = alpha),
          spotColor = Color.Black.copy(alpha = alpha),
        )
    )

    Box(
      Modifier
        .alpha(alpha)
        .clip(clippingShape)
    ) {
      content()
    }
  }
}

// Like Modifier.clip() but uses ClipOp.Difference instead of ClipOp.Intersect.
private fun Modifier.clipDifference(shape: Shape): Modifier = composed {
  val path = remember { Path() }
  drawWithCache {
    path.asAndroidPath().rewind() // rewind() is faster than reset().
    path.addOutline(
      shape.createOutline(
        size = size,
        layoutDirection = layoutDirection,
        density = this
      )
    )
    onDrawWithContent {
      clipPath(path, ClipOp.Difference) {
        this@onDrawWithContent.drawContent()
      }
    }
  }
}

@Stable
private fun Modifier.scale(scale: Float, transformOrigin: TransformOrigin): Modifier {
  return if (scale != 1f) {
    graphicsLayer(
      scaleX = scale,
      scaleY = scale,
      transformOrigin = transformOrigin
    )
  } else this
}
