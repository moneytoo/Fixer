# Fixer

Fixer a suite of tweaks that works around some features I find annoying.

**Why Fixer?**
*    No ads or tracking
*    It's free and open; Tasker is paid
*    Setting up Tasker can be exhausting and non trivial
*    Rework and improvement of existing solutions for maximum efficiency
*    All-in-1: Integrating all tweaks to one app is more efficient, single running service
*    Opportunity for me to learn more and be creative in finding work arounds

## Download

Get [latest apk](https://github.com/moneytoo/Fixer/releases/latest) from the release section.

## Setup

1. Run following command on your computer with Android device connected. This one time setup requires enabled Developer mode & USB debugging. <br> ``adb shell pm grant com.brouken.fixer android.permission.WRITE_SECURE_SETTINGS`` <br> ``adb shell pm grant com.brouken.fixer android.permission.SET_VOLUME_KEY_LONG_PRESS_LISTENER``

2. Enable Accessibility

WARNING: There are no checks for required permissions or runtime applying of changes so some features may require restart of a service or a device.

## Features

### No safe volume warning
Description: <br>
Requirements: WRITE_SECURE_SETTINGS permission<br>
Notes: Based on Tasker guide https://www.xda-developers.com/how-to-automatically-disable-the-high-volume-warning-without-root/

### Hacker's Keyboard only in Termux
Description: Use Gboard as default IME, switch to Hacker's Keyboard in Termux<br>
Requirements: Accessibility and WRITE_SECURE_SETTINGS permission<br>
Notes: Based on Tasker guide https://www.xda-developers.com/how-to-automatically-change-your-keyboard-on-a-per-app-basis/

### Shortcut: Radio (Device info)
Description: Set often unavailable network modes like "LTE only" or "LTE/WCDMA" (4G/3G without 2G)<br>
Requirements: None<br>
Notes: Because Samsung blocks secret codes (``*#*#4646#*#*``)...

#### Since version 0.2

### Side screen gestures
Description: Pie control without actual pie. Swipe from center of a left or right side towards top/center/bottom for recents/home/back<br>
Requirements: Accessibility<br>
Notes/Todo: Consider actual pie UI, base iton PieController from [Paranoid Android](https://github.com/AOSPA/android_frameworks_base/tree/85bab89a8f92f85d210f0c29601cf3b1b2a5225a/packages/SystemUI/src/com/android/systemui/statusbar/pie)

#### Since version 0.3

### App backup
Description: Automatic backup all installed/updated apps (apks) to SD card<br>
Requirements: SD card<br>
Notes: Creates ``apk`` folder on SD card (removable only - for now)

### Auto select client certificate
Description: <br>
Requirements: Accessibility<br>
Notes: Alternative to ``DeviceAdminReceiver.onChoosePrivateKeyAlias()``

### Long press volume controls
Description: <br>
Requirements: SET_VOLUME_KEY_LONG_PRESS_LISTENER permission<br>
Notes: Long press volume down for ``PLAY/PAUSE``; long press volume up for ``NEXT`` (when music is playing) or flashlight (when no music is playing). Based on: https://github.com/Cilenco/skipTrackLongPressVolume

## ToDo

*    Disable VoLTE icon - ``adb shell settings put secure icon_blacklist ims_volte,rotate,headset``
*    Airplane mode switches only cellular network - ``adb shell settings put global airplane_mode_radios "cell"``
*    (?) Compact volume panel - something like [Noyze](https://forum.xda-developers.com/android/apps-games/app-noyze-volume-panel-replacement-t2875501)

#### When running Android P
*    Keep screen off on power events (vendor universal) - base it on https://github.com/mudar/SnooZy, requires Android P: [GLOBAL_ACTION_LOCK_SCREEN](https://developer.android.com/reference/android/accessibilityservice/AccessibilityService.html#GLOBAL_ACTION_LOCK_SCREEN)
*    Disable lock screen on wifi - base it on http://forum.joaoapps.com/index.php?resources/disable-and-enable-your-lock-screen-at-will-no-root.237/, requires Android P: [GLOBAL_ACTION_LOCK_SCREEN](https://developer.android.com/reference/android/accessibilityservice/AccessibilityService.html#GLOBAL_ACTION_LOCK_SCREEN)
