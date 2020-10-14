package me.saket.cascade

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.view.MotionEvent
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.FrameLayout.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout.LayoutParams.WRAP_CONTENT
import android.widget.ViewFlipper
import androidx.core.animation.doOnEnd
import androidx.core.view.doOnLayout
import androidx.interpolator.view.animation.FastOutSlowInInterpolator

/**
 * A [ViewFlipper] that wraps its height to the currently
 * displayed child and animates change in its height.
 *
 * See [show], [goForward] and [goBack].
 */
open class HeightAnimatableViewFlipper(context: Context) : BaseHeightClippableFlipper(context) {

  fun show(
    view: View,
    forward: Boolean
  ) {
    enqueueAnimation {
      val index = if (forward) childCount else 0
      val params = view.layoutParams ?: LayoutParams(MATCH_PARENT, WRAP_CONTENT)
      super.addView(view, index, params)
      if (childCount == 1) {
        return@enqueueAnimation
      }

      val prevView = displayedChildView
      setDisplayedChild(
        view,
        inAnimator = {
          it.translationX = if (forward) width.toFloat() else -(width.toFloat() * 0.25f)
          it.animate()
            .translationX(0f)
            .setDuration(animationDuration)
            .setInterpolator(animationInterpolator)
        },
        outAnimator = {
          it.translationX = 0f
          it.animate()
            .translationX(if (!forward) width.toFloat() else -(width.toFloat() * 0.25f))
            .setDuration(animationDuration)
            .setInterpolator(animationInterpolator)
        }
      )

      doOnLayout {
        animateHeight(
          from = paddedHeight(prevView),
          to = paddedHeight(view),
          onEnd = { removeView(prevView) }
        )
      }
    }
  }

  private fun paddedHeight(child: View): Int {
    return child.height + paddingTop + paddingBottom
  }

  open fun goForward(toView: View) =
    show(toView, forward = true)

  open fun goBack(toView: View) =
    show(toView, forward = false)

  override fun addView(child: View, index: Int, params: android.view.ViewGroup.LayoutParams) =
    throw error("Use show() / goForward() / goBack() instead")

  private fun enqueueAnimation(action: () -> Unit) {
    if (!animator.isRunning) action()
    else animator.doOnEnd { action() }
  }
}

@Suppress("LeakingThis")
abstract class BaseHeightClippableFlipper(context: Context) : ViewFlipper2(context) {
  protected var animationDuration = 350L
  protected var animationInterpolator = FastOutSlowInInterpolator()

  private var clipBounds2: Rect? = null // Because View#clipBounds creates a new Rect on every call
  protected var animator: ValueAnimator = ObjectAnimator()

  init {
    setWillNotDraw(false)
    outlineProvider = ViewOutlineProvider.BACKGROUND
  }

  protected fun animateHeight(from: Int, to: Int, onEnd: () -> Unit) {
    animator.cancel()
    animator = ObjectAnimator.ofFloat(0f, 1f).apply {
      duration = animationDuration
      interpolator = FastOutSlowInInterpolator()

      addUpdateListener {
        val scale = it.animatedValue as Float
        val newHeight = ((to - from) * scale + from).toInt()
        setClippedHeight(newHeight)
      }
      doOnEnd { onEnd() }
      start()
    }
  }

  private fun setClippedHeight(newHeight: Int) {
    clipBounds2 = (clipBounds2 ?: Rect()).also {
      it.set(0, 0, right - left, top + newHeight)
      clipBounds = it
    }
    background()?.clippedHeight = newHeight
    invalidate()
  }

  override fun onDetachedFromWindow() {
    animator.cancel()
    super.onDetachedFromWindow()
  }

  @Suppress("DEPRECATION")
  override fun setBackgroundDrawable(background: Drawable?) {
    super.setBackgroundDrawable(background?.let(::HeightClipDrawable))
  }

  private fun background(): HeightClipDrawable? {
    return background as HeightClipDrawable?
  }

  override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
    return if (clipBounds2 != null && !clipBounds2!!.contains(ev)) false
    else super.dispatchTouchEvent(ev)
  }
}

private fun Rect.contains(ev: MotionEvent): Boolean {
  return contains(ev.x.toInt(), ev.y.toInt())
}
