# Crystal Launcher

Crystal Launcher is a real Android HOME/launcher app built with Kotlin and Jetpack Compose Material 3. It is a launcher app, not a replacement Android OS: installing it does not flash firmware, unlock a bootloader, or require root.

## Features in this starter release

- Android HOME app entry so it can be selected as the default launcher.
- Queries launchable app activities and displays their real names and icons.
- Launches apps with explicit package/activity intents.
- App drawer search by app name or package name.
- Long-press an app to add/remove it from the home favorites.
- Personalization: System/Dark/Light theme, six accent colors, six gradient wallpapers, 3–6 column grid, icon scale, icon shape, labels, and reduced transitions.
- Hide/unhide apps inside Crystal Launcher without uninstalling them or changing Android settings.
- Preferences saved locally; no network permission, account, analytics, or root requirement.
- Unit tests for app-search matching.

## Build an APK with GitHub Actions (phone-friendly)

1. Create a GitHub repository and upload the contents of this project.
2. Open the repository's **Actions** tab.
3. Select **Build Crystal Launcher APK** and tap **Run workflow** (or push a commit to `main`/`master`).
4. When the workflow finishes, open the run and download the `CrystalLauncher-debug` artifact. Extract it to get `app-debug.apk`.
5. Open the APK on Android and install it. Android may ask you to allow installation from the app used to open the file.
6. Open **Settings → Apps → Default apps → Home app** (wording varies by Android skin) and choose **Crystal Launcher**.

The workflow builds with Java 17, Gradle 8.13 and Android SDK 36, and runs local unit tests plus Android lint before uploading the debug APK. This repository has not been built on every OEM/device; use the workflow result as the real compiler/test report.

## Build locally

Use Android Studio with JDK 17 and Android SDK Platform 36, then open this folder and run `./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug`. The included `gradlew` script downloads Gradle 8.13 on first run. The APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

## Limitations

- Android restricts what third-party launchers can control. Crystal does not replace the system quick settings, lock screen, notification shade, or system status bar.
- App icons and launchable activities are provided by installed apps. Some manufacturers hide certain apps or expose multiple launcher activities.
- Wallpaper choices currently change the launcher's gradient background; they do not change the phone's system wallpaper.
- Hide apps only removes them from Crystal Launcher; it does not securely lock or hide them from Android Settings or other launchers.
- This is a first full project release, not a claim of zero defects. Test on the intended device before choosing it as the default Home app.
