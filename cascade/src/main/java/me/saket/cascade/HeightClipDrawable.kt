package me.saket.cascade

import android.graphics.Canvas
import android.graphics.Outline
import android.graphics.drawable.ClipDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.DrawableWrapper
import android.graphics.drawable.ScaleDrawable
import android.view.Gravity

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

/** Because [DrawableWrapper] is API 23+ only. */
internal abstract class DrawableWrapperCompat(
  private val delegate: Drawable
) : ScaleDrawable(delegate, Gravity.CENTER, -1f, -1f) {

  override fun draw(canvas: Canvas) {
    delegate.draw(canvas)
  }

  override fun getOutline(outline: Outline) {
    // ScaleDrawable doesn't delegate getOutline()
    // calls to the wrapped drawable on API 21.
    delegate.getOutline(outline)
  }
}
