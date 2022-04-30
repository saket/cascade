package me.saket.cascade

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.LayoutScopeMarker
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.MenuItemColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties

@Composable
fun CascadeDropdownMenu(
  expanded: Boolean,
  onDismissRequest: () -> Unit,
  modifier: Modifier = Modifier,
  offset: DpOffset = DpOffset(0.dp, 0.dp),
  properties: PopupProperties = PopupProperties(focusable = true),
  content: @Composable CascadeColumnScope.() -> Unit
) {
  val cascadeProperties = CascadePopupProperties()
  val state = rememberCascadeState()

  DropdownMenu(
    modifier = modifier.requiredWidth(cascadeProperties.width),
    expanded = expanded,
    onDismissRequest = onDismissRequest,
    offset = offset,
    properties = properties,
  ) {
    val columnScope: ColumnScope = this
    val backStackEntry = state.backStack.lastOrNull()
    val currentContent = backStackEntry?.pageContent ?: content

    val scope = remember {
      object : CascadeColumnScope, ColumnScope by columnScope {
        override val cascadeState get() = state
      }
    }
    if (backStackEntry != null) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .clickable { state.navigateBack() }
          .padding(16.dp),
        content = { backStackEntry.header() }
      )
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

  @Composable
  fun DropdownMenuItem(
    text: @Composable () -> Unit,
    children: @Composable CascadeColumnScope.() -> Unit,
    modifier: Modifier = Modifier,
    childrenHeader: @Composable () -> Unit = text,
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
      trailingIcon = trailingIcon,
      enabled = enabled,
      colors = colors,
      contentPadding = contentPadding,
      interactionSource = interactionSource,
    )
  }
}

@Immutable
class CascadePopupProperties(
  val width: Dp = 192.dp
)

@Immutable
class CascadeTheme(

)
