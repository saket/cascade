package me.saket.cascade.sample

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.twotone.ContentCopy
import androidx.compose.material.icons.twotone.Language
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import me.saket.cascade.CascadeDropdownMenu
import me.saket.cascade.rememberCascadeState

class ComposeSampleActivity : AppCompatActivity() {

  @OptIn(ExperimentalMaterial3Api::class)
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    WindowCompat.setDecorFitsSystemWindows(window, false)

    setContent {
      CascadeMaterialTheme {
        Box(
          Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .border(1.dp, Color.Magenta)
        ) {
          var isMenuShown by rememberSaveable { mutableStateOf(true) }

          TopAppBar(
            title = { Text(stringResource(R.string.app_name)) },
            //title = { Text("Stonks app") },
            colors = TopAppBarDefaults.smallTopAppBarColors(
              containerColor = Color.Transparent
            ),
            actions = {
              IconButton(
                modifier = Modifier.border(1.dp, Color.Green),
                onClick = { isMenuShown = true }
              ) {
                //                /*Material*/Menu(
                //                  expanded = isMenuShown,
                //                  onDismiss = { isMenuShown = false }
                //                )
                Icon(
                  modifier = Modifier.onGloballyPositioned {
                    println("Icon's position = ${it.positionInWindow()}")
                  },
                  imageVector = Icons.Rounded.MoreVert, contentDescription = "More options"
                )
              }
            }
          )

          // Position menu to the top-end so that it overlaps the overflow menu button.
          Box(
            Modifier
              .padding(end = 12.dp, top = 40.dp)
              .background(Color.Red, CircleShape)
              .size(10.dp)
              .align(Alignment.TopEnd)
          ) {
            /*Material*/Menu(
            expanded = isMenuShown,
            onDismiss = { isMenuShown = false }
          )
          }
        }
      }
    }
  }

  @Composable
  private fun Menu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
  ) {
    val state = rememberCascadeState()
    CascadeDropdownMenu(
      state = state,
      modifier = modifier,
      expanded = expanded,
      onDismissRequest = onDismiss
    ) {
      DropdownMenuItem(
        text = { Text("About") },
        leadingIcon = { Icon(Icons.TwoTone.Language, contentDescription = null) },
        onClick = {},
      )
      DropdownMenuItem(
        text = { Text("Copy") },
        leadingIcon = { Icon(Icons.TwoTone.ContentCopy, contentDescription = null) },
        onClick = {},
      )
      //      DropdownMenuItem(
      //        text = { Text("Share") },
      //        leadingIcon = { Icon(Icons.TwoTone.Share, contentDescription = null) },
      //        children = {
      //          DropdownMenuItem(
      //            text = { Text("To clipboard") },
      //            children = { FileMenuItems() }
      //          )
      //          DropdownMenuItem(
      //            text = { Text("As a file") },
      //            children = { FileMenuItems() }
      //          )
      //        }
      //      )
      //      DropdownMenuItem(
      //        text = { Text("Remove") },
      //        leadingIcon = { Icon(Icons.TwoTone.Delete, contentDescription = null) },
      //        childrenHeader = {
      //          DropdownMenuHeader {
      //            Text(
      //              text = "Are you sure?",
      //              modifier = Modifier.fillMaxWidth()
      //            )
      //          }
      //        },
      //        children = {
      //          DropdownMenuItem(
      //            text = { Text("Yep") },
      //            leadingIcon = { Icon(Icons.TwoTone.Check, contentDescription = null) },
      //            onClick = { onDismiss() }
      //          )
      //          DropdownMenuItem(
      //            text = { Text("Go back") },
      //            leadingIcon = { Icon(Icons.TwoTone.Close, contentDescription = null) },
      //            onClick = {
      //              state.navigateBack()
      //            }
      //          )
      //        },
      //      )
      //      DropdownMenuItem(
      //        text = { Text("Cash App") },
      //        leadingIcon = { Icon(painterResource(R.drawable.ic_cash_app_24), contentDescription = null) },
      //        children = {
      //          DropdownMenuItem(
      //            text = { Text("molecule") },
      //            onClick = { openUrl("https://github.com/cashapp/molecule") }
      //          )
      //          DropdownMenuItem(
      //            text = { Text("paparazzi") },
      //            onClick = { openUrl("https://github.com/cashapp/paparazzi") }
      //          )
      //          DropdownMenuItem(
      //            text = { Text("SQLDelight") },
      //            onClick = { openUrl("https://github.com/cashapp/sqldelight") }
      //          )
      //        },
      //      )
    }
  }

  @Composable
  private fun MaterialMenu(
    expanded: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
  ) {
    DropdownMenu(
      modifier = modifier,
      expanded = expanded,
      onDismissRequest = onDismiss
    ) {
      DropdownMenuItem(
        text = { Text("About") },
        leadingIcon = { Icon(Icons.TwoTone.Language, contentDescription = null) },
        onClick = {},
      )
      DropdownMenuItem(
        text = { Text("Copy") },
        leadingIcon = { Icon(Icons.TwoTone.ContentCopy, contentDescription = null) },
        onClick = {},
      )
    }
  }

  private fun openUrl(url: String) {
    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
  }

  @Composable
  private fun FileMenuItems() {
    DropdownMenuItem(
      text = { Text("PDF") },
      onClick = {}
    )
    DropdownMenuItem(
      text = { Text("EPUB") },
      onClick = {}
    )
    DropdownMenuItem(
      text = { Text("Image") },
      onClick = {}
    )
    DropdownMenuItem(
      text = { Text("Web page") },
      onClick = {}
    )
    DropdownMenuItem(
      text = { Text("Markdown") },
      onClick = {}
    )
    Divider(
      color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
    )
    DropdownMenuItem(
      text = { Text("Plain text") },
      onClick = {}
    )
    DropdownMenuItem(
      text = { Text("Microsoft word") },
      onClick = {}
    )
  }
}
