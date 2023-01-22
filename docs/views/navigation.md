# Navigation

For sub-menus, `cascade` will automatically navigate to the parent menu when the title is clicked. For manual navigation, `CascadePopupMenu#navigateBack()` or [CascadeBackNavigator](https://github.com/saket/cascade/blob/trunk/cascade/src/main/java/me/saket/cascade/CascadeBackNavigator.kt) can be used.

```kotlin
popup.menu.addSubMenu("Remove").also {
  it.setHeaderTitle("Are you sure?")
  it.add("Burn them all")
  it.add("Take me back").setOnMenuItemClickListener {
    popup.navigateBack()
  }
}
```
