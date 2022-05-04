package me.saket.cascade

import android.annotation.SuppressLint
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Measured
import androidx.compose.ui.layout.VerticalAlignmentLine

@SuppressLint("ModifierFactoryUnreferencedReceiver")
class FakeCascadeColumnScope(
  override val cascadeState: CascadeState
) : CascadeColumnScope {

  override fun Modifier.align(alignment: Alignment.Horizontal): Modifier = error("nope")
  override fun Modifier.alignBy(alignmentLineBlock: (Measured) -> Int): Modifier = error("nope")
  override fun Modifier.alignBy(alignmentLine: VerticalAlignmentLine): Modifier = error("nope")
  override fun Modifier.weight(weight: Float, fill: Boolean): Modifier = error("nope")
}
