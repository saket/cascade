@file:Suppress("unused")

package me.saket.cascade

import android.view.Menu
import android.view.MenuItem
import android.view.SubMenu
import androidx.annotation.StringRes
import androidx.core.view.iterator

// ==================================================================
// This file contains extensions that make it easy to manage toolbar
// menus from Kotlin, something that cascade wants to encourage.
// ==================================================================

/**
 * Like [Menu.children], but recursively includes items from sub-menus as well.
 */
@OptIn(ExperimentalStdlibApi::class)
val Menu.allChildren: List<MenuItem>
  get() {
    val menu = this
    return buildList {
      for (item in menu) {
        add(item)
        if (item.hasSubMenu()) {
          addAll(item.subMenu.allChildren)
        }
      }
    }
  }

fun Menu.add(
  title: CharSequence,
  itemId: Int = Menu.NONE,
  groupId: Int = Menu.NONE,
  order: Int = Menu.NONE,
  onClick: ((MenuItem) -> Unit)? = null
): MenuItem = add(groupId, itemId, order, title).apply {
  if (onClick != null) {
    setOnMenuItemClickListener {
      onClick(it)
      true
    }
  }
}

fun Menu.add(
  @StringRes titleRes: Int,
  itemId: Int = Menu.NONE,
  groupId: Int = Menu.NONE,
  order: Int = Menu.NONE,
  onClick: ((MenuItem) -> Unit)? = null
): MenuItem = add(groupId, itemId, order, titleRes).apply {
  if (onClick != null) {
    setOnMenuItemClickListener {
      onClick(it)
      true
    }
  }
}

fun Menu.addSubMenu(
  title: CharSequence,
  groupId: Int = Menu.NONE,
  itemId: Int = Menu.NONE,
  order: Int = Menu.NONE
): SubMenu {
  return addSubMenu(groupId, itemId, order, title)
}

fun Menu.addSubMenu(
  @StringRes titleRes: Int,
  groupId: Int = Menu.NONE,
  itemId: Int = Menu.NONE,
  order: Int = Menu.NONE
): SubMenu {
  return addSubMenu(groupId, itemId, order, titleRes)
}
