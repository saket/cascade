# cascade

![demo](demo.gif)

`cascade` builds nested popup menus with smooth height animations. It is designed to be a *drop-in* replacement for [PopupMenu](https://developer.android.com/reference/androidx/appcompat/widget/PopupMenu) so using it in your project is beautifully only a word away. Try out the [sample app](https://github.com/saket/cascade/releases/download/1.0.0/cascade_v1.0.0_sample.apk) to see it in action.

```groovy
implementation "me.saket.cascade:cascade:1.2.0"
```

```diff
- val popup = PopupMenu(context, anchor)
+ val popup = CascadePopupMenu(context, anchor)
  popup.inflate(R.menu.nicolas_cage_movies)
  popup.show()
```

#### Use as Toolbar's overflow menu

```kotlin
toolbar.overrideOverflowMenu { context, anchor ->
  CascadePopupMenu(context, anchor)
}

// The lambda can be collapsed into a reference
// if you're only using the two-param constructor.
toolbar.overrideOverflowMenu(with = ::CascadePopupMenu)
```

### Customization

`cascade` is great for apps that prefer applying dynamic themes at runtime, which `PopupMenu` makes it extremely hard to do so. By providing a `CascadePopupMenu.Styler` object, you can adjust colors, spacings and text styles from Kotlin ([example](https://github.com/saket/cascade/blob/038bbf054657c243ae62f2d780e5488ed54fcafb/sample/src/main/java/me/saket/cascade/sample/MainActivity.kt#L93-L111)).

```kotlin
CascadePopupMenu(context, anchor, styler = CascadePopupMenu.Styler(...))
```

By default, `cascade` will pick up values from your theme in the same way as `PopupMenu` would.

```xml
<style name="AppTheme">
  <item name="popupMenuStyle">@style/PopupMenuStyle</item>
  <item name="colorControlNormal">@color/menu_icon_color</item>
  <item name="android:textColorPrimary">@color/menu_item_text_color</item>
  <item name="android:textColorSecondary">@color/menu_title_text_color</item>
</style>

<style name="PopupMenuStyle" parent="@style/Widget.AppCompat.PopupMenu">
  <item name="android:popupBackground">...</item>
  <item name="android:popupElevation">...</item>
</style>
```

### Navigation

For sub-menus, `cascade` will automatically navigate to the parent menu when the title is clicked. For manual navigation, `CascadePopupMenu#navigateBack()` can be used.

```kotlin
popup.menu.addSubMenu("Remove").also {
  it.setHeaderTitle("Are you sure?")
  it.add("Burn them all")
  it.add("Take me back").setOnMenuItemClickListener {
    popup.navigateBack()
    true
  }
}
```

### Custom layouts

`cascade` was originally inspired by Google Drive's [menu](https://twitter.com/saketme/status/1313130386743066627) that uses a variety of complex controls. For apps that want to create something similar, a batteries-included [CascadePopupWindow](https://github.com/saket/cascade/blob/trunk/cascade/src/main/java/me/saket/cascade/CascadePopupWindow.kt) is provided for use with custom layouts. 

```kotlin
val popup = CascadePopupWindow(context)
popup.contentView.addView(CustomMenuView(context))  // Also see contentView.goBack().
popup.show(anchor)
```

## License

```
Copyright 2020 Saket Narayan.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
