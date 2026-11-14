# Security Policy

## Supported Versions

| Version | Supported |
|---|---|
| `v1.0.x` | ✅ Yes |
| `< v1.0` | ❌ No |

---

## Security Architecture

`yt-ad-autoskip` is developed under a strict offline, zero-trust model:

- **No Network Capabilities:** The application does not declare or request `android.permission.INTERNET`. It cannot establish socket connections, transmit data, or exfiltrate on-screen information.
- **Package-Scoped Accessibility:** The service configuration strictly filters events to `com.google.android.youtube` and `com.google.android.apps.youtube.music`. Interactions with other installed applications are excluded at the operating system level.
- **Local Storage:** Metrics (advertisement skip counters and timestamps) are kept solely within private internal application storage (`SharedPreferences`).

---

## Reporting a Vulnerability

If you discover a security vulnerability or unexpected privilege behavior:

1. **Do not** disclose the issue publicly via GitHub Issues or discussions.
2. Please open a [GitHub Private Vulnerability Report](https://github.com/a-les/youtube-ad-autoskip/security/advisories/new) or contact the maintainers directly through GitHub.
3. Include detailed reproduction steps and device environment specifics (Android version, device model).

We will review reports promptly and publish patches via release advisories.
