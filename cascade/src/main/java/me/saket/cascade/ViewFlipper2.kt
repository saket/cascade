package me.saket.cascade

import android.content.Context
import android.view.MotionEvent
import android.view.View
import android.view.ViewPropertyAnimator
import android.view.animation.Animation
import android.widget.ViewFlipper
import androidx.core.view.children
import androidx.core.view.isVisible

/**
 * Like [ViewFlipper], but uses [ViewPropertyAnimator] to animate child changes
 * instead of [Animation] which was causing [https://github.com/saket/cascade/issues/4].
 */
abstract class ViewFlipper2(context: Context) : ViewFlipper(context) {
  val displayedChildView: View
    get() = getChildAt(displayedChild)

  override fun setDisplayedChild(whichChild: Int) {
    super.setDisplayedChild(whichChild)
    children.forEach { it.clearAnimation() }
  }

  fun setDisplayedChild(
    inView: View,
    inAnimator: (View) -> ViewPropertyAnimator,
    outAnimator: (View) -> ViewPropertyAnimator
  ) {
    val outView = getChildAt(displayedChild)
    super.setDisplayedChild(indexOfChild(inView))

    if (outView != null) {
      inAnimator(inView)
        .setListener(
          onStart = { inView.isVisible = true }
        )
        .start()
      outAnimator(outView)
        .setListener(
          onStart = { outView.isVisible = true },
          onEnd = { outView.isVisible = false }
        )
        .start()
    }
  }

  override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
    // OG ViewFlipper animates view bitmaps instead of actual views,
    // where the displayed View is always at 0,0. This effectively means
    // that touch events are *always* received by the displayed child.
    return getChildAt(displayedChild).let {
      ev.offsetLocation(it.translationX, 0f)
      it.dispatchTouchEvent(ev)
    }
  }
}
