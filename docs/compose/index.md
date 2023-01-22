# cascade

`cascade` offers a drop-in replacement for [`DropdownMenu`][drop_down_menu] with support for nested menus, smooth height animations and `0dp` vertical content paddings.

[drop_down_menu]: https://developer.android.com/reference/kotlin/androidx/compose/material3/package-summary#DropdownMenu(kotlin.Boolean,kotlin.Function0,androidx.compose.ui.Modifier,androidx.compose.ui.unit.DpOffset,androidx.compose.ui.window.PopupProperties,kotlin.Function1)

```groovy
implementation "me.saket.cascade:cascade-compose:1.3.0"
```

```kotlin
var isMenuVisible by rememberSaveable { mutableStateOf(false) }

CascadeDropdownMenu(
  expanded = isMenuVisible,
  onDismissRequest = { isMenuVisible = false }
) {
  DropdownMenuItem(
    text = { Text("Horizon") },
    children = {
      DropdownMenuItem(
        text = { Text("Zero Dawn") },
        onClick = { … }
      )
      DropdownMenuItem(
        text = { Text("Forbidden West") },
        onClick = { … }
      )
    }
  )
}
```