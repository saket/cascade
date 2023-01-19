package me.saket.cascade.internal

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed

internal fun Modifier.clickableWithoutRipple(onClick: () -> Unit) = composed {
  clickable(
    indication = null,
    interactionSource = remember { MutableInteractionSource() },
    onClick = onClick
  )
}

internal inline fun Modifier.then(predicate: Boolean, modifier: Modifier.() -> Modifier): Modifier {
  return if (predicate) modifier() else this
}
