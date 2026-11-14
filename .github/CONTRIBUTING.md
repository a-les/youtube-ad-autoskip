# Contributing to yt-ad-autoskip

Thank you for your interest in contributing to **yt-ad-autoskip**. This document outlines the development workflow, code conventions, and guidelines for submitting pull requests.

---

## Code of Conduct

All contributors and participants are expected to adhere to our [Code of Conduct](CODE_OF_CONDUCT.md).

---

## Development Setup

### Requirements

- **Android Studio:** Hedgehog (2023.1.1) or newer.
- **JDK:** OpenJDK 17.
- **Android SDK:** Platform 34, Build-Tools 34.0.0.

### Getting the Code

1. Fork the repository on GitHub.
2. Clone your fork locally:
   ```bash
   git clone https://github.com/<your-username>/youtube-ad-autoskip.git
   cd youtube-ad-autoskip
   ```
3. Open the project directory in Android Studio. Gradle will synchronize and download necessary dependencies automatically.

---

## Architecture Principles

1. **Zero Network Footprint:**  
   The application must **never** declare `android.permission.INTERNET` or execute network requests. Any PR introducing network activity or analytics telemetry will be rejected.

2. **Minimal Resource Consumption:**  
   `YouTubeAutoSkipService` must remain strictly event-driven. Do not introduce polling loops, `Thread.sleep()`, or persistent CPU Wakelocks.

3. **Defensive Accessibility Node Handling:**  
   Always call `recycle()` on `AccessibilityNodeInfo` objects to prevent memory leaks in the Android Accessibility client process.

---

## Code Style & Formatting

- Follow official [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html) and [Android Kotlin Style Guide](https://developer.android.com/kotlin/style-guide).
- Use 4 spaces for indentation.
- Maintain camelCase naming for variables/functions and PascalCase for classes.

---

## Submitting Pull Requests

1. Create a dedicated branch for your change:
   ```bash
   git checkout -b feature/your-feature-name
   ```
2. Verify that the debug build compiles cleanly:
   ```bash
   ./gradlew assembleDebug
   ```
3. Commit your changes with descriptive messages:
   ```bash
   git commit -m "feat: enhance node traversal for landscape player layouts"
   ```
4. Push to your fork and submit a Pull Request against the `main` branch.
5. Ensure your PR description clearly states the problem addressed and how the change was tested.
