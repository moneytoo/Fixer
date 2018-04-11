# Fixer

Fixer a suite of tweaks that works around some features I find annoying.

**Why Fixer?**
*    No ads or tracking
*    It's free and open; Tasker is paid
*    Setting up Tasker can be exhausting and non trivial
*    Rework and improvement of existing solutions for maximum efficiency
*    All-in-1: Integrating all tweaks to one app is more efficient, single running service
*    Opportunity for me to learn more and be creative in finding work arounds

## Setup

Root access (or Xposed) is not required, it's just an alternative.

1. Run following command on your computer with Android device connected. This one time setup requires enabled Developer mode & USB debugging. If you're rooted, granting this permission is not necessary. <br> ``adb shell pm grant com.brouken.fixer android.permission.WRITE_SECURE_SETTINGS``
2. Enable Accessibility

WARNING: There are no checks for required permissions or runtime applying of changes so some features may require restart of a service or a device.

## Features

### No safe volume warning
Description: <br>
Requirements: WRITE_SECURE_SETTINGS permission or root<br>
Notes: Based on Tasker guide https://www.xda-developers.com/how-to-automatically-disable-the-high-volume-warning-without-root/

### Media volume by default
Description: <br>
Requirements: Accessibility<br>
Notes: Based on https://github.com/KrongKrongPadakPadak/mvo; Unsure Android P compatibility because of the use of non-public API however P already has media as default stream

### Hacker's Keyboard only in Termux
Description: Use Gboard as default IME, switch to Hacker's Keyboard in Termux<br>
Requirements: Accessibility and (WRITE_SECURE_SETTINGS permission or root)<br>
Notes: Based on Tasker guide https://www.xda-developers.com/how-to-automatically-change-your-keyboard-on-a-per-app-basis/

### No location dialog in GM
Description: No more "To continue, let your device turn on location using Google's Location Service" in Google Maps<br>
Requirements: Accessibility<br>
Notes: Simulates tapping Cancel button. It uses resource id instead of text so it doesn't depend on interface language.

### Samsung: No LED in DnD
Description: Disable LED in Do not disturb mode<br>
Requirements: None (APK is already set to ``targetSdkVersion 22`` to allow system automatically granting WRITE_SETTINGS)<br>
Notes: Based on Tasker plugin: https://www.apkmonk.com/app/com.tinyroar.galaxys3ledcontroller/

### Samsung: No BT/WiFi popups
Description: <br>
Requirements: Accessibility<br>
Notes:

### Shortcut: SIP settings
Description: Brings up system SIP/VoIP configuration screen that is often hidden but functional<br>
Requirements: None<br>
Notes:

### Shortcut: Radio (Device info)
Description: Set often unavailable network modes like "LTE only" or "LTE/WCDMA" (4G/3G without 2G)<br>
Requirements: None<br>
Notes: Because Samsung blocks secret codes (``*#*#4646#*#*``)...

#### Since version 0.2

### Side screen gestures
Description: Pie control without actual pie. Swipe from center of a left or right side towards top/center/bottom for recents/home/back<br>
Requirements: Accessibility<br>
Notes/Todo: Consider actual pie UI, base iton PieController from [Paranoid Android](https://github.com/AOSPA/android_frameworks_base/tree/85bab89a8f92f85d210f0c29601cf3b1b2a5225a/packages/SystemUI/src/com/android/systemui/statusbar/pie)

## ToDo

*    Samsung: DNS changer - base it on Adhell
*    Samsung: Keys light - ``button_key_light``
*    Samsung: Flashlight - ``torchlight_enable`` & ``torchlight_timeout``, https://play.google.com/store/apps/details?id=com.softdx.volumetorchlight
*    Disable VoLTE icon - ``icon_blacklist=ims_volte``
*    (?) App disabler/enabler - [DevicePolicyManager.setPackagesSuspended()](https://developer.android.com/reference/android/app/admin/DevicePolicyManager.html#setPackagesSuspended(android.content.ComponentName,%20java.lang.String[],%20boolean))
*    (?) Compact volume panel - something like [Noyze](https://forum.xda-developers.com/android/apps-games/app-noyze-volume-panel-replacement-t2875501)

#### When running Android O
*    Long press volume controls - based on https://github.com/Cilenco/skipTrackLongPressVolume, requires Android Oreo and root (need to be "systemized", [App Systemizer for Magisk](https://forum.xda-developers.com/apps/magisk/module-app-systemizer-t3477512) works fine)

#### When running Android P
*    Keep screen off on power events - base it on https://github.com/mudar/SnooZy, requires Android P: [GLOBAL_ACTION_LOCK_SCREEN](https://developer.android.com/reference/android/accessibilityservice/AccessibilityService.html#GLOBAL_ACTION_LOCK_SCREEN)
*    Disable lock screen on wifi - base it on http://forum.joaoapps.com/index.php?resources/disable-and-enable-your-lock-screen-at-will-no-root.237/, requires Android P: [GLOBAL_ACTION_LOCK_SCREEN](https://developer.android.com/reference/android/accessibilityservice/AccessibilityService.html#GLOBAL_ACTION_LOCK_SCREEN)

#### When upgrading from S7
*    Fingerprint gestures - [FingerprintGestureController](https://developer.android.com/reference/android/accessibilityservice/FingerprintGestureController.html)
*    Samsung: Remap Bixby button - something like https://play.google.com/store/apps/details?id=com.jamworks.bxactions
