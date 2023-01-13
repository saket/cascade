package me.saket.cascade.internal

import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
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
import androidx.compose.ui.graphics.drawOutline
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.boundsInParent
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

private const val InTransitionDuration = 300 * 10
private const val OutTransitionDuration = 300 * 10

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

  val density = LocalDensity.current
  var contentSize by remember { mutableStateOf(DpSize.Zero) }

  val shape = MaterialTheme.shapes.extraSmall
  val color = MaterialTheme.colorScheme.surface

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
    modifier = Modifier
      .graphicsLayer {
        scaleX = scale
        scaleY = scale
        this.transformOrigin = transformOriginState.value
      }
    ,
    content = {
      Box(
        Modifier
          .matchParentSize()
          .drawWithCache {
            onDrawWithContent {
              val path = Path()
              path.asAndroidPath().rewind()
              val outline = clippingShape.createOutline(
                size = size,
                layoutDirection = layoutDirection,
                density = this
              )
              path.addOutline(outline)

              clipPath(path, clipOp = ClipOp.Difference) {
                this@onDrawWithContent.drawContent()
              }
            }
          }
          .shadow(
          elevation = 20.dp,
          shape = clippingShape,
          clip = false,
          ambientColor = Color.Black.copy(alpha = alpha),
          spotColor = Color.Black.copy(alpha = alpha),
        )
      )

      Box(
//        Modifier.onGloballyPositioned { coordinates ->
//          val bounds = coordinates.boundsInParent()
//          contentSize = density.run {
//            DpSize(width = bounds.width.toDp(), height = bounds.height.toDp())
//          }
//        }
      Modifier.alpha(alpha)
        //.clipReveal(shape = shape, transitionProgress = reveal)
        .clip(clippingShape)
      ) {
        content()
      }
    },
  )

//  val shape = MaterialTheme.shapes.extraSmall
//

//
//  Box(
//    modifier = Modifier
//      .graphicsLayer {
//        scaleX = scale
//        scaleY = scale
//        this.alpha = alpha
//        this.transformOrigin = transformOriginState.value
//      }
//      .shadow(20.dp, shape = clippingShape, clip = false)
//    ,
//    content = {
//      content()
//    }
//  )
}

private fun Modifier.clipReveal(
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
