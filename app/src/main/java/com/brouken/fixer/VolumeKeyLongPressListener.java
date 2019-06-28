package com.brouken.fixer;

// From https://github.com/iamnp/TrackSkipper2/blob/master/app/src/main/java/com/trackskipper2/TrackSkipperService.java

import android.content.Context;
import android.media.session.MediaSessionManager;
import android.view.KeyEvent;

import java.lang.reflect.Method;

public class VolumeKeyLongPressListener {

    private Method setOnVolumeKeyLongPressListener;
    private MediaSessionManager mediaSessionManager;
    private Object proxy;
    private OnVolumeKeyLongPressListener listener;
    private final java.lang.reflect.InvocationHandler invocationHandler = new java.lang.reflect.InvocationHandler() {

        @Override
        public Object invoke(Object proxy, java.lang.reflect.Method method, Object[] args) throws java.lang.Throwable {
            if (method.getName().equals("onVolumeKeyLongPress")) {
                listener.onVolumeKeyLongPress((KeyEvent) args[0]);
            }
            return null;
        }
    };

    VolumeKeyLongPressListener(Context context, OnVolumeKeyLongPressListener listener) {
        setOnVolumeKeyLongPressListener = getSetOnVolumeKeyLongPressListener();
        mediaSessionManager = (MediaSessionManager) context.getSystemService(Context.MEDIA_SESSION_SERVICE);
        this.listener = listener;
        proxy = java.lang.reflect.Proxy.newProxyInstance(
                OnVolumeKeyLongPressListener.class.getClassLoader(),
                new java.lang.Class[]{getOnVolumeKeyLongPressListener()},
                invocationHandler);
    }

    private static Method getSetOnVolumeKeyLongPressListener() {
        final Class mediaSessionManagerClassClass = MediaSessionManager.class;
        final Method[] methods = mediaSessionManagerClassClass.getMethods();
        for (Method method : methods) {
            if (method.getName().equals("setOnVolumeKeyLongPressListener")) {
                return method;
            }
        }
        return null;
    }

    private static Class<?> getOnVolumeKeyLongPressListener() {
        Class<?> onVolumeKeyLongPressListener = null;
        try {
            onVolumeKeyLongPressListener = Class.forName("android.media.session.MediaSessionManager$OnVolumeKeyLongPressListener");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return onVolumeKeyLongPressListener;
    }

    void bind() {
        try {
            setOnVolumeKeyLongPressListener.invoke(mediaSessionManager, proxy, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void unbind() {
        try {
            setOnVolumeKeyLongPressListener.invoke(mediaSessionManager, null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    interface OnVolumeKeyLongPressListener {
        void onVolumeKeyLongPress(KeyEvent keyEvent);
    }
}
