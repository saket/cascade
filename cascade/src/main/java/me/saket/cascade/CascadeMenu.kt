@file:SuppressLint("RestrictedApi")
@file:Suppress("DeprecatedCallableAddReplaceWith")

package me.saket.cascade

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.SubMenu
import android.view.View
import android.view.View.SCROLLBARS_INSIDE_OVERLAY
import android.view.ViewGroup.LayoutParams
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.annotation.MenuRes
import androidx.appcompat.view.SupportMenuInflater
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.view.menu.MenuItemImpl
import androidx.appcompat.view.menu.SubMenuBuilder
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.Stack
import kotlin.DeprecationLevel.ERROR

open class CascadeMenu @JvmOverloads constructor(
  private val context: Context,
  private val anchor: View,
  private var gravity: Int = Gravity.NO_GRAVITY,
  private val styler: Styler = Styler(),
  private val fixedWidth: Int = context.dip(196),
  private val defStyleAttr: Int = android.R.style.Widget_Material_PopupMenu
) {

  val menu: Menu = MenuBuilder(context)
  private val popup = CascadePopupWindow(context, defStyleAttr)
  private val themeAttrs get() = popup.themeAttrs
  private val backstack = Stack<Menu>()

  class Styler(
    val background: (Drawable) -> Drawable = { it },
    val menuList: (RecyclerView) -> Unit = {},
    val menuTitle: (MenuHeaderViewHolder) -> Unit = {},
    val menuItem: (MenuItemViewHolder) -> Unit = {}
  )

  fun show() {
    popup.contentView.background = styler.background(popup.contentView.background)

    showMenu(menu, goingForward = true)
    popup.showAsDropDown(anchor, 0, 0, gravity)
  }

  fun navigateBack() {
    check(backstack.isNotEmpty()) { "There is no visible menu." }
    check(backstack.peek() is SubMenu) { "Already on the root menu. Can't go back any further." }

    val currentMenu = backstack.pop() as SubMenuBuilder
    showMenu(currentMenu.parentMenu, goingForward = false)
  }

  private fun showMenu(menu: Menu, goingForward: Boolean) {
    val menuList = RecyclerView(context).apply {
      layoutManager = LinearLayoutManager(context)
      isVerticalScrollBarEnabled = true
      scrollBarStyle = SCROLLBARS_INSIDE_OVERLAY
      styler.menuList(this)
      adapter = CascadeMenuAdapter(menu, styler, themeAttrs,
        onTitleClick = { navigateBack() },
        onItemClick = { handleItemClick(it) }
      )
      addOnScrollListener(OverScrollIfContentScrolls())

      // Opaque background to avoid cross-drawing
      // of menus during entry/exit animation.
      if (menu is SubMenu) {
        background = themeAttrs.popupBackground(context)
      }

      // PopupWindow doesn't allow its content to have a fixed
      // width so any fixed size must be set on its children instead.
      layoutParams = LayoutParams(fixedWidth, WRAP_CONTENT)
    }

    backstack.push(menu)
    popup.contentView.show(menuList, goingForward)
  }

  protected open fun handleItemClick(item: MenuItem) {
    if (item.hasSubMenu()) {
      showMenu(item.subMenu, goingForward = true)
      return
    }

    val backstackBefore = backstack.peek()
    (item as MenuItemImpl).invoke()

    if (backstack.peek() === backstackBefore) {
      popup.dismiss()
    }
  }

// === APIs to maintain compatibility with PopupMenu === //

  fun inflate(@MenuRes menuRes: Int) =
    SupportMenuInflater(context).inflate(menuRes, menu)

  fun setOnMenuItemClickListener(listener: PopupMenu.OnMenuItemClickListener?) =
    (menu as MenuBuilder).setCallback(listener)

  fun dismiss() =
    popup.dismiss()

  @get:JvmName("getDragToOpenListener")
  @Deprecated("CascadeMenu doesn't support drag-to-open.", level = ERROR)
  val dragToOpenListener: View.OnTouchListener
    get() = error("can't")
}
