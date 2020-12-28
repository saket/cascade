@file:SuppressLint("RestrictedApi", "PrivateResource")

package me.saket.cascade

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.view.Gravity.CENTER_VERTICAL
import android.view.Gravity.START
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.view.menu.SubMenuBuilder
import androidx.core.view.updatePaddingRelative
import androidx.recyclerview.widget.RecyclerView
import me.saket.cascade.internal.dip

/** Layout for a sub-menu header. */
class MenuHeaderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
  val titleView: TextView = view.findViewById(android.R.id.title)
  lateinit var menu: SubMenuBuilder

  private val Int.dip: Int
    get() = itemView.context.dip(this)

  init {
    titleView.isEnabled = false
    titleView.gravity = START or CENTER_VERTICAL
    view.updatePaddingRelative(start = 6.dip, end = 16.dip)
  }

  fun render(menu: SubMenuBuilder, showBackIcon: Boolean) {
    this.menu = menu
    titleView.text = this.menu.headerTitle

    if (showBackIcon) {
      setBackIcon(AppCompatResources.getDrawable(itemView.context, R.drawable.cascade_ic_round_arrow_left_32)!!)
    } else {
      setBackIcon(null)
    }
    itemView.isClickable = showBackIcon
  }

  fun setBackIcon(icon: Drawable?) {
    titleView.setCompoundDrawablesRelativeWithIntrinsicBounds(icon, null, null, null)
  }

  companion object {
    fun inflate(parent: ViewGroup): MenuHeaderViewHolder {
      val inflater = LayoutInflater.from(parent.context).cloneInContext(parent.context)
      val view = inflater.inflate(R.layout.abc_popup_menu_header_item_layout, parent, false)
      return MenuHeaderViewHolder(view)
    }
  }
}
