package com.brouken.fixer;

import android.accessibilityservice.AccessibilityService;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Vibrator;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityWindowInfo;

import java.util.ArrayList;
import java.util.List;

import static com.brouken.fixer.Utils.log;

public class MonitorService extends AccessibilityService {

    Prefs mPrefs;
    Interruption mInterruption;

    float gestureTapX;
    float gestureTapY;

    private WindowManager mWindowManager;
    private View mLeftView;
    private View mRightView;
    boolean gestureDistanceReached = false;

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();

        mPrefs = new Prefs(this);

        if (mPrefs.isSamsungNoLedInDnDEnabled())
            mInterruption = new Interruption(this);

        // TODO: Disable in full screen (?)
        startStopGestureArea();
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        //log("onAccessibilityEvent()");
        //log(accessibilityEvent.toString());

        if (mPrefs.isSamsungNoPopupsEnabled()) {
            if (accessibilityEvent.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
                final String accessibilityEventPackageName = (String) accessibilityEvent.getPackageName();
                if (accessibilityEventPackageName.equals("com.android.settings")) {
                    // com.samsung.android.settings.bluetooth.BluetoothScanDialog
                    if (accessibilityEvent.getClassName().toString().equals("android.app.Dialog")) {
                        List<CharSequence> texts = accessibilityEvent.getText();
                        if (texts.get(0) != null && texts.get(0).toString().toLowerCase().equals("bluetooth")) {
                            clickButton("android:id/button1");
                        }
                    } else if (accessibilityEvent.getClassName().toString().equals("com.samsung.android.settings.wifi.WifiPickerDialog")) {
                        clickButton("com.android.settings:id/wifi_picker_dialog_cancel");
                    }
                } else if (accessibilityEventPackageName.equals("com.samsung.android.MtpApplication")) {
                    if (accessibilityEvent.getClassName().toString().equals("com.samsung.android.MtpApplication.USBConnection")) {
                        clickButton("android:id/button1");
                    }
                }
            }
        }

        if (mPrefs.isGMSNoLocationEnabled()) {
            if (accessibilityEvent.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
                if (accessibilityEvent.getClassName().toString().equals("com.google.android.location.settings.LocationSettingsCheckerActivity")) {
                    clickButton("android:id/button2");
                } else if (accessibilityEvent.getClassName().toString().equals("com.google.android.location.network.ConfirmAlertActivity")) {
                    clickButton("android:id/button1");
                }
            }
        }

        if (mPrefs.isKeyboardSwitchingEnabled()) {
            if (accessibilityEvent.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
                final String accessibilityEventPackageName = (String) accessibilityEvent.getPackageName();

                final List<String> visibleApps = new ArrayList<>();

                List<AccessibilityWindowInfo> accessibilityWindowInfos = getWindows();
                //log("windows=" + accessibilityWindowInfos.size());
                for (AccessibilityWindowInfo accessibilityWindowInfo : accessibilityWindowInfos) {
                    //log(accessibilityWindowInfo.toString());

                    AccessibilityNodeInfo accessibilityNodeInfo = accessibilityWindowInfo.getRoot();
                    if (accessibilityNodeInfo != null) {
                        visibleApps.add(accessibilityNodeInfo.getPackageName().toString());
                    }
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

        if (mPrefs.isAutoSelectClientCertificateEnabled()) {
            final String accessibilityEventPackageName = (String) accessibilityEvent.getPackageName();
            if (accessibilityEventPackageName.equals("com.android.keychain")) {
                if (accessibilityEvent.getClassName().toString().equals("android.app.AlertDialog")) {
                    clickButton("android:id/button1");
                }
            }
        }
    }

    void clickButton(final String viewId) {
        final List<AccessibilityNodeInfo> nodeInfos = getRootInActiveWindow().findAccessibilityNodeInfosByViewId(viewId);
        for (AccessibilityNodeInfo nodeInfo : nodeInfos)
            nodeInfo.performAction(AccessibilityNodeInfo.AccessibilityAction.ACTION_CLICK.getId());
    }

    @Override
    public void onInterrupt() {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        log("onStartCommand");

        mPrefs = new Prefs(this);

        startStopGestureArea();

        return super.onStartCommand(intent, flags, startId);
    }

    private void startGestureArea() {
        mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        mLeftView = new View(this);
        mRightView = new View(this);

        // Colored for debug
        //leftView.setBackgroundColor(Color.argb(0x40, 0xff, 0x00, 0x00));
        //rightView.setBackgroundColor(Color.argb(0x40, 0xff, 0x00, 0x00));

        WindowManager.LayoutParams params= new WindowManager.LayoutParams(
                getOverlayWidth(),
                (int) (200 * getResources().getDisplayMetrics().density), //WindowManager.LayoutParams.WRAP_CONTENT,
                (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_PHONE),
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.CENTER_VERTICAL | Gravity.LEFT;

        mLeftView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                handleEvent(motionEvent);
                return false;
            }
        });

        mRightView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                handleEvent(motionEvent);
                return false;
            }
        });

        mWindowManager.addView(mLeftView, params);

        params.gravity = Gravity.CENTER_VERTICAL | Gravity.RIGHT;
        mWindowManager.addView(mRightView, params);
    }

    private void stopGestureArea() {
        if (mWindowManager != null) {
            if (mLeftView != null)
                mWindowManager.removeView(mLeftView);
            if (mRightView != null)
                mWindowManager.removeView(mRightView);
        }
    }

    private void startStopGestureArea() {
        if (mPrefs.isSideScreenGesturesEnabled())
            startGestureArea();
        else
            stopGestureArea();
    }

    private void handleEvent(MotionEvent motionEvent) {
        final int action = motionEvent.getAction();

        if (action == MotionEvent.ACTION_DOWN) {
            gestureDistanceReached = false;
            gestureTapX = motionEvent.getX();
            gestureTapY = motionEvent.getY();
            //vibrate();
        } else if (action == MotionEvent.ACTION_UP) {
            if (gestureDistanceReached) {
                final double distance = getDistance(motionEvent.getX(), motionEvent.getY(), gestureTapX, gestureTapY);
                if (pxToDp((float) distance) >= 40) {
                    final double degree = getAbsDegree(gestureTapX, gestureTapY, motionEvent.getX(), motionEvent.getY());
                    runAction(degree);
                }
            }
        } else if (action == MotionEvent.ACTION_MOVE) {
            final double distance = getDistance(motionEvent.getX(), motionEvent.getY(), gestureTapX, gestureTapY);
            final float distanceDp = pxToDp((float)distance);

            if (!gestureDistanceReached) {
                if (distanceDp >= 80) {
                    gestureDistanceReached = true;
                    vibrate();
                }
            } else {
                if (distanceDp < 40)
                    gestureDistanceReached = false;
            }
        }
    }

    private void runAction(double degree) {
        if (degree > 120)
            performGlobalAction(AccessibilityService.GLOBAL_ACTION_RECENTS);
        else if (degree < 60)
            performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
        else
            performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME);
    }

    private float pxToDp(float px) {
        final float density = getResources().getDisplayMetrics().density;
        return px / density;
    }

    private double getDistance(double x1, double y1, double x2, double y2) {
        return Math.hypot(y2 - y1, x2 - x1);
    }

    private double getAbsDegree(double x1, double y1, double x2, double y2) {
        return Math.abs(Math.toDegrees(Math.atan2(x2 - x1, y2 - y1)));
    }

    // https://android.googlesource.com/platform/frameworks/support/+/2d9e33a/v4/java/android/support/v4/widget/ViewDragHelper.java#387
    private int getOverlayWidth() {
        final int EDGE_SIZE = 20; // dp
        final float density = getResources().getDisplayMetrics().density;
        return (int) (EDGE_SIZE * density + 0.5f);
    }

    private void vibrate() {
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        long[] pattern = {0, 10};
        if (vibrator != null)
            vibrator.vibrate(pattern, -1);
    }

    void dumpChildren(AccessibilityNodeInfo nodeInfo) {
        log(nodeInfo.toString());

        final int count = nodeInfo.getChildCount();

        for (int i = 0; i < count; i++) {
            final AccessibilityNodeInfo child = nodeInfo.getChild(i);

            if (child == null)
                continue;

            dumpChildren(child);
        }
    }
}
