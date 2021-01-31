@file:OptIn(ExperimentalStdlibApi::class)

package me.saket.cascade

import android.annotation.SuppressLint
import android.view.MenuItem
import android.view.SubMenu
import android.view.ViewGroup
import androidx.appcompat.view.menu.MenuBuilder
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import me.saket.cascade.CascadeMenuAdapter.ItemType.Header
import me.saket.cascade.CascadeMenuAdapter.ItemType.Item
import me.saket.cascade.CascadePopupMenu.Styler
import me.saket.cascade.CascadePopupWindow.ThemeAttributes

@SuppressLint("RestrictedApi")
internal class CascadeMenuAdapter(
  private val menu: MenuBuilder,
  private val styler: Styler,
  private val themeAttrs: ThemeAttributes,
  private val canNavigateBack: Boolean,
  private val onTitleClick: (SubMenu) -> Unit,
  private val onItemClick: (MenuItem) -> Unit
) : RecyclerView.Adapter<ViewHolder>() {

  sealed class ItemType {
    class Header(val menu: SubMenu) : ItemType()
    class Item(val item: MenuItem) : ItemType()
  }

  private val items: List<ItemType> = buildList {
    if (menu is SubMenu) {
      add(Header(menu as SubMenu))
    }
    for (item in menu.nonActionItems) {
      if (item.isVisible) {
        add(Item(item as MenuItem))
      }
    }
  }

  private val hasSubMenuItems = items
    .filterIsInstance<Item>()
    .any { it.item.hasSubMenu() }

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    return when (viewType) {
      VIEW_TYPE_HEADER -> MenuHeaderViewHolder.inflate(parent).apply {
        itemView.setBackgroundResource(themeAttrs.touchFeedbackRes)
        itemView.setOnClickListener { onTitleClick(menu) }
      }
      VIEW_TYPE_ITEM -> MenuItemViewHolder.inflate(parent, hasSubMenuItems).apply {
        itemView.setBackgroundResource(themeAttrs.touchFeedbackRes)
        itemView.setOnClickListener { onItemClick(item) }
      }
      else -> TODO()
    }
  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    when (holder) {
      is MenuHeaderViewHolder -> {
        holder.render((items[position] as Header).menu, showBackIcon = canNavigateBack)
        styler.menuTitle(holder)
      }

      is MenuItemViewHolder -> {
        holder.render(
          item = (items[position] as Item).item,
          hasTopDivider = shouldShowDividerBetween(position, position - 1),
          hasPaddingForNextItemsDivider = shouldShowDividerBetween(position, position + 1)
        )
        styler.menuItem(holder)
      }
    }
  }

  private fun shouldShowDividerBetween(positionA: Int, positionB: Int): Boolean {
    if (!menu.isGroupDividerEnabled) {
      return false
    }

    val itemA = (items.getOrNull(positionA) as? Item)?.item ?: return false
    val itemB = (items.getOrNull(positionB) as? Item)?.item ?: return false
    return itemA.groupId != itemB.groupId
  }

  override fun getItemViewType(position: Int): Int {
    return when (items[position]) {
      is Header -> VIEW_TYPE_HEADER
      is Item -> VIEW_TYPE_ITEM
    }
  }

  override fun getItemCount(): Int {
    return items.size
  }

  companion object {
    const val VIEW_TYPE_HEADER = 0
    const val VIEW_TYPE_ITEM = 1
  }
}
