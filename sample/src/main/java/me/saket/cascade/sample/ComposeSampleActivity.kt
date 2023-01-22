package me.saket.cascade.sample

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.twotone.Check
import androidx.compose.material.icons.twotone.Close
import androidx.compose.material.icons.twotone.ContentCopy
import androidx.compose.material.icons.twotone.Delete
import androidx.compose.material.icons.twotone.Language
import androidx.compose.material.icons.twotone.Share
import androidx.compose.material3.Divider
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.core.view.WindowCompat
import me.saket.cascade.CascadeDropdownMenu
import me.saket.cascade.rememberCascadeState

class ComposeSampleActivity : AppCompatActivity() {

  @OptIn(ExperimentalMaterial3Api::class)
  override fun onCreate(savedInstanceState: Bundle?) {
    WindowCompat.setDecorFitsSystemWindows(window, false)
    super.onCreate(savedInstanceState)

    setContent {
      CascadeMaterialTheme {
        Box(
          Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
        ) {
          var isMenuShown by rememberSaveable { mutableStateOf(true) }

          TopAppBar(
            title = { Text(stringResource(R.string.app_name)) },
            colors = TopAppBarDefaults.smallTopAppBarColors(
              containerColor = Color.Transparent
            ),
            actions = {
              IconButton(onClick = { isMenuShown = true }) {
                Menu(
                  expanded = isMenuShown,
                  onDismiss = { isMenuShown = false }
                )
                Icon(Icons.Rounded.MoreVert, contentDescription = "More options")
              }
            }
          )
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
      DropdownMenuItem(
        text = { Text("Share") },
        leadingIcon = { Icon(Icons.TwoTone.Share, contentDescription = null) },
        children = {
          DropdownMenuItem(
            text = { Text("To clipboard") },
            children = { FileMenuItems() }
          )
          DropdownMenuItem(
            text = { Text("As a file") },
            children = { FileMenuItems() }
          )
        }
      )
      DropdownMenuItem(
        text = { Text("Remove") },
        leadingIcon = { Icon(Icons.TwoTone.Delete, contentDescription = null) },
        childrenHeader = {
          DropdownMenuHeader {
            Text(
              text = "Are you sure?",
              modifier = Modifier.fillMaxWidth()
            )
          }
        },
        children = {
          DropdownMenuItem(
            text = { Text("Yep") },
            leadingIcon = { Icon(Icons.TwoTone.Check, contentDescription = null) },
            onClick = { onDismiss() }
          )
          DropdownMenuItem(
            text = { Text("Go back") },
            leadingIcon = { Icon(Icons.TwoTone.Close, contentDescription = null) },
            onClick = {
              state.navigateBack()
            }
          )
        },
      )
      DropdownMenuItem(
        text = { Text("Cash App") },
        leadingIcon = { Icon(painterResource(R.drawable.ic_cash_app_24), contentDescription = null) },
        children = {
          DropdownMenuItem(
            text = { Text("molecule") },
            onClick = { openUrl("https://github.com/cashapp/molecule") }
          )
          DropdownMenuItem(
            text = { Text("paparazzi") },
            onClick = { openUrl("https://github.com/cashapp/paparazzi") }
          )
          DropdownMenuItem(
            text = { Text("SQLDelight") },
            onClick = { openUrl("https://github.com/cashapp/sqldelight") }
          )
        },
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
      text = { Text("Microsoft Word") },
      onClick = {}
    )
    DropdownMenuItem(
      text = { Text("Microsoft PowerPoint") },
      onClick = {}
    )
    DropdownMenuItem(
      text = { Text("Microsoft Excel") },
      onClick = {}
    )
  }
}
