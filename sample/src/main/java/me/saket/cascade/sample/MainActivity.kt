package me.saket.cascade.sample

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.twotone.ContentCopy
import androidx.compose.material.icons.twotone.DeleteSweep
import androidx.compose.material.icons.twotone.Language
import androidx.compose.material.icons.twotone.Share
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import me.saket.cascade.CascadeDropdownMenu

class MainActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    setContent {
      CascadeMaterialTheme {
        Box(
          Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
        ) {
          val isMenuShown = rememberSaveable { mutableStateOf(true) }
          Box(
            Modifier
              .padding(4.dp)
              .size(1.dp)
              .align(Alignment.TopEnd)
          ) {
            Menu(
              isShown = isMenuShown
            )
          }

          SmallTopAppBar(
            title = { Text(stringResource(R.string.app_name)) },
            colors = TopAppBarDefaults.smallTopAppBarColors(
              containerColor = Color.Transparent
            ),
            actions = {
              IconButton(onClick = { isMenuShown.value = true }) {
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
    isShown: MutableState<Boolean>,
    modifier: Modifier = Modifier
  ) {
    CascadeDropdownMenu(
      modifier = modifier,
      expanded = isShown.value,
      onDismissRequest = { isShown.value = false }
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
            children = {
              DropdownMenuItem(
                text = { Text("PDF") },
                onClick = {}
              )
              DropdownMenuItem(
                text = { Text("HTML") },
                onClick = {}
              )
            }
          )
          DropdownMenuItem(
            text = { Text("As a file") },
            children = {
              DropdownMenuItem(
                text = { Text("PDF") },
                onClick = {}
              )
              DropdownMenuItem(
                text = { Text("HTML") },
                onClick = {}
              )
            }
          )
        }
      )
      DropdownMenuItem(
        text = { Text("Remove") },
        leadingIcon = { Icon(Icons.TwoTone.DeleteSweep, contentDescription = null) },
        onClick = {},
      )
      DropdownMenuItem(
        text = { Text("Cash App") },
        leadingIcon = { Icon(painterResource(R.drawable.ic_cash_app_24), contentDescription = null) },
        onClick = {},
      )
    }
  }
}
