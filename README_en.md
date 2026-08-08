# ShizukuRunner

<div align="center">
<img width="48" height="48" alt="Image" src="https://github.com/user-attachments/assets/b56172fa-260e-421e-8a71-e82c646042c9" />
</div>

如果你的主要语言不是英文，你可以使用 [中文版本](./README.md)。

If your primary language is not Chinese, you can use the [English Version](./README_en.md).

A lightweight Android Shell command execution tool that invokes system shell via [Shizuku](https://shizuku.rikka.app/), enabling adb-level command execution without root access.

- Obtain permissions from activated Shizuku
- Save frequently used commands to quick slots for one-tap execution
- Support root demotion to shell to reduce permission risks
- In-app Chinese/English language switching
- Fully offline, no privacy data collection

## Screenshots

First-launch dialog↓

<img src="https://github.com/user-attachments/assets/98238403-614c-4938-8b67-3271d3c54499" width="160" />

Main interface↓

<img src="https://github.com/user-attachments/assets/36b39ce7-17fa-423d-a696-db844ec82900" width="160" />

Single command input interface↓

<img src="https://github.com/user-attachments/assets/cbc0aa95-d005-4deb-9a4f-40e2beac1a8e" width="160" />

Command output result interface↓

<img src="https://github.com/user-attachments/assets/dbebe678-62b5-4608-8f31-535f2a4065ec" width="160" />

Settings interface↓

<img src="https://github.com/user-attachments/assets/6eb4c589-9f95-411a-9371-9aca7b944de0" width="160" />

## Download

[![Release](https://img.shields.io/github/v/release/September-meteor/ShizukuRunner?label=Latest+Version)](https://github.com/September-meteor/ShizukuRunner/releases/latest)

- Download the latest APK from the [Releases](https://github.com/September-meteor/ShizukuRunner/releases) page
- Currently requires minimum Android 6.0 (API 23)

### Important Notes

1. **Shizuku environment required**: Please ensure [Shizuku](https://shizuku.rikka.app/) is installed and activated on your device, and successfully authorized to this app, otherwise it cannot run
2. **Limited test devices**: Mainly tested on personal devices, compatibility issues may exist on different ROMs or Android versions
3. **Release version recommended**: Modifying Gradle/AGP versions during self-compilation may introduce unexpected issues

If the app fails to run after installation, please [submit an Issue](https://github.com/September-meteor/ShizukuRunner/issues) with device model, Android version, Shizuku version, and detailed problem description.

## Usage

1. Ensure [Shizuku](https://shizuku.rikka.app/) is installed and activated on your device
2. Grant Shizuku permission on first launch and complete setup following prompts
3. Tap a slot or「+」to edit commands
4. Tap「>」to run after editing; long-press output to copy all content
5. Tap the cat avatar to switch to single command mode, tap again to return to main interface
6. Tap empty area to exit the app

**Common Operations**
- Long-press cat avatar: Open help and settings, tap OK to return to main interface
- Long-press saved command entry: Copy command to clipboard
- Tap Shizuku status button at top: Refresh authorization status

**Important Notes**
- Write the command you want to execute directly, **do NOT add `adb shell` prefix**
- Non-root users can ignore the "demotion" option
- Do NOT run commands from unknown sources that may damage the system

**Advanced Features**
- **Environment Variables**: Settings → Edit Environment Variables, preset KEY=VALUE variables (e.g., PATH, LANG, etc.)
- **Config Backup**: Settings → Import/Export Command Config, support one-tap backup and restore

## Differences from Original

This project is modified based on [WuDi-ZhanShen/ShizukuRunner](https://github.com/WuDi-ZhanShen/ShizukuRunner)'s ShizukuRunner, main changes include:

**Feature Enhancements**
- Execution result interface supports long-press to copy all output
- Complete cat avatar flip animation
- Add in-app Chinese/English language switching
- Add multi-line command input with auto-wrap (three modes: multi-line/wrap-only/no-wrap)
- Add command config import/export functionality
- Add custom environment variable injection

**Bug Fixes**
- Fix null pointer crash during log output
- Fix command output redirection (`>` / `>>`) transaction failure due to Binder buffer limits in Shizuku environment
- Fix `root demotion to shell` functionality异常
- Fix parsing errors for commands containing `&&` symbols due to `\r`
- Fix authorization detection failure due to missing Shizuku permission declaration
- Fix abnormal color display on some interfaces when theme follows system switching

**Build & Maintenance**
- Upgrade Gradle / AGP toolchain, remove deprecated jcenter repository
- Fix resource type references and compilation warnings, eliminate Release build errors
- Remove hardcoded signing configuration, decouple from Windows environment
- Add Termux compilation support, supplement .gitignore and LICENSE

## Self-Compilation

**Desktop (Android Studio)**: Refer to [delmsyap/ShizukuRunner](https://github.com/delmsyap/ShizukuRunner)'s README.

**Mobile (Termux)**:

This project is mainly compiled using Termux. If you haven't configured the Termux compilation environment (Android SDK, JDK, etc.), please refer to [BUILD_TERMUX.md](./BUILD_TERMUX.md) for prerequisites first.

After the environment is ready, compile following these steps:

1. Clone the repository

```bash
git clone https://github.com/September-meteor/ShizukuRunner.git
cd ShizukuRunner
```

2. Uncomment Termux configuration

```bash
sed -i 's/^# android.aapt2FromMavenOverride=/android.aapt2FromMavenOverride=/' gradle.properties
sed -i 's/^# org.gradle.java.home=/org.gradle.java.home=/' gradle.properties
```

3. Ensure `local.properties` exists in project root:

```bash
echo "sdk.dir=$ANDROID_HOME" > local.properties
```

4. Compile

```bash
./gradlew :app:assembleRelease
```

5. Install

```bash
cp app/build/outputs/apk/release/app-release-unsigned.apk /sdcard/Download/
```

In the file manager, select the Download folder, find app-release-unsigned.apk and tap to install.

## License

The original project was created by [WuDi-ZhanShen](https://github.com/WuDi-ZhanShen) without a declared open source license.

The **code added and modified by me (September-meteor/z)** in this fork is released under the [MIT](./LICENSE) license.  
Copyright of the original project code remains with the original author.

If the original rights holder has any objections to the use of this project, please [submit an Issue](https://github.com/September-meteor/ShizukuRunner/issues) or contact via email.