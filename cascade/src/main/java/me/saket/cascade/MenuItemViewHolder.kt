@file:Suppress("MemberVisibilityCanBePrivate")
@file:SuppressLint("RestrictedApi", "PrivateResource")

package me.saket.cascade

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.graphics.drawable.PaintDrawable
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.Px
import androidx.appcompat.view.menu.ListMenuItemView
import androidx.appcompat.view.menu.MenuItemImpl
import androidx.core.view.marginBottom
import androidx.core.view.marginEnd
import androidx.core.view.marginStart
import androidx.core.view.marginTop
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePaddingRelative
import androidx.recyclerview.widget.RecyclerView
import me.saket.cascade.internal.dip
import kotlin.LazyThreadSafetyMode.NONE

/**
 * Layout for a menu item.
 */
class MenuItemViewHolder(
  private val view: ListMenuItemView,
  private val hasSubMenuSiblings: Boolean
) : RecyclerView.ViewHolder(view) {
  val titleView: TextView = view.findViewById(R.id.title)
  val titleContainerView: ViewGroup = titleView.parent as ViewGroup

  val contentView: View = view.findViewById(R.id.content)
  val iconView: ImageView by lazy(NONE) { view.findViewById(R.id.icon) }
  val subMenuArrowView: ImageView = view.findViewById(R.id.submenuarrow)
  val groupDividerView: View = view.findViewById(R.id.group_divider)      // Shown at the top of this item's layout.

  lateinit var item: MenuItem

  private val Int.dip: Int
    get() = view.context.dip(this)

  init {
    groupDividerView.updateMargin(top = 0, bottom = 0)
  }

  fun render(
    item: MenuItem,
    hasTopDivider: Boolean = false,
    hasPaddingForNextItemsDivider: Boolean = false
  ) {
    this.item = item

    view.setForceShowIcon(true)
    view.initialize(item as MenuItemImpl, 0)
    view.setGroupDividerEnabled(hasTopDivider)

    if (item.hasSubMenu()) {
      subMenuArrowView.setImageResource(R.drawable.cascade_ic_round_arrow_right_24)
    }

    subMenuArrowView.updateMargin(start = 0.dip)
    setContentSpacing(
      top = if (hasTopDivider) 8.dip else 0,
      bottom = if (hasPaddingForNextItemsDivider) 8.dip else 0,
      start = if (item.icon != null) 12.dip else 14.dip,
      end = when {
        item.hasSubMenu() -> 4.dip
        hasSubMenuSiblings -> 28.dip
        else -> 14.dip
      },
      iconSpacing = 14.dip
    )
  }

  fun setContentSpacing(
    @Px start: Int,
    @Px end: Int,
    @Px iconSpacing: Int,
    @Px top: Int = 0,
    @Px bottom: Int = 0
  ) {
    val hasIcon = item.icon != null
    iconView.updateMargin(start = if (hasIcon) start else 0, end = 0)
    titleContainerView.updateMargin(start = if (hasIcon) iconSpacing else start)
    contentView.updatePaddingRelative(end = end, top = top, bottom = bottom)
  }

  fun setGroupDividerColor(color: Int) {
    // Tinting the divider View is not an option because its drawable has a transparent color.
    groupDividerView.background = (groupDividerView.background as? PaintDrawable ?: PaintDrawable()).apply {
      paint.color = color
    }
  }

  fun setBackground(drawable: Drawable) {
    itemView.background = drawable
  }

  companion object {
    fun inflate(parent: ViewGroup, hasSubMenuSiblings: Boolean): MenuItemViewHolder {
      val inflater = LayoutInflater.from(parent.context).cloneInContext(parent.context)
      val view = inflater.inflate(R.layout.abc_popup_menu_item_layout, parent, false)
      return MenuItemViewHolder(view as ListMenuItemView, hasSubMenuSiblings)
    }
  }
}

private fun View.updateMargin(
  top: Int = marginTop,
  bottom: Int = marginBottom,
  start: Int = marginStart,
  end: Int = marginEnd
) {
  updateLayoutParams<MarginLayoutParams> {
    topMargin = top
    bottomMargin = bottom
    marginStart = start
    marginEnd = end
  }
}
