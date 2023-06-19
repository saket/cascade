# cascade

![demo](https://github.com/saket/cascade/raw/trunk/demo.gif)

`cascade` offers a drop-in replacement for [PopupMenu](https://developer.android.com/reference/androidx/appcompat/widget/PopupMenu). For guidance on creating & nesting menus, the [official documentation](https://developer.android.com/develop/ui/views/components/menus) can be followed while replacing any usages of `PopupMenu` with `CascadePopupMenu`.

```groovy
implementation "me.saket.cascade:cascade:2.2.0"
```

```diff
- val popup = PopupMenu(context, anchor)
+ val popup = CascadePopupMenu(context, anchor)
  popup.inflate(R.menu.nicolas_cage_movies)
  popup.show()
```

#### Consistency with Toolbar's overflow menu

`Toolbar` uses `PopupMenu` for showing overflow menu without offering any way to change this. If you're replacing all `PopupMenu` usages in your project with `cascade`, there are a few ways you could achieve consistency by forcing `Toolbar` to use `cascade`:

- The safest way is to extract out your toolbar menu items with `app:showAsAction="ifRoom"` into their own menu that is shown using `cascade` manually.

- Alternatively, `cascade` offers an override using reflection but is currently [incompatible with Proguard](https://github.com/saket/cascade/issues/31):

```kotlin
toolbar.overrideAllPopupMenus { context, anchor ->
  CascadePopupMenu(context, anchor)
}

// The lambda can be collapsed into a reference
// if you're only using the two-param constructor.
toolbar.overrideAllPopupMenus(with = ::CascadePopupMenu)
```
