package me.saket.cascade.internal

import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
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
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

private const val InTransitionDuration = 300
private const val OutTransitionDuration = 300

@Composable
internal fun AnimatedPopupContent(
  expandedStates: MutableTransitionState<Boolean>,
  transformOriginState: MutableState<TransformOrigin>,
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

  val shape = MaterialTheme.shapes.extraSmall

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
    Modifier.graphicsLayer {
      scaleX = scale
      scaleY = scale
      transformOrigin = transformOriginState.value
    }
  ) {
    val path = remember { Path() }
    Box(
      Modifier
        .matchParentSize()
        .drawWithCache {  // todo: no-op this out when alpha=1f
          // rewind() is faster than reset().
          path
            .asAndroidPath()
            .rewind()
          path.addOutline(
            clippingShape.createOutline(
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
        // todo: it feels weird that shadow is inside AnimatedPopupContent.
        .shadow(
          elevation = 20.dp,
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
