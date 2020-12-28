@file:SuppressLint("RestrictedApi")

package me.saket.cascade

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.OnHierarchyChangeListener
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.widget.ActionMenuView
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.Toolbar
import androidx.core.view.forEach
import java.util.ArrayDeque

/**
 * Steals overflow menu clicks from a Toolbar for showing a [CascadePopupMenu] instead
 * of the native [PopupMenu]. It's safe to call this before any menu is inflated.
 */
fun Toolbar.overrideOverflowMenu(with: (Context, anchor: View) -> CascadePopupMenu) {
  onOverflowMenuClick { button ->
    val cascade = with(context, button)
    check(!cascade.popup.isShowing)
    cascade.menuBuilder = this.menu as MenuBuilder
    cascade.show()
  }
}

private fun Toolbar.onOverflowMenuClick(onClick: View.OnClickListener) {
  val isOverflowButton = { v: View ->
    (v.layoutParams as? ActionMenuView.LayoutParams)?.isOverflowButton == true
  }

  // Toolbar lazily instantiates its children so may have to wait
  // for them to show up, in case a menu _with_ an overflow button
  // hasn't been created yet.
  //
  // It's safe to hold a reference to the overflow button and
  // never update it again. Once the overflow button is created,
  // it never gets replaced, even if the menu is cleared/replaced.
  findOrWaitForChild<ActionMenuView> {
    it.findOrWaitForChild(isOverflowButton) {
      it.setOnClickListener(onClick)

      // Disable drag-to-show-popup touch listener which uses native PopupMenu.
      it.setOnTouchListener(null)
    }
  }
}

private inline fun <reified T> ViewGroup.findOrWaitForChild(
  crossinline predicate: (T) -> Boolean = { true },
  crossinline action: (T) -> Unit
) {
  val child = findChild(predicate)
  if (child != null) action(child)
  else waitForChildAdd(predicate, action)
}

private inline fun <reified T> ViewGroup.findChild(predicate: (T) -> Boolean): T? {
  val queue = ArrayDeque<View>()
  queue.addFirst(this)

  while (true) {
    val current = queue.poll()
    when {
      current == null -> return null
      current is T && predicate(current) -> return current
      current is ViewGroup -> current.forEach { queue.addLast(it) }
    }
  }
}

private inline fun <reified T> ViewGroup.waitForChildAdd(
  crossinline predicate: (T) -> Boolean,
  crossinline action: (T) -> Unit
) {
  setOnHierarchyChangeListener(object : OnHierarchyChangeListener {
    override fun onChildViewAdded(parent: View, child: View) {
      findChild(predicate)?.let {
        action(it)
        setOnHierarchyChangeListener(null)
      }
    }

    override fun onChildViewRemoved(parent: View, child: View) = Unit
  })
}
