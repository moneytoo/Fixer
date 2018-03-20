package com.brouken.fixer;

import android.accessibilityservice.AccessibilityService;
import android.app.ActivityManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.media.AudioManager;
import android.os.Handler;
import android.os.SystemClock;
import android.telephony.TelephonyManager;
import android.view.KeyEvent;
import android.view.ViewConfiguration;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityWindowInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import static com.brouken.fixer.Utils.log;

public class MonitorService extends AccessibilityService {

    private static boolean switchBack = false;

    private Handler handler = new Handler();
    private long buttonDownTime;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        //log("onAccessibilityEvent()");

        //log(accessibilityEvent.toString());

        //AccessibilityNodeInfo accessibilityNodeInfo = getRootInActiveWindow();
        //log(accessibilityNodeInfo.toString());

        if (accessibilityEvent.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            String accessibilityEventPackageName = (String) accessibilityEvent.getPackageName();
            //log(accessibilityEventPackageName);

            List<String> visibleApps = new ArrayList<>();

            List<AccessibilityWindowInfo> accessibilityWindowInfos = getWindows();
            log("windows=" + accessibilityWindowInfos.size());
            for (AccessibilityWindowInfo accessibilityWindowInfo : accessibilityWindowInfos) {
                log(accessibilityWindowInfo.toString());

                AccessibilityNodeInfo accessibilityNodeInfo = accessibilityWindowInfo.getRoot();
                if (accessibilityNodeInfo != null) {
                    log(accessibilityNodeInfo.getPackageName().toString());
                    visibleApps.add(accessibilityNodeInfo.getPackageName().toString());
                }

                int childCount = accessibilityWindowInfo.getChildCount();
                if (childCount > 0)
                    log("childCount=" + childCount);
            }

            if (visibleApps.contains("com.termux")) {
                Utils.changeIME(getApplicationContext(), true);
                return;
            }

            if (accessibilityEventPackageName.equals("org.pocketworkstation.pckeyboard")) {
                Utils.changeIME(getApplicationContext(), false);
            }
        }

    }

    @Override
    protected boolean onKeyEvent(KeyEvent event) {

        log("onKeyEvent " + event.toString());
        final int keyCode = event.getKeyCode();

        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {

            AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
            TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            int callState = telephonyManager.getCallState();

            Object stream = -1;
            if (callState == TelephonyManager.CALL_STATE_IDLE)
                stream = AudioManager.STREAM_MUSIC;

            try {
                audioManager.getClass().getMethod("forceVolumeControlStream", new Class[]{Integer.TYPE}).invoke(audioManager, new Object[]{ stream });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return super.onKeyEvent(event);
    }

    @Override
    public void onInterrupt() {

    }
}
