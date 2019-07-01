package com.brouken.fixer;

import android.accessibilityservice.AccessibilityService;
import android.annotation.TargetApi;
import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.graphics.PixelFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.media.AudioManager;
import android.os.Build;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemClock;
import android.provider.Settings;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityWindowInfo;

import com.brouken.fixer.feature.AppBackup;
import com.brouken.fixer.feature.AppBackupReceiver;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static com.brouken.fixer.Utils.log;

public class MonitorService extends AccessibilityService {

    Prefs mPrefs;

    float gestureTapX;
    float gestureTapY;

    private WindowManager mWindowManager;
    private View mLeftView;
    private View mRightView;
    boolean gestureDistanceReached = false;


//    private MediaSessionManager mediaSessionManager;
    private PowerManager powerManager;
    private AudioManager audioManager;
    private KeyguardManager keyguardManager;
    private Handler mHandler;
    private ContentResolver contentResolver;
    private PowerManager.WakeLock wakeLock;

    private int mDownKey;
    private boolean inLongPress = false;
    private CameraManager mCameraManager;
    private String mCameraId;

    private AppBackupReceiver mAppBackupReceiver;
    private PowerConnectionReceiver mPowerConnectionReceiver;

    private ContentObserver onePlusAlertSliderObserver;
    private ContentObserver onePlusCallRecordingObserver;

    private VolumeKeyLongPressListener volumeKeyLongPressListener;;
    private final VolumeKeyLongPressListener.OnVolumeKeyLongPressListener onVolumeKeyLongPressListener = new VolumeKeyLongPressListener.OnVolumeKeyLongPressListener() {
        @Override
        public void onVolumeKeyLongPress(KeyEvent keyEvent) {
            final boolean isScreenOn = powerManager.isInteractive();
            final boolean isLockScreenOn = keyguardManager.isKeyguardLocked();

            log(keyEvent.getKeyCode() + ", " + keyEvent.getFlags() + ", " + keyEvent.isLongPress() + ", " + keyEvent.getAction());

            //if ((isMusicPlaying || mMediaNotPlayingEnable) && (!isScreenOn || mScreenOnEnable)) {
            if (!isScreenOn || isLockScreenOn) {
                if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyEvent.isLongPress()) {
                        inLongPress = true;
                    } else if (!inLongPress) {
                        inLongPress = keyEvent.isLongPress();
                        mDownKey = keyEvent.getKeyCode();


                        try {
                            Method hasCallbacks = mHandler.getClass().getDeclaredMethod("hasCallbacks", new Class<?>[]{ Runnable.class });
                            if (!((Boolean)(hasCallbacks.invoke(mHandler, mVolumeLongPress))))
                                mHandler.postDelayed(mVolumeLongPress, ViewConfiguration.getLongPressTimeout());

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

//                    if (!mHandler.hasCallbacks(mVolumeLongPress))
//                        mHandler.postDelayed(mVolumeLongPress, ViewConfiguration.getLongPressTimeout());
                    }
                } else if (keyEvent.getAction() == KeyEvent.ACTION_UP) {
                    mHandler.removeCallbacks(mVolumeLongPress);
                    inLongPress = false;
                }

                return;
            }

//        mediaSessionManager.setOnVolumeKeyLongPressListener(null, null);
//        mediaSessionManager.dispatchVolumeKeyEvent(keyEvent, audioManager.getUiSoundsStreamType(), false);  // Graylisted in Android Pie
//        mediaSessionManager.setOnVolumeKeyLongPressListener(this, mHandler);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();

        audioManager = getSystemService(AudioManager.class);
        powerManager = getSystemService(PowerManager.class);
        keyguardManager = getSystemService(KeyguardManager.class);

        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Fixer::ButtonWakeLock");
        contentResolver = getContentResolver();

//        mediaSessionManager = getSystemService(MediaSessionManager.class);
        mHandler = new Handler();

        volumeKeyLongPressListener = new VolumeKeyLongPressListener(this, onVolumeKeyLongPressListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        mediaSessionManager.setOnVolumeKeyLongPressListener(null, null);
        volumeKeyLongPressListener.unbind();

        if (mAppBackupReceiver != null)
            unregisterReceiver(mAppBackupReceiver);

        if (mPowerConnectionReceiver != null)
            unregisterReceiver(mPowerConnectionReceiver);

        if (onePlusAlertSliderObserver != null)
            contentResolver.unregisterContentObserver(onePlusAlertSliderObserver);

        if (onePlusCallRecordingObserver != null)
            contentResolver.unregisterContentObserver(onePlusCallRecordingObserver);
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();

        mPrefs = new Prefs(this);

        // TODO: Disable in full screen (?)
        startStopGestureArea();

        final boolean isScreenOn = powerManager.isInteractive();
        final boolean isLockScreenOn = keyguardManager.isKeyguardLocked();

        if (mPrefs.isLongPressVolumeEnabled() && (isLockScreenOn || !isScreenOn))
//            mediaSessionManager.setOnVolumeKeyLongPressListener(this, mHandler);
            volumeKeyLongPressListener.bind();

        if (mPrefs.isAppBackupEnabled()) {
            final IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
            intentFilter.addDataScheme("package");
            mAppBackupReceiver = new AppBackupReceiver();
            registerReceiver(mAppBackupReceiver, intentFilter);

            AppBackup.checkScheduled(this);
        }

        if (mPrefs.isPowerWakeupEnabled() || mPrefs.isLongPressVolumeEnabled()) {
            final IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(Intent.ACTION_POWER_CONNECTED);
            intentFilter.addAction(Intent.ACTION_POWER_DISCONNECTED);
            intentFilter.addAction(Intent.ACTION_SCREEN_ON);
            intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
            intentFilter.addAction(Intent.ACTION_USER_PRESENT);
            mPowerConnectionReceiver = new PowerConnectionReceiver();
            registerReceiver(mPowerConnectionReceiver, intentFilter);
        }

        if (mPrefs.isOnePlusAlertSliderActionsEnabled()) {
            onePlusAlertSliderObserver = new ContentObserver(new Handler()) {
                @Override
                public void onChange(boolean selfChange) {

                    wakeLock.acquire(3000);

                    super.onChange(selfChange);

                    final int value = Settings.Global.getInt(contentResolver, "three_Key_mode", 0);

                    log("three_Key_mode onChange: " + value);

                    switch (value) {
                        case 1:
                            switchFlashlight(getApplicationContext(), true);
                            break;
                        default:
                            switchFlashlight(getApplicationContext(), false);
                    }
                }
            };
            contentResolver.registerContentObserver(Settings.Global.getUriFor("three_Key_mode"), false, onePlusAlertSliderObserver);
        }

        if (mPrefs.isOnePlusCallRecordingEnabled()) {
            onePlusCallRecordingObserver = new ContentObserver(new Handler()) {
                @Override
                public void onChange(boolean selfChange) {

                    super.onChange(selfChange);

                    final int value = Settings.Global.getInt(contentResolver, "op_voice_recording_supported_by_mcc", 0);

                    log("op_voice_recording_supported_by_mcc onChange: " + value);

                    if (value != 1)
                        Utils.setEnableCallRecording(getApplicationContext());
                }
            };
            contentResolver.registerContentObserver(Settings.Global.getUriFor("op_voice_recording_supported_by_mcc"), false, onePlusCallRecordingObserver);
        }
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        //log("onAccessibilityEvent()");
        //log(accessibilityEvent.toString());

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
//        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
//        long[] pattern = {0, 10};
//        if (vibrator != null)
//            vibrator.vibrate(pattern, -1);
        mLeftView.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
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

    private final Runnable mVolumeLongPress = new Runnable() {
        public void run() {
            vibrate();

            final boolean isMusicPlaying = audioManager.isMusicActive();

            if (mDownKey == KeyEvent.KEYCODE_VOLUME_DOWN) {
                dispatchMediaAction(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE);
            } else if (mDownKey == KeyEvent.KEYCODE_VOLUME_UP) {
                if (isMusicPlaying)
                    dispatchMediaAction(KeyEvent.KEYCODE_MEDIA_NEXT);
                else
                    toggleFlashlight(getApplicationContext());
            }
        }
    };

    private void dispatchMediaAction(int keyCode) {
        long eventTime = SystemClock.uptimeMillis() - 1;
        final KeyEvent downEvent = new KeyEvent(eventTime, eventTime, KeyEvent.ACTION_DOWN, keyCode, 0);
        audioManager.dispatchMediaKeyEvent(downEvent);

        eventTime++;
        final KeyEvent upEvent = new KeyEvent(eventTime, eventTime, KeyEvent.ACTION_UP,keyCode, 0);
        audioManager.dispatchMediaKeyEvent(upEvent);
    }

    private void initCamera(Context context) {
        if (mCameraManager == null) {
            mCameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
            String cameraId = null;
            try {
                cameraId = getCameraId(mCameraManager);
            } catch (Throwable e) {
                return;
            } finally {
                mCameraId = cameraId;
            }
        }
    }

    private void toggleFlashlight(Context context) {
        initCamera(context);
        try {
            mCameraManager.registerTorchCallback(mTorchCallback, null);
        } catch (Throwable e) {
            return;
        }
    }

    private void switchFlashlight(Context context, boolean enable) {
        initCamera(context);
        try {
            mCameraManager.setTorchMode(mCameraId, enable);
        } catch (Throwable t) {}
    }

    static String getCameraId(CameraManager cameraManager) throws CameraAccessException {
        String[] ids = cameraManager.getCameraIdList();
        for (String id : ids) {
            CameraCharacteristics c = cameraManager.getCameraCharacteristics(id);
            Boolean flashAvailable = c.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
            Integer lensFacing = c.get(CameraCharacteristics.LENS_FACING);
            if (flashAvailable != null && flashAvailable && lensFacing != null && lensFacing == CameraCharacteristics.LENS_FACING_BACK) {
                return id;
            }
        }
        return null;
    }

    private CameraManager.TorchCallback mTorchCallback = new CameraManager.TorchCallback() {
        @TargetApi(23)
        @Override
        public void onTorchModeChanged(String cameraId, boolean enabled) {
            super.onTorchModeChanged(cameraId, enabled);
            mCameraManager.unregisterTorchCallback(mTorchCallback);

            try {
                mCameraManager.setTorchMode(mCameraId, !enabled);
            } catch (Throwable t) {}
        }
    };

    private class PowerConnectionReceiver extends BroadcastReceiver {
        long lastAction;

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case Intent.ACTION_POWER_CONNECTED:
                case Intent.ACTION_POWER_DISCONNECTED:
                    if (mPrefs.isPowerWakeupEnabled()) {
                        lastAction = System.currentTimeMillis();
                        performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN);
                    }
                    break;
                case Intent.ACTION_SCREEN_ON:
                    if (mPrefs.isPowerWakeupEnabled()) {
                        log("screen on");
                        if (System.currentTimeMillis() - lastAction < 3000) {
                            lastAction = 0;  // Prevent 2nd screen on (triggered by user) within time range
                            performGlobalAction(GLOBAL_ACTION_LOCK_SCREEN);
                        }
                    }
                    break;
                case Intent.ACTION_SCREEN_OFF:
                    if (mPrefs.isLongPressVolumeEnabled()) {
                        log("screen off");
//                        mediaSessionManager.setOnVolumeKeyLongPressListener(MonitorService.this, mHandler);
                        volumeKeyLongPressListener.bind();
                    }
                    break;
                case Intent.ACTION_USER_PRESENT:
                    if (mPrefs.isLongPressVolumeEnabled()) {
                        log("user present");
//                        mediaSessionManager.setOnVolumeKeyLongPressListener(null, null);
                        volumeKeyLongPressListener.unbind();
                    }
                    break;
            }
        }
    }

}
