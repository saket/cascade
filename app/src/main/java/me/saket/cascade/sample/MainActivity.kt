package me.saket.cascade.sample

import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.Toolbar
import me.saket.cascade.CascadePopupMenu

class MainActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    val toolbar = findViewById<Toolbar>(R.id.toolbar)
    toolbar.inflateMenu(R.menu.toolbar)
    toolbar.findViewById<View>(R.id.overflow_menu).setOnClickListener {
      showCascadeMenu(anchor = it)
    }
  }

  private fun showCascadeMenu(anchor: View) {
    val popupMenu = CascadePopupMenu(this, anchor)
    popupMenu.menu.apply {
      add("About").also {
        it.setIcon(R.drawable.ic_language_24)
      }
      add("Copy").also {
        it.setIcon(R.drawable.ic_file_copy_24)
      }
      addSubMenu("Delete").also {
        it.setIcon(R.drawable.ic_delete_sweep_24)
        it.setHeaderTitle("Are you sure?")
        it.add("Yep").setIcon(R.drawable.ic_check_24)
        it.add("Go back")
          .setIcon(R.drawable.ic_close_24)
          .setOnMenuItemClickListener {
            popupMenu.navigateBack()
            true
          }
      }
      addSubMenu("Export").also {
        it.setIcon(R.drawable.ic_download_24)
        it.setHeaderTitle("Export as")
        it.add("PDF")
        it.add("EPUB")
        it.add("Image")
        it.add("Web page")
        it.add("Markdown")
        it.add("Plain text")
        it.add("Microsoft word")
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
