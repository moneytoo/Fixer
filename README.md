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

2. Enable "Display over other apps" permission in "App info"

3. Enable Accessibility

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
Notes:

#### Since version 0.3

### App backup
Description: Automatic backup all installed/updated apps (apks)<br>
Requirements: None<br>
Notes: Uses location ``Android/data/com.brouken.fixer/files/``

### Auto select client certificate
Description: <br>
Requirements: Accessibility<br>
Notes: Alternative to ``DeviceAdminReceiver.onChoosePrivateKeyAlias()``

### Long press volume controls
Description: <br>
Requirements: SET_VOLUME_KEY_LONG_PRESS_LISTENER permission<br>
Notes: Long press volume down for ``PLAY/PAUSE``; long press volume up for ``NEXT`` (when music is playing) or flashlight (when no music is playing). Based on: https://github.com/Cilenco/skipTrackLongPressVolume

### Keep screen off on power events
Description: <br>
Requirements: Accessibility<br>
Notes: Inspired by https://gitlab.com/mudar-ca/SnooZy, requires Android P: [GLOBAL_ACTION_LOCK_SCREEN](https://developer.android.com/reference/android/accessibilityservice/AccessibilityService.html#GLOBAL_ACTION_LOCK_SCREEN)  

### OnePlus: Call recording
Description: <br>
Requirements: WRITE_SECURE_SETTINGS permission<br>
Notes: Uses ``op_voice_recording_supported_by_mcc`` 
