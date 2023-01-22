# cascade

`cascade` offers a one-word replacement for material3's [DropdownMenu](https://developer.android.com/reference/kotlin/androidx/compose/material3/package-summary#DropdownMenu(kotlin.Boolean,kotlin.Function0,androidx.compose.ui.Modifier,androidx.compose.ui.unit.DpOffset,androidx.compose.ui.window.PopupProperties,kotlin.Function1)).

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

`cascade` offers the same look & feel as `DropdownMenu`, with the differences of a nicer entry/exit animation and `0dp` vertical padding around content.
