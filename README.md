# AutoDisableADB

[繁體中文](#繁體中文) · [简体中文](#简体中文) · [English](#english)

---

## 繁體中文

開啟指定應用程式（例如銀行、支付 App）時，自動關閉 **開發人員選項**、**USB 偵錯** 與無線偵錯；離開後可選擇自動還原。適合日常開發時不想在敏感 App 裡留下偵錯開關的情況。

[下載 APK](https://github.com/ghostknow/AutoDisableADB/releases/latest)

### 功能

- 前景監控：目標 App 在前景時關閉開發人員選項與偵錯
- 離開後自動還原先前狀態（可關閉）
- 快捷設定圖塊：手動開關「開發人員選項」與「USB 偵錯」
- 可從應用程式列表隱藏圖示，改由快捷設定開啟
- 開機或更新後，若已啟用監控會自動繼續
- 介面支援繁體中文與英文

### 系統需求

- Android 8.0（API 26）或以上
- 電腦已安裝 [Android platform-tools](https://developer.android.com/tools/releases/platform-tools)（僅首次授權需要）

### 設定步驟

1. 安裝 [最新 Release 的 APK](https://github.com/ghostknow/AutoDisableADB/releases/latest)。
2. 以 USB 連接手機，開啟 USB 偵錯，在電腦執行一次：

   ```bash
   adb shell pm grant com.smilefactory.autodisableadb android.permission.WRITE_SECURE_SETTINGS
   ```

   部分品牌需先在開發人員選項關閉「權限監控」。
3. 在 App 內開啟 **使用情況存取權**。
4. 排除電池最佳化（Samsung 請再把本 App 排除休眠／自動關閉）。
5. 新增目標應用程式，然後開啟 **啟用自動關閉**。
6. （建議）在下拉快捷設定加入「開發人員選項」與「USB 偵錯」圖塊。

隱藏桌面圖示後，請從快捷設定圖塊進入本 App。

### 權限說明

| 權限 | 用途 |
| --- | --- |
| `WRITE_SECURE_SETTINGS` | 開關開發人員選項與 USB／無線偵錯（系統保護權限，需 ADB 授予一次） |
| 使用情況存取權 | 判斷目前前景應用程式 |
| 前景服務與通知 | 持續監控，避免被系統殺掉 |
| 開機完成 | 重啟後恢復監控 |
| 忽略電池最佳化 | 降低背景被限制的機會 |

本 App **不會**備份裝置資料（`allowBackup` 為 false），也不會把設定上傳到網路。

### 自行編譯

需要 JDK 17 與 Android SDK。

```bash
./gradlew assembleRelease
```

### 注意事項

- 此權限可變更系統安全設定，請只從本倉庫安裝。
- GitHub Release 的 APK 以本機 debug keystore 簽署，方便側載；若要上架或長期更新同一個簽名，請改用自己的正式金鑰。
- 監控期間會顯示常駐通知，這是前景服務的正常行為。

---

## 简体中文

打开指定应用（例如银行、支付 App）时，自动关闭 **开发者选项**、**USB 调试** 和无线调试；离开后可选择自动还原。适合日常开发时不想在敏感 App 里留下调试开关的情况。

[下载 APK](https://github.com/ghostknow/AutoDisableADB/releases/latest)

### 功能

- 前台监控：目标 App 在前台时关闭开发者选项与调试
- 离开后自动还原先前状态（可关闭）
- 快捷设置磁贴：手动开关「开发者选项」与「USB 调试」
- 可从应用列表隐藏图标，改由快捷设置打开
- 开机或更新后，若已启用监控会自动继续
- 界面支持繁体中文与英文

### 系统要求

- Android 8.0（API 26）或以上
- 电脑已安装 [Android platform-tools](https://developer.android.com/tools/releases/platform-tools)（仅首次授权需要）

### 设置步骤

1. 安装 [最新 Release 的 APK](https://github.com/ghostknow/AutoDisableADB/releases/latest)。
2. 用 USB 连接手机，开启 USB 调试，在电脑执行一次：

   ```bash
   adb shell pm grant com.smilefactory.autodisableadb android.permission.WRITE_SECURE_SETTINGS
   ```

   部分品牌需先在开发者选项中关闭「权限监控」。
3. 在 App 内开启 **使用情况访问权限**。
4. 排除电池优化（三星请再把本 App 排除休眠／自动关闭）。
5. 添加目标应用，然后打开 **启用自动关闭**。
6. （建议）在下拉快捷设置中加入「开发者选项」与「USB 调试」磁贴。

隐藏桌面图标后，请从快捷设置磁贴进入本 App。

### 权限说明

| 权限 | 用途 |
| --- | --- |
| `WRITE_SECURE_SETTINGS` | 开关开发者选项与 USB／无线调试（系统保护权限，需 ADB 授予一次） |
| 使用情况访问权限 | 判断当前前台应用 |
| 前台服务与通知 | 持续监控，避免被系统杀掉 |
| 开机完成 | 重启后恢复监控 |
| 忽略电池优化 | 降低后台被限制的机会 |

本 App **不会**备份设备数据（`allowBackup` 为 false），也不会把设置上传到网络。

### 自行编译

需要 JDK 17 与 Android SDK。

```bash
./gradlew assembleRelease
```

### 注意事项

- 此权限可变更系统安全设置，请只从本仓库安装。
- GitHub Release 的 APK 以本地 debug keystore 签名，方便侧载；若要上架或长期更新同一个签名，请改用自己的正式密钥。
- 监控期间会显示常驻通知，这是前台服务的正常行为。

---

## English

Automatically turns off **Developer options**, **USB debugging**, and wireless debugging when a selected app (for example a banking or payment app) comes to the foreground. Optionally restores the previous state when you leave. Useful if you develop with debugging enabled but do not want those switches left on inside sensitive apps.

[Download APK](https://github.com/ghostknow/AutoDisableADB/releases/latest)

### Features

- Foreground monitor: disables Developer options and debugging while a target app is in front
- Optional restore of the previous state after you leave
- Quick Settings tiles to toggle Developer options and USB debugging by hand
- Optional launcher-icon hide; open the app from Quick Settings instead
- Resumes monitoring after reboot or app update if it was enabled
- UI in Traditional Chinese and English

### Requirements

- Android 8.0 (API 26) or later
- [Android platform-tools](https://developer.android.com/tools/releases/platform-tools) on your computer (only for the one-time grant)

### Setup

1. Install the [latest release APK](https://github.com/ghostknow/AutoDisableADB/releases/latest).
2. Connect the phone over USB with USB debugging on, then run once:

   ```bash
   adb shell pm grant com.smilefactory.autodisableadb android.permission.WRITE_SECURE_SETTINGS
   ```

   On some OEMs, turn off permission monitoring in Developer options first.
3. Enable **Usage access** in the app.
4. Exclude the app from battery optimization (on Samsung, also exclude it from sleeping / auto-kill lists).
5. Add target apps, then turn on **Enable auto disable**.
6. (Recommended) Add the Developer options and USB debugging tiles in Quick Settings.

If you hide the launcher icon, open the app from those Quick Settings tiles.

### Permissions

| Permission | Why |
| --- | --- |
| `WRITE_SECURE_SETTINGS` | Toggle Developer options and USB / wireless debugging (protected; grant once via ADB) |
| Usage access | Detect the current foreground app |
| Foreground service and notifications | Keep monitoring alive |
| Boot completed | Resume after reboot |
| Ignore battery optimizations | Reduce background killing |

The app does **not** back up device data (`allowBackup` is false) and does not upload settings.

### Build

JDK 17 and the Android SDK are required.

```bash
./gradlew assembleRelease
```

### Notes

- `WRITE_SECURE_SETTINGS` can change security-related system settings. Install only from this repository.
- The GitHub Release APK is signed with a local Android debug keystore for sideloading. Use your own release keystore for Play Store or long-term updates under the same signature.
- A persistent notification while monitoring is expected for the foreground service.
