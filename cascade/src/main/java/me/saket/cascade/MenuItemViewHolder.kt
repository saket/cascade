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
import me.saket.cascade.AdapterModel.ItemModel
import me.saket.cascade.internal.dip
import kotlin.LazyThreadSafetyMode.NONE

class MenuItemViewHolder(private val view: ListMenuItemView) : RecyclerView.ViewHolder(view) {
  val titleView: TextView = view.findViewById(R.id.title)
  val titleContainerView: ViewGroup = titleView.parent as ViewGroup

  val contentView: View = view.findViewById(R.id.content)
  val iconView: ImageView by lazy(NONE) { view.findViewById(R.id.icon) }
  val subMenuArrowView: ImageView = view.findViewById(R.id.submenuarrow)
  val groupDividerView: View = view.findViewById(R.id.group_divider)      // Shown at the top of this item's layout.

  lateinit var model: ItemModel
    private set

  private val Int.dip: Int
    get() = view.context.dip(this)

  @Deprecated("Use model instead", ReplaceWith("model.item"))
  val item: MenuItem get() = model.item

  init {
    groupDividerView.updateMargin(top = 0, bottom = 0)
  }

  fun render(model: ItemModel) {
    this.model = model

    view.setForceShowIcon(true)
    view.initialize(model.item as MenuItemImpl, 0)
    view.setGroupDividerEnabled(model.isDifferentGroupThanPrev)

    if (model.item.hasSubMenu()) {
      subMenuArrowView.setImageResource(R.drawable.cascade_ic_round_arrow_right_24)
    }

    subMenuArrowView.updateMargin(start = 0.dip)
    setContentSpacing(
      start = if (model.item.icon != null) 12.dip else 14.dip,
      end = when {
        model.item.hasSubMenu() -> 4.dip
        model.hasSubMenuSiblings -> 28.dip
        else -> 14.dip
      },
      iconSpacing = 14.dip
    )
  }

  fun setContentSpacing(
    @Px start: Int,
    @Px end: Int,
    @Px iconSpacing: Int
  ) {
    val hasIcon = model.item.icon != null
    iconView.updateMargin(start = if (hasIcon) start else 0, end = 0)
    titleContainerView.updateMargin(start = if (hasIcon) iconSpacing else start)
    contentView.updatePaddingRelative(end = end)
  }

  /**
   * Useful for customizing vertical spacings when group dividers are present.
   * Also see [ItemModel.isDifferentGroupThanPrev] and [ItemModel.isDifferentGroupThanNext].
   */
  fun setContentSpacing(
    @Px top: Int = 0,
    @Px bottom: Int = 0
  ) {
    contentView.updateMargin(top = top, bottom = bottom)
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
    fun inflate(parent: ViewGroup): MenuItemViewHolder {
      val inflater = LayoutInflater.from(parent.context).cloneInContext(parent.context)
      val view = inflater.inflate(R.layout.abc_popup_menu_item_layout, parent, false)
      return MenuItemViewHolder(view as ListMenuItemView)
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
