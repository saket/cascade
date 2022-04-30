package me.saket.cascade

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember

/**
 * For sub-menus, cascade will automatically navigate to their parent menu when their title is
 * clicked. For manual navigation, `CascadeBackNavigator#navigateBack()` can be used.
 *
 * A `CascadeBackNavigator` is available to all composables scoped to CascadeDropdownMenu:
 *
 * ```
 * CascadeDropdownMenu {
 *   DropdownMenuItem(
 *     text = { Text("Are you sure?"),
 *     children = {
 *       val navigator = LocalCascadeBackNavigator.current
 *
 *       DropdownMenuItem(
 *          text = { Text("Cancel") },
 *          onClick = { navigator.navigateBack() }
 *        )
 *     }
 *   )
 * }
 * ```
 */
// TODO: is a Composition Local this better than providing a navigator through a scope (e.g., CascadeDropdownMenuScope)?
//val LocalCascadeBackNavigator = compositionLocalOf<CascadeBackNavigator> {
//  error("CascadeBackNavigator is only available to composables inside CascadeDropdownMenu()")
//}

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

// TODO: find a better name that doesn't conflict with old CascadeBackNavigator.
interface CascadeBackNavigator2 {
  fun navigateBack()
}

internal class CascadeBackStackEntry(
  val header: @Composable () -> Unit,
  val pageContent: @Composable CascadeColumnScope.() -> Unit
)
