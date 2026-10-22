<div align="center">

# ProxyStar

**Discover better connections. Test. Connect.**

An Android app for discovering proxy servers, checking connectivity, and comparing connections using network measurements.

[Download APK](https://github.com/DevQissy/ProxyStar/releases/latest) · [Telegram Channel](https://t.me/Proxystar_Channel) · [Report an Issue](https://github.com/DevQissy/ProxyStar/issues)

</div>

---

## Overview

ProxyStar collects proxy entries from configured public sources and helps evaluate their availability and latency from your current network. The goal is to make it easier to compare available connections using measured results rather than relying on a static list.

## Features

- **Proxy discovery** — collects entries from configured public sources.
- **Connectivity checks** — checks whether discovered servers are reachable.
- **Ping measurement** — measures response time from your current network.
- **Quality-based sorting** — helps compare entries using test results.
- **Network-aware comparison** — results can differ by region, operator, routing, and changing network conditions.
- **Large server lists** — designed to process large collections of proxy entries when source data is available.
- **Telegram updates** — follow the channel for project news and automated reports.

Test results are snapshots, not guarantees. A reachable server or low ping does not guarantee that every app or service will work through that proxy.

## Download and install

1. Open the [latest ProxyStar release](https://github.com/DevQissy/ProxyStar/releases/latest).
2. Download the APK attached to the release.
3. Open the downloaded file and follow Android's installation prompts.

### Important installation notice

ProxyStar is distributed outside Google Play. Depending on the device, Android or Google Play Protect may show a warning for an APK that is not distributed through Google Play or is not recognized by the device.

Only obtain the APK from the official [ProxyStar Releases page](https://github.com/DevQissy/ProxyStar/releases/latest). Review any warning carefully and do not proceed if you cannot verify the file's origin. Do not disable device security protections just to install an app. The wording and availability of installation options vary by Android version and device; a warning is not guaranteed to appear on every installation.

## Technical information

| Item | Value |
|---|---|
| Application ID | `com.devqissy.proxystar` |
| Current project version | `1.0` |
| Version code | `1` |
| Minimum Android version | Android 7.0 (API 24) |
| Compile SDK | Android API 37, minor API level 1 |
| Target SDK | Android API 37 |
| Language / UI | Kotlin, Jetpack Compose |
| HTTP client | OkHttp 5.5.0 |
| Preferences storage | AndroidX DataStore Preferences 1.2.1 |

These values reflect the Gradle configuration provided for this project and may change in later releases.

## Privacy and safe use

ProxyStar performs network checks to evaluate proxy availability and latency. Results depend on the device's active connection and the remote servers being tested.

Using a proxy does not automatically encrypt all traffic, guarantee anonymity, or make an untrusted server safe. Avoid sending passwords, payment details, or other sensitive information through proxy servers you do not trust.

For details about the app's actual data collection and handling, review the current implementation and any privacy information supplied with the release. This README is not a substitute for a full privacy or security audit.

## Updates and support

- **Repository:** https://github.com/DevQissy/ProxyStar
- **Latest APK:** https://github.com/DevQissy/ProxyStar/releases/latest
- **All releases:** https://github.com/DevQissy/ProxyStar/releases
- **Telegram:** https://t.me/Proxystar_Channel
- **Issues:** https://github.com/DevQissy/ProxyStar/issues

## License

ProxyStar's original code and assets owned by Dev Qissy are proprietary unless a file explicitly states otherwise. Public repository access permits viewing and inspection only; it does not grant permission to copy, modify, redistribute, or create derivative works from proprietary material.

Third-party components remain subject to their own licenses. See [LICENSE](LICENSE) and [THIRD_PARTY_NOTICES.md](THIRD_PARTY_NOTICES.md).

---

<div align="center">

Made by **Dev Qissy**

</div>
