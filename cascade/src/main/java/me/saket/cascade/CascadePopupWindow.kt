package me.saket.cascade

import android.R.attr.listChoiceBackgroundIndicator
import android.R.attr.popupBackground
import android.R.attr.popupElevation
import android.R.attr.popupEnterTransition
import android.R.attr.popupExitTransition
import android.content.Context
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Build.VERSION.SDK_INT
import android.transition.Transition
import android.transition.TransitionInflater
import android.view.View
import android.widget.PopupMenu
import android.widget.PopupWindow
import androidx.annotation.DrawableRes
import androidx.annotation.Px
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.res.getDimensionOrThrow
import androidx.core.content.res.getResourceIdOrThrow
import androidx.core.content.res.use
import androidx.core.view.updatePaddingRelative
import androidx.core.widget.PopupWindowCompat
import me.saket.cascade.CascadePopupWindow.ThemeAttributes

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
  private val defStyleAttr: Int = android.R.style.Widget_Material_PopupMenu
) : PopupWindow(context, null, defStyleAttr) {

  val themeAttrs = resolveThemeAttrs()
  private var margins = Rect()

  init {
    // Dismiss on outside touch.
    isFocusable = true
    isOutsideTouchable = true
    elevation = themeAttrs.popupElevation
    themeAttrs.popupEnterTransitionRes?.let(::setEnterTransition)
    themeAttrs.popupExitTransitionRes?.let(::setExitTransition)

    // Remove PopupWindow's default frame around the content.
    setBackgroundDrawable(null)
    PopupWindowCompat.setOverlapAnchor(this, true)

    contentView = HeightAnimatableViewFlipper(context).apply {
      background = themeAttrs.popupBackground(context)
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
    width += margins.left + margins.right
    action()
    (contentView.parent as View).updatePaddingRelative(
      start = margins.left,
      top = margins.top,
      end = margins.right,
      bottom = margins.bottom
    )
  }

  private fun resolveThemeAttrs(): ThemeAttributes {
    val attrs = listOf(popupBackground, popupElevation, listChoiceBackgroundIndicator)
      .plus(if (SDK_INT >= 24) listOf(popupEnterTransition, popupExitTransition) else emptyList())
      .toIntArray()

    return context.obtainStyledAttributes(defStyleAttr, attrs).use {
      val inflateTransition = { resId: Int? ->
        if (resId == null) null
        else TransitionInflater.from(context).inflateTransition(resId)
      }
      ThemeAttributes(
        popupElevation = it.getDimensionOrThrow(attrs.indexOf(popupElevation)),
        popupBackgroundRes = it.getResourceIdOrThrow(attrs.indexOf(popupBackground)),
        popupEnterTransitionRes = inflateTransition(it.getResourceIdOrNull(attrs.indexOf(popupEnterTransition))),
        popupExitTransitionRes = inflateTransition(it.getResourceIdOrNull(attrs.indexOf(popupExitTransition))),
        touchFeedbackRes = it.getResourceIdOrThrow(attrs.indexOf(listChoiceBackgroundIndicator))
      )
    }
  }

  class ThemeAttributes(
    @Px val popupElevation: Float,
    @DrawableRes val popupBackgroundRes: Int,
    @DrawableRes val touchFeedbackRes: Int,
    val popupEnterTransitionRes: Transition?,
    val popupExitTransitionRes: Transition?
  )
}

fun ThemeAttributes.popupBackground(context: Context): Drawable {
  return AppCompatResources.getDrawable(context, popupBackgroundRes)!!
}
