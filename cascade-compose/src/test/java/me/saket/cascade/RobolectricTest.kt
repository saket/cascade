@file:Suppress("TestFunctionName")

package me.saket.cascade

import android.graphics.Bitmap
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Anchor
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.test.platform.app.InstrumentationRegistry
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.RoborazziRule
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.annotation.GraphicsMode
import java.io.File
import org.robolectric.annotation.Config as RobolectricConfig

@RunWith(ParameterizedRobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@RobolectricConfig(
  qualifiers = RobolectricDeviceQualifiers.Pixel7,
  manifest = RobolectricConfig.NONE,
  sdk = [33],
)
class RobolectricTest(private val anchorAlignment: AlignmentParam) {

  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @get:Rule val roborazziRule = RoborazziRule(
    options = RoborazziRule.Options(
      outputDirectoryPath = "src/test/snapshots",
      outputFileProvider = { description, outputDirectory, fileExtension ->
        File(
          outputDirectory,
          "${description.testClass.name}.${description.methodName}.$fileExtension"
        )
      }
    ),
  )

  @Before fun setUp() {
    composeTestRule.activityRule.scenario.onActivity {
      it.actionBar!!.title = "cascade"
    }
  }

  @Test fun various_alignments() {
    composeTestRule.setContent {
      CascadeMaterialTheme {
        PopupScaffold(
          anchorAlignment = anchorAlignment.alignment,
        ) {
          var isPopupVisible by remember { mutableStateOf(false) }
          AnchorIconButton {
            isPopupVisible = true
          }

          CascadeDropdownMenu(
            expanded = isPopupVisible,
            onDismissRequest = { isPopupVisible = false },
          ) {
            Box(Modifier.height(100.dp))
          }
        }
      }
    }

    composeTestRule.onNodeWithTag("anchor").performClick()
    composeTestRule.runOnIdle {
      captureDeviceScreenshot().captureRoboImage()
    }
  }

  @Composable
  private fun AnchorIconButton(onClick: () -> Unit) {
    Icon(
      modifier = Modifier
        .testTag("anchor")
        .clickable(onClick = onClick),
      imageVector = Icons.Filled.Anchor,
      contentDescription = null,
    )
  }

  @Composable
  private fun PopupScaffold(
    anchorAlignment: Alignment,
    content: @Composable BoxScope.() -> Unit,
  ) {
    Box(
      Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)
    ) {
      Box(
        Modifier
          .align(anchorAlignment)
          .padding(4.dp),
        content = content,
      )
    }
  }

  companion object {
    @JvmStatic
    @ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
    fun params(): List<Array<Any>> = AlignmentParam.entries.map { arrayOf(it) }
  }
}

private fun captureDeviceScreenshot(): Bitmap {
  return InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot()
}

@Suppress("unused")
enum class AlignmentParam(val alignment: Alignment) {
  TopCenter(Alignment.TopCenter),
  BottomCenter(Alignment.BottomCenter),
}
