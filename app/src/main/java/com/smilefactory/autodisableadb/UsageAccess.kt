package com.smilefactory.autodisableadb

import android.app.AppOpsManager
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.Build
import android.os.Process

object UsageAccess {
    private const val LOOKBACK_MS = 120_000L
    @Volatile
    private var lastKnownPackage: String? = null

    fun isGranted(context: Context): Boolean {
        val appOps = context.getSystemService(AppOpsManager::class.java) ?: return false
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName,
            )
        } else {
            @Suppress("DEPRECATION")
            appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                Process.myUid(),
                context.packageName,
            )
        }
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun foregroundPackage(context: Context): String? {
        val manager = context.getSystemService(UsageStatsManager::class.java) ?: return lastKnownPackage
        val end = System.currentTimeMillis()
        val events = manager.queryEvents(end - LOOKBACK_MS, end)
        val event = UsageEvents.Event()
        var last: String? = null
        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            if (isResume(event.eventType)) {
                last = event.packageName
            }
        }
        if (!last.isNullOrBlank()) {
            lastKnownPackage = last
        }
        return lastKnownPackage
    }

    private fun isResume(type: Int): Boolean {
        @Suppress("DEPRECATION")
        if (type == UsageEvents.Event.MOVE_TO_FOREGROUND) return true
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
            type == UsageEvents.Event.ACTIVITY_RESUMED
    }
}
