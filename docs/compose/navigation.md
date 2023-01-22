# Navigation

For sub-menus, `cascade` will automatically navigate to the parent menu when the title is clicked. For manual navigation, `CascadeState#navigateBack()` can be used.

```kotlin hl_lines="1 4 20"
val state = rememberCascadeState()

CascadeDropdownMenu(
  state = state,
  expanded = true,
  onDismissRequest = { … }
) {
  DropdownMenuItem(
    text = { Text("Remove") },
    childrenHeader = {
      DropdownMenuHeader { Text("Are you sure?") }
    },
    children = {
      DropdownMenuItem(
        text = { Text("Burn them all") },
        onClick = { … }
      )
      DropdownMenuItem(
        text = { Text("Take me back") },
        onClick = { state.navigateBack() }
      )
    },
  )
}
```
