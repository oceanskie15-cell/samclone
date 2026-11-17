# Sam Cloner (Android Studio Kotlin Starter Project)

A Jetpack Compose + Material 3 starter project for **Sam Cloner**, modeled after multi-account app cloner workflows (app cloning, custom name/icon shortcuts, per-clone virtual GPS via OpenStreetMap, and per-clone device identity disguise).

## Included Features (v1)
1. **App Cloning & Multi-Account (`HomeScreen`, `AppPickerScreen`)**
   - Lists installed user apps via `PackageManager` (`QUERY_ALL_PACKAGES`).
   - Supports creating multiple isolated clone profiles (`userId` 0, 1, 2...) per package.
   - Clean `VirtualEngine` interface (`engine/VirtualEngine.kt`) ready to connect to an open-source virtualization kernel such as [BlackBox](https://github.com/FBlackBox/BlackBox) or [SpaceCore](https://github.com/FSpaceCore/SpaceCore).
2. **Custom App Name, Badge & Home Shortcut (`CloneSettingsScreen`, `ShortcutHelper`)**
   - Customize clone label, badge color, and icon style.
   - Pin a custom-labeled shortcut to the launcher via `ShortcutManagerCompat`.
3. **Virtual GPS Location (`VirtualLocationScreen`)**
   - Powered by **OpenStreetMap (`osmdroid`)** — free, no API key required.
   - Tap anywhere on the map to pick coordinates, enter Lat/Lng manually, or pick city presets.
4. **Device Info Disguise (`DeviceDisguiseScreen`)**
   - Per-clone spoofing profile for Brand, Manufacturer, Model, Device, Product, Android ID, and Wi-Fi MAC.
   - Includes one-tap presets (Pixel 9 Pro, Galaxy S24 Ultra, OnePlus 12, Xiaomi 14 Ultra) and an ID randomizer.

## How to Open & Run
1. Unzip `sam_cloner_android_project.zip`.
2. Open the `SamCloner` folder in **Android Studio** (Ladybug or newer recommended).
3. Let Gradle sync (compileSdk 35, minSdk 24, Kotlin 2.0.21, Compose BOM 2024.10.01).
4. Run on an Android device or emulator (`arm64-v8a` / `x86_64`).

## Wiring the Virtualization Kernel
`StubVirtualEngine.kt` persists all clone profiles, virtual locations, and device disguise configs locally in `filesDir/clones.json` and simulates launches so you can test the full UI immediately. To run real sandboxed APKs:
1. Add your chosen virtual engine module (e.g., BlackBox or SpaceCore) to `settings.gradle.kts` and `app/build.gradle.kts`.
2. Implement `VirtualEngine` using the engine's `installPackageAsUser`, `launchApk`, location hook, and device-info hook APIs.
