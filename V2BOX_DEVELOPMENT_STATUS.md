# V2BOX Development Status

## راهنمای دانلود در GitHub

1. برای گرفتن همین فایل، در صفحهٔ [V2BOX_DEVELOPMENT_STATUS.md](https://github.com/mtpali/v2box/blob/main/V2BOX_DEVELOPMENT_STATUS.md) روی دکمهٔ **Download raw file** (علامت پیکان رو به پایین کنار **Raw**) بزنید.
2. برای APK در صفحهٔ مخزن، **Releases** را باز کنید و وارد [V2BOX 1 preview](https://github.com/mtpali/v2box/releases/tag/v1-preview) شوید.
3. در بخش **Assets**، برای بیشتر گوشی‌های جدید فایل **V2BOX-1-arm64-v8a.apk** و برای دستگاه‌های ARMv7 فایل **V2BOX-1-armeabi-v7a.apk** را انتخاب کنید. این فایل‌ها APK هستند و نیازی به استخراج ZIP ندارند.

راه جایگزین: در [اجرای موفق GitHub Actions](https://github.com/mtpali/v2box/actions/runs/36222227077)، پایین صفحه زیر **Artifacts** فایل متناظر را بگیرید. برای دریافت Artifacts باید وارد حساب GitHub شوید؛ فایل به شکل ZIP دانلود می‌شود و APK درون آن است.

## Source and repository

- Target: `mtpali/v2box`, implemented on `v2box-development` and merged into `main` via [PR #1](https://github.com/mtpali/v2box/pull/1). Merge commit: `b22a0220ac9cc4360ab46c28432daf0707d0139e`.
- Base: `2dust/v2rayNG` tag `2.2.6`, commit `15b4fff8e45da9bc0acaa5cc1d80a1d3531e8712`; its source and GPL license were imported.
- Reference consulted: `mtpali/MobileTinaVPN` (Smart Connect and subscription metadata behavior). The user also provided `mtpali/v2rayNG` as a previous build reference. V2BOX is implemented on the upstream 2.2.6 base; no binary from the personal references is bundled.
- Pinned AndroidLibXrayLite submodule: `3b5a9c858c4dc98b7079cefb1380537b6b5c155c` (`v26.7.5`); hev tunnel is built from the pinned submodule.

## Implemented in this development branch

- Application ID and Kotlin namespace: `com.v2box.mobiletina`; source and shortcut class names moved accordingly. App label `V2BOX`, `versionName=1`, `versionCode=1`.
- Only English and Persian are exposed. Other app translation directories were removed. Android RTL support is disabled and the window layout direction is set to LTR. Existing light and night themes remain.
- Home/Configs/Settings bottom navigation. Home displays status, connection duration, app UID upload/download counts, Smart Connect toggle, Routing and Instagram `mobile.tina2`. Configs retains original import, list, ping and subscription functions, with Local and subscription tabs.
- Smart Connect uses the real ping service to choose the lowest positive result; it runs only when enabled. An inactive toggle starts the selected config normally. Tests have a 25-second timeout and the operation is cancelable.
- Automatic ping sorting is enabled by default in both the UI and the MMKV read path, with a Configs switch; sorting applies to each subscription and Local.
- Subscription update on app entry is enabled by default and runs on a background dispatcher only if an enabled subscription has a URL. Existing periodic update scheduling is preserved.
- Standard `subscription-userinfo` headers are parsed from the same successful subscription response. Upload, download, total and expiry metadata are stored with the subscription. Remaining traffic, expiration date and days left appear on Home and in Settings when present.
- Instagram tries the installed app for `mobile.tina2`, falling back to its HTTPS profile in a browser. The About and drawer actions point there too.
- GitHub Actions builds signed debug APK artifacts for ARMv7 (`armeabi-v7a`) and ARMv8 (`arm64-v8a`), with pinned core and native build steps. The earlier workflow that ignored build failures was replaced. A CI step checks the installed package, version and native ABI of both artifacts.

## Files to revisit

- `V2rayNG/app/src/main/java/com/v2box/mobiletina/ui/MainActivity.kt`: dashboard, connection mode, startup refresh, traffic and metadata display.
- `V2rayNG/app/src/main/java/com/v2box/mobiletina/handler/V2BoxSubscriptionInfo.kt`, `AngConfigManager.kt`, and `util/HttpUtil.kt`: metadata capture and parsing.
- `V2rayNG/app/src/main/res/layout/activity_main.xml`, `res/xml/pref_settings.xml`, `res/menu/menu_v2box_bottom.xml`: main navigation and settings.
- `.github/workflows/android-build.yml`: reproducible debug artifact build.

## Verification and limitations

- Resource XML parsed successfully and `git diff --check` passed locally. The local environment cannot reach the Gradle distribution, so compilation was checked in GitHub Actions.
- The first development run succeeded: [run 36220594488](https://github.com/mtpali/v2box/actions/runs/36220594488). The final source run also succeeded: [run 36220945310](https://github.com/mtpali/v2box/actions/runs/36220945310), commit `ac335f09dac5498d7b9ec71ddaf934d4bb8b567e`. Its `aapt dump badging` checks confirmed package `com.v2box.mobiletina`, version code `1`, version name `1` in both APKs, and the matching native Xray library for each ABI.
- Final ARMv7 artifact: [V2BOX-1-armeabi-v7a](https://github.com/mtpali/v2box/actions/runs/36220945310/artifacts/10898547923). Extracted APK SHA-256: `ffbd43410b1083cb66ee9d6893b4a75bfe367e7d8fd16cb6e296099d32090f68` (34,344,727 bytes).
- Final ARMv8 artifact: [V2BOX-1-arm64-v8a](https://github.com/mtpali/v2box/actions/runs/36220945310/artifacts/10898458060). Extracted APK SHA-256: `c1e1b316d71a7067915f0ea9316d6cc76988eac035fbcaaf9511698f03dccb36` (33,870,823 bytes).
- Both extracted APKs were inspected for the expected sole native ABI, `libgojni.so`, `classes.dex`, and an APK signing block. No device/emulator interaction or screenshot comparison was available.
- The identical source tree on `main` also passed the full APK and metadata checks: [main run 36221357502](https://github.com/mtpali/v2box/actions/runs/36221357502).
- The previously uploaded `icon.png` and ten UI screenshots are not available as files in this Codex workspace. A centered V2BOX vector placeholder avoids showing the upstream icon; replace it with the exact supplied image once it is attached here. Pixel alignment with the screenshot references is therefore not verified.
- Home upload/download numbers use Android app UID counters from the current connection session. These include app network traffic and are an approximation of tunnel usage; provider traffic quota uses subscription headers.
- The APKs are available in the public [V2BOX 1 preview release](https://github.com/mtpali/v2box/releases/tag/v1-preview), together with `SHA256SUMS`. This release was built and verified in [main run 36222227077](https://github.com/mtpali/v2box/actions/runs/36222227077). Actions artifacts for that run expire on 2026-12-25, but the release assets provide persistent downloads. Both APKs are debug signed; a persistent release keystore is needed for upgrades across runs without reinstalling.
