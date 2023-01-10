package me.saket.cascade.internal

import android.content.Context
import android.graphics.Rect
import android.os.Build.VERSION.SDK_INT
import android.view.Gravity
import android.view.View
import android.widget.PopupMenu
import android.widget.PopupWindow
import androidx.core.view.updatePaddingRelative
import androidx.core.widget.PopupWindowCompat

@Suppress("LeakingThis")
open class BaseCascadePopupWindow @JvmOverloads constructor(
  context: Context,
  defStyleRes: Int = android.R.style.Widget_Material_PopupMenu
) : PopupWindow(context, null, 0, defStyleRes) {

  private var margins = Rect()

  init {
    isFocusable = true                          // For receiving hardware back clicks.
    isOutsideTouchable = true                   // For receiving outside clicks.
    inputMethodMode = INPUT_METHOD_NOT_NEEDED   // Keyboard is recreated otherwise.
    setBackgroundDrawable(null)                 // Remove PopupWindow's default frame around the content.
    PopupWindowCompat.setOverlapAnchor(this, true)
  }

  /**
   * Set a fixed margin between this popup and the window. By default, a margin
   * of 4dp is used on the sides to match that of a Toolbar's overflow menu. Feel
   * free to override this (before the popup is shown) if it doesn't work for you.
   *
   * It'd be nice to use the margin only if the popup extends to the window
   * edges, but PopupWindow doesn't make it easy to do so.
   */
  fun setMargins(
    start: Int = margins.left,
    top: Int = margins.top,
    end: Int = margins.right,
    bottom: Int = margins.bottom
  ) {
    check(!isShowing) { "Can't change once the popup is already visible." }
    margins.set(start, top, end, bottom)
  }

  fun show(
    anchor: View,
    xOffset: Int = 0,
    yOffset: Int = 0,
    gravity: Int = Gravity.NO_GRAVITY
  ) {
    showAsDropDown(anchor, xOffset, yOffset, gravity)
  }

  override fun showAsDropDown(anchor: View, xoff: Int, yoff: Int, gravity: Int) {
    runWithMargins {
      super.showAsDropDown(anchor, xoff, yoff, gravity)
    }
  }

  override fun showAtLocation(parent: View, gravity: Int, x: Int, y: Int) {
    runWithMargins {
      super.showAtLocation(parent, gravity, x, y)
    }
  }

  // todo: this could be recreated by using an invisible elevation.
  private fun runWithMargins(action: () -> Unit) {
    if (SDK_INT > 21) {
      // PopupWindow's content View does not have any parent on
      // API 21 (poor kid) that can be used for faking margins.
      //width += margins.left + margins.right
    }

    action()
//    (contentView.parent as? View)?.updatePaddingRelative(
//      start = margins.left,
//      top = margins.top,
//      end = margins.right,
//      bottom = margins.bottom
//    )
  }
}
