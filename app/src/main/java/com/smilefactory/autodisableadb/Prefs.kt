package com.smilefactory.autodisableadb

import android.content.Context

object Prefs {
    private const val FILE = "prefs"
    private const val KEY_TARGETS = "targets"
    private const val KEY_MONITOR = "monitor_enabled"
    private const val KEY_RESTORE = "restore_enabled"
    private const val KEY_HIDE_LAUNCHER = "hide_launcher"
    private const val KEY_DISABLED_BY_US = "disabled_by_us"
    private const val KEY_SAVED_DEV = "saved_dev"
    private const val KEY_SAVED_ADB = "saved_adb"
    private const val KEY_SAVED_ADB_WIFI = "saved_adb_wifi"
    private const val KEY_HAS_SNAPSHOT = "has_snapshot"
    private const val KEY_LAST_ACTION = "last_action"

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    fun getTargets(context: Context): Set<String> =
        prefs(context).getStringSet(KEY_TARGETS, emptySet())?.toSet().orEmpty()

    fun addTarget(context: Context, packageName: String): Boolean {
        val current = getTargets(context).toMutableSet()
        val added = current.add(packageName)
        if (added) {
            prefs(context).edit().putStringSet(KEY_TARGETS, current).apply()
        }
        return added
    }

    fun removeTarget(context: Context, packageName: String) {
        val current = getTargets(context).toMutableSet()
        if (current.remove(packageName)) {
            prefs(context).edit().putStringSet(KEY_TARGETS, current).apply()
        }
    }

    fun isMonitorEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_MONITOR, false)

    fun setMonitorEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_MONITOR, enabled).apply()
    }

    fun isRestoreEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_RESTORE, true)

    fun setRestoreEnabled(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_RESTORE, enabled).apply()
    }

    fun isLauncherHidden(context: Context): Boolean =
        prefs(context).getBoolean(KEY_HIDE_LAUNCHER, false)

    fun setLauncherHidden(context: Context, hidden: Boolean) {
        prefs(context).edit().putBoolean(KEY_HIDE_LAUNCHER, hidden).apply()
    }

    fun wasDisabledByUs(context: Context): Boolean =
        prefs(context).getBoolean(KEY_DISABLED_BY_US, false)

    fun setDisabledByUs(context: Context, value: Boolean) {
        prefs(context).edit().putBoolean(KEY_DISABLED_BY_US, value).apply()
    }

    fun hasSavedSnapshot(context: Context): Boolean =
        prefs(context).getBoolean(KEY_HAS_SNAPSHOT, false)

    fun saveSnapshot(context: Context, developerOptions: Boolean, adb: Boolean, adbWifi: Boolean) {
        prefs(context).edit()
            .putBoolean(KEY_HAS_SNAPSHOT, true)
            .putBoolean(KEY_SAVED_DEV, developerOptions)
            .putBoolean(KEY_SAVED_ADB, adb)
            .putBoolean(KEY_SAVED_ADB_WIFI, adbWifi)
            .apply()
    }

    fun savedSnapshot(context: Context): DevSnapshot? {
        if (!hasSavedSnapshot(context)) return null
        val stored = prefs(context)
        return DevSnapshot(
            developerOptions = stored.getBoolean(KEY_SAVED_DEV, true),
            adb = stored.getBoolean(KEY_SAVED_ADB, true),
            adbWifi = stored.getBoolean(KEY_SAVED_ADB_WIFI, false),
        )
    }

    fun clearSnapshot(context: Context) {
        prefs(context).edit()
            .putBoolean(KEY_HAS_SNAPSHOT, false)
            .remove(KEY_SAVED_DEV)
            .remove(KEY_SAVED_ADB)
            .remove(KEY_SAVED_ADB_WIFI)
            .apply()
    }

    fun getLastAction(context: Context): String =
        prefs(context).getString(KEY_LAST_ACTION, "").orEmpty()

    fun setLastAction(context: Context, action: String) {
        prefs(context).edit().putString(KEY_LAST_ACTION, action).apply()
    }
}

data class DevSnapshot(
    val developerOptions: Boolean,
    val adb: Boolean,
    val adbWifi: Boolean,
)
