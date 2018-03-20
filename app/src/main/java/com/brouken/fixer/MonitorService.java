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

        String pkg = (String) accessibilityEvent.getPackageName();
        //log(pkg);

        //AccessibilityNodeInfo accessibilityNodeInfo = getRootInActiveWindow();
        //log(accessibilityNodeInfo.toString());

        List<AccessibilityWindowInfo> accessibilityWindowInfos = getWindows();
        log("windows=" + accessibilityWindowInfos.size());
        for (AccessibilityWindowInfo accessibilityWindowInfo : accessibilityWindowInfos) {
            log(accessibilityWindowInfo.toString());
        }

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

        //log("access event app: " + pkg);
        //log("usage app: " + currentApp);

        if (currentApp.equals("com.termux")) {
            Utils.changeIME(getApplicationContext(), true);
            return;
        }

        if (pkg.equals("org.pocketworkstation.pckeyboard")) {
            Utils.changeIME(getApplicationContext(), false);
        }

        /*
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
        */

    }

    @Override
    protected boolean onKeyEvent(KeyEvent event) {

        log("onKeyEvent " + event.toString());
        final int keyCode = event.getKeyCode();

        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {

            TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            int callState = telephonyManager.getCallState();

            if (callState == TelephonyManager.CALL_STATE_IDLE) {
                AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
                try {
                    audioManager.getClass().getMethod("forceVolumeControlStream", new Class[]{Integer.TYPE}).invoke(audioManager, new Object[]{ AudioManager.STREAM_MUSIC });
                } catch (Exception e) {
                    e.printStackTrace();
                }
                /*
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    buttonDownTime = SystemClock.uptimeMillis();

                    //handler.postDelayed(new Runnable() {
                    //    @Override
                    //    public void run() {
                    //        log("from handler");
                    //        volume(keyCode == KeyEvent.KEYCODE_VOLUME_UP);
                    //    }
                    //}, ViewConfiguration.getLongPressTimeout());
                }

                if (event.getAction() == KeyEvent.ACTION_UP) {
                    // Is long press
                    if (SystemClock.uptimeMillis() - buttonDownTime > ViewConfiguration.getLongPressTimeout()) {
                        //handler.removeCallbacksAndMessages(null);
                        AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);

                        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                            //audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI);
                            //audioManager.adjustSuggestedStreamVolume(AudioManager.ADJUST_RAISE, AudioManager.STREAM_MUSIC, AudioManager.FLAG_SHOW_UI);
                            mediaAction(audioManager, KeyEvent.KEYCODE_MEDIA_NEXT);
                        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                            //audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI);
                            //audioManager.adjustSuggestedStreamVolume(AudioManager.ADJUST_LOWER, AudioManager.STREAM_MUSIC, AudioManager.FLAG_SHOW_UI);
                            mediaAction(audioManager, KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE);
                        }
                    } else {
                        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                            volume(true);
                        } else {
                            volume(false);
                        }
                    }
                }
                */

                //return true;
            } else {
                AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
                try {
                    audioManager.getClass().getMethod("forceVolumeControlStream", new Class[]{Integer.TYPE}).invoke(audioManager, new Object[]{ -1 });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }

        return super.onKeyEvent(event);
    }

    private void volume(boolean increase) {
        AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        if (increase)
            audioManager.adjustSuggestedStreamVolume(AudioManager.ADJUST_RAISE, AudioManager.STREAM_MUSIC, AudioManager.FLAG_SHOW_UI);
        else
            audioManager.adjustSuggestedStreamVolume(AudioManager.ADJUST_LOWER, AudioManager.STREAM_MUSIC, AudioManager.FLAG_SHOW_UI);
    }

    private void mediaAction(AudioManager audioManager, int keyCode) {
        /*long eventTime = SystemClock.uptimeMillis() - 1;
        KeyEvent downEvent = new KeyEvent(eventTime, eventTime, KeyEvent.ACTION_DOWN, keyCode, 0);
        audioManager.dispatchMediaKeyEvent(downEvent);

        eventTime++;
        KeyEvent upEvent = new KeyEvent(eventTime, eventTime, KeyEvent.ACTION_UP,keyCode, 0);
        audioManager.dispatchMediaKeyEvent(upEvent);*/

        /*getApplicationContext().getContentResolver().registerContentObserver(android.provider.Settings.System.CONTENT_URI, true, new ContentObserver(new Handler()) {
            @Override
            public void onChange(boolean selfChange) {
                log("onChange()");
                super.onChange(selfChange);
            }
        });*/

        /*final VolumeProvider volumeProvider = new VolumeProvider(VolumeProvider.VOLUME_CONTROL_RELATIVE, 100, 50) {
            @Override
            public void onAdjustVolume(int direction) {
                log("onAdjustVolume " + direction);
                if (direction > 0) {
                    setCurrentVolume(getCurrentVolume() + 5);
                } else if (direction < 0) {
                    setCurrentVolume(getCurrentVolume() - 5);
                }
            }

            @Override
            public void onSetVolumeTo(int volume) {
                super.onSetVolumeTo(volume);
                log("onSetVolumeTo " + volume);
            }
        };
        /*
        volumeProvider.setCallback(new VolumeProvider.Callback() {
            @Override
            public void onVolumeChanged(VolumeProvider volumeProvider) {
                volumeBar.setMax(volumeProvider.getMaxVolume());
                int currentVolume = volumeProvider.getCurrentVolume();
                Log.i(TAG,"onVolumeChanged " + currentVolume);
                volumeBar.setProgress(currentVolume);
            }
        });
        */
    }

    @Override
    public void onInterrupt() {

    }
}
