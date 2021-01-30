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
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import androidx.annotation.MenuRes
import androidx.appcompat.view.SupportMenuInflater
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.view.menu.MenuItemImpl
import androidx.appcompat.view.menu.SubMenuBuilder
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.RecycledViewPool
import me.saket.cascade.internal.OverScrollIfContentScrolls
import me.saket.cascade.internal.dip
import me.saket.cascade.internal.setCallback
import java.util.Stack
import kotlin.DeprecationLevel.ERROR

open class CascadePopupMenu @JvmOverloads constructor(
  private val context: Context,
  private val anchor: View,
  private var gravity: Int = Gravity.NO_GRAVITY,
  private val styler: Styler = Styler(),
  private val fixedWidth: Int = context.dip(196),
  private val defStyleAttr: Int = android.R.style.Widget_Material_PopupMenu,
  private val backNavigator: CascadeBackNavigator = CascadeBackNavigator()
) {
  val menu: Menu get() = menuBuilder
  val popup = CascadePopupWindow(context, defStyleAttr)

  internal var menuBuilder = MenuBuilder(context)
  private val backstack = Stack<Menu>()
  private val themeAttrs get() = popup.themeAttrs
  private val sharedViewPool = RecycledViewPool()

  class Styler(
    /**
     * Popup's background drawable. Also used on sub-menus as an opaque background
     * to avoid cross-drawing of menus during their entry/exit transition. Return
     * `null` to use the background set in XML theme.
     */
    val background: () -> Drawable? = { null },
    val menuList: (RecyclerView) -> Unit = {},
    val menuTitle: (MenuHeaderViewHolder) -> Unit = {},
    val menuItem: (MenuItemViewHolder) -> Unit = {}
  )

  init {
    backNavigator.onBackNavigate = {
      if (backstack.isNotEmpty() && backstack.peek() is SubMenu) {
        val currentMenu = backstack.pop() as SubMenuBuilder
        showMenu(currentMenu.parentMenu as MenuBuilder, goingForward = false)
      }
    }
  }

  fun show() {
    // PopupWindow moves the popup to align with the anchor if a fixed width
    // is known before hand. Note to self: If fixedWidth ever needs to be
    // removed, copy over MenuPopup.measureIndividualMenuWidth().
    popup.width = fixedWidth
    popup.height = WRAP_CONTENT // Doesn't work on API 21 without this.

    popup.setMargins(
      start = context.dip(4),
      end = context.dip(4),
      bottom = context.dip(4)
    )
    styler.background()?.let {
      popup.contentView.background = it
    }

    showMenu(menuBuilder, goingForward = true)
    popup.showAsDropDown(anchor, 0, 0, gravity)
  }

  /**
   * Navigate to the last menu. Also see [CascadeBackNavigator].
   *
   * FYI jumping over multiple back-stack entries isn't supported
   * very well, so avoid navigating multiple menus on a single click.
   */
  fun navigateBack() {
    backNavigator.navigateBack()
  }

  private fun showMenu(menu: MenuBuilder, goingForward: Boolean) {
    val menuList = RecyclerView(context).apply {
      layoutManager = LinearLayoutManager(context).also {
        it.recycleChildrenOnDetach = true
        setRecycledViewPool(sharedViewPool)
      }
      isVerticalScrollBarEnabled = true
      scrollBarStyle = SCROLLBARS_INSIDE_OVERLAY
      styler.menuList(this)

      addOnScrollListener(OverScrollIfContentScrolls())
      adapter = CascadeMenuAdapter(menu, styler, themeAttrs,
        canNavigateBack = backstack.isNotEmpty(),
        onTitleClick = { navigateBack() },
        onItemClick = { handleItemClick(it) }
      )
      layoutParams = LayoutParams(MATCH_PARENT, WRAP_CONTENT)
    }

    backstack.push(menu)
    popup.contentView.show(menuList, goingForward)
  }

  protected open fun handleItemClick(item: MenuItem) {
    if (item.hasSubMenu()) {
      showMenu(item.subMenu as MenuBuilder, goingForward = true)
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
    SupportMenuInflater(context).inflate(menuRes, menuBuilder)

  fun setOnMenuItemClickListener(listener: PopupMenu.OnMenuItemClickListener?) =
    menuBuilder.setCallback(listener)

  fun dismiss() =
    popup.dismiss()

  @get:JvmName("getDragToOpenListener")
  @Deprecated("CascadeMenu doesn't support drag-to-open.", level = ERROR)
  val dragToOpenListener: View.OnTouchListener
    get() = error("can't")
}
