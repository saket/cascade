package me.saket.cascade

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

internal object CascadeDefaults {
  val menuWidth = 196.dp
  val shadowElevation = 3.dp

  val shape: Shape
    @Composable get() = MaterialTheme.shapes.extraSmall
}
