package com.smilefactory.autodisableadb

import android.content.Context
import android.os.Handler
import android.os.Looper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MonitorController(private val context: Context) {
    private val handler = Handler(Looper.getMainLooper())
    private var restoreRunnable: Runnable? = null

    fun tick() {
        if (!Prefs.isMonitorEnabled(context)) return
        val pkg = UsageAccess.foregroundPackage(context) ?: return
        if (pkg.isBlank() || shouldIgnore(pkg)) return

        val targets = Prefs.getTargets(context)
        if (targets.isEmpty()) return

        if (pkg in targets) {
            cancelRestore()
            disableIfNeeded()
            return
        }

        if (Prefs.isRestoreEnabled(context) && Prefs.wasDisabledByUs(context)) {
            scheduleRestoreOnce()
        }
    }

    fun cancel() {
        cancelRestore()
    }

    private fun scheduleRestoreOnce() {
        if (restoreRunnable != null) return
        val runnable = Runnable {
            restoreRunnable = null
            restoreIfNeeded()
        }
        restoreRunnable = runnable
        handler.postDelayed(runnable, RESTORE_DELAY_MS)
    }

    private fun cancelRestore() {
        restoreRunnable?.let { handler.removeCallbacks(it) }
        restoreRunnable = null
    }

    private fun shouldIgnore(pkg: String): Boolean {
        if (pkg == context.packageName) return true
        return pkg in IGNORED_PACKAGES
    }

    private fun disableIfNeeded() {
        if (!DevOptions.isDeveloperOptionsEnabled(context) && !DevOptions.isAdbEnabled(context)) {
            return
        }
        if (!DevOptions.disableForProtectedApp(context)) return
        Prefs.setDisabledByUs(context, true)
        recordAction("off")
        QuickTiles.refreshAll(context)
    }

    private fun restoreIfNeeded() {
        if (!Prefs.wasDisabledByUs(context)) return
        val current = UsageAccess.foregroundPackage(context)
        if (current != null && current in Prefs.getTargets(context)) return
        if (!DevOptions.restoreSaved(context)) return
        Prefs.setDisabledByUs(context, false)
        Prefs.clearSnapshot(context)
        recordAction("on")
        QuickTiles.refreshAll(context)
    }

    private fun recordAction(tag: String) {
        val time = TIME_FORMAT.format(Date())
        Prefs.setLastAction(context, "$time  $tag")
    }

    companion object {
        private const val RESTORE_DELAY_MS = 800L
        private val TIME_FORMAT = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

        private val IGNORED_PACKAGES = setOf(
            "com.android.systemui",
            "com.google.android.permissioncontroller",
            "com.android.permissioncontroller",
            "com.google.android.inputmethod.latin",
            "com.android.inputmethod.latin",
            "android",
        )
    }
}
