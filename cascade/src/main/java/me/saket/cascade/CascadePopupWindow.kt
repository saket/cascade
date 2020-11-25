package me.saket.cascade

import android.R.attr.listChoiceBackgroundIndicator
import android.R.attr.popupBackground
import android.R.attr.popupElevation
import android.content.Context
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Build.VERSION.SDK_INT
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.PopupMenu
import android.widget.PopupWindow
import androidx.annotation.DrawableRes
import androidx.annotation.Px
import androidx.core.content.res.getDimensionOrThrow
import androidx.core.content.res.getDrawableOrThrow
import androidx.core.content.res.getResourceIdOrThrow
import androidx.core.content.res.use
import androidx.core.view.updatePaddingRelative
import androidx.core.widget.PopupWindowCompat
import me.saket.cascade.R.attr

/**
 * Mimics [PopupMenu] by,
 * - offering the same entry & exit transitions
 * - dismissing on outside tap
 * - setting a default elevation
 *
 * Use `getContentView().show(View)` for updating the content,
 * which will handle animating content and height changes.
 */
@Suppress("LeakingThis")
open class CascadePopupWindow @JvmOverloads constructor(
  private val context: Context,
  private val defStyleRes: Int = android.R.style.Widget_Material_PopupMenu
) : PopupWindow(context, null, 0, defStyleRes) {

  val themeAttrs = resolveThemeAttrs()
  private var margins = Rect()

  init {
    // Dismiss on outside touch.
    isFocusable = true
    isOutsideTouchable = true
    elevation = themeAttrs.popupElevation

    // Remove PopupWindow's default frame around the content.
    setBackgroundDrawable(null)
    PopupWindowCompat.setOverlapAnchor(this, true)

    contentView = HeightAnimatableViewFlipper(context).apply {
      background = themeAttrs.popupBackground
      clipToOutline = true
    }
  }

  override fun getContentView(): HeightAnimatableViewFlipper {
    return super.getContentView() as HeightAnimatableViewFlipper
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

  private fun runWithMargins(action: () -> Unit) {
    if (SDK_INT > 21) {
      // PopupWindow's content View does not have any parent on
      // API 21 (poor kid) that can be used for faking margins.
      width += margins.left + margins.right
    }

    action()
    (contentView.parent as? View)?.updatePaddingRelative(
      start = margins.left,
      top = margins.top,
      end = margins.right,
      bottom = margins.bottom
    )
  }

  private fun resolveThemeAttrs(): ThemeAttributes {
    val attrs = intArrayOf(popupBackground, popupElevation, listChoiceBackgroundIndicator)
    return context.obtainStyledAttributes(null, attrs, attr.popupMenuStyle, defStyleRes).use {
      ThemeAttributes(
        popupElevation = it.getDimensionOrThrow(attrs.indexOf(popupElevation)),
        popupBackground = it.getDrawableOrThrow(attrs.indexOf(popupBackground)),
        touchFeedbackRes = it.getResourceIdOrThrow(attrs.indexOf(listChoiceBackgroundIndicator))
      )
    }
  }

  class ThemeAttributes(
    @Px val popupElevation: Float,
    val popupBackground: Drawable,
    @DrawableRes val touchFeedbackRes: Int
  )
}

