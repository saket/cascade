# cascade

![demo](https://github.com/saket/cascade/raw/trunk/demo.gif)

`cascade` builds nested popup menus with smooth height animations. It is designed to be a *drop-in* replacement for both [PopupMenu](https://developer.android.com/reference/androidx/appcompat/widget/PopupMenu) and [DropdownMenu](https://developer.android.com/reference/kotlin/androidx/compose/material3/package-summary#DropdownMenu(kotlin.Boolean,kotlin.Function0,androidx.compose.ui.Modifier,androidx.compose.ui.unit.DpOffset,androidx.compose.ui.window.PopupProperties,kotlin.Function1)), so using it in your project is beautifully only a word away. Try out the [sample app](https://github.com/saket/cascade/releases/download/1.3.0/cascade_v1.3.0_sample.apk) to see it in action.

```groovy
implementation "me.saket.cascade:cascade:1.3.0"
implementation "me.saket.cascade:cascade-compose:1.3.0"
```

=== "Compose UI"
    ```diff
    - DropdownMenu(
    + CascadeDropdownMenu(
      expanded = expanded,
      onDismissRequest = { expanded = false }
    ) { 
      DropdownMenuItem(…)
      DropdownMenuItem(…)
    }
    ```
=== "Views"
    ```diff
    - val popup = PopupMenu(context, anchor)
    + val popup = CascadePopupMenu(context, anchor)
      popup.inflate(R.menu.nicolas_cage_movies)
      popup.show()
    ```
