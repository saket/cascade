@file:Suppress("TestFunctionName", "JUnitMalformedDeclaration")

package me.saket.cascade

import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.view.WindowInsets
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Anchor
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Blue
import androidx.compose.ui.graphics.Color.Companion.Cyan
import androidx.compose.ui.graphics.Color.Companion.Magenta
import androidx.compose.ui.graphics.Color.Companion.Red
import androidx.compose.ui.graphics.Color.Companion.Yellow
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.test.core.app.takeScreenshot
import com.dropbox.dropshots.Dropshots
import com.dropbox.dropshots.ThresholdValidator
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestName
import org.junit.runner.RunWith

@RunWith(TestParameterInjector::class)
internal class CascadePopupAlignmentTest(
  @TestParameter private val showActionBar: ShowActionBarParam,
) {
  @get:Rule val composeTestRule = createAndroidComposeRule<TestActivity>()
  @get:Rule val testName = TestName()
  @get:Rule val dropshots = Dropshots(
    filenameFunc = { it },
    resultValidator = ThresholdValidator(threshold = 0.25f / 100f)  // == 0.25%
  )

  // Drop shadows are strangely causing tiny differences in
  // generated screenshots. A zero elevation is used to avoid this.
  // FWIW shadows are already tested by CascadeDropdownMenuTest.
  private val shadowElevation = 0.dp

  @Before
  fun setUp() {
    composeTestRule.activityRule.scenario.onActivity {
      showActionBar.apply(it)
    }
  }

  @Test fun canary(
    @TestParameter orientation: OrientationParam
  ) {
    composeTestRule.activity.requestedOrientation = orientation.orientation
    composeTestRule.runOnUiThread {
      showActionBar.apply(composeTestRule.activity)
    }

    composeTestRule.setContent {
      CascadeMaterialTheme {
        PopupScaffold {
          CascadeDropdownMenu(
            expanded = true,
            onDismissRequest = {},
            shadowElevation = shadowElevation,
          ) {
            DropdownMenuItem(text = { Text("Never gonna") }, onClick = {})
            DropdownMenuItem(text = { Text("Give you up") }, onClick = {})
            DropdownMenuItem(text = { Text("Never gonna") }, onClick = {})
            DropdownMenuItem(text = { Text("Let you down") }, onClick = {})
          }
        }
      }
    }

    composeTestRule.waitForIdle()
    dropshots.assertSnapshot(takeScreenshotWithoutSystemBars())
  }

  @Test fun size_given_to_cascade_should_be_correctly_applied_to_its_content() {
    composeTestRule.setContent {
      CascadeMaterialTheme {
        PopupScaffold {
          CascadeDropdownMenu(
            modifier = Modifier.requiredHeight(100.dp),
            fixedWidth = 200.dp,
            shadowElevation = shadowElevation,
            expanded = true,
            onDismissRequest = {}
          ) {
            Text(
              modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
              text = "This text should get cut-off",
              fontSize = 25.sp,
              lineHeight = 1.33.em,
            )
          }
        }
      }
    }
    dropshots.assertSnapshot(takeScreenshotWithoutSystemBars())
  }

  @Test fun alignment_of_popup_should_match_with_material3(
    @TestParameter alignment: PopupAlignmentParam,
    @TestParameter useNoLimits: UseNoLimitsLayoutFlag,
  ) {
    if (useNoLimits.useNoLimits) {
      composeTestRule.runOnUiThread {
        composeTestRule.activity.window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
      }
    }
    composeTestRule.setContent {
      CascadeMaterialTheme {
        PopupScaffold(alignment.alignment) {
          val popupSize = DpSize(width = 196.dp, height = 100.dp)

          CascadeDropdownMenu(
            modifier = Modifier.requiredHeight(popupSize.height),
            fixedWidth = popupSize.width,
            shadowElevation = shadowElevation,
            expanded = true,
            onDismissRequest = {}
          ) {
            Box(
              Modifier
                .fillMaxWidth()
                .height(popupSize.height)
                .background(Blue.copy(alpha = 0.5f))
            )
          }

          // Material's DropdownMenu is overlayed on top of cascade as
          // a position guide. It should cover exact pixels of cascade,
          // except in some cases where cascade's positioning is better.
          DropdownMenu(
            modifier = Modifier
              .requiredSize(popupSize)
              .background(Red),
            expanded = true,
            onDismissRequest = {}
          ) {
            LocalView.current.alpha = 0.5f
          }
        }
      }
    }
    dropshots.assertSnapshot(takeScreenshotWithoutSystemBars())
  }

  @Test fun content_inside_popup_can_be_updated() {
    var color by mutableStateOf(Yellow)

    composeTestRule.setContent {
      CascadeMaterialTheme {
        PopupScaffold {
          CascadeDropdownMenu(
            expanded = true,
            onDismissRequest = {},
            shadowElevation = shadowElevation,
          ) {
            Box(
              Modifier
                .fillMaxWidth()
                .height(100.dp)
                .background(color),
            )
          }
        }
      }
    }

    listOf(Red, Magenta, Blue, Cyan).forEachIndexed { num, nextColor ->
      color = nextColor
      dropshots.assertSnapshot(
        bitmap = takeScreenshotWithoutSystemBars(),
        name = testName.methodName + "_[$num]"
      )
      composeTestRule.mainClock.advanceTimeByFrame()
    }
  }

  @Test fun zero_sized_anchor() {
    composeTestRule.setContent {
      CascadeMaterialTheme {
        Box(
          Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
        ) {
          // This anchor has a non-zero size, but its internal
          // padding does not leave any space for content.
          Box(
            Modifier
              .size(10.dp)
              .align(Alignment.TopEnd)
              .padding(5.dp)
          ) {
            CascadeDropdownMenu(
              expanded = true,
              onDismissRequest = {},
              shadowElevation = shadowElevation,
            ) {
              DropdownMenuItem(
                text = { Text("Batman Ipsum") },
                onClick = {}
              )
            }
          }
        }
      }
    }
    dropshots.assertSnapshot(takeScreenshotWithoutSystemBars())
  }

  @Test fun clicking_on_a_menu_item_opens_its_sub_menu() {
    composeTestRule.setContent {
      CascadeMaterialTheme {
        PopupScaffold {
          CascadeDropdownMenu(
            expanded = true,
            onDismissRequest = {},
            shadowElevation = shadowElevation,
          ) {
            DropdownMenuItem(
              text = { Text("I just called") },
              children = {
                DropdownMenuItem(
                  text = { Text("to say") },
                  onClick = {},
                )
                DropdownMenuItem(
                  text = { Text("I love you") },
                  onClick = {},
                )
              }
            )
          }
        }
      }
    }

    composeTestRule.onNodeWithText("I just called").performClick()
    composeTestRule.waitForIdle()
    dropshots.assertSnapshot(takeScreenshotWithoutSystemBars())
  }

  @Composable
  private fun PopupScaffold(
    anchorAlignment: Alignment = Alignment.TopStart,
    content: @Composable () -> Unit
  ) {
    Box(
      Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)
    ) {
      Box(
        Modifier
          .align(anchorAlignment)
          .padding(4.dp)
      ) {
        Icon(Icons.Filled.Anchor, contentDescription = null)
        content()
      }
    }
  }

  /** Screenshots the entire device instead of just the active Activity's content. */
  private fun takeScreenshotWithoutSystemBars(): Bitmap {
    val screenshot: Bitmap = takeScreenshot()

    val insets = composeTestRule.activity.window
      .decorView.rootWindowInsets
      .getInsets(WindowInsets.Type.systemBars())

    // The navigation bar's handle cross-fades its color smoothly and can
    // appear slightly different each time. Crop it out to affect screenshots.
    val navigationBarHeightInPx = composeTestRule.activity.resources.run {
      getDimensionPixelSize(getIdentifier("navigation_bar_height", "dimen", "android"))
    }
    return Bitmap.createBitmap(
      screenshot,
      insets.left,
      insets.top,
      screenshot.width - insets.left - insets.right,
      screenshot.height - insets.top - insets.bottom
    )
  }
}

// These enums produce better test names than Alignment#toString().
@Suppress("unused")
enum class PopupAlignmentParam(val alignment: Alignment) {
  TopStart(Alignment.TopStart),
  TopCenter(Alignment.TopCenter),
  TopEnd(Alignment.TopEnd),
  CenterStart(Alignment.CenterStart),
  Center(Alignment.Center),
  CenterEnd(Alignment.CenterEnd),
  BottomStart(Alignment.BottomStart),
  BottomCenter(Alignment.BottomCenter),
  BottomEnd(Alignment.BottomEnd),
}

@Suppress("unused")
enum class ShowActionBarParam(val show: Boolean) {
  ShowActionBar(true),
  HideActionBar(false),
  ;

  fun apply(activity: AppCompatActivity) {
    activity.title = "cascade"
    if (show) {
      activity.supportActionBar!!.show()
    } else {
      activity.supportActionBar!!.hide()
    }
  }
}

@Suppress("unused")
enum class UseNoLimitsLayoutFlag(val useNoLimits: Boolean) {
  NoLimitsEnabled(true),
  NoLimitsDisabled(false)
}

@Suppress("unused")
enum class OrientationParam(val orientation: Int) {
  Portrait(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT),
  Landscape(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE)
}

@Composable
fun CascadeMaterialTheme(content: @Composable () -> Unit) {
  val colors = lightColorScheme(
    primary = Color(0xFFB5D2C3),
    background = Color(0xFFB5D2C3),
    surface = Color.White,
    onSurface = Color(0xFF356859),
    onSurfaceVariant = Color(0xFF356859),
  )
  val shapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp)
  )
  MaterialTheme(
    colorScheme = colors,
    shapes = shapes,
    content = content
  )
}
