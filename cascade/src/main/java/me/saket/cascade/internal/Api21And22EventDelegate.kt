package me.saket.cascade.internal

import android.annotation.SuppressLint
import android.view.KeyEvent
import android.view.KeyEvent.ACTION_UP
import android.view.KeyEvent.KEYCODE_BACK
import android.view.MotionEvent
import android.view.MotionEvent.ACTION_DOWN
import android.view.MotionEvent.ACTION_OUTSIDE
import android.view.View
import me.saket.cascade.EventDelegate

/**
 * On API 21 & 22, PopupWindow only handles touch and key events if a background is present.
 * Cascade draws its (animatable) background manually so the events must be handled manually.
 */
@SuppressLint("ViewConstructor")
internal class Api21And22EventDelegate(private val onDismiss: () -> Unit) : EventDelegate {
  // todo: copy PopupWindow's implementation.
  override fun dispatchKeyEvent(event: KeyEvent): Boolean {
    return if (event.keyCode == KEYCODE_BACK && event.action == ACTION_UP && !event.isCanceled) {
      onDismiss()
      true
    } else {
      false
    }
  }

  override fun dispatchTouchEvent(view: View, ev: MotionEvent): Boolean {
    return if (ev.action == ACTION_DOWN && (ev.x < 0 || ev.x >= view.width || ev.y < 0 || ev.y >= view.height)) {
      onDismiss()
      true
    } else if (ev.action == ACTION_OUTSIDE) {
      onDismiss()
      true
    } else {
      false
    }
  }
}
