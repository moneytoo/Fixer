package com.brouken.fixer;

import android.app.Notification;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

public class NotificationListener extends NotificationListenerService {
    @Override
    public IBinder onBind(Intent intent) {
        return super.onBind(intent);
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        Utils.log("onNotificationPosted");
        super.onNotificationPosted(sbn);

        Notification notification = sbn.getNotification();

//        Utils.log(sbn.toString());
//        Utils.log(notification.toString());

        Bundle bundle = notification.extras;

//        for (String key : bundle.keySet()) {
//            Utils.log("key=" + key + ", val=" + bundle.get(key).toString());
//        }

        if (sbn.getPackageName().equals("android")) {
            if (bundle.containsKey("android.title")) {
                String title = bundle.getString("android.title");
                if ("Certificate authority installed".equals(title)) {
                    cancelNotification(sbn.getKey());
                }
            }
        }
    }

    @Override
    public void onListenerConnected() {
        Utils.log("onListenerConnected");
        super.onListenerConnected();
    }
}
