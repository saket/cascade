@file:OptIn(ExperimentalAnimationApi::class)

package me.saket.cascade.internal

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.LayoutDirection.Ltr
import me.saket.cascade.BackStackSnapshot

@OptIn(ExperimentalAnimationApi::class)
internal fun AnimatedContentScope<BackStackSnapshot>.cascadeTransitionSpec(
  layoutDirection: LayoutDirection
): ContentTransform {
  val navigatingForward = targetState.backStackSize > initialState.backStackSize

  val inverseMultiplier = if (layoutDirection == Ltr) 1 else -1
  val initialOffset = { width: Int ->
    inverseMultiplier * if (navigatingForward) width else -width / 4
  }
  val targetOffset = { width: Int ->
    inverseMultiplier * if (navigatingForward) -width / 4 else width
  }

  val duration = 350
  return ContentTransform(
    targetContentEnter = slideInHorizontally(tween(duration), initialOffset),
    initialContentExit = slideOutHorizontally(tween(duration), targetOffset),
    targetContentZIndex = targetState.backStackSize.toFloat(),
    sizeTransform = SizeTransform(sizeAnimationSpec = { _, _ -> tween(duration) })
  )
}
