@file:Suppress("TestFunctionName", "JUnitMalformedDeclaration")

package me.saket.cascade

import android.graphics.Bitmap
import android.view.WindowManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.surfaceColorAtElevation
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
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.test.core.app.takeScreenshot
import com.dropbox.dropshots.Dropshots
import com.google.testing.junit.testparameterinjector.TestParameter
import com.google.testing.junit.testparameterinjector.TestParameterInjector
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestName
import org.junit.runner.RunWith

@RunWith(TestParameterInjector::class)
internal class CascadePopupTest {
  @get:Rule val composeTestRule = createAndroidComposeRule<TestActivity>()
  @get:Rule val testName = TestName()
  @get:Rule val dropshots = Dropshots()

  @Test fun single_window_screenshot() {
    composeTestRule.setContent {
      CascadeMaterialTheme {
        Surface(Modifier.fillMaxSize()) {
          Text(
            modifier = Modifier.padding(16.dp),
            text = "Test",
            style = MaterialTheme.typography.titleLarge,
          )
        }
      }
    }
    dropshots.assertSnapshot(composeTestRule.activity)
  }

  @Test fun canary() {
    composeTestRule.setContent {
      MaterialTheme {
        PopupScaffold {
          CascadeDropdownMenu(
            expanded = true,
            onDismissRequest = {}
          ) {
            DropdownMenuItem(text = { Text("Never gonna") }, onClick = {})
            DropdownMenuItem(text = { Text("Give you up") }, onClick = {})
            DropdownMenuItem(text = { Text("Never gonna") }, onClick = {})
            DropdownMenuItem(text = { Text("Let you down") }, onClick = {})
          }
        }
      }
    }
    dropshots.assertDeviceSnapshot()
  }

  @Test fun size_given_to_cascade_should_be_correctly_applied_to_its_content() {
    composeTestRule.setContent {
      CascadeMaterialTheme {
        PopupScaffold {
          CascadeDropdownMenu(
            modifier = Modifier.requiredHeight(100.dp),
            fixedWidth = 200.dp,
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
    dropshots.assertDeviceSnapshot()
  }

  @Test fun alignment_of_popup_should_match_with_material3(
    @TestParameter alignment: PopupAlignment,
    @TestParameter useNoLimitsFlag: Boolean,
  ) {
    if (useNoLimitsFlag) {
      composeTestRule.activity.run {
        runOnUiThread {
          window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        }
      }
    }
    composeTestRule.setContent {
      CascadeMaterialTheme {
        PopupScaffold(alignment.alignment) {
          val popupSize = DpSize(width = 196.dp, height = 100.dp)

          CascadeDropdownMenu(
            modifier = Modifier.requiredHeight(popupSize.height),
            fixedWidth = popupSize.width,
            expanded = true,
            onDismissRequest = {}
          ) {
            Box(
              Modifier
                .fillMaxWidth()
                .height(popupSize.height)
                .background(Blue)
            )
          }

          // Material's DropdownMenu is overlayed on top of cascade as
          // a position guide. It should cover exact pixels of cascade.
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
    dropshots.assertDeviceSnapshot()
  }

  @Test fun content_inside_popup_can_be_updated() {
    var color by mutableStateOf(Yellow)

    composeTestRule.setContent {
      CascadeMaterialTheme {
        PopupScaffold {
          CascadeDropdownMenu(
            expanded = true,
            onDismissRequest = {}
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
      dropshots.assertDeviceSnapshot(nameSuffix = "[$num]")
      composeTestRule.mainClock.advanceTimeByFrame()
    }
  }

  @Test fun zero_sized_anchor() {
    composeTestRule.setContent {
      CascadeMaterialTheme {
        Box(
          Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceColorAtElevation(10.dp))
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
              onDismissRequest = {}
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
    dropshots.assertDeviceSnapshot()
  }

  @Composable
  private fun PopupScaffold(
    align: Alignment = Alignment.TopStart,
    content: @Composable () -> Unit
  ) {
    Box(
      Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.surfaceColorAtElevation(10.dp))
        .wrapContentSize(align)
        .padding(4.dp)
        .size(1.dp)
    ) {
      content()
    }
  }

  private fun Dropshots.assertDeviceSnapshot(nameSuffix: String? = null) {
    println("-------------------------------------")
    println("${testName}()")

    // This screenshots the entire device instead of just the active Activity's content.
    val screenshot: Bitmap = takeScreenshot()
    println("Screenshot taken. Size = ${screenshot.width}x${screenshot.height}")

    // The navigation bar's handle cross-fades its color smoothly and can
    // appear slightly different each time. Crop it out to affect screenshots.
    val navigationBarHeightInPx = 66
    val screenshotWithoutNavBars: Bitmap = Bitmap.createBitmap(
      /* source = */ screenshot,
      /* x = */ 0,
      /* y = */ 0,
      /* width = */ screenshot.width,
      /* height = */ screenshot.height - navigationBarHeightInPx
    )
    println("Screenshot cropped to size = ${screenshotWithoutNavBars.width}x${screenshotWithoutNavBars.height}")

    assertSnapshot(
      bitmap = screenshotWithoutNavBars,
      name = testName.methodName + (if (nameSuffix != null) "_$nameSuffix" else "")
    )
  }
}

// These enums produce better test names than Alignment#toString().
@Suppress("unused")
enum class PopupAlignment(val alignment: Alignment) {
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

@Composable
fun CascadeMaterialTheme(content: @Composable () -> Unit) {
  val colors = lightColorScheme(
    primary = Color(0xFFB5D2C3),
    background = Color(0xFFB5D2C3),
    surface = Color(0xFFE5F0EB),
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
