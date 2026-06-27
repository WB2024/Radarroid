# Radarroid

A native Android TV client for [Radarr](https://radarr.video/) — manage your movie library from your couch.

![Android TV](https://img.shields.io/badge/Platform-Android%20TV-brightgreen?logo=android)
![API](https://img.shields.io/badge/Radarr-v3%20API-blue)
![Min SDK](https://img.shields.io/badge/Min%20SDK-API%2021-orange)
![License](https://img.shields.io/badge/License-MIT-lightgrey)

---

## Screenshots

> Browse your library, search for new movies, monitor downloads, and manage your Radarr instance — all with D-pad navigation designed for the big screen.

---

## Features

- **Movies** — Browse your full library with poster art, quality badges, and monitored status at a glance
- **Movie Detail** — Full metadata, cast info, file details, download queue progress, history, and interactive release search with one-click grab
- **Discover** — Search for movies via TMDB; add them directly to Radarr or jump straight to an already-added title
- **Calendar** — See upcoming and recent releases in a date-based view
- **Activity** — Live download queue with progress bars and history log
- **Wanted** — Missing and cut-off unmet movies in one place
- **Collections** — Browse your Radarr collections
- **Settings** — Quality profiles, indexers, download clients, root folders, media management, naming, tags, custom formats, import lists, notifications, and host config
- **System** — Radarr status, logs, tasks, and update management

---

## Requirements

| Requirement | Detail |
|---|---|
| Android TV | API 21+ (Android 5.0 Lollipop) |
| Radarr | v3 or later (self-hosted) |
| Network | Same LAN or VPN access to your Radarr instance |

---

## Setup

1. Install the APK on your Android TV device (sideload via ADB or file manager)
2. Open **Radarroid** — you'll be greeted by the setup screen
3. Enter your Radarr server URL, e.g. `http://192.168.1.100:7878`
4. **Authentication:**
   - If Radarr has no authentication: leave username and password blank
   - If `Authentication Required` is set to `Disabled For Local Addresses`: leave blank (works automatically on your LAN)
   - If using Forms authentication: enter your Radarr username and password
5. Tap **Connect** — the app fetches your API key automatically and you're in

> No manual API key entry needed. The app handles authentication and key retrieval for you.

---

## Authentication

Radarroid supports all Radarr authentication modes:

| Radarr Mode | How to connect |
|---|---|
| None | Leave username and password blank |
| Disabled For Local Addresses | Leave username and password blank (works from LAN) |
| Forms | Enter your Radarr username and password |

---

## Building from Source

```bash
# Clone the repo
git clone https://github.com/WB2024/Radarroid.git
cd Radarroid

# Build a debug APK
./gradlew assembleDebug

# The APK will be at:
# app/build/outputs/apk/debug/app-debug.apk
```

**Requirements:** Android Studio Hedgehog or later, JDK 17+, Android SDK with API 36.

To install directly to a connected device or emulator:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

## Tech Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose with Material3
- **Navigation:** Jetpack Navigation Compose
- **Networking:** OkHttp
- **JSON:** Gson + org.json
- **Image loading:** Coil
- **Min SDK:** API 21 | **Target SDK:** API 36
- **Build:** AGP 8.x, Kotlin 2.0

---

## Project Structure

```
app/src/main/java/com/radarrtv/androidtv/
├── data/
│   ├── api/model/        # Radarr API data models
│   ├── auth/             # Authentication (session + API key extraction)
│   ├── preferences/      # Persistent server config
│   └── repository/       # All Radarr API calls
├── ui/
│   ├── components/       # Reusable TV-focused Compose components
│   ├── navigation/       # NavHost and route definitions
│   ├── screens/          # One file per screen
│   └── theme/            # Colours, typography, shapes
└── MainActivity.kt
```

---

## Related

- [Sonarrdoid](https://github.com/WB2024/Sonarrdoid) — the companion app for Sonarr (TV series)
- [Radarr](https://radarr.video/) — the movie collection manager this app connects to

---

## Disclaimer

This is an unofficial third-party client. It is not affiliated with or endorsed by the Radarr project.
