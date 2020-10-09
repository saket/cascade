@file:SuppressLint("RestrictedApi")

package me.saket.cascade

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Rect
import android.util.TypedValue
import android.util.TypedValue.COMPLEX_UNIT_DIP
import android.view.MenuItem
import android.view.View
import android.view.View.OVER_SCROLL_ALWAYS
import android.view.View.OVER_SCROLL_NEVER
import android.view.ViewGroup.MarginLayoutParams
import androidx.annotation.StyleableRes
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.RecyclerView

internal fun Context.dip(dp: Int): Int {
  val metrics = resources.displayMetrics
  return TypedValue.applyDimension(COMPLEX_UNIT_DIP, dp.toFloat(), metrics).toInt()
}

internal fun MenuBuilder.setCallback(listener: PopupMenu.OnMenuItemClickListener?) {
  setCallback(object : MenuBuilder.Callback {
    override fun onMenuModeChange(menu: MenuBuilder) = Unit
    override fun onMenuItemSelected(menu: MenuBuilder, item: MenuItem): Boolean =
      listener?.onMenuItemClick(item) ?: false
  })
}

internal class OverScrollIfContentScrolls : RecyclerView.OnScrollListener() {
  override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) = Unit
  override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
    if (dy == 0 && dx == 0) {
      // RecyclerView sends 0,0 if the visible item range changes after a layout calculation.
      val canScrollVertical = recyclerView.computeVerticalScrollRange() > recyclerView.height
      recyclerView.overScrollMode = if (canScrollVertical) OVER_SCROLL_ALWAYS else OVER_SCROLL_NEVER
    }
  }
}

internal fun TypedArray.getResourceIdOrNull(@StyleableRes index: Int): Int? {
  return if (hasValue(index)) getResourceId(index, 0) else null
}

internal fun View.setMargins(margins: Rect) {
  updateLayoutParams<MarginLayoutParams> {
    setMargins(margins.left, margins.top, margins.right, margins.bottom)
  }
}
