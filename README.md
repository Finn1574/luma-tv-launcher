# Luma TV Launcher

Ein Android-TV-/Fire-TV-Launcher im Stil von Apple tvOS — mit einer **Liquid-Glass-Oberfläche**
in Violett und Blau. Gebaut mit Kotlin und Jetpack Compose, lauffähig ab **Android 6 (API 23)**,
getestet auf einem **Fire TV Stick 4K (Fire OS 7.7.1.6, API 28, armeabi-v7a)**.

![Status](https://img.shields.io/badge/Plattform-Android%20TV%20%7C%20Fire%20OS-8B5CF6)
![Lizenz](https://img.shields.io/badge/Lizenz-MIT-3B82F6)

## Warum nicht einfach Blur?

Echtes Hintergrund-Blur (`RenderEffect`) gibt es erst ab **API 31**. Fire OS 7 ist API 28.
Die Glasoptik entsteht hier deshalb vollständig aus **geschichteten Gradienten**:
halbtransparente Füllung, Innenleuchten von unten, oberes Kantenlicht, farbiger Rand im
App-Akzentton und ein wandernder Glanzstreifen. Ergebnis: Glas-Anmutung ohne Blur-API und
ohne die GPU eines 40-€-Sticks zu überfordern.

## Funktionen

| | |
|---|---|
| **Liquid-Glass-Kacheln** | Mehrschichtiges Glas mit Kantenlicht und wanderndem Glanz |
| **Aurora-Hintergrund** | Vier driftende Farbwolken (Violett/Blau/Magenta/Cyan), atmend animiert |
| **Tageszeit-Stimmung** | Palette wird morgens blauer, abends violetter — automatisch |
| **Fokus-Spotlight** | Ein Lichtkegel im Hintergrund folgt der fokussierten Kachel |
| **3D-Parallaxe** | Kacheln neigen sich in die zuletzt gedrückte D-Pad-Richtung |
| **Farbextraktion** | Akzentfarbe wird per Palette aus dem App-Icon gezogen und ins Luma-Schema harmonisiert |
| **Automatische Reihen** | Favoriten · Zuletzt verwendet · Spiele · Alle Apps |
| **Kontextmenü** | OK lange drücken: Favorit, Ausblenden, App-Info, Deinstallieren |
| **Schnellzugriff** | Menü-Taste: WLAN, Bluetooth, Anzeige, Apps, Amazon-Startseite, Luma-Schalter |
| **Uhr & Wetter** | Open-Meteo + IP-Standort, ohne API-Key, in einer Glaspille |
| **Bildschirmschoner** | Nach 90 s Leerlauf: Aerial-artige Aurora mit wandernder Uhr (Burn-in-Schutz) |
| **Ton-Feedback** | Dezente Fokus-/Auswahl-Töne, abschaltbar |

## Bauen

```bash
git clone https://github.com/Finn1574/luma-tv-launcher.git
cd luma-tv-launcher
mkdir -p keystore
keytool -genkeypair -v -keystore keystore/luma.jks -storepass lumatv -keypass lumatv \
  -alias luma -keyalg RSA -keysize 2048 -validity 10000 -dname "CN=Luma TV, O=bolta, C=DE"
./gradlew :app:assembleRelease
```

APK: `app/build/outputs/apk/release/app-release.apk`

## Installieren (Fire TV)

```bash
adb connect <IP-des-Fire-TV>:5555
adb install -r app/build/outputs/apk/release/app-release.apk
```

Fire OS bindet die Home-Taste fest an Amazons Launcher. Damit die Home-Taste Luma öffnet,
zusätzlich [Home on Fire](https://github.com/toolicious/home-on-fire) installieren und dort
`org.bolta.lumatv` als Ziel-App wählen. Auf Standard-Android-TV genügt das Setzen als
Standard-Home-App.

## Bedienung

| Taste | Wirkung |
|---|---|
| D-Pad | Navigieren (Kacheln neigen sich in Bewegungsrichtung) |
| OK | App starten |
| OK lange | Kontextmenü der App |
| Menü | Schnellzugriff-Panel |
| Zurück | Bleibt auf dem Launcher (Wurzel-Verhalten) |

## Technik

- Kotlin 2.0.21, Jetpack Compose (BOM 2024.10.01), AGP 8.7.3
- `minSdk 23`, `targetSdk 34`, keine Fremd-Netzwerkbibliotheken
- Glas-Renderer: `Modifier.liquidGlass()` in `ui/Glass.kt`
- Hintergrund: `ui/Aurora.kt` (reines Canvas-Zeichnen, keine Bitmaps)
- App-Quelle: `CATEGORY_LEANBACK_LAUNCHER` + `CATEGORY_LAUNCHER`, Icons via `androidx.palette`

## Lizenz

MIT — siehe [LICENSE](LICENSE).
