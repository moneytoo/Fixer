package com.brouken.fixer;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;

import static com.brouken.fixer.Utils.log;

public class MonitorService extends AccessibilityService {

    private static boolean switchBack = false;


    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        //log("onAccessibilityEvent()");

        //log(accessibilityEvent.toString());

        String pkg = (String) accessibilityEvent.getPackageName();
        //log(pkg);

        //if (pkg.equals("org.pocketworkstation.pckeyboard") || pkg.equals("com.google.android.inputmethod.latin"))
            //return;

        if (accessibilityEvent.isFullScreen() &&
                accessibilityEvent.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            if (pkg.equals("com.termux")) {
                Utils.changeIME(getApplicationContext(), true);
                switchBack = true;
            } else {
                if (switchBack) {
                    Utils.changeIME(getApplicationContext(), false);
                    switchBack = false;
                }
            }
        }

    }

    @Override
    public void onInterrupt() {

    }
}
