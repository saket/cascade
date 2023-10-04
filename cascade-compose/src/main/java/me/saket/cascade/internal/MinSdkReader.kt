package me.saket.cascade.internal

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

internal fun interface MinSdkReader {
  @Composable fun minSdk(): Int
}

internal object RealMinSdkReader : MinSdkReader {
  @Composable override fun minSdk(): Int {
    return if (Build.VERSION.SDK_INT >= 24) {
      val context = LocalContext.current
      remember {
        context.packageManager.getPackageInfo(context.packageName, 0).applicationInfo.minSdkVersion
      }
    } else {
      23  // This assumes that the library's minSdk is 23.
    }
  }
}
