# Hurkledurkle — Android Wrapper

Hotwire Native Android wrapper that loads the Rails web app in a native shell.

---

## Local Development Setup

### 1. Prerequisites

- [Android Studio](https://developer.android.com/studio) (Hedgehog 2023.1.1 or later)
- Android SDK 26+ (install via Android Studio → SDK Manager)
- JDK 17 (bundled with Android Studio)
- Rails server running locally (`bin/dev` from project root)

### 2. Open the project in Android Studio

1. Open Android Studio
2. Choose **Open** (not "New Project")
3. Navigate to and select the **`android/`** folder inside this repo
4. Wait for Gradle sync to complete (first sync downloads dependencies — may take a few minutes)

### 3. Connect to your local Rails server

The app's `BASE_URL` is set in `app/src/main/java/com/hurkledurkle/android/MainActivity.kt`.

**Emulator (Android Virtual Device):**

- Use `http://10.0.2.2:3000` — this is Android's loopback alias for your Mac's `localhost`
- This is already the default

**Physical Android device (USB or Wi-Fi):**

1. Find your Mac's local IP: run `ipconfig getifaddr en0` in Terminal
2. Change `BASE_URL` to `http://<your-mac-ip>:3000`
3. Make sure your device and Mac are on the same Wi-Fi network
4. You may need to allow the IP through macOS Firewall: System Settings → Network → Firewall → Options → uncheck "Block all incoming connections"

### 4. Start the Rails server

From the project root:

```bash
bin/dev
```

The server must be running and accessible before launching the Android app.

### 5. Run on emulator

1. In Android Studio, open **Device Manager** (right toolbar or View → Tool Windows → Device Manager)
2. Create a virtual device if none exist: **+** → choose a phone (e.g. Pixel 6) → select a system image (API 33 or 34 recommended) → Finish
3. Click the **▶ Run** button (or Shift+F10) — select your emulator from the device picker
4. The app will open and load `http://10.0.2.2:3000` in the native wrapper

### 6. Run on a physical device

1. Enable Developer Options on your phone: Settings → About Phone → tap **Build Number** 7 times
2. Enable **USB Debugging**: Settings → Developer Options → USB Debugging
3. Plug in via USB — accept the "Allow USB debugging?" prompt on the phone
4. The device will appear in the device picker in Android Studio
5. Click **▶ Run**

### 7. Gradle dependency note

The `hotwire-native-android` library is declared in `app/build.gradle.kts`. On first sync Android Studio will download it from Maven Central. If sync fails, check: File → Settings → Build → Gradle → ensure "Gradle JDK" is set to JDK 17.

---

## Troubleshooting

| Problem                       | Fix                                                                                                                                                                                                   |
| ----------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| App shows "Unable to connect" | Rails server not running, or wrong IP in `BASE_URL`                                                                                                                                                   |
| Emulator can't reach server   | Confirm server is on `0.0.0.0` not just `127.0.0.1` — Rails binds to `localhost` by default for `rails s` but `bin/dev` uses Puma which also binds `::1`; use `bin/rails server -b 0.0.0.0` if needed |
| Gradle sync fails             | Check JDK 17 is selected in Android Studio settings                                                                                                                                                   |
| Physical device not detected  | Revoke and re-grant USB debugging, try a different cable                                                                                                                                              |
