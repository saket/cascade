package me.saket.cascade

import android.graphics.Rect
import android.graphics.drawable.Drawable

/**
 * Workaround for [https://issuetracker.google.com/u/1/issues/171026918].
 *
 * The default popup background contains internal paddings, that aren't reset even if the
 * background is overridden using a [CascadePopupMenu.Styler].
 */
internal class ForcePaddingsDrawable(delegate: Drawable) : DrawableWrapperCompat(delegate) {
  override fun getPadding(padding: Rect): Boolean {
    super.getPadding(padding)
    return true
  }
}
