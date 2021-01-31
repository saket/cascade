package me.saket.cascade

import android.content.Context
import android.view.MotionEvent
import android.view.View
import android.view.ViewPropertyAnimator
import android.view.animation.Animation
import android.widget.FrameLayout
import android.widget.ViewFlipper

/**
 * Like [ViewFlipper], but uses [ViewPropertyAnimator] to animate child changes instead of
 * [Animation] which was causing [https://github.com/saket/cascade/issues/4].
 *
 * This does not extend [ViewFlipper] primarily because [ViewFlipper] toggles visibility of
 * children when [ViewFlipper.setDisplayedChild] is called resulting in ripples getting canceled.
 */
abstract class ViewFlipper2 internal constructor(context: Context) : FrameLayout(context) {
  internal var displayedChildView: View? = null
    private set

  internal fun setDisplayedChild(view: View) {
    displayedChildView = view
  }

  internal fun setDisplayedChild(
    inView: View,
    inAnimator: (View) -> ViewPropertyAnimator,
    outAnimator: (View) -> ViewPropertyAnimator
  ) {
    val outView = displayedChildView
    displayedChildView = inView

    requireNotNull(outView)
    inAnimator(inView).start()
    outAnimator(outView).start()
  }

  // TODO: get rid of these 3 functions in favor of onViewRemoved() once minSdk > 23.
  override fun removeAllViews() {
    super.removeAllViews()
    displayedChildView = null
  }

  override fun removeView(view: View) {
    super.removeView(view)
    if (displayedChildView == view) {
      displayedChildView = null
    }
  }

  override fun removeViewAt(index: Int) {
    getChildAt(index)?.let { removeView(it) }
  }

  override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
    // OG ViewFlipper animates view bitmaps instead of actual views,
    // where the displayed View is always at 0,0. This effectively means
    // that touch events are *always* received by the displayed child.
    displayedChildView?.let {
      ev.offsetLocation(it.translationX, 0f)
      return it.dispatchTouchEvent(ev)
    }
    return false
  }
}
