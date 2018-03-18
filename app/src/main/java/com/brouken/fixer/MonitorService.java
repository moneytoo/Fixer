package com.brouken.fixer;

import android.accessibilityservice.AccessibilityService;
import android.app.ActivityManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.view.accessibility.AccessibilityEvent;

import java.util.Collections;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

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

        String currentApp = "";

        UsageStatsManager usm = (UsageStatsManager)this.getSystemService(Context.USAGE_STATS_SERVICE);
        long time = System.currentTimeMillis();
        List<UsageStats> appList = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY,  time - 1000*1000, time);
        if (appList != null && appList.size() > 0) {
            SortedMap<Long, UsageStats> mySortedMap = new TreeMap<Long, UsageStats>();
            for (UsageStats usageStats : appList) {
                mySortedMap.put(usageStats.getLastTimeUsed(), usageStats);
            }
            if (mySortedMap != null && !mySortedMap.isEmpty()) {
                currentApp = mySortedMap.get(mySortedMap.lastKey()).getPackageName();
            }
        }

        log(currentApp);

        if (accessibilityEvent.isFullScreen() &&
                accessibilityEvent.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            if (pkg.equals("com.termux")) {
                Prefs prefs = new Prefs(getApplicationContext());
                if (prefs.isKeyboardSwitchingEnabled()) {
                    Utils.changeIME(getApplicationContext(), true);
                    switchBack = true;
                }
            } else {
                if (switchBack) {
                    Prefs prefs = new Prefs(getApplicationContext());
                    if (prefs.isKeyboardSwitchingEnabled())
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
