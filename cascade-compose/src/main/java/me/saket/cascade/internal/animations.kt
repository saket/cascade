@file:OptIn(ExperimentalAnimationApi::class)

package me.saket.cascade.internal

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import me.saket.cascade.BackStackSnapshot

internal fun AnimatedContentScope<BackStackSnapshot>.cascadeTransitionSpec(): ContentTransform {
  val navigatingForward = targetState.backStackSize > initialState.backStackSize
  val initialOffset = { width: Int ->
    if (navigatingForward) width else -width / 4
  }
  val targetOffset = { width: Int ->
    if (navigatingForward) -width / 4 else width
  }

  val duration = 350
  return ContentTransform(
    targetContentEnter = slideInHorizontally(tween(duration), initialOffset),
    initialContentExit = slideOutHorizontally(tween(duration), targetOffset),
    targetContentZIndex = targetState.backStackSize.toFloat(),
    sizeTransform = SizeTransform(sizeAnimationSpec = { _, _ -> tween(duration) })
  )
}
