package me.saket.cascade

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember

@Composable
fun rememberCascadeState(): CascadeState {
  return remember { CascadeState() }
}

/**
 * The state of a [CascadeDropdownMenu].
 */
@Stable
class CascadeState {
  private val backStack = mutableStateListOf<CascadeBackStackEntry>()

  fun navigateBack(): Boolean {
    val removed = backStack.removeLastOrNull()
    return removed != null
  }

  fun resetBackStack() {
    backStack.clear()
  }

  internal fun navigateTo(entry: CascadeBackStackEntry) {
    backStack.add(entry)
  }

  internal fun backStackSnapshot(): BackStackSnapshot {
    return BackStackSnapshot(
      topMostEntry = backStack.lastOrNull(),
      backStackSize = backStack.size
    )
  }
}

internal class CascadeBackStackEntry(
  val header: @Composable () -> Unit,
  val childrenContent: @Composable CascadeScope.() -> Unit
)

internal data class BackStackSnapshot(
  val topMostEntry: CascadeBackStackEntry?,
  val backStackSize: Int,
)
