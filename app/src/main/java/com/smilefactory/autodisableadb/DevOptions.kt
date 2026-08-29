package com.smilefactory.autodisableadb

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.Settings

object DevOptions {
    private const val ADB_WIFI_ENABLED = "adb_wifi_enabled"
    private val mainHandler = Handler(Looper.getMainLooper())

    fun hasWriteSecureSettings(context: Context): Boolean {
        if (context.checkSelfPermission(Manifest.permission.WRITE_SECURE_SETTINGS) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return runCatching {
            val current = Settings.Global.getInt(
                context.contentResolver,
                Settings.Global.DEVELOPMENT_SETTINGS_ENABLED,
                0,
            )
            Settings.Global.putInt(
                context.contentResolver,
                Settings.Global.DEVELOPMENT_SETTINGS_ENABLED,
                current,
            )
        }.isSuccess
    }

    fun isDeveloperOptionsEnabled(context: Context): Boolean =
        globalInt(context, Settings.Global.DEVELOPMENT_SETTINGS_ENABLED) == 1

    fun isAdbEnabled(context: Context): Boolean =
        globalInt(context, Settings.Global.ADB_ENABLED) == 1

    fun isAdbWifiEnabled(context: Context): Boolean =
        globalInt(context, ADB_WIFI_ENABLED) == 1

    fun disableForProtectedApp(context: Context): Boolean {
        if (!Prefs.hasSavedSnapshot(context)) {
            Prefs.saveSnapshot(
                context,
                developerOptions = isDeveloperOptionsEnabled(context),
                adb = isAdbEnabled(context),
                adbWifi = isAdbWifiEnabled(context),
            )
        }
        return write(
            context,
            developerOptions = false,
            adb = false,
            adbWifi = false,
        )
    }

    fun restoreSaved(context: Context): Boolean {
        val snapshot = Prefs.savedSnapshot(context) ?: return false
        val ok = write(
            context,
            developerOptions = snapshot.developerOptions,
            adb = snapshot.adb,
            adbWifi = snapshot.adbWifi,
        )
        if (ok && (snapshot.adb || snapshot.adbWifi)) {
            // Samsung often clears ADB when developer options flips; write it again shortly after.
            mainHandler.postDelayed({
                write(
                    context,
                    developerOptions = snapshot.developerOptions,
                    adb = snapshot.adb,
                    adbWifi = snapshot.adbWifi,
                )
            }, 800)
        }
        return ok
    }

    fun setDeveloperOptionsEnabled(context: Context, enabled: Boolean): Boolean {
        return if (enabled) {
            write(context, developerOptions = true, adb = true, adbWifi = isAdbWifiEnabled(context))
        } else {
            write(context, developerOptions = false, adb = false, adbWifi = false)
        }
    }

    fun setDeveloperOptionsOnly(context: Context, enabled: Boolean): Boolean {
        return if (enabled) {
            write(
                context,
                developerOptions = true,
                adb = isAdbEnabled(context),
                adbWifi = isAdbWifiEnabled(context),
            )
        } else {
            write(context, developerOptions = false, adb = false, adbWifi = false)
        }
    }

    fun setUsbDebuggingEnabled(context: Context, enabled: Boolean): Boolean {
        return if (enabled) {
            val ok = write(context, developerOptions = true, adb = true, adbWifi = isAdbWifiEnabled(context))
            if (ok) {
                mainHandler.postDelayed({
                    write(context, developerOptions = true, adb = true, adbWifi = isAdbWifiEnabled(context))
                }, 800)
            }
            ok
        } else {
            write(
                context,
                developerOptions = isDeveloperOptionsEnabled(context),
                adb = false,
                adbWifi = false,
            )
        }
    }

    private fun write(
        context: Context,
        developerOptions: Boolean,
        adb: Boolean,
        adbWifi: Boolean,
    ): Boolean {
        val resolver = context.contentResolver
        return runCatching {
            Settings.Global.putInt(
                resolver,
                Settings.Global.DEVELOPMENT_SETTINGS_ENABLED,
                on(developerOptions),
            )
            Settings.Global.putInt(resolver, Settings.Global.ADB_ENABLED, on(adb))
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                Settings.Global.putInt(resolver, ADB_WIFI_ENABLED, on(adbWifi))
            }
            true
        }.getOrDefault(false)
    }

    private fun globalInt(context: Context, name: String): Int =
        Settings.Global.getInt(context.contentResolver, name, 0)

    private fun on(value: Boolean) = if (value) 1 else 0
}
