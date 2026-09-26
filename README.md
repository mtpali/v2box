# V2BOX for Android

V2BOX is an Android VPN client built from the [v2rayNG 2.2.6 release](https://github.com/2dust/v2rayNG/releases/tag/2.2.6), with the package `com.v2box.mobiletina` and app version `1`. The original upstream license is retained in [LICENSE](LICENSE).

## Build

The [Android workflow](.github/workflows/android-build.yml) builds installable, debug-signed APKs for `armeabi-v7a` and `arm64-v8a`. Open a run in GitHub Actions and download its two named artifacts. Each run uses a fresh debug signing key; uninstalling a previous build may be required before installing a new run.

The workflow checks out the two upstream submodules, compiles the hev tunnel with Android NDK 29, downloads the pinned `libv2ray.aar` from AndroidLibXrayLite v26.7.5, then runs `:app:assemblePlaystoreDebug`. The Gradle project lives in `V2rayNG/`.

## Features

- English and Persian; left-to-right layout and system-aware light/dark themes.
- Home screen with connection state, duration, app traffic counters, routing shortcut and Instagram `mobile.tina2`.
- Smart Connect tests imported servers with the upstream real-ping service and selects the lowest positive latency. The toggle controls whether connecting uses this method.
- Configs grouped by Local and subscription, with ping values and automatic ping sorting enabled by default.
- Enabled subscriptions update on launch by default; remaining traffic and expiration show when the provider includes a `subscription-userinfo` header.

See [V2BOX_DEVELOPMENT_STATUS.md](V2BOX_DEVELOPMENT_STATUS.md) for provenance, changes, test results and remaining work.
