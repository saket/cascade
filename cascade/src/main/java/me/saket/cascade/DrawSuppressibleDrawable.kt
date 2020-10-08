package me.saket.cascade

import android.graphics.Canvas
import android.graphics.Outline
import android.graphics.drawable.Drawable
import android.graphics.drawable.DrawableWrapper
import android.graphics.drawable.ScaleDrawable
import android.view.Gravity

internal class DrawSuppressibleDrawable(delegate: Drawable) : DrawableWrapperCompat(delegate) {
  var skipDrawing = false

  override fun draw(canvas: Canvas) {
    if (!skipDrawing) {
      super.draw(canvas)
    }
  }
}

internal fun DrawSuppressibleDrawable?.withDrawSuppressed(action: () -> Unit) {
  if (this == null) {
    action()
  } else {
    skipDrawing = false
    action()
    skipDrawing = true
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
