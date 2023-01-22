# Navigation

For sub-menus, `cascade` will automatically navigate back when their title is clicked. For manual navigation, `CascadeState#navigateBack()` can be used.

```kotlin hl_lines="1-3 18"
val state = rememberCascadeState()
CascadeDropdownMenu(
  state = state, 
  …
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

`childrenHeader` is an optional parameter. If you don't provide one, `cascade` will automatically use the `text` composable (which is "Remove" in the above example).
