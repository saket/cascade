package me.saket.cascade

import android.view.Menu
import android.view.MenuItem
import androidx.core.view.children
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
