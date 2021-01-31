package me.saket.cascade

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout.LayoutParams.WRAP_CONTENT
import android.widget.ViewFlipper
import androidx.core.animation.doOnEnd
import androidx.core.graphics.withTranslation
import androidx.core.view.doOnLayout
import androidx.interpolator.view.animation.FastOutSlowInInterpolator

/**
 * A [ViewFlipper] that wraps its height to the currently
 * displayed child and smoothly animates height changes.
 *
 * See [show], [goForward] and [goBack].
 */
open class HeightAnimatableViewFlipper(context: Context) : ViewFlipper2(context) {
  var animationDuration = 350L
  var animationInterpolator = FastOutSlowInInterpolator()
  var eventDelegate: EventDelegate = NoOpEventDelegate()

  private var clipBounds2: Rect? = null // Because View#clipBounds creates a new Rect on every call.
  private var animator: ValueAnimator = ObjectAnimator()

  fun show(
    view: View,
    forward: Boolean
  ) {
    enqueueAnimation {
      val index = if (forward) childCount else 0
      val params = view.layoutParams ?: generateDefaultLayoutParams()
      super.addView(view, index, params)
      if (childCount == 1) {
        setDisplayedChild(view)
        return@enqueueAnimation
      }

      val prevView = displayedChildView!!
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
          from = prevView.height + verticalPadding,
          to = view.height + verticalPadding,
          onEnd = { removeView(prevView) }
        )
      }
    }
  }

  open fun goForward(toView: View) =
    show(toView, forward = true)

  open fun goBack(toView: View) =
    show(toView, forward = false)

  override fun addView(child: View, index: Int, params: ViewGroup.LayoutParams) {
    child.layoutParams = params
    show(child, forward = true)
  }

  override fun generateDefaultLayoutParams(): LayoutParams {
    return LayoutParams(MATCH_PARENT, WRAP_CONTENT)
  }

  private fun enqueueAnimation(action: () -> Unit) {
    if (!animator.isRunning) action()
    else animator.doOnEnd { action() }
  }

  private fun animateHeight(from: Int, to: Int, onEnd: () -> Unit) {
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

  override fun onDetachedFromWindow() {
    animator.cancel()
    super.onDetachedFromWindow()
  }

  private fun setClippedHeight(newHeight: Int) {
    clipBounds2 = (clipBounds2 ?: Rect()).also {
      it.set(0, 0, right - left, top + newHeight)
    }
    background()?.clippedHeight = newHeight
    invalidate()
  }

  @Suppress("DEPRECATION")
  override fun setBackgroundDrawable(background: Drawable?) {
    if (background == null) {
      super.setBackgroundDrawable(null)
    } else {
      super.setBackgroundDrawable(
        HeightClipDrawable(
          ForcePaddingsDrawable(background)
        )
      )
    }
  }

  private fun background(): HeightClipDrawable? {
    return background as HeightClipDrawable?
  }

  override fun drawChild(canvas: Canvas, child: View, drawingTime: Long): Boolean {
    if (childCount > 1) {
      // When Views are animating, they'll overlap with each other. Re-draw this
      // layout's background behind each child so that they don't cross-draw.
      canvas.withTranslation(x = child.translationX) {
        background()?.draw(canvas)
      }
    }
    return super.drawChild(canvas, child, drawingTime)
  }

  override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
    if (eventDelegate.dispatchTouchEvent(this, ev)) return true
    if (clipBounds2 != null && !clipBounds2!!.contains(ev)) return false
    return super.dispatchTouchEvent(ev)
  }

  override fun dispatchKeyEvent(event: KeyEvent): Boolean {
    return eventDelegate.dispatchKeyEvent(event) || super.dispatchKeyEvent(event)
  }
}

private fun Rect.contains(ev: MotionEvent): Boolean {
  return contains(ev.x.toInt(), ev.y.toInt())
}

private val View.verticalPadding: Int
  get() = paddingTop + paddingBottom
