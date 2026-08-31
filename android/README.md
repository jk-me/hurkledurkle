# Hurkledurkle — Android

A pure-native Android sleep tracker app backed entirely by local SQLite storage.
No account, no server, no network required.

## Features

- Log sleep sessions with four tracking points: **wind-down**, **sleep**, **wake**, **rise**
- Support for **interrupted sleep** (multiple sleep/wake pairs per session)
- Mark sessions as **naps**
- **Sleep Times chart**: line graph of all four tracking points across days
- **Metrics chart**: grouped bar chart for rest time, pre-sleep time, and hurkle-durkle time
- **Configurable settings**: default timezone, bedtime reset time, graph range

## Data storage

All data lives in a Room (SQLite) database on-device at `hurkledurkle.db`. Nothing leaves the phone.

| Table            | Purpose                                       |
| ---------------- | --------------------------------------------- |
| `sleep_sessions` | One row per sleep cycle (wind-down → rise)    |
| `sleep_events`   | Child sleep/wake events for interrupted sleep |

User preferences (timezone, reset time, graph range) are stored in DataStore Preferences.

## Architecture

```
HurkleApplication
│
├── data/
│   ├── local/entity/   — Room entities
│   ├── local/dao/      — DAOs
│   ├── local/db/       — HurkleDatabase
│   ├── model/          — SleepSessionWithEvents, DayMetrics
│   ├── preferences/    — UserPreferences (DataStore)
│   └── repository/     — SleepRepository
│
├── ui/
│   ├── navigation/     — NavGraph (Compose Navigation)
│   ├── screen/
│   │   ├── dashboard/  — DashboardScreen + DashboardViewModel
│   │   ├── log/        — LogSessionScreen + LogSessionViewModel
│   │   └── settings/   — SettingsScreen + SettingsViewModel
│   ├── components/     — SleepLineChart, MetricsChart, ChartLegend
│   └── theme/          — Color, Theme, Type
│
└── util/TimeUtils.kt
```

## Tech stack

- **Kotlin** with coroutines + Flow
- **Jetpack Compose** + Material 3
- **Room 2.6** for SQLite
- **DataStore Preferences** for settings
- **Compose Navigation**
- MVVM with manual DI (no Hilt)
- Min SDK 26 (Android 8.0)

## Build

Open the `android/` folder in Android Studio and click **Run**.

```bash
cd android
./gradlew assembleDebug
```
