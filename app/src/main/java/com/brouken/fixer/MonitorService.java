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
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.media.AudioManager;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.Vibrator;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.ViewConfiguration;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityWindowInfo;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static com.brouken.fixer.Utils.log;

public class MonitorService extends AccessibilityService {

    Prefs mPrefs;

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

    private PowerConnectionReceiver mPowerConnectionReceiver;

    private ContentObserver onePlusAlertSliderObserver;

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

        if (mPowerConnectionReceiver != null)
            unregisterReceiver(mPowerConnectionReceiver);

        if (onePlusAlertSliderObserver != null)
            contentResolver.unregisterContentObserver(onePlusAlertSliderObserver);
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();

        mPrefs = new Prefs(this);

        final boolean isScreenOn = powerManager.isInteractive();
        final boolean isLockScreenOn = keyguardManager.isKeyguardLocked();

        if (mPrefs.isLongPressVolumeEnabled() && (isLockScreenOn || !isScreenOn))
//            mediaSessionManager.setOnVolumeKeyLongPressListener(this, mHandler);
            volumeKeyLongPressListener.bind();

        if (mPrefs.isLongPressVolumeEnabled()) {
            final IntentFilter intentFilter = new IntentFilter();
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
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        //log("onAccessibilityEvent()");
        //log(accessibilityEvent.toString());

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

        return super.onStartCommand(intent, flags, startId);
    }

    private void vibrate() {
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        long[] pattern = {0, 10};
        if (vibrator != null)
            vibrator.vibrate(pattern, -1);
    }

    private final Runnable mVolumeLongPress = new Runnable() {
        public void run() {
            vibrate();

            final boolean isMusicPlaying = audioManager.isMusicActive();

            if (mDownKey == KeyEvent.KEYCODE_VOLUME_DOWN) {
                if (isMusicPlaying)
                    dispatchMediaAction(KeyEvent.KEYCODE_MEDIA_PAUSE);
                else
                    dispatchMediaAction(KeyEvent.KEYCODE_MEDIA_PLAY);
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
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
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
