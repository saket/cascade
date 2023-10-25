package me.saket.cascade

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
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
class CascadeState internal constructor() {
  private val backStack = mutableStateListOf<CascadeBackStackEntry>()

  fun navigateBack() {
    backStack.removeLast()
  }

  fun resetBackStack() {
    backStack.clear()
  }

  fun canNavigateBack(): Boolean {
    return backStack.isNotEmpty()
  }

  internal fun navigateTo(entry: CascadeBackStackEntry) {
    if (entry != backStack.lastOrNull()) {
      backStack.add(entry)
    }
  }

  internal fun backStackSnapshot(): BackStackSnapshot {
    return BackStackSnapshot(
      topMostEntry = backStack.lastOrNull(),
      backStackSize = backStack.size
    )
  }
}

@Immutable
internal data class CascadeBackStackEntry(
  val header: @Composable CascadeColumnScope.() -> Unit,
  val childrenContent: @Composable CascadeColumnScope.() -> Unit
)

@Immutable
internal data class BackStackSnapshot(
  val topMostEntry: CascadeBackStackEntry?,
  val backStackSize: Int,
) {
  fun hasParentMenu(): Boolean = backStackSize > 0
}
