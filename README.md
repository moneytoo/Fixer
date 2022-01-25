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

### Shortcut: Radio (Device info)
Description: Set often unavailable network modes like "LTE only" or "LTE/WCDMA" (4G/3G without 2G)<br>
Requirements: None<br>
Notes: Because Samsung blocks secret codes (``*#*#4646#*#*``)...

#### Since version 0.3

### Auto select client certificate
Description: <br>
Requirements: Accessibility<br>
Notes: Alternative to ``DeviceAdminReceiver.onChoosePrivateKeyAlias()``

### Long press volume controls
Description: Long press volume down for ``PLAY/PAUSE``; long press volume up for ``NEXT`` (when music is playing) or flashlight (when no music is playing)<br>
Requirements: SET_VOLUME_KEY_LONG_PRESS_LISTENER permission<br>
Notes: Based on: https://github.com/Cilenco/skipTrackLongPressVolume

### OnePlus: Alert slider actions
Description: Switch slider to Silent mode to enable flashlight<br>
Requirements: <br>
Notes: Observes global settings ``three_Key_mode`` key. Flashlight is an extra functionality, phone is in actual Silent mode. 
