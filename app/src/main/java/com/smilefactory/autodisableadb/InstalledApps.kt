package com.smilefactory.autodisableadb

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable

data class InstalledApp(
    val packageName: String,
    val label: String,
    val icon: Drawable,
)

object InstalledApps {
    fun launchable(packageManager: PackageManager): List<InstalledApp> {
        val launchIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        return packageManager.queryIntentActivities(launchIntent, PackageManager.MATCH_ALL)
            .mapNotNull { resolve ->
                val info = resolve.activityInfo ?: return@mapNotNull null
                InstalledApp(
                    packageName = info.packageName,
                    label = resolve.loadLabel(packageManager).toString(),
                    icon = resolve.loadIcon(packageManager),
                )
            }
            .distinctBy { it.packageName }
            .sortedBy { it.label.lowercase() }
    }

    fun labelFor(context: android.content.Context, packageName: String): String {
        return runCatching {
            val manager = context.packageManager
            val info = manager.getApplicationInfo(packageName, 0)
            manager.getApplicationLabel(info).toString()
        }.getOrDefault(packageName)
    }

    fun iconFor(context: android.content.Context, packageName: String): Drawable? {
        return runCatching { context.packageManager.getApplicationIcon(packageName) }.getOrNull()
    }
}
