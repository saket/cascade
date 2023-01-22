# Navigation

For sub-menus, `cascade` will automatically navigate back when their title is clicked. For manual navigation, `cascade` provides a back navigator:

```kotlin hl_lines="7"
val popup = CascadePopupMenu(context, anchor)
  
popup.menu.addSubMenu("Remove").also {
  it.setHeaderTitle("Are you sure?")
  it.add("Burn them all")
  it.add("Take me back").setOnMenuItemClickListener {
    popup.navigateBack()
  }
}
```

You can also provide your own back navigator. This injection is useful if a navigator is needed before an instance of `cascade` can be created.

```kotlin
val backNavigator = CascadeBackNavigator()
CascadePopupMenu(context, anchor, backNavigator)
```
