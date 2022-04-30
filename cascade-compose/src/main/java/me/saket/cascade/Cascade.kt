package me.saket.cascade

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.LayoutScopeMarker
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties

@Composable
fun CascadeDropdownMenu(
  expanded: Boolean,
  onDismissRequest: () -> Unit,
  modifier: Modifier = Modifier,
  state: CascadeState = rememberCascadeState(),
  offset: DpOffset = DpOffset(0.dp, 0.dp),
  requiredWidth: Dp = 192.dp,
  properties: PopupProperties = PopupProperties(focusable = true),
  content: @Composable CascadeColumnScope.() -> Unit
) {
  DropdownMenu(
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

    val backStackEntry = state.backStack.lastOrNull()
    val currentContent = backStackEntry?.pageContent ?: content

    backStackEntry?.header?.invoke()

    val columnScope: ColumnScope = this
    val scope = remember(columnScope) {
      object : CascadeColumnScope, ColumnScope by columnScope {
        override val cascadeState: CascadeState get() = state
      }
    }
    scope.currentContent()

    //    val state by remember { mutableStateOf(CascadeMenuState(menu)) }
    //    AnimatedContent(
    //      targetState = state.currentMenuItem,
    //      transitionSpec = {
    //        if (isNavigatingBack(initialState, targetState)) {
    //          animateToPrevious()
    //        } else {
    //          animateToNext()
    //        }
    //      }
    //    ) { targetMenu ->
    //      CascadeMenuContent(
    //        state = state,
    //        targetMenu = targetMenu,
    //        onItemSelected = onItemSelected,
    //        colors = colors,
    //      )
    //    }
  }
}

@Immutable
@LayoutScopeMarker
interface CascadeColumnScope : ColumnScope {
  val cascadeState: CascadeState
  val navigator get() = cascadeState

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
        cascadeState.backStack.add(
          CascadeBackStackEntry(
            header = childrenHeader,
            pageContent = children
          )
        )
      },
      modifier = modifier,
      leadingIcon = leadingIcon,
      trailingIcon = {
        Row(verticalAlignment = CenterVertically) {
          trailingIcon?.invoke()

          Icon(
            imageVector = Icons.Rounded.ArrowRight,
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

// FYI making this function non-inline causes a strange bug where
// the header stops changing when navigating deeper into a sub-menu.
@Composable
inline fun CascadeColumnScope.DropdownMenuHeader(
  modifier: Modifier = Modifier,
  crossinline text: @Composable () -> Unit,
) {
  Row(
    modifier = modifier
      .clickable { navigator.navigateBack() }
      .fillMaxWidth()
      .padding(vertical = 4.dp),
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
        imageVector = Icons.Rounded.ArrowLeft,
        contentDescription = null
      )
      text()
    }
  }
}
