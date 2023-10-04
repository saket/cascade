package me.saket.cascade

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.LayoutScopeMarker
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.LayoutDirection.Ltr
import androidx.compose.ui.unit.LayoutDirection.Rtl
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onEach
import me.saket.cascade.internal.AnimateEntryExit
import me.saket.cascade.internal.CoercePositiveValues
import me.saket.cascade.internal.DropdownMenuPositionProvider
import me.saket.cascade.internal.MinSdkReader
import me.saket.cascade.internal.PositionPopupContent
import me.saket.cascade.internal.RealMinSdkReader
import me.saket.cascade.internal.ScreenRelativeBounds
import me.saket.cascade.internal.calculateTransformOrigin
import me.saket.cascade.internal.cascadeTransitionSpec
import me.saket.cascade.internal.clickableWithoutRipple
import me.saket.cascade.internal.copy
import me.saket.cascade.internal.then

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
 *         onClick = { … }
 *       )
 *       DropdownMenuItem(
 *         text = { Text("Forbidden West") },
 *         onClick = { … }
 *       )
 *     }
 *   )
 * }
 * ```
 *
 * @param fixedWidth A width that will be shared by all nested menus. This can be removed
 * in the future once cascade is able to animate width changes across nested menus.
 *
 * @param shadowElevation A value between 0dp and 8dp. Cascade trims values above 8dp to match [DropdownMenu]'s behavior.
 * [More context can be found here](https://android-review.googlesource.com/c/platform/frameworks/support/+/2117953).
 */
@Composable
fun CascadeDropdownMenu(
  expanded: Boolean,
  onDismissRequest: () -> Unit,
  modifier: Modifier = Modifier,
  offset: DpOffset = DpOffset.Zero,
  fixedWidth: Dp = CascadeDefaults.menuWidth,
  shadowElevation: Dp = CascadeDefaults.shadowElevation,
  properties: PopupProperties = PopupProperties(focusable = true),
  state: CascadeState = rememberCascadeState(),
  shape: Shape = CascadeDefaults.shape,
  content: @Composable CascadeColumnScope.() -> Unit
) {
  val expandedStates = remember { MutableTransitionState(false) }
  expandedStates.targetState = expanded

  if (expandedStates.currentState || expandedStates.targetState) {
    val transformOriginState = remember { mutableStateOf(TransformOrigin.Center) }
    val popupPositionProvider = CoercePositiveValues(
      DropdownMenuPositionProvider(
        offset,
        LocalDensity.current
      ) { parentBounds, menuBounds ->
        transformOriginState.value = calculateTransformOrigin(
          parentBounds = parentBounds,
          menuBounds = CoercePositiveValues.correctMenuBounds(menuBounds)
        )
      }
    )

    val anchorHostView = LocalView.current
    var anchorBounds: ScreenRelativeBounds? by remember { mutableStateOf(null) }
    Box(
      Modifier.onGloballyPositioned { coordinates ->
        // FYI:
        // coordinates -> this box.
        // coordinates.parent -> "anchor" composable that contains CascadeDropdownMenu().
        anchorBounds = ScreenRelativeBounds(coordinates.parentLayoutCoordinates!!, owner = anchorHostView)
      }
    )

    // A full sized popup is shown so that content can render fake shadows
    // that do not suffer from https://issuetracker.google.com/issues/236109671.
    Popup(
      onDismissRequest = onDismissRequest,
      properties = properties.copy(usePlatformDefaultWidth = false),
    ) {
      PositionPopupContent(
        modifier = Modifier
          .fillMaxSize()
          .then(properties.dismissOnClickOutside) {
            clickableWithoutRipple(onClick = onDismissRequest)
          },
        positionProvider = popupPositionProvider,
        anchorBounds = anchorBounds,
        properties = properties,
      ) {
        PopupContent(
          modifier = Modifier
            // Prevent clicks from leaking behind. Otherwise, they'll get picked up as outside
            // clicks to dismiss the popup. This must be set _before_ the downstream modifiers to
            // avoid overriding any clickable modifiers registered by the developer.
            .clickableWithoutRipple {}
            .then(modifier),
          state = state,
          fixedWidth = fixedWidth,
          expandedStates = expandedStates,
          transformOriginState = transformOriginState,
          shadowElevation = shadowElevation,
          shape = shape,
          content = content
        )
      }
    }
  }
}

@Composable
internal fun PopupContent(
  modifier: Modifier = Modifier,
  state: CascadeState,
  fixedWidth: Dp,
  shadowElevation: Dp,
  shape: Shape,
  expandedStates: MutableTransitionState<Boolean>,
  transformOriginState: MutableState<TransformOrigin>,
  minSdkReader: MinSdkReader = RealMinSdkReader,
  content: @Composable (CascadeColumnScope.() -> Unit)
) {
  AnimateEntryExit(
    expandedStates = expandedStates,
    transformOriginState = transformOriginState,
    shadowElevation = if (minSdkReader.minSdk() >= 31) {
      shadowElevation
    } else {
      // 8dp is the recommended max elevation on < API 31.
      // More context here: https://android-review.googlesource.com/c/platform/frameworks/support/+/2117953
      shadowElevation.coerceAtMost(8.dp)
    },
    shape = shape,
  ) {
    CascadeDropdownMenuContent(
      modifier = Modifier
        .requiredWidth(fixedWidth)
        .then(modifier),
      state = state,
      tonalElevation = shadowElevation,
      shape = shape,
      content = content
    )
  }
}

@Composable
@OptIn(ExperimentalAnimationApi::class)
private fun CascadeDropdownMenuContent(
  state: CascadeState,
  modifier: Modifier = Modifier,
  tonalElevation: Dp,
  shape: Shape,
  content: @Composable CascadeColumnScope.() -> Unit,
) {
  DisposableEffect(Unit) {
    onDispose {
      state.resetBackStack()
    }
  }

  Surface(
    shape = shape,
    color = MaterialTheme.colorScheme.surface,
    tonalElevation = tonalElevation,
  ) {
    val isTransitionRunning = remember { MutableStateFlow(false) }
    val backStackSnapshot by remember {
      snapshotFlow { state.backStackSnapshot() }
        .onEach {
          // Block until any ongoing transition has finished. This is a very crude
          // way of queueing navigations. AnimatedContent() does not like it when
          // its content is changed before it is able to finish a transition.
          isTransitionRunning.first { running -> !running }
        }
    }.collectAsState(initial = state.backStackSnapshot())

    val layoutDirection = LocalLayoutDirection.current
    AnimatedContent(
      modifier = modifier,
      targetState = backStackSnapshot,
      transitionSpec = { cascadeTransitionSpec(layoutDirection) },
      label = "cascadeAnimation"
    ) { snapshot ->
      Column(
        Modifier
          // Provide a solid background color to prevent the
          // content of sub-menus from leaking into each other.
          .background(MaterialTheme.colorScheme.surfaceColorAtElevation(tonalElevation))
          .verticalScroll(rememberScrollState())
      ) {
        val contentScope = remember(this, snapshot) {
          object : CascadeColumnScope, ColumnScope by this {
            override val cascadeState get() = state
            override val hasParentMenu: Boolean get() = snapshot.hasParentMenu()
            override val isNavigationRunning: Boolean get() = isTransitionRunning.value
          }
        }
        val currentContent = snapshot.topMostEntry?.childrenContent ?: content
        with(contentScope) {
          snapshot.topMostEntry?.header?.invoke(contentScope)
          currentContent()
        }
      }

      LaunchedEffect(transition.isRunning) {
        isTransitionRunning.tryEmit(transition.isRunning)
      }
    }
  }
}

@Immutable
@LayoutScopeMarker
interface CascadeColumnScope : ColumnScope {
  val cascadeState: CascadeState

  /** True if this is a sub-menu and can navigate back. */
  val hasParentMenu: Boolean

  /** Indicates whether there is any animation running for changing menus. */
  val isNavigationRunning: Boolean

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
    childrenHeader: @Composable CascadeColumnScope.() -> Unit = { DropdownMenuHeader(text = text) },
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

  /**
   * Displays `text` with a back icon. Navigates to its parent menu when clicked.
   */
  @Composable
  fun DropdownMenuHeader(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(10.5.dp),
    text: @Composable () -> Unit,
  ) {
    Row(
      modifier = modifier
        .fillMaxWidth()
        .clickable(enabled = hasParentMenu, role = Role.Button) {
          if (!isNavigationRunning) { // Prevent accidental double clicks.
            cascadeState.navigateBack()
          }
        }
        .padding(contentPadding),
      verticalAlignment = CenterVertically,
    ) {
      val headerColor = LocalContentColor.current.copy(alpha = 0.6f)
      val headerStyle = MaterialTheme.typography.labelLarge.run { // Same style as DropdownMenuItem().
        copy(
          fontSize = fontSize * 0.9f,
          letterSpacing = letterSpacing * 0.9f
        )
      }
      CompositionLocalProvider(
        LocalContentColor provides headerColor,
        LocalTextStyle provides headerStyle
      ) {
        if (this@CascadeColumnScope.hasParentMenu) {
          Image(
            modifier = Modifier
              .padding(end = contentPadding.calculateEndPadding(LocalLayoutDirection.current))
              .size(11.dp),
            painter = painterResource(R.drawable.cascade_ic_arrow_left),
            contentDescription = null,
            colorFilter = ColorFilter.tint(headerColor),
          )
        }
        Box(Modifier.weight(1f)) {
          text()
        }
      }
    }
  }
}
