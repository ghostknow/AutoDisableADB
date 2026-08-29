package com.smilefactory.autodisableadb

import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

object UiInsets {
    fun apply(root: View, appBar: View, bottomContent: View? = null) {
        ViewCompat.setOnApplyWindowInsetsListener(root) { _, windowInsets ->
            val bars = windowInsets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout(),
            )
            val extra = (16 * root.resources.displayMetrics.density).toInt()
            appBar.updatePadding(top = bars.top + extra)
            bottomContent?.updatePadding(bottom = bars.bottom)
            WindowInsetsCompat.CONSUMED
        }
        ViewCompat.requestApplyInsets(root)
    }
}
