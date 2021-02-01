@file:Suppress("MemberVisibilityCanBePrivate")
@file:SuppressLint("RestrictedApi", "PrivateResource")

package me.saket.cascade

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.view.Gravity.CENTER_VERTICAL
import android.view.Gravity.START
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.SubMenu
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.view.menu.SubMenuBuilder
import androidx.core.view.updatePaddingRelative
import androidx.recyclerview.widget.RecyclerView
import me.saket.cascade.AdapterModel.HeaderModel
import me.saket.cascade.AdapterModel.ItemModel
import me.saket.cascade.internal.dip

class MenuHeaderViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
  val titleView: TextView = view.findViewById(android.R.id.title)

  lateinit var model: HeaderModel
    private set

  private val Int.dip: Int
    get() = itemView.context.dip(this)

  @Deprecated("Use model instead", ReplaceWith("model.menu"))
  val menu: SubMenu get() = model.menu

  init {
    titleView.isEnabled = false
    titleView.gravity = START or CENTER_VERTICAL
  }

  fun render(model: HeaderModel) {
    this.model = model
    titleView.text = (model.menu as SubMenuBuilder).headerTitle

    if (model.showBackIcon) {
      setBackIcon(AppCompatResources.getDrawable(itemView.context, R.drawable.cascade_ic_round_arrow_left_32)!!)
      view.updatePaddingRelative(start = 6.dip, end = 16.dip)
    } else {
      setBackIcon(null)
      view.updatePaddingRelative(start = 16.dip, end = 16.dip)
    }
    itemView.isClickable = model.showBackIcon
  }

  fun setBackIcon(icon: Drawable?) {
    titleView.setCompoundDrawablesRelativeWithIntrinsicBounds(icon, null, null, null)
  }

  fun setBackground(drawable: Drawable) {
    itemView.background = drawable
  }

  companion object {
    fun inflate(parent: ViewGroup): MenuHeaderViewHolder {
      val inflater = LayoutInflater.from(parent.context).cloneInContext(parent.context)
      val view = inflater.inflate(R.layout.abc_popup_menu_header_item_layout, parent, false)
      return MenuHeaderViewHolder(view)
    }
  }
}
