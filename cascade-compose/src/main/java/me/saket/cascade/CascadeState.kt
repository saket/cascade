package me.saket.cascade

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList

@Composable
fun rememberCascadeState(): CascadeState {
  return remember { CascadeState() }
}

@Stable
class CascadeState : CascadeBackNavigator2 {
  internal val backStack = mutableStateListOf<CascadeBackStackEntry>()

  override fun navigateBack() {
    backStack.removeLastOrNull()
  }
}

internal class CascadeBackStackEntry(
  val header: @Composable () -> Unit,
  val childrenContent: @Composable CascadeScope.() -> Unit
)

internal fun SnapshotStateList<CascadeBackStackEntry>.snapshot(): BackStackSnapshot {
  return BackStackSnapshot(topMostEntry = lastOrNull(), backStackSize = size)
}

internal data class BackStackSnapshot(
  val topMostEntry: CascadeBackStackEntry?,
  val backStackSize: Int,
)
