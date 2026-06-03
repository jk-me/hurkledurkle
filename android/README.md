# Hurkledurkle — Android Wrapper

Hotwire Native Android wrapper for the Hurkledurkle Rails app.

## Setup

1. Open this `android/` folder in Android Studio
2. Update `BASE_URL` in `MainActivity.kt`:
   - Development: `http://10.0.2.2:3000` (emulator) or your machine's LAN IP (physical device)
   - Production: your deployed Rails URL
3. Run the Rails server: `bin/dev` from the project root
4. Run the Android app from Android Studio

## Requirements

- Android Studio Hedgehog or later
- Android SDK 26+
- Rails server running and accessible from the device/emulator
