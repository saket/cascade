@file:OptIn(ExperimentalAnimationApi::class, ExperimentalComposeUiApi::class)

package me.saket.cascade

import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.LayoutScopeMarker
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowLeft
import androidx.compose.material.icons.rounded.ArrowRight
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.MenuItemColors
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCompositionContext
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.LayoutDirection.Ltr
import androidx.compose.ui.unit.LayoutDirection.Rtl
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.ViewTreeLifecycleOwner
import androidx.lifecycle.ViewTreeViewModelStoreOwner
import androidx.savedstate.findViewTreeSavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import me.saket.cascade.internal.DropdownMenuPositionProvider
import me.saket.cascade.internal.cascadeTransitionSpec
import java.util.UUID
import kotlin.math.roundToInt


/**
 * Material Design dropdown menu with support for nested menus.
 * See [DropdownMenu] for documentation about its parameters.
 *
 * Example usage:
 *
 * ```
 * var expanded by rememberSaveable { mutableStateOf(false) }
 *
 * CascadeDropdownMenu(
 *   expanded = expanded,
 *   onDismissRequest = { expanded = false }
 * ) {
 *   DropdownMenuItem(
 *     text = { Text("Horizon") },
 *     children = {
 *       DropdownMenuItem(
 *         text = { Text("Zero Dawn") },
 *         onClick = { ... }
 *       )
 *       DropdownMenuItem(
 *         text = { Text("Forbidden West") },
 *         onClick = { ... }
 *       )
 *     }
 *   )
 * }
 * ```
 */
@Composable
fun CascadeDropdownMenu(
  expanded: Boolean,
  onDismissRequest: () -> Unit,
  modifier: Modifier = Modifier,
  offset: DpOffset = DpOffset(0.dp, 0.dp),
  properties: PopupProperties = PopupProperties(focusable = true),
  fixedWidth: Dp = 196.dp,
  state: CascadeState = rememberCascadeState(),
  content: @Composable CascadeColumnScope.() -> Unit
) {
  //  DropdownMenu(
  //    modifier = modifier,
  //    expanded = expanded,
  //    onDismissRequest = { onDismissRequest() },
  //    offset = offset,
  //    properties = properties,
  //  ) {
  //    CascadeDropdownMenuContent(
  //      // A fixed width is needed because DropdownMenu
  //      // does not handle width changes smoothly.
  //      modifier = Modifier.requiredWidth(fixedWidth),
  //      state = state,
  //      content = content
  //    )
  //  }

  val context = LocalContext.current
  val density = LocalDensity.current
  val hostView = LocalView.current
  val layoutDirection = LocalLayoutDirection.current
  val parentComposition = rememberCompositionContext()

  val popupId = rememberSaveable { UUID.randomUUID() }
  val popup = remember { ComposablePopupWindow(hostView, popupId) }

  var anchorBounds by remember { mutableStateOf(IntRect.Zero) }

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

  DisposableEffect(Unit) {
    popup.width = ViewGroup.LayoutParams.MATCH_PARENT
    popup.height = ViewGroup.LayoutParams.MATCH_PARENT
    popup.contentView.background = null
    popup.showAsDropDown(hostView, xoff = 0, yoff = 0, gravity = Gravity.NO_GRAVITY)

    val popupPositionProvider = DropdownMenuPositionProvider(
      contentOffset = offset,
      density = density,
    )

    popup.contentView.show(
      ComposeView(context).apply {
        setParentCompositionContext(parentComposition)
        setContent {
          val windowSizeRectBuffer = remember { android.graphics.Rect(0, 0, 0, 0) }
          var popupPosition by remember { mutableStateOf(IntOffset.Zero) }

          CascadeDropdownMenuContent(
            modifier = modifier
              .fillMaxSize()
              .wrapContentWidth(align = Alignment.Start)  // Without this, requiredWidth() centers the content.
              .requiredWidth(fixedWidth)
              .wrapContentHeight(align = Alignment.Top)
              .absoluteOffset { popupPosition }
              .onSizeChanged {
                val windowBounds = windowSizeRectBuffer.let { rect ->
                  hostView.getWindowVisibleDisplayFrame(windowSizeRectBuffer)
                  IntRect(left = rect.left, top = rect.top, right = rect.right, bottom = rect.bottom)
                }

                popupPosition = popupPositionProvider.calculatePosition(
                  anchorBounds = anchorBounds,
                  windowBounds = windowBounds,
                  layoutDirection = layoutDirection,
                  popupContentSize = it,
                )
              }
              .padding(start = 4.dp, end = 4.dp, bottom = 4.dp),
            state = state,
            content = content
          )
        }
      },
      forward = true
    )

    onDispose {
      popup.dismiss()
    }
  }
}

private class ComposablePopupWindow(hostView: View, popupId: UUID) : CascadePopupWindow(hostView.context) {
  init {
    contentView.id = android.R.id.content
    ViewTreeLifecycleOwner.set(contentView, ViewTreeLifecycleOwner.get(hostView))
    ViewTreeViewModelStoreOwner.set(contentView, ViewTreeViewModelStoreOwner.get(hostView))
    contentView.setViewTreeSavedStateRegistryOwner(hostView.findViewTreeSavedStateRegistryOwner())
    // Set unique id for AbstractComposeView. This allows state restoration for the state
    // defined inside the Popup via rememberSaveable()
    contentView.setTag(R.id.compose_view_saveable_id_tag, "Popup:$popupId")

    // Enable children to draw their shadow by not clipping them
    contentView.clipChildren = false
  }
}

@Composable
internal fun CascadeDropdownMenuContent(
  state: CascadeState,
  modifier: Modifier = Modifier,
  content: @Composable CascadeColumnScope.() -> Unit
) {
  DisposableEffect(Unit) {
    onDispose {
      state.resetBackStack()
    }
  }

  val layoutDirection = LocalLayoutDirection.current
  Surface(
    modifier = modifier,
    shape = MaterialTheme.shapes.extraSmall,
    tonalElevation = 3.dp,
    shadowElevation = 3.dp,
  ) {
    AnimatedContent(
      targetState = state.backStackSnapshot(),
      transitionSpec = { cascadeTransitionSpec(layoutDirection) }
    ) { backStack ->
      Column(
        Modifier
          // Provide a solid background color to prevent the
          // content of sub-menus from leaking into each other.
          .background(MaterialTheme.colorScheme.surface)
          // Block navigation while a transition is already playing because the
          // current transitionSpec isn't great at handling another navigation
          // while one is already running.
          .pointerInteropFilter { transition.isRunning }
      ) {
        val currentContent = backStack.topMostEntry?.childrenContent ?: content
        backStack.topMostEntry?.header?.invoke()

        val contentScope = remember { CascadeColumnScope(state) }
        contentScope.currentContent()
      }
    }
  }
}

@Immutable
@LayoutScopeMarker
interface CascadeColumnScope : ColumnScope {
  val cascadeState: CascadeState

  /**
   * Material Design dropdown menu item that navigates to a sub-menu on click.
   * See [androidx.compose.material3.DropdownMenuItem] for documentation about its parameters.
   *
   * For sub-menus, cascade will automatically navigate to their parent menu when their
   * header is clicked. For manual navigation, [CascadeState.navigateBack] can be used.
   *
   * ```
   * val state = rememberCascadeState()
   *
   * CascadeDropdownMenu(state = state, ...) {
   *   DropdownMenuItem(
   *     text = { Text("Are you sure?"),
   *     children = {
   *       DropdownMenuItem(
   *          text = { Text("Not really") },
   *          onClick = { state.navigateBack() }
   *        )
   *     }
   *   )
   * }
   * ```
   */
  @Composable
  fun DropdownMenuItem(
    text: @Composable () -> Unit,
    children: @Composable CascadeColumnScope.() -> Unit,
    modifier: Modifier = Modifier,
    childrenHeader: @Composable () -> Unit = { DropdownMenuHeader(text = text) },
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    enabled: Boolean = true,
    colors: MenuItemColors = MenuDefaults.itemColors(),
    contentPadding: PaddingValues = MenuDefaults.DropdownMenuItemContentPadding,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
  ) {
    DropdownMenuItem(
      text = text,
      onClick = {
        cascadeState.navigateTo(
          CascadeBackStackEntry(
            header = childrenHeader,
            childrenContent = children
          )
        )
      },
      modifier = modifier,
      leadingIcon = leadingIcon,
      trailingIcon = {
        Row(verticalAlignment = CenterVertically) {
          trailingIcon?.invoke()

          val requiredGapWithEdge = 4.dp
          val iconOffset = contentPadding.calculateEndPadding(LocalLayoutDirection.current) - requiredGapWithEdge
          Icon(
            modifier = Modifier.offset(x = iconOffset),
            imageVector = when (LocalLayoutDirection.current) {
              Ltr -> Icons.Rounded.ArrowRight
              Rtl -> Icons.Rounded.ArrowLeft
            },
            contentDescription = null
          )
        }
      },
      enabled = enabled,
      colors = colors,
      contentPadding = contentPadding,
      interactionSource = interactionSource,
    )
  }
}

/**
 * Displays `text` with a back icon. Navigates to its parent menu when clicked.
 */
// FYI making this function non-inline causes a strange bug where
// `text` stops changing when navigating deeper into a sub-menu.
@Composable
inline fun CascadeColumnScope.DropdownMenuHeader(
  modifier: Modifier = Modifier,
  contentPadding: PaddingValues = PaddingValues(vertical = 4.dp),
  crossinline text: @Composable () -> Unit,
) {
  Row(
    modifier = modifier
      .clickable { cascadeState.navigateBack() }
      .fillMaxWidth()
      .padding(contentPadding),
    verticalAlignment = CenterVertically
  ) {
    val headerColor = LocalContentColor.current.copy(alpha = 0.6f)
    val headerStyle = MaterialTheme.typography.labelLarge.run { // labelLarge is used by DropdownMenuItem().
      copy(
        fontSize = fontSize * 0.9f,
        letterSpacing = letterSpacing * 0.9f
      )
    }
    CompositionLocalProvider(
      LocalContentColor provides headerColor,
      LocalTextStyle provides headerStyle
    ) {
      Icon(
        modifier = Modifier.requiredSize(32.dp),
        imageVector = when (LocalLayoutDirection.current) {
          Ltr -> Icons.Rounded.ArrowLeft
          Rtl -> Icons.Rounded.ArrowRight
        },
        contentDescription = null
      )
      Box(Modifier.weight(1f)) {
        text()
      }
    }
  }
}

@Suppress("FunctionName")
private fun ColumnScope.CascadeColumnScope(state: CascadeState): CascadeColumnScope =
  object : CascadeColumnScope, ColumnScope by this {
    override val cascadeState get() = state
  }
