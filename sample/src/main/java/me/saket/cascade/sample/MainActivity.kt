package me.saket.cascade.sample

import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Color.BLACK
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.RippleDrawable
import android.net.Uri
import android.os.Bundle
import android.util.TypedValue
import android.view.SubMenu
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.MenuCompat
import androidx.core.view.postDelayed
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetView
import com.getkeepsafe.taptargetview.TapTargetView.Listener
import me.saket.cascade.CascadePopupMenu
import me.saket.cascade.add
import me.saket.cascade.allChildren

class MainActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    val toolbar = findViewById<Toolbar>(R.id.toolbar)
    toolbar.inflateMenu(R.menu.toolbar)

    val menuButton = toolbar.findViewById<View>(R.id.overflow_menu)
    showcaseMenuButton(toolbar, menuButton)
    menuButton.setOnClickListener {
      showCascadeMenu(anchor = menuButton)
    }
  }

  private fun showCascadeMenu(anchor: View) {
    val popupMenu = CascadePopupMenu(this, anchor, styler = cascadeMenuStyler())
    popupMenu.menu.apply {
      add("About", groupId = 42).setIcon(R.drawable.ic_language_24)
      add("Copy", groupId = 42).setIcon(R.drawable.ic_file_copy_24)
      MenuCompat.setGroupDividerEnabled(this, true)

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

      allChildren.filter { it.intent == null }.forEach {
        it.setOnMenuItemClickListener {
          popupMenu.navigateBack()
        }
      }
    }
    popupMenu.show()
  }

  private fun cascadeMenuStyler(): CascadePopupMenu.Styler {
    val rippleDrawable = {
      RippleDrawable(ColorStateList.valueOf(Color.parseColor("#B1DDC6")), null, ColorDrawable(BLACK))
    }

    return CascadePopupMenu.Styler(
      background = {
        RoundedRectDrawable(Color.parseColor("#E0EEE7"), radius = 8f.dip)
      },
      menuTitle = {
        it.titleView.typeface = ResourcesCompat.getFont(this, R.font.work_sans_medium)
        it.itemView.background = rippleDrawable()
      },
      menuItem = {
        it.titleView.typeface = ResourcesCompat.getFont(this, R.font.work_sans_medium)
        it.contentView.background = rippleDrawable()
      }
    )
  }

  private fun cashAppIcon() =
    AppCompatResources.getDrawable(this, R.drawable.ic_cash_app_24)!!.also {
      it.mutate()
      it.setTint(ContextCompat.getColor(this, R.color.color_control_normal))
    }

  private fun intent(url: String) =
    Intent(ACTION_VIEW, Uri.parse(url))

  private fun showcaseMenuButton(toolbar: Toolbar?, menuButton: View) {
    val tapTarget = TapTarget
      .forToolbarMenuItem(toolbar, R.id.overflow_menu, "Tap to see Cascade in action")
      .transparentTarget(true)
      .outerCircleColor(R.color.color_control_normal)
      .titleTextColor(R.color.window_background)

    TapTargetView.showFor(this, tapTarget, object : Listener() {
      override fun onTargetClick(view: TapTargetView) {
        super.onTargetClick(view)
        view.postDelayed(200) { menuButton.performClick() }
      }
    })
  }

  private val Float.dip: Float
    get() {
      val metrics = resources.displayMetrics
      return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this, metrics)
    }
}

