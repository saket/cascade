package me.saket.cascade

import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import me.saket.cascade.internal.Api21And22EventDelegate

/**
 * Currently only implemented on API 21 and 22. See [Api21And22EventDelegate].
 */
interface EventDelegate {
  fun dispatchKeyEvent(event: KeyEvent): Boolean = false
  fun dispatchTouchEvent(view: View, ev: MotionEvent): Boolean = false
}

object NoOpEventDelegate : EventDelegate
