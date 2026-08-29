package com.smilefactory.autodisableadb

import android.os.Handler
import android.os.Looper
import android.service.quicksettings.TileService

class AdbTileService : TileService() {
    override fun onStartListening() {
        render()
    }

    override fun onClick() {
        if (!DevOptions.hasWriteSecureSettings(this)) {
            QuickTiles.toast(this, getString(R.string.need_write_secure))
            QuickTiles.openApp(this)
            return
        }
        val enable = !DevOptions.isAdbEnabled(this)
        if (!DevOptions.setUsbDebuggingEnabled(this, enable)) {
            QuickTiles.toast(this, getString(R.string.need_write_secure))
            return
        }
        Prefs.setDisabledByUs(this, false)
        render()
        QuickTiles.refreshAll(this)
        if (enable) {
            Handler(Looper.getMainLooper()).postDelayed({
                render()
                QuickTiles.refreshAll(this)
            }, 900)
        }
    }

    private fun render() {
        val on = DevOptions.isAdbEnabled(this)
        QuickTiles.apply(
            this,
            qsTile,
            active = on,
            label = getString(R.string.tile_usb_debugging),
            subtitle = getString(if (on) R.string.status_on else R.string.status_off),
        )
    }
}
