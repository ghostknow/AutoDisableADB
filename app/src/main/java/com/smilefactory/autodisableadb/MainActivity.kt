package com.smilefactory.autodisableadb

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.smilefactory.autodisableadb.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var targetAdapter: TargetAdapter

    private val pickApp = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) { result ->
        val pkg = result.data?.getStringExtra(AppPickerActivity.EXTRA_PACKAGE) ?: return@registerForActivityResult
        addTarget(pkg)
    }

    private val requestNotifications = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { /* status is refreshed in onResume */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        UiInsets.apply(binding.root, binding.appBar, binding.scroll)
        binding.textAdbCommand.text = adbGrantCommand()
        binding.toolbar.setOnMenuItemClickListener { item ->
            if (item.itemId == R.id.action_language) {
                AppLanguage.toggle()
                true
            } else {
                false
            }
        }

        targetAdapter = TargetAdapter { pkg ->
            Prefs.removeTarget(this, pkg)
            refreshTargets()
        }
        binding.recyclerTargets.layoutManager = LinearLayoutManager(this)
        binding.recyclerTargets.adapter = targetAdapter

        binding.switchMonitor.isChecked = Prefs.isMonitorEnabled(this)
        binding.switchRestore.isChecked = Prefs.isRestoreEnabled(this)
        binding.switchHideLauncher.isChecked = Prefs.isLauncherHidden(this)

        binding.switchMonitor.setOnCheckedChangeListener { _, checked ->
            if (checked) enableMonitor() else disableMonitor()
        }
        binding.switchRestore.setOnCheckedChangeListener { _, checked ->
            Prefs.setRestoreEnabled(this, checked)
        }
        binding.switchHideLauncher.setOnCheckedChangeListener { _, checked ->
            LauncherVisibility.apply(this, checked)
            if (checked) toast(getString(R.string.hide_launcher_hint_on, getString(R.string.app_name)))
        }

        binding.buttonAddPackage.setOnClickListener { addFromInput() }
        binding.editPackage.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                addFromInput()
                true
            } else {
                false
            }
        }
        binding.buttonPickApp.setOnClickListener {
            pickApp.launch(Intent(this, AppPickerActivity::class.java))
        }
        binding.buttonOpenUsageAccess.setOnClickListener {
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        }
        binding.buttonBattery.setOnClickListener { requestIgnoreBattery() }
        binding.buttonCopyAdb.setOnClickListener {
            val clipboard = getSystemService(ClipboardManager::class.java)
            clipboard.setPrimaryClip(ClipData.newPlainText("adb", adbGrantCommand()))
            toast(getString(R.string.adb_copied))
        }
        binding.buttonDisableNow.setOnClickListener { setDevOptions(false) }
        binding.buttonEnableNow.setOnClickListener { setDevOptions(true) }

        refreshTargets()
        maybeRequestNotifications()
    }

    override fun onResume() {
        super.onResume()
        refreshStatus()
        if (Prefs.isMonitorEnabled(this) && UsageAccess.isGranted(this)) {
            MonitorService.start(this)
        }
    }

    private fun adbGrantCommand(): String =
        getString(R.string.adb_command, packageName)

    private fun enableMonitor() {
        when {
            Prefs.getTargets(this).isEmpty() -> {
                binding.switchMonitor.isChecked = false
                toast(getString(R.string.need_targets))
            }
            !UsageAccess.isGranted(this) -> {
                binding.switchMonitor.isChecked = false
                toast(getString(R.string.need_usage_access))
            }
            !DevOptions.hasWriteSecureSettings(this) -> {
                binding.switchMonitor.isChecked = false
                toast(getString(R.string.need_write_secure))
            }
            else -> {
                Prefs.setMonitorEnabled(this, true)
                maybeRequestNotifications()
                requestIgnoreBattery()
                MonitorService.start(this)
            }
        }
    }

    private fun disableMonitor() {
        Prefs.setMonitorEnabled(this, false)
        MonitorService.stop(this)
    }

    private fun addFromInput() {
        val pkg = binding.editPackage.text?.toString()?.trim().orEmpty()
        if (!PACKAGE_REGEX.matches(pkg)) {
            toast(getString(R.string.invalid_package))
            return
        }
        addTarget(pkg)
        binding.editPackage.text = null
    }

    private fun addTarget(pkg: String) {
        if (Prefs.addTarget(this, pkg)) {
            toast(getString(R.string.package_added, InstalledApps.labelFor(this, pkg)))
        } else {
            toast(getString(R.string.package_exists))
        }
        refreshTargets()
    }

    private fun setDevOptions(enabled: Boolean) {
        if (!DevOptions.hasWriteSecureSettings(this)) {
            toast(getString(R.string.need_write_secure))
            return
        }
        if (DevOptions.setDeveloperOptionsEnabled(this, enabled)) {
            Prefs.setDisabledByUs(this, !enabled)
            QuickTiles.refreshAll(this)
            toast(
                getString(
                    if (enabled) R.string.toast_enabled_manual else R.string.toast_disabled_manual,
                ),
            )
        } else {
            toast(getString(R.string.need_write_secure))
        }
        refreshStatus()
    }

    private fun refreshTargets() {
        val targets = Prefs.getTargets(this)
        targetAdapter.submit(targets)
        binding.textTargetsEmpty.visibility =
            if (targets.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
    }

    private fun refreshStatus() {
        val usageOn = UsageAccess.isGranted(this)
        binding.textUsageAccessStatus.text =
            getString(if (usageOn) R.string.status_on else R.string.status_off)
        binding.textUsageAccessStatus.setTextColor(statusColor(usageOn))

        val writeSecure = DevOptions.hasWriteSecureSettings(this)
        binding.textWriteSecureStatus.text =
            getString(
                if (writeSecure) R.string.write_secure_granted else R.string.write_secure_missing,
            )
        binding.textWriteSecureStatus.setTextColor(statusColor(writeSecure))

        val batteryOk = isIgnoringBattery()
        binding.textBatteryStatus.text =
            getString(if (batteryOk) R.string.battery_ok else R.string.battery_restricted)
        binding.textBatteryStatus.setTextColor(statusColor(batteryOk))

        val devOn = DevOptions.isDeveloperOptionsEnabled(this)
        binding.textDevOptionsStatus.text =
            getString(if (devOn) R.string.dev_options_on else R.string.dev_options_off)
        binding.textAdbStatus.text =
            getString(if (DevOptions.isAdbEnabled(this)) R.string.adb_on else R.string.adb_off)

        val last = Prefs.getLastAction(this)
        binding.textLastAction.text =
            if (last.isBlank()) getString(R.string.last_action_none)
            else getString(R.string.last_action, last)
        QuickTiles.refreshAll(this)
    }

    private fun isIgnoringBattery(): Boolean {
        val pm = getSystemService(PowerManager::class.java)
        return pm.isIgnoringBatteryOptimizations(packageName)
    }

    private fun requestIgnoreBattery() {
        if (isIgnoringBattery()) return
        runCatching {
            startActivity(
                Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = Uri.parse("package:$packageName")
                },
            )
        }
    }

    private fun maybeRequestNotifications() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) !=
                android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                requestNotifications.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun statusColor(ok: Boolean): Int {
        val colorRes = if (ok) R.color.status_ok else R.color.status_warn
        return ContextCompat.getColor(this, colorRes)
    }

    private fun toast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private val PACKAGE_REGEX = Regex("^[A-Za-z][A-Za-z0-9_]*(\\.[A-Za-z][A-Za-z0-9_]*)+$")
    }
}
