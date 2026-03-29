### QiblaWay2

An Android app that provides:
- **Prayer times** based on the user’s location (coordinates-first), with a live “time left to next prayer” and same-day local caching.
- **Qibla compass** using device sensors with accurate bearing calculation to Mecca.
- **Quran**: list of surahs and a surah details screen.
- **Adhkar (Dua/Azkar)** list.
- **Prayer time calculation settings** (method and school) 
- **Wallpaper selection** and **Language switching** (via `LanguageHelper`, persisted) Arabic,English,Russian,Kazkh

The app uses the Aladhan API (`https://api.aladhan.com/v1`) for prayer times.


### Requirements
- Android Studio (latest stable).
- Android SDK:
  - `minSdk = 24`
  - `targetSdk = 35`
- Kotlin.


### Permissions
Declared in `app/src/main/AndroidManifest.xml`:
- `ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION`: to get the user’s location.
- `INTERNET`, `ACCESS_NETWORK_STATE`: to access the internet and check connectivity.
- Uses device sensor feature: `android.hardware.sensor.compass`.


### Project Structure
- `app/src/main/java/com/example/qiblaway2`
  - `MainActivity.kt`: BottomNavigation setup and NavController wiring.
  - `fragments/`
    - `Home.kt`: Home screen. Fetches prayer times from user location, shows current time and time left to next prayer, displays chosen location, and caches the day’s timings in `SharedPreferences`.
    - `PrayerTimesSettings.kt`: BottomSheet to choose calculation `method` and `school`; saves preferences and notifies Home to refresh.
    - `Qibla.kt`: Qibla compass. Reads sensors and calculates bearing to Mecca.
    - `Quran.kt` and `SurahDetailsFragment.kt`: Surah list and details.
    - `Dua.kt`: Adhkar list.
    - `Language.kt`, `Wallpaper.kt`: language and wallpaper selection.
  - `api/`
    - `RetrofitInstance.kt`: Retrofit setup with `GsonConverterFactory` and Aladhan `baseUrl`.
    - `PrayerTimesApi.kt`: Endpoints definitions:
      - `timingsByCity` (city/country).
      - `timings` (coordinates) — currently the default, for higher reliability.
  - `adapter/`: RecyclerView adapters (Quran, Azkar, wallpapers, methods...).
  - `model/`: Data models (`PrayerTimesResponse`, `Timings`, etc.).
  - `utils/LanguageHelper.kt`: Load/set persisted app language.

- `app/src/main/res/navigation/mobile_navigation.xml`: Navigation graph between screens.
- `app/src/main/assets/`: `quran.json`, `adhkar.json`: local data for Quran and Adhkar.


### Dependencies
Defined in `app/build.gradle.kts`, notably:
- Retrofit and Gson: REST and JSON parsing.
- Google Play Services Location: user location.
- Glide: images and wallpapers.
- AndroidX Navigation, RecyclerView, Material.


### Build & Run
1) Open the project in Android Studio.
2) Let Gradle sync.
3) Run on a physical device or emulator:
   - A physical device is recommended for accurate sensors (compass) and GPS.
4) On first launch:
   - Grant location permission.
   - Ensure internet connectivity.


### How Prayer Times Work
- In `Home.kt`:
  - Requests location permission and retrieves the latest coordinates via `FusedLocationProviderClient`.
  - Uses `Geocoder` only for displaying a human-readable city/country, but calls the API using coordinates to avoid name mismatches.
  - Calls `PrayerTimesApi.getPrayerTimesByCoordinates(latitude, longitude, method, school, date)` where `date` uses `dd-MM-yyyy`.
  - Saves timings in `SharedPreferences` for the current day along with city/country (if available) for offline display.
  - Calculates and updates “time left to next prayer” every minute.


### Calculation Method & Madhhab
- From Home (⋮ menu) open “Prayer Times Settings”.
- Choose calculation method (e.g., MWL, ISNA, Umm Al-Qura...) and school (Shafi/Hanafi).
- Preferences are saved in `SharedPreferences` (`prayer_settings`) with keys:
  - `selected_method_id`
  - `selected_madhhab`
- A `prayer_settings_changed` result is posted to refresh timings in `Home.kt`.




 
This project is educational/personal

