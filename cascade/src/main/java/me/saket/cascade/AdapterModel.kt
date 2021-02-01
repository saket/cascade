package me.saket.cascade

import android.annotation.SuppressLint
import android.view.MenuItem
import android.view.SubMenu
import androidx.appcompat.view.menu.MenuBuilder
import me.saket.cascade.AdapterModel.HeaderModel
import me.saket.cascade.AdapterModel.ItemModel

sealed class AdapterModel {
  abstract val prevGroupId: Int?
  abstract val nextGroupId: Int?

  class HeaderModel(
    val menu: SubMenu,
    val showBackIcon: Boolean,
    override val nextGroupId: Int?,
  ) : AdapterModel() {
    override val prevGroupId: Int? = null
  }

  class ItemModel(
    val item: MenuItem,
    val hasSubMenuSiblings: Boolean,
    override val prevGroupId: Int?,
    override val nextGroupId: Int?,
  ) : AdapterModel() {

    val isDifferentGroupThanPrev
      get() = prevGroupId.let { it != null && it != item.groupId }

    val isDifferentGroupThanNext
      get() = nextGroupId.let { it != null && it != item.groupId }
  }
}

@SuppressLint("RestrictedApi")
@OptIn(ExperimentalStdlibApi::class)
internal fun buildModels(menu: MenuBuilder, canNavigateBack: Boolean): List<AdapterModel> {
  val items = mutableListOf<Any>()
  if (menu is SubMenu) items += menu
  items.addAll(menu.nonActionItems.filter { it.isVisible })

  val hasSubMenuItems = items.filterIsInstance<MenuItem>().any { it.hasSubMenu() }

  return items.mapIndexed { index, item ->
    when (item) {
      is SubMenu -> HeaderModel(
        menu = item,
        showBackIcon = canNavigateBack,
        nextGroupId = (items.getOrNull(index + 1) as? MenuItem)?.groupId
      )
      is MenuItem -> ItemModel(
        item = item,
        hasSubMenuSiblings = hasSubMenuItems,
        prevGroupId = (items.getOrNull(index - 1) as? MenuItem)?.groupId,
        nextGroupId = (items.getOrNull(index + 1) as? MenuItem)?.groupId
      )
      else -> error("unknown $item")
    }
  }
}
