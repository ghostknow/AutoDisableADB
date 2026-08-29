package com.smilefactory.autodisableadb

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager

object LauncherVisibility {
    fun apply(context: Context, hidden: Boolean) {
        val component = ComponentName(context, "${context.packageName}.LauncherEntry")
        val state = if (hidden) {
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED
        } else {
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED
        }
        context.packageManager.setComponentEnabledSetting(
            component,
            state,
            PackageManager.DONT_KILL_APP,
        )
        Prefs.setLauncherHidden(context, hidden)
    }
}
