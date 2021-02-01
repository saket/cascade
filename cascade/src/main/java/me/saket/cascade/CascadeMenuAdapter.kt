package me.saket.cascade

import android.view.MenuItem
import android.view.SubMenu
import android.view.ViewGroup
import androidx.appcompat.view.menu.MenuBuilder
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import me.saket.cascade.AdapterModel.HeaderModel
import me.saket.cascade.AdapterModel.ItemModel
import me.saket.cascade.CascadePopupMenu.Styler
import me.saket.cascade.CascadePopupWindow.ThemeAttributes

internal class CascadeMenuAdapter(
  private val items: List<AdapterModel>,
  private val styler: Styler,
  private val themeAttrs: ThemeAttributes,
  private val onTitleClick: (SubMenu) -> Unit,
  private val onItemClick: (MenuItem) -> Unit
) : RecyclerView.Adapter<ViewHolder>() {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
    return when (viewType) {
      VIEW_TYPE_HEADER -> MenuHeaderViewHolder.inflate(parent).apply {
        itemView.setBackgroundResource(themeAttrs.touchFeedbackRes)
        itemView.setOnClickListener { onTitleClick(model.menu) }
      }
      VIEW_TYPE_ITEM -> MenuItemViewHolder.inflate(parent).apply {
        itemView.setBackgroundResource(themeAttrs.touchFeedbackRes)
        itemView.setOnClickListener { onItemClick(model.item) }
      }
      else -> TODO()
    }
  }

  override fun onBindViewHolder(holder: ViewHolder, position: Int) {
    when (holder) {
      is MenuHeaderViewHolder -> {
        holder.render(items[position] as HeaderModel)
        styler.menuTitle(holder)
      }

      is MenuItemViewHolder -> {
        holder.render(items[position] as ItemModel)
        styler.menuItem(holder)
      }
    }
  }

  override fun getItemViewType(position: Int): Int {
    return when (items[position]) {
      is HeaderModel -> VIEW_TYPE_HEADER
      is ItemModel -> VIEW_TYPE_ITEM
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
