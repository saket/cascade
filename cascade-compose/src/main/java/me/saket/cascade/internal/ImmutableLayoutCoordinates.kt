package me.saket.cascade.internal

import androidx.compose.runtime.Immutable
import androidx.compose.ui.layout.LayoutCoordinates

@Immutable
@JvmInline
internal value class ImmutableLayoutCoordinates(
  val coordinates: LayoutCoordinates
)
