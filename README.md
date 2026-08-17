# NetDown

**All-in-one Video Downloader** for Android — powered by [yt-dlp](https://github.com/yt-dlp/yt-dlp) and FFmpeg.

NetDown lets you download videos and audio from **1000+ websites** (YouTube, Facebook, TikTok, Instagram, X/Twitter and many more) with a clean Material 3 interface, built with Jetpack Compose.

> Only download content you have the right to download. Respect each platform's terms of service and copyright.

## Features

- **1000+ supported sites** via the yt-dlp engine
- **Best quality** one-tap download (video + audio merged with FFmpeg)
- **MP3 audio extraction** from any video
- **Format picker** — choose resolution / quality before downloading
- **Concurrent downloads** (1–3) with per-file progress, speed and ETA
- **Cancel / retry** any download anytime
- **Download history** with file size, open (via FileProvider) and delete
- **Share-to-download** — paste or share a link from any app
- **In-app engine update** — update yt-dlp when a site breaks, without reinstalling
- **Start.io ads** — banner on Home, interstitial on download start
- Material 3 UI with light & dark themes

## Screenshots

*(Add screenshots here)*

## Requirements

- Android **8.0 (API 26)** or higher
- Internet connection (for fetching and downloading)
- ~200 MB free storage (video files)

## Download Location

Downloads are saved to:

```
Internal Storage / Download / NetDown/
```

## Tech Stack

- **Kotlin** 1.9.24
- **Jetpack Compose** (Material 3, BOM 2024.06)
- **[youtubedl-android](https://github.com/yausername/youtubedl-android)** 0.18.1 (yt-dlp + FFmpeg bundled as native libraries)
- **Start.io** (StartApp) in-app SDK 5.1.0 — banner & interstitial ads
- Coil (images), Gson (JSON), Navigation Compose, Coroutines
- Supported ABIs: `armeabi-v7a`, `arm64-v8a`, `x86`, `x86_64`

## Building

### With Android Studio

1. Clone the repository:
   ```bash
   git clone https://github.com/abdullahgalib21/NetDown.git
   ```
2. Open the project in Android Studio (or IntelliJ IDEA with the Android plugin).
3. Let Gradle sync download the dependencies.
4. Build & run:
   ```bash
   ./gradlew assembleDebug
   ```

### With AndroidIDE (on your phone)

1. Open the project folder directly in [AndroidIDE](https://androidide.com/).
2. The Gradle wrapper is preconfigured (Gradle 8.6, AGP 8.2.2).
3. Build the debug APK from the IDE.

### Build requirements

- JDK 17
- Android SDK 34 (compileSdk), minSdk 26, targetSdk 34

## Configuration

### Start.io ads

The app ID is configured in `AdManager.kt`:

```kotlin
const val START_IO_APP_ID = "207922512"
```

To change it, replace the value with your own Start.io app ID and rebuild.

### app-ads.txt

`app-ads.txt` (with the full seller list) is included in the repository. For programmatic fill, **host it at the root of your website domain** and verify the domain in the Start.io portal:

```
https://your-domain.com/app-ads.txt
```

## How it works

1. Paste or share a video URL on the Home screen.
2. NetDown calls yt-dlp to fetch media information (formats, thumbnail, duration).
3. Choose a format (Best Quality, MP3, or a specific resolution).
4. The download runs in the background with a real progress bar, speed and ETA.
5. Completed files appear in Downloads / History and can be opened or shared.

If a site stops working, open **Settings → Download engine → Update** to update yt-dlp to the latest version from within the app.

## Project Structure

```
app/src/main/java/com/net/down/
├── MainActivity.kt            # Entry point, share-intent handling
├── NetDownApp.kt              # Application: engine + SDK initialization
├── ads/                       # Start.io ad manager
├── data/                      # Settings & history repositories, models
├── download/                  # yt-dlp engine + download queue engine
├── ui/
│   ├── components/            # Reusable Compose components
│   ├── screens/               # Home, Downloads, History, Settings
│   ├── theme/                 # Material 3 theme
│   └── viewmodel/             # ViewModels per screen
└── util/                      # Formatters & helpers
```

## License

This project is licensed under the **Apache License 2.0**. See [LICENSE](LICENSE).

The bundled download engine (yt-dlp / youtubedl-android) is licensed under **GPL-3.0**. When redistributing, please review the applicable license terms for the bundled libraries.

## Disclaimer

- NetDown is not affiliated with YouTube, TikTok, Instagram, Facebook or any other content platform.
- It does not bypass DRM, paywalls or login requirements.
- You are responsible for complying with the laws of your country and the terms of service of the websites you download from.
