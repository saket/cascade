package me.saket.cascade.internal

import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCompositionContext
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.lifecycle.ViewTreeLifecycleOwner
import androidx.lifecycle.ViewTreeViewModelStoreOwner
import androidx.savedstate.findViewTreeSavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import me.saket.cascade.R
import java.util.UUID
import kotlin.math.roundToInt

/**
 * TODO: doc.
 *
 * - [androidx.compose.ui.window.Popup] can't be used because it does not use platform entry/exit transitions.
 * - DropdownPopupMenu() can't be used because it adds vertical paddings.
 */
@Composable
internal fun CascadePopup(
  offset: DpOffset,
  content: @Composable () -> Unit
) {
  val density = LocalDensity.current
  val hostView = LocalView.current
  val layoutDirection = LocalLayoutDirection.current

  val popupId = rememberSaveable { UUID.randomUUID() }
  val compositionContext = rememberCompositionContext()

  val popupPositionProvider = remember {
    CascadePopupPositionProvider(contentOffset = offset, density = density)
  }

  val windowSizeRectBuffer = remember { android.graphics.Rect(0, 0, 0, 0) }
  var anchorBounds by remember { mutableStateOf(IntRect.Zero) }
  var popupPosition by remember { mutableStateOf(IntOffset.Zero) }

  Box(
    Modifier.onGloballyPositioned { coordinates ->
      val parentCoordinates = coordinates.parentLayoutCoordinates!!
      val layoutPosition = parentCoordinates.positionInWindow().let { offset ->
        IntOffset(x = offset.x.roundToInt(), y = offset.y.roundToInt())
      }
      val layoutSize = parentCoordinates.size
      anchorBounds = IntRect(layoutPosition, layoutSize)
    }
  )

  val popup = remember {
    ComposableCascadePopupWindow(
      hostView = hostView,
      popupId = popupId
    ).apply {
      contentView = ComposeView(hostView.context).apply {
        setParentCompositionContext(compositionContext)
        setContent {
          Box(
            modifier = Modifier
              .wrapContentSize(align = Alignment.TopStart)
              .absoluteOffset { popupPosition }
              .onSizeChanged { contentSize ->
                val windowBounds = windowSizeRectBuffer.let { rect ->
                  hostView.getWindowVisibleDisplayFrame(windowSizeRectBuffer)
                  IntRect(left = rect.left, top = rect.top, right = rect.right, bottom = rect.bottom)
                }

                popupPosition = popupPositionProvider.calculatePosition(
                  anchorBounds = anchorBounds,
                  windowBounds = windowBounds,
                  layoutDirection = layoutDirection,
                  popupContentSize = contentSize,
                )
              },
            content = { content() },
          )
        }
      }
    }
  }

  DisposableEffect(Unit) {
    popup.show(anchor = hostView, xOffset = 0, yOffset = 0, gravity = Gravity.NO_GRAVITY)
    onDispose {
      popup.dismiss()
    }
  }
}

private class ComposableCascadePopupWindow(
  private val hostView: View,
  private val popupId: UUID
) : BaseCascadePopupWindow(hostView.context) {

  init {
    width = WindowManager.LayoutParams.MATCH_PARENT
    height = WindowManager.LayoutParams.MATCH_PARENT
  }

  override fun setContentView(contentView: View) {
    super.setContentView(contentView)

    // During onAttachedToWindow, AbstractComposeView will attempt to resolve its
    // parent's CompositionContext, which requires first finding the "content view",
    // then using that to find a root view with a ViewTreeLifecycleOwner.
    contentView.id = android.R.id.content

    ViewTreeLifecycleOwner.set(contentView, ViewTreeLifecycleOwner.get(hostView))
    ViewTreeViewModelStoreOwner.set(contentView, ViewTreeViewModelStoreOwner.get(hostView))
    contentView.setViewTreeSavedStateRegistryOwner(hostView.findViewTreeSavedStateRegistryOwner())

    // Unique id for AbstractComposeView. This allows state restoration
    // for the state defined inside the Popup via rememberSaveable().
    contentView.setTag(R.id.compose_view_saveable_id_tag, "Popup:$popupId")

    // Enable children to draw their shadow by not clipping them.
    if (contentView is ViewGroup) {
      contentView.clipChildren = false
    }

    setOnDismissListener {
      // I'm not sure why this would be needed, but material3 does this.
      ViewTreeLifecycleOwner.set(contentView, null)
    }
  }
}
