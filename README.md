# Achtung

Achtung is an Android application for improving German pronunciation and translation. It leverages Text-to-Speech (TTS) and on-device machine learning to provide a seamless learning experience.

---

## Features

- **Text-to-Speech (TTS)**: Enter German text to hear it pronounced correctly.
- **Voice Selection**: Choose from a list of available German voices on your device.
- **Speech Rate Control**: Adjust the speed of the pronunciation to better understand the nuances of the language.
- **Translation**: Translate German text to English using on-device machine learning.
- **Modern UI**: A clean and intuitive user interface built with Jetpack Compose and Material Design 3.

---

## Architecture

The application follows the MVVM (Model-View-ViewModel) architecture pattern, which separates the UI from the business logic.

- **View**: The UI is built with Jetpack Compose and is located in `MainActivity.kt`. It observes the state of the `TranslatorViewModel` and displays the UI accordingly.
- **ViewModel**: The `TranslatorViewModel.kt` is responsible for holding and managing the UI-related data. It exposes the state of the UI and the translation results to the View.
- **Model**: The `MlKitTranslator.kt` is responsible for the translation logic. It uses Google's ML Kit to translate German text to English.

---

## Project Structure

```
/app/src/main/java/com/musno/achtung/
├── MainActivity.kt         # Main activity, contains the Jetpack Compose UI
├── MlKitTranslator.kt      # Handles translation using ML Kit
├── TranslatorViewModel.kt  # ViewModel for the translator screen
└── ui/
    └── theme/              # Jetpack Compose theme files
```

---

## Technology Stack

- **Kotlin**: The primary programming language for the application.
- **Jetpack Compose**: For building the user interface.
- **Material Design 3**: For the UI design.
- **Google ML Kit**: For on-device translation.
- **Android Text-to-Speech (TTS)**: For pronouncing the German text.
- **Kotlin Coroutines**: For asynchronous programming.
- **StateFlow**: For managing the state of the UI.

---

## Getting Started

1. Clone this repository:
   ```shell
   git clone https://github.com/Musn0o/Achtung.git
   ```
2. Open the project in Android Studio.
3. Build and run on an Android device or emulator (make sure German TTS data is installed).

---

## Contributing

Pull requests are welcome. For major changes, please open an issue first to discuss what you'd like to change or add.