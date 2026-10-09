# My Medical History — Flutter Project (VS Code + Offline + Google Drive Backup + APK)

This folder (`flutter_medical_history/`) contains the complete, standalone **Flutter** application implementing the **My Medical History** design system.

## Key Capabilities
1. **100% Offline-First Local Storage**: All vital signs, clinical records, medication adherence logs, and emergency directives are saved automatically on the device filesystem (`StorageAndDriveService`) and work without an internet connection.
2. **Google Drive Backup & Restore**: Export your full clinical database as a portable `.json` backup file directly to **Google Drive** (or restore from Google Drive anytime).
3. **Modern Clinical Calm Design System**:
   - Bundled **Plus Jakarta Sans** & **Inter** fonts (`assets/fonts/`)
   - Deep Teal (`#0D9488`), Oceanic Blue (`#0284C7`), Emerald Green (`#10B981`), Warning Refill Amber (`#F59E0B`), and Critical Emergency Red (`#EF4444`).

---

## How to Run Smoothly in VS Code

1. **Extract the ZIP file** and open the `flutter_medical_history` folder in **VS Code**:
   ```bash
   cd flutter_medical_history
   code .
   ```
2. **Initialize platform folders (if running from a fresh source zip)** and fetch packages:
   ```bash
   flutter create . --project-name my_medical_history
   flutter pub get
   ```
3. **Run the app from the VS Code Terminal**:
   - Connect an Android phone (with USB Debugging enabled) or start an Android Emulator, then run:
   ```bash
   flutter run
   ```

---

## How to Build the Installable `.apk` File for Mobile

To create a smooth, standalone Android `.apk` file that you can copy to any Android phone and install:

1. In the VS Code terminal (inside `flutter_medical_history/`), run:
   ```bash
   flutter build apk --release
   ```
   *(Or for smaller per-ABI APKs: `flutter build apk --split-per-abi --release`)*

2. Once the build finishes, your installable APK file is located at:
   ```text
   build/app/outputs/flutter-apk/app-release.apk
   ```
3. Transfer `app-release.apk` to your mobile phone (via USB or Google Drive) and tap it to install!
