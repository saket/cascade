@file:SuppressLint("RestrictedApi")

package me.saket.cascade

import android.annotation.SuppressLint
import android.content.Context
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.OnHierarchyChangeListener
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.view.menu.MenuPresenter
import androidx.appcompat.view.menu.MenuView
import androidx.appcompat.view.menu.SubMenuBuilder
import androidx.appcompat.widget.ActionMenuView
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.Toolbar
import androidx.core.view.children
import androidx.core.view.forEach
import java.lang.ref.WeakReference
import java.lang.reflect.Field
import java.util.ArrayDeque
import kotlin.LazyThreadSafetyMode.NONE

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

/**
 * WARNING: This uses reflection and isn't guaranteed to be stable.
 *
 * Steals clicks for both overflow menu button and action menu buttons from a Toolbar for
 * showing a [CascadePopupMenu] instead of the native [PopupMenu]. It's safe to call this
 * before any menu is inflated.
 */
fun Toolbar.overrideAllPopupMenus(with: (Context, anchor: View) -> CascadePopupMenu) {
  fun showMenu(anchor: View, menu: MenuBuilder): CascadePopupMenu {
    val cascade = with(context, anchor)
    check(!cascade.popup.isShowing)
    cascade.menuBuilder = menu
    cascade.show()
    return cascade
  }

  onOverflowMenuClick { button ->
    showMenu(anchor = button, menu as MenuBuilder)
  }
  onSubMenuClick { anchor, subMenu ->
    showMenu(anchor, subMenu)
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

private fun Toolbar.onSubMenuClick(showMenu: (anchor: View, SubMenuBuilder) -> CascadePopupMenu) {
  if (this.getTag(R.id.cascade_primary_menu_presenter) != null) {
    // Mole already planted.
    return
  }

  val menu = this.menu as MenuBuilder
  var isSubMenuOpen = false

  // Toolbar is very tightly coupled with native PopupMenu and doesn't offer any way to override it.
  // Cascade finds the primary presenter responsible for showing sub-menus and steals its callbacks.
  val ogPresenter = Reflection.findPrimaryMenuPresenter(menu)
  val molePresenter = object : MenuPresenter by ogPresenter {
    override fun onSubMenuSelected(subMenu: SubMenuBuilder): Boolean {
      if (!isSubMenuOpen) {
        val menuView = findViewForMenuItem(subMenu.item)
        showMenu(menuView, subMenu)
          .also { isSubMenuOpen = true }
          .popup.setOnDismissListener { isSubMenuOpen = false }
      }
      return true
    }
  }

  // Prevent the presenter from getting garbage collected.
  this.setTag(R.id.cascade_primary_menu_presenter, molePresenter)

  menu.removeMenuPresenter(ogPresenter)
  menu.addMenuPresenter(molePresenter)
}

private fun Toolbar.findViewForMenuItem(item: MenuItem): View {
  return findChild<ActionMenuView>()!!.children.first { (it as? MenuView.ItemView)?.itemData == item }
}

private object Reflection {
  private val presentersField: Field? by lazy(NONE) {
    MenuBuilder::class.java.getDeclaredField("mPresenters").also {
      it.isAccessible = true
    }
  }

  @Suppress("UNCHECKED_CAST")
  fun findPrimaryMenuPresenter(menu: MenuBuilder): MenuPresenter {
    val presenters = presentersField?.get(menu) as List<WeakReference<MenuPresenter>>?
      ?: error("Couldn't find Toolbar's primary menu presenter")

    return presenters
      .mapNotNull { it.get() }
      .firstOrNull { it::class.qualifiedName == "androidx.appcompat.widget.ActionMenuPresenter" }
      ?: error("Couldn't find Toolbar's primary menu presenter")
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

private inline fun <reified T> ViewGroup.findChild(predicate: (T) -> Boolean = { true }): T? {
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
