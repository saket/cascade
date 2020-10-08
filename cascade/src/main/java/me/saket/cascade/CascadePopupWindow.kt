package me.saket.cascade

import android.R.attr.listChoiceBackgroundIndicator
import android.R.attr.popupBackground
import android.R.attr.popupElevation
import android.R.attr.popupEnterTransition
import android.R.attr.popupExitTransition
import android.content.Context
import android.graphics.drawable.Drawable
import android.transition.Transition
import android.transition.TransitionInflater
import android.widget.PopupMenu
import android.widget.PopupWindow
import androidx.annotation.DrawableRes
import androidx.annotation.Px
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.res.getDimensionOrThrow
import androidx.core.content.res.getResourceIdOrThrow
import androidx.core.content.res.use
import androidx.core.widget.PopupWindowCompat
import me.saket.cascade.CascadePopupWindow.ThemeAttributes

/**
 * Mimics [PopupMenu] by,
 * - offering the same entry & exit transitions
 * - dismissing on outside tap
 * - setting a default elevation
 */
@Suppress("LeakingThis")
open class CascadePopupWindow @JvmOverloads constructor(
  private val context: Context,
  private val defStyleAttr: Int = android.R.style.Widget_Material_PopupMenu
) : PopupWindow(context, null, defStyleAttr) {

  val themeAttrs = resolveThemeAttrs()

  init {
    // Dismiss on outside touch.
    isFocusable = true
    isOutsideTouchable = true
    elevation = themeAttrs.popupElevation
    enterTransition = themeAttrs.popupEnterTransition
    exitTransition = themeAttrs.popupExitTransition

    // Remove PopupWindow's default frame around the content.
    setBackgroundDrawable(null)
    PopupWindowCompat.setOverlapAnchor(this, true)

    contentView = HeightAnimatableViewFlipper(context).apply {
      clipToOutline = true
      background = themeAttrs.popupBackground(context)
    }
  }

  override fun getContentView(): HeightAnimatableViewFlipper {
    return super.getContentView() as HeightAnimatableViewFlipper
  }

  private fun resolveThemeAttrs(): ThemeAttributes {
    val attrs = intArrayOf(
      popupBackground,
      popupElevation,
      popupEnterTransition,
      popupExitTransition,
      listChoiceBackgroundIndicator
    )

    return context.obtainStyledAttributes(defStyleAttr, attrs).use {
      val inflateTransition = { resId: Int -> TransitionInflater.from(context).inflateTransition(resId) }
      ThemeAttributes(
        popupElevation = it.getDimensionOrThrow(attrs.indexOf(popupElevation)),
        popupBackgroundRes = it.getResourceIdOrThrow(attrs.indexOf(popupBackground)),
        popupEnterTransition = inflateTransition(it.getResourceIdOrThrow(attrs.indexOf(popupEnterTransition))),
        popupExitTransition = inflateTransition(it.getResourceIdOrThrow(attrs.indexOf(popupExitTransition))),
        touchFeedbackRes = it.getResourceIdOrThrow(attrs.indexOf(listChoiceBackgroundIndicator))
      )
    }
  }

  data class ThemeAttributes(
    @Px val popupElevation: Float,
    @DrawableRes val popupBackgroundRes: Int,
    @DrawableRes val touchFeedbackRes: Int,
    val popupEnterTransition: Transition,
    val popupExitTransition: Transition
  )
}

fun ThemeAttributes.popupBackground(context: Context): Drawable {
  return AppCompatResources.getDrawable(context, popupBackgroundRes)!!
}
