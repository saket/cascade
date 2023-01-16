@file:Suppress("TestFunctionName")

package me.saket.cascade

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.cash.paparazzi.Paparazzi
import com.squareup.burst.BurstJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(BurstJUnit4::class)
class CascadeDropdownMenuTest(
  private val layoutDirection: LayoutDirection
) {

  @get:Rule val paparazzi = Paparazzi()

  @Test fun `menu with no sub-menus`() {
    paparazzi.snapshot {
      PopupScaffold {
        CascadeDropdownMenuContent(state = rememberCascadeState()) {
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
  }

  @Test fun `menu with sub-menus`() {
    paparazzi.snapshot {
      PopupScaffold {
        CascadeDropdownMenuContent(state = rememberCascadeState()) {
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
      PopupScaffold {
        CascadeDropdownMenuContent(
          modifier = Modifier.testTag("foo"),
          state = state,
          content = {}
        )
      }
    }
  }

  @Test fun `popup with content longer than available window height`() {
    // todo.
  }

  /**
   * Recreates [androidx.compose.material3.DropdownMenuContent]
   */
  @Composable
  private fun PopupScaffold(content: @Composable () -> Unit) {
    CascadeMaterialTheme {
      Box(
        Modifier
          .fillMaxSize()
          .background(MaterialTheme.colorScheme.background)
      ) {
        Box(
          Modifier
            .padding(24.dp)
            .requiredWidth(196.dp)  // Same as used by CascadeDropdownMenu().
            .wrapContentHeight()
            .shadow(3.dp, MaterialTheme.shapes.extraSmall)
            .clip(MaterialTheme.shapes.extraSmall)
            .background(MaterialTheme.colorScheme.surface)
            .padding(vertical = 8.dp)
        ) {
          CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
            content()
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
