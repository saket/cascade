# Theming

`cascade` is great for apps that prefer applying dynamic themes at runtime, which `PopupMenu` makes it extremely hard to do so. By providing a `CascadePopupMenu.Styler` object, you can adjust colors, spacings and text styles from Kotlin ([example](https://github.com/saket/cascade/blob/ea668552999be0d3fd235e9feefa782ac92b13d4/sample/src/main/java/me/saket/cascade/sample/MainActivity.kt#L91:L110)).

```kotlin
CascadePopupMenu(
  styler = CascadePopupMenu.Styler(…)
)
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
