@file:Suppress("TestFunctionName")

package me.saket.cascade

import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.cash.paparazzi.Paparazzi
import app.cash.paparazzi.androidHome
import app.cash.paparazzi.detectEnvironment
import com.google.common.truth.Truth.assertThat
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import me.saket.cascade.internal.MinSdkReader
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(TestParameterInjector::class)
class CascadeDropdownMenuTest(
  @TestParameter val layoutDirection: LayoutDirection
) {

  private val apiLevel = 32
  @get:Rule val paparazzi = Paparazzi(
    environment = detectEnvironment().copy(
      platformDir = "${androidHome()}/platforms/android-$apiLevel",
    )
  )

  @Test fun `menu with no sub-menus`() {
    paparazzi.snapshot {
      PopupScaffold {
        DropdownMenuItem(
          text = { Text("Horizon") },
          onClick = {}
        )
        DropdownMenuItem(
          text = { Text("The Witcher 3") },
          onClick = {}
        )
      }
    }
  }

  @Test fun `menu with sub-menus`() {
    paparazzi.snapshot {
      PopupScaffold {
        DropdownMenuItem(
          text = { Text("Horizon") },
          children = {
            DropdownMenuItem(
              text = { Text("Zero Dawn") },
              onClick = {}
            )
            DropdownMenuItem(
              text = { Text("Forbidden West") },
              onClick = {}
            )
          }
        )
        DropdownMenuItem(
          text = { Text("The Witcher 3") },
          onClick = {}
        )
      }
    }
  }

  @Test fun `navigate to a sub-menu with a header`() {
    // Paparazzi currently only composes once so
    // the navigation must happen before hand.
    val state = CascadeState().apply {
      navigateTo(
        CascadeBackStackEntry(
          header = {
            DropdownMenuHeader {
              Text("Horizon")
            }
          },
          childrenContent = {
            DropdownMenuItem(
              text = { Text("Zero Dawn") },
              onClick = {}
            )
            DropdownMenuItem(
              text = { Text("Forbidden West") },
              onClick = {}
            )
          }
        )
      )
    }

    paparazzi.snapshot {
      PopupScaffold(state) {
        Text("This should get replaced by the sub-menu")
      }
    }
  }

  @Test fun `navigate to a sub-menu with a custom header`() {
    // Paparazzi currently only composes once so
    // the navigation must happen before hand.
    val state = CascadeState().apply {
      navigateTo(
        CascadeBackStackEntry(
          header = {
            Text(
              modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black),
              text = "Horizon",
              color = Color.White,
            )
          },
          childrenContent = {
            DropdownMenuItem(
              text = { Text("Zero Dawn") },
              onClick = {}
            )
            DropdownMenuItem(
              text = { Text("Forbidden West") },
              onClick = {}
            )
          }
        )
      )
    }

    paparazzi.snapshot {
      PopupScaffold(state) {
        Text("This should get replaced by the sub-menu")
      }
    }
  }

  @Test fun `header can be used in menus without any parent`() {
    paparazzi.snapshot {
      PopupScaffold {
        DropdownMenuHeader {
          Text("A header that overflows to the next line")
        }
        DropdownMenuItem(
          text = { Text("This menu does not have a parent so the header should not have a back icon") },
          onClick = {},
          contentPadding = PaddingValues(start = 12.dp, end = 12.dp, bottom = 8.dp)
        )
      }
    }
  }

  @Test fun `custom padding values for header in the root menu`() {
    paparazzi.snapshot {
      PopupScaffold {
        DropdownMenuHeader(
          contentPadding = PaddingValues(start = 0.dp, end = 16.dp)
        ) {
          Box(
            Modifier
              .fillMaxWidth()
              .height(32.dp)
              .background(Color.Black)
          )
        }
        DropdownMenuItem(
          text = { Text("The header should not have any leading space") },
          onClick = {},
          contentPadding = PaddingValues(start = 12.dp, end = 12.dp, bottom = 8.dp)
        )
      }
    }
  }

  @Test fun `custom shape`() {
    paparazzi.snapshot {
      PopupScaffold(
        shape = RoundedCornerShape(24.dp)
      ) {
        DropdownMenuItem(text = { Text("I just called") }, onClick = {})
        DropdownMenuItem(text = { Text("to say") }, onClick = {})
        DropdownMenuItem(text = { Text("I love you") }, onClick = {})
      }
    }
  }

  @Test fun `custom shadow elevation`(
    @TestParameter("23", "31", "34") minSdk: Int
  ) {
    paparazzi.snapshot {
      PopupScaffold(
        shadowElevation = CascadeDefaults.shadowElevation + 20.dp,
        minSdkReader = { minSdk },
      ) {
        DropdownMenuItem(text = { Text("I just called") }, onClick = {})
        DropdownMenuItem(text = { Text("to say") }, onClick = {})
        DropdownMenuItem(text = { Text("I love you") }, onClick = {})
      }
    }
  }

  // TODO: this should be an instrumented test, but I'm unable to enable animations in UI tests.
  @Test fun `prevent multiple navigations`() {
    val menuHeader: @Composable CascadeColumnScope.() -> Unit = { Text("Header") }
    val menuContent: @Composable CascadeColumnScope.() -> Unit = { Text("Content") }

    val state = CascadeState().apply {
      navigateTo(
        CascadeBackStackEntry(
          header = menuHeader,
          childrenContent = menuContent
        )
      )
      navigateTo(
        CascadeBackStackEntry(
          header = menuHeader,
          childrenContent = menuContent
        )
      )
    }

    state.navigateBack()
    assertThat(state.canNavigateBack()).isFalse()
  }

  /**
   * Recreates [androidx.compose.material3.DropdownMenuContent] because paparazzi can't screenshot popups yet.
   */
  @Composable
  private fun PopupScaffold(
    state: CascadeState = rememberCascadeState(),
    shape: Shape = CascadeDefaults.shape,
    shadowElevation: Dp = CascadeDefaults.shadowElevation,
    minSdkReader: MinSdkReader = MinSdkReader { apiLevel },
    content: @Composable CascadeColumnScope.() -> Unit
  ) {
    CascadeMaterialTheme {
      Box(
        Modifier
          .fillMaxSize()
          .background(MaterialTheme.colorScheme.background)
          .padding(24.dp)
          .wrapContentSize(align = Alignment.TopEnd)
      ) {
        CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
          PopupContent(
            state = state,
            fixedWidth = CascadeDefaults.menuWidth,
            shadowElevation = shadowElevation,
            expandedStates = MutableTransitionState(true),
            transformOriginState = remember { mutableStateOf(TransformOrigin.Center) },
            shape = shape,
            minSdkReader = minSdkReader,
            content = content,
          )
        }
      }
    }
  }
}

@Composable
fun CascadeMaterialTheme(content: @Composable () -> Unit) {
  val colors = lightColorScheme(
    primary = Color(0xFFB5D2C3),
    background = Color(0xFFB5D2C3),
    surface = Color(0xFFE5F0EB),
    onSurface = Color(0xFF356859),
    onSurfaceVariant = Color(0xFF356859),
  )
  val typography = Typography(
    titleLarge = MaterialTheme.typography.titleLarge.copy(
      fontWeight = FontWeight.Bold,
      fontSize = 20.sp,
    ),
    labelLarge = MaterialTheme.typography.labelLarge.copy(
      fontWeight = FontWeight.Medium,
      fontSize = 16.sp,
    ),
  )
  MaterialTheme(
    colorScheme = colors,
    typography = typography,
    content = content
  )
}
