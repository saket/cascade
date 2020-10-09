package me.saket.cascade.sample

import android.annotation.SuppressLint
import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.SubMenu
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.Toolbar
import androidx.core.view.iterator
import androidx.core.view.postDelayed
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetView
import me.saket.cascade.CascadePopupMenu

@SuppressLint("ClickableViewAccessibility")
class MainActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    val toolbar = findViewById<Toolbar>(R.id.toolbar)
    toolbar.inflateMenu(R.menu.toolbar)

    val menuButton = toolbar.findViewById<View>(R.id.overflow_menu)
    menuButton.setOnClickListener {
      showCascadeMenu(anchor = menuButton)
    }

    TapTargetView.showFor(this,
      TapTarget.forToolbarMenuItem(toolbar, R.id.overflow_menu, "Tap to see Cascade in action")
        .transparentTarget(true)
        .outerCircleColor(R.color.colorControlNormal)
        .titleTextColor(R.color.windowBackground),
      object : TapTargetView.Listener() {
        override fun onTargetClick(view: TapTargetView) {
          super.onTargetClick(view)
          view.postDelayed(200) { menuButton.performClick() }
        }
      }
    )
  }

  private fun showCascadeMenu(anchor: View) {
    val popupMenu = CascadePopupMenu(this, anchor)
    popupMenu.menu.apply {
      add("About").setIcon(R.drawable.ic_language_24)
      add("Copy").setIcon(R.drawable.ic_file_copy_24)
      addSubMenu("Share").also {
        val addShareTargets = { sub: SubMenu ->
          sub.add("PDF")
          sub.add("EPUB")
          sub.add("Image")
          sub.add("Web page")
          sub.add("Markdown")
          sub.add("Plain text")
          sub.add("Microsoft word")
        }
        it.setIcon(R.drawable.ic_share_24)
        addShareTargets(it.addSubMenu("To clipboard"))
        addShareTargets(it.addSubMenu("As a file"))
      }
      addSubMenu("Remove").also {
        it.setIcon(R.drawable.ic_delete_sweep_24)
        it.setHeaderTitle("Are you sure?")
        it.add("Yep").setIcon(R.drawable.ic_check_24)
        it.add("Go back").setIcon(R.drawable.ic_close_24)
      }
      addSubMenu("Cash App").also {
        it.setIcon(cashAppIcon())
        it.add("contour").intent = intent("https://github.com/cashapp/contour")
        it.add("duktape").intent = intent("https://github.com/cashapp/duktape-android")
        it.add("misk").intent = intent("https://github.com/cashapp/misk")
        it.add("paparazzi").intent = intent("https://github.com/cashapp/paparazzi")
        it.add("sqldelight").intent = intent("https://github.com/cashapp/SQLDelight")
        it.add("turbine").intent = intent("https://github.com/cashapp/turbine")
      }

      allItems().filter { it.intent == null }.forEach {
        it.setOnMenuItemClickListener {
          popupMenu.navigateBack()
          true
        }
      }

      popupMenu.show()
    }
  }

  private fun cashAppIcon() =
    AppCompatResources.getDrawable(this, R.drawable.ic_cash_app_24)!!.also {
      it.mutate()
      it.setTint(getColor(R.color.colorControlNormal))
    }

  private fun intent(url: String) =
    Intent(ACTION_VIEW, Uri.parse(url))
}

@OptIn(ExperimentalStdlibApi::class)
private fun Menu.allItems(): List<MenuItem> {
  val menu = this
  return buildList {
    for (item in menu) {
      add(item)
      if (item.hasSubMenu()) {
        addAll(item.subMenu.allItems())
      }
    }
  }
}
