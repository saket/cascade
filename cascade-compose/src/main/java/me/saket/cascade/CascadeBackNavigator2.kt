package me.saket.cascade

/**
 * For sub-menus, cascade will automatically navigate to their parent menu when their header is
 * clicked. For manual navigation, `CascadeBackNavigator#navigateBack()` can be used.
 *
 * A navigator is available to all composables scoped to CascadeDropdownMenu:
 *
 * ```
 * CascadeDropdownMenu {
 *   DropdownMenuItem(
 *     text = { Text("Are you sure?"),
 *     children = {
 *       val scope: CascadeScope = this
 *       val navigator: CascadeBackNavigator2 = scope.cascadeNavigator
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
// TODO: find a better name that doesn't conflict with old CascadeBackNavigator.
interface CascadeBackNavigator2 {
  fun navigateBack()
}
