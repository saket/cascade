package me.saket.cascade

import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun Sample() {
  CascadeDropdownMenu(expanded = true, onDismissRequest = { /*TODO*/ }) {
    DropdownMenuItem(
      text = { Text("About") },
      children = {
        DropdownMenuItem(
          text = { Text("Child 1") },
          onClick = { /*TODO*/ }
        )
        DropdownMenuItem(
          text = { Text("Child 2") },
          children = {
            DropdownMenuItem(
              text = { Text("Nested child 1") },
              onClick = { /*TODO*/ }
            )
            DropdownMenuItem(
              text = { Text("Nested child 2") },
              onClick = { /*TODO*/ }
            )
          }
        )
      }
    )
    DropdownMenuItem(
      text = { Text("Copy") },
      onClick = { /*TODO*/ }
    )
  }
}
