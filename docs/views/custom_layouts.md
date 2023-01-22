# Custom layouts

`cascade` was originally inspired by Google Drive's that uses a variety of complex controls. For apps that want to create something similar, a batteries-included [CascadePopupWindow](https://github.com/saket/cascade/blob/trunk/cascade/src/main/java/me/saket/cascade/CascadePopupWindow.kt) is provided for use with custom layouts.

```kotlin
val customMenuView: View = …

val popup = CascadePopupWindow(context)
popup.contentView.addView(customMenuView)  // Also see contentView.goBack().
popup.show(anchor)
```

<blockquote class="twitter-tweet" data-dnt="true" data-theme="dark"><p lang="en" dir="ltr">I really like Google Drive&#39;s popup menu that smoothly animates between sub-menus. Is there any existing library that recreates this? <a href="https://t.co/bnalL56pcR">pic.twitter.com/bnalL56pcR</a></p>&mdash; Saket Narayan (@saketme) <a href="https://twitter.com/saketme/status/1313130386743066627?ref_src=twsrc%5Etfw">October 5, 2020</a></blockquote> <script async src="https://platform.twitter.com/widgets.js" charset="utf-8"></script>
