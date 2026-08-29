package com.smilefactory.autodisableadb

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.widget.Toast

object QuickTiles {
    fun refreshAll(context: android.content.Context) {
        runCatching {
            TileService.requestListeningState(
                context,
                ComponentName(context, DevOptionsTileService::class.java),
            )
        }
        runCatching {
            TileService.requestListeningState(
                context,
                ComponentName(context, AdbTileService::class.java),
            )
        }
    }

    fun apply(context: Context, tile: Tile?, active: Boolean, label: String, subtitle: String) {
        tile ?: return
        tile.icon = Icon.createWithResource(context, R.drawable.ic_stat)
        tile.label = label
        tile.state = when {
            active -> Tile.STATE_ACTIVE
            else -> Tile.STATE_INACTIVE
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            tile.subtitle = subtitle
        }
        tile.updateTile()
    }

    fun openApp(service: TileService) {
        val intent = Intent(service, MainActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val pending = PendingIntent.getActivity(
                service,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
            )
            service.startActivityAndCollapse(pending)
        } else {
            @Suppress("DEPRECATION")
            service.startActivityAndCollapse(intent)
        }
    }

    fun toast(service: TileService, message: String) {
        Handler(Looper.getMainLooper()).post {
            Toast.makeText(service.applicationContext, message, Toast.LENGTH_SHORT).show()
        }
    }
}
