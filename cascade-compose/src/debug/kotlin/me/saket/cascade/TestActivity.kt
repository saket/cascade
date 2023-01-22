package me.saket.cascade

import android.os.Build
import android.os.Bundle
import android.view.WindowInsets
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat

/**
 * Used by [me.saket.cascade.CascadePopupTest].
 *
 * Can be moved to test sources when https://issuetracker.google.com/issues/127986458 is fixed.
 * In the meantime, this must be exposed to the main source-set to be picked up in tests.
 */
@RequiresApi(Build.VERSION_CODES.R)
internal class TestActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    window.setDecorFitsSystemWindows(false)
    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
    window.statusBarColor = Color.Transparent.toArgb()
    window.navigationBarColor = Color.Transparent.toArgb()

    // Hide the status bar because its clock time will change in screenshots.
    window.decorView.windowInsetsController!!.hide(WindowInsets.Type.statusBars())

    super.onCreate(savedInstanceState)
  }
}
