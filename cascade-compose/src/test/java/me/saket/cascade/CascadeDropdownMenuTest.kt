@file:Suppress("TestFunctionName")

package me.saket.cascade

import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
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
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.cash.paparazzi.Paparazzi
import app.cash.paparazzi.androidHome
import app.cash.paparazzi.detectEnvironment
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(TestParameterInjector::class)
class CascadeDropdownMenuTest(
  @TestParameter val layoutDirection: LayoutDirection
) {

  @get:Rule val paparazzi = Paparazzi(
    environment = detectEnvironment().copy(
      platformDir = "${androidHome()}/platforms/android-32",
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

  @Test fun `navigate to a sub-menu with header`() {
    paparazzi.snapshot {
      val state = rememberCascadeState()

      // Paparazzi currently only composes once so
      // the navigation must happen before hand.
      state.navigateTo(
        CascadeBackStackEntry(
          header = {
            FakeCascadeColumnScope(state).DropdownMenuHeader {
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
      PopupScaffold(state) {
        Text("This should get replaced by the sub-menu")
      }
    }
  }

  /**
   * Recreates [androidx.compose.material3.DropdownMenuContent]
   */
  @Composable
  private fun PopupScaffold(
    state: CascadeState = rememberCascadeState(),
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
            fixedWidth = 196.dp, // Same as used by CascadeDropdownMenu().
            shadowElevation = 0.dp,
            expandedStates = MutableTransitionState(true),
            transformOriginState = remember { mutableStateOf(TransformOrigin.Center) },
            content = content,
            tonalElevation = 0.dp,
          )
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
    val shapes = Shapes(
      extraSmall = RoundedCornerShape(8.dp)
    )
    MaterialTheme(
      colorScheme = colors,
      typography = typography,
      shapes = shapes,
      content = content
    )
  }
}
