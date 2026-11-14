## Description

Provide a clear and concise description of the changes introduced by this pull request.

## Type of Change

- [ ] Bug fix (non-breaking change fixing an issue)
- [ ] New feature (non-breaking change adding functionality)
- [ ] Documentation update
- [ ] Code style / Refactor (no behavioral changes)
- [ ] Build / CI pipeline update

## Testing Checklist

- [ ] Project compiles locally (`./gradlew assembleDebug`)
- [ ] Tested on a physical device or emulator running Android 7.0+
- [ ] Verified that `android.permission.INTERNET` remains absent from `AndroidManifest.xml`
- [ ] Memory footprint verified (no memory leaks on recycled `AccessibilityNodeInfo`)
