@file:OptIn(ExperimentalAnimationApi::class, ExperimentalComposeUiApi::class)

package me.saket.cascade

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.LayoutScopeMarker
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidth
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.LayoutDirection.Ltr
import androidx.compose.ui.unit.LayoutDirection.Rtl
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import me.saket.cascade.internal.cascadeTransitionSpec

/**
 * Material Design dropdown menu with support for nested menus.
 * See [DropdownMenu] for documentation about its parameters.
 *
 * Example usage:
 *
 * ```
 * val expanded by rememberSaveable { mutableStateOf(false) }
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
  requiredWidth: Dp = 196.dp,
  state: CascadeState = rememberCascadeState(),
  content: @Composable CascadeScope.() -> Unit
) {
  DropdownMenu(
    // A fixed width is needed because DropdownMenu
    // does not handle width changes smoothly.
    modifier = modifier.requiredWidth(requiredWidth),
    expanded = expanded,
    onDismissRequest = { onDismissRequest() },
    offset = offset,
    properties = properties,
  ) {
    DisposableEffect(Unit) {
      onDispose {
        state.backStack.clear()
      }
    }

    val layoutDirection = LocalLayoutDirection.current
    AnimatedContent(
      targetState = state.backStack.snapshot(),
      transitionSpec = { cascadeTransitionSpec(layoutDirection) }
    ) { snapshot ->
      // Surface provides a solid background color to prevent the
      // content of sub-menus from leaking into each other.
      Surface(
        // Block navigation while a transition is already playing because the
        // current transitionSpec isn't great at handling another navigation
        // while one is already running.
        Modifier.pointerInteropFilter { transition.isRunning }
      ) {
        Column {
          val currentContent = snapshot.topMostEntry?.childrenContent ?: content
          snapshot.topMostEntry?.header?.invoke()

          val contentScope = remember { CascadeScope(state) }
          contentScope.currentContent()
        }
      }
    }
  }
}

@Immutable
@LayoutScopeMarker
interface CascadeScope : ColumnScope {
  val cascadeState: CascadeState
  val cascadeNavigator: CascadeBackNavigator2

  @Composable
  fun DropdownMenuItem(
    text: @Composable () -> Unit,
    children: @Composable CascadeScope.() -> Unit,
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
        cascadeState.backStack.add(
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
// the header stops changing when navigating deeper into a sub-menu.
@Composable
inline fun CascadeScope.DropdownMenuHeader(
  modifier: Modifier = Modifier,
  contentPadding: PaddingValues = PaddingValues(vertical = 4.dp),
  crossinline text: @Composable () -> Unit,
) {
  Row(
    modifier = modifier
      .clickable { cascadeNavigator.navigateBack() }
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
private fun ColumnScope.CascadeScope(state: CascadeState): CascadeScope =
  object : CascadeScope, ColumnScope by this {
    override val cascadeState get() = state
    override val cascadeNavigator get() = state
  }
