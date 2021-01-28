package me.saket.cascade

import android.view.MenuItem

/**
 * A navigator that can be used for setting up a menu's navigation before an instance of
 * [CascadePopupMenu] can be created. This is especially useful for overriding Toolbar's
 * popup menus where a [CascadePopupMenu] can only be created *after* a menu item is clicked
 * (to maintain backwards compatibility with PopupMenu).
 *
 * Example usage:
 *
 * ```
 * val navigator = CascadeBackNavigator()
 * toolbar.menu.addSubMenu("Are you sure?").apply {
 *   add("Cancel").setOnMenuItemClickListener {
 *     backNavigator.navigateBack()
 *   }
 * }
 *
 * toolbar.overrideAllPopupMenus { context, anchor ->
 *   CascadePopupMenu(context, anchor, backNavigator = navigator)
 * }
 * ```
 */
class CascadeBackNavigator {
  internal var onBackNavigate: (() -> Unit)? = null

  /** @return A useless boolean for use with [MenuItem.setOnMenuItemClickListener] .*/
  fun navigateBack(): Boolean {
    onBackNavigate?.invoke()
    return true
  }
}
