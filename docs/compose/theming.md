# Theming

Because `cascade` reuses the same components as `DropdownMenu`, you can follow the
official [material3 documentation](https://m3.material.io/components/menus/specs) for theming menus and expect the
specs to work with `cascade`.

### `CascadeDropdownMenu`

| Design attribute | Theme token                         |
|------------------|-------------------------------------|
| Shape            | `MaterialTheme.shapes.extraSmall`   |
| Background color | `MaterialTheme.colorScheme.surface` |

### `DropdownMenuHeader`

| Design attribute | Theme token                                                                 |
|------------------|-----------------------------------------------------------------------------|
| Content color    | `LocalContentColor` with 60% opacity                                        |
| Text style       | `MaterialTheme.typography.labelLarge` with 90% font size and letter spacing |     

### `DropdownMenuItem`

| Design attribute   | Theme token                           |
|--------------------|---------------------------------------|
| Icon size          | `24dp` minimum                        |    
| Colors             | [`MenuItemColors`][menu_item_colors]  |
| Text style         | `MaterialTheme.typography.labelLarge` |     

[drop_down_menu_item]: https://developer.android.com/reference/kotlin/androidx/compose/material3/package-summary#DropdownMenuItem(kotlin.Function0,kotlin.Function0,androidx.compose.ui.Modifier,kotlin.Function0,kotlin.Function0,kotlin.Boolean,androidx.compose.material3.MenuItemColors,androidx.compose.foundation.layout.PaddingValues,androidx.compose.foundation.interaction.MutableInteractionSource)

[menu_item_colors]: https://cs.android.com/androidx/platform/frameworks/support/+/androidx-main:compose/material3/material3/src/commonMain/kotlin/androidx/compose/material3/Menu.kt?q=file:androidx%2Fcompose%2Fmaterial3%2FMenu.kt%20class:androidx.compose.material3.MenuItemColors
