# V2BOX Development Status

## Project
V2BOX Android VPN client based on v2rayNG 2.2.6.

## Target Identity
- Application name: V2BOX
- Package name: com.v2box.mobiletina
- Application version: 1

## Migration Progress

### Repository Preparation
- Target repository initialized.
- Development tracking document created.
- GitHub Actions Android build workflow added.

### Source Migration
- Base source: v2rayNG 2.2.6.
- Reference migration source: mtpali/v2rayNG.
- Reference feature source: mtpali/MobileTinaVPN.
- Smart Connect implementation source identified for integration.

### Branding
- Replace original v2rayNG branding with V2BOX branding.
- Replace application icon with provided icon.png.
- Configure adaptive icon scaling to avoid excessive zoom.

### UI
- Implement V2BOX interface matching provided Android and iOS references.
- Support Dark Mode and Light Mode.
- Keep layout direction LTR only.
- Keep only English and Persian languages.

### Home Screen
- Duration display.
- Upload and Download statistics.
- Smart Connect switch.
- Routing entry.
- Replace Telegram Community with Instagram:
  - Username: mobile.tina2

### Config Management
- Automatic sorting by ping enabled by default.
- Subscription grouping.
- Ping display for nodes.

### Smart Connect
- Integrate Smart Connect logic from MobileTinaVPN.
- Run Smart Connect only when enabled by user settings.

### Subscription
- Add automatic subscription update option in Settings.
- Enabled by default.
- Execute update on application startup when subscription URL exists.
- Display remaining traffic and expiration time when available.

### Build
- GitHub Actions build workflow prepared.
- ARMv7 and ARMv8 APK output configuration prepared.

## References
- Base project: v2rayNG 2.2.6
- Previous custom projects:
  - mtpali/v2rayNG
  - mtpali/MobileTinaVPN

## Current Stage
- Migration workspace prepared.
- Source integration in progress.
