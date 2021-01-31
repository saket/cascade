package me.saket.cascade

import android.graphics.drawable.ClipDrawable
import android.graphics.drawable.Drawable
import me.saket.cascade.internal.DrawableWrapperCompat

/** Like [ClipDrawable], but allows clipping in terms of pixels instead of percentage. */
internal class HeightClipDrawable(delegate: Drawable) : DrawableWrapperCompat(delegate) {
  var clippedHeight: Int? = null
    set(value) {
      field = value
      bounds = bounds
    }

  override fun setBounds(left: Int, top: Int, right: Int, bottom: Int) {
    super.setBounds(left, top, right, clippedHeight ?: bottom)
  }
}
