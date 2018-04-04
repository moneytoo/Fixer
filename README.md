# Fixer

##Setup

1. ``adb shell pm grant com.brouken.fixer android.permission.WRITE_SECURE_SETTINGS`` (or allow root access)
2. Enable Accessibility

##Features

###No safe volume warning
Description: <br>
Requirements: WRITE_SECURE_SETTINGS permission or root<br>
Notes: Based on Tasker guide https://www.xda-developers.com/how-to-automatically-disable-the-high-volume-warning-without-root/

###Media volume by default
Description: <br>
Requirements: Accessibility<br>
Notes: Based on https://github.com/KrongKrongPadakPadak/mvo; Unsure Android P compatibility because of the use of non-public API however P already has media as default stream

###Hacker's Keyboard only in Termux
Description: Use Gboard as default IME, switch to Hacker's Keyboard in Termux<br>
Requirements: Accessibility and (WRITE_SECURE_SETTINGS permission or root)<br>
Notes: Based on Tasker guide https://www.xda-developers.com/how-to-automatically-change-your-keyboard-on-a-per-app-basis/ |

...