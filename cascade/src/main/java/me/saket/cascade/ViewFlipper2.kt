package me.saket.cascade

import android.animation.Animator
import android.animation.Animator.AnimatorListener
import android.content.Context
import android.view.MotionEvent
import android.view.View
import android.view.ViewPropertyAnimator
import android.view.animation.Animation
import android.widget.ViewFlipper
import androidx.core.view.isVisible

/**
 * Like [ViewFlipper], but uses [ViewPropertyAnimator] to animate child changes
 * instead of [Animation] which was causing [https://github.com/saket/cascade/issues/4].
 */
abstract class ViewFlipper2(context: Context) : ViewFlipper(context) {
  val displayedChildView: View
    get() = getChildAt(displayedChild)

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

private fun ViewPropertyAnimator.setListener(
  onEnd: () -> Unit = {},
  onStart: () -> Unit = {}
): ViewPropertyAnimator {
  setListener(object : AnimatorListener {
    override fun onAnimationRepeat(animator: Animator) = Unit
    override fun onAnimationCancel(animator: Animator) = Unit
    override fun onAnimationEnd(animator: Animator) = onEnd()
    override fun onAnimationStart(animator: Animator) = onStart()
  })
  return this
}
