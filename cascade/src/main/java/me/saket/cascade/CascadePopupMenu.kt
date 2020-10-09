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
import android.view.ViewOutlineProvider
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

open class CascadePopupMenu @JvmOverloads constructor(
  private val context: Context,
  private val anchor: View,
  private var gravity: Int = Gravity.NO_GRAVITY,
  private val styler: Styler = Styler(),
  private val fixedWidth: Int = context.dip(196),
  private val defStyleAttr: Int = android.R.style.Widget_Material_PopupMenu
) {

  val menu: Menu = MenuBuilder(context)
  private val popup = CascadePopupWindow(context, defStyleAttr)
  private val backstack = Stack<Menu>()
  private val themeAttrs get() = popup.themeAttrs

  class Styler(
    val background: () -> Drawable? = { null },
    val menuList: (RecyclerView) -> Unit = {},
    val menuTitle: (MenuHeaderViewHolder) -> Unit = {},
    val menuItem: (MenuItemViewHolder) -> Unit = {}
  )

  fun show() {
    // PopupWindow moves the popup to align with the anchor if a fixed width
    // is known before hand. Note to self: If fixedWidth ever needs to be
    // removed, copy over MenuPopup.measureIndividualMenuWidth().
    popup.width = fixedWidth
    popup.setMargins(left = context.dip(4), right = context.dip(4))
    styler.background()?.let {
      popup.contentView.background = it
    }

    showMenu(menu, goingForward = true)
    popup.showAsDropDown(anchor, 0, 0, gravity)
  }

  /**
   * Navigate to the last menu.
   *
   * FYI jumping over multiple back-stack entries isn't supported
   * very well, so avoid navigating multiple menus on a single click.
   */
  fun navigateBack() {
    if (backstack.isNotEmpty() && backstack.peek() is SubMenu) {
      val currentMenu = backstack.pop() as SubMenuBuilder
      showMenu(currentMenu.parentMenu, goingForward = false)
    }
  }

  private fun showMenu(menu: Menu, goingForward: Boolean) {
    val menuList = RecyclerView(context).apply {
      layoutManager = LinearLayoutManager(context)
      isVerticalScrollBarEnabled = true
      scrollBarStyle = SCROLLBARS_INSIDE_OVERLAY
      addOnScrollListener(OverScrollIfContentScrolls())
      styler.menuList(this)
      adapter = CascadeMenuAdapter(menu, styler, themeAttrs,
        onTitleClick = { navigateBack() },
        onItemClick = { handleItemClick(it) }
      )

      // Opaque background to avoid cross-drawing
      // of menus during entry/exit animation.
      if (menu is SubMenu) {
        background = styler.background() ?: themeAttrs.popupBackground(context)
        outlineProvider = ViewOutlineProvider.BACKGROUND
        clipToOutline = true
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
